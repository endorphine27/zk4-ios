package com.cinebox.app.core.storage

import com.cinebox.app.features.profiles.ProfileRepository


object ProfileScopedKey {
    fun of(baseKey: String): String = "${baseKey}_${ProfileRepository.activeProfileId}"
    fun of(baseKey: String, profileId: Int): String = "${baseKey}_$profileId"
}
