package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AlbumsTabContent
import com.example.ArtistsTabContent
import com.example.FavoritesTabContent
import com.example.FilterChipsRow
import com.example.OneUITabBar
import com.example.PlaylistsTabContent
import com.example.TracksTabContent
import com.example.data.AnimatedArtRepository
import com.example.data.AnimatedCoverMedia
import com.example.model.AlbumArtSize
import com.example.model.AlbumLayoutSetting
import com.example.model.DynamicThemeEngine
import com.example.model.ElementColors
import com.example.model.FilterType
import com.example.model.MusicTab
import com.example.model.Playlist
import com.example.model.ProgressBarDesign
import com.example.model.ProgressBarThickness
import com.example.model.SortOption
import com.example.model.Track
import com.example.ui.theme.ExpressivePalette
import com.example.ui.theme.LocalAnimatedVisibilityScope
import com.example.ui.theme.MotionSpecs
import com.example.ui.theme.NavigationTransitions
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.OneUISquircleLarge
import com.example.ui.theme.bouncableClick
import com.example.ui.theme.glassmorphism
import com.example.ui.theme.parseHexColor
import kotlin.coroutines.cancellation.CancellationException

private data class CoverLandscapeDetailState(
    val depth: Int,
    val albumId: String?,
    val artistId: String?
)

/**
 * Custom Horizon Deck layout tailored specifically for a Foldable Cover Screen in Landscape orientation
 * (wide, height-constrained viewport where unfolded inner Dual-Pane should NOT be active).
 *
 * Left Side: Ergonomic Quick Playback Deck with hero (optionally animated) cover art, marquee title,
 * expressive scrubber, and quick-action bar. Tapping cover opens fullscreen Now Playing.
 * Right Side: Full Library browser, tabs, search, filters, and nested Album/Artist detail views.
 */
@Composable
fun CoverLandscapeLayout(
    currentTrack: Track,
    isPlaying: Boolean,
    currentPositionMs: Long,
    tracks: List<Track>,
    allTracks: List<Track>,
    favoriteTracks: List<Track>,
    playlists: List<Playlist>,
    selectedTab: MusicTab,
    searchQuery: String,
    selectedAlbumId: String?,
    selectedArtistId: String?,
    palette: ExpressivePalette,
    isBluetoothConnected: Boolean,
    connectedDeviceName: String?,
    albumLayoutSetting: AlbumLayoutSetting,
    albumArtSize: AlbumArtSize,
    progressBarDesign: ProgressBarDesign,
    progressBarThickness: ProgressBarThickness,
    activeFilter: FilterType,
    sortOption: SortOption,
    elementColors: ElementColors,
    themeEngine: DynamicThemeEngine,
    useCustomElementColors: Boolean,
    enableAnimatedCovers: Boolean,
    autoFetchAnimatedCoversWifiOnly: Boolean,
    simulateMotionOnStaticCovers: Boolean,
    tabListStates: Map<MusicTab, LazyListState>,
    tracksListState: LazyListState,
    albumsGridState: LazyGridState,
    artistsGridState: LazyGridState,
    albumDetailListState: LazyListState,
    artistDetailListState: LazyListState,
    artistDetailAlbumsRowState: LazyListState,
    onTabSelected: (MusicTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectAlbumForDetail: (String?) -> Unit,
    onSelectArtistForDetail: (String?) -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlayFromContext: (List<Track>, Int) -> Unit,
    onShuffleFromContext: (List<Track>) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onExpandPlayer: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenSettings: () -> Unit,
    onFilterSelected: (FilterType) -> Unit,
    onOpenSortDialog: () -> Unit,
    onPickLocalFiles: () -> Unit,
    onScanDeviceStorage: () -> Unit,
    onImportAnimatedCover: (Uri) -> Unit,
    onRemoveCustomAnimatedCover: () -> Unit,
    onRemoveLocalTrack: ((Track) -> Unit)? = null,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSearchExpanded by remember { mutableStateOf(searchQuery.isNotEmpty()) }
    var isDraggingSlider by remember(currentTrack.id) { mutableStateOf(false) }
    var sliderDragPosition by remember(currentTrack.id) { mutableFloatStateOf(0f) }
    var showAnimatedCoverDialog by remember { mutableStateOf(false) }

    val animatedCoverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportAnimatedCover(uri)
        }
    }

    val isSubscreen = selectedAlbumId != null || selectedArtistId != null || isSearchExpanded
    PredictiveBackHandler(enabled = isSubscreen) { progress ->
        try {
            progress.collect { }
            when {
                selectedAlbumId != null -> onSelectAlbumForDetail(null)
                selectedArtistId != null -> onSelectArtistForDetail(null)
                isSearchExpanded -> {
                    onSearchQueryChange("")
                    isSearchExpanded = false
                }
            }
        } catch (_: CancellationException) {}
    }

    PredictiveBackHandler(enabled = !isSubscreen) { progress ->
        try {
            progress.collect { }
            onBackPress()
        } catch (_: CancellationException) {}
    }

    val animatedPrimary by animateColorAsState(
        targetValue = currentTrack.primaryColor,
        animationSpec = MotionSpecs.expressiveSpring(),
        label = "cl_primary"
    )
    val animatedAccent by animateColorAsState(
        targetValue = currentTrack.accentColor,
        animationSpec = MotionSpecs.expressiveSpring(),
        label = "cl_accent"
    )

    val activePositionMs = if (isDraggingSlider) {
        (sliderDragPosition * currentTrack.durationMs).toLong()
    } else {
        currentPositionMs
    }

    val animatedArtCacheVersion by AnimatedArtRepository.cacheVersion.collectAsStateWithLifecycle()

    if (showAnimatedCoverDialog) {
        val hasCustom = remember(currentTrack.id, currentTrack.artist, currentTrack.album, currentTrack.title, animatedArtCacheVersion) {
            AnimatedArtRepository.getCachedOrSidecarImmediate(context, currentTrack) != AnimatedCoverMedia.None
        }
        AnimatedCoverOptionsDialog(
            track = currentTrack,
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

    Row(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("cover_landscape_layout"),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ============================================================
        // LEFT PANEL: Compact Cover Horizon Playback Deck (~36% width)
        // ============================================================
        Column(
            modifier = Modifier
                .weight(0.38f)
                .widthIn(min = 228.dp, max = 310.dp)
                .fillMaxHeight()
                .glassmorphism(
                    shape = OneUISquircleLarge,
                    backgroundColor = Color(0x3614172B),
                    borderWidth = 1.2.dp,
                    elevation = 10.dp
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            animatedPrimary.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        radius = 480f
                    ),
                    shape = OneUISquircleLarge
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Deck Header Bar: Brand Pill + Compact Utility Buttons (48dp touch targets)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(OneUIPillShape)
                        .background(Color(0x22FFFFFF))
                        .bouncableClick(pressedScale = 0.94f) { onOpenBluetooth() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("cover_landscape_bt_pill")
                ) {
                    Icon(
                        imageVector = if (isBluetoothConnected) Icons.Default.BluetoothConnected else Icons.Default.MusicNote,
                        contentDescription = if (isBluetoothConnected) "Bluetooth Audio" else "OneMusic",
                        tint = if (isBluetoothConnected) Color(0xFF00E5FF) else animatedAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isBluetoothConnected && connectedDeviceName != null) connectedDeviceName else "OneMusic",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 86.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("cover_landscape_search_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onOpenEqualizer,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("cover_landscape_eq_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Equalizer",
                            tint = animatedAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("cover_landscape_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Center Adaptive Cover Art + Track Metadata
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val deckArtSize = minOf(maxWidth * 0.62f, maxHeight * 0.56f).coerceIn(72.dp, 138.dp)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CoverArtView(
                        track = currentTrack,
                        size = deckArtSize,
                        cornerRadius = (deckArtSize.value * 0.16f).dp,
                        isPlaying = isPlaying,
                        showVinylPeek = false,
                        animatePlaybackScale = true,
                        enableAnimatedCover = enableAnimatedCovers,
                        autoFetchWifiOnly = autoFetchAnimatedCoversWifiOnly,
                        simulateMotionOnStaticCovers = simulateMotionOnStaticCovers,
                        onClick = onExpandPlayer,
                        onLongPress = { showAnimatedCoverDialog = true },
                        modifier = Modifier.testTag("cover_landscape_artwork")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bouncableClick(pressedScale = 0.97f, onClick = onExpandPlayer),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentTrack.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .basicMarquee(iterations = Int.MAX_VALUE)
                            )
                            Text(
                                text = "${currentTrack.artist} • ${currentTrack.album}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xBBFFFFFF),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .bouncableClick(pressedScale = 0.90f) { onToggleFavorite(currentTrack.id) }
                                .testTag("cover_landscape_fav_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = currentTrack.isFavorite,
                                transitionSpec = { MotionSpecs.iconMorphTransition() },
                                label = "cover_landscape_fav_morph"
                            ) { isFav ->
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Toggle Favorite",
                                    tint = if (isFav) Color(0xFFFF4081) else Color(0xCCFFFFFF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Deck Controls: Compact Scrubber + Transport Row
            Column(modifier = Modifier.fillMaxWidth()) {
                ExpressiveScrubber(
                    progress = if (currentTrack.durationMs > 0) {
                        (activePositionMs.toFloat() / currentTrack.durationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f,
                    onProgressChange = { fraction ->
                        isDraggingSlider = true
                        sliderDragPosition = fraction
                    },
                    onProgressChangeFinished = {
                        isDraggingSlider = false
                        onSeek((sliderDragPosition * currentTrack.durationMs).toLong())
                    },
                    design = progressBarDesign,
                    thickness = progressBarThickness,
                    accentColor = animatedAccent,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxWidth()
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
                        text = formatTime(currentTrack.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xAAFFFFFF)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .glassmorphism(shape = CircleShape, backgroundColor = Color(0x26FFFFFF))
                            .bouncableClick(pressedScale = 0.92f, onClick = onPrevious)
                            .testTag("cover_landscape_prev_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 68.dp, height = 48.dp)
                            .clip(OneUIPillShape)
                            .background(animatedPrimary)
                            .bouncableClick(pressedScale = 0.92f, onClick = onTogglePlayPause)
                            .testTag("cover_landscape_play_pause"),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = isPlaying,
                            transitionSpec = { MotionSpecs.iconMorphTransition() },
                            label = "cover_landscape_play_morph"
                        ) { playing ->
                            Icon(
                                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playing) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .glassmorphism(shape = CircleShape, backgroundColor = Color(0x26FFFFFF))
                            .bouncableClick(pressedScale = 0.92f, onClick = onNext)
                            .testTag("cover_landscape_next_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Track",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // ============================================================
        // RIGHT PANEL: Compact Horizon Library & Detail Browser
        // ============================================================
        Column(
            modifier = Modifier
                .weight(0.62f)
                .fillMaxHeight()
                .glassmorphism(
                    shape = OneUISquircleLarge,
                    backgroundColor = Color(0x28171A2E),
                    borderWidth = 1.dp
                )
                .padding(top = 8.dp)
        ) {
            val detailState = when {
                selectedAlbumId != null -> CoverLandscapeDetailState(2, selectedAlbumId, selectedArtistId)
                selectedArtistId != null -> CoverLandscapeDetailState(1, null, selectedArtistId)
                else -> CoverLandscapeDetailState(0, null, null)
            }

            AnimatedContent(
                targetState = detailState,
                transitionSpec = {
                    NavigationTransitions.hierarchicalContentTransform(
                        scope = this,
                        isForward = targetState.depth >= initialState.depth
                    )
                },
                label = "CoverLandscapeHierarchy",
                modifier = Modifier.fillMaxSize()
            ) { state ->
                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@AnimatedContent) {
                if (state.albumId != null) {
                    val albumTracks = remember(state.albumId, allTracks) {
                        allTracks
                            .filter { it.album.equals(state.albumId, ignoreCase = true) }
                            .sortedWith(com.example.model.AlbumTrackOrderComparator)
                    }
                    AlbumDetailScreen(
                        albumName = state.albumId,
                        tracks = albumTracks,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        accentColor = palette.accent,
                        albumArtSize = albumArtSize,
                        elementColors = elementColors,
                        listState = albumDetailListState,
                        bottomContentPadding = 12.dp,
                        enableBackHandler = false,
                        onBack = { onSelectAlbumForDetail(null) },
                        onPlayFromContext = onPlayFromContext,
                        onShuffleFromContext = onShuffleFromContext,
                        onToggleFavorite = { onToggleFavorite(it.id) }
                    )
                } else if (state.artistId != null) {
                    val artistTracks = remember(state.artistId, allTracks) {
                        allTracks.filter { it.artist.equals(state.artistId, ignoreCase = true) }
                    }
                    ArtistDetailScreen(
                        artistName = state.artistId,
                        tracks = artistTracks,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        accentColor = palette.accent,
                        albumArtSize = albumArtSize,
                        elementColors = elementColors,
                        listState = artistDetailListState,
                        albumsRowState = artistDetailAlbumsRowState,
                        bottomContentPadding = 12.dp,
                        enableBackHandler = false,
                        onBack = { onSelectArtistForDetail(null) },
                        onSelectAlbum = { onSelectAlbumForDetail(it) },
                        onPlayFromContext = onPlayFromContext,
                        onShuffleFromContext = onShuffleFromContext,
                        onToggleFavorite = { onToggleFavorite(it.id) }
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        val tabHighlightColor = if (themeEngine == DynamicThemeEngine.INDIVIDUAL_ELEMENTS || useCustomElementColors) {
                            parseHexColor(elementColors.tabHighlight) ?: palette.accent
                        } else {
                            palette.accent
                        }

                        // Compact Top Control Row: Tabs + Sort Trigger
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OneUITabBar(
                                    selectedTab = selectedTab,
                                    accentColor = palette.accent,
                                    tabHighlightColor = tabHighlightColor,
                                    onTabSelected = onTabSelected
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(OneUIPillShape)
                                    .background(Color(0x22FFFFFF))
                                    .bouncableClick(pressedScale = 0.94f, onClick = onOpenSortDialog)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("cover_landscape_sort_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Sort",
                                        tint = palette.accent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = sortOption.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = isSearchExpanded) {
                            TextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                placeholder = { Text("Search songs, artists, genres...", color = Color(0x88FFFFFF)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                                    .glassmorphism(shape = OneUIPillShape, backgroundColor = Color(0x331F213A))
                                    .testTag("cover_landscape_search_input"),
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
                                        IconButton(onClick = { onSearchQueryChange("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                                        }
                                    }
                                }
                            )
                        }

                        FilterChipsRow(
                            selectedFilter = activeFilter,
                            accentColor = palette.accent,
                            onFilterSelected = onFilterSelected
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = { NavigationTransitions.tabFadeThrough() },
                                label = "CoverLandscapeTabFadeThrough",
                                modifier = Modifier.fillMaxSize()
                            ) { tab ->
                                when (tab) {
                                    MusicTab.TRACKS -> {
                                        TracksTabContent(
                                            tracks = tracks,
                                            currentTrack = currentTrack,
                                            isPlaying = isPlaying,
                                            accentColor = palette.accent,
                                            albumArtSize = albumArtSize,
                                            elementColors = elementColors,
                                            listState = tabListStates[MusicTab.TRACKS] ?: tracksListState,
                                            onTrackClick = onTrackClick,
                                            onPlayFromContext = onPlayFromContext,
                                            onToggleFavorite = { onToggleFavorite(it.id) },
                                            onPickLocalFiles = onPickLocalFiles,
                                            onScanDeviceStorage = onScanDeviceStorage,
                                            onRemoveLocalTrack = onRemoveLocalTrack
                                        )
                                    }
                                    MusicTab.PLAYLISTS -> {
                                        PlaylistsTabContent(
                                            playlists = playlists,
                                            allTracks = allTracks,
                                            currentTrack = currentTrack,
                                            accentColor = palette.accent,
                                            listState = tabListStates[MusicTab.PLAYLISTS]
                                                ?: rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
                                            onPlayTrack = onTrackClick,
                                            onPlayFromContext = onPlayFromContext
                                        )
                                    }
                                    MusicTab.ALBUMS -> {
                                        AlbumsTabContent(
                                            tracks = allTracks,
                                            layoutSetting = albumLayoutSetting,
                                            sortOption = sortOption,
                                            accentColor = palette.accent,
                                            listState = tabListStates[MusicTab.ALBUMS]
                                                ?: rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
                                            gridState = albumsGridState,
                                            onSelectAlbum = onSelectAlbumForDetail,
                                            onSelectTrack = onTrackClick
                                        )
                                    }
                                    MusicTab.ARTISTS -> {
                                        ArtistsTabContent(
                                            tracks = allTracks,
                                            sortOption = sortOption,
                                            accentColor = palette.accent,
                                            gridState = artistsGridState,
                                            onSelectArtist = onSelectArtistForDetail,
                                            onSelectTrack = onTrackClick
                                        )
                                    }
                                    MusicTab.FAVORITES -> {
                                        FavoritesTabContent(
                                            tracks = favoriteTracks,
                                            currentTrack = currentTrack,
                                            isPlaying = isPlaying,
                                            accentColor = palette.accent,
                                            albumArtSize = albumArtSize,
                                            elementColors = elementColors,
                                            listState = tabListStates[MusicTab.FAVORITES]
                                                ?: rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
                                            onTrackClick = onTrackClick,
                                            onPlayFromContext = onPlayFromContext,
                                            onToggleFavorite = { onToggleFavorite(it.id) },
                                            onRemoveLocalTrack = onRemoveLocalTrack
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }
}
