package com.cinebox.app.features.streams

/** Ranks streams by video quality first, so the list can lead with the best picture. */
internal object StreamQuality {
    private val resolution2160 = Regex("""2160p|\b4k\b|\buhd\b""")
    private val dolbyVision = Regex("""dolby[ ._-]?vision|\bdv\b|\bdovi\b""")
    private val hdr10Plus = Regex("""hdr10\+""")
    private val hdr = Regex("""\bhdr""")
    private val remux = Regex("""remux""")
    private val bluray = Regex("""blu[ ._-]?ray|\bbd(rip)?\b|\buhd\b""")
    private val webDl = Regex("""web[ ._-]?dl""")
    private val webRip = Regex("""web[ ._-]?rip""")
    private val av1 = Regex("""\bav1\b""")
    private val hevc = Regex("""hevc|h[ .]?265|x265""")
    private val atmos = Regex("""atmos""")
    private val losslessAudio = Regex("""truehd|dts[ -]?(x|hd)""")
    private val dolbyDigitalPlus = Regex("""dd\+|e-?ac-?3|ddp""")

    fun score(stream: StreamItem): Int {
        val text = listOfNotNull(stream.name, stream.title, stream.description).joinToString(" ").lowercase()
        var score = when {
            resolution2160.containsMatchIn(text) -> 4000
            Regex("""1440p""").containsMatchIn(text) -> 3000
            Regex("""1080p""").containsMatchIn(text) -> 2000
            Regex("""720p""").containsMatchIn(text) -> 1000
            else -> 0
        }
        score += when {
            dolbyVision.containsMatchIn(text) && (hdr10Plus.containsMatchIn(text) || hdr.containsMatchIn(text)) -> 320
            dolbyVision.containsMatchIn(text) -> 300
            hdr10Plus.containsMatchIn(text) -> 250
            hdr.containsMatchIn(text) -> 200
            else -> 0
        }
        score += when {
            remux.containsMatchIn(text) -> 150
            bluray.containsMatchIn(text) -> 100
            webDl.containsMatchIn(text) -> 70
            webRip.containsMatchIn(text) -> 40
            else -> 0
        }
        if (av1.containsMatchIn(text) || hevc.containsMatchIn(text)) score += 10
        // Audio only breaks ties between streams with the same picture.
        score += when {
            atmos.containsMatchIn(text) -> 6
            losslessAudio.containsMatchIn(text) -> 4
            dolbyDigitalPlus.containsMatchIn(text) -> 2
            else -> 0
        }
        // A cached debrid link starts right away.
        if (listOf("[rd+]", "[tb+]", "[pm+]", "[ad+]", "⚡").any(text::contains)) score += 3
        return score
    }

    fun sortedByVideoQuality(streams: List<StreamItem>): List<StreamItem> =
        streams.sortedByDescending(::score)
}
