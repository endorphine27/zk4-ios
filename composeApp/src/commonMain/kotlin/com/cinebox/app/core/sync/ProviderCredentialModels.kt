package com.cinebox.app.core.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

internal const val PROVIDER_API_KEY_FIELD = "api_key"
internal const val PROVIDER_CLIENT_ID_FIELD = "client_id"
internal const val PROVIDER_AUTH_JSON_FIELD = "auth_json"

/** Trakt values added later: a server row that does not exist yet must not wipe what is set here. */
private val KEEP_LOCAL_WHEN_REMOTE_MISSING = setOf("trakt", "trakt_auth")

internal object ProviderCredentialIds {
    const val TMDB = "tmdb"
    const val MDBLIST = "mdblist"
    const val ANIMESKIP = "animeskip"
    const val INTRODB = "introdb"
    const val TRAKT = "trakt"
    const val TRAKT_AUTH = "trakt_auth"

    fun debrid(providerId: String): String = "debrid:$providerId"
}

internal data class ProviderCredentialValue(
    val provider: String,
    val field: String,
    val value: String,
) {
    fun credentialJson(): JsonObject = buildJsonObject {
        put(field, value.trim())
    }
}

/** Values set here that the server has no usable copy of yet (missing or blank row). */
internal fun ProviderCredentialSnapshot.valuesMissingRemotely(
    rows: List<SupabaseProviderCredential>,
    providers: Set<String>,
): List<ProviderCredentialValue> {
    val remoteByProvider = rows.associateBy { it.provider.lowercase() }
    return values.filter { local ->
        local.provider in providers && local.value.isNotBlank() && run {
            val remote = remoteByProvider[local.provider.lowercase()]
            val remoteValue = (remote?.credentialJson?.get(local.field) as? JsonPrimitive)?.contentOrNull
            remoteValue.isNullOrBlank()
        }
    }
}

internal data class ProviderCredentialSnapshot(
    val profileId: Int,
    val values: List<ProviderCredentialValue>,
) {
    init {
        require(values.map(ProviderCredentialValue::provider).distinct().size == values.size)
    }

    fun mergeRemote(rows: List<SupabaseProviderCredential>): ProviderCredentialSnapshot {
        val remoteByProvider = rows.associateBy { it.provider.lowercase() }
        return copy(
            values = values.map { local ->
                val remote = remoteByProvider[local.provider.lowercase()]
                    ?: return@map if (local.provider in KEEP_LOCAL_WHEN_REMOTE_MISSING) local else local.copy(value = "")
                val element = remote.credentialJson[local.field] as? JsonPrimitive
                    ?: error("Invalid credential payload for ${local.provider}")
                val value = element.contentOrNull
                    ?: error("Invalid credential value for ${local.provider}")
                local.copy(value = value.trim())
            },
        )
    }
}

@Serializable
internal data class SupabaseProviderCredential(
    val provider: String,
    @SerialName("credential_json") val credentialJson: JsonObject,
    @SerialName("updated_at") val updatedAt: String? = null,
)
