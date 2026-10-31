package com.cinebox.app.features.notifications

import com.cinebox.app.core.storage.DesktopStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/**
 * macOS notifications through osascript. Episodes are announced while the app is running: on the day of
 * release, from [EpisodeReleaseNotificationHour] on, and once per episode (remembered across restarts).
 */
internal actual object EpisodeReleaseNotificationPlatform {
    private val isMac = System.getProperty("os.name").orEmpty().lowercase().contains("mac")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val store = DesktopStorage.store("cinebox_episode_notifications")
    private const val ShownKey = "shown_request_ids"
    private val lock = Any()
    private var scheduled: List<EpisodeReleaseNotificationRequest> = emptyList()
    private var loop: Job? = null

    actual suspend fun notificationsAuthorized(): Boolean = isMac

    actual suspend fun requestAuthorization(): Boolean = isMac

    actual suspend fun scheduleEpisodeReleaseNotifications(
        requests: List<EpisodeReleaseNotificationRequest>,
    ) {
        if (!isMac) return
        synchronized(lock) {
            scheduled = requests
            if (loop?.isActive != true) {
                loop = scope.launch {
                    while (isActive) {
                        deliverDueNotifications()
                        delay(60_000L)
                    }
                }
            }
        }
        deliverDueNotifications()
    }

    actual suspend fun clearScheduledEpisodeReleaseNotifications() {
        synchronized(lock) {
            scheduled = emptyList()
            loop?.cancel()
            loop = null
        }
    }

    actual suspend fun showTestNotification(request: EpisodeReleaseNotificationRequest) {
        if (isMac) show(request.notificationTitle, request.notificationBody)
    }

    private fun deliverDueNotifications() {
        val today = LocalDate.now().toString()
        val afterHour = LocalTime.now().hour >= EpisodeReleaseNotificationHour
        if (!afterHour) return
        val due = synchronized(lock) { scheduled.filter { it.releaseDateIso == today } }
        if (due.isEmpty()) return
        val shown = store.getStringSet(ShownKey).orEmpty().toMutableSet()
        var changed = false
        due.forEach { request ->
            if (shown.add(request.requestId)) {
                show(request.notificationTitle, request.notificationBody)
                changed = true
            }
        }
        if (changed) store.putStringSet(ShownKey, shown.toList().takeLast(500).toSet())
    }

    private fun show(title: String, body: String) {
        runCatching {
            val script = "display notification \"${body.escapeForAppleScript()}\" " +
                "with title \"${title.escapeForAppleScript()}\""
            ProcessBuilder("osascript", "-e", script).redirectErrorStream(true).start()
        }
    }

    private fun String.escapeForAppleScript(): String =
        replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
}
