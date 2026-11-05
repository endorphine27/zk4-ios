package com.cinebox.app.features.cast

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import java.net.URI

/**
 * UPnP / DLNA media renderer client (SSDP discovery + AVTransport SOAP), no external dependencies.
 * Works with TVs such as LG webOS, Samsung, Sony and any DLNA renderer.
 */
internal object DlnaCastBackend : CastBackend {
    private const val AVTRANSPORT = "urn:schemas-upnp-org:service:AVTransport:1"
    private const val SSDP_ADDRESS = "239.255.255.250"
    private const val SSDP_PORT = 1900
    private const val STALE_DEVICE_MS = 90_000L

    private class Renderer(val device: CastDevice, val controlUrl: String)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val renderers = java.util.concurrent.ConcurrentHashMap<String, Renderer>()
    private val seenAt = java.util.concurrent.ConcurrentHashMap<String, Long>()
    @Volatile private var activeDeviceId: String? = null
    private val deviceList = MutableStateFlow<List<CastDevice>>(emptyList())
    private var discoveryJob: Job? = null
    @Volatile private var seekJob: Job? = null
    private var proxyExecutor: java.util.concurrent.ExecutorService? = null

    override val devices: StateFlow<List<CastDevice>> = deviceList.asStateFlow()

    override fun startDiscovery() {
        if (discoveryJob?.isActive == true) return
        discoveryJob = scope.launch {
            while (isActive) {
                runCatching { searchOnce() }
                delay(8_000)
            }
        }
    }

    override fun stopDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = null
    }

    private suspend fun searchOnce() = withContext(Dispatchers.IO) {
        val request = (
            "M-SEARCH * HTTP/1.1\r\nHOST: $SSDP_ADDRESS:$SSDP_PORT\r\nMAN: \"ssdp:discover\"\r\n" +
                "MX: 2\r\nST: $AVTRANSPORT\r\n\r\n"
            ).toByteArray()
        val locations = linkedSetOf<String>()
        val group = InetAddress.getByName(SSDP_ADDRESS)
        val interfaces = runCatching {
            NetworkInterface.getNetworkInterfaces().toList().filter { nic ->
                // SSDP here is IPv4 only: skip VPN/virtual/IPv6-only interfaces, each would just add a timeout wait.
                nic.isUp && !nic.isLoopback && nic.supportsMulticast() &&
                    nic.interfaceAddresses.any { it.address is java.net.Inet4Address }
            }
        }.getOrDefault(emptyList())
        for (nic in interfaces) {
            runCatching {
                MulticastSocket(null).use { socket ->
                    socket.reuseAddress = true
                    socket.bind(InetSocketAddress(0))
                    socket.networkInterface = nic
                    socket.soTimeout = 1_500
                    socket.timeToLive = 4
                    repeat(2) { socket.send(DatagramPacket(request, request.size, group, SSDP_PORT)) }
                    val buffer = ByteArray(4096)
                    val deadline = System.currentTimeMillis() + 3_000
                    while (System.currentTimeMillis() < deadline) {
                        try {
                            val packet = DatagramPacket(buffer, buffer.size)
                            socket.receive(packet)
                            val text = String(packet.data, 0, packet.length)
                            Regex("(?im)^location:\\s*(\\S+)").find(text)?.groupValues?.get(1)?.let(locations::add)
                        } catch (_: SocketTimeoutException) {
                            break
                        }
                    }
                }
            }
        }
        val now = System.currentTimeMillis()
        for (location in locations) {
            seenAt[location] = now
            if (renderers.containsKey(location)) continue
            runCatching { describe(location) }.getOrNull()?.let { renderer ->
                renderers[renderer.device.id] = renderer
            }
        }
        // A TV that was switched off must not stay in the list forever (casting to it only times out).
        renderers.keys.removeAll { id ->
            id != activeDeviceId && (seenAt[id] ?: 0L) < now - STALE_DEVICE_MS
        }
        seenAt.keys.removeAll { !renderers.containsKey(it) }
        deviceList.value = renderers.values.map { it.device }.sortedBy { it.name.lowercase() }
    }

    private fun describe(location: String): Renderer? {
        val xml = httpGet(location)
        val name = Regex("<friendlyName>(.*?)</friendlyName>", RegexOption.DOT_MATCHES_ALL)
            .find(xml)?.groupValues?.get(1)?.let(::xmlUnescape)?.trim().orEmpty()
        val serviceBlock = Regex("<service>.*?</service>", RegexOption.DOT_MATCHES_ALL).findAll(xml)
            .map { it.value }.firstOrNull { it.contains("AVTransport") } ?: return null
        val control = Regex("<controlURL>(.*?)</controlURL>").find(serviceBlock)?.groupValues?.get(1)?.trim()
            ?: return null
        val base = Regex("<URLBase>(.*?)</URLBase>").find(xml)?.groupValues?.get(1)?.trim()
        val controlUrl = URI(base ?: location).resolve(control).toString()
        return Renderer(CastDevice(id = location, name = name.ifBlank { URI(location).host }, protocol = "DLNA"), controlUrl)
    }

    override suspend fun cast(
        device: CastDevice,
        url: String,
        title: String,
        startPositionMs: Long,
        subtitleUrl: String?,
    ): Result<Unit> =
        runCatching {
            val renderer = renderers[device.id] ?: error("Dispozitiv indisponibil")
            activeDeviceId = device.id
            val upstreamUrl = withContext(Dispatchers.IO) { resolveRedirects(url) }
            // The TV fetches the stream from this app over plain local HTTP: many renderers cannot
            // open https links, redirects or long signed URLs directly.
            // A resume-seek from an earlier cast must not move this new session.
            seekJob?.cancel()
            // The TV may still hold a previous session ("Transition not available"): stop it first.
            runCatching { soap(renderer, "Stop", "") }
            delay(400)
            proxySubtitleUpstream = subtitleUrl.orEmpty()
            val finalUrl = withContext(Dispatchers.IO) { startProxy(upstreamUrl, renderer) }
            val subtitleProxyUrl = if (subtitleUrl.isNullOrBlank()) null else finalUrl.substringBeforeLast('/') + "/subtitle.srt"
            proxySubtitlePublicUrl = subtitleProxyUrl.orEmpty()
            val didl = buildDidl(title, finalUrl, subtitleProxyUrl)
            try {
                soap(
                    renderer,
                    "SetAVTransportURI",
                    "<CurrentURI>${xmlEscape(finalUrl)}</CurrentURI><CurrentURIMetaData>${xmlEscape(didl)}</CurrentURIMetaData>",
                    readTimeoutMs = 30_000,
                )
            } catch (e: Exception) {
                error("${e.message} | $finalUrl | cereri de la TV: ${proxyHits.get()}")
            }
            soap(renderer, "Play", "<Speed>1</Speed>")
            if (startPositionMs > 5_000L) {
                // The TV only accepts a seek once it is really playing, so do it in the background and
                // check the reported position; retry with both REL_TIME and ABS_TIME.
                seekJob = scope.launch { seekWhenReady(renderer, startPositionMs) }
            }
        }

    private suspend fun seekWhenReady(renderer: Renderer, targetMs: Long) {
        val target = formatTime(targetMs)
        for (attempt in 0 until 20) {
            delay(1_500)
            val state = runCatching {
                Regex("<CurrentTransportState>(.*?)</CurrentTransportState>")
                    .find(soap(renderer, "GetTransportInfo", ""))?.groupValues?.get(1)
            }.getOrNull()
            if (state != "PLAYING") continue
            val unit = if (attempt % 2 == 0) "REL_TIME" else "ABS_TIME"
            runCatching { soap(renderer, "Seek", "<Unit>$unit</Unit><Target>$target</Target>") }
            delay(1_500)
            val position = runCatching {
                Regex("<RelTime>(.*?)</RelTime>").find(soap(renderer, "GetPositionInfo", ""))?.groupValues?.get(1)
            }.getOrNull()?.let(::parseTime)
            if (position != null && position >= targetMs - 15_000L) return
        }
    }

    override suspend fun pause(device: CastDevice) = control(device, "Pause", "")
    override suspend fun resume(device: CastDevice) = control(device, "Play", "<Speed>1</Speed>")
    override suspend fun stop(device: CastDevice): Result<Unit> {
        seekJob?.cancel()
        seekJob = null
        activeDeviceId = null
        val result = control(device, "Stop", "")
        stopProxy()
        return result
    }
    override suspend fun seek(device: CastDevice, positionMs: Long) =
        control(device, "Seek", "<Unit>REL_TIME</Unit><Target>${formatTime(positionMs)}</Target>")

    override suspend fun position(device: CastDevice): Long? = runCatching {
        val renderer = renderers[device.id] ?: return null
        val response = soap(renderer, "GetPositionInfo", "")
        Regex("<RelTime>(.*?)</RelTime>").find(response)?.groupValues?.get(1)?.let(::parseTime)
    }.getOrNull()

    private suspend fun control(device: CastDevice, action: String, args: String): Result<Unit> = runCatching {
        val renderer = renderers[device.id] ?: error("Dispozitiv indisponibil")
        soap(renderer, action, args)
        Unit
    }

    private suspend fun soap(renderer: Renderer, action: String, args: String, readTimeoutMs: Int = 8_000): String = withContext(Dispatchers.IO) {
        val body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><s:Body>" +
            "<u:$action xmlns:u=\"$AVTRANSPORT\"><InstanceID>0</InstanceID>$args</u:$action></s:Body></s:Envelope>"
        val connection = URI(renderer.controlUrl).toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 4_000
            connection.readTimeout = readTimeoutMs
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"")
            connection.setRequestProperty("SOAPACTION", "\"$AVTRANSPORT#$action\"")
            connection.outputStream.use { it.write(body.toByteArray()) }
            val code = connection.responseCode
            val text = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.readText().orEmpty()
            if (code !in 200..299) {
                val detail = Regex("<errorDescription>(.*?)</errorDescription>").find(text)?.groupValues?.get(1)
                error("$action: HTTP $code${detail?.let { " ($it)" }.orEmpty()}")
            }
            text
        } finally {
            connection.disconnect()
        }
    }

    private fun httpGet(url: String): String {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 3_000
            connection.readTimeout = 4_000
            connection.inputStream.bufferedReader().readText()
        } finally {
            connection.disconnect()
        }
    }

    private var proxy: com.sun.net.httpserver.HttpServer? = null
    @Volatile private var proxyUpstream: String = ""
    private val proxyHits = java.util.concurrent.atomic.AtomicInteger(0)
    @Volatile private var proxyPublicUrl: String = ""
    @Volatile private var proxySubtitleUpstream: String = ""
    @Volatile private var proxySubtitlePublicUrl: String = ""

    private var proxyBindIp: String? = null

    private fun startProxy(upstream: String, renderer: Renderer): String {
        proxyUpstream = upstream
        val host = URI(renderer.controlUrl).host
        val localIp = java.net.DatagramSocket().use { socket ->
            socket.connect(InetAddress.getByName(host), 9)
            socket.localAddress.hostAddress
        }
        // Listen only on the address facing the TV (not on every interface), and rebind if the network changed.
        if (proxy != null && proxyBindIp != localIp) stopProxy()
        val server = proxy ?: com.sun.net.httpserver.HttpServer.create(
            InetSocketAddress(InetAddress.getByName(localIp), 0),
            0,
        ).also { created ->
            // Daemon threads: they must never keep the app process alive after quit.
            val pool = java.util.concurrent.Executors.newCachedThreadPool { runnable ->
                Thread(runnable, "cast-proxy").apply { isDaemon = true }
            }
            proxyExecutor = pool
            created.executor = pool
            created.createContext("/") { exchange -> serveProxy(exchange) }
            created.createContext("/subtitle.srt") { exchange -> serveSubtitle(exchange) }
            created.start()
            proxy = created
            proxyBindIp = localIp
        }
        val ext = runCatching { URI(upstream).path.substringAfterLast('.', "mp4").lowercase() }
            .getOrDefault("mp4").takeIf { it.length in 2..4 } ?: "mp4"
        proxyHits.set(0)
        proxyPublicUrl = "http://$localIp:${server.address.port}/video.$ext"
        return proxyPublicUrl
    }

    private fun stopProxy() {
        proxy?.stop(0)
        proxy = null
        proxyExecutor?.shutdownNow()
        proxyExecutor = null
    }

    /** Serves the chosen subtitle as UTF-8 SRT so renderers can load it as an external subtitle. */
    private fun serveSubtitle(exchange: com.sun.net.httpserver.HttpExchange) {
        var upstream: HttpURLConnection? = null
        try {
            upstream = URI(proxySubtitleUpstream).toURL().openConnection() as HttpURLConnection
            upstream.instanceFollowRedirects = true
            upstream.connectTimeout = 10_000
            upstream.readTimeout = 20_000
            val raw = upstream.inputStream.use { it.readBytes() }
            val utf8 = runCatching {
                Charsets.UTF_8.newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .decode(java.nio.ByteBuffer.wrap(raw)).toString()
            }.getOrElse { String(raw, charset("windows-1250")) }
            val text = utf8.removePrefix("\uFEFF")
            val body = if (text.trimStart().startsWith("WEBVTT")) vttToSrt(text).toByteArray() else text.toByteArray()
            exchange.responseHeaders.add("Content-Type", "text/srt; charset=utf-8")
            exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
            if (exchange.requestMethod.equals("HEAD", ignoreCase = true)) {
                exchange.responseHeaders.add("Content-Length", body.size.toString())
                exchange.sendResponseHeaders(200, -1)
            } else {
                exchange.sendResponseHeaders(200, body.size.toLong())
                exchange.responseBody.use { it.write(body) }
            }
        } catch (_: Exception) {
            runCatching { exchange.sendResponseHeaders(404, -1) }
        } finally {
            runCatching { upstream?.disconnect() }
            exchange.close()
        }
    }

    private fun vttToSrt(vtt: String): String {
        var index = 0
        val out = StringBuilder()
        vtt.replace("\r\n", "\n").split("\n\n").forEach { block ->
            val lines = block.lines().filter { it.isNotBlank() }
            val timeIdx = lines.indexOfFirst { it.contains("-->") }
            if (timeIdx < 0) return@forEach
            index++
            out.append(index).append('\n')
                .append(lines[timeIdx].replace('.', ',').substringBefore(" align").substringBefore(" line").substringBefore(" position")).append('\n')
            lines.drop(timeIdx + 1).forEach { out.append(it).append('\n') }
            out.append('\n')
        }
        return out.toString()
    }

    private fun serveProxy(exchange: com.sun.net.httpserver.HttpExchange) {
        proxyHits.incrementAndGet()
        var upstream: HttpURLConnection? = null
        try {
            val isHead = exchange.requestMethod.equals("HEAD", ignoreCase = true)
            upstream = URI(proxyUpstream).toURL().openConnection() as HttpURLConnection
            upstream.instanceFollowRedirects = true
            upstream.connectTimeout = 10_000
            upstream.readTimeout = 30_000
            val range = exchange.requestHeaders.getFirst("Range")
            upstream.setRequestProperty("Range", if (isHead) "bytes=0-0" else range ?: "bytes=0-")
            val code = upstream.responseCode
            val contentRange = upstream.getHeaderField("Content-Range")
            val total = contentRange?.substringAfter('/')?.toLongOrNull()
            val headers = exchange.responseHeaders
            headers.add("Content-Type", mimeFor("http://x/video.${exchange.requestURI.path.substringAfterLast('.', "mp4")}"))
            headers.add("Accept-Ranges", "bytes")
            headers.add("transferMode.dlna.org", "Streaming")
            if (proxySubtitlePublicUrl.isNotBlank()) headers.add("CaptionInfo.sec", proxySubtitlePublicUrl)
            headers.add("contentFeatures.dlna.org", "DLNA.ORG_OP=01;DLNA.ORG_CI=0")
            if (isHead) {
                val length = total ?: upstream.contentLengthLong
                if (length >= 0) headers.add("Content-Length", length.toString())
                exchange.sendResponseHeaders(200, -1)
                return
            }
            if (contentRange != null && range != null) headers.add("Content-Range", contentRange)
            val status = if (code == 206 && range != null) 206 else 200
            val length = upstream.contentLengthLong
            exchange.sendResponseHeaders(status, if (length >= 0) length else 0)
            upstream.inputStream.use { input -> exchange.responseBody.use { input.copyTo(it) } }
        } catch (_: Exception) {
            // The TV closing the connection (seeking, stopping) is normal.
        } finally {
            runCatching { upstream?.disconnect() }
            exchange.close()
        }
    }

    /** Follows HTTP redirects so the TV gets the final direct link (many renderers do not follow them). */
    private fun resolveRedirects(url: String): String {
        var current = url
        repeat(6) {
            val connection = URI(current).toURL().openConnection() as HttpURLConnection
            try {
                connection.instanceFollowRedirects = false
                connection.connectTimeout = 6_000
                connection.readTimeout = 8_000
                connection.setRequestProperty("Range", "bytes=0-0")
                val code = connection.responseCode
                val location = connection.getHeaderField("Location")
                if (code in 300..399 && !location.isNullOrBlank()) {
                    current = URI(current).resolve(location).toString()
                } else {
                    return current
                }
            } catch (_: Exception) {
                return current
            } finally {
                connection.disconnect()
            }
        }
        return current
    }

    private fun mimeFor(url: String): String {
        val path = runCatching { URI(url).path.lowercase() }.getOrDefault("")
        return when {
            path.endsWith(".mkv") -> "video/x-matroska"
            path.endsWith(".avi") -> "video/x-msvideo"
            path.endsWith(".ts") || path.endsWith(".m2ts") -> "video/mpeg"
            path.endsWith(".webm") -> "video/webm"
            else -> "video/mp4"
        }
    }

    private fun buildDidl(title: String, url: String, subtitleUrl: String?): String {
        val sub = subtitleUrl?.let { xmlEscape(it) }
        return "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" " +
            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" " +
            "xmlns:sec=\"http://www.sec.co.kr/\" xmlns:pv=\"http://www.pv.com/pvns/\">" +
            "<item id=\"0\" parentID=\"-1\" restricted=\"1\"><dc:title>${xmlEscape(title)}</dc:title>" +
            "<upnp:class>object.item.videoItem</upnp:class>" +
            "<res protocolInfo=\"http-get:*:${mimeFor(url)}:*\">${xmlEscape(url)}</res>" +
            (if (sub != null) {
                "<res protocolInfo=\"http-get:*:text/srt:*\">$sub</res>" +
                    "<sec:CaptionInfoEx sec:type=\"srt\">$sub</sec:CaptionInfoEx>" +
                    "<sec:CaptionInfo sec:type=\"srt\">$sub</sec:CaptionInfo>" +
                    "<pv:subtitleFileUri>$sub</pv:subtitleFileUri><pv:subtitleFileType>srt</pv:subtitleFileType>"
            } else {
                ""
            }) +
            "</item></DIDL-Lite>"
    }

    private fun xmlEscape(value: String): String =
        value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    private fun xmlUnescape(value: String): String =
        value.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&")

    private fun formatTime(ms: Long): String {
        val total = (ms / 1000).coerceAtLeast(0)
        return "%d:%02d:%02d".format(total / 3600, (total % 3600) / 60, total % 60)
    }

    private fun parseTime(value: String): Long? {
        val parts = value.substringBefore('.').split(':').mapNotNull { it.toLongOrNull() }
        if (parts.size != 3) return null
        return (parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000
    }
}
