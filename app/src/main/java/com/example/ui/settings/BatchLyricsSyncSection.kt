package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppSettings
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.OneUISquircleMedium
import com.example.viewmodel.LyricsSyncUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BatchLyricsSyncSection(
    settings: AppSettings,
    accentColor: Color,
    lyricsSyncUiState: LyricsSyncUiState,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onStartBatchLyricsSync: () -> Unit,
    onCancelBatchLyricsSync: () -> Unit,
    onRetryFailedLyricsSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = lyricsSyncUiState.statusSummary
    val unavailableCount = summary.notFoundCount + summary.instrumentalCount

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("settings_batch_lyrics_section")
    ) {
        // Section header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = stringResource(R.string.lyrics_sync_title),
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.lyrics_sync_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.lyrics_sync_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live cache breakdown chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LyricsStatusChip(
                label = "${summary.foundCount} Cached",
                tint = accentColor
            )
            LyricsStatusChip(
                label = "${summary.pendingCount} Pending",
                tint = MaterialTheme.colorScheme.secondary
            )
            if (unavailableCount > 0) {
                LyricsStatusChip(
                    label = "$unavailableCount Unavailable",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(8.dp))

        // Toggle: Auto-Fetch Lyrics for New Tracks
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = stringResource(R.string.lyrics_auto_fetch_title),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.lyrics_auto_fetch_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
            Switch(
                checked = settings.autoFetchLyricsOnScan,
                onCheckedChange = { enabled ->
                    onUpdateSettings { it.copy(autoFetchLyricsOnScan = enabled) }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor
                ),
                modifier = Modifier.testTag("settings_auto_fetch_lyrics_switch")
            )
        }

        // Toggle: Wi-Fi Only constraint
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = stringResource(R.string.lyrics_wifi_only_title),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.lyrics_wifi_only_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
            Switch(
                checked = settings.lyricsSyncWifiOnly,
                onCheckedChange = { enabled ->
                    onUpdateSettings { it.copy(lyricsSyncWifiOnly = enabled) }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor
                ),
                modifier = Modifier.testTag("settings_lyrics_wifi_only_switch")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live progress block while WorkManager task is queued or active
        if (lyricsSyncUiState.isRunning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(OneUISquircleMedium)
                    .background(accentColor.copy(alpha = 0.12f))
                    .padding(14.dp)
                    .testTag("settings_lyrics_sync_progress_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val progressTitle = when {
                        lyricsSyncUiState.total > 0 ->
                            "Fetching ${lyricsSyncUiState.current.coerceAtLeast(1)} of ${lyricsSyncUiState.total} tracks..."
                        lyricsSyncUiState.isWaitingForNetwork ->
                            "Waiting for ${if (settings.lyricsSyncWifiOnly) "Wi-Fi" else "network"} connection..."
                        else -> "Scanning library for missing lyrics..."
                    }

                    Text(
                        text = progressTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (lyricsSyncUiState.currentTrackTitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = lyricsSyncUiState.currentTrackTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (lyricsSyncUiState.total > 0) {
                        val fraction = (lyricsSyncUiState.current.toFloat() / lyricsSyncUiState.total.toFloat())
                            .coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { fraction },
                            color = accentColor,
                            trackColor = Color.White.copy(alpha = 0.16f),
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .testTag("settings_lyrics_sync_progress_bar")
                        )
                    } else {
                        LinearProgressIndicator(
                            color = accentColor,
                            trackColor = Color.White.copy(alpha = 0.16f),
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .testTag("settings_lyrics_sync_progress_bar")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onCancelBatchLyricsSync,
                        shape = OneUIPillShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("settings_cancel_lyrics_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.lyrics_cancel_sync),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.lyrics_cancel_sync),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        } else {
            if (lyricsSyncUiState.lastRunCompleted) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(OneUISquircleMedium)
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Batch sync completed",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Last sync finished: ${lyricsSyncUiState.foundInRun} cached, ${lyricsSyncUiState.failedInRun} unavailable",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Primary Action: Fetch All Missing Lyrics Now
            Button(
                onClick = onStartBatchLyricsSync,
                shape = OneUIPillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("settings_fetch_all_lyrics_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = stringResource(R.string.lyrics_fetch_all_now),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.lyrics_fetch_all_now),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Secondary Action: Retry Failed / Missing Lookups
        OutlinedButton(
            onClick = onRetryFailedLyricsSync,
            enabled = !lyricsSyncUiState.isRunning,
            shape = OneUIPillShape,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White,
                disabledContentColor = Color.White.copy(alpha = 0.4f)
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .testTag("settings_retry_failed_lyrics_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.lyrics_retry_failed),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.lyrics_retry_failed),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun LyricsStatusChip(
    label: String,
    tint: Color
) {
    Box(
        modifier = Modifier
            .clip(OneUIPillShape)
            .background(tint.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = tint
        )
    }
}
