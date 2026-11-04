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

    private class Renderer(val device: CastDevice, val controlUrl: String)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val renderers = mutableMapOf<String, Renderer>()
    private val deviceList = MutableStateFlow<List<CastDevice>>(emptyList())
    private var discoveryJob: Job? = null

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
            NetworkInterface.getNetworkInterfaces().toList().filter { it.isUp && !it.isLoopback && it.supportsMulticast() }
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
        for (location in locations) {
            if (renderers.values.any { it.device.id == location }) continue
            runCatching { describe(location) }.getOrNull()?.let { renderer ->
                renderers[renderer.device.id] = renderer
            }
        }
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

    override suspend fun cast(device: CastDevice, url: String, title: String, startPositionMs: Long): Result<Unit> =
        runCatching {
            val renderer = renderers[device.id] ?: error("Dispozitiv indisponibil")
            val didl = buildDidl(title, url)
            soap(
                renderer,
                "SetAVTransportURI",
                "<CurrentURI>${xmlEscape(url)}</CurrentURI><CurrentURIMetaData>${xmlEscape(didl)}</CurrentURIMetaData>",
            )
            soap(renderer, "Play", "<Speed>1</Speed>")
            if (startPositionMs > 5_000L) {
                // Many renderers only accept Seek after playback has begun.
                repeat(6) {
                    delay(1_000)
                    if (runCatching { soap(renderer, "Seek", "<Unit>REL_TIME</Unit><Target>${formatTime(startPositionMs)}</Target>") }.isSuccess) {
                        return@runCatching
                    }
                }
            }
        }

    override suspend fun pause(device: CastDevice) = control(device, "Pause", "")
    override suspend fun resume(device: CastDevice) = control(device, "Play", "<Speed>1</Speed>")
    override suspend fun stop(device: CastDevice) = control(device, "Stop", "")
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

    private suspend fun soap(renderer: Renderer, action: String, args: String): String = withContext(Dispatchers.IO) {
        val body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><s:Body>" +
            "<u:$action xmlns:u=\"$AVTRANSPORT\"><InstanceID>0</InstanceID>$args</u:$action></s:Body></s:Envelope>"
        val connection = URI(renderer.controlUrl).toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 4_000
            connection.readTimeout = 8_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"")
            connection.setRequestProperty("SOAPACTION", "\"$AVTRANSPORT#$action\"")
            connection.outputStream.use { it.write(body.toByteArray()) }
            val code = connection.responseCode
            val text = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.readText().orEmpty()
            if (code !in 200..299) error("$action: HTTP $code")
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

    private fun buildDidl(title: String, url: String): String =
        "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" " +
            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\">" +
            "<item id=\"0\" parentID=\"-1\" restricted=\"1\"><dc:title>${xmlEscape(title)}</dc:title>" +
            "<upnp:class>object.item.videoItem</upnp:class>" +
            "<res protocolInfo=\"http-get:*:video/mp4:*\">${xmlEscape(url)}</res></item></DIDL-Lite>"

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
