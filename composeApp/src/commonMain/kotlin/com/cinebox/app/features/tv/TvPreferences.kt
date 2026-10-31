package com.cinebox.app.features.tv

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal expect object TvPreferencesStorage {
    fun loadPayload(): String?
    fun savePayload(payload: String)
}

@Serializable
internal data class RecentChannel(
    val id: String,
    val type: String,
    val name: String,
    val poster: String? = null,
)

@Serializable
internal data class TvPreferences(
    val favorites: List<String> = emptyList(),
    val recents: List<RecentChannel> = emptyList(),
)

private const val MaxRecentChannels = 12

/** Favourite and recently watched TV channels, kept per profile. */
internal object TvPreferencesRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val _state = MutableStateFlow(load())
    val state: StateFlow<TvPreferences> = _state.asStateFlow()

    private fun load(): TvPreferences =
        runCatching {
            TvPreferencesStorage.loadPayload()?.let { json.decodeFromString<TvPreferences>(it) }
        }.getOrNull() ?: TvPreferences()

    private fun update(next: TvPreferences) {
        _state.value = next
        runCatching { TvPreferencesStorage.savePayload(json.encodeToString(next)) }
    }

    fun toggleFavorite(channelId: String) {
        val current = _state.value
        update(
            current.copy(
                favorites = if (channelId in current.favorites) {
                    current.favorites - channelId
                } else {
                    current.favorites + channelId
                },
            ),
        )
    }

    fun markWatched(channel: RecentChannel) {
        val current = _state.value
        update(
            current.copy(
                recents = (listOf(channel) + current.recents.filterNot { it.id == channel.id })
                    .take(MaxRecentChannels),
            ),
        )
    }
}

/** The channels of the TV tab in display order, so the player can switch to the next/previous one. */
internal object TvChannelRegistry {
    var channels: List<com.cinebox.app.features.home.MetaPreview> = emptyList()
}

/** Lets the player ask the app shell to open the next or previous TV channel. */
internal object ChannelZapController {
    var handler: ((Int) -> Unit)? = null

    fun zap(direction: Int) {
        handler?.invoke(direction)
    }
}
