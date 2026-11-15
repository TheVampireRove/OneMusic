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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.model.AlbumArtSize
import com.example.model.ElementColors
import com.example.model.LyricLine
import com.example.model.RepeatMode
import com.example.model.Track
import com.example.model.VisualizerStyle
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.OneUISquircleLarge
import com.example.ui.theme.OneUISquircleMedium
import com.example.ui.theme.glassmorphism
import com.example.ui.theme.parseHexColor

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
    onImportLyricsUri: (Uri) -> Unit = {},
    onSavePastedLyrics: (String) -> Unit = {},
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

    var displayMode by remember { mutableStateOf(DisplayMode.ARTWORK) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragPosition by remember { mutableFloatStateOf(0f) }

    val activePositionMs = if (isDraggingSlider) {
        (sliderDragPosition * track.durationMs).toLong()
    } else {
        currentPositionMs
    }

    // Dynamic animated colors
    val animatedPrimary by animateColorAsState(track.primaryColor, label = "bg_primary")
    val animatedSecondary by animateColorAsState(track.secondaryColor, label = "bg_secondary")
    val animatedAccent by animateColorAsState(track.accentColor, label = "bg_accent")

    Box(
        modifier = modifier
            .fillMaxSize()
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
                    selected = displayMode == DisplayMode.ARTWORK,
                    title = "Artwork",
                    accentColor = animatedAccent,
                    onClick = { displayMode = DisplayMode.ARTWORK }
                )
                ModePillButton(
                    selected = displayMode == DisplayMode.LYRICS,
                    title = "Lyrics",
                    accentColor = animatedAccent,
                    onClick = { displayMode = DisplayMode.LYRICS }
                )
                VisualizerModePillWithDropdown(
                    selected = displayMode == DisplayMode.VISUALIZER,
                    currentStyle = visualizerStyle,
                    accentColor = animatedAccent,
                    onClick = { displayMode = DisplayMode.VISUALIZER },
                    onStyleSelected = { newStyle ->
                        displayMode = DisplayMode.VISUALIZER
                        onVisualizerStyleChange(newStyle)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seamless Middle Visual (Artwork vs Lyrics vs Visualizer)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = displayMode,
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
                                CoverArtView(
                                    track = track,
                                    size = visualizerArtSizeDp,
                                    cornerRadius = (visualizerArtSizeDp.value * 0.11f).dp,
                                    isPlaying = isPlaying,
                                    showVinylPeek = true,
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
                                onSelectLyric = { line ->
                                    if (line.isSynced) onSeek(line.timeMs)
                                },
                                onImportLyricsUri = onImportLyricsUri,
                                onSavePastedLyrics = onSavePastedLyrics
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
                                    CoverArtView(
                                        track = track,
                                        size = 180.dp,
                                        cornerRadius = 28.dp,
                                        isPlaying = isPlaying,
                                        showVinylPeek = false,
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
                                        onSelectLyric = { line ->
                                            if (line.isSynced) onSeek(line.timeMs)
                                        },
                                        onImportLyricsUri = onImportLyricsUri,
                                        onSavePastedLyrics = onSavePastedLyrics
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
                        overflow = TextOverflow.Ellipsis
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
                            .size(46.dp)
                            .glassmorphism(shape = CircleShape, backgroundColor = Color(0x28FFFFFF))
                            .testTag("favorite_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        val scale by animateFloatAsState(
                            targetValue = if (track.isFavorite) 1.15f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.5f),
                            label = "fav_scale"
                        )
                        Icon(
                            imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (track.isFavorite) Color(0xFFFF4081) else Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(scale)
                        )
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
                ButtonAnimationEffect.BOUNCY_SPRING -> spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                ButtonAnimationEffect.ROTATION_TILT -> spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                ButtonAnimationEffect.NEON_GLOW_POP -> spring<Float>(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMediumLow)
                ButtonAnimationEffect.JELLY_SQUISH -> spring<Float>(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)
                ButtonAnimationEffect.SUBTLE_HAPTIC -> spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
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
                    // Shuffle button with blurred glass look
                    OneUITooltipButton(
                        name = if (isShuffleActive) "Shuffle: Enabled" else "Shuffle: Disabled",
                        onClick = onToggleShuffle
                    ) {
                        Box(
                            modifier = Modifier
                                .size(effectiveModeBtn)
                                .glassmorphism(
                                    shape = CircleShape,
                                    backgroundColor = if (isShuffleActive) animatedAccent.copy(alpha = 0.28f) else Color(0x20FFFFFF),
                                    borderWidth = 1.dp
                                )
                                .testTag("shuffle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffleActive) animatedAccent else Color(0x88FFFFFF),
                                modifier = Modifier.size(effectiveModeIcon)
                            )
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
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.82f)) togetherWith
                                    (fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.82f))
                                },
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

                    // Repeat button with blurred glass look
                    OneUITooltipButton(
                        name = when (repeatMode) {
                            RepeatMode.OFF -> "Repeat: Off"
                            RepeatMode.ALL -> "Repeat: All Tracks"
                            RepeatMode.ONE -> "Repeat: Current Track"
                        },
                        onClick = onCycleRepeat
                    ) {
                        Box(
                            modifier = Modifier
                                .size(effectiveModeBtn)
                                .glassmorphism(
                                    shape = CircleShape,
                                    backgroundColor = if (repeatMode != RepeatMode.OFF) animatedAccent.copy(alpha = 0.28f) else Color(0x20FFFFFF),
                                    borderWidth = 1.dp
                                )
                                .testTag("repeat_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = "Repeat",
                                tint = if (repeatMode != RepeatMode.OFF) animatedAccent else Color(0x88FFFFFF),
                                modifier = Modifier.size(effectiveModeIcon)
                            )
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
    OneUITooltipButton(
        name = "$title View",
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .clip(OneUIPillShape)
                .background(if (selected) accentColor else Color.Transparent)
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
                    .background(if (selected) accentColor else Color.Transparent)
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

@Composable
fun LyricsView(
    lyrics: List<LyricLine>,
    currentPositionMs: Long,
    primaryColor: Color,
    inactiveTextColor: Color = Color(0x66FFFFFF),
    isLoading: Boolean = false,
    onSelectLyric: (LyricLine) -> Unit,
    onImportLyricsUri: (Uri) -> Unit = {},
    onSavePastedLyrics: (String) -> Unit = {}
) {
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    var showPasteDialog by remember { mutableStateOf(false) }
    var pastedLyricsInput by remember { mutableStateOf("") }

    val lrcFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportLyricsUri(uri)
        }
    }

    val isSyncedTrack = lyrics.any { it.isSynced }
    val activeIndex = lyrics.indexOfLast { it.timeMs <= currentPositionMs }.coerceAtLeast(0)

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
                            onClick = {
                                lrcFilePickerLauncher.launch(arrayOf("*/*"))
                            },
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
                                pastedLyricsInput = ""
                                showPasteDialog = true
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
                contentPadding = PaddingValues(vertical = 36.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val isActive = if (isSyncedTrack) index == activeIndex else false
                    val textColor = when {
                        isActive -> primaryColor
                        !isSyncedTrack -> Color(0xEEFFFFFF)
                        else -> inactiveTextColor
                    }
                    val textStyle = if (isActive) {
                        MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    } else {
                        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = line.isSynced) { onSelectLyric(line) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = line.text,
                            style = textStyle,
                            color = textColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showPasteDialog) {
        AlertDialog(
            onDismissRequest = { showPasteDialog = false },
            title = {
                Text(
                    text = "Paste Lyrics",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Paste plain text or [mm:ss.xx] timestamped LRC lyrics:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xAAFFFFFF)
                    )
                    OutlinedTextField(
                        value = pastedLyricsInput,
                        onValueChange = { pastedLyricsInput = it },
                        placeholder = {
                            Text(
                                text = "[00:12.00] Line one...\n[00:16.50] Line two...",
                                color = Color(0x66FFFFFF)
                            )
                        },
                        minLines = 6,
                        maxLines = 12,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color(0x55FFFFFF),
                            cursorColor = primaryColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("paste_lyrics_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pastedLyricsInput.isNotBlank()) {
                            onSavePastedLyrics(pastedLyricsInput)
                        }
                        showPasteDialog = false
                    },
                    enabled = pastedLyricsInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color(0xFF0C0E1A)
                    ),
                    modifier = Modifier.testTag("save_pasted_lyrics_button")
                ) {
                    Text("Save Lyrics", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteDialog = false }) {
                    Text("Cancel", color = Color(0xCCFFFFFF))
                }
            },
            containerColor = Color(0xFF16182B),
            shape = OneUISquircleLarge
        )
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
