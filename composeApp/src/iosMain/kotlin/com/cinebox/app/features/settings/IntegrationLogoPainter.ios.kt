package com.cinebox.app.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.cinebox.app.features.simkl.SimklBrandAsset
import com.cinebox.app.features.simkl.simklBrandPainter
import cinebox.composeapp.generated.resources.Res
import cinebox.composeapp.generated.resources.introdb_favicon
import cinebox.composeapp.generated.resources.mdblist_logo
import cinebox.composeapp.generated.resources.rating_tmdb
import cinebox.composeapp.generated.resources.trakt_tv_favicon
import org.jetbrains.compose.resources.painterResource

@Composable
internal actual fun integrationLogoPainter(logo: IntegrationLogo): Painter =
    when (logo) {
        IntegrationLogo.Tmdb -> painterResource(Res.drawable.rating_tmdb)
        IntegrationLogo.Trakt -> painterResource(Res.drawable.trakt_tv_favicon)
        IntegrationLogo.Simkl -> simklBrandPainter(SimklBrandAsset.Glyph)
        IntegrationLogo.MdbList -> painterResource(Res.drawable.mdblist_logo)
        IntegrationLogo.IntroDb -> painterResource(Res.drawable.introdb_favicon)
    }
