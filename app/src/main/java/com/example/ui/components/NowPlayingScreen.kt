package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.coroutines.cancellation.CancellationException
import com.example.model.ButtonAnimationEffect
import com.example.model.ProgressBarDesign
import com.example.model.ProgressBarThickness
import com.example.model.PlayerButtonSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.example.data.AnimatedArtRepository
import com.example.data.AnimatedCoverMedia
import com.example.data.LyricsRepository
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.example.model.AlbumArtSize
import com.example.model.ElementColors
import com.example.model.LyricLine
import com.example.model.LyricsAnimationSpeed
import com.example.model.LyricsAnimationStyle
import com.example.model.RepeatMode
import com.example.model.Track
import com.example.model.VisualizerStyle
import com.example.ui.theme.MotionSpecs
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.OneUISquircleLarge
import com.example.ui.theme.OneUISquircleMedium
import com.example.ui.theme.appSharedBounds
import com.example.ui.theme.glassmorphism
import com.example.ui.theme.parseHexColor
import kotlin.math.PI
import kotlin.math.sin

enum class DisplayMode {
    ARTWORK, LYRICS, VISUALIZER, SPLIT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    track: Track,
    isPlaying: Boolean,
    currentPositionMs: Long,
    visualizerAmplitudes: List<Float>,
    isShuffleActive: Boolean,
    repeatMode: RepeatMode,
    volume: Float,
    speed: Float,
    sleepTimerMinutesLeft: Int?,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onToggleFlexMode: (() -> Unit)? = null,
    progressBarDesign: ProgressBarDesign = ProgressBarDesign.WAVY,
    playerButtonSize: PlayerButtonSize = PlayerButtonSize.MEDIUM,
    visualizerAlbumArtSize: AlbumArtSize = AlbumArtSize.MEDIUM,
    progressBarThickness: ProgressBarThickness = ProgressBarThickness.MEDIUM,
    visualizerStyle: VisualizerStyle = VisualizerStyle.BARS,
    onVisualizerStyleChange: (VisualizerStyle) -> Unit = {},
    buttonAnimationEffect: ButtonAnimationEffect = ButtonAnimationEffect.BOUNCY_SPRING,
    elementColors: ElementColors = ElementColors(),
    isCollapsible: Boolean = true,
    isLoadingLyrics: Boolean = false,
    lyricsAnimationStyle: LyricsAnimationStyle = LyricsAnimationStyle.SMOOTH_FLOAT,
    lyricsAnimationSpeed: LyricsAnimationSpeed = LyricsAnimationSpeed.BALANCED,
    lyricsGlowEnabled: Boolean = true,
    displayMode: DisplayMode = DisplayMode.ARTWORK,
    onDisplayModeChange: (DisplayMode) -> Unit = {},
    enableAnimatedCovers: Boolean = true,
    autoFetchAnimatedCoversWifiOnly: Boolean = true,
    simulateMotionOnStaticCovers: Boolean = true,
    onImportAnimatedCover: (Uri) -> Unit = {},
    onRemoveCustomAnimatedCover: () -> Unit = {},
    onImportLyricsUri: (Uri) -> Unit = {},
    onSavePastedLyrics: (String) -> Unit = {},
    loadEditableLyrics: (suspend () -> String)? = null,
    onRefetchLyrics: (() -> Unit)? = null,
    onClearLyrics: (() -> Unit)? = null,
    onOpenExternalLyricsEditor: ((LyricsEditorTab) -> Unit)? = null,
    enableSharedPlayerTransition: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Only intercept back swipe gesture if the player is collapsible (e.g. modal full-screen in handheld mode).
    // In persistent dual-pane unfolded mode, isCollapsible is false so system back edge swipes cleanly exit the app.
    PredictiveBackHandler(enabled = isCollapsible) { progress ->
        try {
            progress.collect { /* tracks swipe progress */ }
            onCollapse()
        } catch (e: CancellationException) {
            // Cancelled back gesture
        }
    }

    val context = LocalContext.current
    var localDisplayMode by remember { mutableStateOf(displayMode) }
    LaunchedEffect(displayMode) {
        localDisplayMode = displayMode
    }
    val updateDisplayMode: (DisplayMode) -> Unit = { newMode ->
        localDisplayMode = newMode
        onDisplayModeChange(newMode)
    }

    var showAnimatedCoverDialog by remember { mutableStateOf(false) }
    val animatedCoverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportAnimatedCover(uri)
        }
    }

    var isDraggingSlider by remember(track.id) { mutableStateOf(false) }
    var sliderDragPosition by remember(track.id) { mutableFloatStateOf(0f) }

    val activePositionMs = if (isDraggingSlider) {
        (sliderDragPosition * track.durationMs).toLong()
    } else {
        currentPositionMs
    }

    // Dynamic animated colors with ExpressiveSpring physics
    val animatedPrimary by animateColorAsState(
        targetValue = track.primaryColor,
        animationSpec = MotionSpecs.expressiveSpring(),
        label = "bg_primary"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = track.secondaryColor,
        animationSpec = MotionSpecs.expressiveSpring(),
        label = "bg_secondary"
    )
    val animatedAccent by animateColorAsState(
        targetValue = track.accentColor,
        animationSpec = MotionSpecs.expressiveSpring(),
        label = "bg_accent"
    )

    val lyricsController = rememberLyricsStudioController(
        track = track,
        currentPositionMs = activePositionMs,
        accentColor = animatedAccent,
        onImportLyricsUri = onImportLyricsUri,
        onSavePastedLyrics = onSavePastedLyrics,
        loadEditableLyrics = loadEditableLyrics,
        onClearLyrics = onClearLyrics,
        onOpenExternalEditor = onOpenExternalLyricsEditor
    )

    val animatedArtCacheVersion by AnimatedArtRepository.cacheVersion.collectAsStateWithLifecycle()

    if (showAnimatedCoverDialog) {
        val hasCustom = remember(track.id, track.artist, track.album, track.title, animatedArtCacheVersion) {
            AnimatedArtRepository.getCachedOrSidecarImmediate(context, track) != AnimatedCoverMedia.None
        }
        AnimatedCoverOptionsDialog(
            track = track,
            hasCustomCover = hasCustom,
            accentColor = animatedAccent,
            onDismiss = { showAnimatedCoverDialog = false },
            onPickAnimatedFile = {
                animatedCoverPickerLauncher.launch(
                    arrayOf("video/mp4", "video/webm", "image/gif", "image/webp")
                )
            },
            onRemoveCustomCover = onRemoveCustomAnimatedCover
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .appSharedBounds(
                key = "player_container",
                enabled = enableSharedPlayerTransition,
                clipShape = OneUISquircleLarge
            )
            .background(Color(0xFF0F101C))
    ) {
        // Ambient background glow reflecting track colors
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedPrimary.copy(alpha = 0.35f),
                            animatedSecondary.copy(alpha = 0.18f),
                            Color(0xFF0E0F1A)
                        ),
                        radius = 1200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with reachability styling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OneUITooltipButton(
                    name = "Collapse Player",
                    onClick = onCollapse
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .glassmorphism(shape = CircleShape, backgroundColor = Color(0x33FFFFFF))
                            .testTag("collapse_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse Player",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {


                    OneUITooltipButton(
                        name = "SoundAlive Equalizer",
                        onClick = onOpenEqualizer
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .glassmorphism(shape = CircleShape, backgroundColor = Color(0x33FFFFFF))
                                .testTag("equalizer_quick_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Equalizer,
                                contentDescription = "SoundAlive Equalizer",
                                tint = animatedAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display Mode Switcher (Segmented Glass Capsule)
            Row(
                modifier = Modifier
                    .glassmorphism(
                        shape = OneUIPillShape,
                        backgroundColor = Color(0x28FFFFFF),
                        borderWidth = 1.dp
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ModePillButton(
                    selected = localDisplayMode == DisplayMode.ARTWORK,
                    title = "Artwork",
                    accentColor = animatedAccent,
                    onClick = { updateDisplayMode(DisplayMode.ARTWORK) }
                )
                LyricsModePillWithDropdown(
                    selected = localDisplayMode == DisplayMode.LYRICS,
                    isLyricsVisible = localDisplayMode == DisplayMode.LYRICS || localDisplayMode == DisplayMode.SPLIT,
                    controller = lyricsController,
                    accentColor = animatedAccent,
                    onClick = { updateDisplayMode(DisplayMode.LYRICS) },
                    onEnsureLyricsMode = {
                        if (localDisplayMode != DisplayMode.LYRICS && localDisplayMode != DisplayMode.SPLIT) {
                            updateDisplayMode(DisplayMode.LYRICS)
                        }
                    },
                    onRefetchOnline = onRefetchLyrics,
                    onClearLyrics = onClearLyrics
                )
                VisualizerModePillWithDropdown(
                    selected = localDisplayMode == DisplayMode.VISUALIZER,
                    currentStyle = visualizerStyle,
                    accentColor = animatedAccent,
                    onClick = { updateDisplayMode(DisplayMode.VISUALIZER) },
                    onStyleSelected = { newStyle ->
                        updateDisplayMode(DisplayMode.VISUALIZER)
                        onVisualizerStyleChange(newStyle)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Seamless Middle Visual (Artwork vs Lyrics vs Visualizer)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val maxAvailableArtHeight = (maxHeight - 12.dp).coerceAtLeast(100.dp)
                AnimatedContent(
                    targetState = localDisplayMode,
                    transitionSpec = {
                        (fadeIn(animationSpec = MotionSpecs.snappySpring()) + scaleIn(
                            initialScale = 0.96f,
                            animationSpec = MotionSpecs.snappySpring()
                        )) togetherWith (fadeOut(animationSpec = MotionSpecs.snappySpring()) + scaleOut(
                            targetScale = 0.96f,
                            animationSpec = MotionSpecs.snappySpring()
                        ))
                    },
                    label = "center_content_switch"
                ) { mode ->
                    when (mode) {
                        DisplayMode.ARTWORK -> {
                            var dragHorizontalAmount by remember { mutableFloatStateOf(0f) }
                            Box(
                                modifier = Modifier
                                    .pointerInput(track.id) {
                                        detectHorizontalDragGestures(
                                            onDragStart = { dragHorizontalAmount = 0f },
                                            onDragEnd = {
                                                if (dragHorizontalAmount < -45f) {
                                                    onNext()
                                                } else if (dragHorizontalAmount > 45f) {
                                                    onPrevious()
                                                }
                                                dragHorizontalAmount = 0f
                                            },
                                            onDragCancel = { dragHorizontalAmount = 0f },
                                            onHorizontalDrag = { _, dragAmount ->
                                                dragHorizontalAmount += dragAmount
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val visualizerArtSizeDp = when (visualizerAlbumArtSize) {
                                    AlbumArtSize.SMALL -> 210.dp
                                    AlbumArtSize.MEDIUM -> 255.dp
                                    AlbumArtSize.LARGE -> 295.dp
                                    AlbumArtSize.EXTRA_LARGE -> 330.dp
                                    AlbumArtSize.JUMBO -> 365.dp
                                }
                                val fittedArtSize = minOf(visualizerArtSizeDp, maxAvailableArtHeight)
                                CoverArtView(
                                    track = track,
                                    size = fittedArtSize,
                                    cornerRadius = (fittedArtSize.value * 0.11f).dp,
                                    isPlaying = isPlaying,
                                    showVinylPeek = maxAvailableArtHeight >= 150.dp,
                                    animatePlaybackScale = true,
                                    artSharedElementKey = if (enableSharedPlayerTransition) "player_artwork_${track.id}" else null,
                                    enableAnimatedCover = enableAnimatedCovers,
                                    autoFetchWifiOnly = autoFetchAnimatedCoversWifiOnly,
                                    simulateMotionOnStaticCovers = simulateMotionOnStaticCovers,
                                    onLongPress = { showAnimatedCoverDialog = true },
                                    modifier = Modifier.testTag("hero_cover_art")
                                )
                            }
                        }
                        DisplayMode.LYRICS -> {
                            LyricsView(
                                lyrics = track.lyrics,
                                currentPositionMs = activePositionMs,
                                primaryColor = animatedAccent,
                                isLoading = isLoadingLyrics,
                                trackDurationMs = track.durationMs,
                                animationStyle = lyricsAnimationStyle,
                                animationSpeed = lyricsAnimationSpeed,
                                glowEnabled = lyricsGlowEnabled,
                                onSelectLyric = { line ->
                                    if (line.isSynced) onSeek(line.timeMs)
                                },
                                onImportLyricsUri = onImportLyricsUri,
                                onSavePastedLyrics = onSavePastedLyrics,
                                loadEditableLyrics = loadEditableLyrics,
                                onClearLyrics = onClearLyrics,
                                studioController = lyricsController
                            )
                        }
                        DisplayMode.VISUALIZER -> {
                            var visualizerVerticalAmount by remember { mutableFloatStateOf(0f) }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .glassmorphism(
                                        shape = OneUISquircleLarge,
                                        backgroundColor = Color(0x2B1F223D)
                                    )
                                    .pointerInput(Unit) {
                                        detectVerticalDragGestures(
                                            onDragStart = { visualizerVerticalAmount = 0f },
                                            onDragEnd = {
                                                if (visualizerVerticalAmount > 50f) {
                                                    onCollapse()
                                                }
                                                visualizerVerticalAmount = 0f
                                            },
                                            onDragCancel = { visualizerVerticalAmount = 0f },
                                            onVerticalDrag = { _, dragAmount ->
                                                visualizerVerticalAmount += dragAmount
                                            }
                                        )
                                    }
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val visPeak = parseHexColor(elementColors.visualizerPeakColor)
                                val visBar = parseHexColor(elementColors.visualizerBarColor)
                                val visBase = parseHexColor(elementColors.visualizerBaseColor)
                                val visGlow = parseHexColor(elementColors.visualizerGlowColor)

                                AudioVisualizer(
                                    amplitudes = visualizerAmplitudes,
                                    primaryColor = visBar ?: animatedPrimary,
                                    secondaryColor = visBase ?: animatedAccent,
                                    peakColor = visPeak,
                                    barBodyColor = visBar,
                                    baseColor = visBase,
                                    glowColor = visGlow,
                                    style = visualizerStyle,
                                    maxHeight = 140.dp,
                                    barWidth = 6.dp,
                                    isPlaying = isPlaying
                                )
                            }
                        }
                        DisplayMode.SPLIT -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val splitArtSize = minOf(180.dp, maxAvailableArtHeight)
                                    CoverArtView(
                                        track = track,
                                        size = splitArtSize,
                                        cornerRadius = (splitArtSize.value * 0.15f).dp,
                                        isPlaying = isPlaying,
                                        showVinylPeek = false,
                                        animatePlaybackScale = true,
                                        artSharedElementKey = if (enableSharedPlayerTransition) "player_artwork_${track.id}" else null,
                                        enableAnimatedCover = enableAnimatedCovers,
                                        autoFetchWifiOnly = autoFetchAnimatedCoversWifiOnly,
                                        simulateMotionOnStaticCovers = simulateMotionOnStaticCovers,
                                        onLongPress = { showAnimatedCoverDialog = true },
                                        modifier = Modifier.testTag("split_cover_art")
                                    )
                                }
                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    LyricsView(
                                        lyrics = track.lyrics,
                                        currentPositionMs = activePositionMs,
                                        primaryColor = animatedAccent,
                                        isLoading = isLoadingLyrics,
                                        trackDurationMs = track.durationMs,
                                        animationStyle = lyricsAnimationStyle,
                                        animationSpeed = lyricsAnimationSpeed,
                                        glowEnabled = lyricsGlowEnabled,
                                        onSelectLyric = { line ->
                                            if (line.isSynced) onSeek(line.timeMs)
                                        },
                                        onImportLyricsUri = onImportLyricsUri,
                                        onSavePastedLyrics = onSavePastedLyrics,
                                        loadEditableLyrics = loadEditableLyrics,
                                        onClearLyrics = onClearLyrics,
                                        studioController = lyricsController
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Track Title & Favorite Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .appSharedBounds(
                                key = "player_title_${track.id}",
                                enabled = enableSharedPlayerTransition,
                                useScaleToBounds = true
                            )
                            .basicMarquee(iterations = Int.MAX_VALUE)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${track.artist} — ${track.album}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xCCFFFFFF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OneUITooltipButton(
                    name = if (track.isFavorite) "Remove from Favorites" else "Add to Favorites",
                    onClick = onToggleFavorite
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .glassmorphism(shape = CircleShape, backgroundColor = Color(0x28FFFFFF))
                            .testTag("favorite_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        val favScale by animateFloatAsState(
                            targetValue = if (track.isFavorite) 1.12f else 1.0f,
                            animationSpec = MotionSpecs.TactilePressSpring,
                            label = "fav_scale"
                        )
                        AnimatedContent(
                            targetState = track.isFavorite,
                            transitionSpec = { MotionSpecs.iconMorphTransition() },
                            label = "now_playing_fav_morph"
                        ) { isFav ->
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) Color(0xFFFF4081) else Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        scaleX = favScale
                                        scaleY = favScale
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrubber Slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                ExpressiveScrubber(
                    progress = if (track.durationMs > 0) {
                        (activePositionMs.toFloat() / track.durationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f,
                    onProgressChange = { fraction ->
                        isDraggingSlider = true
                        sliderDragPosition = fraction
                    },
                    onProgressChangeFinished = {
                        isDraggingSlider = false
                        onSeek((sliderDragPosition * track.durationMs).toLong())
                    },
                    design = progressBarDesign,
                    thickness = progressBarThickness,
                    accentColor = animatedAccent,
                    isPlaying = isPlaying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scrubber_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(activePositionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xAAFFFFFF)
                    )
                    Text(
                        text = formatTime(track.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xAAFFFFFF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Playback Controls with BoxWithConstraints for Auto-Fitting & Animation
            val playBtnWidth = (86 * playerButtonSize.scale).dp
            val playBtnHeight = (66 * playerButtonSize.scale).dp
            val playIconSize = (36 * playerButtonSize.scale).dp
            val skipBtnSize = (52 * playerButtonSize.scale).dp
            val skipIconSize = (30 * playerButtonSize.scale).dp
            val modeBtnSize = (44 * playerButtonSize.scale).dp
            val modeIconSize = (24 * playerButtonSize.scale).dp

            // Press & interaction states for tactile micro-interactions
            val prevInteractionSource = remember { MutableInteractionSource() }
            val isPrevPressed by prevInteractionSource.collectIsPressedAsState()

            val nextInteractionSource = remember { MutableInteractionSource() }
            val isNextPressed by nextInteractionSource.collectIsPressedAsState()

            val playInteractionSource = remember { MutableInteractionSource() }
            val isPlayPressed by playInteractionSource.collectIsPressedAsState()

            val (prevTargetScaleX, prevTargetScaleY, prevTargetRotation) = when (buttonAnimationEffect) {
                ButtonAnimationEffect.BOUNCY_SPRING -> Triple(if (isPrevPressed) 0.85f else 1.0f, if (isPrevPressed) 0.85f else 1.0f, if (isPrevPressed) -8f else 0f)
                ButtonAnimationEffect.ROTATION_TILT -> Triple(if (isPrevPressed) 0.88f else 1.0f, if (isPrevPressed) 0.88f else 1.0f, if (isPrevPressed) -16f else 0f)
                ButtonAnimationEffect.NEON_GLOW_POP -> Triple(if (isPrevPressed) 1.20f else 1.0f, if (isPrevPressed) 1.20f else 1.0f, 0f)
                ButtonAnimationEffect.JELLY_SQUISH -> Triple(if (isPrevPressed) 1.25f else 1.0f, if (isPrevPressed) 0.78f else 1.0f, if (isPrevPressed) -4f else 0f)
                ButtonAnimationEffect.SUBTLE_HAPTIC -> Triple(if (isPrevPressed) 0.95f else 1.0f, if (isPrevPressed) 0.95f else 1.0f, 0f)
            }

            val (nextTargetScaleX, nextTargetScaleY, nextTargetRotation) = when (buttonAnimationEffect) {
                ButtonAnimationEffect.BOUNCY_SPRING -> Triple(if (isNextPressed) 0.85f else 1.0f, if (isNextPressed) 0.85f else 1.0f, if (isNextPressed) 8f else 0f)
                ButtonAnimationEffect.ROTATION_TILT -> Triple(if (isNextPressed) 0.88f else 1.0f, if (isNextPressed) 0.88f else 1.0f, if (isNextPressed) 16f else 0f)
                ButtonAnimationEffect.NEON_GLOW_POP -> Triple(if (isNextPressed) 1.20f else 1.0f, if (isNextPressed) 1.20f else 1.0f, 0f)
                ButtonAnimationEffect.JELLY_SQUISH -> Triple(if (isNextPressed) 1.25f else 1.0f, if (isNextPressed) 0.78f else 1.0f, if (isNextPressed) 4f else 0f)
                ButtonAnimationEffect.SUBTLE_HAPTIC -> Triple(if (isNextPressed) 0.95f else 1.0f, if (isNextPressed) 0.95f else 1.0f, 0f)
            }

            val (playTargetScaleX, playTargetScaleY) = when (buttonAnimationEffect) {
                ButtonAnimationEffect.BOUNCY_SPRING -> {
                    val s = if (isPlayPressed) 0.88f else if (isPlaying) 1.05f else 1.0f
                    s to s
                }
                ButtonAnimationEffect.ROTATION_TILT -> {
                    val s = if (isPlayPressed) 0.88f else if (isPlaying) 1.06f else 1.0f
                    s to s
                }
                ButtonAnimationEffect.NEON_GLOW_POP -> {
                    val s = if (isPlayPressed) 1.22f else if (isPlaying) 1.08f else 1.0f
                    s to s
                }
                ButtonAnimationEffect.JELLY_SQUISH -> {
                    if (isPlayPressed) 1.22f to 0.82f else if (isPlaying) 1.04f to 1.04f else 1.0f to 1.0f
                }
                ButtonAnimationEffect.SUBTLE_HAPTIC -> {
                    val s = if (isPlayPressed) 0.96f else 1.0f
                    s to s
                }
            }

            val springSpec = when (buttonAnimationEffect) {
                ButtonAnimationEffect.BOUNCY_SPRING -> MotionSpecs.TactilePressSpring
                ButtonAnimationEffect.ROTATION_TILT -> MotionSpecs.ExpressiveSpring
                ButtonAnimationEffect.NEON_GLOW_POP -> spring<Float>(dampingRatio = 0.58f, stiffness = Spring.StiffnessMedium)
                ButtonAnimationEffect.JELLY_SQUISH -> spring<Float>(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)
                ButtonAnimationEffect.SUBTLE_HAPTIC -> MotionSpecs.SnappySpring
            }

            val prevScaleX by animateFloatAsState(targetValue = prevTargetScaleX, animationSpec = springSpec, label = "prev_sx")
            val prevScaleY by animateFloatAsState(targetValue = prevTargetScaleY, animationSpec = springSpec, label = "prev_sy")
            val prevRotation by animateFloatAsState(targetValue = prevTargetRotation, animationSpec = springSpec, label = "prev_rot")

            val nextScaleX by animateFloatAsState(targetValue = nextTargetScaleX, animationSpec = springSpec, label = "next_sx")
            val nextScaleY by animateFloatAsState(targetValue = nextTargetScaleY, animationSpec = springSpec, label = "next_sy")
            val nextRotation by animateFloatAsState(targetValue = nextTargetRotation, animationSpec = springSpec, label = "next_rot")

            val playScaleX by animateFloatAsState(targetValue = playTargetScaleX, animationSpec = springSpec, label = "play_sx")
            val playScaleY by animateFloatAsState(targetValue = playTargetScaleY, animationSpec = springSpec, label = "play_sy")

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                val availableWidth = maxWidth
                val totalNeededWidth = playBtnWidth + (skipBtnSize * 2) + (modeBtnSize * 2) + 24.dp
                // Auto-fit factor to guarantee all 5 buttons fit comfortably without pushing repeat off screen
                val fitFactor = if (totalNeededWidth > availableWidth - 8.dp) {
                    ((availableWidth - 8.dp) / totalNeededWidth).coerceIn(0.55f, 1f)
                } else 1f

                val effectivePlayW = playBtnWidth * fitFactor
                val effectivePlayH = playBtnHeight * fitFactor
                val effectivePlayIcon = playIconSize * fitFactor
                val effectiveSkipBtn = skipBtnSize * fitFactor
                val effectiveSkipIcon = skipIconSize * fitFactor
                val effectiveModeBtn = modeBtnSize * fitFactor
                val effectiveModeIcon = modeIconSize * fitFactor

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = (4 * fitFactor).dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle button with blurred glass look & SnappySpring icon morph
                    OneUITooltipButton(
                        name = if (isShuffleActive) "Shuffle: Enabled" else "Shuffle: Disabled",
                        onClick = onToggleShuffle
                    ) {
                        val shuffleBg by animateColorAsState(
                            targetValue = if (isShuffleActive) animatedAccent.copy(alpha = 0.28f) else Color(0x20FFFFFF),
                            animationSpec = MotionSpecs.snappySpring(),
                            label = "shuffle_bg"
                        )
                        Box(
                            modifier = Modifier
                                .size(effectiveModeBtn)
                                .glassmorphism(
                                    shape = CircleShape,
                                    backgroundColor = shuffleBg,
                                    borderWidth = 1.dp
                                )
                                .testTag("shuffle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = isShuffleActive,
                                transitionSpec = { MotionSpecs.iconMorphTransition() },
                                label = "shuffle_icon_morph"
                            ) { active ->
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = if (active) animatedAccent else Color(0x88FFFFFF),
                                    modifier = Modifier.size(effectiveModeIcon)
                                )
                            }
                        }
                    }

                    // Previous button with blurred glass look & tactile spring press + tilt
                    OneUITooltipButton(
                        name = "Previous Track",
                        onClick = onPrevious,
                        interactionSource = prevInteractionSource
                    ) {
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = prevScaleX
                                    scaleY = prevScaleY
                                    rotationZ = prevRotation
                                }
                                .size(effectiveSkipBtn)
                                .glassmorphism(shape = CircleShape, backgroundColor = Color(0x28FFFFFF))
                                .testTag("prev_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Track",
                                tint = Color.White,
                                modifier = Modifier.size(effectiveSkipIcon)
                            )
                        }
                    }

                    // Hero Play/Pause Button (Signature blurred glass pill with animated morph and spring pulse)
                    OneUITooltipButton(
                        name = if (isPlaying) "Pause Playback" else "Play Track",
                        onClick = onTogglePlayPause,
                        interactionSource = playInteractionSource
                    ) {
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = playScaleX
                                    scaleY = playScaleY
                                }
                                .size(width = effectivePlayW, height = effectivePlayH)
                                .glassmorphism(
                                    shape = OneUIPillShape,
                                    backgroundColor = animatedPrimary.copy(alpha = 0.85f),
                                    borderWidth = 1.5.dp,
                                    elevation = 16.dp
                                )
                                .testTag("play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = isPlaying,
                                transitionSpec = { MotionSpecs.iconMorphTransition() },
                                label = "play_pause_icon_crossfade"
                            ) { playing ->
                                Icon(
                                    imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playing) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(effectivePlayIcon)
                                )
                            }
                        }
                    }

                    // Next button with blurred glass look & tactile spring press + tilt
                    OneUITooltipButton(
                        name = "Next Track",
                        onClick = onNext,
                        interactionSource = nextInteractionSource
                    ) {
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = nextScaleX
                                    scaleY = nextScaleY
                                    rotationZ = nextRotation
                                }
                                .size(effectiveSkipBtn)
                                .glassmorphism(shape = CircleShape, backgroundColor = Color(0x28FFFFFF))
                                .testTag("next_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Track",
                                tint = Color.White,
                                modifier = Modifier.size(effectiveSkipIcon)
                            )
                        }
                    }

                    // Repeat button with blurred glass look & SnappySpring icon morph
                    OneUITooltipButton(
                        name = when (repeatMode) {
                            RepeatMode.OFF -> "Repeat: Off"
                            RepeatMode.ALL -> "Repeat: All Tracks"
                            RepeatMode.ONE -> "Repeat: Current Track"
                        },
                        onClick = onCycleRepeat
                    ) {
                        val repeatBg by animateColorAsState(
                            targetValue = if (repeatMode != RepeatMode.OFF) animatedAccent.copy(alpha = 0.28f) else Color(0x20FFFFFF),
                            animationSpec = MotionSpecs.snappySpring(),
                            label = "repeat_bg"
                        )
                        Box(
                            modifier = Modifier
                                .size(effectiveModeBtn)
                                .glassmorphism(
                                    shape = CircleShape,
                                    backgroundColor = repeatBg,
                                    borderWidth = 1.dp
                                )
                                .testTag("repeat_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = repeatMode,
                                transitionSpec = { MotionSpecs.iconMorphTransition() },
                                label = "repeat_icon_morph"
                            ) { mode ->
                                Icon(
                                    imageVector = if (mode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                    contentDescription = "Repeat",
                                    tint = if (mode != RepeatMode.OFF) animatedAccent else Color(0x88FFFFFF),
                                    modifier = Modifier.size(effectiveModeIcon)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModePillButton(
    selected: Boolean,
    title: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val pillBg by animateColorAsState(
        targetValue = if (selected) accentColor else Color.Transparent,
        animationSpec = MotionSpecs.snappySpring(),
        label = "mode_pill_bg"
    )
    OneUITooltipButton(
        name = "$title View",
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .clip(OneUIPillShape)
                .background(pillBg)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) Color(0xFF0D0F1B) else Color.White
            )
        }
    }
}

/**
 * Visualizer Mode Pill with direct dropdown menu allowing users to switch visualizer styles on the fly.
 */
@Composable
fun VisualizerModePillWithDropdown(
    selected: Boolean,
    currentStyle: VisualizerStyle,
    accentColor: Color,
    onClick: () -> Unit,
    onStyleSelected: (VisualizerStyle) -> Unit
) {
    var isDropdownOpen by remember { mutableStateOf(false) }
    val pillBg by animateColorAsState(
        targetValue = if (selected) accentColor else Color.Transparent,
        animationSpec = MotionSpecs.snappySpring(),
        label = "visualizer_pill_bg"
    )

    Box {
        OneUITooltipButton(
            name = "Visualizer View (${currentStyle.label}) - Tap or Dropdown to Switch Style",
            onClick = {
                onClick()
                if (selected) {
                    isDropdownOpen = true
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .clip(OneUIPillShape)
                    .background(pillBg)
                    .padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Visualizer",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (selected) Color(0xFF0D0F1B) else Color.White
                    )
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable {
                                onClick()
                                isDropdownOpen = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Visualizer Style",
                            tint = if (selected) Color(0xFF0D0F1B) else Color(0xCCFFFFFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = isDropdownOpen,
            onDismissRequest = { isDropdownOpen = false },
            modifier = Modifier
                .background(Color(0xF0181A2E))
                .border(1.dp, Color(0x35FFFFFF), OneUISquircleMedium)
        ) {
            Text(
                text = "Choose Visualizer Style",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
            VisualizerStyle.entries.forEach { style ->
                val isCurrent = currentStyle == style
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = style.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isCurrent) accentColor else Color.White
                            )
                        }
                    },
                    trailingIcon = {
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    onClick = {
                        onStyleSelected(style)
                        isDropdownOpen = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LyricsView(
    lyrics: List<LyricLine>,
    currentPositionMs: Long,
    primaryColor: Color,
    inactiveTextColor: Color = Color(0x66FFFFFF),
    isLoading: Boolean = false,
    trackDurationMs: Long = 180_000L,
    animationStyle: LyricsAnimationStyle = LyricsAnimationStyle.SMOOTH_FLOAT,
    animationSpeed: LyricsAnimationSpeed = LyricsAnimationSpeed.BALANCED,
    glowEnabled: Boolean = true,
    onSelectLyric: (LyricLine) -> Unit,
    onImportLyricsUri: (Uri) -> Unit = {},
    onSavePastedLyrics: (String) -> Unit = {},
    loadEditableLyrics: (suspend () -> String)? = null,
    onClearLyrics: (() -> Unit)? = null,
    studioController: LyricsStudioController? = null
) {
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val coroutineScope = rememberCoroutineScope()
    var showLocalDialog by remember { mutableStateOf(false) }
    var editorInitialTab by remember { mutableStateOf(LyricsEditorTab.PASTE_EDIT) }
    var pastedLyricsInput by remember { mutableStateOf("") }

    val localPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportLyricsUri(uri)
        }
    }

    val isSyncedTrack = lyrics.any { it.isSynced }
    val activeIndex = lyrics.indexOfLast { it.timeMs <= currentPositionMs }.coerceAtLeast(0)
    val speedMult = animationSpeed.durationMultiplier

    val openEditor: (LyricsEditorTab) -> Unit = { tab ->
        if (studioController != null) {
            studioController.openEditor(tab)
        } else {
            editorInitialTab = tab
            pastedLyricsInput = LyricsRepository.serializeLyricsToLrc(lyrics)
            showLocalDialog = true
            if (loadEditableLyrics != null) {
                coroutineScope.launch {
                    val loaded = loadEditableLyrics.invoke()
                    if (loaded.isNotBlank()) {
                        pastedLyricsInput = loaded
                    }
                }
            }
        }
    }

    val launchFilePicker: () -> Unit = {
        if (studioController != null) {
            studioController.launchFilePicker()
        } else {
            localPickerLauncher.launch(arrayOf("*/*"))
        }
    }

    val waveTransition = rememberInfiniteTransition(label = "lyrics_wave_transition")
    val wavePhase by waveTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI.toFloat()),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (2400f * speedMult).toInt().coerceAtLeast(800),
                easing = LinearEasing
            ),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "lyrics_wave_phase"
    )

    LaunchedEffect(activeIndex, lyrics.size) {
        if (lyrics.isNotEmpty() && isSyncedTrack) {
            listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .glassmorphism(
                shape = OneUISquircleLarge,
                backgroundColor = Color(0x281B1C33)
            )
            .padding(16.dp)
            .testTag("lyrics_view_container"),
        contentAlignment = Alignment.Center
    ) {
        val isNoLyrics = lyrics.isEmpty() || (lyrics.size == 1 && lyrics[0].text.contains("No embedded lyrics", ignoreCase = true))

        if (isLoading && isNoLyrics) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.testTag("lyrics_loading_indicator")
            ) {
                CircularProgressIndicator(
                    color = primaryColor,
                    modifier = Modifier.size(34.dp),
                    strokeWidth = 3.dp
                )
                Text(
                    text = "Searching lyrics...",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xCCFFFFFF)
                )
            }
        } else if (isNoLyrics) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
                    .testTag("lyrics_empty_state"),
                contentAlignment = Alignment.Center
            ) {
                val stackButtonsVertically = maxWidth < 420.dp
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "No Lyrics",
                        tint = Color(0x66FFFFFF),
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "No lyrics found",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Import a local .lrc / .txt file or paste lyrics manually",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val importButton: @Composable (Modifier) -> Unit = { btnModifier ->
                        Button(
                            onClick = launchFilePicker,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color(0xFF0C0E1A)
                            ),
                            shape = OneUIPillShape,
                            modifier = btnModifier
                                .heightIn(min = 48.dp)
                                .testTag("import_lrc_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = "Import .LRC File",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Import .LRC File",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    val pasteButton: @Composable (Modifier) -> Unit = { btnModifier ->
                        OutlinedButton(
                            onClick = {
                                if (studioController != null) {
                                    studioController.openEditor(LyricsEditorTab.PASTE_EDIT)
                                } else {
                                    pastedLyricsInput = ""
                                    editorInitialTab = LyricsEditorTab.PASTE_EDIT
                                    showLocalDialog = true
                                }
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = OneUIPillShape,
                            modifier = btnModifier
                                .heightIn(min = 48.dp)
                                .testTag("paste_lyrics_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste Lyrics",
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Paste Lyrics",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    if (stackButtonsVertically) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            importButton(Modifier.widthIn(min = 216.dp))
                            pasteButton(Modifier.widthIn(min = 216.dp))
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            importButton(Modifier)
                            pasteButton(Modifier)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                    itemsIndexed(lyrics) { index, line ->
                        val isActive = if (isSyncedTrack) index == activeIndex else false
                        val nextLineTime = lyrics.getOrNull(index + 1)?.timeMs ?: (line.timeMs + 4200L)
                        val lineDuration = (nextLineTime - line.timeMs).coerceAtLeast(800L)
                        val lineProgress = if (isActive && isSyncedTrack) {
                            ((currentPositionMs - line.timeMs).toFloat() / lineDuration.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        val durationMs = (320f * speedMult).toInt().coerceIn(120, 700)
                        val targetScale = when {
                            !isSyncedTrack -> 1.0f
                            !isActive && animationStyle == LyricsAnimationStyle.CINEMATIC_FOCUS -> 0.87f
                            !isActive -> 0.95f
                            animationStyle == LyricsAnimationStyle.ELASTIC_POP -> 1.12f
                            animationStyle == LyricsAnimationStyle.CINEMATIC_FOCUS -> 1.09f
                            else -> 1.06f
                        }
                        val targetAlpha = when {
                            !isSyncedTrack -> 0.94f
                            isActive -> 1.0f
                            animationStyle == LyricsAnimationStyle.CINEMATIC_FOCUS -> 0.34f
                            else -> 0.65f
                        }

                        val animScale by animateFloatAsState(
                            targetValue = targetScale,
                            animationSpec = if (animationStyle == LyricsAnimationStyle.ELASTIC_POP && isActive) {
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = (360f / speedMult).coerceIn(150f, 700f)
                                )
                            } else {
                                tween(durationMillis = durationMs)
                            },
                            label = "lyric_scale_$index"
                        )
                        val animAlpha by animateFloatAsState(
                            targetValue = targetAlpha,
                            animationSpec = tween(durationMillis = durationMs),
                            label = "lyric_alpha_$index"
                        )
                        val animColor by animateColorAsState(
                            targetValue = when {
                                isActive -> primaryColor
                                !isSyncedTrack -> Color(0xEEFFFFFF)
                                else -> inactiveTextColor
                            },
                            animationSpec = tween(durationMillis = durationMs),
                            label = "lyric_color_$index"
                        )
                        val animOffsetY by animateFloatAsState(
                            targetValue = if (isActive && animationStyle == LyricsAnimationStyle.SMOOTH_FLOAT) -4f else 0f,
                            animationSpec = tween(durationMillis = durationMs),
                            label = "lyric_offset_y_$index"
                        )

                        val waveDeltaY = if (animationStyle == LyricsAnimationStyle.WAVE_FLOW && isSyncedTrack) {
                            if (isActive) sin(wavePhase) * 7f else sin(wavePhase + index * 0.65f) * 2.5f
                        } else 0f

                        val baseTextStyle = if (isActive) {
                            MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        } else {
                            MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal)
                        }

                        val haloBg = if (isActive && glowEnabled && isSyncedTrack) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    primaryColor.copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        } else null

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = animScale
                                    scaleY = animScale
                                    alpha = animAlpha
                                    translationY = animOffsetY + waveDeltaY
                                }
                                .then(
                                    if (haloBg != null) {
                                        Modifier
                                            .clip(OneUIPillShape)
                                            .background(haloBg)
                                    } else {
                                        Modifier
                                    }
                                )
                                .combinedClickable(
                                    onClick = { if (line.isSynced) onSelectLyric(line) },
                                    onLongClick = { openEditor(LyricsEditorTab.PASTE_EDIT) }
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            when {
                                isActive && isSyncedTrack && line.words.isNotEmpty() -> {
                                    WordSyncedLyricText(
                                        line = line,
                                        nextLineTimeMs = nextLineTime,
                                        currentPositionMs = currentPositionMs,
                                        primaryColor = primaryColor,
                                        textStyle = baseTextStyle,
                                        animationStyle = animationStyle
                                    )
                                }
                                isActive && animationStyle == LyricsAnimationStyle.KARAOKE_SWEEP && isSyncedTrack -> {
                                    val sweepStop = lineProgress.coerceIn(0.03f, 0.93f)
                                    val fadeEnd = (sweepStop + 0.06f).coerceAtMost(1f)
                                    val sweepBrush = if (lineProgress >= 0.98f) {
                                        Brush.horizontalGradient(listOf(primaryColor, primaryColor))
                                    } else {
                                        Brush.horizontalGradient(
                                            0f to primaryColor,
                                            sweepStop to primaryColor,
                                            fadeEnd to Color.White.copy(alpha = 0.50f),
                                            1f to Color.White.copy(alpha = 0.50f)
                                        )
                                    }
                                    Text(
                                        text = line.text,
                                        style = baseTextStyle.copy(brush = sweepBrush),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                isActive && animationStyle == LyricsAnimationStyle.TYPEWRITER_REVEAL && isSyncedTrack -> {
                                    val rawCut = (line.text.length * lineProgress).toInt().coerceIn(0, line.text.length)
                                    val wordBoundaryCut = if (rawCut >= line.text.length) {
                                        line.text.length
                                    } else {
                                        val nextSpace = line.text.indexOf(' ', rawCut)
                                        if (nextSpace != -1 && nextSpace - rawCut <= 4) nextSpace else rawCut
                                    }
                                    val annotated = buildAnnotatedString {
                                        withStyle(SpanStyle(color = primaryColor)) {
                                            append(line.text.substring(0, wordBoundaryCut))
                                        }
                                        withStyle(SpanStyle(color = Color.White.copy(alpha = 0.40f))) {
                                            append(line.text.substring(wordBoundaryCut))
                                        }
                                    }
                                    Text(
                                        text = annotated,
                                        style = baseTextStyle,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                else -> {
                                    Text(
                                        text = line.text,
                                        style = baseTextStyle,
                                        color = animColor,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                    }
                }
            }
        }
    }

    if (studioController == null && showLocalDialog) {
        val hasExisting = !(lyrics.isEmpty() || (lyrics.size == 1 && lyrics[0].text.contains("No embedded lyrics", ignoreCase = true)))
        LyricsEditorDialog(
            initialText = pastedLyricsInput,
            initialTab = editorInitialTab,
            hasExistingLyrics = hasExisting,
            trackDurationMs = trackDurationMs,
            currentPositionMs = currentPositionMs,
            primaryColor = primaryColor,
            onDismiss = { showLocalDialog = false },
            onSave = { updatedRaw ->
                onSavePastedLyrics(updatedRaw)
            },
            onPickFile = launchFilePicker,
            onClearLyrics = onClearLyrics
        )
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
