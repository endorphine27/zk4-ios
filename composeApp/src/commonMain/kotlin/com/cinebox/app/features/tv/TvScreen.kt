package com.cinebox.app.features.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.cinebox.app.features.addons.AddonRepository
import com.cinebox.app.features.catalog.CatalogRepository
import com.cinebox.app.features.catalog.CatalogTarget
import com.cinebox.app.features.home.HomeCatalogSection
import com.cinebox.app.features.home.MetaPreview
import com.cinebox.app.features.home.buildHomeCatalogDefinitions

private val TileMinWidth = 220.dp

private data class TvCatalog(
    val key: String,
    val title: String,
    val target: CatalogTarget,
)

/**
 * The TV addon puts the running programme at the start of the description, e.g.
 * "🔴 ACUM 10:30–13:00 · Show · încă 1 min — long text". Returns "10:30–13:00 · Show · încă 1 min".
 */
private fun MetaPreview.currentProgramme(): String? {
    val text = description?.trim().orEmpty()
    if (!text.startsWith("🔴")) return null
    val head = text.removePrefix("🔴").trim().removePrefix("ACUM").trim()
    return head.substringBefore(" — ").trim().takeIf { it.isNotEmpty() }
}

/** Live TV channels: every addon catalog of type "tv" (independent of the home screen), as a grid. */
@Composable
fun TvScreen(
    modifier: Modifier = Modifier,
    topChromePadding: Dp? = null,
    onChannelClick: ((MetaPreview) -> Unit)? = null,
    @Suppress("UNUSED_PARAMETER") onSectionClick: ((HomeCatalogSection) -> Unit)? = null,
) {
    val addonsState by AddonRepository.uiState.collectAsState()
    val catalogs = remember(addonsState.addons) {
        buildHomeCatalogDefinitions(addonsState.addons)
            .filter { it.serverConnectionId == null && it.type.equals("tv", ignoreCase = true) }
            .map { definition ->
                TvCatalog(
                    key = definition.key,
                    title = definition.catalogName,
                    target = CatalogTarget.Addon(
                        manifestUrl = definition.manifestUrl,
                        contentType = definition.type,
                        catalogId = definition.catalogId,
                        supportsPagination = definition.supportsPagination,
                    ),
                )
            }
    }
    var selectedKey by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    val selected = catalogs.firstOrNull { it.key == selectedKey } ?: catalogs.firstOrNull()

    LaunchedEffect(selected?.target) {
        selected?.let { CatalogRepository.load(it.target) }
    }
    val catalogState by CatalogRepository.uiState.collectAsState()
    val preferences by TvPreferencesRepository.state.collectAsState()
    val channels = if (selected == null) emptyList() else catalogState.items

    LaunchedEffect(channels) {
        if (channels.isNotEmpty()) TvChannelRegistry.channels = channels
    }

    val searching = query.isNotBlank()
    val filtered = remember(channels, query) {
        if (query.isBlank()) channels else channels.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }
    val favoriteChannels = remember(channels, preferences.favorites) {
        preferences.favorites.mapNotNull { id -> channels.firstOrNull { it.id == id } }
    }

    val openChannel: (MetaPreview) -> Unit = { channel ->
        onChannelClick?.invoke(channel)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = topChromePadding ?: 0.dp)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Canale TV",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            SearchField(
                query = query,
                onQueryChange = { query = it },
                modifier = Modifier.width(280.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        if (catalogs.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                catalogs.forEach { section ->
                    val isSelected = section.key == selected?.key
                    Text(
                        text = section.title,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            )
                            .clickable { selectedKey = section.key }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (selected == null) {
            Text(
                text = "Nu am găsit canale TV. Adaugă un addon cu canale (tip „tv”) din Addonuri.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (channels.isEmpty()) {
            Text(
                text = catalogState.errorMessage ?: if (catalogState.isLoading) "Se încarcă canalele..." else "Catalogul nu are canale.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(TileMinWidth),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                if (!searching && favoriteChannels.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "h-fav") { SectionTitle("Favorite") }
                    items(favoriteChannels, key = { "fav-${it.id}" }) { channel ->
                        ChannelTile(
                            channel = channel,
                            isFavorite = true,
                            onClick = { openChannel(channel) },
                            onToggleFavorite = { TvPreferencesRepository.toggleFavorite(channel.id) },
                        )
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }, key = "h-all") {
                    SectionTitle(
                        when {
                            searching -> "Rezultate (${filtered.size})"
                            else -> selected.title
                        },
                    )
                }
                if (filtered.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                        Text(
                            text = "Niciun canal nu se potrivește.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(filtered, key = { "all-${it.id}" }) { channel ->
                    ChannelTile(
                        channel = channel,
                        isFavorite = channel.id in preferences.favorites,
                        onClick = { openChannel(channel) },
                        onToggleFavorite = { TvPreferencesRepository.toggleFavorite(channel.id) },
                    )
                }
                if (!searching && catalogState.canLoadMore) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "load-more-${channels.size}") {
                        LaunchedEffect(channels.size) { CatalogRepository.loadMore() }
                        Spacer(Modifier.height(1.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            modifier = Modifier.fillMaxWidth(),
        )
        if (query.isEmpty()) {
            Text(
                text = "Caută un canal",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChannelTile(
    channel: MetaPreview,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val programme = channel.currentProgramme()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = channel.poster ?: channel.logo ?: channel.landscapePoster,
                contentDescription = channel.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Text(
                text = if (isFavorite) "★" else "☆",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onToggleFavorite)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.titleMedium,
                color = if (isFavorite) colors.primary else colors.onSurfaceVariant,
            )
        }
        Text(
            text = channel.name,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        if (programme != null) {
            Text(
                text = programme,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}
