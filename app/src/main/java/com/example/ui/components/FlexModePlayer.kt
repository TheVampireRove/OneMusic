package com.example.ui.components

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.coroutines.cancellation.CancellationException
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.basicMarquee
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.AnimatedArtRepository
import com.example.data.AnimatedCoverMedia
import com.example.model.ButtonAnimationEffect
import com.example.model.LyricsAnimationSpeed
import com.example.model.LyricsAnimationStyle
import com.example.model.ProgressBarDesign
import com.example.model.ProgressBarThickness
import com.example.model.TabletopButtonSize
import com.example.model.TabletopControlStyle
import com.example.model.TabletopAlbumArtSize
import com.example.model.TabletopTopLayout
import com.example.model.VisualizerStyle
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Speed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clipToBounds
import com.example.model.TabletopControlPositions
import kotlin.math.roundToInt
import com.example.model.ElementColors
import com.example.model.RepeatMode
import com.example.model.Track
import com.example.ui.theme.MotionSpecs
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.OneUISquircleLarge
import com.example.ui.theme.OneUISquircleMedium
import com.example.ui.theme.OneUISquircleSmall
import com.example.ui.theme.bouncableClick
import com.example.ui.theme.parseHexColor
import com.example.ui.theme.glassmorphism

/**
 * Samsung Tabletop Flex Mode UI for foldables (Galaxy Z Fold & Z Flip).
 * The screen is split into:
 * - Upper Half (Viewing Stage): Angled upward displaying Hero Cover Art, synchronized lyrics, or audio visualizer.
 * - Hinge Crease: Visual subtle foldable hinge divider.
 * - Lower Half (Control Console): Lying flat on surface with ergonomic thumb-reachable playback console.
 */
@Composable
fun FlexModePlayer(
    track: Track,
    isPlaying: Boolean,
    currentPositionMs: Long,
    visualizerAmplitudes: List<Float>,
    isShuffleActive: Boolean,
    repeatMode: RepeatMode,
    volume: Float,
    speed: Float,
    sleepTimerMinutesLeft: Int?,
    dolbyAtmosEnabled: Boolean,
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
    onExitFlexMode: () -> Unit,
    progressBarDesign: ProgressBarDesign = ProgressBarDesign.WAVY,
    progressBarThickness: ProgressBarThickness = ProgressBarThickness.MEDIUM,
    tabletopButtonSize: TabletopButtonSize = TabletopButtonSize.MEDIUM,
    tabletopControlStyle: TabletopControlStyle = TabletopControlStyle.ONE_UI_TACTILE,
    tabletopAlbumArtSize: TabletopAlbumArtSize = TabletopAlbumArtSize.MEDIUM,
    tabletopControlsScale: Float = 1.0f,
    tabletopControlPositions: TabletopControlPositions = TabletopControlPositions(),
    tabletopImmersiveMode: Boolean = false,
    tabletopTopLayout: TabletopTopLayout = TabletopTopLayout.STANDARD,
    visualizerStyle: VisualizerStyle = VisualizerStyle.BARS,
    onVisualizerStyleChange: (VisualizerStyle) -> Unit = {},
    tabletopReflectedBackgroundEnabled: Boolean = false,
    tabletopShowVolumeBar: Boolean = true,
    allTracks: List<Track> = emptyList(),
    onTrackClick: (Track) -> Unit = {},
    onPlayFromContext: (List<Track>, Int) -> Unit = { list, idx ->
        if (idx in list.indices) onTrackClick(list[idx])
    },
    onUpdateControlPositions: (TabletopControlPositions) -> Unit = {},
    elementColors: ElementColors = ElementColors(),
    useCustomElementColors: Boolean = false,
    buttonAnimationEffect: ButtonAnimationEffect = ButtonAnimationEffect.BOUNCY_SPRING,
    lyricsAnimationStyle: LyricsAnimationStyle = LyricsAnimationStyle.SMOOTH_FLOAT,
    lyricsAnimationSpeed: LyricsAnimationSpeed = LyricsAnimationSpeed.BALANCED,
    lyricsGlowEnabled: Boolean = true,
    isLoadingLyrics: Boolean = false,
    displayMode: DisplayMode = DisplayMode.ARTWORK,
    onDisplayModeChange: (DisplayMode) -> Unit = {},
    enableAnimatedCovers: Boolean = true,
    autoFetchAnimatedCoversWifiOnly: Boolean = true,
    simulateMotionOnStaticCovers: Boolean = true,
    onImportAnimatedCover: (Uri) -> Unit = {},
    onRemoveCustomAnimatedCover: () -> Unit = {},
    onImportLyricsUri: (android.net.Uri) -> Unit = {},
    onSavePastedLyrics: (String) -> Unit = {},
    loadEditableLyrics: (suspend () -> String)? = null,
    onRefetchLyrics: (() -> Unit)? = null,
    onClearLyrics: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(tabletopImmersiveMode) {
        if (tabletopImmersiveMode) {
            try {
                val window = activity?.window
                val decor = try { window?.decorView } catch (_: Exception) { null }
                if (window != null && decor != null) {
                    val insetsController = WindowCompat.getInsetsController(window, decor)
                    insetsController.hide(WindowInsetsCompat.Type.statusBars())
                    insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } catch (_: Exception) {}
        }
        onDispose {
            if (tabletopImmersiveMode) {
                try {
                    val window = activity?.window
                    val decor = try { window?.decorView } catch (_: Exception) { null }
                    if (window != null && decor != null) {
                        val insetsController = WindowCompat.getInsetsController(window, decor)
                        insetsController.show(WindowInsetsCompat.Type.statusBars())
                    }
                } catch (_: Exception) {}
            }
        }
    }

    PredictiveBackHandler(enabled = true) { progress ->
        try {
            progress.collect { }
            onExitFlexMode()
        } catch (e: CancellationException) { }
    }
    var flexDisplayMode by remember(tabletopTopLayout) {
        mutableStateOf(
            if (displayMode == DisplayMode.ARTWORK && tabletopTopLayout == TabletopTopLayout.SPLIT_ART_LYRICS) {
                DisplayMode.SPLIT
            } else {
                displayMode
            }
        )
    }
    LaunchedEffect(displayMode, tabletopTopLayout) {
        flexDisplayMode = if (displayMode == DisplayMode.ARTWORK && tabletopTopLayout == TabletopTopLayout.SPLIT_ART_LYRICS) {
            DisplayMode.SPLIT
        } else {
            displayMode
        }
    }
    val updateFlexDisplayMode: (DisplayMode) -> Unit = { newMode ->
        flexDisplayMode = newMode
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

    var showTrackListOnBottom by remember { mutableStateOf(false) }
    var isDraggingSlider by remember(track.id) { mutableStateOf(false) }
    var sliderDragPosition by remember(track.id) { mutableFloatStateOf(0f) }
    var isEditMode by remember { mutableStateOf(false) }
    var currentPositions by remember(tabletopControlPositions) { mutableStateOf(tabletopControlPositions) }

    val activePositionMs = if (isDraggingSlider) {
        (sliderDragPosition * track.durationMs).toLong()
    } else {
        currentPositionMs
    }

    val animatedPrimary by animateColorAsState(
        targetValue = track.primaryColor,
        animationSpec = MotionSpecs.expressiveSpring(),
        label = "flex_bg_primary"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = track.secondaryColor,
        animationSpec = MotionSpecs.expressiveSpring(),
        label = "flex_bg_secondary"
    )
    val animatedAccent by animateColorAsState(
        targetValue = track.accentColor,
        animationSpec = MotionSpecs.expressiveSpring(),
        label = "flex_bg_accent"
    )

    // Comprehensive Tabletop Element Colors (only apply custom hex overrides when Individual Elements mode is active)
    val customTopBg = if (useCustomElementColors) parseHexColor(elementColors.tabletopTopHalfBackground) else null
    val ttTopBg = customTopBg ?: animatedPrimary.copy(alpha = 0.42f)
    val ttTopSecondary = customTopBg ?: animatedSecondary.copy(alpha = 0.24f)
    val ttBottomBg = (if (useCustomElementColors) parseHexColor(elementColors.tabletopBottomHalfBackground) else null) ?: Color(0xFF16182B)
    val ttReflection = (if (useCustomElementColors) parseHexColor(elementColors.tabletopReflectionTint) else null) ?: Color(0x40000000)
    val ttPlayBtn = ((if (useCustomElementColors) parseHexColor(elementColors.tabletopPlayButton) ?: parseHexColor(elementColors.playerControlPlayBtn) else null) ?: animatedPrimary).copy(alpha = 1.0f)
    val ttPlayIcon = (if (useCustomElementColors) parseHexColor(elementColors.tabletopPlayIcon) else null) ?: Color.White
    val ttSkipBtns = (if (useCustomElementColors) parseHexColor(elementColors.tabletopSkipButtons) ?: parseHexColor(elementColors.playerControlSkipBtn) else null) ?: Color.White
    val ttSecBtns = (if (useCustomElementColors) parseHexColor(elementColors.tabletopSecondaryButtons) else null) ?: Color(0x88FFFFFF)
    val ttActiveTint = (if (useCustomElementColors) parseHexColor(elementColors.tabletopActiveButtonTint) else null) ?: animatedAccent
    val ttTitle = (if (useCustomElementColors) parseHexColor(elementColors.tabletopTrackTitle) else null) ?: Color.White
    val ttArtist = (if (useCustomElementColors) parseHexColor(elementColors.tabletopTrackArtist) else null) ?: Color(0x99FFFFFF)
    val ttLyrics = (if (useCustomElementColors) parseHexColor(elementColors.tabletopLyricsText) else null) ?: Color(0x99FFFFFF)
    val ttLyricsHighlight = (if (useCustomElementColors) parseHexColor(elementColors.tabletopLyricsHighlight) else null) ?: animatedAccent
    val ttScrubberActive = (if (useCustomElementColors) parseHexColor(elementColors.tabletopScrubberActive) else null) ?: animatedAccent
    val ttScrubberInactive = (if (useCustomElementColors) parseHexColor(elementColors.tabletopScrubberInactive) else null) ?: Color(0x35FFFFFF)
    val ttVolume = (if (useCustomElementColors) parseHexColor(elementColors.tabletopVolumeTrack) ?: parseHexColor(elementColors.playerVolumeTrack) else null) ?: animatedAccent
    val ttVis = (if (useCustomElementColors) parseHexColor(elementColors.tabletopVisualizerColor) else null) ?: animatedAccent

    val lyricsController = rememberLyricsStudioController(
        track = track,
        currentPositionMs = activePositionMs,
        accentColor = ttLyricsHighlight,
        onImportLyricsUri = onImportLyricsUri,
        onSavePastedLyrics = onSavePastedLyrics,
        loadEditableLyrics = loadEditableLyrics,
        onClearLyrics = onClearLyrics
    )

    val insetsToUse = if (tabletopImmersiveMode) WindowInsets(0, 0, 0, 0) else WindowInsets.safeDrawing

    val animatedArtCacheVersion by AnimatedArtRepository.cacheVersion.collectAsStateWithLifecycle()

    if (showAnimatedCoverDialog) {
        val hasCustomAnimated = remember(track.id, track.artist, track.album, track.title, animatedArtCacheVersion) {
            AnimatedArtRepository.getCachedOrSidecarImmediate(context, track) != AnimatedCoverMedia.None
        }
        AnimatedCoverOptionsDialog(
            track = track,
            hasCustomCover = hasCustomAnimated,
            accentColor = animatedAccent,
            onDismiss = { showAnimatedCoverDialog = false },
            onPickAnimatedFile = {
                animatedCoverPickerLauncher.launch(arrayOf("video/mp4", "video/webm", "image/gif", "image/webp"))
            },
            onRemoveCustomCover = onRemoveCustomAnimatedCover
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D17))
            .testTag("flex_mode_player")
    ) {
        // Ambient glow synced with track album art colors
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ttTopBg,
                            ttTopSecondary,
                            Color(0xFF090A13)
                        ),
                        radius = 1100f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(insetsToUse)
        ) {
            // ==========================================
            // TOP HALF: Tabletop Viewing Screen (50% height)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ttTopBg,
                                ttTopSecondary,
                                Color(0xCC0D0E1B)
                            ),
                            radius = 950f
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OneUITooltipButton(
                        name = "Collapse Player",
                        onClick = onCollapse
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .glassmorphism(shape = CircleShape, backgroundColor = Color(0x30FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Collapse",
                                tint = Color.White
                            )
                        }
                    }

                    // Mode switch pills
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ModePillButton(
                            selected = flexDisplayMode == DisplayMode.ARTWORK,
                            title = "Art",
                            accentColor = animatedAccent,
                            onClick = { updateFlexDisplayMode(DisplayMode.ARTWORK) }
                        )
                        LyricsModePillWithDropdown(
                            selected = flexDisplayMode == DisplayMode.LYRICS,
                            isLyricsVisible = flexDisplayMode == DisplayMode.LYRICS || flexDisplayMode == DisplayMode.SPLIT,
                            controller = lyricsController,
                            accentColor = animatedAccent,
                            onClick = { updateFlexDisplayMode(DisplayMode.LYRICS) },
                            onEnsureLyricsMode = {
                                if (flexDisplayMode != DisplayMode.LYRICS && flexDisplayMode != DisplayMode.SPLIT) {
                                    updateFlexDisplayMode(DisplayMode.LYRICS)
                                }
                            },
                            onRefetchOnline = onRefetchLyrics,
                            onClearLyrics = onClearLyrics
                        )
                        ModePillButton(
                            selected = flexDisplayMode == DisplayMode.SPLIT,
                            title = "Split",
                            accentColor = animatedAccent,
                            onClick = { updateFlexDisplayMode(DisplayMode.SPLIT) }
                        )
                        VisualizerModePillWithDropdown(
                            selected = flexDisplayMode == DisplayMode.VISUALIZER,
                            currentStyle = visualizerStyle,
                            accentColor = animatedAccent,
                            onClick = { updateFlexDisplayMode(DisplayMode.VISUALIZER) },
                            onStyleSelected = { newStyle ->
                                updateFlexDisplayMode(DisplayMode.VISUALIZER)
                                onVisualizerStyleChange(newStyle)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Upper Stage: Artwork / Lyrics / Split / Visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = flexDisplayMode,
                        transitionSpec = {
                            (fadeIn(animationSpec = MotionSpecs.snappySpring()) + scaleIn(
                                initialScale = 0.96f,
                                animationSpec = MotionSpecs.snappySpring()
                            )) togetherWith (fadeOut(animationSpec = MotionSpecs.snappySpring()) + scaleOut(
                                targetScale = 0.96f,
                                animationSpec = MotionSpecs.snappySpring()
                            ))
                        },
                        label = "flex_view_content"
                    ) { mode ->
                        when (mode) {
                            DisplayMode.ARTWORK -> {
                                var dragDistance by remember { mutableFloatStateOf(0f) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(Unit) {
                                            detectHorizontalDragGestures(
                                                onDragStart = { dragDistance = 0f },
                                                onDragEnd = {
                                                    if (dragDistance < -60f) {
                                                        onNext()
                                                    } else if (dragDistance > 60f) {
                                                        onPrevious()
                                                    }
                                                    dragDistance = 0f
                                                },
                                                onDragCancel = { dragDistance = 0f },
                                                onHorizontalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragDistance += dragAmount
                                                }
                                            )
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CoverArtView(
                                        track = track,
                                        size = tabletopAlbumArtSize.sizeDp.dp,
                                        cornerRadius = (tabletopAlbumArtSize.sizeDp * 0.13f).coerceIn(16f, 36f).dp,
                                        isPlaying = isPlaying,
                                        showVinylPeek = true,
                                        animatePlaybackScale = true,
                                        enableAnimatedCover = enableAnimatedCovers,
                                        autoFetchWifiOnly = autoFetchAnimatedCoversWifiOnly,
                                        simulateMotionOnStaticCovers = simulateMotionOnStaticCovers,
                                        onLongPress = { showAnimatedCoverDialog = true }
                                    )
                                }
                            }
                            DisplayMode.LYRICS -> {
                                LyricsView(
                                    lyrics = track.lyrics,
                                    currentPositionMs = activePositionMs,
                                    primaryColor = ttLyricsHighlight,
                                    inactiveTextColor = ttLyrics,
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
                            DisplayMode.SPLIT -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left side: Album Art
                                    Box(
                                        modifier = Modifier
                                            .weight(0.48f)
                                            .fillMaxHeight()
                                            .pointerInput(Unit) {
                                                detectHorizontalDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    if (dragAmount < -40f) onNext()
                                                    else if (dragAmount > 40f) onPrevious()
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CoverArtView(
                                            track = track,
                                            size = (tabletopAlbumArtSize.sizeDp * 0.82f).dp,
                                            cornerRadius = 18.dp,
                                            isPlaying = isPlaying,
                                            showVinylPeek = true,
                                            animatePlaybackScale = true,
                                            enableAnimatedCover = enableAnimatedCovers,
                                            autoFetchWifiOnly = autoFetchAnimatedCoversWifiOnly,
                                            simulateMotionOnStaticCovers = simulateMotionOnStaticCovers,
                                            onLongPress = { showAnimatedCoverDialog = true }
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Right side: Lyrics
                                    Box(
                                        modifier = Modifier
                                            .weight(0.52f)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        LyricsView(
                                            lyrics = track.lyrics,
                                            currentPositionMs = activePositionMs,
                                            primaryColor = ttLyricsHighlight,
                                            inactiveTextColor = ttLyrics,
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
                            DisplayMode.VISUALIZER -> {
                                var visualizerDragY by remember { mutableFloatStateOf(0f) }
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .pointerInput(Unit) {
                                            detectVerticalDragGestures(
                                                onDragStart = { visualizerDragY = 0f },
                                                onDragEnd = {
                                                    if (visualizerDragY > 70f) {
                                                        onCollapse()
                                                    }
                                                    visualizerDragY = 0f
                                                },
                                                onDragCancel = { visualizerDragY = 0f },
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    visualizerDragY += dragAmount
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    val visPeak = if (useCustomElementColors) parseHexColor(elementColors.visualizerPeakColor) else null
                                    val visBar = if (useCustomElementColors) parseHexColor(elementColors.tabletopVisualizerColor) ?: parseHexColor(elementColors.visualizerBarColor) else null
                                    val visBase = if (useCustomElementColors) parseHexColor(elementColors.visualizerBaseColor) else null
                                    val visGlow = if (useCustomElementColors) parseHexColor(elementColors.visualizerGlowColor) else null
                                    val fullTopHeight = maxHeight

                                    AudioVisualizer(
                                        amplitudes = visualizerAmplitudes,
                                        primaryColor = visBar ?: ttVis,
                                        secondaryColor = visBase ?: animatedAccent,
                                        peakColor = visPeak,
                                        barBodyColor = visBar ?: ttVis,
                                        baseColor = visBase,
                                        glowColor = visGlow,
                                        style = visualizerStyle,
                                        maxHeight = fullTopHeight,
                                        barWidth = 6.dp,
                                        isPlaying = isPlaying,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                // Title info in top pane
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ttTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )
                Text(
                    text = "${track.artist} • ${track.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ttArtist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ==========================================
            // HINGE / CREASE DIVIDER (One UI Fold Line)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0x00000000),
                                Color(0x661E2034),
                                Color(0x992B2F4C),
                                Color(0x661E2034),
                                Color(0x00000000)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Subtle metallic crease highlight
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(3.dp)
                        .clip(OneUIPillShape)
                        .background(Color(0x55FFFFFF))
                )
            }

            // ==========================================
            // BOTTOM HALF: Touchpad Control Console (50% height)
            // Lying flat on desk in Flex Mode
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (tabletopReflectedBackgroundEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleY = -1f
                                alpha = 0.22f
                            }
                            .blur(50.dp)
                    ) {
                        CoverArtView(
                            track = track,
                            size = 600.dp,
                            cornerRadius = 0.dp,
                            isPlaying = isPlaying,
                            showVinylPeek = false
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ttReflection)
                        )
                    }
                }

                if (tabletopControlStyle == TabletopControlStyle.NETFLIX_DUO) {
                    NetflixDuoTabletopConsole(
                        track = track,
                        isPlaying = isPlaying,
                        activePositionMs = activePositionMs,
                        trackDurationMs = track.durationMs,
                        speed = speed,
                        volume = volume,
                        isShuffleActive = isShuffleActive,
                        repeatMode = repeatMode,
                        visualizerStyle = visualizerStyle,
                        currentDisplayMode = flexDisplayMode,
                        tabletopControlsScale = tabletopControlsScale,
                        showTrackListOnBottom = showTrackListOnBottom,
                        allTracks = allTracks,
                        animatedAccent = animatedAccent,
                        buttonAnimationEffect = buttonAnimationEffect,
                        onTogglePlayPause = onTogglePlayPause,
                        onPrevious = onPrevious,
                        onNext = onNext,
                        onSeek = onSeek,
                        onSpeedChange = onSpeedChange,
                        onVolumeChange = onVolumeChange,
                        onToggleShuffle = onToggleShuffle,
                        onCycleRepeat = onCycleRepeat,
                        onVisualizerStyleChange = onVisualizerStyleChange,
                        onToggleTrackList = { showTrackListOnBottom = !showTrackListOnBottom },
                        onTrackClick = onTrackClick,
                        onPlayFromContext = onPlayFromContext,
                        onExitFlexMode = onExitFlexMode,
                        onOpenEqualizer = onOpenEqualizer,
                        onOpenDisplayMode = {
                            updateFlexDisplayMode(
                                when (flexDisplayMode) {
                                    DisplayMode.ARTWORK -> DisplayMode.LYRICS
                                    DisplayMode.LYRICS -> DisplayMode.SPLIT
                                    DisplayMode.SPLIT -> DisplayMode.VISUALIZER
                                    DisplayMode.VISUALIZER -> DisplayMode.ARTWORK
                                }
                            )
                        }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .glassmorphism(
                                shape = OneUISquircleLarge,
                                backgroundColor = ttBottomBg,
                                borderWidth = 1.2.dp,
                                elevation = 0.dp
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    // Console Header: Dolby & Queue Toggle & Sleep Timer & Favorite
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OneUITooltipButton(
                            name = if (dolbyAtmosEnabled) "Dolby Atmos 3D (Active)" else "Stereo Audio (Tap for Equalizer)",
                            onClick = onOpenEqualizer,
                            shape = OneUIPillShape,
                            consumeClicks = false
                        ) {
                            Box(
                                modifier = Modifier
                                    .glassmorphism(
                                        shape = OneUIPillShape,
                                        backgroundColor = if (dolbyAtmosEnabled) Color(0x357C4DFF) else Color(0x20FFFFFF),
                                        borderWidth = 0.8.dp
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SurroundSound,
                                        contentDescription = "Dolby Atmos",
                                        tint = if (dolbyAtmosEnabled) animatedAccent else Color(0x88FFFFFF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (dolbyAtmosEnabled) "Dolby Atmos 3D" else "Stereo",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Tabletop Track Queue Toggle
                            OneUITooltipButton(
                                name = if (showTrackListOnBottom) "Show Tabletop Controls" else "Show Track Queue",
                                onClick = { showTrackListOnBottom = !showTrackListOnBottom },
                                shape = CircleShape,
                                consumeClicks = false
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .glassmorphism(
                                            shape = CircleShape,
                                            backgroundColor = if (showTrackListOnBottom) animatedAccent else Color(0x20FFFFFF)
                                        )
                                        .clickable { showTrackListOnBottom = !showTrackListOnBottom }
                                        .testTag("tabletop_toggle_track_list"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (showTrackListOnBottom) Icons.Default.Tune else Icons.Default.QueueMusic,
                                        contentDescription = "Track Queue",
                                        tint = if (showTrackListOnBottom) Color(0xFF0F101E) else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            OneUITooltipButton(
                                name = "Playback Speed: ${speed}x",
                                onClick = {
                                    val nextSpeed = when (speed) {
                                        0.8f -> 1.0f
                                        1.0f -> 1.25f
                                        1.5f -> 2.0f
                                        else -> 0.8f
                                    }
                                    onSpeedChange(nextSpeed)
                                },
                                shape = CircleShape,
                                consumeClicks = false
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .glassmorphism(shape = OneUIPillShape, backgroundColor = Color(0x20FFFFFF))
                                        .clickable {
                                            val nextSpeed = when (speed) {
                                                0.8f -> 1.0f
                                                1.0f -> 1.25f
                                                1.5f -> 2.0f
                                                else -> 0.8f
                                            }
                                            onSpeedChange(nextSpeed)
                                        }
                                        .padding(horizontal = 10.dp)
                                        .testTag("tabletop_speed_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${speed}x",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            OneUITooltipButton(
                                name = if (sleepTimerMinutesLeft != null) "Sleep Timer: ${sleepTimerMinutesLeft}m remaining" else "Sleep Timer: Off",
                                onClick = onOpenSleepTimer,
                                shape = CircleShape,
                                consumeClicks = false
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .glassmorphism(shape = CircleShape, backgroundColor = Color(0x20FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Sleep Timer",
                                        tint = if (sleepTimerMinutesLeft != null) animatedAccent else Color(0x99FFFFFF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            OneUITooltipButton(
                                name = if (track.isFavorite) "Favorited Track" else "Add to Favorites",
                                onClick = onToggleFavorite,
                                shape = CircleShape,
                                consumeClicks = false
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .glassmorphism(shape = CircleShape, backgroundColor = Color(0x20FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedContent(
                                        targetState = track.isFavorite,
                                        transitionSpec = { MotionSpecs.iconMorphTransition() },
                                        label = "flex_fav_morph"
                                    ) { isFav ->
                                        Icon(
                                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (isFav) Color(0xFFFF4081) else Color(0x99FFFFFF),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (showTrackListOnBottom) {
                        val currentTrackIndex = remember(allTracks, track.id) {
                            val idx = allTracks.indexOfFirst { it.id == track.id }
                            if (idx >= 0) idx else 0
                        }
                        val queueListState = rememberSaveable(saver = LazyListState.Saver) {
                            LazyListState(firstVisibleItemIndex = currentTrackIndex)
                        }
                        val coroutineScope = rememberCoroutineScope()

                        androidx.compose.runtime.LaunchedEffect(showTrackListOnBottom, track.id) {
                            if (showTrackListOnBottom && currentTrackIndex >= 0 && allTracks.isNotEmpty()) {
                                queueListState.scrollToItem(currentTrackIndex)
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Track Queue (${allTracks.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Current: #${currentTrackIndex + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = animatedAccent
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                LazyColumn(
                                    state = queueListState,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    itemsIndexed(allTracks, key = { _, it -> it.id }) { index, itemTrack ->
                                        val isSelected = itemTrack.id == track.id
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .animateItem(
                                                    fadeInSpec = MotionSpecs.snappySpring(),
                                                    placementSpec = MotionSpecs.LazyItemPlacementSpring,
                                                    fadeOutSpec = MotionSpecs.snappySpring()
                                                )
                                                .clip(OneUISquircleSmall)
                                                .background(if (isSelected) animatedAccent.copy(alpha = 0.25f) else Color(0x18FFFFFF))
                                                .bouncableClick(pressedScale = 0.96f) { onPlayFromContext(allTracks, index) }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Metadata album art display
                                            Box(
                                                modifier = Modifier.size(40.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CoverArtView(
                                                    track = itemTrack,
                                                    size = 40.dp,
                                                    cornerRadius = 8.dp
                                                )
                                                if (isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(OneUISquircleSmall)
                                                            .background(Color(0x77000000)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isPlaying) Icons.Default.Equalizer else Icons.Default.PlayArrow,
                                                            contentDescription = null,
                                                            tint = animatedAccent,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    itemTrack.title,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = if (isSelected) animatedAccent else Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = if (isSelected) {
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .basicMarquee(iterations = Int.MAX_VALUE)
                                                    } else {
                                                        Modifier
                                                    }
                                                )
                                                Text(
                                                    itemTrack.artist,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0x99FFFFFF),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                formatTime(itemTrack.durationMs),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0x77FFFFFF)
                                            )
                                        }
                                    }
                                }

                                // Quick scroll bar for queue
                                if (allTracks.size > 5) {
                                    BoxWithConstraints(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .fillMaxHeight()
                                            .padding(horizontal = 2.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        val totalH = maxHeight
                                        val thumbH = 32.dp
                                        val currentIdx = queueListState.firstVisibleItemIndex
                                        val fraction = (currentIdx.toFloat() / (allTracks.size - 1).coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                                        val thumbY = (totalH - thumbH) * fraction
                                        var isDraggingQueueThumb by remember { mutableStateOf(false) }
                                        var draggedQueueIdx by remember { mutableStateOf(currentIdx.coerceIn(0, (allTracks.size - 1).coerceAtLeast(0))) }

                                        // Track rail
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .fillMaxHeight()
                                                .clip(CircleShape)
                                                .background(Color(0x28FFFFFF))
                                        )

                                        // Thumb
                                        Box(
                                            modifier = Modifier
                                                .offset(y = thumbY)
                                                .size(width = 20.dp, height = thumbH)
                                                .clip(CircleShape)
                                                .glassmorphism(
                                                    shape = CircleShape,
                                                    backgroundColor = animatedAccent.copy(alpha = 0.85f),
                                                    borderWidth = 1.dp,
                                                    elevation = 0.dp
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DragHandle,
                                                contentDescription = "Quick Scroll",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }

                                        // Full touch surface overlay for responsive drag and jump
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .pointerInput(allTracks.size) {
                                                    detectTapGestures { offset ->
                                                        val frac = (offset.y / totalH.toPx()).coerceIn(0f, 1f)
                                                        val target = (frac * (allTracks.size - 1)).roundToInt().coerceIn(0, allTracks.size - 1)
                                                        draggedQueueIdx = target
                                                        coroutineScope.launch {
                                                            queueListState.scrollToItem(target)
                                                        }
                                                    }
                                                }
                                                .pointerInput(allTracks.size) {
                                                    detectVerticalDragGestures(
                                                        onDragStart = { offset ->
                                                            isDraggingQueueThumb = true
                                                            val frac = (offset.y / totalH.toPx()).coerceIn(0f, 1f)
                                                            val target = (frac * (allTracks.size - 1)).roundToInt().coerceIn(0, allTracks.size - 1)
                                                            draggedQueueIdx = target
                                                            coroutineScope.launch {
                                                                queueListState.scrollToItem(target)
                                                            }
                                                        },
                                                        onDragEnd = { isDraggingQueueThumb = false },
                                                        onDragCancel = { isDraggingQueueThumb = false },
                                                        onVerticalDrag = { change, _ ->
                                                            change.consume()
                                                            val frac = (change.position.y / totalH.toPx()).coerceIn(0f, 1f)
                                                            val target = (frac * (allTracks.size - 1)).roundToInt().coerceIn(0, allTracks.size - 1)
                                                            draggedQueueIdx = target
                                                            coroutineScope.launch {
                                                                queueListState.scrollToItem(target)
                                                            }
                                                        }
                                                    )
                                                }
                                        )

                                        if (isDraggingQueueThumb && draggedQueueIdx in allTracks.indices) {
                                            val hoveredChar = allTracks[draggedQueueIdx].title.firstOrNull()?.let {
                                                if (it.isLetter()) it.uppercaseChar() else '#'
                                            } ?: '#'
                                            val density = LocalDensity.current
                                            val yPx = with(density) { thumbY.roundToPx() }
                                            val xGap = with(density) { 40.dp.roundToPx() }
                                            androidx.compose.ui.window.Popup(
                                                alignment = Alignment.TopEnd,
                                                offset = IntOffset(x = -xGap, y = yPx)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .shadow(12.dp, CircleShape)
                                                        .clip(CircleShape)
                                                        .background(animatedAccent)
                                                        .border(2.dp, Color.White, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = hoveredChar.toString(),
                                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                                        color = Color(0xFF0C0E1A)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Scrubber Bar using ExpressiveScrubber moved down for optimal visual balance
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp, bottom = 4.dp)
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
                                accentColor = ttScrubberActive,
                                inactiveColor = ttScrubberInactive,
                                isPlaying = isPlaying,
                                modifier = Modifier
                                    .fillMaxWidth()
                            .testTag("flex_scrubber_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(activePositionMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0x99FFFFFF)
                        )
                        Text(
                            text = formatTime(track.durationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0x99FFFFFF)
                        )
                    }
                }

                // Tabletop Infinite Canvas Grid & Movable Playback Controls
                val canvasState = rememberInfiniteCanvasState(
                    initialPan = Offset(currentPositions.panOffsetX, currentPositions.panOffsetY),
                    initialZoom = if (currentPositions.customScale in 0.5f..5.0f) currentPositions.customScale else 1.0f,
                    initialGridSnap = currentPositions.isGridSnapEnabled,
                    gridSizeDp = 28.dp
                )

                val activeScale = canvasState.zoomScale

                val playInteractionSource = remember { MutableInteractionSource() }
                val isPlayPressed by playInteractionSource.collectIsPressedAsState()
                val prevInteractionSource = remember { MutableInteractionSource() }
                val isPrevPressed by prevInteractionSource.collectIsPressedAsState()
                val nextInteractionSource = remember { MutableInteractionSource() }
                val isNextPressed by nextInteractionSource.collectIsPressedAsState()
                val shuffleInteractionSource = remember { MutableInteractionSource() }
                val isShufflePressed by shuffleInteractionSource.collectIsPressedAsState()
                val repeatInteractionSource = remember { MutableInteractionSource() }
                val isRepeatPressed by repeatInteractionSource.collectIsPressedAsState()

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

                val (playTargetScaleX, playTargetScaleY, playTargetRotation) = when (buttonAnimationEffect) {
                    ButtonAnimationEffect.BOUNCY_SPRING -> {
                        val s = if (isPlayPressed) 0.88f else if (isPlaying) 1.04f else 1.0f
                        Triple(s, s, 0f)
                    }
                    ButtonAnimationEffect.ROTATION_TILT -> {
                        val s = if (isPlayPressed) 0.88f else if (isPlaying) 1.04f else 1.0f
                        val r = if (isPlayPressed) 6f else 0f
                        Triple(s, s, r)
                    }
                    ButtonAnimationEffect.NEON_GLOW_POP -> {
                        val s = if (isPlayPressed) 1.22f else if (isPlaying) 1.06f else 1.0f
                        Triple(s, s, 0f)
                    }
                    ButtonAnimationEffect.JELLY_SQUISH -> {
                        if (isPlayPressed) Triple(1.22f, 0.82f, 0f) else if (isPlaying) Triple(1.04f, 1.04f, 0f) else Triple(1.0f, 1.0f, 0f)
                    }
                    ButtonAnimationEffect.SUBTLE_HAPTIC -> {
                        val s = if (isPlayPressed) 0.96f else 1.0f
                        Triple(s, s, 0f)
                    }
                }

                val (shuffleTargetScaleX, shuffleTargetScaleY) = when (buttonAnimationEffect) {
                    ButtonAnimationEffect.BOUNCY_SPRING -> (if (isShufflePressed) 0.85f else 1.0f) to (if (isShufflePressed) 0.85f else 1.0f)
                    ButtonAnimationEffect.ROTATION_TILT -> (if (isShufflePressed) 0.88f else 1.0f) to (if (isShufflePressed) 0.88f else 1.0f)
                    ButtonAnimationEffect.NEON_GLOW_POP -> (if (isShufflePressed) 1.20f else 1.0f) to (if (isShufflePressed) 1.20f else 1.0f)
                    ButtonAnimationEffect.JELLY_SQUISH -> (if (isShufflePressed) 1.22f else 1.0f) to (if (isShufflePressed) 0.80f else 1.0f)
                    ButtonAnimationEffect.SUBTLE_HAPTIC -> (if (isShufflePressed) 0.95f else 1.0f) to (if (isShufflePressed) 0.95f else 1.0f)
                }

                val (repeatTargetScaleX, repeatTargetScaleY) = when (buttonAnimationEffect) {
                    ButtonAnimationEffect.BOUNCY_SPRING -> (if (isRepeatPressed) 0.85f else 1.0f) to (if (isRepeatPressed) 0.85f else 1.0f)
                    ButtonAnimationEffect.ROTATION_TILT -> (if (isRepeatPressed) 0.88f else 1.0f) to (if (isRepeatPressed) 0.88f else 1.0f)
                    ButtonAnimationEffect.NEON_GLOW_POP -> (if (isRepeatPressed) 1.20f else 1.0f) to (if (isRepeatPressed) 1.20f else 1.0f)
                    ButtonAnimationEffect.JELLY_SQUISH -> (if (isRepeatPressed) 1.22f else 1.0f) to (if (isRepeatPressed) 0.80f else 1.0f)
                    ButtonAnimationEffect.SUBTLE_HAPTIC -> (if (isRepeatPressed) 0.95f else 1.0f) to (if (isRepeatPressed) 0.95f else 1.0f)
                }

                val springSpec = when (buttonAnimationEffect) {
                    ButtonAnimationEffect.BOUNCY_SPRING -> MotionSpecs.TactilePressSpring
                    ButtonAnimationEffect.ROTATION_TILT -> MotionSpecs.ExpressiveSpring
                    ButtonAnimationEffect.NEON_GLOW_POP -> spring<Float>(dampingRatio = 0.58f, stiffness = Spring.StiffnessMedium)
                    ButtonAnimationEffect.JELLY_SQUISH -> spring<Float>(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)
                    ButtonAnimationEffect.SUBTLE_HAPTIC -> MotionSpecs.SnappySpring
                }

                val prevScaleX by animateFloatAsState(targetValue = prevTargetScaleX, animationSpec = springSpec, label = "tt_prev_sx")
                val prevScaleY by animateFloatAsState(targetValue = prevTargetScaleY, animationSpec = springSpec, label = "tt_prev_sy")
                val prevRotation by animateFloatAsState(targetValue = prevTargetRotation, animationSpec = springSpec, label = "tt_prev_rot")

                val nextScaleX by animateFloatAsState(targetValue = nextTargetScaleX, animationSpec = springSpec, label = "tt_next_sx")
                val nextScaleY by animateFloatAsState(targetValue = nextTargetScaleY, animationSpec = springSpec, label = "tt_next_sy")
                val nextRotation by animateFloatAsState(targetValue = nextTargetRotation, animationSpec = springSpec, label = "tt_next_rot")

                val playScaleX by animateFloatAsState(targetValue = playTargetScaleX, animationSpec = springSpec, label = "tt_play_sx")
                val playScaleY by animateFloatAsState(targetValue = playTargetScaleY, animationSpec = springSpec, label = "tt_play_sy")
                val playRotation by animateFloatAsState(targetValue = playTargetRotation, animationSpec = springSpec, label = "tt_play_rot")

                val shuffleScaleX by animateFloatAsState(targetValue = shuffleTargetScaleX, animationSpec = springSpec, label = "tt_shuf_sx")
                val shuffleScaleY by animateFloatAsState(targetValue = shuffleTargetScaleY, animationSpec = springSpec, label = "tt_shuf_sy")

                val repeatScaleX by animateFloatAsState(targetValue = repeatTargetScaleX, animationSpec = springSpec, label = "tt_rep_sx")
                val repeatScaleY by animateFloatAsState(targetValue = repeatTargetScaleY, animationSpec = springSpec, label = "tt_rep_sy")

                val density = LocalDensity.current
                // Tabletop button scale combines size tier and the Tabletop Controls Scale setting (up to 500%)
                val baseButtonTier = when (tabletopButtonSize) {
                    TabletopButtonSize.SMALL -> 0.82f
                    TabletopButtonSize.MEDIUM -> 1.00f
                    TabletopButtonSize.LARGE -> 1.15f
                    TabletopButtonSize.LARGER -> 1.28f
                }
                val effectiveControlsScale = tabletopControlsScale.coerceIn(0.70f, 5.00f)
                val buttonScale = baseButtonTier * effectiveControlsScale

                // Synchronize the canvas zoom scale whenever Tabletop Controls Scale changes from settings
                androidx.compose.runtime.LaunchedEffect(effectiveControlsScale) {
                    if (!canvasState.isEditMode) {
                        canvasState.zoomScale = effectiveControlsScale
                    }
                }

                // Detect legacy corrupted raw-pixel positions (which were packed into < 80px causing heavy overlaps)
                val isLegacyCorrupted = currentPositions.isCustomLayoutEnabled && (
                    (currentPositions.prevX != 0f && kotlin.math.abs(currentPositions.prevX) < with(density) { 75.dp.toPx() }) ||
                    (currentPositions.rewind10X != 0f && kotlin.math.abs(currentPositions.rewind10X) < with(density) { 50.dp.toPx() }) ||
                    (currentPositions.shuffleX != 0f && kotlin.math.abs(currentPositions.shuffleX) < with(density) { 90.dp.toPx() })
                )

                androidx.compose.runtime.LaunchedEffect(isLegacyCorrupted) {
                    if (isLegacyCorrupted) {
                        currentPositions = TabletopControlPositions()
                        onUpdateControlPositions(TabletopControlPositions())
                    }
                }

                val isCustom = currentPositions.isCustomLayoutEnabled && !isLegacyCorrupted

                val handleButtonLongPress: (Offset) -> Unit = { _ ->
                    canvasState.isEditMode = true
                    val current = canvasState.zoomScale
                    val nextScale = when {
                        current < 1.0f -> 1.0f
                        current < 1.9f -> 2.0f
                        current < 2.9f -> 3.0f
                        current < 3.9f -> 4.0f
                        current < 4.9f -> 5.0f
                        else -> 1.0f
                    }
                    canvasState.zoomScale = nextScale
                }

                // Map movable widget items with calibrated, non-overlapping spacing
                val canvasItems = remember(
                    tabletopControlStyle,
                    tabletopButtonSize,
                    effectiveControlsScale,
                    buttonScale,
                    currentPositions,
                    isPlaying,
                    isShuffleActive,
                    repeatMode,
                    speed,
                    volume,
                    playScaleX, playScaleY, playRotation,
                    prevScaleX, prevScaleY, prevRotation,
                    nextScaleX, nextScaleY, nextRotation,
                    shuffleScaleX, shuffleScaleY,
                    repeatScaleX, repeatScaleY,
                    showTrackListOnBottom,
                    ttPlayBtn, ttPlayIcon, ttSkipBtns, ttSecBtns, ttActiveTint, ttVolume,
                    density
                ) {
                    val list = mutableListOf<MovableWidgetItem>()

                    // ONE UI TACTILE STYLE (Ergonomic Horizontal Arc)
                    // Calibrated dimensions & physical clearance
                    val uiPlayWidth = 78.dp * buttonScale
                        val uiPlayHeight = 50.dp * buttonScale
                        val uiSkipSize = 42.dp * buttonScale
                        val uiToolSize = 38.dp * buttonScale

                        // 1. One UI Tactile Play/Pause Pill
                        list.add(
                            MovableWidgetItem(
                                id = "play_pause",
                                name = "Play/Pause",
                                worldPosition = Offset(
                                    if (isCustom) currentPositions.playX else 0f,
                                    if (isCustom) currentPositions.playY else 0f
                                )
                            ) { isEdit, isDragged ->
                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = playScaleX
                                            scaleY = playScaleY
                                            rotationZ = playRotation
                                        }
                                        .size(width = uiPlayWidth, height = uiPlayHeight)
                                        .glassmorphism(
                                            shape = OneUIPillShape,
                                            backgroundColor = ttPlayBtn,
                                            borderWidth = 1.5.dp,
                                            elevation = if (isDragged) 10.dp else 0.dp
                                        )
                                        .pointerInput(isEdit) {
                                            if (!isEdit) {
                                                detectTapGestures(
                                                    onPress = { offset ->
                                                        val press = PressInteraction.Press(offset)
                                                        try {
                                                            playInteractionSource.emit(press)
                                                            if (tryAwaitRelease()) {
                                                                playInteractionSource.emit(PressInteraction.Release(press))
                                                            } else {
                                                                playInteractionSource.emit(PressInteraction.Cancel(press))
                                                            }
                                                        } catch (_: Exception) {
                                                            try { playInteractionSource.emit(PressInteraction.Cancel(press)) } catch (_: Exception) {}
                                                        }
                                                    },
                                                    onLongPress = handleButtonLongPress,
                                                    onTap = { onTogglePlayPause() }
                                                )
                                            }
                                        }
                                        .testTag("flex_play_pause_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedContent(
                                        targetState = isPlaying,
                                        transitionSpec = { MotionSpecs.iconMorphTransition() },
                                        label = "flex_play_icon_morph"
                                    ) { playing ->
                                        Icon(
                                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (playing) "Pause" else "Play",
                                            tint = ttPlayIcon,
                                            modifier = Modifier.size(28.dp * buttonScale)
                                        )
                                    }
                                }
                            }
                        )

                        // 2. Previous Track (Positioned to the Left with guaranteed clearance)
                        val defaultPrevX = with(density) { -(72.dp * buttonScale).toPx() }
                        list.add(
                            MovableWidgetItem(
                                id = "skip_prev",
                                name = "Previous Track",
                                worldPosition = Offset(
                                    if (isCustom && currentPositions.prevX != 0f) currentPositions.prevX else defaultPrevX,
                                    if (isCustom && currentPositions.prevY != 0f) currentPositions.prevY else 0f
                                )
                            ) { isEdit, isDragged ->
                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = prevScaleX
                                            scaleY = prevScaleY
                                            rotationZ = prevRotation
                                        }
                                        .size(uiSkipSize)
                                        .glassmorphism(
                                            shape = CircleShape,
                                            backgroundColor = Color(0x2BFFFFFF),
                                            elevation = if (isDragged) 8.dp else 0.dp
                                        )
                                        .pointerInput(isEdit) {
                                            if (!isEdit) {
                                                detectTapGestures(
                                                    onPress = { offset ->
                                                        val press = PressInteraction.Press(offset)
                                                        try {
                                                            prevInteractionSource.emit(press)
                                                            if (tryAwaitRelease()) {
                                                                prevInteractionSource.emit(PressInteraction.Release(press))
                                                            } else {
                                                                prevInteractionSource.emit(PressInteraction.Cancel(press))
                                                            }
                                                        } catch (_: Exception) {
                                                            try { prevInteractionSource.emit(PressInteraction.Cancel(press)) } catch (_: Exception) {}
                                                        }
                                                    },
                                                    onLongPress = handleButtonLongPress,
                                                    onTap = { onPrevious() }
                                                )
                                            }
                                        }
                                        .testTag("flex_prev_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.SkipPrevious,
                                        "Previous Track",
                                        tint = ttSkipBtns,
                                        modifier = Modifier.size(22.dp * buttonScale)
                                    )
                                }
                            }
                        )

                        // 3. Next Track (Positioned to the Right with guaranteed clearance)
                        val defaultNextX = with(density) { +(72.dp * buttonScale).toPx() }
                        list.add(
                            MovableWidgetItem(
                                id = "skip_next",
                                name = "Next Track",
                                worldPosition = Offset(
                                    if (isCustom && currentPositions.nextX != 0f) currentPositions.nextX else defaultNextX,
                                    if (isCustom && currentPositions.nextY != 0f) currentPositions.nextY else 0f
                                )
                            ) { isEdit, isDragged ->
                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = nextScaleX
                                            scaleY = nextScaleY
                                            rotationZ = nextRotation
                                        }
                                        .size(uiSkipSize)
                                        .glassmorphism(
                                            shape = CircleShape,
                                            backgroundColor = Color(0x2BFFFFFF),
                                            elevation = if (isDragged) 8.dp else 0.dp
                                        )
                                        .pointerInput(isEdit) {
                                            if (!isEdit) {
                                                detectTapGestures(
                                                    onPress = { offset ->
                                                        val press = PressInteraction.Press(offset)
                                                        try {
                                                            nextInteractionSource.emit(press)
                                                            if (tryAwaitRelease()) {
                                                                nextInteractionSource.emit(PressInteraction.Release(press))
                                                            } else {
                                                                nextInteractionSource.emit(PressInteraction.Cancel(press))
                                                            }
                                                        } catch (_: Exception) {
                                                            try { nextInteractionSource.emit(PressInteraction.Cancel(press)) } catch (_: Exception) {}
                                                        }
                                                    },
                                                    onLongPress = handleButtonLongPress,
                                                    onTap = { onNext() }
                                                )
                                            }
                                        }
                                        .testTag("flex_next_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.SkipNext,
                                        "Next Track",
                                        tint = ttSkipBtns,
                                        modifier = Modifier.size(22.dp * buttonScale)
                                    )
                                }
                            }
                        )

                        // 4. Shuffle (Positioned beyond Previous Track)
                        val defaultShuffleX = with(density) { -(130.dp * buttonScale).toPx() }
                        list.add(
                            MovableWidgetItem(
                                id = "shuffle",
                                name = "Shuffle",
                                worldPosition = Offset(
                                    if (isCustom && currentPositions.shuffleX != 0f) currentPositions.shuffleX else defaultShuffleX,
                                    if (isCustom && currentPositions.shuffleY != 0f) currentPositions.shuffleY else 0f
                                )
                            ) { isEdit, isDragged ->
                                Box(
                                    modifier = Modifier
                                        .graphicsLayer { scaleX = shuffleScaleX; scaleY = shuffleScaleY }
                                        .size(uiToolSize)
                                        .glassmorphism(
                                            shape = CircleShape,
                                            backgroundColor = if (isShuffleActive) ttActiveTint.copy(alpha = 0.28f) else Color(0x20FFFFFF),
                                            elevation = if (isDragged) 6.dp else 0.dp
                                        )
                                        .pointerInput(isEdit) {
                                            if (!isEdit) {
                                                detectTapGestures(
                                                    onPress = { offset ->
                                                        val press = PressInteraction.Press(offset)
                                                        try {
                                                            shuffleInteractionSource.emit(press)
                                                            if (tryAwaitRelease()) {
                                                                shuffleInteractionSource.emit(PressInteraction.Release(press))
                                                            } else {
                                                                shuffleInteractionSource.emit(PressInteraction.Cancel(press))
                                                            }
                                                        } catch (_: Exception) {
                                                            try { shuffleInteractionSource.emit(PressInteraction.Cancel(press)) } catch (_: Exception) {}
                                                        }
                                                    },
                                                    onLongPress = handleButtonLongPress,
                                                    onTap = { onToggleShuffle() }
                                                )
                                            }
                                        }
                                        .testTag("flex_shuffle_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedContent(
                                        targetState = isShuffleActive,
                                        transitionSpec = { MotionSpecs.iconMorphTransition() },
                                        label = "flex_shuffle_icon_morph"
                                    ) { active ->
                                        Icon(
                                            Icons.Default.Shuffle,
                                            "Shuffle",
                                            tint = if (active) ttActiveTint else ttSecBtns,
                                            modifier = Modifier.size(18.dp * buttonScale)
                                        )
                                    }
                                }
                            }
                        )

                        // 5. Repeat (Positioned beyond Next Track)
                        val defaultRepeatX = with(density) { +(130.dp * buttonScale).toPx() }
                        list.add(
                            MovableWidgetItem(
                                id = "repeat",
                                name = "Repeat",
                                worldPosition = Offset(
                                    if (isCustom && currentPositions.repeatX != 0f) currentPositions.repeatX else defaultRepeatX,
                                    if (isCustom && currentPositions.repeatY != 0f) currentPositions.repeatY else 0f
                                )
                            ) { isEdit, isDragged ->
                                Box(
                                    modifier = Modifier
                                        .graphicsLayer { scaleX = repeatScaleX; scaleY = repeatScaleY }
                                        .size(uiToolSize)
                                        .glassmorphism(
                                            shape = CircleShape,
                                            backgroundColor = if (repeatMode != RepeatMode.OFF) ttActiveTint.copy(alpha = 0.28f) else Color(0x20FFFFFF),
                                            elevation = if (isDragged) 6.dp else 0.dp
                                        )
                                        .pointerInput(isEdit) {
                                            if (!isEdit) {
                                                detectTapGestures(
                                                    onPress = { offset ->
                                                        val press = PressInteraction.Press(offset)
                                                        try {
                                                            repeatInteractionSource.emit(press)
                                                            if (tryAwaitRelease()) {
                                                                repeatInteractionSource.emit(PressInteraction.Release(press))
                                                            } else {
                                                                repeatInteractionSource.emit(PressInteraction.Cancel(press))
                                                            }
                                                        } catch (_: Exception) {
                                                            try { repeatInteractionSource.emit(PressInteraction.Cancel(press)) } catch (_: Exception) {}
                                                        }
                                                    },
                                                    onLongPress = handleButtonLongPress,
                                                    onTap = { onCycleRepeat() }
                                                )
                                            }
                                        }
                                        .testTag("flex_repeat_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedContent(
                                        targetState = repeatMode,
                                        transitionSpec = { MotionSpecs.iconMorphTransition() },
                                        label = "flex_repeat_icon_morph"
                                    ) { mode ->
                                        Icon(
                                            imageVector = if (mode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                            contentDescription = "Repeat",
                                            tint = if (mode != RepeatMode.OFF) ttActiveTint else ttSecBtns,
                                            modifier = Modifier.size(18.dp * buttonScale)
                                        )
                                    }
                                }
                            }
                        )

                        list
                    }

                // Infinite Canvas Container strictly bounded inside the bottom half
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clipToBounds()
                ) {
                    TabletopInfiniteCanvas(
                        state = canvasState,
                        items = canvasItems,
                        onItemMoved = { id, newPos ->
                            currentPositions = when (id) {
                                "play_pause" -> currentPositions.copy(playX = newPos.x, playY = newPos.y)
                                "skip_prev" -> currentPositions.copy(prevX = newPos.x, prevY = newPos.y)
                                "skip_next" -> currentPositions.copy(nextX = newPos.x, nextY = newPos.y)
                                "shuffle" -> currentPositions.copy(shuffleX = newPos.x, shuffleY = newPos.y)
                                "repeat" -> currentPositions.copy(repeatX = newPos.x, repeatY = newPos.y)
                                "speed" -> currentPositions.copy(speedX = newPos.x, speedY = newPos.y)
                                "rewind10" -> currentPositions.copy(rewind10X = newPos.x, rewind10Y = newPos.y)
                                "forward10" -> currentPositions.copy(forward10X = newPos.x, forward10Y = newPos.y)
                                "queue" -> currentPositions.copy(queueX = newPos.x, queueY = newPos.y)
                                "volume" -> currentPositions.copy(volumeX = newPos.x, volumeY = newPos.y)
                                else -> currentPositions
                            }.copy(isCustomLayoutEnabled = true)
                        },
                        onResetAll = {
                            canvasState.recenter()
                            currentPositions = TabletopControlPositions()
                            onUpdateControlPositions(TabletopControlPositions())
                        },
                        onSavePositions = {
                            val saved = currentPositions.copy(
                                panOffsetX = canvasState.panOffset.x,
                                panOffsetY = canvasState.panOffset.y,
                                customScale = canvasState.zoomScale,
                                isGridSnapEnabled = canvasState.isGridSnapEnabled,
                                isCustomLayoutEnabled = true
                            )
                            currentPositions = saved
                            onUpdateControlPositions(saved)
                        },
                        accentColor = animatedAccent,
                        canvasBackgroundColor = Color.Transparent
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
private fun NetflixDuoTabletopConsole(
    track: Track,
    isPlaying: Boolean,
    activePositionMs: Long,
    trackDurationMs: Long,
    speed: Float,
    volume: Float,
    isShuffleActive: Boolean = false,
    repeatMode: RepeatMode,
    visualizerStyle: VisualizerStyle,
    currentDisplayMode: DisplayMode,
    tabletopControlsScale: Float,
    showTrackListOnBottom: Boolean,
    allTracks: List<Track>,
    animatedAccent: Color,
    buttonAnimationEffect: ButtonAnimationEffect,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleShuffle: () -> Unit = {},
    onCycleRepeat: () -> Unit,
    onVisualizerStyleChange: (VisualizerStyle) -> Unit,
    onToggleTrackList: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlayFromContext: (List<Track>, Int) -> Unit = { list, idx ->
        if (idx in list.indices) onTrackClick(list[idx])
    },
    onExitFlexMode: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenDisplayMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = Color(0x38202538)
    val cardBorder = Color(0x22FFFFFF)
    val netflixRed = Color(0xFFE50914)
    val iconScaleFactor = (tabletopControlsScale.coerceIn(0.70f, 5.00f) / 1.0f).coerceIn(0.85f, 2.6f)

    if (showTrackListOnBottom) {
        val currentTrackIndex = remember(allTracks, track.id) {
            val idx = allTracks.indexOfFirst { it.id == track.id }
            if (idx >= 0) idx else 0
        }
        val queueListState = rememberSaveable(saver = LazyListState.Saver) {
            LazyListState(firstVisibleItemIndex = currentTrackIndex)
        }
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Track Queue (${allTracks.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                IconButton(
                    onClick = onToggleTrackList,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Queue",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Queue List with Quick Scrollbar (Screenshot 4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    state = queueListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(allTracks, key = { _, it -> it.id }) { index, itemTrack ->
                        val isSelected = itemTrack.id == track.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(
                                    fadeInSpec = MotionSpecs.snappySpring(),
                                    placementSpec = MotionSpecs.LazyItemPlacementSpring,
                                    fadeOutSpec = MotionSpecs.snappySpring()
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) netflixRed.copy(alpha = 0.22f) else Color(0x18FFFFFF))
                                .border(1.dp, if (isSelected) netflixRed else Color.Transparent, RoundedCornerShape(12.dp))
                                .bouncableClick(pressedScale = 0.96f) { onPlayFromContext(allTracks, index) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoverArtView(
                                track = itemTrack,
                                size = 40.dp,
                                cornerRadius = 8.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = itemTrack.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) netflixRed else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = if (isSelected) {
                                        Modifier
                                            .fillMaxWidth()
                                            .basicMarquee(iterations = Int.MAX_VALUE)
                                    } else {
                                        Modifier
                                    }
                                )
                                Text(
                                    text = itemTrack.artist,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0x99FFFFFF),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = formatTime(itemTrack.durationMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0x77FFFFFF)
                            )
                        }
                    }
                }

                if (allTracks.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    BoxWithConstraints(
                        modifier = Modifier
                            .width(26.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x18FFFFFF))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        val totalItems = allTracks.size
                        val scrollbarHeight = maxHeight
                        val firstVisible = queueListState.firstVisibleItemIndex
                        val thumbHeight = 36.dp
                        val maxOffset = (scrollbarHeight - thumbHeight).coerceAtLeast(0.dp)
                        val progress = if (totalItems > 1) {
                            (firstVisible.toFloat() / (totalItems - 1).toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        var isDraggingThumb by remember { mutableStateOf(false) }
                        var draggedIdx by remember { mutableStateOf(firstVisible.coerceIn(0, (totalItems - 1).coerceAtLeast(0))) }

                        // Center rail line
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                                .align(Alignment.Center)
                        )

                        // Interaction area: tap or drag to quickly seek through queue
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(totalItems) {
                                    detectTapGestures { offset ->
                                        val ratio = (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                                        val targetIdx = ((ratio * (totalItems - 1)).toInt()).coerceIn(0, totalItems - 1)
                                        draggedIdx = targetIdx
                                        coroutineScope.launch {
                                            queueListState.scrollToItem(targetIdx)
                                        }
                                    }
                                }
                                .pointerInput(totalItems) {
                                    detectVerticalDragGestures(
                                        onDragStart = { offset ->
                                            isDraggingThumb = true
                                            val ratio = (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                                            val targetIdx = ((ratio * (totalItems - 1)).toInt()).coerceIn(0, totalItems - 1)
                                            draggedIdx = targetIdx
                                        },
                                        onDragEnd = { isDraggingThumb = false },
                                        onDragCancel = { isDraggingThumb = false },
                                        onVerticalDrag = { change, _ ->
                                            change.consume()
                                            val ratio = (change.position.y / size.height.toFloat()).coerceIn(0f, 1f)
                                            val targetIdx = ((ratio * (totalItems - 1)).toInt()).coerceIn(0, totalItems - 1)
                                            draggedIdx = targetIdx
                                            coroutineScope.launch {
                                                queueListState.scrollToItem(targetIdx)
                                            }
                                        }
                                    )
                                }
                        ) {
                            val thumbYOffset = maxOffset * progress
                            Box(
                                modifier = Modifier
                                    .offset(y = thumbYOffset)
                                    .align(Alignment.TopCenter)
                                    .size(width = 22.dp, height = thumbHeight)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDraggingThumb) netflixRed else Color.White.copy(alpha = 0.85f))
                                    .border(1.dp, if (isDraggingThumb) Color.White else Color(0x33000000), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = "Scroll Thumb",
                                    tint = if (isDraggingThumb) Color.White else Color.Black.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            if (isDraggingThumb && draggedIdx in allTracks.indices) {
                                val hoveredChar = allTracks[draggedIdx].title.firstOrNull()?.let {
                                    if (it.isLetter()) it.uppercaseChar() else '#'
                                } ?: '#'
                                val density = LocalDensity.current
                                val yPx = with(density) { thumbYOffset.roundToPx() }
                                val xGap = with(density) { 40.dp.roundToPx() }
                                androidx.compose.ui.window.Popup(
                                    alignment = Alignment.TopEnd,
                                    offset = IntOffset(x = -xGap, y = yPx)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .shadow(12.dp, CircleShape)
                                            .clip(CircleShape)
                                            .background(netflixRed)
                                            .border(2.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = hoveredChar.toString(),
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Scrubber drag state
        var isDragging by remember(track.id) { mutableStateOf(false) }
        var dragFraction by remember(track.id) { mutableFloatStateOf(0f) }
        val currentTrackDurationMs by rememberUpdatedState(trackDurationMs)
        val currentOnSeek by rememberUpdatedState(onSeek)
        val currentProgress = if (isDragging) dragFraction else {
            if (trackDurationMs > 0) (activePositionMs.toFloat() / trackDurationMs.toFloat()).coerceIn(0f, 1f) else 0f
        }

        // Tactile button bounce & animation effects on press
        val playInteractionSource = remember { MutableInteractionSource() }
        val isPlayPressed by playInteractionSource.collectIsPressedAsState()
        val prevInteractionSource = remember { MutableInteractionSource() }
        val isPrevPressed by prevInteractionSource.collectIsPressedAsState()
        val nextInteractionSource = remember { MutableInteractionSource() }
        val isNextPressed by nextInteractionSource.collectIsPressedAsState()
        val repeatInteractionSource = remember { MutableInteractionSource() }
        val isRepeatPressed by repeatInteractionSource.collectIsPressedAsState()
        val shuffleInteractionSource = remember { MutableInteractionSource() }
        val isShufflePressed by shuffleInteractionSource.collectIsPressedAsState()
        val speedInteractionSource = remember { MutableInteractionSource() }
        val isSpeedPressed by speedInteractionSource.collectIsPressedAsState()
        val muteInteractionSource = remember { MutableInteractionSource() }
        val isMutePressed by muteInteractionSource.collectIsPressedAsState()

        // Track the last active non-zero volume so we can restore it on unmute
        var lastNonZeroVolume by remember { mutableFloatStateOf(if (volume > 0.05f) volume else 0.85f) }
        androidx.compose.runtime.LaunchedEffect(volume) {
            if (volume > 0.05f) {
                lastNonZeroVolume = volume
            }
        }
        val currentVolume by rememberUpdatedState(volume)
        val currentLastNonZeroVolume by rememberUpdatedState(lastNonZeroVolume)
        val currentSpeed by rememberUpdatedState(speed)
        val currentOnVolumeChange by rememberUpdatedState(onVolumeChange)
        val currentOnSpeedChange by rememberUpdatedState(onSpeedChange)

        fun tileAnimTargets(pressed: Boolean): Triple<Float, Float, Float> = when (buttonAnimationEffect) {
            ButtonAnimationEffect.BOUNCY_SPRING -> {
                val s = if (pressed) 0.88f else 1.0f
                Triple(s, s, 0f)
            }
            ButtonAnimationEffect.ROTATION_TILT -> {
                val s = if (pressed) 0.88f else 1.0f
                val r = if (pressed) 6f else 0f
                Triple(s, s, r)
            }
            ButtonAnimationEffect.NEON_GLOW_POP -> {
                val s = if (pressed) 0.92f else 1.0f
                Triple(s, s, 0f)
            }
            ButtonAnimationEffect.JELLY_SQUISH -> {
                if (pressed) Triple(1.06f, 0.85f, 0f) else Triple(1.0f, 1.0f, 0f)
            }
            ButtonAnimationEffect.SUBTLE_HAPTIC -> {
                val s = if (pressed) 0.96f else 1.0f
                Triple(s, s, 0f)
            }
        }

        val (prevTargetScaleX, prevTargetScaleY, prevTargetRotation) = tileAnimTargets(isPrevPressed)
        val (nextTargetScaleX, nextTargetScaleY, nextTargetRotation) = tileAnimTargets(isNextPressed)
        val (playTargetScaleX, playTargetScaleY, playTargetRotation) = tileAnimTargets(isPlayPressed)

        val (repeatTargetScaleX, repeatTargetScaleY) = when (buttonAnimationEffect) {
            ButtonAnimationEffect.BOUNCY_SPRING -> (if (isRepeatPressed) 0.85f else 1.0f) to (if (isRepeatPressed) 0.85f else 1.0f)
            ButtonAnimationEffect.ROTATION_TILT -> (if (isRepeatPressed) 0.88f else 1.0f) to (if (isRepeatPressed) 0.88f else 1.0f)
            ButtonAnimationEffect.NEON_GLOW_POP -> (if (isRepeatPressed) 1.20f else 1.0f) to (if (isRepeatPressed) 1.20f else 1.0f)
            ButtonAnimationEffect.JELLY_SQUISH -> (if (isRepeatPressed) 1.22f else 1.0f) to (if (isRepeatPressed) 0.80f else 1.0f)
            ButtonAnimationEffect.SUBTLE_HAPTIC -> (if (isRepeatPressed) 0.95f else 1.0f) to (if (isRepeatPressed) 0.95f else 1.0f)
        }

        val (shuffleTargetScaleX, shuffleTargetScaleY) = when (buttonAnimationEffect) {
            ButtonAnimationEffect.BOUNCY_SPRING -> (if (isShufflePressed) 0.85f else 1.0f) to (if (isShufflePressed) 0.85f else 1.0f)
            ButtonAnimationEffect.ROTATION_TILT -> (if (isShufflePressed) 0.88f else 1.0f) to (if (isShufflePressed) 0.88f else 1.0f)
            ButtonAnimationEffect.NEON_GLOW_POP -> (if (isShufflePressed) 1.20f else 1.0f) to (if (isShufflePressed) 1.20f else 1.0f)
            ButtonAnimationEffect.JELLY_SQUISH -> (if (isShufflePressed) 1.22f else 1.0f) to (if (isShufflePressed) 0.80f else 1.0f)
            ButtonAnimationEffect.SUBTLE_HAPTIC -> (if (isShufflePressed) 0.95f else 1.0f) to (if (isShufflePressed) 0.95f else 1.0f)
        }

        val (speedTargetScaleX, speedTargetScaleY) = when (buttonAnimationEffect) {
            ButtonAnimationEffect.BOUNCY_SPRING -> (if (isSpeedPressed) 0.85f else 1.0f) to (if (isSpeedPressed) 0.85f else 1.0f)
            ButtonAnimationEffect.ROTATION_TILT -> (if (isSpeedPressed) 0.88f else 1.0f) to (if (isSpeedPressed) 0.88f else 1.0f)
            ButtonAnimationEffect.NEON_GLOW_POP -> (if (isSpeedPressed) 1.20f else 1.0f) to (if (isSpeedPressed) 1.20f else 1.0f)
            ButtonAnimationEffect.JELLY_SQUISH -> (if (isSpeedPressed) 1.22f else 1.0f) to (if (isSpeedPressed) 0.80f else 1.0f)
            ButtonAnimationEffect.SUBTLE_HAPTIC -> (if (isSpeedPressed) 0.95f else 1.0f) to (if (isSpeedPressed) 0.95f else 1.0f)
        }

        val (muteTargetScaleX, muteTargetScaleY) = when (buttonAnimationEffect) {
            ButtonAnimationEffect.BOUNCY_SPRING -> (if (isMutePressed) 0.85f else 1.0f) to (if (isMutePressed) 0.85f else 1.0f)
            ButtonAnimationEffect.ROTATION_TILT -> (if (isMutePressed) 0.88f else 1.0f) to (if (isMutePressed) 0.88f else 1.0f)
            ButtonAnimationEffect.NEON_GLOW_POP -> (if (isMutePressed) 1.20f else 1.0f) to (if (isMutePressed) 1.20f else 1.0f)
            ButtonAnimationEffect.JELLY_SQUISH -> (if (isMutePressed) 1.22f else 1.0f) to (if (isMutePressed) 0.80f else 1.0f)
            ButtonAnimationEffect.SUBTLE_HAPTIC -> (if (isMutePressed) 0.95f else 1.0f) to (if (isMutePressed) 0.95f else 1.0f)
        }

        val springSpec = when (buttonAnimationEffect) {
            ButtonAnimationEffect.BOUNCY_SPRING -> MotionSpecs.TactilePressSpring
            ButtonAnimationEffect.ROTATION_TILT -> MotionSpecs.ExpressiveSpring
            ButtonAnimationEffect.NEON_GLOW_POP -> spring<Float>(dampingRatio = 0.58f, stiffness = Spring.StiffnessMedium)
            ButtonAnimationEffect.JELLY_SQUISH -> spring<Float>(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)
            ButtonAnimationEffect.SUBTLE_HAPTIC -> MotionSpecs.SnappySpring
        }

        val prevScaleX by animateFloatAsState(targetValue = prevTargetScaleX, animationSpec = springSpec, label = "netflix_prev_sx")
        val prevScaleY by animateFloatAsState(targetValue = prevTargetScaleY, animationSpec = springSpec, label = "netflix_prev_sy")
        val prevRotation by animateFloatAsState(targetValue = prevTargetRotation, animationSpec = springSpec, label = "netflix_prev_rot")

        val nextScaleX by animateFloatAsState(targetValue = nextTargetScaleX, animationSpec = springSpec, label = "netflix_next_sx")
        val nextScaleY by animateFloatAsState(targetValue = nextTargetScaleY, animationSpec = springSpec, label = "netflix_next_sy")
        val nextRotation by animateFloatAsState(targetValue = nextTargetRotation, animationSpec = springSpec, label = "netflix_next_rot")

        val playScaleX by animateFloatAsState(targetValue = playTargetScaleX, animationSpec = springSpec, label = "netflix_play_sx")
        val playScaleY by animateFloatAsState(targetValue = playTargetScaleY, animationSpec = springSpec, label = "netflix_play_sy")
        val playRotation by animateFloatAsState(targetValue = playTargetRotation, animationSpec = springSpec, label = "netflix_play_rot")

        val repeatScaleX by animateFloatAsState(targetValue = repeatTargetScaleX, animationSpec = springSpec, label = "netflix_rep_sx")
        val repeatScaleY by animateFloatAsState(targetValue = repeatTargetScaleY, animationSpec = springSpec, label = "netflix_rep_sy")

        val shuffleScaleX by animateFloatAsState(targetValue = shuffleTargetScaleX, animationSpec = springSpec, label = "netflix_shuf_sx")
        val shuffleScaleY by animateFloatAsState(targetValue = shuffleTargetScaleY, animationSpec = springSpec, label = "netflix_shuf_sy")

        val speedScaleX by animateFloatAsState(targetValue = speedTargetScaleX, animationSpec = springSpec, label = "netflix_spd_sx")
        val speedScaleY by animateFloatAsState(targetValue = speedTargetScaleY, animationSpec = springSpec, label = "netflix_spd_sy")

        val muteScaleX by animateFloatAsState(targetValue = muteTargetScaleX, animationSpec = springSpec, label = "netflix_mute_sx")
        val muteScaleY by animateFloatAsState(targetValue = muteTargetScaleY, animationSpec = springSpec, label = "netflix_mute_sy")

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ==========================================
            // ROW 1: TOP CONTROL BAR
            // [X]  [ (OneMusic Logo) Track Title / Artist  > ]  [Display Mode]  [Equalizer]
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Close / Collapse 'X'
                Box(
                    modifier = Modifier
                        .size(width = 52.dp, height = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                        .bouncableClick(pressedScale = 0.92f) { onExitFlexMode() }
                        .testTag("netflix_duo_close_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Tabletop",
                        tint = Color.White,
                        modifier = Modifier.size((20 * iconScaleFactor.coerceAtMost(1.4f)).dp)
                    )
                }

                // 2. Center Metadata Bar: OneMusic App Logo + Title & Artist + Chevron ">"
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                        .bouncableClick(pressedScale = 0.97f) { onToggleTrackList() }
                        .padding(horizontal = 12.dp)
                        .testTag("netflix_duo_track_info_bar"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // OneMusic App Logo Badge (matches OneMusic app icon)
                    Box(
                        modifier = Modifier
                            .size((32 * iconScaleFactor.coerceAtMost(1.25f)).dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_onemusic_app_icon),
                            contentDescription = "OneMusic App Icon",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (13 * iconScaleFactor.coerceAtMost(1.2f)).sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee(iterations = Int.MAX_VALUE)
                        )
                        Text(
                            text = if (track.artist.isNotBlank()) track.artist else "OneMusic Expressive",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = (10.5f * iconScaleFactor.coerceAtMost(1.2f)).sp
                            ),
                            color = Color(0x99FFFFFF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Queue",
                        tint = Color(0x88FFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // 3. Screen Mode Button (Screenshot 2: cycles Art -> Lyrics -> Split -> Visualizer -> Art)
                Box(
                    modifier = Modifier
                        .size(width = 50.dp, height = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                        .bouncableClick(pressedScale = 0.92f) { onOpenDisplayMode() }
                        .testTag("netflix_duo_display_mode_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (currentDisplayMode) {
                            DisplayMode.ARTWORK -> Icons.Default.SmartDisplay
                            DisplayMode.LYRICS -> Icons.Default.Subtitles
                            DisplayMode.SPLIT -> Icons.Default.ViewSidebar
                            DisplayMode.VISUALIZER -> Icons.Default.Waves
                        },
                        contentDescription = "Cycle Screen Mode (Current: ${currentDisplayMode.name})",
                        tint = Color.White,
                        modifier = Modifier.size((20 * iconScaleFactor.coerceAtMost(1.4f)).dp)
                    )
                }

                // 4. Audio Equalizer / SoundAlive
                Box(
                    modifier = Modifier
                        .size(width = 50.dp, height = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                        .bouncableClick(pressedScale = 0.92f) { onOpenEqualizer() }
                        .testTag("netflix_duo_audio_tools_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Audio Equalizer & SoundAlive",
                        tint = Color.White,
                        modifier = Modifier.size((20 * iconScaleFactor.coerceAtMost(1.4f)).dp)
                    )
                }
            }

            // ==========================================
            // ROW 2: MASSIVE OVERSIZED MAIN PLAYBACK TILES (Huge Height, weight 1f)
            // [ |< Previous Track ]   [ || or ▶ Play/Pause ]   [ >| Next Track ]
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Massive Tile: Previous Track (Screenshot 1)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = prevScaleX
                            scaleY = prevScaleY
                            rotationZ = prevRotation
                        }
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isPrevPressed) Color(0x55283048) else cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val press = PressInteraction.Press(offset)
                                    prevInteractionSource.emit(press)
                                    if (tryAwaitRelease()) {
                                        prevInteractionSource.emit(PressInteraction.Release(press))
                                    } else {
                                        prevInteractionSource.emit(PressInteraction.Cancel(press))
                                    }
                                },
                                onTap = { onPrevious() }
                            )
                        }
                        .testTag("netflix_duo_previous"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = Color.White,
                        modifier = Modifier.size((52 * iconScaleFactor.coerceIn(0.85f, 2.3f)).dp)
                    )
                }

                // Center Massive Tile: Giant Play / Pause
                Box(
                    modifier = Modifier
                        .weight(1.05f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = playScaleX
                            scaleY = playScaleY
                            rotationZ = playRotation
                        }
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isPlayPressed) Color(0x55283048) else cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val press = PressInteraction.Press(offset)
                                    playInteractionSource.emit(press)
                                    if (tryAwaitRelease()) {
                                        playInteractionSource.emit(PressInteraction.Release(press))
                                    } else {
                                        playInteractionSource.emit(PressInteraction.Cancel(press))
                                    }
                                },
                                onTap = { onTogglePlayPause() }
                            )
                        }
                        .testTag("netflix_duo_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isPlaying,
                        transitionSpec = { MotionSpecs.iconMorphTransition() },
                        label = "netflix_play_icon_morph"
                    ) { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size((62 * iconScaleFactor.coerceIn(0.85f, 2.5f)).dp)
                        )
                    }
                }

                // Right Massive Tile: Next Track (Screenshot 1)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = nextScaleX
                            scaleY = nextScaleY
                            rotationZ = nextRotation
                        }
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isNextPressed) Color(0x55283048) else cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val press = PressInteraction.Press(offset)
                                    nextInteractionSource.emit(press)
                                    if (tryAwaitRelease()) {
                                        nextInteractionSource.emit(PressInteraction.Release(press))
                                    } else {
                                        nextInteractionSource.emit(PressInteraction.Cancel(press))
                                    }
                                },
                                onTap = { onNext() }
                            )
                        }
                        .testTag("netflix_duo_next"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = Color.White,
                        modifier = Modifier.size((52 * iconScaleFactor.coerceIn(0.85f, 2.3f)).dp)
                    )
                }
            }

            // ==========================================
            // ROW 3: FULL-WIDTH PROGRESS SCRUBBER BAR CAPSULE
            // [ ────●──────────────────────   4:10 ]
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp)
                    .testTag("netflix_duo_scrubber_capsule"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Interactive horizontal drag & tap progress bar
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val widthPx = size.width.toFloat().coerceAtLeast(1f)
                                    isDragging = true
                                    var latestFraction = (down.position.x / widthPx).coerceIn(0f, 1f)
                                    dragFraction = latestFraction
                                    down.consume()

                                    var activePointerId = down.id
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == activePointerId }
                                            ?: event.changes.firstOrNull()
                                            ?: break
                                        activePointerId = change.id
                                        if (!change.pressed) {
                                            latestFraction = (change.position.x / widthPx).coerceIn(0f, 1f)
                                            dragFraction = latestFraction
                                            change.consume()
                                            break
                                        }
                                        if (change.position != change.previousPosition) {
                                            latestFraction = (change.position.x / widthPx).coerceIn(0f, 1f)
                                            dragFraction = latestFraction
                                            change.consume()
                                        }
                                    }
                                    isDragging = false
                                    currentOnSeek((latestFraction * currentTrackDurationMs).toLong())
                                }
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val barWidth = maxWidth
                        val progressWidth = barWidth * currentProgress

                        // Inactive rail
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(Color(0x35FFFFFF))
                        )

                        // Active red track
                        Box(
                            modifier = Modifier
                                .width(progressWidth)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(netflixRed)
                        )

                        // Pill Scrubber Thumb
                        Box(
                            modifier = Modifier
                                .offset(x = (progressWidth - 6.dp).coerceAtLeast(0.dp))
                                .size(width = 12.dp, height = 18.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(netflixRed)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Time Text
                    Text(
                        text = formatTime(if (isDragging) (dragFraction * trackDurationMs).toLong() else activePositionMs),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.5.sp
                        )
                    )
                }
            }

            // ==========================================
            // ROW 4: BOTTOM UTILITY BUTTONS (4 Equal Tiles)
            // [Dynamic Speedometer]  [Shuffle: On/Off]  [Repeat: 1/All/Off]  [Volume / Mute]
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Dynamic Speedometer (Screenshot 3: dynamically displays rate of speed)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = speedScaleX
                            scaleY = speedScaleY
                        }
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val press = PressInteraction.Press(offset)
                                    speedInteractionSource.emit(press)
                                    if (tryAwaitRelease()) {
                                        speedInteractionSource.emit(PressInteraction.Release(press))
                                    } else {
                                        speedInteractionSource.emit(PressInteraction.Cancel(press))
                                    }
                                },
                                onTap = {
                                    val nextSpeed = when (currentSpeed) {
                                        0.8f -> 1.0f
                                        1.0f -> 1.25f
                                        1.25f -> 1.5f
                                        2.0f -> 0.8f
                                        else -> 1.0f
                                    }
                                    currentOnSpeedChange(nextSpeed)
                                }
                            )
                        }
                        .testTag("netflix_duo_speed_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Playback Speed: ${speed}x",
                            tint = Color.White,
                            modifier = Modifier.size((18 * iconScaleFactor.coerceAtMost(1.3f)).dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()}x",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = (12 * iconScaleFactor.coerceAtMost(1.3f)).sp
                            )
                        )
                    }
                }

                // 2. Shuffle Button (replaces Repeat in slot 2)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = shuffleScaleX
                            scaleY = shuffleScaleY
                        }
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isShuffleActive) animatedAccent.copy(alpha = 0.22f) else cardBg
                        )
                        .border(
                            1.dp,
                            if (isShuffleActive) animatedAccent.copy(alpha = 0.6f) else cardBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val press = PressInteraction.Press(offset)
                                    shuffleInteractionSource.emit(press)
                                    if (tryAwaitRelease()) {
                                        shuffleInteractionSource.emit(PressInteraction.Release(press))
                                    } else {
                                        shuffleInteractionSource.emit(PressInteraction.Cancel(press))
                                    }
                                },
                                onTap = { onToggleShuffle() }
                            )
                        }
                        .testTag("netflix_duo_shuffle_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle: ${if (isShuffleActive) "On" else "Off"}",
                            tint = if (isShuffleActive) Color.White else Color(0x66FFFFFF),
                            modifier = Modifier.size((18 * iconScaleFactor.coerceAtMost(1.3f)).dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isShuffleActive) "On" else "Off",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isShuffleActive) Color.White else Color(0x66FFFFFF),
                                fontSize = (12 * iconScaleFactor.coerceAtMost(1.3f)).sp
                            )
                        )
                    }
                }

                // 3. Repeat Button (moved to slot 3 where Mute was; dynamic repeat indicator: 1, All, Off; cycles on press)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = repeatScaleX
                            scaleY = repeatScaleY
                        }
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (repeatMode != RepeatMode.OFF) animatedAccent.copy(alpha = 0.22f) else cardBg
                        )
                        .border(
                            1.dp,
                            if (repeatMode != RepeatMode.OFF) animatedAccent.copy(alpha = 0.6f) else cardBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val press = PressInteraction.Press(offset)
                                    repeatInteractionSource.emit(press)
                                    if (tryAwaitRelease()) {
                                        repeatInteractionSource.emit(PressInteraction.Release(press))
                                    } else {
                                        repeatInteractionSource.emit(PressInteraction.Cancel(press))
                                    }
                                },
                                onTap = { onCycleRepeat() }
                            )
                        }
                        .testTag("netflix_duo_repeat_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AnimatedContent(
                            targetState = repeatMode,
                            transitionSpec = { MotionSpecs.iconMorphTransition() },
                            label = "netflix_repeat_icon_morph"
                        ) { mode ->
                            Icon(
                                imageVector = if (mode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = "Repeat Mode: ${mode.name}",
                                tint = if (mode == RepeatMode.OFF) Color(0x66FFFFFF) else Color.White,
                                modifier = Modifier.size((18 * iconScaleFactor.coerceAtMost(1.3f)).dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (repeatMode) {
                                RepeatMode.ONE -> "1"
                                RepeatMode.ALL -> "All"
                                RepeatMode.OFF -> "Off"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (repeatMode == RepeatMode.OFF) Color(0x66FFFFFF) else Color.White,
                                fontSize = (12 * iconScaleFactor.coerceAtMost(1.3f)).sp
                            )
                        )
                    }
                }

                // 4. Mute / Speaker Button (moved to slot 4 on far right, replacing duplicate track queue)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = muteScaleX
                            scaleY = muteScaleY
                        }
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (volume <= 0.05f) animatedAccent.copy(alpha = 0.22f) else cardBg
                        )
                        .border(
                            1.dp,
                            if (volume <= 0.05f) animatedAccent.copy(alpha = 0.6f) else cardBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val press = PressInteraction.Press(offset)
                                    muteInteractionSource.emit(press)
                                    if (tryAwaitRelease()) {
                                        muteInteractionSource.emit(PressInteraction.Release(press))
                                    } else {
                                        muteInteractionSource.emit(PressInteraction.Cancel(press))
                                    }
                                },
                                onTap = {
                                    val nextVol = if (currentVolume > 0.05f) {
                                        0f
                                    } else {
                                        if (currentLastNonZeroVolume > 0.05f) currentLastNonZeroVolume else 0.85f
                                    }
                                    currentOnVolumeChange(nextVol)
                                }
                            )
                        }
                        .testTag("netflix_duo_volume_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (volume > 0.05f) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (volume <= 0.05f) "Unmute" else "Mute",
                        tint = if (volume <= 0.05f) animatedAccent else Color.White,
                        modifier = Modifier.size((22 * iconScaleFactor.coerceAtMost(1.4f)).dp)
                    )
                }
            }
        }
    }
}

