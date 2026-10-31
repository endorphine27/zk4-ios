package com.cinebox.app.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.cinebox.app.features.details.MetaDetailsRepository
import com.cinebox.app.features.library.LibraryClock
import com.cinebox.app.features.library.LibraryItem
import com.cinebox.app.features.library.LibraryRepository
import com.cinebox.app.features.notifications.EpisodeReleaseNotificationsClock
import com.cinebox.app.features.notifications.isSeriesLibraryType
import com.cinebox.app.features.notifications.releaseDateIso
import com.cinebox.app.features.watched.WatchedClock
import com.cinebox.app.features.watched.WatchedItem
import com.cinebox.app.features.watched.WatchedRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

private val MonthNames = listOf(
    "ianuarie", "februarie", "martie", "aprilie", "mai", "iunie",
    "iulie", "august", "septembrie", "octombrie", "noiembrie", "decembrie",
)
private val WeekdayNames = listOf("Luni", "Marți", "Miercuri", "Joi", "Vineri", "Sâmbătă", "Duminică")
private val WeekdayNamesLower = listOf("luni", "marți", "miercuri", "joi", "vineri", "sâmbătă", "duminică")

private const val CalendarMetaConcurrency = 4

private data class CalendarEntry(
    val item: LibraryItem,
    val dateIso: String,
    val season: Int?,
    val episode: Int?,
    val videoId: String,
    val episodeTitle: String,
    val thumbnail: String?,
)

private data class YearMonth(val year: Int, val month: Int) {
    fun plusMonths(delta: Int): YearMonth {
        val index = year * 12 + (month - 1) + delta
        return YearMonth(index / 12, index % 12 + 1)
    }
}

private fun daysFromCivil(year: Int, month: Int, day: Int): Long {
    val y = if (month <= 2) year - 1 else year
    val era = (if (y >= 0) y else y - 399) / 400
    val yoe = y - era * 400
    val doy = (153 * (month + (if (month > 2) -3 else 9)) + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097L + doe - 719468L
}

/** Monday = 0 ... Sunday = 6 */
private fun weekdayIndex(year: Int, month: Int, day: Int): Int =
    (((daysFromCivil(year, month, day) + 3) % 7 + 7) % 7).toInt()

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    else -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
}

private fun isoDate(year: Int, month: Int, day: Int): String =
    "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

private fun parseIso(iso: String): Triple<Int, Int, Int>? {
    val parts = iso.split("-")
    if (parts.size != 3) return null
    val y = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    val d = parts[2].toIntOrNull() ?: return null
    return Triple(y, m, d)
}

private fun longDateLabel(iso: String): String {
    val (y, m, d) = parseIso(iso) ?: return iso
    return "${WeekdayNamesLower[weekdayIndex(y, m, d)]}, $d ${MonthNames[m - 1]}"
}

private fun episodeCode(entry: CalendarEntry): String? = when {
    entry.season != null && entry.episode != null -> "S${entry.season.toString().padStart(2, '0')}E${entry.episode.toString().padStart(2, '0')}"
    entry.episode != null -> "E${entry.episode}"
    else -> null
}

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    topChromePadding: Dp? = null,
    onItemClick: ((LibraryItem) -> Unit)? = null,
) {
    val today = remember { EpisodeReleaseNotificationsClock.isoDateFromEpochMs(LibraryClock.nowEpochMs()) }
    val todayParts = remember(today) { parseIso(today) ?: Triple(2026, 1, 1) }
    var visibleMonth by remember { mutableStateOf(YearMonth(todayParts.first, todayParts.second)) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    val entries = remember { mutableStateListOf<CalendarEntry>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { LibraryRepository.ensureLoaded() }
    val libraryState by LibraryRepository.uiState.collectAsState()
    val seriesItems = remember(libraryState.items) {
        libraryState.items.filter { isSeriesLibraryType(it.type) }
    }

    LaunchedEffect(seriesItems) {
        isLoading = true
        entries.clear()
        val semaphore = Semaphore(CalendarMetaConcurrency)
        try {
            coroutineScope {
                seriesItems.map { item ->
                    async {
                        val meta = semaphore.withPermit {
                            runCatching { MetaDetailsRepository.fetch(type = item.type, id = item.id) }
                                .onFailure { if (it is CancellationException) throw it }
                                .getOrNull()
                        } ?: return@async emptyList<CalendarEntry>()
                        meta.videos.mapNotNull { video ->
                            val date = releaseDateIso(video.released) ?: return@mapNotNull null
                            if (video.season == null && video.episode == null) return@mapNotNull null
                            CalendarEntry(
                                item = item,
                                dateIso = date,
                                season = video.season,
                                episode = video.episode,
                                videoId = video.id,
                                episodeTitle = video.title,
                                thumbnail = video.thumbnail ?: meta.background ?: item.poster,
                            )
                        }
                    }
                }.awaitAll().forEach { found ->
                    entries.addAll(found)
                }
            }
        } finally {
            isLoading = false
        }
    }

    val visibleEntries = entries.toList()
    val watchedState by WatchedRepository.uiState.collectAsState()
    val byDate = remember(visibleEntries) { visibleEntries.groupBy { it.dateIso } }
    val sidePanelDates = remember(visibleEntries, selectedDate, today) {
        val selected = selectedDate
        if (selected != null) {
            listOf(selected)
        } else {
            byDate.keys.filter { it >= today }.sorted().take(30)
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(top = topChromePadding ?: 0.dp)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(modifier = Modifier.weight(1f).fillMaxSize()) {
            MonthHeader(
                month = visibleMonth,
                onPrevious = { visibleMonth = visibleMonth.plusMonths(-1) },
                onNext = { visibleMonth = visibleMonth.plusMonths(1) },
            )
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                WeekdayNames.forEach { name ->
                    Text(
                        text = name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            MonthGrid(
                month = visibleMonth,
                today = today,
                selectedDate = selectedDate,
                byDate = byDate,
                onDayClick = { iso -> selectedDate = if (selectedDate == iso) null else iso },
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }

        Column(modifier = Modifier.width(340.dp).fillMaxSize()) {
            Text(
                text = if (selectedDate != null) longDateLabel(selectedDate!!) else "Următoarele episoade",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))
            val hasAny = sidePanelDates.any { byDate[it].orEmpty().isNotEmpty() }
            if (!hasAny) {
                Text(
                    text = when {
                        isLoading -> "Se încarcă..."
                        seriesItems.isEmpty() -> "Adaugă seriale în bibliotecă ca să apară aici."
                        selectedDate != null -> "Niciun episod în această zi."
                        else -> "Nu sunt episoade viitoare."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    sidePanelDates.forEach { date ->
                        val dayEntries = byDate[date].orEmpty()
                        if (dayEntries.isEmpty()) return@forEach
                        item(key = "h-$date") {
                            Text(
                                text = longDateLabel(date),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        items(dayEntries, key = { "$date-${it.item.id}-${it.season}-${it.episode}" }) { entry ->
                            val watched = watchedState.isLoaded && WatchedRepository.isWatched(
                                id = entry.item.id,
                                type = entry.item.type,
                                season = entry.season,
                                episode = entry.episode,
                            )
                            EpisodeRow(
                                entry = entry,
                                watched = watched,
                                onClick = { onItemClick?.invoke(entry.item) },
                                onToggleWatched = {
                                    WatchedRepository.toggleWatched(
                                        WatchedItem(
                                            id = entry.item.id,
                                            type = entry.item.type,
                                            name = entry.episodeTitle.ifBlank { entry.item.name },
                                            poster = entry.thumbnail ?: entry.item.poster,
                                            releaseInfo = entry.item.releaseInfo,
                                            season = entry.season,
                                            episode = entry.episode,
                                            videoId = entry.videoId,
                                            markedAtEpochMs = WatchedClock.nowEpochMs(),
                                        ),
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "‹",
            modifier = Modifier.clip(CircleShape).clickable(onClick = onPrevious).padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(220.dp)) {
            Text(
                text = month.year.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = MonthNames[month.month - 1],
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = "›",
            modifier = Modifier.clip(CircleShape).clickable(onClick = onNext).padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    today: String,
    selectedDate: String?,
    byDate: Map<String, List<CalendarEntry>>,
    onDayClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstOffset = weekdayIndex(month.year, month.month, 1)
    val totalDays = daysInMonth(month.year, month.month)
    val weeks = (firstOffset + totalDays + 6) / 7
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (week in 0 until weeks) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (column in 0 until 7) {
                    val day = week * 7 + column - firstOffset + 1
                    if (day < 1 || day > totalDays) {
                        Spacer(Modifier.weight(1f).fillMaxSize())
                    } else {
                        val iso = isoDate(month.year, month.month, day)
                        DayCell(
                            day = day,
                            isToday = iso == today,
                            isSelected = iso == selectedDate,
                            entries = byDate[iso].orEmpty(),
                            onClick = { onDayClick(iso) },
                            modifier = Modifier.weight(1f).fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    entries: List<CalendarEntry>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) colors.primary.copy(alpha = 0.22f) else colors.surfaceVariant.copy(alpha = 0.35f))
            .clickable(onClick = onClick)
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isToday) colors.primary else androidx.compose.ui.graphics.Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = if (isToday) colors.onPrimary else colors.onSurface,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            entries.distinctBy { it.item.id }.take(2).forEach { entry ->
                AsyncImage(
                    model = entry.item.poster,
                    contentDescription = entry.item.name,
                    modifier = Modifier.height(54.dp).width(36.dp).clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            if (entries.map { it.item.id }.distinct().size > 2) {
                Text(
                    text = "+${entries.map { it.item.id }.distinct().size - 2}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    entry: CalendarEntry,
    watched: Boolean,
    onClick: () -> Unit,
    onToggleWatched: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant.copy(alpha = if (watched) 0.2f else 0.45f))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = entry.thumbnail,
            contentDescription = entry.item.name,
            modifier = Modifier.width(96.dp).height(56.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.weight(1f)) {
            episodeCode(entry)?.let { code ->
                Text(
                    text = code,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primary,
                )
            }
            Text(
                text = entry.item.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (entry.episodeTitle.isNotBlank()) {
                Text(
                    text = entry.episodeTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            text = if (watched) "✓" else "○",
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onToggleWatched)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.titleMedium,
            color = if (watched) colors.primary else colors.onSurfaceVariant,
        )
    }
}
