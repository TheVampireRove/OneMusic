package com.example.ui.settings

import android.app.Activity
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import com.example.viewmodel.LyricsSyncUiState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.example.diagnostics.CrashDiagnosticsPreferences
import com.example.diagnostics.CrashReportManager
import com.example.ui.diagnostics.CrashReportsScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import com.example.model.AccentTheme
import com.example.model.AlbumArtSize
import com.example.model.AlbumLayoutSetting
import com.example.model.AppSettings
import com.example.model.DualPaneSetting
import com.example.model.DynamicThemeEngine
import com.example.model.ElementColors
import com.example.model.LyricsAnimationSpeed
import com.example.model.LyricsAnimationStyle
import com.example.model.MusicTab
import com.example.model.ButtonAnimationEffect
import com.example.model.PlayerButtonSize
import com.example.model.ProgressBarDesign
import com.example.model.ProgressBarThickness
import com.example.model.TabletopButtonSize
import com.example.model.TabletopControlPositions
import com.example.model.TabletopControlStyle
import com.example.model.TabletopAlbumArtSize
import com.example.model.TabletopTopLayout
import com.example.model.VisualizerStyle
import com.example.ui.components.ExpressiveScrubber
import com.example.ui.components.OneUITooltipButton
import com.example.ui.theme.NavigationTransitions
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.OneUISquircleLarge
import com.example.ui.theme.OneUISquircleMedium
import com.example.ui.theme.OneUISquircleSmall
import com.example.ui.theme.glassmorphism
import com.example.ui.theme.parseHexColor
import kotlin.coroutines.cancellation.CancellationException

enum class SettingsCategory(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tag: String
) {
    THEME_COLORS(
        title = "Theme & Colors",
        subtitle = "Dynamic color engines, accent themes & hex customization",
        icon = Icons.Default.Palette,
        tag = "theme_colors"
    ),
    PLAYER_VISUALIZER(
        title = "Player & Visualizer",
        subtitle = "Progress bars, button sizing & visualizer styles",
        icon = Icons.Default.Waves,
        tag = "player_visualizer"
    ),
    LYRICS(
        title = "Lyrics & Sync",
        subtitle = "Lyrics animations, auto-fetch, Wi-Fi batch sync & LRCLIB cache",
        icon = Icons.Default.Lyrics,
        tag = "lyrics"
    ),
    TABLETOP_FOLDABLE(
        title = "Tabletop & Foldable",
        subtitle = "Flex mode layouts, console styles, blurred background reflections",
        icon = Icons.Default.StayCurrentLandscape,
        tag = "tabletop_foldable"
    ),
    DISPLAY_LAYOUT(
        title = "Display & Library",
        subtitle = "Dual-pane mode, album grid layouts, alphabet quick scrollbar",
        icon = Icons.Default.GridView,
        tag = "display_layout"
    ),
    AUDIO_ENGINE(
        title = "Audio & SoundAlive",
        subtitle = "SoundAlive Equalizer, screen awake, gapless transition",
        icon = Icons.Default.Tune,
        tag = "audio_engine"
    ),
    DEVICE_AUDIO(
        title = "Local Audio & Storage",
        subtitle = "Import audio files, media scanner & offline library",
        icon = Icons.Default.FolderOpen,
        tag = "device_audio"
    ),
    CAR_ABOUT(
        title = "Connected Car & About",
        subtitle = "Android Auto integration, live notification styles & info",
        icon = Icons.Default.DirectionsCar,
        tag = "car_about"
    ),
    DIAGNOSTICS(
        title = "Diagnostics & Crash Reports",
        subtitle = "Auto crash interceptor, system diagnostics & log file export",
        icon = Icons.Default.BugReport,
        tag = "diagnostics"
    )
}

@Composable
fun SettingsScreen(
    settings: AppSettings,
    accentColor: Color,
    onBack: () -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onOpenEqualizer: () -> Unit,
    localTracksCount: Int = 0,
    totalTracksCount: Int = 0,
    onPickLocalFiles: () -> Unit = {},
    onScanDeviceStorage: () -> Unit = {},
    lyricsSyncUiState: LyricsSyncUiState = LyricsSyncUiState(),
    onStartBatchLyricsSync: () -> Unit = {},
    onCancelBatchLyricsSync: () -> Unit = {},
    onRetryFailedLyricsSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by rememberSaveable { mutableStateOf<SettingsCategory?>(null) }
    val context = LocalContext.current
    val activity = context as? Activity
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val crashPrefs = remember(context) { CrashDiagnosticsPreferences.getInstance(context) }
    val isCrashLoggingEnabled by crashPrefs.isCrashLoggingEnabled.collectAsState(initial = true)
    var showCrashReportsScreen by rememberSaveable { mutableStateOf(false) }

    // Foldable posture & vertical hinge awareness for Z Fold / Pixel Fold / Surface Duo
    var hasVerticalHinge by remember { mutableStateOf(false) }
    var verticalHingeLeftDp by remember { mutableStateOf<Dp?>(null) }
    var verticalHingeWidthDp by remember { mutableStateOf(0.dp) }
    var isVerticalHingeSeparating by remember { mutableStateOf(false) }
    var isFoldableHalfOpened by remember { mutableStateOf(false) }

    LaunchedEffect(activity) {
        if (activity == null) return@LaunchedEffect
        try {
            WindowInfoTracker.getOrCreate(activity)
                .windowLayoutInfo(activity)
                .collectLatest { layoutInfo ->
                    val foldingFeature = layoutInfo.displayFeatures
                        .filterIsInstance<FoldingFeature>()
                        .firstOrNull()
                    if (foldingFeature != null) {
                        isFoldableHalfOpened = foldingFeature.state == FoldingFeature.State.HALF_OPENED
                        if (foldingFeature.orientation == FoldingFeature.Orientation.VERTICAL) {
                            hasVerticalHinge = true
                            isVerticalHingeSeparating = foldingFeature.isSeparating
                            verticalHingeLeftDp = with(density) { foldingFeature.bounds.left.toDp() }
                            verticalHingeWidthDp = with(density) { foldingFeature.bounds.width().toDp() }
                        } else {
                            hasVerticalHinge = false
                            isVerticalHingeSeparating = false
                            verticalHingeLeftDp = null
                            verticalHingeWidthDp = 0.dp
                        }
                    } else {
                        hasVerticalHinge = false
                        isVerticalHingeSeparating = false
                        verticalHingeLeftDp = null
                        verticalHingeWidthDp = 0.dp
                        isFoldableHalfOpened = false
                    }
                }
        } catch (_: Exception) {
            hasVerticalHinge = false
            isVerticalHingeSeparating = false
            verticalHingeLeftDp = null
            verticalHingeWidthDp = 0.dp
            isFoldableHalfOpened = false
        }
    }

    if (showCrashReportsScreen) {
        CrashReportsScreen(
            accentColor = accentColor,
            onBack = { showCrashReportsScreen = false }
        )
        return
    }

    val hubScrollState = rememberScrollState()
    val detailScrollStates = SettingsCategory.entries.associateWith { rememberScrollState() }
    var previewScrubberProgress by remember { mutableFloatStateOf(0.42f) }
    var customHexInput by remember(settings.customHexColor) { mutableStateOf(settings.customHexColor) }
    var showStandaloneColorWheel by remember { mutableStateOf(false) }

    val settingsBg = if (settings.useCustomElementColors) {
        parseHexColor(settings.elementColors.settingsBackground) ?: Color(0xFF0C0D18)
    } else {
        Color(0xFF0C0D18)
    }
    val settingsHeaderColor = if (settings.useCustomElementColors) {
        parseHexColor(settings.elementColors.settingsCategoryHeaderColor) ?: accentColor
    } else {
        accentColor
    }
    val settingsTextColor = if (settings.useCustomElementColors) {
        parseHexColor(settings.elementColors.settingsTextColor) ?: Color.White
    } else {
        Color.White
    }
    val settingsBackButtonColor = if (settings.useCustomElementColors) {
        parseHexColor(settings.elementColors.settingsBackButtonColor) ?: Color.White
    } else {
        Color.White
    }

    if (showStandaloneColorWheel) {
        ColorWheelDialog(
            initialHex = settings.customHexColor,
            onDismiss = { showStandaloneColorWheel = false },
            onColorSelected = { chosenHex ->
                customHexInput = chosenHex
                onUpdateSettings {
                    it.copy(
                        customHexColor = chosenHex,
                        themeEngine = DynamicThemeEngine.CUSTOM_HEX,
                        dynamicThemeEnabled = true
                    )
                }
            }
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(settingsBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        val isDualPane = when (settings.dualPaneMode) {
            DualPaneSetting.AUTO -> maxWidth >= 600.dp || hasVerticalHinge
            DualPaneSetting.ALWAYS -> true
            DualPaneSetting.OFF -> false
        }

        PredictiveBackHandler(enabled = true) { progress ->
            try {
                progress.collect { }
                if (showCrashReportsScreen) {
                    showCrashReportsScreen = false
                } else if (!isDualPane && selectedCategory != null) {
                    selectedCategory = null
                } else {
                    onBack()
                }
            } catch (_: CancellationException) { }
        }

        if (isDualPane) {
            val activeDetailCategory = selectedCategory ?: SettingsCategory.THEME_COLORS
            val horizontalOuterPadding = if (maxWidth >= 840.dp) 20.dp else 14.dp
            val availableRowWidth = (maxWidth - horizontalOuterPadding * 2).coerceAtLeast(320.dp)

            val rawHingeLeft = verticalHingeLeftDp?.let { it - horizontalOuterPadding }
            val useFoldAwareSplit = hasVerticalHinge &&
                rawHingeLeft != null &&
                rawHingeLeft >= 200.dp &&
                (availableRowWidth - rawHingeLeft - verticalHingeWidthDp) >= 240.dp

            val leftPaneWidth = when {
                useFoldAwareSplit -> rawHingeLeft!!
                availableRowWidth >= 900.dp -> 340.dp
                availableRowWidth >= 600.dp -> (availableRowWidth * 0.37f).coerceIn(220.dp, 320.dp)
                else -> (availableRowWidth * 0.40f).coerceAtLeast(148.dp)
            }
            val hingeGapWidth = when {
                useFoldAwareSplit -> maxOf(verticalHingeWidthDp, 14.dp)
                availableRowWidth >= 840.dp -> 18.dp
                availableRowWidth >= 600.dp -> 12.dp
                else -> 8.dp
            }
            val isCompactMasterPane = leftPaneWidth < 210.dp

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalOuterPadding, vertical = 14.dp)
                    .testTag("settings_dual_pane_container")
            ) {
                // LEFT PANE: Adaptive Master Category List
                Column(
                    modifier = Modifier
                        .width(leftPaneWidth)
                        .fillMaxHeight()
                        .testTag("settings_master_pane")
                ) {
                    // Master Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OneUITooltipButton(
                            name = "Close Settings",
                            onClick = onBack
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .glassmorphism(shape = CircleShape, backgroundColor = Color(0x25FFFFFF))
                                    .testTag("settings_back_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = settingsBackButtonColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Settings",
                                style = if (isCompactMasterPane) {
                                    MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                } else {
                                    MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                                },
                                color = settingsTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!isCompactMasterPane) {
                                Text(
                                    text = when {
                                        hasVerticalHinge && isFoldableHalfOpened -> "Foldable Book Mode"
                                        hasVerticalHinge -> "Foldable Dual-Pane"
                                        else -> "Adaptive Dual-Pane"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = accentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        OneUITooltipButton(
                            name = "Quick Color Wheel",
                            onClick = { showStandaloneColorWheel = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .glassmorphism(shape = CircleShape, backgroundColor = accentColor.copy(alpha = 0.22f))
                                    .testTag("settings_quick_wheel"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ColorLens,
                                    contentDescription = "Color Wheel",
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(hubScrollState),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SettingsDualPaneMasterList(
                            selectedCategory = activeDetailCategory,
                            accentColor = accentColor,
                            settingsTextColor = settingsTextColor,
                            isCompact = isCompactMasterPane,
                            onSelectCategory = { selectedCategory = it },
                            onOpenColorWheel = { showStandaloneColorWheel = true }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // HINGE / SEPARATOR GAP (aligns cleanly with physical foldable hinge when present)
                Box(
                    modifier = Modifier
                        .width(hingeGapWidth)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    VerticalDivider(
                        thickness = if (isVerticalHingeSeparating) 2.dp else 1.dp,
                        color = if (isVerticalHingeSeparating) accentColor.copy(alpha = 0.32f) else Color(0x1EFFFFFF),
                        modifier = Modifier
                            .fillMaxHeight(0.92f)
                    )
                }

                // RIGHT PANE: Dynamic Detail Studio
                val activeScrollState = detailScrollStates.getValue(activeDetailCategory)
                val statusBadgeText = remember(
                    activeDetailCategory,
                    settings,
                    localTracksCount,
                    totalTracksCount,
                    isCrashLoggingEnabled
                ) {
                    categoryStatusSummary(
                        category = activeDetailCategory,
                        settings = settings,
                        localTracksCount = localTracksCount,
                        totalTracksCount = totalTracksCount,
                        isCrashLoggingEnabled = isCrashLoggingEnabled
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .glassmorphism(
                            shape = OneUISquircleLarge,
                            backgroundColor = Color(0x16171A2D),
                            borderWidth = 1.dp
                        )
                        .testTag("settings_detail_pane")
                ) {
                    // Persistent Detail Context Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.20f))
                                .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = activeDetailCategory.icon,
                                contentDescription = activeDetailCategory.title,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeDetailCategory.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = settingsTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = activeDetailCategory.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = settingsTextColor.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (availableRowWidth >= 660.dp) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(OneUIPillShape)
                                    .background(accentColor.copy(alpha = 0.16f))
                                    .border(1.dp, accentColor.copy(alpha = 0.35f), OneUIPillShape)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("settings_detail_status_pill")
                            ) {
                                Text(
                                    text = statusBadgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = accentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0x16FFFFFF))

                    // Animated Category Detail Content
                    AnimatedContent(
                        targetState = activeDetailCategory,
                        transitionSpec = { NavigationTransitions.tabFadeThrough() },
                        label = "SettingsDetailTransition",
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) { category ->
                        val paneScrollState = detailScrollStates[category] ?: activeScrollState
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(paneScrollState)
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 760.dp)
                            ) {
                                SettingsCategoryContent(
                                    category = category,
                                    settings = settings,
                                    accentColor = accentColor,
                                    settingsHeaderColor = settingsHeaderColor,
                                    previewScrubberProgress = previewScrubberProgress,
                                    onPreviewScrubberProgressChange = { previewScrubberProgress = it },
                                    customHexInput = customHexInput,
                                    onCustomHexInputChange = { customHexInput = it },
                                    onShowStandaloneColorWheel = { showStandaloneColorWheel = true },
                                    onUpdateSettings = onUpdateSettings,
                                    onOpenEqualizer = onOpenEqualizer,
                                    localTracksCount = localTracksCount,
                                    totalTracksCount = totalTracksCount,
                                    onPickLocalFiles = onPickLocalFiles,
                                    onScanDeviceStorage = onScanDeviceStorage,
                                    isCrashLoggingEnabled = isCrashLoggingEnabled,
                                    onToggleCrashLogging = { enabled ->
                                        coroutineScope.launch {
                                            crashPrefs.setCrashLoggingEnabled(enabled)
                                        }
                                    },
                                    onOpenCrashReports = { showCrashReportsScreen = true },
                                    lyricsSyncUiState = lyricsSyncUiState,
                                    onStartBatchLyricsSync = onStartBatchLyricsSync,
                                    onCancelBatchLyricsSync = onCancelBatchLyricsSync,
                                    onRetryFailedLyricsSync = onRetryFailedLyricsSync
                                )
                                Spacer(modifier = Modifier.height(28.dp))
                            }
                        }
                    }
                }
            }
        } else {
            // SINGLE-PANE HANDHELD / COVER SCREEN LAYOUT
            val singleScrollState = if (selectedCategory == null) {
                hubScrollState
            } else {
                detailScrollStates[selectedCategory] ?: hubScrollState
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(singleScrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .testTag("settings_single_pane_container")
            ) {
                // Header with Back Button and Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OneUITooltipButton(
                        name = if (selectedCategory != null) "Back to Categories" else "Navigate Back",
                        onClick = {
                            if (selectedCategory != null) {
                                selectedCategory = null
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .glassmorphism(shape = CircleShape, backgroundColor = Color(0x25FFFFFF))
                                .testTag("settings_back_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = settingsBackButtonColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedCategory?.title ?: "Settings",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = settingsTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = selectedCategory?.subtitle ?: "Customize visuals, player controls, foldables & car",
                            style = MaterialTheme.typography.bodySmall,
                            color = settingsTextColor.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (selectedCategory == null) {
                        OneUITooltipButton(
                            name = "Quick Color Wheel",
                            onClick = { showStandaloneColorWheel = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .glassmorphism(shape = CircleShape, backgroundColor = accentColor.copy(alpha = 0.25f))
                                    .testTag("settings_quick_wheel"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ColorLens,
                                    contentDescription = "Color Wheel",
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                AnimatedContent(
                    targetState = selectedCategory,
                    transitionSpec = {
                        NavigationTransitions.hierarchicalContentTransform(
                            scope = this,
                            isForward = targetState != null
                        )
                    },
                    label = "SettingsSinglePaneHierarchy",
                    modifier = Modifier.fillMaxWidth()
                ) { currentCategory ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (currentCategory == null) {
                            SettingsHubView(
                                accentColor = accentColor,
                                settingsTextColor = settingsTextColor,
                                onSelectCategory = { selectedCategory = it },
                                onOpenColorWheel = { showStandaloneColorWheel = true }
                            )
                        } else {
                            SettingsCategoryContent(
                                category = currentCategory,
                                settings = settings,
                                accentColor = accentColor,
                                settingsHeaderColor = settingsHeaderColor,
                                previewScrubberProgress = previewScrubberProgress,
                                onPreviewScrubberProgressChange = { previewScrubberProgress = it },
                                customHexInput = customHexInput,
                                onCustomHexInputChange = { customHexInput = it },
                                onShowStandaloneColorWheel = { showStandaloneColorWheel = true },
                                onUpdateSettings = onUpdateSettings,
                                onOpenEqualizer = onOpenEqualizer,
                                localTracksCount = localTracksCount,
                                totalTracksCount = totalTracksCount,
                                onPickLocalFiles = onPickLocalFiles,
                                onScanDeviceStorage = onScanDeviceStorage,
                                isCrashLoggingEnabled = isCrashLoggingEnabled,
                                onToggleCrashLogging = { enabled ->
                                    coroutineScope.launch {
                                        crashPrefs.setCrashLoggingEnabled(enabled)
                                    }
                                },
                                onOpenCrashReports = { showCrashReportsScreen = true },
                                lyricsSyncUiState = lyricsSyncUiState,
                                onStartBatchLyricsSync = onStartBatchLyricsSync,
                                onCancelBatchLyricsSync = onCancelBatchLyricsSync,
                                onRetryFailedLyricsSync = onRetryFailedLyricsSync
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

private fun categoryStatusSummary(
    category: SettingsCategory,
    settings: AppSettings,
    localTracksCount: Int,
    totalTracksCount: Int,
    isCrashLoggingEnabled: Boolean
): String {
    return when (category) {
        SettingsCategory.THEME_COLORS -> if (!settings.dynamicThemeEnabled) {
            settings.accentTheme.displayName
        } else {
            when (settings.themeEngine) {
                DynamicThemeEngine.ANDROID_17_MONET -> "Monet Engine"
                DynamicThemeEngine.TRACK_ALBUM_ART -> "Artwork Sync"
                DynamicThemeEngine.CUSTOM_HEX -> settings.customHexColor.uppercase()
                DynamicThemeEngine.INDIVIDUAL_ELEMENTS -> "Custom Elements"
                DynamicThemeEngine.STATIC_ACCENT -> settings.accentTheme.displayName
            }
        }
        SettingsCategory.PLAYER_VISUALIZER -> settings.visualizerStyle.label
        SettingsCategory.LYRICS -> if (settings.autoFetchLyricsOnScan) {
            "Auto-Fetch On"
        } else if (settings.lyricsSyncWifiOnly) {
            "Wi-Fi Only"
        } else {
            "Manual Sync"
        }
        SettingsCategory.TABLETOP_FOLDABLE -> settings.tabletopControlStyle.label
        SettingsCategory.DISPLAY_LAYOUT -> "Dual-Pane: ${settings.dualPaneMode.label}"
        SettingsCategory.AUDIO_ENGINE -> settings.defaultSoundPreset.displayName
        SettingsCategory.DEVICE_AUDIO -> "$localTracksCount Local / $totalTracksCount Total"
        SettingsCategory.CAR_ABOUT -> "Android Auto Ready"
        SettingsCategory.DIAGNOSTICS -> if (isCrashLoggingEnabled) "Interceptor Active" else "Capture Paused"
    }
}

@Composable
private fun SettingsDualPaneMasterList(
    selectedCategory: SettingsCategory,
    accentColor: Color,
    settingsTextColor: Color,
    isCompact: Boolean,
    onSelectCategory: (SettingsCategory) -> Unit,
    onOpenColorWheel: () -> Unit
) {
    // Quick Color Wheel Banner
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphism(
                shape = OneUISquircleLarge,
                backgroundColor = accentColor.copy(alpha = 0.16f),
                borderWidth = 1.dp
            )
            .clickable(onClick = onOpenColorWheel)
            .padding(if (isCompact) 12.dp else 14.dp)
            .testTag("settings_hub_color_wheel_banner")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 34.dp else 40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ColorLens,
                    contentDescription = "HSV Color Wheel",
                    tint = accentColor,
                    modifier = Modifier.size(if (isCompact) 18.dp else 22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCompact) "Color Wheel" else "Interactive Color Wheel",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isCompact) {
                    Text(
                        text = "Pick any HSV spectrum hue",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xBBFFFFFF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    Text(
        text = "Categories",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = accentColor,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 2.dp)
    )

    SettingsCategory.entries.forEach { category ->
        val isSelected = category == selectedCategory
        val cardBg by animateColorAsState(
            targetValue = if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0x20151828),
            animationSpec = tween(durationMillis = 200),
            label = "MasterCategoryBg"
        )
        val borderClr by animateColorAsState(
            targetValue = if (isSelected) accentColor else Color(0x18FFFFFF),
            animationSpec = tween(durationMillis = 200),
            label = "MasterCategoryBorder"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(OneUISquircleMedium)
                .background(cardBg)
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = borderClr,
                    shape = OneUISquircleMedium
                )
                .clickable { onSelectCategory(category) }
                .padding(horizontal = if (isCompact) 10.dp else 14.dp, vertical = 12.dp)
                .testTag("settings_category_${category.tag}")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(if (isCompact) 24.dp else 32.dp)
                            .clip(OneUIPillShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier
                        .size(if (isCompact) 34.dp else 40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.28f) else Color(0x1AFFFFFF)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) accentColor.copy(alpha = 0.65f) else Color(0x24FFFFFF),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.title,
                        tint = if (isSelected) accentColor else settingsTextColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = if (isSelected) Color.White else settingsTextColor,
                        maxLines = if (isCompact) 2 else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!isCompact) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = category.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = settingsTextColor.copy(alpha = if (isSelected) 0.78f else 0.58f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsCategoryContent(
    category: SettingsCategory,
    settings: AppSettings,
    accentColor: Color,
    settingsHeaderColor: Color,
    previewScrubberProgress: Float,
    onPreviewScrubberProgressChange: (Float) -> Unit,
    customHexInput: String,
    onCustomHexInputChange: (String) -> Unit,
    onShowStandaloneColorWheel: () -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onOpenEqualizer: () -> Unit,
    localTracksCount: Int,
    totalTracksCount: Int,
    onPickLocalFiles: () -> Unit,
    onScanDeviceStorage: () -> Unit,
    isCrashLoggingEnabled: Boolean,
    onToggleCrashLogging: (Boolean) -> Unit,
    onOpenCrashReports: () -> Unit,
    lyricsSyncUiState: LyricsSyncUiState,
    onStartBatchLyricsSync: () -> Unit,
    onCancelBatchLyricsSync: () -> Unit,
    onRetryFailedLyricsSync: () -> Unit
) {
    when (category) {
        SettingsCategory.THEME_COLORS -> {

            // ==========================================
            // 1. PERSONALIZATION & THEMES
            // ==========================================
            SettingsSectionHeader(
                title = "Theme & Colors",
                icon = Icons.Default.Palette,
                accentColor = settingsHeaderColor
            )

            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dynamic Expressive Colors",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Harmonize player UI with Android 17 Monet, album art or custom hex",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x99FFFFFF)
                        )
                    }

                    Switch(
                        checked = settings.dynamicThemeEnabled,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(dynamicThemeEnabled = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = Color(0xFFB0B0B0),
                            uncheckedTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.testTag("dynamic_colors_switch")
                    )
                }

                AnimatedVisibility(visible = settings.dynamicThemeEnabled) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Theme Engine Source",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xCCFFFFFF)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Android 17 Monet option
                        val isMonet = settings.themeEngine == DynamicThemeEngine.ANDROID_17_MONET
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(OneUIPillShape)
                                .background(if (isMonet) accentColor.copy(alpha = 0.22f) else Color(0x12FFFFFF))
                                .border(1.dp, if (isMonet) accentColor else Color.Transparent, OneUIPillShape)
                                .clickable {
                                    onUpdateSettings { it.copy(themeEngine = DynamicThemeEngine.ANDROID_17_MONET) }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("theme_engine_monet"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Monet",
                                tint = if (isMonet) accentColor else Color(0x88FFFFFF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Android 17 Monet Engine",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isMonet) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isMonet) Color.White else Color(0xDDFFFFFF)
                                )
                                Text(
                                    text = "Extracts Material You tonal palettes from system wallpaper & theme",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0x88FFFFFF)
                                )
                            }
                            if (isMonet) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Track Album Art Dynamic option
                        val isAlbumArt = settings.themeEngine == DynamicThemeEngine.TRACK_ALBUM_ART
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(OneUIPillShape)
                                .background(if (isAlbumArt) accentColor.copy(alpha = 0.22f) else Color(0x12FFFFFF))
                                .border(1.dp, if (isAlbumArt) accentColor else Color.Transparent, OneUIPillShape)
                                .clickable {
                                    onUpdateSettings { it.copy(themeEngine = DynamicThemeEngine.TRACK_ALBUM_ART) }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("theme_engine_album_art"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Album Art",
                                tint = if (isAlbumArt) accentColor else Color(0x88FFFFFF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Track Artwork Dynamic",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isAlbumArt) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isAlbumArt) Color.White else Color(0xDDFFFFFF)
                                )
                                Text(
                                    text = "Extracts vibrant hues from playing track's embedded artwork",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0x88FFFFFF)
                                )
                            }
                            if (isAlbumArt) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom Hex Code Color option
                        val isCustomHex = settings.themeEngine == DynamicThemeEngine.CUSTOM_HEX
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(OneUIPillShape)
                                .background(if (isCustomHex) accentColor.copy(alpha = 0.22f) else Color(0x12FFFFFF))
                                .border(1.dp, if (isCustomHex) accentColor else Color.Transparent, OneUIPillShape)
                                .clickable {
                                    onUpdateSettings { it.copy(themeEngine = DynamicThemeEngine.CUSTOM_HEX) }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("theme_engine_custom_hex"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Custom Hex",
                                tint = if (isCustomHex) accentColor else Color(0x88FFFFFF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Custom Hex Code Color",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isCustomHex) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isCustomHex) Color.White else Color(0xDDFFFFFF)
                                )
                                Text(
                                    text = "Set your own signature hex color (${settings.customHexColor})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0x88FFFFFF)
                                )
                            }
                            if (isCustomHex) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Custom Hex Picker & Input Subpanel
                        AnimatedVisibility(visible = isCustomHex) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, start = 8.dp, end = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val parsedColor = parseHexColor(customHexInput) ?: accentColor

                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(OneUISquircleSmall)
                                            .background(parsedColor)
                                            .border(1.5.dp, Color.White, OneUISquircleSmall)
                                            .clickable { onShowStandaloneColorWheel() }
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    OutlinedTextField(
                                        value = customHexInput,
                                        onValueChange = { input ->
                                            val sanitized = if (input.startsWith("#")) input else "#$input"
                                            onCustomHexInputChange(sanitized)
                                            val validColor = parseHexColor(sanitized)
                                            if (validColor != null) {
                                                onUpdateSettings {
                                                    it.copy(
                                                        customHexColor = sanitized,
                                                        themeEngine = DynamicThemeEngine.CUSTOM_HEX
                                                    )
                                                }
                                            }
                                        },
                                        label = { Text("Hex Color Code (#RRGGBB)") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("custom_hex_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = accentColor,
                                            unfocusedBorderColor = Color(0x44FFFFFF),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Color Wheel Button
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(OneUISquircleSmall)
                                            .background(Color(0x22FFFFFF))
                                            .clickable { onShowStandaloneColorWheel() }
                                            .testTag("open_color_wheel_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = "Open Color Wheel",
                                            tint = accentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Quick Palette Presets",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0x88FFFFFF)
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                val hexPresets = listOf(
                                    "#0072DE" to "One UI Blue",
                                    "#7C4DFF" to "Electric Violet",
                                    "#00C853" to "Neon Emerald",
                                    "#FF6E6C" to "Sunset Coral",
                                    "#FF2A85" to "Cherry Blossom",
                                    "#00E5FF" to "Cyber Cyan",
                                    "#FFB300" to "Amber Gold"
                                )

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    hexPresets.forEach { (hex, name) ->
                                        val color = parseHexColor(hex) ?: Color.White
                                        val isCurrent = settings.customHexColor.equals(hex, ignoreCase = true)
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                    width = if (isCurrent) 2.5.dp else 1.dp,
                                                    color = if (isCurrent) Color.White else Color(0x33FFFFFF),
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    onCustomHexInputChange(hex)
                                                    onUpdateSettings {
                                                        it.copy(
                                                            customHexColor = hex,
                                                            themeEngine = DynamicThemeEngine.CUSTOM_HEX
                                                        )
                                                    }
                                                }
                                                .testTag("preset_hex_$name"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isCurrent) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = name,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Individual Element Hex Colors option
                        val isIndividualElements = settings.themeEngine == DynamicThemeEngine.INDIVIDUAL_ELEMENTS
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(OneUIPillShape)
                                .background(if (isIndividualElements) accentColor.copy(alpha = 0.22f) else Color(0x12FFFFFF))
                                .border(1.dp, if (isIndividualElements) accentColor else Color.Transparent, OneUIPillShape)
                                .clickable {
                                    onUpdateSettings {
                                        it.copy(
                                            themeEngine = DynamicThemeEngine.INDIVIDUAL_ELEMENTS,
                                            useCustomElementColors = true
                                        )
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("theme_engine_individual_elements"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Individual Elements",
                                tint = if (isIndividualElements) accentColor else Color(0x88FFFFFF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Individual App Element Hex Colors",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isIndividualElements) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isIndividualElements) Color.White else Color(0xDDFFFFFF)
                                )
                                Text(
                                    text = "Customize the hex code color for each individual element in the app",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0x88FFFFFF)
                                )
                            }
                            if (isIndividualElements) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Individual Element Colors Subpanel
                        AnimatedVisibility(visible = isIndividualElements) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, start = 4.dp, end = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Configure Element Hex Colors",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = accentColor
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(OneUIPillShape)
                                            .background(Color(0x22FFFFFF))
                                            .clickable {
                                                onUpdateSettings {
                                                    it.copy(
                                                        elementColors = ElementColors(),
                                                        useCustomElementColors = true
                                                    )
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                            .testTag("reset_all_element_colors"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Reset All",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color.White
                                        )
                                    }
                                }

                                val elements = listOf(
                                    // Expanded & Collapsed App Bar (Samsung One UI Header)
                                    ElementColorItem("Header Expanded Background", "Background color when viewing header is fully expanded", settings.elementColors.headerExpandedBackground, "One UI Header") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(headerExpandedBackground = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Header Collapsed Background", "Top app bar surface color when collapsed on scroll", settings.elementColors.headerCollapsedBackground, "One UI Header") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(headerCollapsedBackground = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Header Title Text", "Large OneMusic or tab title text in top bar", settings.elementColors.headerTitleColor, "One UI Header") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(headerTitleColor = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Header Subtitle & Details", "Track counter, category info, and Bluetooth badge", settings.elementColors.headerSubtitleColor, "One UI Header") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(headerSubtitleColor = hex), useCustomElementColors = true) }
                                    },

                                    // Side Rail (Foldable / Dual-Pane)
                                    ElementColorItem("Side Rail Background", "Navigation rail container surface", settings.elementColors.railBackground, "Side Rail") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(railBackground = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Side Rail Selected Item", "Active rail tab icon and indicator pill", settings.elementColors.railSelectedItem, "Side Rail") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(railSelectedItem = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Side Rail Unselected Items", "Inactive tab icons and text in rail", settings.elementColors.railUnselectedItem, "Side Rail") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(railUnselectedItem = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Side Rail Header Icon", "Top OneMusic logo badge in rail", settings.elementColors.railHeaderIcon, "Side Rail") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(railHeaderIcon = hex), useCustomElementColors = true) }
                                    },

                                    // Settings Menu
                                    ElementColorItem("Settings Background", "Settings screen outer background surface", settings.elementColors.settingsBackground, "Settings Menu") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(settingsBackground = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Settings Card Surface", "Background glassmorphic container for settings cards", settings.elementColors.settingsCardBg, "Settings Menu") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(settingsCardBg = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Settings Category Headers", "Section headers and category icons in settings", settings.elementColors.settingsCategoryHeaderColor, "Settings Menu") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(settingsCategoryHeaderColor = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Settings Text Color", "Primary text and titles inside settings menu", settings.elementColors.settingsTextColor, "Settings Menu") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(settingsTextColor = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Settings Switch Accent", "Toggle switches track and thumb accent in settings", settings.elementColors.settingsSwitchTrack, "Settings Menu") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(settingsSwitchTrack = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Settings Back Button", "Top back navigation arrow in settings", settings.elementColors.settingsBackButtonColor, "Settings Menu") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(settingsBackButtonColor = hex), useCustomElementColors = true) }
                                    },

                                    // Track List
                                    ElementColorItem("Track Card Background", "Surface glass of track item cards in lists", settings.elementColors.trackListCardBg, "Track List") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(trackListCardBg = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Track Title Text", "Song titles in music track lists", settings.elementColors.trackTitleColor, "Track List") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(trackTitleColor = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Track Subtitle Text", "Artist, album, and metadata text in lists", settings.elementColors.trackSubtitleColor, "Track List") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(trackSubtitleColor = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Active Track Highlight", "Playing song title and active indicator color", settings.elementColors.trackActiveHighlight, "Track List") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(trackActiveHighlight = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Track Duration Text", "Duration timestamp and track badges", settings.elementColors.trackDurationColor, "Track List") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(trackDurationColor = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Track Favorite Heart", "Heart icon for favorite songs", settings.elementColors.trackFavoriteColor, "Track List") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(trackFavoriteColor = hex), useCustomElementColors = true) }
                                    },

                                    // Player & Console
                                    ElementColorItem("Player Top Stage Background", "Upper half of Now Playing and tabletop display", settings.elementColors.playerBackgroundTop, "Player & Console") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(playerBackgroundTop = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Player Bottom Shelf Background", "Lower control shelf background surface", settings.elementColors.playerBackgroundBottom, "Player & Console") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(playerBackgroundBottom = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Play / Pause Button", "Primary central playback button in player", settings.elementColors.playerControlPlayBtn, "Player & Console") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(playerControlPlayBtn = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Skip & Seek Buttons", "Previous, next, and seek control buttons", settings.elementColors.playerControlSkipBtn, "Player & Console") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(playerControlSkipBtn = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Playback Timestamps", "Elapsed and total duration time indicators", settings.elementColors.playerTimestampColor, "Player & Console") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(playerTimestampColor = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Volume Slider Bar", "Tabletop and player volume track color", settings.elementColors.playerVolumeTrack, "Player & Console") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(playerVolumeTrack = hex), useCustomElementColors = true) }
                                    },

                                    // Tabletop Flex Mode Elements
                                    ElementColorItem("Tabletop Screen Background", "Overall background color of the tabletop mode screen", settings.elementColors.tabletopBackgroundColor, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopBackgroundColor = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Upper Stage Glow", "Upper half angled viewing background glow", settings.elementColors.tabletopTopHalfBackground, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopTopHalfBackground = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Lower Shelf Glass", "Lower touch control console background surface", settings.elementColors.tabletopBottomHalfBackground, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopBottomHalfBackground = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Reflected Blur Tint", "Inverted album art reflection overlay tint", settings.elementColors.tabletopReflectionTint, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopReflectionTint = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Play Button", "Central hero play/pause button surface", settings.elementColors.tabletopPlayButton, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopPlayButton = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Play Icon", "Icon tint for play and pause in tabletop mode", settings.elementColors.tabletopPlayIcon, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopPlayIcon = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Skip Buttons", "Previous, next, rewind, and forward buttons", settings.elementColors.tabletopSkipButtons, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopSkipButtons = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Secondary Buttons", "Inactive shuffle, repeat, and speed buttons", settings.elementColors.tabletopSecondaryButtons, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopSecondaryButtons = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Active Button Accent", "Active shuffle and repeat state indicator", settings.elementColors.tabletopActiveButtonTint, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopActiveButtonTint = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Song Title", "Title headline in upper stage viewing pane", settings.elementColors.tabletopTrackTitle, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopTrackTitle = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Artist Text", "Artist and album subtitle in upper pane", settings.elementColors.tabletopTrackArtist, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopTrackArtist = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Lyrics Inactive", "Unfocused lyric lines text color", settings.elementColors.tabletopLyricsText, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopLyricsText = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Lyrics Active Highlight", "Current synchronized singing line highlight", settings.elementColors.tabletopLyricsHighlight, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopLyricsHighlight = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Scrubber Active Track", "Filled progress bar track in tabletop console", settings.elementColors.tabletopScrubberActive, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopScrubberActive = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Scrubber Inactive Track", "Unplayed scrubber bar track background", settings.elementColors.tabletopScrubberInactive, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopScrubberInactive = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Volume Track", "Volume indicator bar in tabletop console", settings.elementColors.tabletopVolumeTrack, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopVolumeTrack = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Tabletop Visualizer Spectrum", "Equalizer bars and waveform in tabletop stage", settings.elementColors.tabletopVisualizerColor, "Tabletop Mode") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabletopVisualizerColor = hex), useCustomElementColors = true) }
                                    },

                                    // Audio Visualizer
                                    ElementColorItem("Visualizer Peak Accent", "Top tip of spectrum resonance bars", settings.elementColors.visualizerPeakColor, "Audio Visualizer") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(visualizerPeakColor = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Visualizer Bar Body", "Main body color of audio visualizer bars", settings.elementColors.visualizerBarColor, "Audio Visualizer") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(visualizerBarColor = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Visualizer Bar Base", "Bottom baseline color of spectrum bars", settings.elementColors.visualizerBaseColor, "Audio Visualizer") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(visualizerBaseColor = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Visualizer Ambient Glow", "Soft radial halo behind visualizer spectrum", settings.elementColors.visualizerGlowColor, "Audio Visualizer") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(visualizerGlowColor = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Visualizer Background Glow Halo", "Surrounding atmospheric glow in visualizer", settings.elementColors.visualizerBackgroundGlow, "Audio Visualizer") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(visualizerBackgroundGlow = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Visualizer Waveform Stroke", "Contour line and curve stroke for harmonic waveform", settings.elementColors.visualizerWaveformStroke, "Audio Visualizer") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(visualizerWaveformStroke = hex), useCustomElementColors = true) }
                                    },

                                    // General App Surfaces
                                    ElementColorItem("Main App Background", "Overall screen and window surface", settings.elementColors.background, "General Surfaces") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(background = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Primary Brand Accent", "Active playback buttons and key highlights", settings.elementColors.primary, "General Surfaces") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(primary = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Secondary Accent", "Visualizers, badges, and glow trails", settings.elementColors.secondary, "General Surfaces") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(secondary = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Cards & Surface Glass", "Track cards, sheets, and dialog surfaces", settings.elementColors.cardBackground, "General Surfaces") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(cardBackground = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Scrubber & Playback Wave", "Waveform scrubber and progress indicator", settings.elementColors.scrubber, "General Surfaces") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(scrubber = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Active Tab & Pill Highlight", "Selected navigation tab and pills", settings.elementColors.tabHighlight, "General Surfaces") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(tabHighlight = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Primary Text & Headers", "Song titles and main headlines", settings.elementColors.textPrimary, "General Surfaces") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(textPrimary = hex), useCustomElementColors = true) }
                                    },
                                    ElementColorItem("Secondary Text & Subtitles", "Artist names, durations, and metadata", settings.elementColors.textSecondary, "General Surfaces") { hex ->
                                        onUpdateSettings { it.copy(elementColors = it.elementColors.copy(textSecondary = hex), useCustomElementColors = true) }
                                    }
                                )

                                var lastCategory = ""
                                elements.forEach { item ->
                                    if (item.category.isNotEmpty() && item.category != lastCategory) {
                                        lastCategory = item.category
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = item.category.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                            color = accentColor,
                                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                        )
                                    }
                                    ElementColorField(
                                        label = item.label,
                                        description = item.description,
                                        currentHex = item.currentHex,
                                        accentColor = accentColor,
                                        onHexChange = item.onHexChange
                                    )
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = !settings.dynamicThemeEnabled) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Choose Static Accent Theme",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xCCFFFFFF)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AccentTheme.entries.forEach { theme ->
                                val isSelected = settings.accentTheme == theme
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(theme.primary)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color(0x33FFFFFF),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            onUpdateSettings { it.copy(accentTheme = theme) }
                                        }
                                        .testTag("accent_color_${theme.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Selected: ${settings.accentTheme.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor
                        )
                    }
                }
            }
        }
        SettingsCategory.PLAYER_VISUALIZER -> {
            SettingsSectionHeader(
                title = "Player Controls & Visualizer",
                icon = Icons.Default.Waves,
                accentColor = accentColor
            )

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Expressive Progress Bar Design",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        text = "Customize the playback scrubber with animated expressive wave patterns",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress bar design options
                    ProgressBarDesign.entries.forEach { design ->
                        val isSelected = settings.progressBarDesign == design
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(OneUIPillShape)
                                .background(if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0x10FFFFFF))
                                .border(1.dp, if (isSelected) accentColor else Color.Transparent, OneUIPillShape)
                                .clickable {
                                    onUpdateSettings { it.copy(progressBarDesign = design) }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("progress_bar_${design.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (design) {
                                    ProgressBarDesign.SMOOTH -> Icons.Default.SmartDisplay
                                    ProgressBarDesign.WAVY -> Icons.Default.Waves
                                    ProgressBarDesign.SQUIGGLY -> Icons.Default.MusicNote
                                    ProgressBarDesign.PULSE -> Icons.Default.Equalizer
                                    ProgressBarDesign.GRADIENT_GLOW -> Icons.Default.AutoAwesome
                                    ProgressBarDesign.SEGMENTED -> Icons.Default.ViewAgenda
                                    ProgressBarDesign.MINIMAL_DOT -> Icons.Default.TouchApp
                                    ProgressBarDesign.STEPPED_LADDER -> Icons.Default.Speed
                                },
                                contentDescription = null,
                                tint = if (isSelected) accentColor else Color(0x88FFFFFF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = design.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) Color.White else Color(0xDDFFFFFF)
                                )
                                Text(
                                    text = design.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0x88FFFFFF)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar Thickness selector
                    Text(
                        text = "Progress Bar Thickness",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Increase the height and presence of the playback progress bar",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ProgressBarThickness.entries.size) { idx ->
                            val thickness = ProgressBarThickness.entries[idx]
                            val isSelected = settings.progressBarThickness == thickness
                            Box(
                                modifier = Modifier
                                    .clip(OneUIPillShape)
                                    .background(if (isSelected) accentColor else Color(0x18FFFFFF))
                                    .border(1.dp, if (isSelected) accentColor else Color(0x20FFFFFF), OneUIPillShape)
                                    .clickable {
                                        onUpdateSettings { it.copy(progressBarThickness = thickness) }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("thickness_${thickness.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = thickness.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) Color(0xFF0C0E1A) else Color(0xDDFFFFFF)
                                    )
                                    Text(
                                        text = "${thickness.thicknessDp} dp",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (isSelected) Color(0xCC0C0E1A) else Color(0x88FFFFFF)
                                    )
                                }
                            }
                        }
                    }

                    // Interactive Live Scrubber Preview
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Live Scrubber Preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ExpressiveScrubber(
                        progress = previewScrubberProgress,
                        onProgressChange = onPreviewScrubberProgressChange,
                        onProgressChangeFinished = {},
                        design = settings.progressBarDesign,
                        thickness = settings.progressBarThickness,
                        accentColor = accentColor,
                        isPlaying = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Player Control Buttons Size
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Player Control Buttons Size",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        text = "Adjust standard player controls for ergonomic thumb comfort",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlayerButtonSize.entries.forEach { size ->
                            val isSelected = settings.playerButtonSize == size
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(OneUIPillShape)
                                    .background(if (isSelected) accentColor else Color(0x18FFFFFF))
                                    .clickable {
                                        onUpdateSettings { it.copy(playerButtonSize = size) }
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("player_button_size_${size.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = size.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color(0xFF0C0E1A) else Color(0xDDFFFFFF)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Playback Controls Animation Effect
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Playback Controls Animation Effect",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        text = "Fun interactive micro-interactions and tactile physics when pressing buttons across all layouts",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ButtonAnimationEffect.entries.forEach { effect ->
                            val isSelected = settings.buttonAnimationEffect == effect
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(OneUIPillShape)
                                    .background(if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0x12FFFFFF))
                                    .border(1.dp, if (isSelected) accentColor else Color.Transparent, OneUIPillShape)
                                    .clickable {
                                        onUpdateSettings { it.copy(buttonAnimationEffect = effect) }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .testTag("button_effect_${effect.name.lowercase()}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (effect) {
                                        ButtonAnimationEffect.BOUNCY_SPRING -> Icons.Default.AutoAwesome
                                        ButtonAnimationEffect.ROTATION_TILT -> Icons.Default.Tune
                                        ButtonAnimationEffect.NEON_GLOW_POP -> Icons.Default.Whatshot
                                        ButtonAnimationEffect.JELLY_SQUISH -> Icons.Default.WaterDrop
                                        ButtonAnimationEffect.SUBTLE_HAPTIC -> Icons.Default.TouchApp
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) accentColor else Color(0x88FFFFFF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = effect.label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) Color.White else Color(0xDDFFFFFF)
                                    )
                                    Text(
                                        text = effect.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0x88FFFFFF)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Visualizer Style Selection (Synced across Tabletop, Dual Pane & Cover Screen)
                VisualizerStyleSelector(
                    settings = settings,
                    accentColor = accentColor,
                    onUpdateSettings = onUpdateSettings
                )
            }
        }
        SettingsCategory.LYRICS -> {
            SettingsSectionHeader(
                title = "Synced Lyrics Animations",
                icon = Icons.Default.AutoAwesome,
                accentColor = accentColor
            )

            SettingsCard {
                LyricsAnimationSettingsSection(
                    settings = settings,
                    accentColor = accentColor,
                    onUpdateSettings = onUpdateSettings
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            SettingsSectionHeader(
                title = "Lyrics & LRCLIB Sync",
                icon = Icons.Default.Lyrics,
                accentColor = accentColor
            )

            SettingsCard {
                // Batch Lyrics Fetcher & Offline Cache (WorkManager + LRCLIB)
                BatchLyricsSyncSection(
                    settings = settings,
                    accentColor = accentColor,
                    lyricsSyncUiState = lyricsSyncUiState,
                    onUpdateSettings = onUpdateSettings,
                    onStartBatchLyricsSync = onStartBatchLyricsSync,
                    onCancelBatchLyricsSync = onCancelBatchLyricsSync,
                    onRetryFailedLyricsSync = onRetryFailedLyricsSync
                )
            }
        }
        SettingsCategory.TABLETOP_FOLDABLE -> {
            SettingsSectionHeader(
                title = "Tabletop & Foldable Mode",
                icon = Icons.Default.StayCurrentLandscape,
                accentColor = accentColor
            )

            SettingsCard {
                // Tabletop Console Style: One UI Tactile vs Netflix Duo
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tabletop Console Style",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        text = "Switch between One UI tactile ergonomics and Netflix Duo dual-screen console style",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TabletopControlStyle.entries.forEach { style ->
                        val isSelected = settings.tabletopControlStyle == style
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(OneUIPillShape)
                                .background(if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0x10FFFFFF))
                                .border(1.dp, if (isSelected) accentColor else Color.Transparent, OneUIPillShape)
                                .clickable {
                                    onUpdateSettings { it.copy(tabletopControlStyle = style) }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("tabletop_style_${style.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) accentColor else Color(0x66FFFFFF),
                                        shape = CircleShape
                                    )
                                    .background(if (isSelected) accentColor else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = style.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) Color.White else Color(0xDDFFFFFF)
                                )
                                Text(
                                    text = style.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0x88FFFFFF)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Tabletop Top Screen Layout (Split Art & Lyrics vs Standard)
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tabletop Top Screen Layout",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        text = "Choose between standard centered album art or split art and synchronized lyrics",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TabletopTopLayout.entries.forEach { layout ->
                        val isSelected = settings.tabletopTopLayout == layout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(OneUIPillShape)
                                .background(if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0x10FFFFFF))
                                .border(1.dp, if (isSelected) accentColor else Color.Transparent, OneUIPillShape)
                                .clickable {
                                    onUpdateSettings { it.copy(tabletopTopLayout = layout) }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("tabletop_layout_${layout.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (layout) {
                                    TabletopTopLayout.STANDARD -> Icons.Default.Album
                                    TabletopTopLayout.SPLIT_ART_LYRICS -> Icons.Default.ViewAgenda
                                },
                                contentDescription = null,
                                tint = if (isSelected) accentColor else Color(0x88FFFFFF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = layout.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) Color.White else Color(0xDDFFFFFF)
                                )
                                Text(
                                    text = layout.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0x88FFFFFF)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                VisualizerStyleSelector(
                    settings = settings,
                    accentColor = accentColor,
                    onUpdateSettings = onUpdateSettings
                )

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Tabletop Reflected Background Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reflected Album Background",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Display a blurred transparent reflection of the playing song on the bottom half of tabletop mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x99FFFFFF)
                        )
                    }

                    Switch(
                        checked = settings.tabletopReflectedBackgroundEnabled,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(tabletopReflectedBackgroundEnabled = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor
                        )
                    )
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Tabletop Volume Slider Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show Tabletop Volume Slider",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Toggle the dedicated hardware volume slider bar on the tabletop shelf",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x99FFFFFF)
                        )
                    }

                    Switch(
                        checked = settings.tabletopShowVolumeBar,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(tabletopShowVolumeBar = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor
                        )
                    )
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tabletop Controls Scale",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            Text(
                                text = "Scale controls from 70% up to 500% to fill large foldable screens",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0x99FFFFFF)
                            )
                        }
                        Text(
                            text = "${(settings.tabletopControlsScale * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = settings.tabletopControlsScale,
                        onValueChange = { newScale ->
                            onUpdateSettings { it.copy(tabletopControlsScale = newScale) }
                        },
                        valueRange = 0.70f..5.00f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = Color(0x33FFFFFF)
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Scale Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1.0f to "100%", 2.0f to "200%", 3.0f to "300%", 4.0f to "400%", 5.0f to "500%").forEach { (scaleVal, label) ->
                            val isCurrent = (settings.tabletopControlsScale * 10).toInt() == (scaleVal * 10).toInt()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(OneUIPillShape)
                                    .background(if (isCurrent) accentColor else Color(0x18FFFFFF))
                                    .clickable {
                                        onUpdateSettings { it.copy(tabletopControlsScale = scaleVal) }
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isCurrent) Color(0xFF0C0E1A) else Color(0xDDFFFFFF)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Tabletop Album Art Size in Tabletop Mode (Multi-row Grid)
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tabletop Album Art Size",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        text = "Increase the artwork scale displayed on the upper stage in tabletop mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 1: Medium (180dp), Large (220dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(TabletopAlbumArtSize.MEDIUM, TabletopAlbumArtSize.LARGE).forEach { artSize ->
                            val isSelected = settings.tabletopAlbumArtSize == artSize
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(OneUIPillShape)
                                    .background(if (isSelected) accentColor else Color(0x18FFFFFF))
                                    .clickable {
                                        onUpdateSettings { it.copy(tabletopAlbumArtSize = artSize) }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                                    .testTag("tabletop_art_size_${artSize.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = artSize.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) Color(0xFF0C0E1A) else Color(0xDDFFFFFF),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${artSize.sizeDp} dp",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (isSelected) Color(0xCC0C0E1A) else Color(0x88FFFFFF)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Extra Large (260dp), Jumbo (300dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(TabletopAlbumArtSize.EXTRA_LARGE, TabletopAlbumArtSize.JUMBO).forEach { artSize ->
                            val isSelected = settings.tabletopAlbumArtSize == artSize
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(OneUIPillShape)
                                    .background(if (isSelected) accentColor else Color(0x18FFFFFF))
                                    .clickable {
                                        onUpdateSettings { it.copy(tabletopAlbumArtSize = artSize) }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                                    .testTag("tabletop_art_size_${artSize.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = artSize.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) Color(0xFF0C0E1A) else Color(0xDDFFFFFF),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${artSize.sizeDp} dp",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (isSelected) Color(0xCC0C0E1A) else Color(0x88FFFFFF)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 3: Massive (360dp), Colossal (420dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(TabletopAlbumArtSize.MASSIVE, TabletopAlbumArtSize.COLOSSAL).forEach { artSize ->
                            val isSelected = settings.tabletopAlbumArtSize == artSize
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(OneUIPillShape)
                                    .background(if (isSelected) accentColor else Color(0x18FFFFFF))
                                    .clickable {
                                        onUpdateSettings { it.copy(tabletopAlbumArtSize = artSize) }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                                    .testTag("tabletop_art_size_${artSize.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = artSize.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) Color(0xFF0C0E1A) else Color(0xDDFFFFFF),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${artSize.sizeDp} dp",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (isSelected) Color(0xCC0C0E1A) else Color(0x88FFFFFF)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 4: Maximum (500dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val artSize = TabletopAlbumArtSize.MAXIMUM
                        val isSelected = settings.tabletopAlbumArtSize == artSize
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(OneUIPillShape)
                                .background(if (isSelected) accentColor else Color(0x18FFFFFF))
                                .clickable {
                                    onUpdateSettings { it.copy(tabletopAlbumArtSize = artSize) }
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                .testTag("tabletop_art_size_${artSize.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = artSize.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color(0xFF0C0E1A) else Color(0xDDFFFFFF),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${artSize.sizeDp} dp",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = if (isSelected) Color(0xCC0C0E1A) else Color(0x88FFFFFF)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Tabletop Immersive View (Removes Native Status Bar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onUpdateSettings { it.copy(tabletopImmersiveMode = !it.tabletopImmersiveMode) }
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tabletop Immersive View",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Remove native status bar for a clean, distraction-free edge-to-edge viewing screen in tabletop mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x99FFFFFF)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = settings.tabletopImmersiveMode,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(tabletopImmersiveMode = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = Color(0x88FFFFFF),
                            uncheckedTrackColor = Color(0x22FFFFFF)
                        ),
                        modifier = Modifier.testTag("tabletop_immersive_toggle")
                    )
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Tabletop Mode Player Control Buttons Size
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tabletop Mode Control Buttons Size",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        text = "Small, Medium, Large, or Larger with auto-arranging layout to fill empty shelf space",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TabletopButtonSize.entries.forEach { size ->
                        val isSelected = settings.tabletopButtonSize == size
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(OneUIPillShape)
                                .background(if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0x10FFFFFF))
                                .border(1.dp, if (isSelected) accentColor else Color.Transparent, OneUIPillShape)
                                .clickable {
                                    onUpdateSettings { it.copy(tabletopButtonSize = size) }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("tabletop_button_size_${size.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) accentColor else Color(0x66FFFFFF),
                                        shape = CircleShape
                                    )
                                    .background(if (isSelected) accentColor else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = size.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) Color.White else Color(0xDDFFFFFF)
                                )
                                Text(
                                    text = size.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0x88FFFFFF)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0x1AFFFFFF))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Custom Movable & Resizable Controls status & reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Movable & Resizable Buttons",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = if (settings.tabletopControlPositions.isCustomLayoutEnabled) {
                                    "Custom placement active (${(settings.tabletopControlPositions.customScale * 100).toInt()}% scale). Touch and hold buttons in tabletop to adjust."
                                } else {
                                    "Touch and hold any button in tabletop mode to enter edit mode and drag to move or resize."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0x99FFFFFF)
                            )
                        }

                        if (settings.tabletopControlPositions.isCustomLayoutEnabled) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(OneUIPillShape)
                                    .background(Color(0x25FFFFFF))
                                    .clickable {
                                        onUpdateSettings { it.copy(tabletopControlPositions = TabletopControlPositions()) }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("reset_tabletop_positions_settings"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Reset Layout",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
        SettingsCategory.DISPLAY_LAYOUT -> {
            SettingsSectionHeader(
                title = "Display & Adaptive Layouts",
                icon = Icons.Default.StayCurrentPortrait,
                accentColor = accentColor
            )

            SettingsCard {
                // Dual Pane Layout
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dual-Pane Layout (Foldables & Tablets)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        text = "Side-by-side library and Now Playing player",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DualPaneSetting.entries.forEach { option ->
                        val isSelected = settings.dualPaneMode == option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(OneUIPillShape)
                                .background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable {
                                    onUpdateSettings { it.copy(dualPaneMode = option) }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("dual_pane_${option.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) accentColor else Color(0x66FFFFFF),
                                        shape = CircleShape
                                    )
                                    .background(if (isSelected) accentColor else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) Color.White else Color(0xDDFFFFFF)
                                )
                                Text(
                                    text = option.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0x88FFFFFF)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Visualizer Album Art Display Size (controls visualizer / Now Playing screen)
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Visualizer Album Art Display Size",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        text = "Scale of the album cover art in the Now Playing visualizer",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary sizes: Small, Medium, Large
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(AlbumArtSize.SMALL, AlbumArtSize.MEDIUM, AlbumArtSize.LARGE).forEach { artSize ->
                            val isSelected = settings.visualizerAlbumArtSize == artSize
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(OneUIPillShape)
                                    .background(if (isSelected) accentColor else Color(0x18FFFFFF))
                                    .clickable {
                                        onUpdateSettings { it.copy(visualizerAlbumArtSize = artSize, albumArtSize = artSize) }
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("visualizer_album_art_size_${artSize.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = artSize.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color(0xFF0C0E1A) else Color(0xDDFFFFFF)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Larger sizes: Extra Large, Jumbo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(AlbumArtSize.EXTRA_LARGE, AlbumArtSize.JUMBO).forEach { artSize ->
                            val isSelected = settings.visualizerAlbumArtSize == artSize
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(OneUIPillShape)
                                    .background(if (isSelected) accentColor else Color(0x18FFFFFF))
                                    .clickable {
                                        onUpdateSettings { it.copy(visualizerAlbumArtSize = artSize, albumArtSize = artSize) }
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("visualizer_album_art_size_${artSize.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = artSize.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color(0xFF0C0E1A) else Color(0xDDFFFFFF)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Track List Album Art Size (controls track items in lists)
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Track List Album Art Size",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        text = "Thumbnail scale for track items in music lists",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary sizes: Small, Medium, Large
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(AlbumArtSize.SMALL, AlbumArtSize.MEDIUM, AlbumArtSize.LARGE).forEach { artSize ->
                            val isSelected = settings.trackListAlbumArtSize == artSize
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(OneUIPillShape)
                                    .background(if (isSelected) accentColor else Color(0x18FFFFFF))
                                    .clickable {
                                        onUpdateSettings { it.copy(trackListAlbumArtSize = artSize) }
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("tracklist_album_art_size_${artSize.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${artSize.label} (${artSize.sizeDp}dp)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color(0xFF0C0E1A) else Color(0xDDFFFFFF)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Larger sizes: Extra Large, Jumbo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(AlbumArtSize.EXTRA_LARGE, AlbumArtSize.JUMBO).forEach { artSize ->
                            val isSelected = settings.trackListAlbumArtSize == artSize
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(OneUIPillShape)
                                    .background(if (isSelected) accentColor else Color(0x18FFFFFF))
                                    .clickable {
                                        onUpdateSettings { it.copy(trackListAlbumArtSize = artSize) }
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("tracklist_album_art_size_${artSize.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${artSize.label} (${artSize.sizeDp}dp)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color(0xFF0C0E1A) else Color(0xDDFFFFFF)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Album Layout Selection
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Album & Playlist Presentation",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        text = "Visual grid density for album collection",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AlbumLayoutSetting.entries.forEach { layout ->
                            val isSelected = settings.albumLayout == layout
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(OneUISquircleMedium)
                                    .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color(0x18FFFFFF))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) accentColor else Color(0x22FFFFFF),
                                        shape = OneUISquircleMedium
                                    )
                                    .clickable {
                                        onUpdateSettings { it.copy(albumLayout = layout) }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                                    .testTag("album_layout_${layout.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = when (layout) {
                                            AlbumLayoutSetting.GRID_2 -> Icons.Default.GridView
                                            AlbumLayoutSetting.GRID_3 -> Icons.Default.ViewAgenda
                                            AlbumLayoutSetting.LIST -> Icons.Default.ViewList
                                        },
                                        contentDescription = layout.label,
                                        tint = if (isSelected) accentColor else Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = layout.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) Color.White else Color(0xAAFFFFFF)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Startup Display Tab
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Startup Display Tab",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        text = "Default landing tab when OneMusic launches",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MusicTab.entries.forEach { tab ->
                            val isSelected = settings.startupTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(OneUIPillShape)
                                    .background(if (isSelected) accentColor else Color(0x1EFFFFFF))
                                    .clickable {
                                        onUpdateSettings { it.copy(startupTab = tab) }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                                    .testTag("startup_tab_${tab.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color(0xFF0C0E1A) else Color(0xCCFFFFFF),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Auto Halfway Open Flex Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.StayCurrentLandscape,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Auto Halfway Open Flex Mode",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Detects folding angle (75°-115°) on unfolded landscape display to auto-trigger Tabletop console (deactivated on cover screen and portrait)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x99FFFFFF)
                        )
                    }

                    Switch(
                        checked = settings.autoFlexMode,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(autoFlexMode = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = Color(0xFFB0B0B0),
                            uncheckedTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.testTag("auto_flex_mode_switch")
                    )
                }
            }
        }
        SettingsCategory.DEVICE_AUDIO -> {
            SettingsSectionHeader(
                title = "Local Device Audio",
                icon = Icons.Default.FolderOpen,
                accentColor = accentColor
            )

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Local Storage",
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Device Audio Library",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "$localTracksCount local files loaded • $totalTracksCount total library tracks",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xAAFFFFFF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Scanned tracks and imported audio files are automatically cached and remembered across app restarts so you never have to rescan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xCCFFFFFF),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Open File Picker Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(OneUIPillShape)
                                .background(accentColor)
                                .clickable(onClick = onPickLocalFiles)
                                .padding(vertical = 12.dp)
                                .testTag("settings_import_audio_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = "Open Files",
                                    tint = Color(0xFF0C0E1A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Import Audio",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF0C0E1A)
                                )
                            }
                        }

                        // Scan Device Storage Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .glassmorphism(
                                    shape = OneUIPillShape,
                                    backgroundColor = Color(0x2BFFFFFF),
                                    borderWidth = 1.dp
                                )
                                .clickable(onClick = onScanDeviceStorage)
                                .padding(vertical = 12.dp)
                                .testTag("settings_scan_storage_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Scan Storage",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Scan Storage",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
        SettingsCategory.AUDIO_ENGINE -> {
            SettingsSectionHeader(
                title = "Audio & Playback",
                icon = Icons.Default.Tune,
                accentColor = accentColor
            )

            SettingsCard {
                // SoundAlive EQ link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenEqualizer)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "SoundAlive Equalizer & Spatial",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Dolby Atmos 3D audio, Bass/Treble tuning",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x99FFFFFF)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(OneUIPillShape)
                            .background(accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Configure",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = accentColor
                        )
                    }
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Keep screen on
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Keep Screen On During Playback",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Maintains album art & visualizer awake on desk stands",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x99FFFFFF)
                        )
                    }

                    Switch(
                        checked = settings.keepScreenOn,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(keepScreenOn = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = Color(0xFFB0B0B0),
                            uncheckedTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.testTag("keep_screen_on_switch")
                    )
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // Gapless Playback
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gapless Audio Transition",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Zero latency switching between consecutive songs",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x99FFFFFF)
                        )
                    }

                    Switch(
                        checked = settings.gaplessPlayback,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(gaplessPlayback = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = Color(0xFFB0B0B0),
                            uncheckedTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.testTag("gapless_playback_switch")
                    )
                }

                HorizontalDivider(color = Color(0x1AFFFFFF))

                // High fidelity visualizer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "High-Fidelity Visualizer",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Smooth 60fps real-time frequency analysis",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x99FFFFFF)
                        )
                    }

                    Switch(
                        checked = settings.highFidelityVisualizer,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(highFidelityVisualizer = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = Color(0xFFB0B0B0),
                            uncheckedTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.testTag("high_fi_visualizer_switch")
                    )
                }
            }
        }
        SettingsCategory.CAR_ABOUT -> {
            SettingsSectionHeader(
                title = "Live Notifications & Lock Screen",
                icon = Icons.Default.NotificationsActive,
                accentColor = accentColor
            )

            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Live Notification Preview Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(OneUISquircleMedium)
                            .background(Color(0x25191C30))
                            .border(1.dp, accentColor.copy(alpha = 0.35f), OneUISquircleMedium)
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Live Playback Notification",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = accentColor
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(OneUIPillShape)
                                        .background(if (settings.liveNotificationsEnabled) accentColor.copy(alpha = 0.2f) else Color(0x22FFFFFF))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (settings.liveNotificationsEnabled) "ACTIVE" else "DISABLED",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (settings.liveNotificationsEnabled) accentColor else Color(0x88FFFFFF)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(OneUISquircleSmall)
                                        .background(accentColor.copy(alpha = 0.25f))
                                        .border(1.dp, Color(0x33FFFFFF), OneUISquircleSmall),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Album,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Current Playing Track",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Artist Name • Album Title",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0x99FFFFFF),
                                        maxLines = 1
                                    )
                                    if (settings.liveNotificationSoundBadge) {
                                        Text(
                                            text = "Live • High-Fidelity 320kbps",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = accentColor
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(accentColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null, tint = Color(0xFF0F101E), modifier = Modifier.size(20.dp))
                                }
                                Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                if (settings.liveNotificationShowFavorite) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF4081), modifier = Modifier.size(20.dp))
                                }
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0x88FFFFFF), modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Enable Live Notifications Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live Notifications",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            Text(
                                text = "Interactive real-time playback notifications in shade, lock screen, and Now Bar",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0x99FFFFFF)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = settings.liveNotificationsEnabled,
                            onCheckedChange = { isChecked ->
                                onUpdateSettings { it.copy(liveNotificationsEnabled = isChecked) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accentColor,
                                uncheckedThumbColor = Color(0xFFB0B0B0),
                                uncheckedTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.testTag("live_notifications_switch")
                        )
                    }

                    HorizontalDivider(
                        color = Color(0x1AFFFFFF),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // 2. Favorite Quick Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Favorite Quick Action",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            Text(
                                text = "Show 1-tap heart button in the notification to favorite songs",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0x99FFFFFF)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = settings.liveNotificationShowFavorite,
                            onCheckedChange = { isChecked ->
                                onUpdateSettings { it.copy(liveNotificationShowFavorite = isChecked) }
                            },
                            enabled = settings.liveNotificationsEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accentColor,
                                uncheckedThumbColor = Color(0xFFB0B0B0),
                                uncheckedTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.testTag("live_notification_favorite_switch")
                        )
                    }

                    HorizontalDivider(
                        color = Color(0x1AFFFFFF),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // 3. Live Sound & Bluetooth Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live Sound & Bluetooth Status",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            Text(
                                text = "Display audio fidelity, Dolby Atmos, and connected Bluetooth device subtext",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0x99FFFFFF)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = settings.liveNotificationSoundBadge,
                            onCheckedChange = { isChecked ->
                                onUpdateSettings { it.copy(liveNotificationSoundBadge = isChecked) }
                            },
                            enabled = settings.liveNotificationsEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accentColor,
                                uncheckedThumbColor = Color(0xFFB0B0B0),
                                uncheckedTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.testTag("live_notification_badge_switch")
                        )
                    }

                    HorizontalDivider(
                        color = Color(0x1AFFFFFF),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // 4. Samsung One UI Lock Screen & AOD Live Updates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lock Screen & AOD Live Updates",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            Text(
                                text = "Stream live media sessions to Samsung lock screen Now Bar & Always On Display",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0x99FFFFFF)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = settings.liveNotificationLockScreenAOD,
                            onCheckedChange = { isChecked ->
                                onUpdateSettings { it.copy(liveNotificationLockScreenAOD = isChecked) }
                            },
                            enabled = settings.liveNotificationsEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accentColor,
                                uncheckedThumbColor = Color(0xFFB0B0B0),
                                uncheckedTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.testTag("live_notification_lockscreen_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 6. ANDROID AUTO & VEHICLE INTEGRATION
            // ==========================================
            SettingsSectionHeader(
                title = "Android Auto & Connected Car",
                icon = Icons.Default.DirectionsCar,
                accentColor = accentColor
            )

            SettingsCard {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0x3000E5FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "Android Auto",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Android Auto",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(OneUIPillShape)
                                        .background(Color(0xFF00C853).copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Active & Ready",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF69F0AE)
                                    )
                                }
                            }
                            Text(
                                text = "Service: OneMusicAutoService",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0x99FFFFFF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "OneMusic is configured for car head unit dashboards. When connected via USB or wireless Android Auto:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xCCFFFFFF),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val autoFeatures = listOf(
                        "In-car touch display library browsing (Tracks, Albums, Playlists)",
                        "Steering wheel media keys (Play, Pause, Skip, Seek)",
                        "Google Assistant voice search integration",
                        "High contrast night mode optimized for driver safety",
                        "Direct playback sync with phone player"
                    )

                    autoFeatures.forEach { feature ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feature,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xB3FFFFFF)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 6.5 CRASH DIAGNOSTICS & LOGGING
            // ==========================================
            SettingsSectionHeader(
                title = "Crash Diagnostics & Logs",
                icon = Icons.Default.BugReport,
                accentColor = accentColor
            )

            SettingsCard {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Save Crash Logs & Diagnostics",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            Text(
                                text = "Automatically capture exception stack traces, device model, posture, and player state on unexpected crashes",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0x99FFFFFF)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = isCrashLoggingEnabled,
                            onCheckedChange = onToggleCrashLogging,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accentColor,
                                uncheckedThumbColor = Color(0xFF888888),
                                uncheckedTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.testTag("toggle_save_crash_logs_car_about")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0x1AFFFFFF))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(OneUIPillShape)
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(1.dp, accentColor.copy(alpha = 0.35f), OneUIPillShape)
                            .clickable { onOpenCrashReports() }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("btn_view_crash_reports_car_about"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "View Saved Crash Reports",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 7. ABOUT
            // ==========================================
            SettingsSectionHeader(
                title = "About",
                icon = Icons.Default.Info,
                accentColor = accentColor
            )

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "OneMusic • Expressive Studio",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Built with Galaxy-style glass aesthetic, Material You dynamic color blending, foldable tabletop mode, and Android Auto connected car services.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF),
                        lineHeight = 18.sp
                    )
                }
            }
        }
        SettingsCategory.DIAGNOSTICS -> {
            DiagnosticsSettingsCategoryContent(
                accentColor = accentColor,
                isCrashLoggingEnabled = isCrashLoggingEnabled,
                onToggleCrashLogging = onToggleCrashLogging,
                onOpenCrashReports = onOpenCrashReports
            )
        }
    }
}

@Composable
private fun VisualizerStyleSelector(
    settings: AppSettings,
    accentColor: Color,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Visualizer Style (Tabletop, Dual-Pane & Cover Screen)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
        Text(
            text = "Choose from 19 reactive visualizers including Mandala, DNA Helix, Fireflies, Supernova, XY Oscilloscope, and Retro Cassette",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0x99FFFFFF)
        )

        Spacer(modifier = Modifier.height(12.dp))

        VisualizerStyle.entries.forEach { style ->
            val isSelected = settings.visualizerStyle == style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(OneUIPillShape)
                    .background(if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0x10FFFFFF))
                    .border(1.dp, if (isSelected) accentColor else Color.Transparent, OneUIPillShape)
                    .clickable {
                        onUpdateSettings { it.copy(visualizerStyle = style) }
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("visualizer_style_${style.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (style) {
                        VisualizerStyle.BARS -> Icons.Default.Equalizer
                        VisualizerStyle.LINES -> Icons.Default.Waves
                        VisualizerStyle.WAVEFORM -> Icons.Default.AutoAwesome
                        VisualizerStyle.MIRRORED_BARS -> Icons.Default.Equalizer
                        VisualizerStyle.CIRCULAR_DOTS -> Icons.Default.TouchApp
                        VisualizerStyle.DIGITAL_FLAMES -> Icons.Default.Whatshot
                        VisualizerStyle.LIQUID_WAVES -> Icons.Default.WaterDrop
                        VisualizerStyle.NEON_AURORA -> Icons.Default.Palette
                        VisualizerStyle.RADIAL_ORBIT -> Icons.Default.TouchApp
                        VisualizerStyle.PARTICLE_RAIN -> Icons.Default.WaterDrop
                        VisualizerStyle.RETRO_GRID -> Icons.Default.GridView
                        VisualizerStyle.SOUND_BLOX -> Icons.Default.Equalizer
                        VisualizerStyle.RECORD_PLAYING -> Icons.Default.Album
                        VisualizerStyle.KALEIDOSCOPE_MANDALA -> Icons.Default.AutoAwesome
                        VisualizerStyle.DNA_DOUBLE_HELIX -> Icons.Default.Waves
                        VisualizerStyle.FIREFLY_SWARM -> Icons.Default.Whatshot
                        VisualizerStyle.GALAXY_SUPERNOVA -> Icons.Default.ColorLens
                        VisualizerStyle.OSCILLOSCOPE_LISSAJOUS -> Icons.Default.Tune
                        VisualizerStyle.CASSETTE_TAPE -> Icons.Default.MusicNote
                    },
                    contentDescription = null,
                    tint = if (isSelected) accentColor else Color(0x88FFFFFF),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = style.label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) Color.White else Color(0xDDFFFFFF)
                    )
                    Text(
                        text = style.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x88FFFFFF)
                    )
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun LyricsAnimationSettingsSection(
    settings: AppSettings,
    accentColor: Color,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    val activeStyle = settings.lyricsAnimationStyle
    val activeSpeed = settings.lyricsAnimationSpeed
    val glowEnabled = settings.lyricsGlowEnabled

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Lyrics Line Animation Style",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
        Text(
            text = "Customize how active synced lyrics transition across Cover Screen, Dual-Pane & Tabletop mode",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0x99FFFFFF)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Style Options
        LyricsAnimationStyle.entries.forEach { style ->
            val isSelected = activeStyle == style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(OneUIPillShape)
                    .background(if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0x10FFFFFF))
                    .border(1.dp, if (isSelected) accentColor else Color.Transparent, OneUIPillShape)
                    .clickable {
                        onUpdateSettings { it.copy(lyricsAnimationStyle = style) }
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("lyrics_anim_style_${style.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (style) {
                        LyricsAnimationStyle.SMOOTH_FLOAT -> Icons.Default.Waves
                        LyricsAnimationStyle.KARAOKE_SWEEP -> Icons.Default.MusicNote
                        LyricsAnimationStyle.ELASTIC_POP -> Icons.Default.AutoAwesome
                        LyricsAnimationStyle.CINEMATIC_FOCUS -> Icons.Default.SmartDisplay
                        LyricsAnimationStyle.WAVE_FLOW -> Icons.Default.WaterDrop
                        LyricsAnimationStyle.TYPEWRITER_REVEAL -> Icons.Default.Lyrics
                    },
                    contentDescription = null,
                    tint = if (isSelected) accentColor else Color(0x88FFFFFF),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = style.label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) Color.White else Color(0xDDFFFFFF)
                    )
                    Text(
                        text = style.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x88FFFFFF)
                    )
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color(0x1AFFFFFF))
        Spacer(modifier = Modifier.height(14.dp))

        // Transition Speed Selector
        Text(
            text = "Transition Speed",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
        Text(
            text = "Adjust how quickly synced lyrics highlight and scroll between lines",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0x99FFFFFF)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LyricsAnimationSpeed.entries.forEach { speed ->
                val isSelected = activeSpeed == speed
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(OneUIPillShape)
                        .background(if (isSelected) accentColor.copy(alpha = 0.28f) else Color(0x14FFFFFF))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) accentColor else Color(0x22FFFFFF),
                            shape = OneUIPillShape
                        )
                        .clickable {
                            onUpdateSettings { it.copy(lyricsAnimationSpeed = speed) }
                        }
                        .padding(vertical = 10.dp)
                        .testTag("lyrics_anim_speed_${speed.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = speed.label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else Color(0xBBFFFFFF),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = Color(0x1AFFFFFF))
        Spacer(modifier = Modifier.height(12.dp))

        // Glow Halo Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(OneUISquircleSmall)
                .clickable {
                    onUpdateSettings { it.copy(lyricsGlowEnabled = !glowEnabled) }
                }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Active Lyric Glow Halo",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Illuminate the active lyric line with a soft album-themed pill backdrop",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0x99FFFFFF)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = glowEnabled,
                onCheckedChange = { checked ->
                    onUpdateSettings { it.copy(lyricsGlowEnabled = checked) }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = Color(0xFF888888),
                    uncheckedTrackColor = Color(0x33FFFFFF)
                ),
                modifier = Modifier.testTag("lyrics_anim_glow_switch")
            )
        }
    }
}

@Composable
private fun SettingsHubView(
    accentColor: Color,
    settingsTextColor: Color,
    onSelectCategory: (SettingsCategory) -> Unit,
    onOpenColorWheel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Action Interactive Color Wheel Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphism(
                    shape = OneUISquircleLarge,
                    backgroundColor = accentColor.copy(alpha = 0.16f),
                    borderWidth = 1.2.dp
                )
                .clickable(onClick = onOpenColorWheel)
                .padding(18.dp)
                .testTag("settings_hub_color_wheel_banner")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = "HSV Color Wheel",
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Interactive Color Wheel",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Touch or drag anywhere on the HSV spectrum wheel to create custom hues",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xCCFFFFFF),
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(OneUIPillShape)
                        .background(accentColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Open Wheel",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0C0E1A)
                    )
                }
            }
        }

        Text(
            text = "Categories",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = accentColor,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
        )

        SettingsCategory.entries.forEach { category ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphism(
                        shape = OneUISquircleLarge,
                        backgroundColor = Color(0x20151828),
                        borderWidth = 1.dp
                    )
                    .clickable { onSelectCategory(category) }
                    .padding(16.dp)
                    .testTag("settings_category_${category.tag}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.18f))
                            .border(1.dp, accentColor.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.title,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = settingsTextColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = category.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = settingsTextColor.copy(alpha = 0.65f),
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open ${category.title}",
                        tint = Color(0x66FFFFFF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = accentColor,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphism(
                shape = OneUISquircleLarge,
                backgroundColor = Color(0x28191C30),
                borderWidth = 1.dp
            ),
        shape = OneUISquircleLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        content()
    }
}

private data class ElementColorItem(
    val label: String,
    val description: String,
    val currentHex: String,
    val category: String = "",
    val onHexChange: (String) -> Unit
)

@Composable
private fun ElementColorField(
    label: String,
    description: String,
    currentHex: String,
    accentColor: Color,
    onHexChange: (String) -> Unit
) {
    var textInput by remember(currentHex) { mutableStateOf(currentHex) }
    var showColorWheel by remember { mutableStateOf(false) }
    val parsedColor = parseHexColor(textInput) ?: parseHexColor(currentHex) ?: Color.White

    if (showColorWheel) {
        ColorWheelDialog(
            initialHex = currentHex,
            onDismiss = { showColorWheel = false },
            onColorSelected = { chosenHex ->
                textInput = chosenHex
                onHexChange(chosenHex)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OneUISquircleMedium)
            .background(Color(0x16FFFFFF))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(parsedColor)
                    .border(1.5.dp, Color.White, CircleShape)
                    .clickable { showColorWheel = true }
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0x88FFFFFF)
                )
            }

            // Interactive Color Wheel Trigger Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF))
                    .clickable { showColorWheel = true }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Color Wheel for $label",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textInput,
            onValueChange = { input ->
                val formatted = if (input.startsWith("#")) input else "#$input"
                textInput = formatted
                if (parseHexColor(formatted) != null) {
                    onHexChange(formatted)
                }
            },
            label = { Text("Hex Code (#RRGGBB)", fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("element_hex_${label.lowercase().replace(" ", "_")}"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color(0x33FFFFFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        val quickSwatches = listOf(
            "#0072DE", "#2979FF", "#7C4DFF", "#00E5FF",
            "#00C853", "#FF6E6C", "#FF2A85", "#FFFFFF", "#0F101E"
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickSwatches.forEach { swatchHex ->
                val swatchColor = parseHexColor(swatchHex) ?: Color.Gray
                val isSelected = textInput.equals(swatchHex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(swatchColor)
                        .border(
                            width = if (isSelected) 2.dp else 0.8.dp,
                            color = if (isSelected) Color.White else Color(0x44FFFFFF),
                            shape = CircleShape
                        )
                        .clickable {
                            textInput = swatchHex
                            onHexChange(swatchHex)
                        }
                )
            }
        }
    }
}

@Composable
fun ColorWheelDialog(
    initialHex: String,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    var currentHex by remember(initialHex) { mutableStateOf(if (initialHex.startsWith("#")) initialHex else "#$initialHex") }
    val initialColor = remember(initialHex) { parseHexColor(initialHex) ?: Color(0xFF0072DE) }

    val initialHsv = remember(initialColor) {
        val argb = android.graphics.Color.argb(
            (initialColor.alpha * 255).toInt(),
            (initialColor.red * 255).toInt(),
            (initialColor.green * 255).toInt(),
            (initialColor.blue * 255).toInt()
        )
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(argb, hsv)
        hsv
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var brightness by remember { mutableFloatStateOf(initialHsv[2].coerceAtLeast(0.1f)) }

    fun updateHexFromHsv(h: Float, s: Float, v: Float) {
        val hsv = floatArrayOf(h, s, v)
        val argb = android.graphics.Color.HSVToColor(hsv)
        currentHex = String.format("#%06X", 0xFFFFFF and argb)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(OneUISquircleLarge)
                .glassmorphism(
                    shape = OneUISquircleLarge,
                    backgroundColor = Color(0xFC131525),
                    borderWidth = 1.dp
                )
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "HSV Chromatic Color Wheel",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Drag across the wheel to choose hue & saturation",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0x99FFFFFF)
                )

                Spacer(modifier = Modifier.height(16.dp))

                val wheelSizeDp = 200.dp
                Box(
                    modifier = Modifier.size(wheelSizeDp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(wheelSizeDp)
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val radius = size.width / 2f
                                    val dx = change.position.x - center.x
                                    val dy = change.position.y - center.y
                                    val dist = kotlin.math.hypot(dx, dy).coerceAtMost(radius)
                                    var angleDeg = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    if (angleDeg < 0) angleDeg += 360f
                                    hue = angleDeg
                                    saturation = (dist / radius).coerceIn(0f, 1f)
                                    updateHexFromHsv(hue, saturation, brightness)
                                }
                            }
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val radius = size.width / 2f
                                    val dx = offset.x - center.x
                                    val dy = offset.y - center.y
                                    val dist = kotlin.math.hypot(dx, dy).coerceAtMost(radius)
                                    var angleDeg = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    if (angleDeg < 0) angleDeg += 360f
                                    hue = angleDeg
                                    saturation = (dist / radius).coerceIn(0f, 1f)
                                    updateHexFromHsv(hue, saturation, brightness)
                                }
                            }
                    ) {
                        val radius = size.width / 2f
                        val sweepColors = listOf(
                            Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                        )
                        drawCircle(
                            brush = Brush.sweepGradient(sweepColors, center = center),
                            radius = radius,
                            center = center
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = brightness.coerceIn(0f, 1f)), Color.Transparent),
                                center = center,
                                radius = radius
                            ),
                            radius = radius,
                            center = center
                        )
                        if (brightness < 1f) {
                            drawCircle(
                                color = Color.Black.copy(alpha = (1f - brightness) * 0.9f),
                                radius = radius,
                                center = center
                            )
                        }

                        // Thumb ring marker
                        val angleRad = Math.toRadians(hue.toDouble())
                        val dist = saturation * radius
                        val thumbX = center.x + (dist * kotlin.math.cos(angleRad)).toFloat()
                        val thumbY = center.y + (dist * kotlin.math.sin(angleRad)).toFloat()
                        val thumbCenter = Offset(thumbX, thumbY)
                        val currentColor = parseHexColor(currentHex) ?: Color.White

                        drawCircle(
                            color = Color.White,
                            radius = 11.dp.toPx(),
                            center = thumbCenter,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawCircle(
                            color = currentColor,
                            radius = 9.dp.toPx(),
                            center = thumbCenter
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Brightness Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lightness",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xCCFFFFFF),
                        modifier = Modifier.width(60.dp)
                    )
                    Slider(
                        value = brightness,
                        onValueChange = {
                            brightness = it
                            updateHexFromHsv(hue, saturation, brightness)
                        },
                        valueRange = 0.05f..1.0f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = parseHexColor(currentHex) ?: Color.White,
                            inactiveTrackColor = Color(0x33FFFFFF)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Preview & Hex code
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(currentHex) ?: Color.White)
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = currentHex,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(OneUIPillShape)
                                .background(Color(0x22FFFFFF))
                                .clickable { onDismiss() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("Cancel", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                        Box(
                            modifier = Modifier
                                .clip(OneUIPillShape)
                                .background(parseHexColor(currentHex) ?: Color(0xFF0072DE))
                                .clickable {
                                    onColorSelected(currentHex)
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Apply", color = Color.White, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticsSettingsCategoryContent(
    accentColor: Color,
    isCrashLoggingEnabled: Boolean,
    onToggleCrashLogging: (Boolean) -> Unit,
    onOpenCrashReports: () -> Unit
) {
    val context = LocalContext.current
    var savedReportsCount by remember(context) {
        mutableStateOf(CrashReportManager.getSavedCrashReports(context).size)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        SettingsSectionHeader(
            title = "Diagnostics & Crash Capture",
            icon = Icons.Default.BugReport,
            accentColor = accentColor
        )

        SettingsCard {
            Column(modifier = Modifier.padding(18.dp)) {
                // 1. Save Crash Logs & Diagnostics Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Save Crash Logs & Diagnostics",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Persisted using Jetpack DataStore Preferences. Automatically intercept uncaught exceptions and save system diagnostics before process exit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x99FFFFFF)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = isCrashLoggingEnabled,
                        onCheckedChange = onToggleCrashLogging,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = Color(0xFF888888),
                            uncheckedTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.testTag("settings_toggle_save_crash_logs")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0x1AFFFFFF))
                Spacer(modifier = Modifier.height(14.dp))

                // 2. View Saved Crash Reports Button / Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(OneUIPillShape)
                        .background(accentColor.copy(alpha = 0.16f))
                        .border(1.2.dp, accentColor.copy(alpha = 0.45f), OneUIPillShape)
                        .clickable { onOpenCrashReports() }
                        .padding(horizontal = 16.dp, vertical = 13.dp)
                        .testTag("settings_btn_view_crash_reports"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "View Saved Crash Reports",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Inspect stack traces, export/share via system sheet & manage files",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xAAFFFFFF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(OneUIPillShape)
                                .background(accentColor)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$savedReportsCount ${if (savedReportsCount == 1) "File" else "Files"}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0C0E1A)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open reports",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0x1AFFFFFF))
                Spacer(modifier = Modifier.height(14.dp))

                // 3. Generate Simulated Report for testing
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(OneUIPillShape)
                        .background(Color(0x1AFFFFFF))
                        .clickable {
                            CrashReportManager.generateTestReport(context)
                            savedReportsCount = CrashReportManager.getSavedCrashReports(context).size
                            android.widget.Toast.makeText(
                                context,
                                "Test diagnostic crash report generated!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        .padding(horizontal = 16.dp, vertical = 11.dp)
                        .testTag("settings_btn_simulate_crash_report"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Simulate Diagnostic Crash Report",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Create Test Log",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Device Diagnostics & Posture Metadata
        SettingsSectionHeader(
            title = "Current Runtime Telemetry",
            icon = Icons.Default.Info,
            accentColor = accentColor
        )

        SettingsCard {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "System Diagnostics Captured on Error",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))

                val telemetryItems = listOf(
                    "Device Model" to "${android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${android.os.Build.MODEL} (${android.os.Build.DEVICE})",
                    "Android Version" to "Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})",
                    "Screen / Posture" to CrashReportManager.currentPostureState,
                    "Crash Log Directory" to "${context.filesDir.name}/crash_reports/",
                    "FileProvider Authority" to "${context.packageName}.fileprovider",
                    "Interceptor Status" to if (isCrashLoggingEnabled) "Active (Thread.UncaughtExceptionHandler)" else "Disabled by user preference"
                )

                telemetryItems.forEachIndexed { index, (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x99FFFFFF)
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (label == "Interceptor Status") {
                                if (isCrashLoggingEnabled) accentColor else Color(0xFFFF5252)
                            } else Color.White
                        )
                    }
                    if (index < telemetryItems.lastIndex) {
                        HorizontalDivider(color = Color(0x0EFFFFFF), modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}


