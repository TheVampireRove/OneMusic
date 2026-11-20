package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Track
import com.example.ui.theme.MotionSpecs
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.appSharedBounds
import com.example.ui.theme.bouncableClick
import com.example.ui.theme.glassmorphism

@Composable
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    currentPositionMs: Long,
    onExpand: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (track.durationMs > 0) {
        (currentPositionMs.toFloat() / track.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .appSharedBounds(
                key = "player_container",
                clipShape = OneUIPillShape
            )
            .testTag("mini_player")
            .shadow(12.dp, OneUIPillShape, clip = false)
            .background(Color(0xFA16182B), OneUIPillShape)
            .glassmorphism(
                shape = OneUIPillShape,
                backgroundColor = Color(0xCC222540),
                borderWidth = 1.2.dp
            )
            .bouncableClick(pressedScale = 0.96f, onClick = onExpand)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art morphing seamlessly into full-screen Now Playing hero cover
                CoverArtView(
                    track = track,
                    size = 46.dp,
                    cornerRadius = 14.dp,
                    isPlaying = isPlaying,
                    showVinylPeek = false,
                    artSharedElementKey = "player_artwork_${track.id}"
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Track & Artist text
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .appSharedBounds(
                                key = "player_title_${track.id}",
                                useScaleToBounds = true
                            )
                            .basicMarquee(iterations = Int.MAX_VALUE)
                    )
                    Text(
                        text = "${track.artist} • ${track.album}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xCCFFFFFF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Play / Pause Button with smooth scale+fade icon morph and tactile press
                OneUITooltipButton(
                    name = if (isPlaying) "Pause Playback" else "Play Track",
                    onClick = onTogglePlayPause
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .testTag("mini_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = isPlaying,
                            transitionSpec = { MotionSpecs.iconMorphTransition() },
                            label = "mini_play_pause_morph"
                        ) { playing ->
                            Icon(
                                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playing) "Pause" else "Play",
                                tint = track.accentColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                // Next Button
                OneUITooltipButton(
                    name = "Next Track",
                    onClick = onNext
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .testTag("mini_next_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Track",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Sleek bottom progress indicator
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = track.accentColor,
                trackColor = Color(0x1FFFFFFF),
            )
        }
    }
}
