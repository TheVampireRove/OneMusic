package com.example.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.audio.AudioPlaybackManager
import com.example.data.AnimatedArtRepository
import com.example.data.LocalMediaManager
import com.example.data.LocalTracksCache
import com.example.data.LyricsRepository
import com.example.data.LyricsStatusSummary
import com.example.data.MusicRepository
import com.example.model.AppSettings
import com.example.model.FilterType
import com.example.model.MusicTab
import com.example.model.Playlist
import com.example.model.RepeatMode
import com.example.model.SortOption
import com.example.model.SoundAlivePreset
import com.example.model.Track
import com.example.ui.components.DisplayMode
import com.example.worker.LyricsSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LyricsSyncUiState(
    val isRunning: Boolean = false,
    val isWaitingForNetwork: Boolean = false,
    val current: Int = 0,
    val total: Int = 0,
    val foundInRun: Int = 0,
    val failedInRun: Int = 0,
    val currentTrackTitle: String = "",
    val lastRunCompleted: Boolean = false,
    val statusSummary: LyricsStatusSummary = LyricsStatusSummary(0, 0, 0, 0, 0)
)

data class ImportSummaryBanner(
    val addedCount: Int,
    val skippedDuplicates: Int,
    val consolidatedDuplicates: Int = 0,
    val importedTrackIds: List<String> = emptyList(),
    val summaryLabel: String,
    val isFileImport: Boolean = true
)

class MusicPlayerViewModel(
    val repository: MusicRepository = MusicRepository.getInstance()
) : ViewModel() {

    private val playbackManager = AudioPlaybackManager.getInstance()

    val tracks: StateFlow<List<Track>> = repository.tracks
    val playlists: StateFlow<List<Playlist>> = repository.playlists
    val appSettings: StateFlow<AppSettings> = repository.settings

    val playbackQueue: StateFlow<List<Track>> = playbackManager.playbackQueue
    val isPlaying: StateFlow<Boolean> = playbackManager.isPlaying
    val currentPositionMs: StateFlow<Long> = playbackManager.currentPositionMs
    val visualizerAmplitudes: StateFlow<List<Float>> = playbackManager.visualizerAmplitudes
    val currentTrack: StateFlow<Track> = playbackManager.currentTrack

    val isShuffleActive: StateFlow<Boolean> = playbackManager.isShuffleActive
    val repeatMode: StateFlow<RepeatMode> = playbackManager.repeatMode

    private val _selectedTab = MutableStateFlow(repository.settings.value.startupTab)
    val selectedTab: StateFlow<MusicTab> = _selectedTab.asStateFlow()

    // Hoisted detail navigation IDs so active Album/Artist details survive orientation, folding, and layout switches
    private val _selectedAlbumId = MutableStateFlow<String?>(null)
    val selectedAlbumId: StateFlow<String?> = _selectedAlbumId.asStateFlow()

    private val _selectedArtistId = MutableStateFlow<String?>(null)
    val selectedArtistId: StateFlow<String?> = _selectedArtistId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(repository.settings.value.sortOption)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _activeFilter = MutableStateFlow(repository.settings.value.activeFilter)
    val activeFilter: StateFlow<FilterType> = _activeFilter.asStateFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _volume = MutableStateFlow(0.85f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isEqualizerOpen = MutableStateFlow(false)
    val isEqualizerOpen: StateFlow<Boolean> = _isEqualizerOpen.asStateFlow()

    private val _isSleepTimerOpen = MutableStateFlow(false)
    val isSleepTimerOpen: StateFlow<Boolean> = _isSleepTimerOpen.asStateFlow()

    private val _isFlexModeActive = MutableStateFlow(false)
    val isFlexModeActive: StateFlow<Boolean> = _isFlexModeActive.asStateFlow()

    // Hoisted player display mode so Artwork / Lyrics / Visualizer persists smoothly across postures & tabletop mode
    private val _displayMode = MutableStateFlow(DisplayMode.ARTWORK)
    val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()

    private val _sleepTimerMinutesLeft = MutableStateFlow<Int?>(null)
    val sleepTimerMinutesLeft: StateFlow<Int?> = _sleepTimerMinutesLeft.asStateFlow()

    private val _soundPreset = MutableStateFlow(repository.settings.value.defaultSoundPreset)
    val soundPreset: StateFlow<SoundAlivePreset> = _soundPreset.asStateFlow()

    private val _bassGain = MutableStateFlow(0f)
    val bassGain: StateFlow<Float> = _bassGain.asStateFlow()

    private val _midGain = MutableStateFlow(0f)
    val midGain: StateFlow<Float> = _midGain.asStateFlow()

    private val _trebleGain = MutableStateFlow(0f)
    val trebleGain: StateFlow<Float> = _trebleGain.asStateFlow()

    private val _dolbyAtmosEnabled = MutableStateFlow(true)
    val dolbyAtmosEnabled: StateFlow<Boolean> = _dolbyAtmosEnabled.asStateFlow()

    private val _isLoadingLyrics = MutableStateFlow(false)
    val isLoadingLyrics: StateFlow<Boolean> = _isLoadingLyrics.asStateFlow()

    private val _lyricsSyncUiState = MutableStateFlow(LyricsSyncUiState())
    val lyricsSyncUiState: StateFlow<LyricsSyncUiState> = _lyricsSyncUiState.asStateFlow()

    private val _recentImportBanner = MutableStateFlow<ImportSummaryBanner?>(null)
    val recentImportBanner: StateFlow<ImportSummaryBanner?> = _recentImportBanner.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var lyricsFetchJob: Job? = null
    private var hasLoadedCache = false
    private val attemptedLyricsIds = mutableSetOf<String>()

    // Preserved scroll position across Cover Screen, Dual Pane, Tabletop, and screen rotations
    var trackListIndex: Int = 0
    var trackListOffset: Int = 0

    fun updateTrackListScroll(index: Int, offset: Int) {
        trackListIndex = index
        trackListOffset = offset
    }

    // Independent of _selectedTab so switching tabs never re-emits or collapses scroll positions
    val filteredTracks: StateFlow<List<Track>> = combine(
        tracks, _searchQuery, _sortOption, _activeFilter
    ) { list, query, sort, filter ->
        applyFiltersAndSort(list, query, sort, filter)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, repository.tracks.value)

    val favoriteTracks: StateFlow<List<Track>> = combine(
        tracks, _searchQuery, _sortOption, _activeFilter
    ) { list, query, sort, filter ->
        applyFiltersAndSort(list.filter { it.isFavorite }, query, sort, filter)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, repository.tracks.value.filter { it.isFavorite })

    private fun applyFiltersAndSort(
        source: List<Track>,
        query: String,
        sort: SortOption,
        filter: FilterType
    ): List<Track> {
        var result = when (filter) {
            FilterType.ALL -> source
            FilterType.FAVORITES -> source.filter { it.isFavorite }
            FilterType.LOCAL_ONLY -> source.filter { it.isLocalFile }
            FilterType.SYNTHWAVE -> source.filter { it.genre.contains("Synthwave", ignoreCase = true) }
            FilterType.LOFI -> source.filter { it.genre.contains("Lo-Fi", ignoreCase = true) }
            FilterType.ELECTRONIC -> source.filter { it.genre.contains("Electronic", ignoreCase = true) }
            FilterType.AMBIENT -> source.filter { it.genre.contains("Ambient", ignoreCase = true) }
            FilterType.KPOP -> source.filter { it.genre.contains("K-Pop", ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            result = result.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true) ||
                    it.album.contains(query, ignoreCase = true) ||
                    it.genre.contains(query, ignoreCase = true)
            }
        }

        return when (sort) {
            SortOption.TITLE_AZ -> result.sortedBy { it.title.lowercase() }
            SortOption.TITLE_ZA -> result.sortedByDescending { it.title.lowercase() }
            SortOption.ARTIST -> result.sortedWith(compareBy({ it.artist.lowercase() }, { it.title.lowercase() }))
            SortOption.ALBUM -> result.sortedWith(compareBy({ it.album.lowercase() }, { it.title.lowercase() }))
            SortOption.DURATION -> result.sortedByDescending { it.durationMs }
            SortOption.DATE_ADDED -> result.asReversed()
        }
    }

    fun playFromContext(tracks: List<Track>, startIndex: Int) {
        playbackManager.playFromContext(tracks, startIndex)
    }

    fun playFromContextShuffled(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        playbackManager.setShuffleEnabled(true)
        playbackManager.playFromContext(tracks, 0)
    }

    fun playTrack(track: Track) {
        playbackManager.playTrack(track)
    }

    fun togglePlayPause() {
        playbackManager.togglePlayPause()
    }

    fun nextTrack() {
        playbackManager.nextTrack()
    }

    fun previousTrack() {
        playbackManager.previousTrack()
    }

    fun seekTo(positionMs: Long) {
        playbackManager.seekTo(positionMs)
    }

    fun setVolume(vol: Float) {
        _volume.value = vol
        playbackManager.audioEngine.setVolume(vol)
    }

    fun setPlaybackSpeed(spd: Float) {
        _playbackSpeed.value = spd
        playbackManager.audioEngine.setPlaybackSpeed(spd)
    }

    fun toggleShuffle() {
        playbackManager.toggleShuffle()
    }

    fun setShuffleEnabled(enabled: Boolean) {
        playbackManager.setShuffleEnabled(enabled)
    }

    fun cycleRepeatMode() {
        playbackManager.cycleRepeatMode()
    }

    fun toggleFavorite(trackId: String) {
        repository.toggleFavorite(trackId)
    }

    fun selectTab(tab: MusicTab) {
        _selectedTab.value = tab
    }

    fun selectAlbumForDetail(albumId: String?) {
        _selectedAlbumId.value = albumId
    }

    fun selectArtistForDetail(artistId: String?) {
        _selectedArtistId.value = artistId
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        repository.updateSettings { it.copy(sortOption = option) }
    }

    fun setActiveFilter(filter: FilterType) {
        _activeFilter.value = filter
        repository.updateSettings { it.copy(activeFilter = filter) }
    }

    fun expandPlayer() {
        _isPlayerExpanded.value = true
    }

    fun collapsePlayer() {
        _isPlayerExpanded.value = false
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        repository.updateSettings(transform)
    }

    fun openEqualizer() {
        _isEqualizerOpen.value = true
    }

    fun closeEqualizer() {
        _isEqualizerOpen.value = false
    }

    fun openSleepTimer() {
        _isSleepTimerOpen.value = true
    }

    fun closeSleepTimer() {
        _isSleepTimerOpen.value = false
    }

    fun toggleFlexMode() {
        _isFlexModeActive.value = !_isFlexModeActive.value
        if (_isFlexModeActive.value) {
            _isPlayerExpanded.value = true
        }
    }

    fun setFlexMode(active: Boolean) {
        _isFlexModeActive.value = active
        if (active) {
            _isPlayerExpanded.value = true
        }
    }

    fun setDisplayMode(mode: DisplayMode) {
        _displayMode.value = mode
    }

    fun ensureActivePlayback(context: Context) {
        initContext(context)
        playbackManager.ensureActivePlayer()
    }

    fun importAnimatedCover(context: Context, track: Track, uri: Uri) {
        val appCtx = context.applicationContext
        initContext(appCtx)
        viewModelScope.launch {
            AnimatedArtRepository.importAnimatedCover(appCtx, track, uri)
        }
    }

    fun removeCustomAnimatedCover(context: Context, track: Track) {
        val appCtx = context.applicationContext
        initContext(appCtx)
        viewModelScope.launch {
            AnimatedArtRepository.removeCustomCover(appCtx, track)
        }
    }

    fun clearAnimatedCoverCache(context: Context, onCleared: (Int) -> Unit = {}) {
        val appCtx = context.applicationContext
        viewModelScope.launch {
            val count = AnimatedArtRepository.clearCache(appCtx)
            onCleared(count)
        }
    }

    fun refreshVisualizerPermission() {
        playbackManager.refreshVisualizerPermission()
    }

    fun initContext(context: Context) {
        val appCtx = context.applicationContext
        repository.initContext(appCtx)
        playbackManager.initContext(appCtx)
        if (!hasLoadedCache) {
            hasLoadedCache = true
            val settings = repository.settings.value
            _selectedTab.value = settings.startupTab
            _sortOption.value = settings.sortOption
            _activeFilter.value = settings.activeFilter
            _soundPreset.value = settings.defaultSoundPreset

            // Observe active track transitions and automatically trigger 3-tier lyrics lookup when needed
            viewModelScope.launch {
                currentTrack.collect { track ->
                    maybeResolveLyrics(appCtx, track)
                }
            }

            // Backfill missing track/disc numbers for tracks cached before track-number support existed
            viewModelScope.launch(Dispatchers.IO) {
                val currentList = repository.tracks.value
                if (currentList.any { it.isLocalFile && it.trackNumber <= 0 }) {
                    val updates = LocalMediaManager.backfillMissingTrackNumbers(appCtx, currentList)
                    if (updates.isNotEmpty() && repository.applyTrackNumberUpdates(updates)) {
                        LocalTracksCache.saveTracks(appCtx, repository.tracks.value)
                    }
                }
            }

            // Keep lyrics cache status summary up to date as library changes
            viewModelScope.launch(Dispatchers.IO) {
                tracks.collect { list ->
                    val summary = LyricsRepository.getStatusSummary(appCtx, list)
                    _lyricsSyncUiState.value = _lyricsSyncUiState.value.copy(statusSummary = summary)
                }
            }

            // Observe WorkManager batch lyrics sync progress
            viewModelScope.launch {
                try {
                    WorkManager.getInstance(appCtx)
                        .getWorkInfosForUniqueWorkFlow(LyricsSyncWorker.UNIQUE_WORK_NAME)
                        .collect { workInfos ->
                            val active = workInfos.firstOrNull { !it.state.isFinished } ?: workInfos.firstOrNull()
                            val summary = LyricsRepository.getStatusSummary(appCtx, repository.tracks.value)
                            if (active == null) {
                                _lyricsSyncUiState.value = _lyricsSyncUiState.value.copy(
                                    isRunning = false,
                                    isWaitingForNetwork = false,
                                    statusSummary = summary
                                )
                            } else {
                                val isEnqueued = active.state == WorkInfo.State.ENQUEUED || active.state == WorkInfo.State.BLOCKED
                                val isRunningNow = active.state == WorkInfo.State.RUNNING
                                val data = if (active.state.isFinished) active.outputData else active.progress
                                val current = data.getInt(LyricsSyncWorker.KEY_CURRENT, 0)
                                val total = data.getInt(LyricsSyncWorker.KEY_TOTAL, 0)
                                val found = data.getInt(LyricsSyncWorker.KEY_FOUND, 0)
                                val failed = data.getInt(LyricsSyncWorker.KEY_FAILED, 0)
                                val title = active.progress.getString(LyricsSyncWorker.KEY_TRACK_TITLE).orEmpty()
                                _lyricsSyncUiState.value = LyricsSyncUiState(
                                    isRunning = isEnqueued || isRunningNow,
                                    isWaitingForNetwork = isEnqueued,
                                    current = current,
                                    total = total,
                                    foundInRun = found,
                                    failedInRun = failed,
                                    currentTrackTitle = title,
                                    lastRunCompleted = active.state == WorkInfo.State.SUCCEEDED,
                                    statusSummary = summary
                                )
                            }
                        }
                } catch (_: IllegalStateException) {
                    // WorkManager is optional in isolated unit tests that don't initialize WorkManagerTestInitHelper
                }
            }
        }
    }

    fun maybeResolveLyrics(context: Context, track: Track, forceRefresh: Boolean = false) {
        val hasUsableLyrics = track.lyrics.isNotEmpty() &&
            !(track.lyrics.size == 1 && track.lyrics[0].text.contains("No embedded lyrics", ignoreCase = true))
        if (hasUsableLyrics) {
            _isLoadingLyrics.value = false
            return
        }
        if (!forceRefresh && attemptedLyricsIds.contains(track.id)) {
            _isLoadingLyrics.value = false
            return
        }

        attemptedLyricsIds.add(track.id)
        lyricsFetchJob?.cancel()
        lyricsFetchJob = viewModelScope.launch {
            _isLoadingLyrics.value = true
            try {
                val appCtx = context.applicationContext
                val resolved = LyricsRepository.resolveLyrics(appCtx, track, forceRefresh = forceRefresh)
                if (resolved.isNotEmpty()) {
                    repository.updateTrackLyrics(track.id, resolved)
                }
                val summary = LyricsRepository.getStatusSummary(appCtx, repository.tracks.value)
                _lyricsSyncUiState.value = _lyricsSyncUiState.value.copy(statusSummary = summary)
            } finally {
                if (currentTrack.value.id == track.id) {
                    _isLoadingLyrics.value = false
                }
            }
        }
    }

    fun importLyricsFromUri(context: Context, track: Track, uri: Uri) {
        initContext(context)
        lyricsFetchJob?.cancel()
        viewModelScope.launch {
            _isLoadingLyrics.value = true
            try {
                val appCtx = context.applicationContext
                val parsed = LyricsRepository.importLyricsFromUri(appCtx, track, uri)
                if (parsed.isNotEmpty()) {
                    attemptedLyricsIds.add(track.id)
                    repository.updateTrackLyrics(track.id, parsed)
                }
                val summary = LyricsRepository.getStatusSummary(appCtx, repository.tracks.value)
                _lyricsSyncUiState.value = _lyricsSyncUiState.value.copy(statusSummary = summary)
            } finally {
                _isLoadingLyrics.value = false
            }
        }
    }

    fun savePastedLyrics(context: Context, track: Track, rawText: String) {
        initContext(context)
        lyricsFetchJob?.cancel()
        viewModelScope.launch {
            _isLoadingLyrics.value = true
            try {
                val appCtx = context.applicationContext
                val parsed = LyricsRepository.saveManualLyrics(appCtx, track, rawText)
                if (parsed.isNotEmpty()) {
                    attemptedLyricsIds.add(track.id)
                    repository.updateTrackLyrics(track.id, parsed)
                }
                val summary = LyricsRepository.getStatusSummary(appCtx, repository.tracks.value)
                _lyricsSyncUiState.value = _lyricsSyncUiState.value.copy(statusSummary = summary)
            } finally {
                _isLoadingLyrics.value = false
            }
        }
    }

    suspend fun getEditableLyricsText(context: Context, track: Track): String = withContext(Dispatchers.IO) {
        LyricsRepository.getEditableRawLyrics(context.applicationContext, track)
    }

    fun refetchLyricsOnline(context: Context, track: Track) {
        initContext(context)
        lyricsFetchJob?.cancel()
        lyricsFetchJob = viewModelScope.launch {
            _isLoadingLyrics.value = true
            try {
                val appCtx = context.applicationContext
                withContext(Dispatchers.IO) {
                    LyricsRepository.clearCachedLyrics(appCtx, track.id)
                }
                attemptedLyricsIds.remove(track.id)
                repository.updateTrackLyrics(track.id, emptyList())
                attemptedLyricsIds.add(track.id)
                val resolved = LyricsRepository.resolveLyrics(
                    appCtx,
                    track.copy(lyrics = emptyList()),
                    forceRefresh = true
                )
                repository.updateTrackLyrics(track.id, resolved)
                val summary = withContext(Dispatchers.IO) {
                    LyricsRepository.getStatusSummary(appCtx, repository.tracks.value)
                }
                _lyricsSyncUiState.value = _lyricsSyncUiState.value.copy(statusSummary = summary)
            } finally {
                if (currentTrack.value.id == track.id) {
                    _isLoadingLyrics.value = false
                }
            }
        }
    }

    fun clearTrackLyrics(context: Context, track: Track) {
        initContext(context)
        lyricsFetchJob?.cancel()
        viewModelScope.launch {
            val appCtx = context.applicationContext
            withContext(Dispatchers.IO) {
                LyricsRepository.clearCachedLyrics(appCtx, track.id)
            }
            attemptedLyricsIds.add(track.id)
            repository.updateTrackLyrics(track.id, emptyList())
            val summary = withContext(Dispatchers.IO) {
                LyricsRepository.getStatusSummary(appCtx, repository.tracks.value)
            }
            _lyricsSyncUiState.value = _lyricsSyncUiState.value.copy(statusSummary = summary)
            _isLoadingLyrics.value = false
        }
    }

    fun startBatchLyricsSync(context: Context, retryFailed: Boolean = false) {
        val appCtx = context.applicationContext
        initContext(appCtx)
        viewModelScope.launch(Dispatchers.IO) {
            if (retryFailed) {
                LyricsRepository.clearFailedStatuses(appCtx, includeInstrumental = true)
                attemptedLyricsIds.clear()
            }
            val summary = LyricsRepository.getStatusSummary(appCtx, repository.tracks.value)
            _lyricsSyncUiState.value = _lyricsSyncUiState.value.copy(
                statusSummary = summary,
                lastRunCompleted = false
            )
            LyricsSyncWorker.enqueue(
                context = appCtx,
                wifiOnly = repository.settings.value.lyricsSyncWifiOnly,
                replaceExisting = retryFailed
            )
        }
    }

    fun cancelBatchLyricsSync(context: Context) {
        LyricsSyncWorker.cancel(context.applicationContext)
    }

    fun retryFailedLyricsLookups(context: Context) {
        startBatchLyricsSync(context, retryFailed = true)
    }

    fun refreshLyricsStatusSummary(context: Context) {
        val appCtx = context.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            val summary = LyricsRepository.getStatusSummary(appCtx, repository.tracks.value)
            _lyricsSyncUiState.value = _lyricsSyncUiState.value.copy(statusSummary = summary)
        }
    }

    fun importLocalFiles(context: Context, uris: List<Uri>) {
        val appCtx = context.applicationContext
        initContext(appCtx)
        viewModelScope.launch(Dispatchers.IO) {
            val imported = LocalMediaManager.importAudioFromUris(appCtx, uris)
            if (imported.isNotEmpty()) {
                val result = repository.addTracksWithReport(imported)
                if (result.remappedIds.isNotEmpty()) {
                    playbackManager.removeOrRemapTracks(emptySet(), result.remappedIds)
                }
                LocalTracksCache.saveTracks(appCtx, repository.tracks.value)

                val addedIds = result.addedTracks.map { it.id }
                val summaryMsg = buildString {
                    if (result.addedTracks.isNotEmpty()) {
                        append("Imported ${result.addedTracks.size} track${if (result.addedTracks.size == 1) "" else "s"}")
                        if (result.skippedDuplicates > 0) {
                            append(" • ${result.skippedDuplicates} duplicate${if (result.skippedDuplicates == 1) "" else "s"} skipped")
                        }
                    } else {
                        append("All ${result.skippedDuplicates} selected track${if (result.skippedDuplicates == 1) "" else "s"} already in library")
                    }
                }
                _recentImportBanner.value = ImportSummaryBanner(
                    addedCount = result.addedTracks.size,
                    skippedDuplicates = result.skippedDuplicates,
                    importedTrackIds = addedIds,
                    summaryLabel = summaryMsg,
                    isFileImport = true
                )

                if (result.addedTracks.isNotEmpty() && repository.settings.value.autoFetchLyricsOnScan) {
                    LyricsSyncWorker.enqueue(
                        context = appCtx,
                        wifiOnly = repository.settings.value.lyricsSyncWifiOnly,
                        replaceExisting = false
                    )
                }
                if (result.addedTracks.isNotEmpty() && !isPlaying.value) {
                    launch(Dispatchers.Main) {
                        playFromContext(result.addedTracks, 0)
                    }
                }
            }
        }
    }

    fun scanDeviceMusic(context: Context) {
        val appCtx = context.applicationContext
        initContext(appCtx)
        viewModelScope.launch(Dispatchers.IO) {
            val scanned = LocalMediaManager.scanDeviceAudio(appCtx)
            if (scanned.isNotEmpty()) {
                val addResult = repository.addTracksWithReport(scanned)
                val dedupResult = repository.removeDuplicateTracks()
                playbackManager.removeOrRemapTracks(
                    removedIds = dedupResult.removedIds,
                    remappedIds = addResult.remappedIds + dedupResult.remappedIds
                )
                LocalTracksCache.saveTracks(appCtx, repository.tracks.value)

                val mergedCount = addResult.remappedIds.size + dedupResult.removedIds.size
                if (addResult.addedTracks.isNotEmpty() || mergedCount > 0) {
                    val label = buildString {
                        if (addResult.addedTracks.isNotEmpty()) {
                            append("Added ${addResult.addedTracks.size} scanned track${if (addResult.addedTracks.size == 1) "" else "s"}")
                        } else {
                            append("Library scan complete")
                        }
                        if (mergedCount > 0) {
                            append(" • Merged $mergedCount duplicate${if (mergedCount == 1) "" else "s"}")
                        }
                    }
                    _recentImportBanner.value = ImportSummaryBanner(
                        addedCount = addResult.addedTracks.size,
                        skippedDuplicates = addResult.skippedDuplicates,
                        consolidatedDuplicates = mergedCount,
                        importedTrackIds = addResult.addedTracks.map { it.id },
                        summaryLabel = label,
                        isFileImport = false
                    )
                }

                if (repository.settings.value.autoFetchLyricsOnScan) {
                    LyricsSyncWorker.enqueue(
                        context = appCtx,
                        wifiOnly = repository.settings.value.lyricsSyncWifiOnly,
                        replaceExisting = false
                    )
                }
            }
        }
    }

    fun undoLastImport(context: Context) {
        val appCtx = context.applicationContext
        initContext(appCtx)
        val banner = _recentImportBanner.value ?: return
        val idsToRemove = banner.importedTrackIds.toSet()
        _recentImportBanner.value = null
        if (idsToRemove.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val removed = repository.removeTracksByIds(idsToRemove)
            if (removed.isNotEmpty()) {
                playbackManager.removeOrRemapTracks(idsToRemove)
                LocalTracksCache.saveTracks(appCtx, repository.tracks.value)
            }
        }
    }

    fun dismissImportBanner() {
        _recentImportBanner.value = null
    }

    fun removeTrack(context: Context, trackId: String) {
        removeTracksByIds(context, setOf(trackId))
    }

    fun removeTracksByIds(context: Context, trackIds: Set<String>) {
        if (trackIds.isEmpty()) return
        val appCtx = context.applicationContext
        initContext(appCtx)
        viewModelScope.launch(Dispatchers.IO) {
            val removed = repository.removeTracksByIds(trackIds)
            if (removed.isNotEmpty()) {
                playbackManager.removeOrRemapTracks(trackIds)
                LocalTracksCache.saveTracks(appCtx, repository.tracks.value)
                _recentImportBanner.value?.let { currentBanner ->
                    val remaining = currentBanner.importedTrackIds.filterNot { it in trackIds }
                    _recentImportBanner.value = if (remaining.isEmpty()) null else currentBanner.copy(
                        addedCount = remaining.size,
                        importedTrackIds = remaining
                    )
                }
            }
        }
    }

    fun removeDuplicateTracks(context: Context, onComplete: (Int) -> Unit = {}) {
        val appCtx = context.applicationContext
        initContext(appCtx)
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.removeDuplicateTracks()
            if (result.removedIds.isNotEmpty() || result.remappedIds.isNotEmpty()) {
                playbackManager.removeOrRemapTracks(result.removedIds, result.remappedIds)
                LocalTracksCache.saveTracks(appCtx, repository.tracks.value)
            }
            launch(Dispatchers.Main) {
                onComplete(result.removedIds.size)
            }
        }
    }

    fun clearFilePickedImports(context: Context, onComplete: (Int) -> Unit = {}) {
        val appCtx = context.applicationContext
        initContext(appCtx)
        viewModelScope.launch(Dispatchers.IO) {
            val filePickedIds = repository.tracks.value
                .filter { it.isLocalFile && it.id.startsWith("local_") }
                .map { it.id }
                .toSet()
            val removed = repository.removeTracksByIds(filePickedIds)
            if (removed.isNotEmpty()) {
                playbackManager.removeOrRemapTracks(filePickedIds)
                LocalTracksCache.saveTracks(appCtx, repository.tracks.value)
                if (_recentImportBanner.value?.isFileImport == true) {
                    _recentImportBanner.value = null
                }
            }
            launch(Dispatchers.Main) {
                onComplete(removed.size)
            }
        }
    }

    fun setSleepTimer(minutes: Int?) {
        _sleepTimerMinutesLeft.value = minutes
        sleepTimerJob?.cancel()

        if (minutes != null && minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                var remaining = minutes
                while (isActive && remaining > 0) {
                    delay(60000L)
                    remaining--
                    _sleepTimerMinutesLeft.value = remaining
                }
                playbackManager.pause()
                _sleepTimerMinutesLeft.value = null
            }
        }
    }

    fun selectSoundPreset(preset: SoundAlivePreset) {
        _soundPreset.value = preset
        _bassGain.value = preset.bass
        _midGain.value = preset.mid
        _trebleGain.value = preset.treble
        applyEqualizer()
    }

    fun setBass(bass: Float) {
        _bassGain.value = bass
        applyEqualizer()
    }

    fun setMid(mid: Float) {
        _midGain.value = mid
        applyEqualizer()
    }

    fun setTreble(treble: Float) {
        _trebleGain.value = treble
        applyEqualizer()
    }

    fun toggleDolbyAtmos(enabled: Boolean) {
        _dolbyAtmosEnabled.value = enabled
        applyEqualizer()
    }

    private fun applyEqualizer() {
        val extraSpatial = if (_dolbyAtmosEnabled.value) 2.0f else 0.0f
        playbackManager.audioEngine.setEqualizer(
            bass = _bassGain.value + extraSpatial,
            mid = _midGain.value,
            treble = _trebleGain.value + extraSpatial * 0.5f
        )
    }
}
