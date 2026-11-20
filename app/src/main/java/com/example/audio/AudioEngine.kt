package com.example.audio

import android.content.Context
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Looper
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.model.RepeatMode
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * High-performance ExoPlayer-backed audio engine supporting:
 * 1. Hardware-accelerated local audio playback & contextual MediaItem queues.
 * 2. Real-time post-equalizer audio FFT analysis via [AudioVisualizerManager] bound to ExoPlayer's audioSessionId.
 * 3. Hardware [Equalizer] integration synced with SoundAlive EQ bands.
 * 4. High-fidelity synthesized stereo audio rendering for bundled tracks.
 */
@OptIn(UnstableApi::class)
class AudioEngine(
    private val scope: CoroutineScope,
    private val contextProvider: () -> Context? = { null }
) {

    private var _player: ExoPlayer? = null
    private val releasedPlayers = java.util.Collections.newSetFromMap(java.util.WeakHashMap<ExoPlayer, Boolean>())

    @Volatile
    private var lastErrorOccurred: Boolean = false

    val exoPlayer: ExoPlayer?
        get() {
            val current = _player
            if (isPlayerHealthy(current) && !lastErrorOccurred) return current
            return synchronized(this) {
                val locked = _player
                if (isPlayerHealthy(locked) && !lastErrorOccurred) {
                    locked
                } else {
                    if (locked != null) {
                        releasedPlayers.add(locked)
                        try { locked.release() } catch (_: Exception) {}
                    }
                    _player = null
                    lastErrorOccurred = false
                    createExoPlayer()?.also { _player = it }
                }
            }
        }

    private fun isPlayerHealthy(player: ExoPlayer?): Boolean {
        if (player == null || releasedPlayers.contains(player)) return false
        return try {
            player.playbackLooper.thread.isAlive && player.applicationLooper.thread.isAlive
        } catch (_: Exception) {
            false
        }
    }

    fun markCurrentPlayerDeadForRecovery() {
        synchronized(this) {
            _player?.let { releasedPlayers.add(it) }
            try { _player?.release() } catch (_: Exception) {}
            _player = null
            lastErrorOccurred = false
            _isPlaying.value = false
        }
    }

    fun isCurrentlyAlive(): Boolean = isPlayerHealthy(_player) && !lastErrorOccurred

    val visualizerManager = AudioVisualizerManager(scope, contextProvider)
    val visualizerAmplitudes: StateFlow<List<Float>> = visualizerManager.visualizerAmplitudes

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private var currentTrack: Track? = null
    private var positionJob: Job? = null

    @Volatile
    private var seekGraceUntilMs: Long = 0L

    private var volumeMultiplier: Float = 0.85f
    private var playbackSpeed: Float = 1.0f
    private var shuffleEnabled: Boolean = false
    private var repeatMode: RepeatMode = RepeatMode.OFF

    // Equalizer state & hardware audio effect
    private var bassGain: Float = 1.0f
    private var midGain: Float = 1.0f
    private var trebleGain: Float = 1.0f
    private var rawBassDb: Float = 0f
    private var rawMidDb: Float = 0f
    private var rawTrebleDb: Float = 0f
    private var hardwareEqualizer: Equalizer? = null

    var onTrackFinished: (() -> Unit)? = null
    var onActiveMediaItemChanged: ((mediaId: String?) -> Unit)? = null

    private fun getContext(): Context? {
        contextProvider()?.let { return it }
        return try {
            val appProviderClass = Class.forName("androidx.test.core.app.ApplicationProvider")
            val getContextMethod = appProviderClass.getMethod("getApplicationContext")
            getContextMethod.invoke(null) as? Context
        } catch (_: Exception) {
            null
        }
    }

    private fun createExoPlayer(): ExoPlayer? {
        val ctx = getContext() ?: return null

        return try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()

            val builder = ExoPlayer.Builder(ctx)
                .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
                .setHandleAudioBecomingNoisy(true)
                .setUsePlatformDiagnostics(false)

            // Note: C.WAKE_MODE_NETWORK can throw SecurityException on Android 16/17 when app is launched from background
            try {
                builder.setWakeMode(C.WAKE_MODE_LOCAL)
            } catch (_: Exception) {}

            builder.build().apply {
                volume = volumeMultiplier
                playbackParameters = PlaybackParameters(playbackSpeed)
                shuffleModeEnabled = shuffleEnabled
                repeatMode = this@AudioEngine.repeatMode.toPlayerRepeatMode()

                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                        visualizerManager.onIsPlayingChanged(playing)
                        if (playing) {
                            startPositionMonitoring()
                        }
                    }

                    override fun onAudioSessionIdChanged(audioSessionId: Int) {
                        attachHardwareEqualizer(audioSessionId)
                        visualizerManager.onAudioSessionIdChanged(audioSessionId)
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        mediaItem?.mediaId?.let { id ->
                            scope.launch(Dispatchers.Main) {
                                onActiveMediaItemChanged?.invoke(id)
                            }
                        }
                    }

                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        if (reason == Player.DISCONTINUITY_REASON_SEEK ||
                            reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT
                        ) {
                            seekGraceUntilMs = 0L
                            _currentPositionMs.value = newPosition.positionMs.coerceAtLeast(0L)
                        }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            seekGraceUntilMs = 0L
                        } else if (state == Player.STATE_ENDED) {
                            _isPlaying.value = false
                            visualizerManager.onIsPlayingChanged(false)
                            scope.launch(Dispatchers.Main) {
                                onTrackFinished?.invoke()
                            }
                        }
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        lastErrorOccurred = true
                        _isPlaying.value = false
                        visualizerManager.onIsPlayingChanged(false)
                        positionJob?.cancel()
                    }
                })

                visualizerManager.attachToPlayer(this)
                if (audioSessionId != C.AUDIO_SESSION_ID_UNSET && audioSessionId > 0) {
                    attachHardwareEqualizer(audioSessionId)
                }
            }
        } catch (_: Exception) {
            try {
                ExoPlayer.Builder(ctx)
                    .setHandleAudioBecomingNoisy(true)
                    .build().apply {
                        volume = volumeMultiplier
                        playbackParameters = PlaybackParameters(playbackSpeed)
                        visualizerManager.attachToPlayer(this)
                    }
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun attachHardwareEqualizer(audioSessionId: Int) {
        if (audioSessionId == C.AUDIO_SESSION_ID_UNSET || audioSessionId <= 0) return
        try {
            hardwareEqualizer?.release()
            hardwareEqualizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
            applyHardwareEqualizerBands()
        } catch (_: Throwable) {
            hardwareEqualizer = null
        }
    }

    private fun applyHardwareEqualizerBands() {
        val eq = hardwareEqualizer ?: return
        try {
            val numBands = eq.numberOfBands.toInt()
            if (numBands <= 0) return
            val levelRange = eq.bandLevelRange
            val minLevel = levelRange[0].toInt()
            val maxLevel = levelRange[1].toInt()

            for (bandIdx in 0 until numBands) {
                val centerFreqHz = eq.getCenterFreq(bandIdx.toShort()) / 1000
                val dbBoost = when {
                    centerFreqHz < 250 -> rawBassDb
                    centerFreqHz <= 2000 -> rawMidDb
                    else -> rawTrebleDb
                }
                val millibels = (dbBoost * 150f).toInt().coerceIn(minLevel, maxLevel).toShort()
                eq.setBandLevel(bandIdx.toShort(), millibels)
            }
        } catch (_: Throwable) {}
    }

    fun playTrack(track: Track, startFromMs: Long = 0L) {
        playQueue(listOf(track), startIndex = 0, startFromMs = startFromMs) {
            buildMediaItem(it)
        }
    }

    fun playQueue(
        tracks: List<Track>,
        startIndex: Int,
        startFromMs: Long = 0L,
        mediaItemFactory: (Track) -> MediaItem = { buildMediaItem(it) }
    ) {
        if (tracks.isEmpty()) return
        val safeIndex = startIndex.coerceIn(0, tracks.lastIndex)
        val selectedTrack = tracks[safeIndex]
        currentTrack = selectedTrack
        updateVisualizerFallbackMetadata()

        if (getContext() == null) return
        val player = exoPlayer ?: return
        val mediaItems = tracks.map { mediaItemFactory(it) }

        val clampedStartMs = if (selectedTrack.durationMs > 300L) {
            startFromMs.coerceIn(0L, selectedTrack.durationMs - 200L)
        } else {
            startFromMs.coerceAtLeast(0L)
        }
        seekGraceUntilMs = if (clampedStartMs > 0L) SystemClock.elapsedRealtime() + 450L else 0L
        player.setMediaItems(mediaItems, safeIndex, clampedStartMs)
        player.shuffleModeEnabled = shuffleEnabled
        player.repeatMode = repeatMode.toPlayerRepeatMode()
        player.prepare()
        player.play()
        _currentPositionMs.value = clampedStartMs
        startPositionMonitoring()
    }

    /**
     * Lightweight MediaItem for Android Auto browse tree & search results (does NOT synthesize WAVs on binder thread).
     */
    fun buildBrowseMediaItem(track: Track): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setGenre(track.genre)
            .setIsPlayable(true)
            .setIsBrowsable(false)
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)

        if (track.albumArtUri != null) {
            metadataBuilder.setArtworkUri(Uri.parse(track.albumArtUri))
        }

        return MediaItem.Builder()
            .setMediaId(track.id)
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }

    fun buildMediaItem(track: Track): MediaItem {
        val ctx = getContext()
        val uri = ctx?.let { getTrackUri(it, track) }
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setGenre(track.genre)
            .setIsPlayable(true)
            .setIsBrowsable(false)
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)

        if (track.albumArtUri != null) {
            metadataBuilder.setArtworkUri(Uri.parse(track.albumArtUri))
        }

        return MediaItem.Builder()
            .setMediaId(track.id)
            .apply {
                if (uri != null) setUri(uri)
            }
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }

    fun updateCurrentTrackMetadata(track: Track) {
        currentTrack = track
        updateVisualizerFallbackMetadata()
    }

    fun restoreTrackState(track: Track, positionMs: Long) {
        currentTrack = track
        val clampedPos = if (track.durationMs > 0L) {
            positionMs.coerceIn(0L, track.durationMs)
        } else {
            positionMs.coerceAtLeast(0L)
        }
        _currentPositionMs.value = clampedPos
        updateVisualizerFallbackMetadata()
        // Only seek an already-created ExoPlayer on the Main thread
        val existingPlayer = _player
        if (existingPlayer != null && existingPlayer.currentMediaItem?.mediaId == track.id) {
            scope.launch(Dispatchers.Main) {
                try {
                    _player?.seekTo(clampedPos)
                } catch (_: Exception) {}
            }
        }
    }

    fun resume() {
        val player = exoPlayer ?: return
        if (player.playbackState == Player.STATE_IDLE ||
            player.playbackState == Player.STATE_ENDED ||
            player.currentMediaItem == null ||
            player.mediaItemCount == 0
        ) {
            currentTrack?.let { playTrack(it, startFromMs = _currentPositionMs.value) }
        } else {
            player.play()
        }
    }

    fun pause() {
        _player?.let { p ->
            if (isPlayerHealthy(p)) {
                _currentPositionMs.value = p.currentPosition.coerceAtLeast(0L)
                p.pause()
            }
        }
        _isPlaying.value = false
        visualizerManager.onIsPlayingChanged(false)
    }

    fun togglePlayPause() {
        val actuallyPlaying = _isPlaying.value || (_player?.isPlaying == true)
        if (actuallyPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun seekTo(positionMs: Long) {
        val trackDuration = currentTrack?.durationMs ?: 0L
        val playerDuration = _player?.duration?.takeIf { it != C.TIME_UNSET && it > 0L } ?: 0L
        val effectiveDuration = when {
            playerDuration > 0L && trackDuration > 0L -> minOf(playerDuration, trackDuration)
            playerDuration > 0L -> playerDuration
            else -> trackDuration
        }
        val maxSeek = if (effectiveDuration > 300L) effectiveDuration - 200L else effectiveDuration
        val target = if (maxSeek > 0L) positionMs.coerceIn(0L, maxSeek) else positionMs.coerceAtLeast(0L)

        seekGraceUntilMs = SystemClock.elapsedRealtime() + 450L
        _currentPositionMs.value = target

        val performSeek = {
            try {
                val player = exoPlayer
                if (player != null) {
                    if (player.playbackState == Player.STATE_IDLE ||
                        player.playbackState == Player.STATE_ENDED ||
                        player.mediaItemCount == 0
                    ) {
                        currentTrack?.let { playTrack(it, startFromMs = target) }
                    } else {
                        val liveDuration = player.duration.takeIf { it != C.TIME_UNSET && it > 300L }
                        val safeTarget = if (liveDuration != null) {
                            target.coerceIn(0L, liveDuration - 200L)
                        } else {
                            target
                        }
                        _currentPositionMs.value = safeTarget
                        player.seekTo(safeTarget)
                    }
                }
            } catch (_: Exception) {}
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            performSeek()
        } else {
            scope.launch(Dispatchers.Main) {
                performSeek()
            }
        }
    }

    fun setVolume(vol: Float) {
        volumeMultiplier = vol.coerceIn(0f, 1f)
        _player?.volume = volumeMultiplier
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = speed.coerceIn(0.5f, 2.0f)
        _player?.playbackParameters = PlaybackParameters(playbackSpeed)
    }

    fun setShuffleEnabled(enabled: Boolean) {
        shuffleEnabled = enabled
        _player?.shuffleModeEnabled = enabled
    }

    fun setRepeatMode(mode: RepeatMode) {
        repeatMode = mode
        _player?.repeatMode = mode.toPlayerRepeatMode()
    }

    fun setEqualizer(bass: Float, mid: Float, treble: Float) {
        rawBassDb = bass
        rawMidDb = mid
        rawTrebleDb = treble
        bassGain = (1f + (bass / 10f)).coerceIn(0.2f, 2.0f)
        midGain = (1f + (mid / 10f)).coerceIn(0.2f, 2.0f)
        trebleGain = (1f + (treble / 10f)).coerceIn(0.2f, 2.0f)
        applyHardwareEqualizerBands()
        updateVisualizerFallbackMetadata()
    }

    fun refreshVisualizerPermission() {
        visualizerManager.refreshPermissionState()
    }

    private fun updateVisualizerFallbackMetadata() {
        visualizerManager.updateFallbackContext(
            bpm = currentTrack?.bpm ?: 120,
            bassGain = bassGain,
            midGain = midGain,
            trebleGain = trebleGain,
            positionMsProvider = { _player?.currentPosition ?: _currentPositionMs.value }
        )
    }

    fun getTrackUri(context: Context, track: Track): Uri {
        if (track.uriString != null) {
            return Uri.parse(track.uriString)
        }

        // Remove legacy 30-second synthesized cache file if present
        val legacyFile = File(context.cacheDir, "synth_${track.id}.wav")
        if (legacyFile.exists()) {
            try { legacyFile.delete() } catch (_: Exception) {}
        }

        // Cache or generate full-duration synthesized PCM WAV file for bundled songs
        val expectedDurationSec = if (track.durationMs > 0L) {
            ((track.durationMs + 999L) / 1000L).coerceIn(30L, 600L)
        } else {
            30L
        }
        val sampleRate = 11025L
        val minExpectedBytes = 44L + (sampleRate * expectedDurationSec * 2L) - 1024L
        val file = File(context.cacheDir, "synth_v2_${track.id}_${expectedDurationSec}.wav")
        if (!file.exists() || file.length() < minExpectedBytes) {
            generateWavForTrack(file, track, expectedDurationSec.toInt())
        }
        return Uri.fromFile(file)
    }

    private fun generateWavForTrack(file: File, track: Track, durationSeconds: Int) {
        try {
            val sampleRate = 11025
            val totalFrames = sampleRate * durationSeconds
            val channels = 1
            val rootHz = track.rootPitchHz
            val bpm = track.bpm.toFloat()

            FileOutputStream(file).use { out ->
                val totalAudioLen = (totalFrames.toLong() * channels * 2L)
                val totalDataLen = totalAudioLen + 36L
                val byteRate = 16 * sampleRate * channels / 8

                val header = ByteArray(44)
                header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
                header[4] = (totalDataLen and 0xff).toByte()
                header[5] = ((totalDataLen shr 8) and 0xff).toByte()
                header[6] = ((totalDataLen shr 16) and 0xff).toByte()
                header[7] = ((totalDataLen shr 24) and 0xff).toByte()
                header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
                header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
                header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
                header[20] = 1; header[21] = 0 // PCM
                header[22] = channels.toByte(); header[23] = 0
                header[24] = (sampleRate and 0xff).toByte()
                header[25] = ((sampleRate shr 8) and 0xff).toByte()
                header[26] = ((sampleRate shr 16) and 0xff).toByte()
                header[27] = ((sampleRate shr 24) and 0xff).toByte()
                header[28] = (byteRate and 0xff).toByte()
                header[29] = ((byteRate shr 8) and 0xff).toByte()
                header[30] = ((byteRate shr 16) and 0xff).toByte()
                header[31] = ((byteRate shr 24) and 0xff).toByte()
                header[32] = (channels * 2).toByte(); header[33] = 0 // block align
                header[34] = 16; header[35] = 0 // 16-bit
                header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
                header[40] = (totalAudioLen and 0xff).toByte()
                header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
                header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
                header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

                out.write(header)

                val chunkFrames = 4096
                val byteBuffer = ByteBuffer.allocate(chunkFrames * 2).order(ByteOrder.LITTLE_ENDIAN)
                var frame = 0
                while (frame < totalFrames) {
                    val count = minOf(chunkFrames, totalFrames - frame)
                    byteBuffer.clear()
                    for (j in 0 until count) {
                        val i = frame + j
                        val t = i.toDouble() / sampleRate
                        val beatIndex = ((t * (bpm / 60.0))).toLong()
                        val chordStep = (beatIndex / 4) % 4
                        val chordRatio = when (chordStep) {
                            0L -> 1.0
                            1L -> 1.2599
                            2L -> 1.4983
                            else -> 1.3348
                        }
                        val chordFreq = rootHz * chordRatio
                        val bassFreq = (rootHz * 0.5) * if (chordStep % 2L == 0L) 1.0 else 1.3348

                        val beatPhase = (t * (bpm / 60.0)) % 1.0
                        val kickEnvelope = (1.0 - beatPhase).coerceIn(0.0, 1.0)
                        val kickEnvPow = kickEnvelope * kickEnvelope * kickEnvelope

                        val s1 = sin(2.0 * PI * chordFreq * t)
                        val s2 = sin(2.0 * PI * (chordFreq * 1.5) * t) * 0.5
                        val s3 = sin(2.0 * PI * (chordFreq * 2.0) * t) * 0.35
                        val pad = (s1 + s2 + s3) * 0.28

                        val bass = (sin(2.0 * PI * bassFreq * t) + sin(2.0 * PI * (bassFreq * 0.5) * t) * 0.6) *
                            (0.35 + 0.4 * kickEnvPow)

                        val arpStep = ((t * (bpm / 60.0) * 4).toLong() % 6)
                        val arpMult = 1.0 + (arpStep * 0.25)
                        val melody = sin(2.0 * PI * (chordFreq * 2.0 * arpMult) * t) * 0.15

                        val mono = (pad + bass + melody) * 20000.0
                        byteBuffer.putShort(mono.toInt().coerceIn(-32767, 32767).toShort())
                    }
                    out.write(byteBuffer.array(), 0, count * 2)
                    frame += count
                }
            }
        } catch (_: Exception) {}
    }

    private fun startPositionMonitoring() {
        positionJob?.cancel()
        updateVisualizerFallbackMetadata()
        positionJob = scope.launch(Dispatchers.Main) {
            while (isActive && _isPlaying.value) {
                val player = _player
                if (player != null && SystemClock.elapsedRealtime() >= seekGraceUntilMs) {
                    val currentPosition = player.currentPosition.coerceAtLeast(0L)
                    _currentPositionMs.value = currentPosition
                }
                delay(50L)
            }
        }
    }

    fun release() {
        positionJob?.cancel()
        positionJob = null
        visualizerManager.release()
        try {
            hardwareEqualizer?.release()
        } catch (_: Exception) {}
        hardwareEqualizer = null
        try {
            _player?.release()
        } catch (_: Exception) {}
        _player = null
        _isPlaying.value = false
    }

    private fun RepeatMode.toPlayerRepeatMode(): Int = when (this) {
        RepeatMode.OFF -> Player.REPEAT_MODE_OFF
        RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        RepeatMode.ONE -> Player.REPEAT_MODE_ONE
    }
}
