package com.cinebox.app.features.membership

import com.cinebox.app.features.profiles.CineboxProfile

sealed interface ProfileBackgroundSelection {
    data class Catalog(val id: String) : ProfileBackgroundSelection
    data class Custom(val url: String) : ProfileBackgroundSelection
}

fun resolveProfileBackground(
    profile: CineboxProfile,
    entitlements: CosmeticEntitlements,
): ProfileBackgroundSelection? {
    if (!entitlements.includes(CosmeticEntitlement.PROFILE_BACKGROUNDS)) return null
    profile.profileBackgroundUrl?.trim()?.takeIf { it.isNotBlank() }?.let {
        return ProfileBackgroundSelection.Custom(it)
    }
    profile.profileBackgroundId?.trim()?.takeIf { it.isNotBlank() }?.let {
        return ProfileBackgroundSelection.Catalog(it)
    }
    return null
}
