package com.example.ui.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.LyricsRepository
import com.example.model.LyricLine
import com.example.model.LyricsAnimationStyle
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.OneUISquircleLarge
import com.example.ui.theme.OneUISquircleMedium
import com.example.ui.theme.glassmorphism

enum class LyricsEditorTab {
    PASTE_EDIT,
    TAP_SYNC
}

private enum class TapSyncGranularity {
    LINE,
    WORD
}

/**
 * Renders word-by-word synchronized lyrics highlighting using [LyricLine.words].
 * Illuminates completed words, smoothly sweeps/brightens the active word/syllable,
 * and dims upcoming words.
 */
@Composable
fun WordSyncedLyricText(
    line: LyricLine,
    nextLineTimeMs: Long,
    currentPositionMs: Long,
    primaryColor: Color,
    textStyle: TextStyle,
    animationStyle: LyricsAnimationStyle,
    modifier: Modifier = Modifier
) {
    val words = line.words
    val annotated = remember(words, currentPositionMs, nextLineTimeMs, primaryColor, animationStyle) {
        buildAnnotatedString {
            words.forEachIndexed { idx, word ->
                val defaultNextStart = words.getOrNull(idx + 1)?.timeMs ?: nextLineTimeMs
                val wordEnd = (word.endTimeMs ?: defaultNextStart).coerceAtLeast(word.timeMs + 80L)
                when {
                    currentPositionMs >= wordEnd -> {
                        withStyle(
                            SpanStyle(
                                color = primaryColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                        ) {
                            append(word.text)
                        }
                    }
                    currentPositionMs >= word.timeMs -> {
                        val progress = ((currentPositionMs - word.timeMs).toFloat() /
                            (wordEnd - word.timeMs).coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
                        val activeWordColor = lerp(
                            Color.White,
                            primaryColor,
                            0.45f + (0.55f * progress)
                        )
                        if (
                            (animationStyle == LyricsAnimationStyle.KARAOKE_SWEEP ||
                                animationStyle == LyricsAnimationStyle.TYPEWRITER_REVEAL) &&
                            word.text.length >= 3
                        ) {
                            val splitIdx = (word.text.length * progress).toInt().coerceIn(0, word.text.length)
                            withStyle(
                                SpanStyle(
                                    color = lerp(primaryColor, Color.White, 0.25f),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) {
                                append(word.text.substring(0, splitIdx))
                            }
                            withStyle(
                                SpanStyle(
                                    color = activeWordColor.copy(alpha = 0.72f),
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(word.text.substring(splitIdx))
                            }
                        } else {
                            withStyle(
                                SpanStyle(
                                    color = activeWordColor,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) {
                                append(word.text)
                            }
                        }
                    }
                    else -> {
                        withStyle(
                            SpanStyle(
                                color = Color.White.copy(alpha = 0.38f),
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append(word.text)
                        }
                    }
                }
            }
        }
    }

    Text(
        text = annotated,
        style = textStyle,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Unobtrusive glass header bar shown above active lyrics so the user can always
 * inspect sync precision (Word-Synced / Synced / Plain), edit or replace wrong lyrics,
 * import a replacement .LRC file, tap-sync plain lyrics, re-fetch from LRCLIB, or clear them.
 */
@Composable
fun LyricsControlOverlayBar(
    hasWordSync: Boolean,
    isSyncedTrack: Boolean,
    primaryColor: Color,
    onEditLyrics: () -> Unit,
    onOpenTapSync: () -> Unit,
    onImportFile: () -> Unit,
    onRefetchOnline: (() -> Unit)?,
    onClearLyrics: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xD815172B), Color(0x8815172B), Color.Transparent)
                )
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Sync mode indicator chip
        Row(
            modifier = Modifier
                .clip(OneUIPillShape)
                .background(Color(0x2AFFFFFF))
                .border(0.8.dp, Color(0x28FFFFFF), OneUIPillShape)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = if (hasWordSync) Icons.Default.AutoAwesome else Icons.Default.MusicNote,
                contentDescription = "Lyrics Sync Mode",
                tint = if (hasWordSync || isSyncedTrack) primaryColor else Color(0xAAFFFFFF),
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = when {
                    hasWordSync -> "Word-Synced"
                    isSyncedTrack -> "Synced LRC"
                    else -> "Plain Text"
                },
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (hasWordSync) primaryColor else Color(0xDDFFFFFF),
                maxLines = 1
            )
        }

        // Right: Quick Edit + More Actions Menu
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .heightIn(min = 36.dp)
                    .clip(OneUIPillShape)
                    .background(primaryColor.copy(alpha = 0.22f))
                    .border(1.dp, primaryColor.copy(alpha = 0.55f), OneUIPillShape)
                    .clickable(onClick = onEditLyrics)
                    .padding(horizontal = 11.dp, vertical = 6.dp)
                    .testTag("edit_lyrics_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit or Replace Lyrics",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Edit",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x28FFFFFF))
                        .clickable { menuExpanded = true }
                        .testTag("lyrics_options_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Lyrics Options",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(Color(0xF5181A2E))
                        .border(1.dp, Color(0x35FFFFFF), OneUISquircleMedium)
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit / Paste Word-Synced", color = Color.White) },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = primaryColor)
                        },
                        onClick = {
                            menuExpanded = false
                            onEditLyrics()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Live Tap-Sync Studio", color = Color.White) },
                        leadingIcon = {
                            Icon(Icons.Default.TouchApp, contentDescription = null, tint = primaryColor)
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenTapSync()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Replace with .LRC / .TXT", color = Color.White) },
                        leadingIcon = {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = primaryColor)
                        },
                        onClick = {
                            menuExpanded = false
                            onImportFile()
                        }
                    )
                    if (onRefetchOnline != null) {
                        DropdownMenuItem(
                            text = { Text("Re-fetch Online (LRCLIB)", color = Color.White) },
                            leadingIcon = {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = primaryColor)
                            },
                            onClick = {
                                menuExpanded = false
                                onRefetchOnline()
                            }
                        )
                    }
                    if (onClearLyrics != null) {
                        DropdownMenuItem(
                            text = { Text("Clear Lyrics", color = Color(0xFFFF7A85)) },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFFF7A85))
                            },
                            onClick = {
                                menuExpanded = false
                                onClearLyrics()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Lyrics Studio modal supporting:
 * 1. Pasting or editing plain, line-synced (`[mm:ss.xx]`), and word-synced (`<mm:ss.xx>`) lyrics
 * 2. One-tap clipboard paste, `.lrc`/`.txt` replacement, live +/-0.5s timestamp offset shifting, and clear/reset
 * 3. Live Tap-Sync Studio to stamp line or word-synced timestamps directly during playback
 */
@Composable
fun LyricsEditorDialog(
    initialText: String,
    initialTab: LyricsEditorTab,
    hasExistingLyrics: Boolean,
    trackDurationMs: Long,
    currentPositionMs: Long,
    primaryColor: Color,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onPickFile: () -> Unit,
    onClearLyrics: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(initialTab) }
    var editorText by remember { mutableStateOf(initialText) }

    // Keep synced when initialText asynchronously loads (if user hasn't typed yet)
    var initializedFromAsync by remember { mutableStateOf(initialText.isNotEmpty()) }
    LaunchedEffect(initialText) {
        if (!initializedFromAsync && initialText.isNotEmpty() && editorText.isEmpty()) {
            editorText = initialText
            initializedFromAsync = true
        }
    }

    val parsedPreview = remember(editorText, trackDurationMs) {
        if (editorText.isBlank()) emptyList()
        else LyricsRepository.parseLyricsContent(editorText, trackDurationMs)
    }
    val previewWordCount = remember(parsedPreview) {
        parsedPreview.sumOf { it.words.size }
    }
    val hasAnyTimestamps = remember(editorText) {
        editorText.contains('[') || editorText.contains('<')
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp)
                .glassmorphism(
                    shape = OneUISquircleLarge,
                    backgroundColor = Color(0xF515172B),
                    borderWidth = 1.2.dp,
                    elevation = 16.dp
                ),
            color = Color.Transparent,
            shape = OneUISquircleLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasExistingLyrics) "Edit & Sync Lyrics" else "Add Lyrics",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Supports plain text, [mm:ss.xx] LRC & <mm:ss.xx> word-sync",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x99FFFFFF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_lyrics_editor_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xCCFFFFFF)
                        )
                    }
                }

                // Studio Mode Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(OneUIPillShape)
                        .background(Color(0x22FFFFFF))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StudioTabPill(
                        label = "Paste & Edit",
                        icon = Icons.Default.Edit,
                        selected = activeTab == LyricsEditorTab.PASTE_EDIT,
                        primaryColor = primaryColor,
                        onClick = { activeTab = LyricsEditorTab.PASTE_EDIT },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lyrics_tab_paste_edit")
                    )
                    StudioTabPill(
                        label = "Tap-Sync Studio",
                        icon = Icons.Default.TouchApp,
                        selected = activeTab == LyricsEditorTab.TAP_SYNC,
                        primaryColor = primaryColor,
                        onClick = { activeTab = LyricsEditorTab.TAP_SYNC },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lyrics_tab_tap_sync")
                    )
                }

                // Tab Content
                when (activeTab) {
                    LyricsEditorTab.PASTE_EDIT -> {
                        // Quick Tools Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EditorActionChip(
                                label = "Paste Clipboard",
                                icon = Icons.Default.ContentPaste,
                                accentColor = primaryColor,
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clip = cm?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
                                    if (!clip.isNullOrBlank()) {
                                        editorText = clip
                                    }
                                },
                                modifier = Modifier.testTag("paste_clipboard_chip")
                            )
                            EditorActionChip(
                                label = "Import File",
                                icon = Icons.Default.UploadFile,
                                accentColor = primaryColor,
                                onClick = {
                                    onDismiss()
                                    onPickFile()
                                },
                                modifier = Modifier.testTag("dialog_import_lrc_chip")
                            )
                            if (hasAnyTimestamps) {
                                EditorActionChip(
                                    label = "-0.5s Sync",
                                    icon = Icons.Default.Timer,
                                    accentColor = primaryColor,
                                    onClick = {
                                        editorText = LyricsRepository.shiftLrcTimestamps(editorText, -500L)
                                    },
                                    modifier = Modifier.testTag("shift_back_chip")
                                )
                                EditorActionChip(
                                    label = "+0.5s Sync",
                                    icon = Icons.Default.Timer,
                                    accentColor = primaryColor,
                                    onClick = {
                                        editorText = LyricsRepository.shiftLrcTimestamps(editorText, 500L)
                                    },
                                    modifier = Modifier.testTag("shift_forward_chip")
                                )
                            } else {
                                EditorActionChip(
                                    label = "Word-Sync Sample",
                                    icon = Icons.Default.AutoAwesome,
                                    accentColor = primaryColor,
                                    onClick = {
                                        editorText = "[00:02.00]<00:02.00>First <00:02.60>word <00:03.20>synced <00:03.80>line<00:04.50>\n" +
                                            "[00:05.00]<00:05.00>Second <00:05.70>word <00:06.30>synced <00:07.00>line<00:07.80>"
                                    },
                                    modifier = Modifier.testTag("word_sync_sample_chip")
                                )
                            }
                            if (editorText.isNotEmpty()) {
                                EditorActionChip(
                                    label = "Clear Text",
                                    icon = Icons.Default.Clear,
                                    accentColor = Color(0xFFFF8A80),
                                    onClick = { editorText = "" },
                                    modifier = Modifier.testTag("clear_editor_text_chip")
                                )
                            }
                        }

                        OutlinedTextField(
                            value = editorText,
                            onValueChange = { editorText = it },
                            placeholder = {
                                Text(
                                    text = "[00:12.00] Line synced...\n" +
                                        "[00:16.50]<00:16.50>Word <00:17.10>synced <00:17.80>line<00:18.40>",
                                    color = Color(0x55FFFFFF),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            ),
                            minLines = 6,
                            maxLines = 10,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color(0x44FFFFFF),
                                cursorColor = primaryColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("paste_lyrics_input")
                        )

                        // Live Parser Feedback
                        if (editorText.isNotBlank()) {
                            val syncedCount = parsedPreview.count { it.isSynced }
                            val statusText = when {
                                previewWordCount > 0 ->
                                    "Detected: ${parsedPreview.size} lines • $previewWordCount word timestamps"
                                syncedCount > 0 ->
                                    "Detected: $syncedCount synced lines"
                                else ->
                                    "Detected: ${parsedPreview.size} plain lines (switch to Tap-Sync Studio to time them)"
                            }
                            val statusColor = when {
                                previewWordCount > 0 -> Color(0xFF69F0AE)
                                syncedCount > 0 -> primaryColor
                                else -> Color(0xFFFFD54F)
                            }
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = statusColor,
                                modifier = Modifier.testTag("lyrics_parser_status")
                            )
                        }
                    }

                    LyricsEditorTab.TAP_SYNC -> {
                        TapSyncStudioContent(
                            rawText = editorText,
                            currentPositionMs = currentPositionMs,
                            primaryColor = primaryColor,
                            onGeneratedLrc = { generated ->
                                editorText = generated
                            }
                        )
                    }
                }

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasExistingLyrics && onClearLyrics != null) {
                        TextButton(
                            onClick = {
                                onClearLyrics()
                                onDismiss()
                            },
                            modifier = Modifier.testTag("dialog_clear_lyrics_button")
                        ) {
                            Text("Remove", color = Color(0xFFFF7A85), fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color(0xCCFFFFFF))
                        }
                        Button(
                            onClick = {
                                if (editorText.isNotBlank()) {
                                    onSave(editorText)
                                }
                                onDismiss()
                            },
                            enabled = editorText.isNotBlank(),
                            shape = OneUIPillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color(0xFF0C0E1A)
                            ),
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .testTag("save_pasted_lyrics_button")
                        ) {
                            Text("Save & Apply", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TapSyncStudioContent(
    rawText: String,
    currentPositionMs: Long,
    primaryColor: Color,
    onGeneratedLrc: (String) -> Unit
) {
    val cleanLines = remember(rawText) { LyricsRepository.stripTimestampsForTapSync(rawText) }
    var granularity by remember { mutableStateOf(TapSyncGranularity.WORD) }
    var cursorLineIdx by remember(cleanLines.size, granularity) { mutableIntStateOf(0) }
    var cursorWordIdx by remember(cleanLines.size, granularity) { mutableIntStateOf(0) }

    // Stores stamped timestamps: lineIdx -> list of word timestamps (in LINE mode, 1 timestamp per line)
    val stampedLineWords = remember(cleanLines.size, granularity) {
        mutableStateListOf<MutableList<Long>>().apply {
            repeat(cleanLines.size) { add(mutableListOf()) }
        }
    }
    // Track exact tap history for 1-tap Undo
    val tapHistory = remember(cleanLines.size, granularity) {
        mutableStateListOf<Pair<Int, Int>>()
    }

    fun emitLrcString() {
        val sb = StringBuilder()
        cleanLines.forEachIndexed { lineIdx, lineText ->
            val times = stampedLineWords.getOrNull(lineIdx).orEmpty()
            if (granularity == TapSyncGranularity.LINE) {
                if (times.isNotEmpty()) {
                    sb.append(LyricsRepository.formatLrcTimestamp(times.first())).append(lineText)
                } else {
                    sb.append(lineText)
                }
            } else {
                val words = lineText.split(Regex("\\s+")).filter { it.isNotEmpty() }
                if (times.isNotEmpty() && words.isNotEmpty()) {
                    sb.append(LyricsRepository.formatLrcTimestamp(times.first()))
                    words.forEachIndexed { wIdx, w ->
                        val t = times.getOrNull(wIdx)
                        if (t != null) {
                            sb.append(LyricsRepository.formatWordTimestamp(t))
                        }
                        sb.append(w)
                        if (wIdx < words.lastIndex) sb.append(" ")
                    }
                } else {
                    sb.append(lineText)
                }
            }
            if (lineIdx < cleanLines.lastIndex) sb.append('\n')
        }
        onGeneratedLrc(sb.toString())
    }

    if (cleanLines.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .clip(OneUISquircleMedium)
                .background(Color(0x18FFFFFF))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Paste lyric text in the \"Paste & Edit\" tab first, then tap along here to stamp line or word timestamps.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xBBFFFFFF),
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val currentLineText = cleanLines.getOrNull(cursorLineIdx).orEmpty()
    val currentLineWords = remember(currentLineText) {
        currentLineText.split(Regex("\\s+")).filter { it.isNotEmpty() }
    }
    val isFinished = cursorLineIdx >= cleanLines.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Granularity toggle (Word-by-Word vs Line-by-Line)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                EditorActionChip(
                    label = "Word-by-Word",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = primaryColor,
                    selected = granularity == TapSyncGranularity.WORD,
                    onClick = { granularity = TapSyncGranularity.WORD }
                )
                EditorActionChip(
                    label = "Line-by-Line",
                    icon = Icons.Default.MusicNote,
                    accentColor = primaryColor,
                    selected = granularity == TapSyncGranularity.LINE,
                    onClick = { granularity = TapSyncGranularity.LINE }
                )
            }

            Text(
                text = formatTime(currentPositionMs),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = primaryColor
            )
        }

        // Active Line & Word Preview Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(OneUISquircleMedium)
                .background(Color(0x1EFFFFFF))
                .border(1.dp, Color(0x28FFFFFF), OneUISquircleMedium)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isFinished) {
                Text(
                    text = "All ${cleanLines.size} lines synced! Tap \"Save & Apply\" below.",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF69F0AE),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Line ${cursorLineIdx + 1} of ${cleanLines.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0x99FFFFFF)
                )

                if (granularity == TapSyncGranularity.WORD) {
                    val annotatedLine = buildAnnotatedString {
                        currentLineWords.forEachIndexed { idx, word ->
                            val stamped = stampedLineWords.getOrNull(cursorLineIdx)?.getOrNull(idx) != null
                            when {
                                idx == cursorWordIdx -> {
                                    withStyle(
                                        SpanStyle(
                                            color = Color(0xFF0C0E1A),
                                            background = primaryColor,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    ) {
                                        append(" $word ")
                                    }
                                }
                                stamped -> {
                                    withStyle(
                                        SpanStyle(
                                            color = primaryColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append("$word ")
                                    }
                                }
                                else -> {
                                    withStyle(SpanStyle(color = Color(0xCCFFFFFF))) {
                                        append("$word ")
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        text = annotatedLine,
                        style = MaterialTheme.typography.titleMedium.copy(lineHeight = 26.sp)
                    )
                } else {
                    Text(
                        text = currentLineText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = primaryColor
                    )
                }

                val nextLinePreview = cleanLines.getOrNull(cursorLineIdx + 1)
                if (!nextLinePreview.isNullOrBlank()) {
                    Text(
                        text = "Next: $nextLinePreview",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x77FFFFFF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Primary Stamp Button + Undo + Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (!isFinished) {
                        val list = stampedLineWords[cursorLineIdx]
                        list.add(currentPositionMs)
                        tapHistory.add(cursorLineIdx to cursorWordIdx)

                        if (granularity == TapSyncGranularity.LINE || cursorWordIdx >= currentLineWords.lastIndex) {
                            cursorLineIdx += 1
                            cursorWordIdx = 0
                        } else {
                            cursorWordIdx += 1
                        }
                        emitLrcString()
                    }
                },
                enabled = !isFinished,
                shape = OneUIPillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color(0xFF0C0E1A)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("tap_sync_stamp_button")
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val targetLabel = if (isFinished) {
                    "Sync Complete"
                } else if (granularity == TapSyncGranularity.WORD) {
                    val wordPreview = currentLineWords.getOrNull(cursorWordIdx).orEmpty().take(12)
                    "Stamp \"$wordPreview\""
                } else {
                    "Stamp Line"
                }
                Text(
                    text = targetLabel,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OutlinedButton(
                onClick = {
                    if (tapHistory.isNotEmpty()) {
                        val (prevLine, prevWord) = tapHistory.removeAt(tapHistory.lastIndex)
                        val lineTimes = stampedLineWords.getOrNull(prevLine)
                        if (lineTimes != null && lineTimes.isNotEmpty()) {
                            lineTimes.removeAt(lineTimes.lastIndex)
                        }
                        cursorLineIdx = prevLine
                        cursorWordIdx = prevWord
                        emitLrcString()
                    }
                },
                enabled = tapHistory.isNotEmpty(),
                shape = OneUIPillShape,
                border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo Last Stamp",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            OutlinedButton(
                onClick = {
                    stampedLineWords.forEach { it.clear() }
                    tapHistory.clear()
                    cursorLineIdx = 0
                    cursorWordIdx = 0
                },
                shape = OneUIPillShape,
                border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart Tap-Sync",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun StudioTabPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    primaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (selected) primaryColor else Color.Transparent,
        animationSpec = tween(180),
        label = "studio_tab_bg"
    )
    val contentColor = if (selected) Color(0xFF0C0E1A) else Color.White
    Row(
        modifier = modifier
            .clip(OneUIPillShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun EditorActionChip(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    Row(
        modifier = modifier
            .clip(OneUIPillShape)
            .background(if (selected) accentColor.copy(alpha = 0.25f) else Color(0x1EFFFFFF))
            .border(
                width = 1.dp,
                color = if (selected) accentColor else accentColor.copy(alpha = 0.45f),
                shape = OneUIPillShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            maxLines = 1,
            softWrap = false
        )
    }
}
