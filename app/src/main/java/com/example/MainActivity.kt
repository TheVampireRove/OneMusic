package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.audio.BluetoothAudioHelper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import android.widget.Toast
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.lerp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AlbumArtSize
import com.example.model.ElementColors
import com.example.model.AlbumLayoutSetting
import com.example.model.DualPaneSetting
import com.example.model.DynamicThemeEngine
import com.example.model.FilterType
import com.example.model.MusicTab
import com.example.model.Playlist
import com.example.model.SortOption
import com.example.model.Track
import com.example.ui.components.AlbumDetailScreen
import com.example.ui.components.AlphabetIndexer
import com.example.ui.components.ArtistDetailScreen
import com.example.ui.components.CoverArtView
import com.example.ui.components.CoverLandscapeLayout
import com.example.ui.components.EqualizerSheet
import com.example.ui.components.ExpandedDualPaneScreen
import com.example.ui.components.FlexModePlayer
import com.example.ui.components.FoldableAutoFlexTracker
import com.example.ui.components.LocalOneUITooltipHandler
import com.example.ui.components.MiniPlayer
import com.example.ui.components.NowPlayingScreen
import com.example.ui.components.OneUITooltipBanner
import com.example.ui.components.OneUITooltipButton
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.TrackListItem
import com.example.ui.components.formatTime
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.LocalAnimatedVisibilityScope
import com.example.ui.theme.LocalSharedTransitionScope
import com.example.ui.theme.MotionSpecs
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavigationTransitions
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.OneUISquircleLarge
import com.example.ui.theme.OneUISquircleMedium
import com.example.ui.theme.appSharedBounds
import com.example.ui.theme.bouncableClick
import com.example.ui.theme.generatePaletteFromHex
import com.example.ui.theme.getAndroid17MonetPalette
import com.example.ui.theme.glassmorphism
import com.example.ui.theme.parseHexColor
import com.example.ui.theme.rememberAnimatedExpressivePalette
import com.example.data.MusicRepository
import com.example.viewmodel.ImportSummaryBanner
import com.example.viewmodel.MusicPlayerViewModel
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestHighestRefreshRate()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                OneUIMusicApp()
            }
        }
    }

    /**
     * Hints the Android window composer to prefer the display's highest available refresh rate
     * (e.g., 120Hz LTPO) at the current physical resolution for silky-smooth animations.
     */
    private fun requestHighestRefreshRate() {
        try {
            val activeDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            } ?: return
            val currentMode = activeDisplay.mode
            val bestMode = activeDisplay.supportedModes
                .filter {
                    it.physicalWidth == currentMode.physicalWidth &&
                        it.physicalHeight == currentMode.physicalHeight
                }
                .maxByOrNull { it.refreshRate }
                ?: activeDisplay.supportedModes.maxByOrNull { it.refreshRate }
                ?: return

            val params = window.attributes
            params.preferredDisplayModeId = bestMode.modeId
            params.preferredRefreshRate = bestMode.refreshRate
            window.attributes = params
        } catch (_: Exception) {
            // Safe fallback on virtual / headless displays
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onPause() {
        super.onPause()
        com.example.audio.AudioPlaybackManager.getInstance().persistPlaybackState()
    }

    override fun onStop() {
        super.onStop()
        com.example.audio.AudioPlaybackManager.getInstance().persistPlaybackState()
    }
}

private enum class AppLayoutMode {
    TABLETOP,
    DUAL_PANE,
    COVER_LANDSCAPE,
    COVER
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun OneUIMusicApp(viewModel: MusicPlayerViewModel = viewModel()) {
    // Samsung One UI reachability scroll behavior:
    // LargeTopAppBar starts at expanded height (~270dp) keeping controls in thumb reach,
    // then collapses smoothly to standard 64dp as the user scrolls up.
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState()
    )
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val visualizerAmplitudes by viewModel.visualizerAmplitudes.collectAsStateWithLifecycle()
    val tracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val allTracksList by viewModel.tracks.collectAsStateWithLifecycle()
    val playbackQueue by viewModel.playbackQueue.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedAlbumForDetail by viewModel.selectedAlbumId.collectAsStateWithLifecycle()
    val selectedArtistForDetail by viewModel.selectedArtistId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val isShuffleActive by viewModel.isShuffleActive.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val speed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val isEqualizerOpen by viewModel.isEqualizerOpen.collectAsStateWithLifecycle()
    val isSleepTimerOpen by viewModel.isSleepTimerOpen.collectAsStateWithLifecycle()
    val sleepTimerMinutesLeft by viewModel.sleepTimerMinutesLeft.collectAsStateWithLifecycle()
    val soundPreset by viewModel.soundPreset.collectAsStateWithLifecycle()
    val bassGain by viewModel.bassGain.collectAsStateWithLifecycle()
    val midGain by viewModel.midGain.collectAsStateWithLifecycle()
    val trebleGain by viewModel.trebleGain.collectAsStateWithLifecycle()
    val dolbyAtmosEnabled by viewModel.dolbyAtmosEnabled.collectAsStateWithLifecycle()
    val isFlexModeActive by viewModel.isFlexModeActive.collectAsStateWithLifecycle()
    val displayMode by viewModel.displayMode.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val isLoadingLyrics by viewModel.isLoadingLyrics.collectAsStateWithLifecycle()
    val lyricsSyncUiState by viewModel.lyricsSyncUiState.collectAsStateWithLifecycle()
    val recentImportBanner by viewModel.recentImportBanner.collectAsStateWithLifecycle()

    LaunchedEffect(recentImportBanner) {
        if (recentImportBanner != null) {
            delay(9000L)
            viewModel.dismissImportBanner()
        }
    }

    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Saveable backup for active detail screen IDs across process restoration as well as ViewModel for rotation/folding
    var savedAlbumIdBackup by rememberSaveable { mutableStateOf<String?>(null) }
    var savedArtistIdBackup by rememberSaveable { mutableStateOf<String?>(null) }
    var restoredDetailBackup by remember { mutableStateOf(false) }

    LaunchedEffect(selectedAlbumForDetail, selectedArtistForDetail) {
        if (!restoredDetailBackup) {
            restoredDetailBackup = true
            if (selectedAlbumForDetail == null && savedAlbumIdBackup != null) {
                viewModel.selectAlbumForDetail(savedAlbumIdBackup)
            } else {
                savedAlbumIdBackup = selectedAlbumForDetail
            }
            if (selectedArtistForDetail == null && savedArtistIdBackup != null) {
                viewModel.selectArtistForDetail(savedArtistIdBackup)
            } else {
                savedArtistIdBackup = selectedArtistForDetail
            }
        } else {
            savedAlbumIdBackup = selectedAlbumForDetail
            savedArtistIdBackup = selectedArtistForDetail
        }
    }

    // Hoisted persistent scroll states per MusicTab & detail screens so scroll positions survive tab switches, foldable postures, and orientation changes
    val tracksListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState(
            firstVisibleItemIndex = viewModel.trackListIndex,
            firstVisibleItemScrollOffset = viewModel.trackListOffset
        )
    }

    LaunchedEffect(tracksListState) {
        androidx.compose.runtime.snapshotFlow {
            tracksListState.firstVisibleItemIndex to tracksListState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            viewModel.updateTrackListScroll(index, offset)
        }
    }
    val playlistsListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val albumsListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val albumsGridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() }
    val artistsGridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() }
    val artistsListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val favoritesListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val albumDetailListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val artistDetailListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val artistDetailAlbumsRowState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    val tabListStates = remember(
        tracksListState,
        playlistsListState,
        albumsListState,
        artistsListState,
        favoritesListState
    ) {
        mapOf(
            MusicTab.TRACKS to tracksListState,
            MusicTab.PLAYLISTS to playlistsListState,
            MusicTab.ALBUMS to albumsListState,
            MusicTab.ARTISTS to artistsListState,
            MusicTab.FAVORITES to favoritesListState
        )
    }

    val onPlayFromContext: (List<Track>, Int) -> Unit = { queueList, startIndex ->
        viewModel.playFromContext(queueList, startIndex)
    }
    val onShuffleFromContext: (List<Track>) -> Unit = { queueList ->
        viewModel.playFromContextShuffled(queueList)
    }

    // Samsung One UI & Android 17 Navigation Gestures Back Handler:
    // When on the root view, the back swipe gesture instantly and cleanly terminates the task and exits the app.
    val isRootView = !isPlayerExpanded && !isSettingsOpen && !isEqualizerOpen && !isSleepTimerOpen && !isFlexModeActive && selectedAlbumForDetail == null && selectedArtistForDetail == null
    PredictiveBackHandler(enabled = isRootView) { progress ->
        try {
            progress.collect { }
            activity?.finishAffinity()
            activity?.finish()
        } catch (_: CancellationException) {}
    }

    val isBluetoothConnected by BluetoothAudioHelper.isBluetoothConnected.collectAsStateWithLifecycle()
    val connectedDeviceName by BluetoothAudioHelper.connectedDeviceName.collectAsStateWithLifecycle()
    var isViewingAreaExpanded by remember { mutableStateOf(true) }

    val (monetPrimary, monetSecondary, monetAccent) = remember(context) {
        getAndroid17MonetPalette(context, darkTheme = true)
    }

    val (targetPrimary, targetSecondary, targetAccent) = when {
        !settings.dynamicThemeEnabled -> Triple(settings.accentTheme.primary, settings.accentTheme.secondary, settings.accentTheme.accent)
        settings.themeEngine == DynamicThemeEngine.ANDROID_17_MONET -> Triple(monetPrimary, monetSecondary, monetAccent)
        settings.themeEngine == DynamicThemeEngine.TRACK_ALBUM_ART -> Triple(currentTrack.primaryColor, currentTrack.secondaryColor, currentTrack.accentColor)
        settings.themeEngine == DynamicThemeEngine.CUSTOM_HEX -> {
            val parsedColor = parseHexColor(settings.customHexColor) ?: monetPrimary
            generatePaletteFromHex(parsedColor)
        }
        settings.themeEngine == DynamicThemeEngine.INDIVIDUAL_ELEMENTS || settings.useCustomElementColors -> {
            val pri = parseHexColor(settings.elementColors.primary) ?: monetPrimary
            val sec = parseHexColor(settings.elementColors.secondary) ?: monetSecondary
            val acc = parseHexColor(settings.elementColors.scrubber) ?: monetAccent
            Triple(pri, sec, acc)
        }
        else -> Triple(monetPrimary, monetSecondary, monetAccent)
    }

    val palette = rememberAnimatedExpressivePalette(
        primaryTarget = targetPrimary,
        secondaryTarget = targetSecondary,
        accentTarget = targetAccent
    )

    var currentTooltipHUD by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val showTooltipHUD: (String) -> Unit = { name ->
        currentTooltipHUD = name
        coroutineScope.launch {
            delay(1800)
            if (currentTooltipHUD == name) {
                currentTooltipHUD = null
            }
        }
    }

    // Automatic Tabletop / Flex mode detection when foldable is halfway open
    // (Strictly deactivated on cover screen and in portrait orientation)
    FoldableAutoFlexTracker(
        enabled = settings.autoFlexMode,
        onFlexModeChange = { isHalfOpen ->
            viewModel.setFlexMode(isHalfOpen)
        }
    )

    DisposableEffect(settings.keepScreenOn, isPlaying) {
        try {
            if (settings.keepScreenOn && isPlaying) {
                activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        } catch (_: Exception) {}
        onDispose {
            try {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } catch (_: Exception) {}
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.refreshVisualizerPermission()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            viewModel.refreshVisualizerPermission()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.ensureActivePlayback(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initContext(context)
        BluetoothAudioHelper.init(context)
        val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsNotificationPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            viewModel.refreshVisualizerPermission()
        }
        val intent = activity?.intent
        if (intent?.getBooleanExtra("OPEN_NOW_PLAYING", false) == true) {
            viewModel.expandPlayer()
        } else if (intent?.action == android.provider.MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH) {
            val query = intent.getStringExtra(android.app.SearchManager.QUERY)?.trim().orEmpty()
            val all = viewModel.tracks.value
            val matchIndex = if (query.isNotEmpty()) {
                all.indexOfFirst {
                    it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true) ||
                        it.album.contains(query, ignoreCase = true)
                }
            } else {
                0
            }
            if (all.isNotEmpty()) {
                viewModel.playFromContext(all, if (matchIndex >= 0) matchIndex else 0)
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importLocalFiles(context, uris)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanDeviceMusic(context)
        }
    }

    val onScanStorage: () -> Unit = {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.scanDeviceMusic(context)
        } else {
            permissionLauncher.launch(permission)
        }
    }

    val onPickFiles: () -> Unit = {
        filePickerLauncher.launch(arrayOf("audio/*"))
    }

    var isSearchExpanded by remember { mutableStateOf(false) }
    var isSortDialogOpen by remember { mutableStateOf(false) }

    val appBgColor = if (settings.themeEngine == DynamicThemeEngine.INDIVIDUAL_ELEMENTS || settings.useCustomElementColors) {
        parseHexColor(settings.elementColors.background) ?: Color(0xFF0F101E)
    } else {
        Color(0xFF0F101E)
    }

    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalOneUITooltipHandler provides showTooltipHUD,
            LocalSharedTransitionScope provides this@SharedTransitionLayout
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(appBgColor)
            ) {
            val configuration = LocalConfiguration.current
            val isInnerDisplay = configuration.smallestScreenWidthDp >= 580
            val isLandscape = maxWidth > maxHeight

            val isExpanded = when (settings.dualPaneMode) {
                DualPaneSetting.AUTO -> isInnerDisplay && maxWidth >= 720.dp
                DualPaneSetting.ALWAYS -> true
                DualPaneSetting.OFF -> false
            }

            // Ambient backdrop gradient responding to track
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                palette.ambientGlow,
                                palette.secondary.copy(alpha = 0.15f),
                                appBgColor
                            ),
                            radius = 1100f
                        )
                    )
            )

            val layoutMode = when {
                isFlexModeActive -> AppLayoutMode.TABLETOP
                isExpanded -> AppLayoutMode.DUAL_PANE
                isLandscape -> AppLayoutMode.COVER_LANDSCAPE
                else -> AppLayoutMode.COVER
            }

            AnimatedContent(
                targetState = layoutMode,
                transitionSpec = { NavigationTransitions.postureFade() },
                modifier = Modifier.fillMaxSize(),
                label = "app_layout_posture"
            ) { currentLayoutMode ->
                when (currentLayoutMode) {
                    AppLayoutMode.TABLETOP -> {
                        // Foldable Tabletop Flex Mode UI
                        FlexModePlayer(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    visualizerAmplitudes = visualizerAmplitudes,
                    isShuffleActive = isShuffleActive,
                    repeatMode = repeatMode,
                    volume = volume,
                    speed = speed,
                    sleepTimerMinutesLeft = sleepTimerMinutesLeft,
                    dolbyAtmosEnabled = dolbyAtmosEnabled,
                    onCollapse = { viewModel.setFlexMode(false) },
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.nextTrack() },
                    onPrevious = { viewModel.previousTrack() },
                    onSeek = { viewModel.seekTo(it) },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onCycleRepeat = { viewModel.cycleRepeatMode() },
                    onToggleFavorite = { viewModel.toggleFavorite(currentTrack.id) },
                    onVolumeChange = { viewModel.setVolume(it) },
                    onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                    onOpenEqualizer = { viewModel.openEqualizer() },
                    onOpenSleepTimer = { viewModel.openSleepTimer() },
                    onExitFlexMode = { viewModel.setFlexMode(false) },
                    progressBarDesign = settings.progressBarDesign,
                    progressBarThickness = settings.progressBarThickness,
                    tabletopButtonSize = settings.tabletopButtonSize,
                    tabletopControlStyle = settings.tabletopControlStyle,
                    tabletopAlbumArtSize = settings.tabletopAlbumArtSize,
                    tabletopControlsScale = settings.tabletopControlsScale,
                    tabletopControlPositions = settings.tabletopControlPositions,
                    onUpdateControlPositions = { newPositions ->
                        viewModel.updateSettings { it.copy(tabletopControlPositions = newPositions) }
                    },
                    tabletopImmersiveMode = settings.tabletopImmersiveMode,
                    tabletopTopLayout = settings.tabletopTopLayout,
                    visualizerStyle = settings.visualizerStyle,
                    onVisualizerStyleChange = { newStyle ->
                        viewModel.updateSettings { it.copy(visualizerStyle = newStyle) }
                    },
                    tabletopReflectedBackgroundEnabled = settings.tabletopReflectedBackgroundEnabled,
                    tabletopShowVolumeBar = settings.tabletopShowVolumeBar,
                    allTracks = playbackQueue,
                    onTrackClick = { viewModel.playTrack(it) },
                    onPlayFromContext = onPlayFromContext,
                    elementColors = settings.elementColors,
                    useCustomElementColors = settings.useCustomElementColors || settings.themeEngine == DynamicThemeEngine.INDIVIDUAL_ELEMENTS,
                    buttonAnimationEffect = settings.buttonAnimationEffect,
                    lyricsAnimationStyle = settings.lyricsAnimationStyle,
                    lyricsAnimationSpeed = settings.lyricsAnimationSpeed,
                    lyricsGlowEnabled = settings.lyricsGlowEnabled,
                    isLoadingLyrics = isLoadingLyrics,
                    displayMode = displayMode,
                    onDisplayModeChange = { viewModel.setDisplayMode(it) },
                    enableAnimatedCovers = settings.enableAnimatedCovers,
                    autoFetchAnimatedCoversWifiOnly = settings.autoFetchAnimatedCoversWifiOnly,
                    simulateMotionOnStaticCovers = settings.simulateMotionOnStaticCovers,
                    onImportAnimatedCover = { uri -> viewModel.importAnimatedCover(context, currentTrack, uri) },
                    onRemoveCustomAnimatedCover = { viewModel.removeCustomAnimatedCover(context, currentTrack) },
                    onImportLyricsUri = { uri -> viewModel.importLyricsFromUri(context, currentTrack, uri) },
                    onSavePastedLyrics = { text -> viewModel.savePastedLyrics(context, currentTrack, text) },
                    loadEditableLyrics = { viewModel.getEditableLyricsText(context, currentTrack) },
                    onRefetchLyrics = { viewModel.refetchLyricsOnline(context, currentTrack) },
                    onClearLyrics = { viewModel.clearTrackLyrics(context, currentTrack) }
                )
                    }
                    AppLayoutMode.DUAL_PANE -> {
                        // Foldable Unfolded Inner Screen / Tablet Dual-Pane Mode
                        ExpandedDualPaneScreen(
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    visualizerAmplitudes = visualizerAmplitudes,
                    tracks = tracks,
                    allTracks = allTracksList,
                    favoriteTracks = favoriteTracks,
                    playlists = playlists,
                    selectedTab = selectedTab,
                    searchQuery = searchQuery,
                    selectedAlbumId = selectedAlbumForDetail,
                    selectedArtistId = selectedArtistForDetail,
                    isShuffleActive = isShuffleActive,
                    repeatMode = repeatMode,
                    volume = volume,
                    speed = speed,
                    sleepTimerMinutesLeft = sleepTimerMinutesLeft,
                    dolbyAtmosEnabled = dolbyAtmosEnabled,
                    palette = palette,
                    onTabSelected = { viewModel.selectTab(it) },
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onSelectAlbumForDetail = { viewModel.selectAlbumForDetail(it) },
                    onSelectArtistForDetail = { viewModel.selectArtistForDetail(it) },
                    onTrackClick = { viewModel.playTrack(it) },
                    onPlayFromContext = onPlayFromContext,
                    onShuffleFromContext = onShuffleFromContext,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.nextTrack() },
                    onPrevious = { viewModel.previousTrack() },
                    onSeek = { viewModel.seekTo(it) },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onCycleRepeat = { viewModel.cycleRepeatMode() },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onVolumeChange = { viewModel.setVolume(it) },
                    onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                    onOpenEqualizer = { viewModel.openEqualizer() },
                    onOpenSleepTimer = { viewModel.openSleepTimer() },
                    onEnterFlexMode = { viewModel.setFlexMode(true) },
                    onOpenSettings = { viewModel.openSettings() },
                    albumLayoutSetting = settings.albumLayout,
                    albumArtSize = settings.trackListAlbumArtSize,
                    visualizerAlbumArtSize = settings.visualizerAlbumArtSize,
                    visualizerStyle = settings.visualizerStyle,
                    onVisualizerStyleChange = { newStyle ->
                        viewModel.updateSettings { it.copy(visualizerStyle = newStyle) }
                    },
                    progressBarDesign = settings.progressBarDesign,
                    progressBarThickness = settings.progressBarThickness,
                    playerButtonSize = settings.playerButtonSize,
                    activeFilter = activeFilter,
                    sortOption = sortOption,
                    onFilterSelected = { viewModel.setActiveFilter(it) },
                    onOpenSortDialog = { isSortDialogOpen = true },
                    onBackPress = {
                        activity?.finishAffinity()
                        activity?.finish()
                    },
                    tracksListState = tracksListState,
                    tabListStates = tabListStates,
                    albumsGridState = albumsGridState,
                    artistsGridState = artistsGridState,
                    albumDetailListState = albumDetailListState,
                    artistDetailListState = artistDetailListState,
                    artistDetailAlbumsRowState = artistDetailAlbumsRowState,
                    buttonAnimationEffect = settings.buttonAnimationEffect,
                    lyricsAnimationStyle = settings.lyricsAnimationStyle,
                    lyricsAnimationSpeed = settings.lyricsAnimationSpeed,
                    lyricsGlowEnabled = settings.lyricsGlowEnabled,
                    elementColors = settings.elementColors,
                    onPickLocalFiles = onPickFiles,
                    onScanDeviceStorage = onScanStorage,
                    isLoadingLyrics = isLoadingLyrics,
                    displayMode = displayMode,
                    onDisplayModeChange = { viewModel.setDisplayMode(it) },
                    enableAnimatedCovers = settings.enableAnimatedCovers,
                    autoFetchAnimatedCoversWifiOnly = settings.autoFetchAnimatedCoversWifiOnly,
                    simulateMotionOnStaticCovers = settings.simulateMotionOnStaticCovers,
                    onImportAnimatedCover = { uri -> viewModel.importAnimatedCover(context, currentTrack, uri) },
                    onRemoveCustomAnimatedCover = { viewModel.removeCustomAnimatedCover(context, currentTrack) },
                    onImportLyricsUri = { uri -> viewModel.importLyricsFromUri(context, currentTrack, uri) },
                    onSavePastedLyrics = { text -> viewModel.savePastedLyrics(context, currentTrack, text) },
                    loadEditableLyrics = { viewModel.getEditableLyricsText(context, currentTrack) },
                    onRefetchLyrics = { viewModel.refetchLyricsOnline(context, currentTrack) },
                    onClearLyrics = { viewModel.clearTrackLyrics(context, currentTrack) },
                    onRemoveLocalTrack = { track ->
                        viewModel.removeTrack(context, track.id)
                        Toast.makeText(context, "Removed \"${track.title}\" from library", Toast.LENGTH_SHORT).show()
                    }
                )
                    }
                    AppLayoutMode.COVER_LANDSCAPE -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CoverLandscapeLayout(
                                currentTrack = currentTrack,
                                isPlaying = isPlaying,
                                currentPositionMs = currentPositionMs,
                                tracks = tracks,
                                allTracks = allTracksList,
                                favoriteTracks = favoriteTracks,
                                playlists = playlists,
                                selectedTab = selectedTab,
                                searchQuery = searchQuery,
                                selectedAlbumId = selectedAlbumForDetail,
                                selectedArtistId = selectedArtistForDetail,
                                palette = palette,
                                isBluetoothConnected = isBluetoothConnected,
                                connectedDeviceName = connectedDeviceName,
                                albumLayoutSetting = settings.albumLayout,
                                albumArtSize = settings.trackListAlbumArtSize,
                                progressBarDesign = settings.progressBarDesign,
                                progressBarThickness = settings.progressBarThickness,
                                activeFilter = activeFilter,
                                sortOption = sortOption,
                                elementColors = settings.elementColors,
                                themeEngine = settings.themeEngine,
                                useCustomElementColors = settings.useCustomElementColors,
                                enableAnimatedCovers = settings.enableAnimatedCovers,
                                autoFetchAnimatedCoversWifiOnly = settings.autoFetchAnimatedCoversWifiOnly,
                                simulateMotionOnStaticCovers = settings.simulateMotionOnStaticCovers,
                                tabListStates = tabListStates,
                                tracksListState = tracksListState,
                                albumsGridState = albumsGridState,
                                artistsGridState = artistsGridState,
                                albumDetailListState = albumDetailListState,
                                artistDetailListState = artistDetailListState,
                                artistDetailAlbumsRowState = artistDetailAlbumsRowState,
                                onTabSelected = { viewModel.selectTab(it) },
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onSelectAlbumForDetail = { viewModel.selectAlbumForDetail(it) },
                                onSelectArtistForDetail = { viewModel.selectArtistForDetail(it) },
                                onTrackClick = { viewModel.playTrack(it) },
                                onPlayFromContext = onPlayFromContext,
                                onShuffleFromContext = onShuffleFromContext,
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onNext = { viewModel.nextTrack() },
                                onPrevious = { viewModel.previousTrack() },
                                onSeek = { viewModel.seekTo(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onExpandPlayer = { viewModel.expandPlayer() },
                                onOpenEqualizer = { viewModel.openEqualizer() },
                                onOpenBluetooth = { BluetoothAudioHelper.openBluetoothSettings(context) },
                                onOpenSettings = { viewModel.openSettings() },
                                onFilterSelected = { viewModel.setActiveFilter(it) },
                                onOpenSortDialog = { isSortDialogOpen = true },
                                onPickLocalFiles = onPickFiles,
                                onScanDeviceStorage = onScanStorage,
                                onImportAnimatedCover = { uri -> viewModel.importAnimatedCover(context, currentTrack, uri) },
                                onRemoveCustomAnimatedCover = { viewModel.removeCustomAnimatedCover(context, currentTrack) },
                                onRemoveLocalTrack = { track ->
                                    viewModel.removeTrack(context, track.id)
                                    Toast.makeText(context, "Removed \"${track.title}\" from library", Toast.LENGTH_SHORT).show()
                                },
                                onBackPress = {
                                    activity?.finishAffinity()
                                    activity?.finish()
                                }
                            )

                            AnimatedContent(
                                targetState = isPlayerExpanded,
                                transitionSpec = {
                                    if (targetState) {
                                        NavigationTransitions.playerSheetEnter(this) togetherWith ExitTransition.None
                                    } else {
                                        EnterTransition.None togetherWith NavigationTransitions.playerSheetExit(this)
                                    }
                                },
                                modifier = Modifier.fillMaxSize(),
                                label = "cover_landscape_now_playing_sheet_transition"
                            ) { expanded ->
                                if (expanded) {
                                    NowPlayingScreen(
                                        track = currentTrack,
                                        isPlaying = isPlaying,
                                        currentPositionMs = currentPositionMs,
                                        visualizerAmplitudes = visualizerAmplitudes,
                                        isShuffleActive = isShuffleActive,
                                        repeatMode = repeatMode,
                                        volume = volume,
                                        speed = speed,
                                        sleepTimerMinutesLeft = sleepTimerMinutesLeft,
                                        onCollapse = { viewModel.collapsePlayer() },
                                        onTogglePlayPause = { viewModel.togglePlayPause() },
                                        onNext = { viewModel.nextTrack() },
                                        onPrevious = { viewModel.previousTrack() },
                                        onSeek = { viewModel.seekTo(it) },
                                        onToggleShuffle = { viewModel.toggleShuffle() },
                                        onCycleRepeat = { viewModel.cycleRepeatMode() },
                                        onToggleFavorite = { viewModel.toggleFavorite(currentTrack.id) },
                                        onVolumeChange = { viewModel.setVolume(it) },
                                        onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                                        onOpenEqualizer = { viewModel.openEqualizer() },
                                        onOpenSleepTimer = { viewModel.openSleepTimer() },
                                        progressBarDesign = settings.progressBarDesign,
                                        progressBarThickness = settings.progressBarThickness,
                                        playerButtonSize = settings.playerButtonSize,
                                        visualizerAlbumArtSize = settings.visualizerAlbumArtSize,
                                        visualizerStyle = settings.visualizerStyle,
                                        onVisualizerStyleChange = { newStyle ->
                                            viewModel.updateSettings { it.copy(visualizerStyle = newStyle) }
                                        },
                                        buttonAnimationEffect = settings.buttonAnimationEffect,
                                        lyricsAnimationStyle = settings.lyricsAnimationStyle,
                                        lyricsAnimationSpeed = settings.lyricsAnimationSpeed,
                                        lyricsGlowEnabled = settings.lyricsGlowEnabled,
                                        elementColors = settings.elementColors,
                                        isLoadingLyrics = isLoadingLyrics,
                                        displayMode = displayMode,
                                        onDisplayModeChange = { viewModel.setDisplayMode(it) },
                                        enableAnimatedCovers = settings.enableAnimatedCovers,
                                        autoFetchAnimatedCoversWifiOnly = settings.autoFetchAnimatedCoversWifiOnly,
                                        simulateMotionOnStaticCovers = settings.simulateMotionOnStaticCovers,
                                        onImportAnimatedCover = { uri -> viewModel.importAnimatedCover(context, currentTrack, uri) },
                                        onRemoveCustomAnimatedCover = { viewModel.removeCustomAnimatedCover(context, currentTrack) },
                                        onImportLyricsUri = { uri -> viewModel.importLyricsFromUri(context, currentTrack, uri) },
                                        onSavePastedLyrics = { text -> viewModel.savePastedLyrics(context, currentTrack, text) },
                                        loadEditableLyrics = { viewModel.getEditableLyricsText(context, currentTrack) },
                                        onRefetchLyrics = { viewModel.refetchLyricsOnline(context, currentTrack) },
                                        onClearLyrics = { viewModel.clearTrackLyrics(context, currentTrack) }
                                    )
                                }
                            }
                        }
                    }
                    AppLayoutMode.COVER -> {
                        val navBarBottomPadding = WindowInsets.navigationBars.union(WindowInsets.displayCutout).asPaddingValues().calculateBottomPadding()
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route ?: "cover_library"

                        var retainedArtistForDetail by remember { mutableStateOf(selectedArtistForDetail) }
                        var retainedAlbumForDetail by remember { mutableStateOf(selectedAlbumForDetail) }
                        if (selectedArtistForDetail != null) retainedArtistForDetail = selectedArtistForDetail
                        if (selectedAlbumForDetail != null) retainedAlbumForDetail = selectedAlbumForDetail

                        var prevSyncedRoute by remember { mutableStateOf(currentRoute) }
                        var prevSyncedArtist by remember { mutableStateOf(selectedArtistForDetail) }
                        var prevSyncedAlbum by remember { mutableStateOf(selectedAlbumForDetail) }

                        LaunchedEffect(currentRoute, selectedArtistForDetail, selectedAlbumForDetail) {
                            val routeChanged = currentRoute != prevSyncedRoute
                            val vmChanged = selectedArtistForDetail != prevSyncedArtist ||
                                selectedAlbumForDetail != prevSyncedAlbum
                            prevSyncedRoute = currentRoute
                            prevSyncedArtist = selectedArtistForDetail
                            prevSyncedAlbum = selectedAlbumForDetail

                            if (routeChanged && !vmChanged) {
                                when (currentRoute) {
                                    "cover_artist" -> if (selectedAlbumForDetail != null) {
                                        viewModel.selectAlbumForDetail(null)
                                    }
                                    "cover_library" -> {
                                        if (selectedAlbumForDetail != null) viewModel.selectAlbumForDetail(null)
                                        if (selectedArtistForDetail != null) viewModel.selectArtistForDetail(null)
                                    }
                                }
                            } else {
                                val targetRoute = when {
                                    selectedAlbumForDetail != null -> "cover_album"
                                    selectedArtistForDetail != null -> "cover_artist"
                                    else -> "cover_library"
                                }
                                if (currentRoute != targetRoute) {
                                    when (targetRoute) {
                                        "cover_library" -> {
                                            navController.popBackStack("cover_library", inclusive = false)
                                        }
                                        "cover_artist" -> {
                                            if (currentRoute == "cover_album") {
                                                navController.popBackStack("cover_artist", inclusive = false)
                                            } else {
                                                navController.navigate("cover_artist") { launchSingleTop = true }
                                            }
                                        }
                                        "cover_album" -> {
                                            if (selectedArtistForDetail != null && currentRoute == "cover_library") {
                                                navController.navigate("cover_artist") { launchSingleTop = true }
                                            }
                                            navController.navigate("cover_album") { launchSingleTop = true }
                                        }
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            NavHost(
                                navController = navController,
                                startDestination = "cover_library",
                                enterTransition = { NavigationTransitions.hierarchicalEnter(this) },
                                exitTransition = { NavigationTransitions.hierarchicalExit(this) },
                                popEnterTransition = { NavigationTransitions.hierarchicalPopEnter(this) },
                                popExitTransition = { NavigationTransitions.hierarchicalPopExit(this) },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composable("cover_library") {
                                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                                    // Samsung One UI Reachability Layout:
                                    // Viewing area on top with Material 3 LargeTopAppBar (~270dp expanded height) that collapses smoothly to 64dp
                                    // Interaction area in the thumb zone below with proper padding for sticky MiniPlayer
                                    Scaffold(
                                        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
                                        containerColor = Color.Transparent,
                        topBar = {
                            val collapsedFraction = topAppBarScrollBehavior.state.collapsedFraction
                            val titleText = selectedTab.title
                            val expandedBg = parseHexColor(settings.elementColors.headerExpandedBackground) ?: Color.Transparent
                            val collapsedBg = parseHexColor(settings.elementColors.headerCollapsedBackground) ?: Color(0xE60F101E)
                            val titleClr = parseHexColor(settings.elementColors.headerTitleColor) ?: Color.White
                            val subtitleClr = parseHexColor(settings.elementColors.headerSubtitleColor) ?: palette.accent

                            LargeTopAppBar(
                                title = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 8.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = if (collapsedFraction > 0.6f) titleText else "OneMusic",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = lerp(34.sp, 20.sp, collapsedFraction),
                                                letterSpacing = (-0.5).sp
                                            ),
                                            color = titleClr,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (collapsedFraction < 0.65f) {
                                            val subtitleAlpha = (1f - (collapsedFraction / 0.65f)).coerceIn(0f, 1f)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier
                                                    .graphicsLayer { alpha = subtitleAlpha }
                                                    .padding(top = 4.dp)
                                            ) {
                                                Text(
                                                    text = "${tracks.size} tracks • $titleText",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                    color = subtitleClr
                                                )
                                                if (isBluetoothConnected && connectedDeviceName != null) {
                                                    Text(
                                                        text = "• $connectedDeviceName",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = subtitleClr.copy(alpha = 0.7f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                actions = {
                                    OneUITopBarActions(
                                        isBluetoothConnected = isBluetoothConnected,
                                        connectedDeviceName = connectedDeviceName,
                                        dolbyAtmosActive = dolbyAtmosEnabled,
                                        accentColor = palette.accent,
                                        isSearchActive = isSearchExpanded,
                                        onOpenBluetooth = { BluetoothAudioHelper.openBluetoothSettings(context) },
                                        onOpenEqualizer = { viewModel.openEqualizer() },
                                        onToggleSearch = { isSearchExpanded = !isSearchExpanded },
                                        onOpenSettings = { viewModel.openSettings() }
                                    )
                                },
                                collapsedHeight = 64.dp,
                                expandedHeight = 270.dp,
                                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                                colors = TopAppBarDefaults.largeTopAppBarColors(
                                    containerColor = expandedBg,
                                    scrolledContainerColor = collapsedBg,
                                    titleContentColor = titleClr
                                ),
                                scrollBehavior = topAppBarScrollBehavior
                            )
                        },
                        bottomBar = {
                            // Reserve space for the shared persistent MiniPlayer rendered above main tabs & detail overlays
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.displayCutout.only(WindowInsetsSides.Bottom)))
                                    .height(84.dp)
                            )
                        }
                    ) { innerPadding ->
                    // Lower Interaction Area Zone (Galaxy-Specific Thumb-Reachable Styling)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = innerPadding.calculateTopPadding(),
                                bottom = innerPadding.calculateBottomPadding()
                            )
                            .clip(OneUISquircleLarge)
                            .background(Color(0x15FFFFFF))
                            .padding(top = 8.dp)
                            .testTag("interaction_area_zone")
                    ) {
                        // Expandable search bar if active
                        AnimatedVisibility(visible = isSearchExpanded) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.updateSearchQuery(it) },
                                    placeholder = { Text("Search songs, artists, genres...", color = Color(0x88FFFFFF)) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .glassmorphism(shape = OneUIPillShape, backgroundColor = Color(0x331F213A))
                                        .testTag("search_input"),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = "Search", tint = palette.accent)
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        val tabHighlightColor = if (settings.themeEngine == DynamicThemeEngine.INDIVIDUAL_ELEMENTS || settings.useCustomElementColors) {
                            parseHexColor(settings.elementColors.tabHighlight) ?: palette.accent
                        } else {
                            palette.accent
                        }
                        OneUITabBar(
                            selectedTab = selectedTab,
                            accentColor = palette.accent,
                            tabHighlightColor = tabHighlightColor,
                            onTabSelected = { viewModel.selectTab(it) }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Filter Chips row (All, Favorites, Device Storage, Genres)
                        FilterChipsRow(
                            selectedFilter = activeFilter,
                            accentColor = palette.accent,
                            onFilterSelected = { viewModel.setActiveFilter(it) }
                        )

                        // Sort bar row (Songs count & Sort trigger)
                        SortBarRow(
                            totalCount = if (selectedTab == MusicTab.FAVORITES) favoriteTracks.size else tracks.size,
                            selectedSort = sortOption,
                            accentColor = palette.accent,
                            onOpenSortDialog = { isSortDialogOpen = true }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = { NavigationTransitions.tabFadeThrough() },
                                modifier = Modifier.fillMaxSize(),
                                label = "cover_tab_fade_through"
                            ) { currentTab ->
                                when (currentTab) {
                                    MusicTab.TRACKS -> {
                                        TracksTabContent(
                                            tracks = tracks,
                                            currentTrack = currentTrack,
                                            isPlaying = isPlaying,
                                            accentColor = palette.accent,
                                            albumArtSize = settings.trackListAlbumArtSize,
                                            elementColors = settings.elementColors,
                                            listState = tabListStates.getValue(MusicTab.TRACKS),
                                            onTrackClick = { viewModel.playTrack(it) },
                                            onPlayFromContext = onPlayFromContext,
                                            onToggleFavorite = { viewModel.toggleFavorite(it.id) },
                                            onPickLocalFiles = onPickFiles,
                                            onScanDeviceStorage = onScanStorage,
                                            onRemoveLocalTrack = { track ->
                                                viewModel.removeTrack(context, track.id)
                                                Toast.makeText(context, "Removed \"${track.title}\" from library", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                    MusicTab.PLAYLISTS -> {
                                        PlaylistsTabContent(
                                            playlists = playlists,
                                            allTracks = allTracksList,
                                            currentTrack = currentTrack,
                                            accentColor = palette.accent,
                                            listState = tabListStates.getValue(MusicTab.PLAYLISTS),
                                            onPlayTrack = { viewModel.playTrack(it) },
                                            onPlayFromContext = onPlayFromContext
                                        )
                                    }
                                    MusicTab.ALBUMS -> {
                                        AlbumsTabContent(
                                            tracks = allTracksList,
                                            layoutSetting = settings.albumLayout,
                                            sortOption = sortOption,
                                            accentColor = palette.accent,
                                            listState = tabListStates.getValue(MusicTab.ALBUMS),
                                            gridState = albumsGridState,
                                            onSelectAlbum = { viewModel.selectAlbumForDetail(it) },
                                            onSelectTrack = { viewModel.playTrack(it) }
                                        )
                                    }
                                    MusicTab.ARTISTS -> {
                                        ArtistsTabContent(
                                            tracks = allTracksList,
                                            sortOption = sortOption,
                                            accentColor = palette.accent,
                                            gridState = artistsGridState,
                                            onSelectArtist = { viewModel.selectArtistForDetail(it) },
                                            onSelectTrack = { viewModel.playTrack(it) }
                                        )
                                    }
                                    MusicTab.FAVORITES -> {
                                        FavoritesTabContent(
                                            tracks = favoriteTracks,
                                            currentTrack = currentTrack,
                                            isPlaying = isPlaying,
                                            accentColor = palette.accent,
                                            albumArtSize = settings.trackListAlbumArtSize,
                                            elementColors = settings.elementColors,
                                            listState = tabListStates.getValue(MusicTab.FAVORITES),
                                            onTrackClick = { viewModel.playTrack(it) },
                                            onPlayFromContext = onPlayFromContext,
                                            onToggleFavorite = { viewModel.toggleFavorite(it.id) },
                                            onRemoveLocalTrack = { track ->
                                                viewModel.removeTrack(context, track.id)
                                                Toast.makeText(context, "Removed \"${track.title}\" from library", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }
                                    }
                                }
                                composable("cover_artist") {
                                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                                        val artistName = selectedArtistForDetail ?: retainedArtistForDetail
                                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F101E))) {
                                            artistName?.let { name ->
                                                val artistTracks = remember(name, allTracksList) {
                                                    allTracksList.filter { it.artist.equals(name, ignoreCase = true) }
                                                }
                                                ArtistDetailScreen(
                                                    artistName = name,
                                                    tracks = artistTracks,
                                                    currentTrack = currentTrack,
                                                    isPlaying = isPlaying,
                                                    accentColor = palette.accent,
                                                    albumArtSize = settings.trackListAlbumArtSize,
                                                    elementColors = settings.elementColors,
                                                    listState = artistDetailListState,
                                                    albumsRowState = artistDetailAlbumsRowState,
                                                    bottomContentPadding = navBarBottomPadding + 92.dp,
                                                    enableBackHandler = false,
                                                    onBack = { viewModel.selectArtistForDetail(null) },
                                                    onSelectAlbum = { viewModel.selectAlbumForDetail(it) },
                                                    onPlayFromContext = onPlayFromContext,
                                                    onShuffleFromContext = onShuffleFromContext,
                                                    onToggleFavorite = { viewModel.toggleFavorite(it.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                                composable("cover_album") {
                                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                                        val albumName = selectedAlbumForDetail ?: retainedAlbumForDetail
                                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F101E))) {
                                            albumName?.let { name ->
                                                val albumTracks = remember(name, allTracksList) {
                                                    allTracksList
                                                        .filter { it.album.equals(name, ignoreCase = true) }
                                                        .sortedWith(com.example.model.AlbumTrackOrderComparator)
                                                }
                                                AlbumDetailScreen(
                                                    albumName = name,
                                                    tracks = albumTracks,
                                                    currentTrack = currentTrack,
                                                    isPlaying = isPlaying,
                                                    accentColor = palette.accent,
                                                    albumArtSize = settings.trackListAlbumArtSize,
                                                    elementColors = settings.elementColors,
                                                    listState = albumDetailListState,
                                                    bottomContentPadding = navBarBottomPadding + 92.dp,
                                                    enableBackHandler = false,
                                                    onBack = { viewModel.selectAlbumForDetail(null) },
                                                    onPlayFromContext = onPlayFromContext,
                                                    onShuffleFromContext = onShuffleFromContext,
                                                    onToggleFavorite = { viewModel.toggleFavorite(it.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Persistent Compact MiniPlayer across Cover Screen tabs, Album Details, and Artist Details
                            AnimatedVisibility(
                                visible = !isPlayerExpanded,
                                enter = fadeIn(MotionSpecs.snappySpring()) + slideInVertically(
                                    animationSpec = MotionSpecs.expressiveSpring(),
                                    initialOffsetY = { it / 2 }
                                ),
                                exit = fadeOut(MotionSpecs.snappySpring()) + slideOutVertically(
                                    animationSpec = MotionSpecs.expressiveSpring(),
                                    targetOffsetY = { it / 2 }
                                ),
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@AnimatedVisibility) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .windowInsetsPadding(
                                                WindowInsets.navigationBars.union(
                                                    WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                                                )
                                            )
                                    ) {
                                        MiniPlayer(
                                            track = currentTrack,
                                            isPlaying = isPlaying,
                                            currentPositionMs = currentPositionMs,
                                            onExpand = { viewModel.expandPlayer() },
                                            onTogglePlayPause = { viewModel.togglePlayPause() },
                                            onNext = { viewModel.nextTrack() }
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }

                            // Full Now Playing Sheet (renders above tabs, detail screens, and MiniPlayer with SharedTransitionLayout morph)
                            AnimatedContent(
                                targetState = isPlayerExpanded,
                                transitionSpec = {
                                    if (targetState) {
                                        NavigationTransitions.playerSheetEnter(this) togetherWith ExitTransition.None
                                    } else {
                                        EnterTransition.None togetherWith NavigationTransitions.playerSheetExit(this)
                                    }
                                },
                                modifier = Modifier.fillMaxSize(),
                                label = "now_playing_sheet_transition"
                            ) { expanded ->
                                if (expanded) {
                                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@AnimatedContent) {
                                        NowPlayingScreen(
                                        track = currentTrack,
                                        isPlaying = isPlaying,
                                        currentPositionMs = currentPositionMs,
                                        visualizerAmplitudes = visualizerAmplitudes,
                                        isShuffleActive = isShuffleActive,
                                        repeatMode = repeatMode,
                                        volume = volume,
                                        speed = speed,
                                        sleepTimerMinutesLeft = sleepTimerMinutesLeft,
                                        onCollapse = { viewModel.collapsePlayer() },
                                        onTogglePlayPause = { viewModel.togglePlayPause() },
                                        onNext = { viewModel.nextTrack() },
                                        onPrevious = { viewModel.previousTrack() },
                                        onSeek = { viewModel.seekTo(it) },
                                        onToggleShuffle = { viewModel.toggleShuffle() },
                                        onCycleRepeat = { viewModel.cycleRepeatMode() },
                                        onToggleFavorite = { viewModel.toggleFavorite(currentTrack.id) },
                                        onVolumeChange = { viewModel.setVolume(it) },
                                        onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                                        onOpenEqualizer = { viewModel.openEqualizer() },
                                        onOpenSleepTimer = { viewModel.openSleepTimer() },
                                        progressBarDesign = settings.progressBarDesign,
                                        progressBarThickness = settings.progressBarThickness,
                                        playerButtonSize = settings.playerButtonSize,
                                        visualizerAlbumArtSize = settings.visualizerAlbumArtSize,
                                        visualizerStyle = settings.visualizerStyle,
                                        onVisualizerStyleChange = { newStyle ->
                                            viewModel.updateSettings { it.copy(visualizerStyle = newStyle) }
                                        },
                                        buttonAnimationEffect = settings.buttonAnimationEffect,
                                        lyricsAnimationStyle = settings.lyricsAnimationStyle,
                                        lyricsAnimationSpeed = settings.lyricsAnimationSpeed,
                                        lyricsGlowEnabled = settings.lyricsGlowEnabled,
                                        elementColors = settings.elementColors,
                                        isLoadingLyrics = isLoadingLyrics,
                                        displayMode = displayMode,
                                        onDisplayModeChange = { viewModel.setDisplayMode(it) },
                                        enableAnimatedCovers = settings.enableAnimatedCovers,
                                        autoFetchAnimatedCoversWifiOnly = settings.autoFetchAnimatedCoversWifiOnly,
                                        simulateMotionOnStaticCovers = settings.simulateMotionOnStaticCovers,
                                        onImportAnimatedCover = { uri -> viewModel.importAnimatedCover(context, currentTrack, uri) },
                                        onRemoveCustomAnimatedCover = { viewModel.removeCustomAnimatedCover(context, currentTrack) },
                                        onImportLyricsUri = { uri -> viewModel.importLyricsFromUri(context, currentTrack, uri) },
                                        onSavePastedLyrics = { text -> viewModel.savePastedLyrics(context, currentTrack, text) },
                                        loadEditableLyrics = { viewModel.getEditableLyricsText(context, currentTrack) },
                                            onRefetchLyrics = { viewModel.refetchLyricsOnline(context, currentTrack) },
                                            onClearLyrics = { viewModel.clearTrackLyrics(context, currentTrack) },
                                            enableSharedPlayerTransition = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Equalizer Modal Sheet
            if (isEqualizerOpen) {
                EqualizerSheet(
                    selectedPreset = soundPreset,
                    bassValue = bassGain,
                    midValue = midGain,
                    trebleValue = trebleGain,
                    dolbyAtmosEnabled = dolbyAtmosEnabled,
                    primaryColor = palette.primary,
                    accentColor = palette.accent,
                    onSelectPreset = { viewModel.selectSoundPreset(it) },
                    onBassChange = { viewModel.setBass(it) },
                    onMidChange = { viewModel.setMid(it) },
                    onTrebleChange = { viewModel.setTreble(it) },
                    onToggleDolbyAtmos = { viewModel.toggleDolbyAtmos(it) },
                    onDismiss = { viewModel.closeEqualizer() }
                )
            }

            // Sleep Timer Dialog
            if (isSleepTimerOpen) {
                SleepTimerDialog(
                    currentMinutesLeft = sleepTimerMinutesLeft,
                    accentColor = palette.accent,
                    onSetTimerMinutes = { viewModel.setSleepTimer(it) },
                    onDismiss = { viewModel.closeSleepTimer() }
                )
            }

            // Settings Fullscreen Screen with hierarchical slide transition
            AnimatedContent(
                targetState = isSettingsOpen,
                transitionSpec = {
                    if (targetState) {
                        NavigationTransitions.hierarchicalEnter(this) togetherWith ExitTransition.None
                    } else {
                        EnterTransition.None togetherWith NavigationTransitions.hierarchicalPopExit(this)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                label = "settings_screen_transition"
            ) { settingsOpen ->
                if (settingsOpen) {
                    val localTracks = remember(allTracksList) { allTracksList.filter { it.isLocalFile } }
                    val duplicateTracksCount = remember(allTracksList) {
                        MusicRepository.getInstance().findDuplicateLocalTracks().size
                    }
                    SettingsScreen(
                        settings = settings,
                        accentColor = palette.accent,
                        onBack = { viewModel.closeSettings() },
                        onUpdateSettings = { viewModel.updateSettings(it) },
                        onOpenEqualizer = { viewModel.openEqualizer() },
                        localTracksCount = localTracks.size,
                        totalTracksCount = allTracksList.size,
                        onPickLocalFiles = onPickFiles,
                        onScanDeviceStorage = onScanStorage,
                        localTracks = localTracks,
                        duplicateTracksCount = duplicateTracksCount,
                        recentImportBanner = recentImportBanner,
                        onUndoLastImport = {
                            viewModel.undoLastImport(context)
                            Toast.makeText(context, "Undid last import", Toast.LENGTH_SHORT).show()
                        },
                        onRemoveLocalTrack = { trackId ->
                            viewModel.removeTrack(context, trackId)
                            Toast.makeText(context, "Removed track from library", Toast.LENGTH_SHORT).show()
                        },
                        onRemoveDuplicateTracks = {
                            viewModel.removeDuplicateTracks(context) { removedCount ->
                                val msg = if (removedCount > 0) {
                                    "Merged $removedCount duplicate track${if (removedCount == 1) "" else "s"}"
                                } else {
                                    "No duplicate local tracks found"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onClearFilePickedImports = {
                            viewModel.clearFilePickedImports(context) { removedCount ->
                                val msg = if (removedCount > 0) {
                                    "Removed $removedCount file-picked track${if (removedCount == 1) "" else "s"}"
                                } else {
                                    "No file-picked imports to remove"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        lyricsSyncUiState = lyricsSyncUiState,
                        onStartBatchLyricsSync = { viewModel.startBatchLyricsSync(context) },
                        onCancelBatchLyricsSync = { viewModel.cancelBatchLyricsSync(context) },
                        onRetryFailedLyricsSync = { viewModel.retryFailedLyricsLookups(context) }
                    )
                }
            }

            // Sort Options Selection Dialog
            if (isSortDialogOpen) {
                SortOptionsDialog(
                    currentSort = sortOption,
                    accentColor = palette.accent,
                    onSortSelected = { viewModel.setSortOption(it) },
                    onDismiss = { isSortDialogOpen = false }
                )
            }

            // Floating Import Undo / Merge Summary Banner (visible outside Settings)
            AnimatedVisibility(
                visible = recentImportBanner != null && !isSettingsOpen,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 96.dp)
            ) {
                recentImportBanner?.let { banner ->
                    ImportUndoSnackbarBanner(
                        banner = banner,
                        accentColor = palette.accent,
                        onUndo = {
                            viewModel.undoLastImport(context)
                            Toast.makeText(context, "Undid last import", Toast.LENGTH_SHORT).show()
                        },
                        onDismiss = { viewModel.dismissImportBanner() }
                    )
                }
            }

            // Global Long Press Tooltip HUD Banner
            OneUITooltipBanner(
                text = currentTooltipHUD,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp)
            )
        }
        }
    }
}

@Composable
private fun ImportUndoSnackbarBanner(
    banner: ImportSummaryBanner,
    accentColor: Color,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .glassmorphism(
                shape = OneUIPillShape,
                backgroundColor = Color(0xEC16192B),
                borderWidth = 1.dp,
                elevation = 10.dp
            )
            .border(1.2.dp, accentColor.copy(alpha = 0.5f), OneUIPillShape)
            .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
            .testTag("import_summary_floating_banner"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = banner.summaryLabel,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (banner.importedTrackIds.isNotEmpty()) {
            TextButton(
                onClick = onUndo,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("import_undo_banner_button"),
                colors = ButtonDefaults.textButtonColors(contentColor = accentColor)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Undo",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(48.dp)
                .testTag("import_dismiss_banner_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss import notice",
                tint = Color(0xAAFFFFFF),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TrackActionDialog(
    track: Track,
    accentColor: Color,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRemoveFromLibrary: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val sourceLabel = when {
        track.id.startsWith("local_") -> "File-Picked Import"
        track.isLocalFile -> "Device Local Audio"
        else -> "Built-in Library Track"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF171A2E),
        titleContentColor = Color.White,
        textContentColor = Color(0xCCFFFFFF),
        title = {
            Column {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${track.artist} • ${track.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xAAFFFFFF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = sourceLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = accentColor
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        onPlay()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = accentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Play Now",
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }

                TextButton(
                    onClick = {
                        onToggleFavorite()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = accentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (track.isFavorite) "Remove from Favorites" else "Add to Favorites",
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (track.isLocalFile && onRemoveFromLibrary != null) {
                    TextButton(
                        onClick = {
                            onRemoveFromLibrary()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("track_context_remove_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = Color(0xFFFF6B81)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remove from Library",
                            color = Color(0xFFFF6B81),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = accentColor)
            }
        }
    )
}

/**
 * Top App Bar Actions for Samsung One UI Reachability Header.
 * Houses Bluetooth audio route status, Dolby Atmos toggle, Search button, and Settings button.
 */
@Composable
fun OneUITopBarActions(
    isBluetoothConnected: Boolean,
    connectedDeviceName: String?,
    dolbyAtmosActive: Boolean,
    accentColor: Color,
    isSearchActive: Boolean,
    onOpenBluetooth: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onToggleSearch: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(end = 4.dp)
    ) {
        // Bluetooth Audio Output Pill Button
        OneUITooltipButton(
            name = if (isBluetoothConnected) "Connected: ${connectedDeviceName ?: "Bluetooth Audio"} (Tap to change)" else "Audio Output: Phone Speaker (Tap to connect Bluetooth)",
            onClick = onOpenBluetooth
        ) {
            Box(
                modifier = Modifier
                    .glassmorphism(
                        shape = OneUIPillShape,
                        backgroundColor = if (isBluetoothConnected) Color(0x3500E5FF) else Color(0x20FFFFFF),
                        borderWidth = 1.dp
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("bluetooth_audio_pill")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isBluetoothConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                        contentDescription = "Bluetooth Audio",
                        tint = if (isBluetoothConnected) Color(0xFF00E5FF) else Color(0x88FFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBluetoothConnected) (connectedDeviceName ?: "BT") else "Speaker",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Dolby Atmos indicator pill
        OneUITooltipButton(
            name = if (dolbyAtmosActive) "Dolby Atmos 3D Spatial (Active)" else "Stereo SoundAlive",
            onClick = onOpenEqualizer
        ) {
            Box(
                modifier = Modifier
                    .glassmorphism(
                        shape = OneUIPillShape,
                        backgroundColor = if (dolbyAtmosActive) Color(0x357C4DFF) else Color(0x20FFFFFF),
                        borderWidth = 1.dp
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SurroundSound,
                        contentDescription = "Dolby Atmos",
                        tint = if (dolbyAtmosActive) accentColor else Color(0x88FFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (dolbyAtmosActive) "Atmos" else "Stereo",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        // Search toggle button
        OneUITooltipButton(
            name = "Search Tracks, Artists & Albums",
            onClick = onToggleSearch
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .glassmorphism(shape = CircleShape, backgroundColor = Color(0x25FFFFFF))
                    .testTag("search_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSearchActive) Icons.Default.Clear else Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Settings button
        OneUITooltipButton(
            name = "Settings & Customization",
            onClick = onOpenSettings
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .glassmorphism(shape = CircleShape, backgroundColor = Color(0x35FFFFFF))
                    .testTag("settings_header_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Samsung One UI Upper Viewing Area Header.
 * Follows Samsung's Viewing Area / Interaction Area architecture:
 * When pulled down or at rest, displays a large One UI-style viewing stage (~170dp)
 * with big expressive typography, Bluetooth audio status pill, Dolby Atmos, and actions.
 * Tapping or scrolling collapses into a sleek compact bar.
 */
@Composable
fun OneUIViewingHeader(
    selectedTab: MusicTab,
    dolbyAtmosActive: Boolean,
    isSearchActive: Boolean,
    searchQuery: String,
    accentColor: Color,
    totalTracksCount: Int,
    isBluetoothConnected: Boolean,
    connectedDeviceName: String?,
    isViewingAreaExpanded: Boolean,
    onToggleViewingArea: () -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("one_ui_viewing_area")
    ) {
        // Top Action Bar Row (Bluetooth Audio, Atmos, Search, Settings)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Bluetooth Audio Output Pill Button
            OneUITooltipButton(
                name = if (isBluetoothConnected) "Connected: ${connectedDeviceName ?: "Bluetooth Audio"} (Tap to change)" else "Audio Output: Phone Speaker (Tap to connect Bluetooth)",
                onClick = onOpenBluetoothSettings
            ) {
                Box(
                    modifier = Modifier
                        .glassmorphism(
                            shape = OneUIPillShape,
                            backgroundColor = if (isBluetoothConnected) Color(0x3500E5FF) else Color(0x20FFFFFF),
                            borderWidth = 1.dp
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("bluetooth_audio_pill")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isBluetoothConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                            contentDescription = "Bluetooth Audio",
                            tint = if (isBluetoothConnected) Color(0xFF00E5FF) else Color(0x88FFFFFF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isBluetoothConnected) (connectedDeviceName ?: "Bluetooth") else "Speaker",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Dolby Atmos indicator pill (with tooltip)
                OneUITooltipButton(
                    name = if (dolbyAtmosActive) "Dolby Atmos 3D Spatial (Active)" else "Stereo SoundAlive",
                    onClick = onOpenEqualizer
                ) {
                    Box(
                        modifier = Modifier
                            .glassmorphism(
                                shape = OneUIPillShape,
                                backgroundColor = if (dolbyAtmosActive) Color(0x357C4DFF) else Color(0x20FFFFFF),
                                borderWidth = 1.dp
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SurroundSound,
                                contentDescription = "Dolby Atmos",
                                tint = if (dolbyAtmosActive) accentColor else Color(0x88FFFFFF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (dolbyAtmosActive) "Atmos" else "Stereo",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Search toggle button
                OneUITooltipButton(
                    name = "Search Tracks, Artists & Albums",
                    onClick = onToggleSearch
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .glassmorphism(shape = CircleShape, backgroundColor = Color(0x25FFFFFF))
                            .testTag("search_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Prominent Settings button at the top of the app
                OneUITooltipButton(
                    name = "Settings & Customization",
                    onClick = onOpenSettings
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .glassmorphism(shape = CircleShape, backgroundColor = Color(0x35FFFFFF))
                            .testTag("settings_header_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Viewing Area Stage (when pulled down or expanded, displays the large One UI-style header)
        if (isViewingAreaExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 10.dp)
                    .clickable { onToggleViewingArea() }
                    .testTag("large_viewing_stage")
            ) {
                Text(
                    text = "OneMusic",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.8).sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$totalTracksCount Songs in Library",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0x99FFFFFF)
                    )
                    if (isBluetoothConnected) {
                        Box(
                            modifier = Modifier
                                .clip(OneUIPillShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.22f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Bluetooth Audio",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                }
            }
        } else {
            // Compact Header bar when collapsed
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 4.dp)
                    .clickable { onToggleViewingArea() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "OneMusic",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• $totalTracksCount Songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x88FFFFFF)
                    )
                }
            }
        }

        // Search Input Expandable Field
        AnimatedVisibility(visible = isSearchActive) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search songs, artists, genres...", color = Color(0x88FFFFFF)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphism(shape = OneUIPillShape, backgroundColor = Color(0x331F213A))
                        .testTag("search_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    selectedFilter: FilterType,
    accentColor: Color,
    onFilterSelected: (FilterType) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(FilterType.entries.toTypedArray(), key = { it.name }) { filter ->
            val isSelected = filter == selectedFilter
            val chipBg by animateColorAsState(
                targetValue = if (isSelected) accentColor.copy(alpha = 0.28f) else Color(0x18FFFFFF),
                animationSpec = MotionSpecs.snappySpring(),
                label = "filter_chip_bg_${filter.name}"
            )
            val chipBorder by animateColorAsState(
                targetValue = if (isSelected) accentColor else Color.Transparent,
                animationSpec = MotionSpecs.snappySpring(),
                label = "filter_chip_border_${filter.name}"
            )
            Box(
                modifier = Modifier
                    .clip(OneUIPillShape)
                    .background(chipBg)
                    .border(
                        width = 1.dp,
                        color = chipBorder,
                        shape = OneUIPillShape
                    )
                    .bouncableClick(pressedScale = 0.94f) { onFilterSelected(filter) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("filter_chip_${filter.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) accentColor else Color(0xBBFFFFFF)
                )
            }
        }
    }
}

@Composable
fun SortBarRow(
    totalCount: Int,
    selectedSort: SortOption,
    accentColor: Color,
    onOpenSortDialog: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SONGS ($totalCount)",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = Color(0x88FFFFFF)
        )

        OneUITooltipButton(
            name = "Sort: ${selectedSort.label}",
            onClick = onOpenSortDialog
        ) {
            Row(
                modifier = Modifier
                    .clip(OneUIPillShape)
                    .background(Color(0x18FFFFFF))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("sort_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Sort Options",
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = selectedSort.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SortOptionsDialog(
    currentSort: SortOption,
    accentColor: Color,
    onSortSelected: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sort Music",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SortOption.entries.forEach { option ->
                    val isSelected = option == currentSort
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(OneUIPillShape)
                            .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color(0x12FFFFFF))
                            .bouncableClick(pressedScale = 0.97f) {
                                onSortSelected(option)
                                onDismiss()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) Color.White else Color(0xDDFFFFFF)
                        )
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
        },
        confirmButton = {},
        dismissButton = {
            Text(
                text = "Close",
                color = accentColor,
                modifier = Modifier
                    .bouncableClick(onClick = onDismiss)
                    .padding(8.dp)
            )
        },
        containerColor = Color(0xFF16182B),
        shape = OneUISquircleLarge
    )
}

@Composable
fun OneUITabBar(
    selectedTab: MusicTab,
    accentColor: Color,
    onTabSelected: (MusicTab) -> Unit,
    tabHighlightColor: Color = accentColor
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(MusicTab.entries.toTypedArray(), key = { it.name }) { tab ->
            val isSelected = tab == selectedTab
            val tabBg by animateColorAsState(
                targetValue = if (isSelected) tabHighlightColor else Color(0x22FFFFFF),
                animationSpec = MotionSpecs.snappySpring(),
                label = "tab_pill_bg_${tab.name}"
            )
            Box(
                modifier = Modifier
                    .clip(OneUIPillShape)
                    .background(tabBg)
                    .bouncableClick(pressedScale = 0.94f) { onTabSelected(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("tab_${tab.name.lowercase()}")
            ) {
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) Color(0xFF0C0E1A) else Color(0xDDFFFFFF)
                )
            }
        }
    }
}

@Composable
fun TracksTabContent(
    tracks: List<Track>,
    currentTrack: Track,
    isPlaying: Boolean,
    accentColor: Color,
    albumArtSize: AlbumArtSize = AlbumArtSize.MEDIUM,
    elementColors: ElementColors? = null,
    listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
    onTrackClick: (Track) -> Unit,
    onPlayFromContext: (List<Track>, Int) -> Unit = { list, idx ->
        if (idx in list.indices) onTrackClick(list[idx])
    },
    onToggleFavorite: (Track) -> Unit,
    onPickLocalFiles: () -> Unit = {},
    onScanDeviceStorage: () -> Unit = {},
    onRemoveLocalTrack: ((Track) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTrackForMenu by remember { mutableStateOf<Track?>(null) }

    selectedTrackForMenu?.let { menuTrack ->
        TrackActionDialog(
            track = menuTrack,
            accentColor = accentColor,
            onPlay = {
                val idx = tracks.indexOfFirst { it.id == menuTrack.id }
                if (idx >= 0) onPlayFromContext(tracks, idx) else onTrackClick(menuTrack)
            },
            onToggleFavorite = { onToggleFavorite(menuTrack) },
            onRemoveFromLibrary = onRemoveLocalTrack?.let { removeCb -> { removeCb(menuTrack) } },
            onDismiss = { selectedTrackForMenu = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 38.dp, top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Featured Hero Banner
            item(key = "featured_hero_banner") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(
                            fadeInSpec = MotionSpecs.snappySpring(),
                            placementSpec = MotionSpecs.LazyItemPlacementSpring,
                            fadeOutSpec = MotionSpecs.snappySpring()
                        )
                        .glassmorphism(
                            shape = OneUISquircleLarge,
                            backgroundColor = Color(0x35232742),
                            borderWidth = 1.2.dp,
                            elevation = 8.dp
                        )
                        .bouncableClick(pressedScale = 0.97f) {
                            val idx = tracks.indexOfFirst { it.id == currentTrack.id }
                            if (idx >= 0) onPlayFromContext(tracks, idx) else onTrackClick(currentTrack)
                        }
                        .padding(18.dp)
                        .testTag("hero_featured_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CoverArtView(
                            track = currentTrack,
                            size = 80.dp,
                            cornerRadius = 20.dp,
                            isPlaying = isPlaying,
                            showVinylPeek = false
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(OneUIPillShape)
                                    .background(accentColor.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "NOW PLAYING",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = accentColor
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = currentTrack.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .basicMarquee(iterations = Int.MAX_VALUE)
                            )
                            Text(
                                text = "${currentTrack.artist} • ${currentTrack.genre}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xAAFFFFFF)
                            )
                        }

                        OneUITooltipButton(
                            name = if (isPlaying) "Pause Current Track" else "Play ${currentTrack.title}",
                            onClick = {
                                val idx = tracks.indexOfFirst { it.id == currentTrack.id }
                                if (idx >= 0) onPlayFromContext(tracks, idx) else onTrackClick(currentTrack)
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedContent(
                                    targetState = isPlaying,
                                    transitionSpec = { MotionSpecs.iconMorphTransition() },
                                    label = "hero_card_play_morph"
                                ) { playing ->
                                    Icon(
                                        imageVector = if (playing) Icons.Default.Equalizer else Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color(0xFF0C0E1A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                TrackListItem(
                    track = track,
                    isCurrentTrack = track.id == currentTrack.id,
                    isPlaying = isPlaying,
                    onTrackClick = { onPlayFromContext(tracks, index) },
                    onToggleFavorite = { onToggleFavorite(track) },
                    onLongClick = { selectedTrackForMenu = track },
                    albumArtSize = albumArtSize,
                    elementColors = elementColors,
                    modifier = Modifier.animateItem(
                        fadeInSpec = MotionSpecs.snappySpring(),
                        placementSpec = MotionSpecs.LazyItemPlacementSpring,
                        fadeOutSpec = MotionSpecs.snappySpring()
                    )
                )
            }
        }

        // Alphabet Quick Scroll Bar on the right edge
        AlphabetIndexer(
            accentColor = accentColor,
            onLetterSelected = { char ->
                val targetIndex = if (char == '#') {
                    tracks.indexOfFirst { it.title.firstOrNull()?.isLetter() != true }
                } else {
                    tracks.indexOfFirst { it.title.startsWith(char, ignoreCase = true) }
                }
                if (targetIndex != -1) {
                    coroutineScope.launch {
                        listState.scrollToItem(targetIndex + 1) // +1 accounts for Hero Banner
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp, top = 8.dp, bottom = 8.dp)
        )
    }
}

@Composable
fun PlaylistsTabContent(
    playlists: List<Playlist>,
    allTracks: List<Track>,
    currentTrack: Track,
    accentColor: Color = Color(0xFF0072DE),
    listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
    onPlayTrack: (Track) -> Unit,
    onPlayFromContext: (List<Track>, Int) -> Unit = { list, idx ->
        if (idx in list.indices) onPlayTrack(list[idx])
    }
) {
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 38.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(playlists, key = { it.id }) { playlist ->
                val playlistTracks = allTracks.filter { playlist.trackIds.contains(it.id) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(
                            fadeInSpec = MotionSpecs.snappySpring(),
                            placementSpec = MotionSpecs.LazyItemPlacementSpring,
                            fadeOutSpec = MotionSpecs.snappySpring()
                        )
                        .glassmorphism(
                            shape = OneUISquircleLarge,
                            backgroundColor = Color(0x35232742),
                            borderWidth = 1.dp
                        )
                        .bouncableClick(pressedScale = 0.97f) {
                            if (playlistTracks.isNotEmpty()) {
                                onPlayFromContext(playlistTracks, 0)
                            }
                        }
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(OneUISquircleMedium)
                                        .background(
                                            Brush.linearGradient(playlist.gradientColors)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = "Playlist",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = playlist.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${playlistTracks.size} Tracks • ${playlist.description}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xAAFFFFFF)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x30FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Playlist",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            playlistTracks.forEachIndexed { idx, track ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bouncableClick(pressedScale = 0.97f) { onPlayFromContext(playlistTracks, idx) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0x77FFFFFF),
                                        modifier = Modifier.width(22.dp)
                                    )
                                    Text(
                                        text = track.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (track.id == currentTrack.id) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (track.id == currentTrack.id) track.accentColor else Color.White,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = formatTime(track.durationMs),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0x77FFFFFF)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        AlphabetIndexer(
            accentColor = accentColor,
            onLetterSelected = { char ->
                val targetIndex = if (char == '#') {
                    playlists.indexOfFirst { it.title.firstOrNull()?.isLetter() != true }
                } else {
                    playlists.indexOfFirst { it.title.startsWith(char, ignoreCase = true) }
                }
                if (targetIndex != -1) {
                    coroutineScope.launch {
                        listState.scrollToItem(targetIndex)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp, top = 8.dp, bottom = 8.dp)
        )
    }
}

@Composable
fun AlbumsTabContent(
    tracks: List<Track>,
    layoutSetting: AlbumLayoutSetting = AlbumLayoutSetting.GRID_2,
    sortOption: SortOption = SortOption.TITLE_AZ,
    accentColor: Color = Color(0xFF0072DE),
    listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
    gridState: LazyGridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() },
    onSelectAlbum: (String) -> Unit = {},
    onSelectTrack: (Track) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val albums = remember(tracks, sortOption) {
        val list = tracks.groupBy { it.album }.map { (albumName, albumTracks) ->
            albumName to albumTracks
        }
        when (sortOption) {
            SortOption.ARTIST -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.second.firstOrNull()?.artist ?: "" })
            SortOption.TITLE_ZA -> list.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.first })
            else -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.first })
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (layoutSetting == AlbumLayoutSetting.LIST) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 38.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(albums, key = { it.first }) { (albumName, albumTracks) ->
                    val leadTrack = albumTracks.first()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(
                                fadeInSpec = MotionSpecs.snappySpring(),
                                placementSpec = MotionSpecs.LazyItemPlacementSpring,
                                fadeOutSpec = MotionSpecs.snappySpring()
                            )
                            .glassmorphism(
                                shape = OneUISquircleMedium,
                                backgroundColor = Color(0x281F223A),
                                borderWidth = 1.dp
                            )
                            .bouncableClick(pressedScale = 0.96f) { onSelectAlbum(albumName) }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CoverArtView(
                                track = leadTrack,
                                size = 60.dp,
                                cornerRadius = 14.dp,
                                isPlaying = false,
                                showVinylPeek = false,
                                artSharedElementKey = "album_art_$albumName"
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = albumName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.appSharedBounds(
                                        key = "album_title_$albumName",
                                        useScaleToBounds = true
                                    )
                                )
                                Text(
                                    text = "${leadTrack.artist} • ${leadTrack.genre}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xCCFFFFFF),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${albumTracks.size} Songs",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0x88FFFFFF)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            val cols = layoutSetting.columns
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(cols),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 38.dp, top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(if (cols == 3) 10.dp else 14.dp),
                verticalArrangement = Arrangement.spacedBy(if (cols == 3) 10.dp else 14.dp)
            ) {
                items(albums, key = { it.first }) { (albumName, albumTracks) ->
                    val leadTrack = albumTracks.first()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(
                                fadeInSpec = MotionSpecs.snappySpring(),
                                placementSpec = MotionSpecs.LazyItemPlacementSpring,
                                fadeOutSpec = MotionSpecs.snappySpring()
                            )
                            .glassmorphism(
                                shape = OneUISquircleLarge,
                                backgroundColor = Color(0x2D21243D),
                                borderWidth = 1.dp
                            )
                            .bouncableClick(pressedScale = 0.95f) { onSelectAlbum(albumName) }
                            .padding(if (cols == 3) 8.dp else 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val targetArtSize = minOf(maxWidth, if (cols == 3) 95.dp else 135.dp)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CoverArtView(
                                    track = leadTrack,
                                    size = targetArtSize,
                                    cornerRadius = if (cols == 3) 16.dp else 22.dp,
                                    isPlaying = false,
                                    showVinylPeek = false,
                                    artSharedElementKey = "album_art_$albumName"
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = albumName,
                                    style = if (cols == 3) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                           else MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .appSharedBounds(
                                            key = "album_title_$albumName",
                                            useScaleToBounds = true
                                        )
                                )
                                Text(
                                    text = "${leadTrack.artist} • ${albumTracks.size} Tracks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xAAFFFFFF),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        AlphabetIndexer(
            accentColor = accentColor,
            onLetterSelected = { char ->
                val targetIndex = if (char == '#') {
                    albums.indexOfFirst { it.first.firstOrNull()?.isLetter() != true }
                } else {
                    albums.indexOfFirst { it.first.startsWith(char, ignoreCase = true) }
                }
                if (targetIndex != -1) {
                    coroutineScope.launch {
                        if (layoutSetting == AlbumLayoutSetting.LIST) {
                            listState.scrollToItem(targetIndex)
                        } else {
                            gridState.scrollToItem(targetIndex)
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp, top = 8.dp, bottom = 8.dp)
        )
    }
}

@Composable
fun ArtistsTabContent(
    tracks: List<Track>,
    sortOption: SortOption = SortOption.TITLE_AZ,
    accentColor: Color = Color(0xFF0072DE),
    gridState: LazyGridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() },
    onSelectArtist: (String) -> Unit = {},
    onSelectTrack: (Track) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val artists = remember(tracks, sortOption) {
        val list = tracks.groupBy { it.artist }.map { (artistName, artistTracks) ->
            artistName to artistTracks
        }
        when (sortOption) {
            SortOption.TITLE_ZA -> list.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.first })
            else -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.first })
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 38.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(artists, key = { it.first }) { (artistName, artistTracks) ->
                val lead = artistTracks.first()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(
                            fadeInSpec = MotionSpecs.snappySpring(),
                            placementSpec = MotionSpecs.LazyItemPlacementSpring,
                            fadeOutSpec = MotionSpecs.snappySpring()
                        )
                        .glassmorphism(
                            shape = OneUISquircleLarge,
                            backgroundColor = Color(0x2D21243D),
                            borderWidth = 1.dp
                        )
                        .bouncableClick(pressedScale = 0.95f) { onSelectArtist(artistName) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val targetArtSize = minOf(maxWidth, 135.dp)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CoverArtView(
                                track = lead,
                                size = targetArtSize,
                                cornerRadius = 22.dp,
                                isPlaying = false,
                                showVinylPeek = false,
                                artSharedElementKey = "artist_art_$artistName"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = artistName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .appSharedBounds(
                                        key = "artist_title_$artistName",
                                        useScaleToBounds = true
                                    )
                            )
                            Text(
                                text = "${lead.genre} • ${artistTracks.size} Tracks",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xAAFFFFFF),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        AlphabetIndexer(
            accentColor = accentColor,
            onLetterSelected = { char ->
                val targetIndex = if (char == '#') {
                    artists.indexOfFirst { it.first.firstOrNull()?.isLetter() != true }
                } else {
                    artists.indexOfFirst { it.first.startsWith(char, ignoreCase = true) }
                }
                if (targetIndex != -1) {
                    coroutineScope.launch {
                        gridState.scrollToItem(targetIndex)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp, top = 8.dp, bottom = 8.dp)
        )
    }
}

@Composable
fun FavoritesTabContent(
    tracks: List<Track>,
    currentTrack: Track,
    isPlaying: Boolean,
    accentColor: Color = Color(0xFF0072DE),
    albumArtSize: AlbumArtSize = AlbumArtSize.MEDIUM,
    elementColors: ElementColors? = null,
    listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
    onTrackClick: (Track) -> Unit,
    onPlayFromContext: (List<Track>, Int) -> Unit = { list, idx ->
        if (idx in list.indices) onTrackClick(list[idx])
    },
    onToggleFavorite: (Track) -> Unit,
    onRemoveLocalTrack: ((Track) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTrackForMenu by remember { mutableStateOf<Track?>(null) }

    selectedTrackForMenu?.let { menuTrack ->
        TrackActionDialog(
            track = menuTrack,
            accentColor = accentColor,
            onPlay = {
                val idx = tracks.indexOfFirst { it.id == menuTrack.id }
                if (idx >= 0) onPlayFromContext(tracks, idx) else onTrackClick(menuTrack)
            },
            onToggleFavorite = { onToggleFavorite(menuTrack) },
            onRemoveFromLibrary = onRemoveLocalTrack?.let { removeCb -> { removeCb(menuTrack) } },
            onDismiss = { selectedTrackForMenu = null }
        )
    }

    if (tracks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "No Favorites",
                    tint = Color(0x55FFFFFF),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No favorite tracks yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xAAFFFFFF)
                )
                Text(
                    text = "Tap the heart icon on any track to add it here",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0x77FFFFFF)
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 38.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                    TrackListItem(
                        track = track,
                        isCurrentTrack = track.id == currentTrack.id,
                        isPlaying = isPlaying,
                        onTrackClick = { onPlayFromContext(tracks, index) },
                        onToggleFavorite = { onToggleFavorite(track) },
                        onLongClick = { selectedTrackForMenu = track },
                        albumArtSize = albumArtSize,
                        elementColors = elementColors,
                        modifier = Modifier.animateItem(
                            fadeInSpec = MotionSpecs.snappySpring(),
                            placementSpec = MotionSpecs.LazyItemPlacementSpring,
                            fadeOutSpec = MotionSpecs.snappySpring()
                        )
                    )
                }
            }

            AlphabetIndexer(
                accentColor = accentColor,
                onLetterSelected = { char ->
                    val targetIndex = if (char == '#') {
                        tracks.indexOfFirst { it.title.firstOrNull()?.isLetter() != true }
                    } else {
                        tracks.indexOfFirst { it.title.startsWith(char, ignoreCase = true) }
                    }
                    if (targetIndex != -1) {
                        coroutineScope.launch {
                            listState.scrollToItem(targetIndex)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp, top = 8.dp, bottom = 8.dp)
            )
        }
    }
}
