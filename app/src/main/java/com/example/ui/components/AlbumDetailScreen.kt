package com.example.ui.components

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlbumArtSize
import com.example.model.AlbumTrackOrderComparator
import com.example.model.ElementColors
import com.example.model.Track
import com.example.ui.theme.MotionSpecs
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.OneUISquircleLarge
import com.example.ui.theme.appSharedBounds
import com.example.ui.theme.bouncableClick
import com.example.ui.theme.glassmorphism
import com.example.ui.theme.parseHexColor
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun AlbumDetailScreen(
    albumName: String,
    tracks: List<Track>,
    currentTrack: Track,
    isPlaying: Boolean,
    accentColor: Color,
    albumArtSize: AlbumArtSize = AlbumArtSize.MEDIUM,
    elementColors: ElementColors? = null,
    listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
    bottomContentPadding: Dp = 8.dp,
    enableBackHandler: Boolean = true,
    onBack: () -> Unit,
    onPlayFromContext: (List<Track>, Int) -> Unit,
    onShuffleFromContext: (List<Track>) -> Unit = { list -> onPlayFromContext(list, 0) },
    onToggleFavorite: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    PredictiveBackHandler(enabled = enableBackHandler) { progress ->
        try {
            progress.collect { }
            onBack()
        } catch (_: CancellationException) { }
    }

    val sortedTracks = remember(tracks) {
        tracks.sortedWith(AlbumTrackOrderComparator)
    }

    val leadTrack = sortedTracks.firstOrNull() ?: currentTrack
    val totalDurationMs = sortedTracks.sumOf { it.durationMs }
    val totalMinutes = totalDurationMs / 60000
    val totalSeconds = (totalDurationMs % 60000) / 1000
    val screenBg = (elementColors?.background?.let { parseHexColor(it) }) ?: Color(0xFF0F101E)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .testTag("album_detail_screen")
    ) {
        // Navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF))
                    .bouncableClick(onClick = onBack)
                    .testTag("album_detail_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Albums",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Album Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = bottomContentPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Album Hero Header Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphism(
                            shape = OneUISquircleLarge,
                            backgroundColor = Color(0x3220233D),
                            borderWidth = 1.2.dp,
                            elevation = 8.dp
                        )
                        .padding(18.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CoverArtView(
                            track = leadTrack,
                            size = 170.dp,
                            cornerRadius = 24.dp,
                            isPlaying = isPlaying && currentTrack.album == albumName,
                            showVinylPeek = true,
                            artSharedElementKey = "album_art_$albumName"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = albumName,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.appSharedBounds(
                                key = "album_title_$albumName",
                                useScaleToBounds = true
                            )
                        )

                        Text(
                            text = "${leadTrack.artist} • ${leadTrack.genre}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xCCFFFFFF),
                            maxLines = 1
                        )

                        Text(
                            text = "${sortedTracks.size} Songs • ${totalMinutes}m ${totalSeconds}s",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0x99FFFFFF)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Play All & Shuffle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(OneUIPillShape)
                                    .background(accentColor)
                                    .bouncableClick {
                                        if (sortedTracks.isNotEmpty()) {
                                            onPlayFromContext(sortedTracks, 0)
                                        }
                                    }
                                    .padding(vertical = 12.dp)
                                    .testTag("album_play_all_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play All",
                                        tint = Color(0xFF0C0E1A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Play All",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF0C0E1A)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(OneUIPillShape)
                                    .background(Color(0x25FFFFFF))
                                    .bouncableClick {
                                        if (sortedTracks.isNotEmpty()) {
                                            onShuffleFromContext(sortedTracks)
                                        }
                                    }
                                    .padding(vertical = 12.dp)
                                    .testTag("album_shuffle_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "Shuffle",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Shuffle",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Track list header
            item {
                Text(
                    text = "TRACKS (${sortedTracks.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0x88FFFFFF),
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            // Track items ordered by track number
            itemsIndexed(sortedTracks, key = { _, track -> track.id }) { index, track ->
                TrackListItem(
                    track = track,
                    isCurrentTrack = track.id == currentTrack.id,
                    isPlaying = isPlaying,
                    onTrackClick = { onPlayFromContext(sortedTracks, index) },
                    onToggleFavorite = { onToggleFavorite(track) },
                    albumArtSize = albumArtSize,
                    elementColors = elementColors,
                    showTrackNumber = true,
                    modifier = Modifier.animateItem(
                        fadeInSpec = MotionSpecs.snappySpring(),
                        placementSpec = MotionSpecs.LazyItemPlacementSpring,
                        fadeOutSpec = MotionSpecs.snappySpring()
                    )
                )
            }
        }
    }
}
