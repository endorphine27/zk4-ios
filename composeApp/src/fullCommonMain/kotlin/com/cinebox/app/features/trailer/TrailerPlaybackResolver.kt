package com.cinebox.app.features.trailer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource

actual object TrailerPlaybackResolver {
    private val extractor by lazy { InAppYouTubeExtractor() }
    private val cacheMutex = Mutex()
    private var playbackCache = emptyMap<String, CachedPlaybackSource>()
    private val extractionScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val inFlight = mutableMapOf<String, Deferred<TrailerPlaybackSource?>>()

    actual suspend fun resolveFromYouTubeUrl(youtubeUrl: String): TrailerPlaybackSource? {
        if (youtubeUrl.isBlank()) return null
        cacheMutex.lock()
        try {
            playbackCache = playbackCache.filterValues { entry ->
                entry.cachedAt.elapsedNow() < PlaybackCacheTtl
            }
            playbackCache[youtubeUrl]?.let { cached ->
                TrailerExtractionPlatform.diagnostic(
                    "cache hit ageMs=${cached.cachedAt.elapsedNow().inWholeMilliseconds}",
                )
                return cached.source
            }
        } finally {
            cacheMutex.unlock()
        }

        // A prefetch and a click on the same trailer share one extraction; it keeps running if a caller leaves
        val extraction = run {
            cacheMutex.lock()
            try {
                inFlight.getOrPut(youtubeUrl) {
                    extractionScope.async {
                        val extracted = extractor.extractPlaybackSource(youtubeUrl)
                        cacheMutex.lock()
                        try {
                            if (extracted != null) {
                                playbackCache = playbackCache + (
                                    youtubeUrl to CachedPlaybackSource(
                                        source = extracted,
                                        cachedAt = TimeSource.Monotonic.markNow(),
                                    )
                                )
                            }
                            inFlight.remove(youtubeUrl)
                        } finally {
                            cacheMutex.unlock()
                        }
                        extracted
                    }
                }
            } finally {
                cacheMutex.unlock()
            }
        }
        return extraction.await()
    }

    actual suspend fun resolveSingleUrlFromYouTubeUrl(youtubeUrl: String): String? {
        if (youtubeUrl.isBlank()) return null
        return extractor.extractSingleUrl(youtubeUrl)
    }

    private data class CachedPlaybackSource(
        val source: TrailerPlaybackSource,
        val cachedAt: TimeMark,
    )
}

private val PlaybackCacheTtl = 10.minutes
