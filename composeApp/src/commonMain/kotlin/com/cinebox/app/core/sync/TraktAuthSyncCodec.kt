package com.cinebox.app.core.sync

import com.cinebox.app.features.trakt.TraktAuthState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

/**
 * Wire format shared by every CINEBOX app for a synced Trakt login:
 * `{"v":1,"a":access,"r":refresh,"t":type,"c":createdAtSeconds,"e":expiresIn,"u":username,"s":slug}`
 * or, after an explicit disconnect, `{"v":1,"x":disconnectedAtEpochMs}`.
 */
sealed interface SyncedTraktAuth {
    data class Login(
        val accessToken: String,
        val refreshToken: String,
        val tokenType: String?,
        val createdAt: Long,
        val expiresIn: Int,
        val username: String?,
        val userSlug: String?,
    ) : SyncedTraktAuth

    data class Disconnected(val atMillis: Long) : SyncedTraktAuth
}

object TraktAuthSyncCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(state: TraktAuthState, lastKnownPayload: String): String = when {
        state.isAuthenticated -> buildJsonObject {
            put("v", 1)
            put("a", state.accessToken.orEmpty())
            put("r", state.refreshToken.orEmpty())
            state.tokenType?.let { put("t", it) }
            put("c", state.createdAt ?: 0L)
            put("e", state.expiresIn ?: 0)
            state.username?.let { put("u", it) }
            state.userSlug?.let { put("s", it) }
        }.toString()
        state.disconnectedAtMillis != null -> buildJsonObject {
            put("v", 1)
            put("x", state.disconnectedAtMillis)
        }.toString()
        // Not logged in here and not disconnected on purpose: leave what other devices share alone.
        else -> lastKnownPayload
    }

    fun decode(payload: String): SyncedTraktAuth? {
        if (payload.isBlank()) return null
        val obj = runCatching { json.parseToJsonElement(payload) as? JsonObject }.getOrNull() ?: return null
        obj["x"]?.let { x ->
            val at = (x as? JsonPrimitive)?.longOrNull ?: return null
            return SyncedTraktAuth.Disconnected(at)
        }
        val access = (obj["a"] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() } ?: return null
        val refresh = (obj["r"] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() } ?: return null
        return SyncedTraktAuth.Login(
            accessToken = access,
            refreshToken = refresh,
            tokenType = (obj["t"] as? JsonPrimitive)?.contentOrNull,
            createdAt = (obj["c"] as? JsonPrimitive)?.longOrNull ?: 0L,
            expiresIn = (obj["e"] as? JsonPrimitive)?.intOrNull ?: 0,
            username = (obj["u"] as? JsonPrimitive)?.contentOrNull,
            userSlug = (obj["s"] as? JsonPrimitive)?.contentOrNull,
        )
    }
}
