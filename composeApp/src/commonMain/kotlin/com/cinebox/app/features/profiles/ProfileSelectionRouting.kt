package com.cinebox.app.features.profiles

import com.cinebox.app.core.ui.CineboxToastController
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.profile_already_active
import org.jetbrains.compose.resources.getString

internal fun routeProfileSelection(
    profile: CineboxProfile,
    isEditMode: Boolean,
    activeProfileIndex: Int? = null,
    onEditProfile: (CineboxProfile) -> Unit,
    onActiveProfileSelected: (CineboxProfile) -> Unit,
    onPinRequired: (CineboxProfile) -> Unit,
    onProfileSelected: (CineboxProfile) -> Unit,
) {
    when {
        isEditMode -> onEditProfile(profile)
        profile.profileIndex == activeProfileIndex -> onActiveProfileSelected(profile)
        profile.pinEnabled -> onPinRequired(profile)
        else -> onProfileSelected(profile)
    }
}

internal suspend fun showAlreadyActiveProfileToast(profile: CineboxProfile) {
    CineboxToastController.show(getString(Res.string.profile_already_active, profile.name))
}
