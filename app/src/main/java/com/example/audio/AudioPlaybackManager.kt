package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.LocalTracksCache
import com.example.data.MusicRepository
import com.example.model.RepeatMode
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Shared singleton playback manager coordinating audio between the One UI Compose UI
 * and the background Android Auto MediaBrowserServiceCompat / MediaSessionCompat.
 */
class AudioPlaybackManager private constructor() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var appContext: Context? = null
    val repository: MusicRepository = MusicRepository.getInstance()

    val audioEngine: AudioEngine = AudioEngine(scope) { appContext }

    val exoPlayer: androidx.media3.exoplayer.ExoPlayer?
        get() = audioEngine.exoPlayer

    fun trackToMediaItem(track: Track): androidx.media3.common.MediaItem {
        return audioEngine.buildMediaItem(track)
    }

    fun trackToBrowseMediaItem(track: Track): androidx.media3.common.MediaItem {
        return audioEngine.buildBrowseMediaItem(track)
    }

    val isPlaying: StateFlow<Boolean> = audioEngine.isPlaying
    val currentPositionMs: StateFlow<Long> = audioEngine.currentPositionMs
    val visualizerAmplitudes: StateFlow<List<Float>> = audioEngine.visualizerAmplitudes

    private val _playbackQueue = MutableStateFlow<List<Track>>(repository.tracks.value)
    val playbackQueue: StateFlow<List<Track>> = _playbackQueue.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track>(repository.tracks.value.first())
    val currentTrack: StateFlow<Track> = _currentTrack.asStateFlow()

    private val _isShuffleActive = MutableStateFlow(false)
    val isShuffleActive: StateFlow<Boolean> = _isShuffleActive.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    @Volatile
    private var hasUserInitiatedPlayback = false

    @Volatile
    private var hasRestoredSavedTrack = false

    private var positionPersistJob: Job? = null

    init {
        audioEngine.onTrackFinished = {
            handleTrackCompletion()
        }
        audioEngine.onActiveMediaItemChanged = { mediaId ->
            if (mediaId != null) {
                val matched = _playbackQueue.value.find { it.id == mediaId }
                    ?: repository.tracks.value.find { it.id == mediaId }
                if (matched != null && matched.id != _currentTrack.value.id) {
                    _currentTrack.value = matched
                    audioEngine.updateCurrentTrackMetadata(matched)
                    repository.updateSettings {
                        it.copy(
                            lastPlayedTrackId = matched.id,
                            lastPlayedPositionMs = 0L,
                            lastPlayedQueueIds = _playbackQueue.value.map { q -> q.id }
                        )
                    }
                }
            }
        }
        scope.launch {
            audioEngine.isPlaying.collect { playing ->
                if (playing) {
                    startPositionPersistenceLoop()
                } else {
                    stopPositionPersistenceLoop()
                    if (hasUserInitiatedPlayback) {
                        persistPlaybackState()
                    }
                }
            }
        }
        scope.launch {
            repository.tracks.collect { list ->
                if (list.isEmpty()) return@collect
                val current = _currentTrack.value
                val updated = list.find { it.id == current.id }
                if (updated != null) {
                    _currentTrack.value = updated
                    audioEngine.updateCurrentTrackMetadata(updated)
                }

                // If playback hasn't started yet, keep queue & restored track synchronized as cached tracks arrive
                if (!hasUserInitiatedPlayback && !isPlaying.value && !audioEngine.isPlaying.value) {
                    restoreLastPlaybackStateInternal(list)
                } else {
                    val currentQueue = _playbackQueue.value
                    if (currentQueue.isEmpty()) {
                        _playbackQueue.value = list
                    } else {
                        val mapById = list.associateBy { it.id }
                        _playbackQueue.value = currentQueue.map { mapById[it.id] ?: it }
                    }
                }
            }
        }
    }

    @androidx.annotation.VisibleForTesting
    internal fun resetForProcessReopenTest() {
        stopPositionPersistenceLoop()
        audioEngine.release()
        appContext = null
        hasRestoredSavedTrack = false
        hasUserInitiatedPlayback = false
        _playbackQueue.value = repository.tracks.value
        _currentTrack.value = repository.tracks.value.first()
    }

    fun initContext(context: Context) {
        val ctx = context.applicationContext
        val previousContext = appContext
        val contextChanged = previousContext != null && previousContext !== ctx
        appContext = ctx
        if (contextChanged) {
            audioEngine.release()
            hasRestoredSavedTrack = false
            hasUserInitiatedPlayback = false
        }
        repository.initContext(ctx)

        if (!hasRestoredSavedTrack && !isPlaying.value && !audioEngine.isPlaying.value) {
            val cached = LocalTracksCache.loadTracks(ctx)
            if (cached.isNotEmpty()) {
                repository.addTracks(cached)
            }
            restoreLastPlaybackStateInternal(repository.tracks.value)
        }
    }

    private fun restoreLastPlaybackStateInternal(availableTracks: List<Track>) {
        if (availableTracks.isEmpty() || isPlaying.value || audioEngine.isPlaying.value) return
        val settings = repository.settings.value
        val byId = availableTracks.associateBy { it.id }

        val restoredQueue = if (settings.lastPlayedQueueIds.isNotEmpty()) {
            settings.lastPlayedQueueIds.mapNotNull { byId[it] }.ifEmpty { availableTracks }
        } else {
            availableTracks
        }

        val lastId = settings.lastPlayedTrackId
        val matchedTrack = if (lastId != null) byId[lastId] else null

        if (matchedTrack != null) {
            val finalQueue = if (restoredQueue.any { it.id == matchedTrack.id }) {
                restoredQueue
            } else {
                listOf(matchedTrack) + restoredQueue
            }
            _playbackQueue.value = finalQueue
            _currentTrack.value = matchedTrack
            audioEngine.restoreTrackState(matchedTrack, settings.lastPlayedPositionMs)
            hasRestoredSavedTrack = true
        } else {
            _playbackQueue.value = restoredQueue
            if (_playbackQueue.value.none { it.id == _currentTrack.value.id }) {
                _currentTrack.value = _playbackQueue.value.first()
                audioEngine.restoreTrackState(_currentTrack.value, 0L)
            }
        }
    }

    private fun startPositionPersistenceLoop() {
        positionPersistJob?.cancel()
        positionPersistJob = scope.launch {
            while (isActive && audioEngine.isPlaying.value) {
                delay(2500L)
                if (audioEngine.isPlaying.value) {
                    persistPlaybackState()
                }
            }
        }
    }

    private fun stopPositionPersistenceLoop() {
        positionPersistJob?.cancel()
        positionPersistJob = null
    }

    fun persistPlaybackState() {
        val current = _currentTrack.value
        val pos = audioEngine.currentPositionMs.value
        val queueIds = _playbackQueue.value.map { it.id }.ifEmpty { listOf(current.id) }
        repository.updateSettings {
            it.copy(
                lastPlayedTrackId = current.id,
                lastPlayedPositionMs = pos,
                lastPlayedQueueIds = queueIds
            )
        }
    }

    fun refreshVisualizerPermission() {
        audioEngine.refreshVisualizerPermission()
    }

    /**
     * Called when MainActivity or OneMusicAutoService returns from background/inactivity
     * to verify ExoPlayer is alive, recover if released, and refresh audio focus/visualizer state.
     */
    fun ensureActivePlayer() {
        val ctx = appContext ?: return
        repository.initContext(ctx)
        val player = audioEngine.exoPlayer ?: return
        if (player.mediaItemCount == 0) {
            val current = _currentTrack.value
            val pos = currentPositionMs.value
            audioEngine.restoreTrackState(current, pos)
        } else {
            val actualPlaying = player.isPlaying
            if (!actualPlaying && isPlaying.value) {
                audioEngine.pause()
            }
        }
        audioEngine.refreshVisualizerPermission()
    }

    fun syncQueueFromExternalMediaItems(
        items: List<androidx.media3.common.MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ) {
        if (items.isEmpty()) return
        val allTracks = repository.tracks.value
        val byId = allTracks.associateBy { it.id }
        val resolvedTracks = items.mapNotNull { byId[it.mediaId] }
        if (resolvedTracks.isNotEmpty()) {
            val safeIdx = startIndex.coerceIn(0, resolvedTracks.lastIndex)
            val selected = resolvedTracks[safeIdx]
            hasUserInitiatedPlayback = true
            hasRestoredSavedTrack = true
            _playbackQueue.value = resolvedTracks
            _currentTrack.value = selected
            audioEngine.updateCurrentTrackMetadata(selected)
            repository.updateSettings {
                it.copy(
                    lastPlayedTrackId = selected.id,
                    lastPlayedPositionMs = startPositionMs.coerceAtLeast(0L),
                    lastPlayedQueueIds = resolvedTracks.map { t -> t.id }
                )
            }
        }
    }

    fun startPlaybackService() {
        val ctx = appContext ?: return
        val intent = Intent(ctx, com.example.service.OneMusicAutoService::class.java).apply {
            action = com.example.service.OneMusicAutoService.ACTION_SYNC
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        } catch (_: Exception) {
            // On Android 12+/17+, calling startService from background also throws IllegalStateException/ForegroundServiceStartNotAllowedException
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                try {
                    ctx.startService(intent)
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Loads the full contextual track list into ExoPlayer / active Up Next queue
     * and begins playback at [startIndex] from [startFromMs].
     */
    fun playFromContext(tracks: List<Track>, startIndex: Int, startFromMs: Long = 0L) {
        if (tracks.isEmpty()) return
        hasUserInitiatedPlayback = true
        hasRestoredSavedTrack = true
        val safeIndex = startIndex.coerceIn(0, tracks.lastIndex)
        val selectedTrack = tracks[safeIndex]

        _playbackQueue.value = tracks.toList()
        _currentTrack.value = selectedTrack

        audioEngine.playQueue(
            tracks = tracks,
            startIndex = safeIndex,
            startFromMs = startFromMs,
            mediaItemFactory = { trackToMediaItem(it) }
        )
        startPlaybackService()
        repository.updateSettings {
            it.copy(
                lastPlayedTrackId = selectedTrack.id,
                lastPlayedPositionMs = startFromMs,
                lastPlayedQueueIds = tracks.map { q -> q.id }
            )
        }
    }

    fun playTrack(track: Track) {
        val queue = _playbackQueue.value.ifEmpty { repository.tracks.value }
        val existingIdx = queue.indexOfFirst { it.id == track.id }
        if (existingIdx >= 0) {
            playFromContext(queue, existingIdx, startFromMs = 0L)
        } else {
            val all = repository.tracks.value
            val allIdx = all.indexOfFirst { it.id == track.id }
            if (allIdx >= 0) {
                playFromContext(all, allIdx, startFromMs = 0L)
            } else {
                playFromContext(listOf(track), 0, startFromMs = 0L)
            }
        }
    }

    fun setLastTrack(track: Track, positionMs: Long = 0L) {
        // Prevent spurious seek or track overwrite if audio is already actively playing
        if (isPlaying.value || audioEngine.isPlaying.value || hasUserInitiatedPlayback) {
            return
        }
        val currentQueue = _playbackQueue.value
        if (currentQueue.none { it.id == track.id }) {
            val repoTracks = repository.tracks.value
            _playbackQueue.value = if (repoTracks.any { it.id == track.id }) {
                repoTracks
            } else {
                listOf(track) + currentQueue
            }
        }
        _currentTrack.value = track
        audioEngine.restoreTrackState(track, positionMs)
        hasRestoredSavedTrack = true
    }

    fun playTrackById(trackId: String) {
        val queue = _playbackQueue.value
        val queueIdx = queue.indexOfFirst { it.id == trackId }
        if (queueIdx >= 0) {
            playFromContext(queue, queueIdx)
            return
        }
        val track = repository.tracks.value.find { it.id == trackId }
        if (track != null) {
            playTrack(track)
        }
    }

    fun removeOrRemapTracks(
        removedIds: Set<String>,
        remappedIds: Map<String, String> = emptyMap()
    ) {
        if (removedIds.isEmpty() && remappedIds.isEmpty()) return
        val repoTracks = repository.tracks.value
        val repoMap = repoTracks.associateBy { it.id }

        val updatedQueue = _playbackQueue.value
            .mapNotNull { queued ->
                val canonicalId = remappedIds[queued.id]
                when {
                    canonicalId != null -> repoMap[canonicalId] ?: queued.copy(id = canonicalId)
                    queued.id in removedIds -> null
                    else -> repoMap[queued.id] ?: queued
                }
            }
            .distinctBy { it.id }
            .ifEmpty { repoTracks }

        _playbackQueue.value = updatedQueue

        val currentId = _currentTrack.value.id
        val remappedCurrent = remappedIds[currentId]?.let { repoMap[it] }
        if (remappedCurrent != null) {
            _currentTrack.value = remappedCurrent
            audioEngine.updateCurrentTrackMetadata(remappedCurrent)
        } else if (currentId in removedIds) {
            if (isPlaying.value || audioEngine.isPlaying.value) {
                audioEngine.pause()
            }
            val fallback = updatedQueue.firstOrNull() ?: repoTracks.firstOrNull()
            if (fallback != null) {
                _currentTrack.value = fallback
                audioEngine.restoreTrackState(fallback, 0L)
            }
        }
    }

    fun togglePlayPause() {
        val player = audioEngine.exoPlayer
        val needsPrepare = player == null ||
            player.playbackState == androidx.media3.common.Player.STATE_IDLE ||
            player.playbackState == androidx.media3.common.Player.STATE_ENDED ||
            player.currentMediaItem == null ||
            player.mediaItemCount == 0
        val currentlyPlaying = isPlaying.value || (player?.isPlaying == true)
        if (!currentlyPlaying && needsPrepare) {
            resumeCurrentTrackFromSavedPosition()
        } else {
            hasUserInitiatedPlayback = true
            audioEngine.togglePlayPause()
            startPlaybackService()
            if (!isPlaying.value) {
                persistPlaybackState()
            }
        }
    }

    fun pause() {
        audioEngine.pause()
        persistPlaybackState()
    }

    fun resume() {
        val player = audioEngine.exoPlayer
        val needsPrepare = player == null ||
            player.playbackState == androidx.media3.common.Player.STATE_IDLE ||
            player.playbackState == androidx.media3.common.Player.STATE_ENDED ||
            player.currentMediaItem == null ||
            player.mediaItemCount == 0
        if (needsPrepare) {
            resumeCurrentTrackFromSavedPosition()
        } else {
            hasUserInitiatedPlayback = true
            audioEngine.resume()
            startPlaybackService()
        }
    }

    private fun resumeCurrentTrackFromSavedPosition() {
        val current = _currentTrack.value
        val savedPos = currentPositionMs.value
        val queue = _playbackQueue.value.ifEmpty { repository.tracks.value }
        val idx = queue.indexOfFirst { it.id == current.id }
        if (idx >= 0) {
            playFromContext(queue, idx, startFromMs = savedPos)
        } else {
            val all = repository.tracks.value
            val allIdx = all.indexOfFirst { it.id == current.id }
            if (allIdx >= 0) {
                playFromContext(all, allIdx, startFromMs = savedPos)
            } else {
                playFromContext(listOf(current) + queue, 0, startFromMs = savedPos)
            }
        }
    }

    fun toggleFavorite(trackId: String) {
        repository.toggleFavorite(trackId)
    }

    fun nextTrack() {
        hasUserInitiatedPlayback = true
        val queue = _playbackQueue.value.ifEmpty { repository.tracks.value }
        if (queue.isEmpty()) return

        val player = exoPlayer
        if (player != null && player.mediaItemCount > 1 && player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            val newIdx = player.currentMediaItemIndex
            if (newIdx in queue.indices) {
                val next = queue[newIdx]
                _currentTrack.value = next
                audioEngine.updateCurrentTrackMetadata(next)
                repository.updateSettings {
                    it.copy(
                        lastPlayedTrackId = next.id,
                        lastPlayedPositionMs = 0L,
                        lastPlayedQueueIds = queue.map { q -> q.id }
                    )
                }
                return
            }
        }

        val curr = queue.indexOfFirst { it.id == _currentTrack.value.id }
        val nextIndex = if (_isShuffleActive.value) {
            queue.indices.filter { it != curr }.randomOrNull() ?: 0
        } else {
            if (curr >= 0) (curr + 1) % queue.size else 0
        }

        playFromContext(queue, nextIndex)
    }

    fun previousTrack(forcePrevious: Boolean = false) {
        hasUserInitiatedPlayback = true
        if (!forcePrevious && currentPositionMs.value > 3000L) {
            audioEngine.seekTo(0L)
            persistPlaybackState()
            return
        }
        val queue = _playbackQueue.value.ifEmpty { repository.tracks.value }
        if (queue.isEmpty()) return

        val player = exoPlayer
        if (player != null && player.mediaItemCount > 1 && player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            val newIdx = player.currentMediaItemIndex
            if (newIdx in queue.indices) {
                val prev = queue[newIdx]
                _currentTrack.value = prev
                audioEngine.updateCurrentTrackMetadata(prev)
                repository.updateSettings {
                    it.copy(
                        lastPlayedTrackId = prev.id,
                        lastPlayedPositionMs = 0L,
                        lastPlayedQueueIds = queue.map { q -> q.id }
                    )
                }
                return
            }
        }

        val curr = queue.indexOfFirst { it.id == _currentTrack.value.id }
        val prevIndex = if (_isShuffleActive.value) {
            queue.indices.filter { it != curr }.randomOrNull() ?: 0
        } else {
            if (curr > 0) curr - 1 else queue.size - 1
        }

        playFromContext(queue, prevIndex)
    }

    fun seekTo(positionMs: Long) {
        val track = _currentTrack.value
        val maxSeek = if (track.durationMs > 300L) track.durationMs - 200L else track.durationMs
        val clamped = if (maxSeek > 0L) positionMs.coerceIn(0L, maxSeek) else positionMs.coerceAtLeast(0L)
        val player = audioEngine.exoPlayer
        val needsPrepare = player == null ||
            player.playbackState == androidx.media3.common.Player.STATE_IDLE ||
            player.playbackState == androidx.media3.common.Player.STATE_ENDED ||
            player.mediaItemCount == 0

        if (needsPrepare) {
            if (isPlaying.value) {
                hasUserInitiatedPlayback = true
                val queue = _playbackQueue.value.ifEmpty { repository.tracks.value }
                val idx = queue.indexOfFirst { it.id == track.id }
                if (idx >= 0) {
                    playFromContext(queue, idx, startFromMs = clamped)
                    return
                }
            }
            audioEngine.restoreTrackState(track, clamped)
        } else {
            audioEngine.seekTo(clamped)
        }

        repository.updateSettings {
            it.copy(
                lastPlayedTrackId = track.id,
                lastPlayedPositionMs = clamped,
                lastPlayedQueueIds = _playbackQueue.value.map { q -> q.id }
            )
        }
    }

    fun toggleShuffle() {
        setShuffleEnabled(!_isShuffleActive.value)
    }

    fun setShuffleEnabled(enabled: Boolean) {
        _isShuffleActive.value = enabled
        audioEngine.setShuffleEnabled(enabled)
    }

    fun cycleRepeatMode() {
        val next = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = next
        audioEngine.setRepeatMode(next)
    }

    private fun handleTrackCompletion() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                audioEngine.seekTo(0L)
                audioEngine.resume()
            }
            RepeatMode.ALL -> {
                nextTrack()
            }
            RepeatMode.OFF -> {
                val queue = _playbackQueue.value.ifEmpty { repository.tracks.value }
                val curr = queue.indexOfFirst { it.id == _currentTrack.value.id }
                if (curr >= 0 && curr < queue.size - 1) {
                    nextTrack()
                } else {
                    audioEngine.pause()
                    persistPlaybackState()
                }
            }
        }
    }

    companion object {
        @Volatile
        private var instance: AudioPlaybackManager? = null

        fun getInstance(): AudioPlaybackManager {
            return instance ?: synchronized(this) {
                instance ?: AudioPlaybackManager().also { instance = it }
            }
        }
    }
}
