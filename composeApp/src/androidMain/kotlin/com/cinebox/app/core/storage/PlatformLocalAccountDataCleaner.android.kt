package com.cinebox.app.core.storage

import android.content.Context

internal actual object PlatformLocalAccountDataCleaner {
    private val preferenceNames = listOf(
        "episode_shuffle",
        "cinebox_addons",
        "cinebox_library",
        "cinebox_library_display_settings",
        "cinebox_home_catalog_settings",
        "cinebox_player_settings",
        "torrent_settings",
        "cinebox_profile_cache",
        "cinebox_avatar_cache",
        "cinebox_profile_pin_cache",
        "cinebox_theme_settings",
        "cinebox_poster_card_style",
        "cinebox_debrid_settings",
        "cinebox_mdblist_settings",
        "cinebox_mdblist_auth",
        "cinebox_mdblist_sync",
        "cinebox_auth",
        "cinebox_trakt_auth",
        "cinebox_simkl_auth",
        "cinebox_simkl_sync",
        "cinebox_trakt_library",
        "cinebox_trakt_settings",
        "cinebox_watched",
        "cinebox_stream_link_cache",
        "cinebox_stream_badge_settings",
        "cinebox_continue_watching_preferences",
        "cinebox_cw_enrichment",
        "cinebox_episode_release_notifications",
        "cinebox_episode_release_notifications_platform",
        "cinebox_watch_progress",
        "cinebox_discover_selection",
        "cinebox_collection_mobile_settings",
        "cinebox_collections",
        "cinebox_plugins",
        "cinebox_member_access",
    )

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    actual fun wipe() {
        val context = appContext ?: return
        preferenceNames.forEach { name ->
            context.getSharedPreferences(name, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        }
        context.filesDir.resolve("cinebox_plugin_scrapers").deleteRecursively()
    }
}
