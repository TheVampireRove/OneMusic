package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AlbumArtSize
import com.example.model.ElementColors
import com.example.model.Track
import com.example.ui.theme.OneUISquircleMedium
import com.example.ui.theme.glassmorphism
import com.example.ui.theme.parseHexColor

@Composable
fun TrackListItem(
    track: Track,
    isCurrentTrack: Boolean,
    isPlaying: Boolean,
    onTrackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    albumArtSize: AlbumArtSize = AlbumArtSize.MEDIUM,
    elementColors: ElementColors? = null,
    showTrackNumber: Boolean = false,
    modifier: Modifier = Modifier
) {
    val activeCardHex = elementColors?.trackActiveHighlight?.let { parseHexColor(it) }?.copy(alpha = 0.28f) ?: Color(0x357C4DFF)
    val normalCardHex = elementColors?.trackListCardBg?.let { parseHexColor(it) }?.copy(alpha = 0.35f) ?: Color(0x1F1E2034)

    val bgColor by animateColorAsState(
        targetValue = if (isCurrentTrack) activeCardHex else normalCardHex,
        label = "track_bg"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassmorphism(
                shape = OneUISquircleMedium,
                backgroundColor = bgColor,
                borderWidth = if (isCurrentTrack) 1.2.dp else 0.8.dp,
                elevation = if (isCurrentTrack) 6.dp else 2.dp
            )
            .clickable(onClick = onTrackClick)
            .testTag("track_item_${track.id}")
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showTrackNumber) {
                val numText = if (track.trackNumber > 0) String.format(java.util.Locale.US, "%02d", track.trackNumber) else "--"
                val activeTitleColor = elementColors?.trackActiveHighlight?.let { parseHexColor(it) } ?: track.accentColor
                Text(
                    text = numText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isCurrentTrack) FontWeight.ExtraBold else FontWeight.SemiBold
                    ),
                    color = if (isCurrentTrack) activeTitleColor else Color(0x99FFFFFF),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .widthIn(min = 28.dp)
                        .padding(end = 6.dp)
                        .testTag("track_number_${track.id}")
                )
            }

            // Cover Art thumbnail
            CoverArtView(
                track = track,
                size = albumArtSize.sizeDp.dp,
                cornerRadius = albumArtSize.cornerRadiusDp.dp,
                isPlaying = isCurrentTrack && isPlaying,
                showVinylPeek = false
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                val activeTitleColor = elementColors?.trackActiveHighlight?.let { parseHexColor(it) } ?: track.accentColor
                val normalTitleColor = elementColors?.trackTitleColor?.let { parseHexColor(it) } ?: Color.White
                val subtitleColor = elementColors?.trackSubtitleColor?.let { parseHexColor(it) } ?: Color(0xAAFFFFFF)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCurrentTrack) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Playing Indicator",
                            tint = activeTitleColor,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = if (isCurrentTrack) activeTitleColor else normalTitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "${track.artist} • ${track.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Duration
            val durColor = elementColors?.trackDurationColor?.let { parseHexColor(it) } ?: Color(0x88FFFFFF)
            Text(
                text = formatTime(track.durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = durColor,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            // Favorite button
            val favoriteColor = elementColors?.trackFavoriteColor?.let { parseHexColor(it) } ?: Color(0xFFFF4081)
            val unselectedFavColor = elementColors?.trackSubtitleColor?.let { parseHexColor(it) }?.copy(alpha = 0.6f) ?: Color(0x77FFFFFF)
            OneUITooltipButton(
                name = if (track.isFavorite) "Remove from Favorites" else "Add to Favorites",
                onClick = onToggleFavorite
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .testTag("track_fav_${track.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (track.isFavorite) favoriteColor else unselectedFavColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
