package com.cinebox.app.features.membership

import com.cinebox.app.features.profiles.CineboxProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileBackgroundTest {
    @Test
    fun backgroundDoesNotRenderWithoutEntitlement() {
        val profile = CineboxProfile(profileBackgroundId = "aurora")

        assertNull(resolveProfileBackground(profile, CosmeticEntitlements.None))
    }

    @Test
    fun customBackgroundTakesPriorityWhenEntitled() {
        val profile = CineboxProfile(
            profileBackgroundId = "aurora",
            profileBackgroundUrl = "https://example.com/background.png",
        )
        val entitlements = CosmeticEntitlements(setOf(CosmeticEntitlement.PROFILE_BACKGROUNDS))

        assertEquals(
            ProfileBackgroundSelection.Custom("https://example.com/background.png"),
            resolveProfileBackground(profile, entitlements),
        )
    }
}
