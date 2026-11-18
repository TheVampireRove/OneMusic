package com.example.audio

import android.content.Context
import android.media.audiofx.Equalizer
import android.net.Uri
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

    val exoPlayer: ExoPlayer?
        get() = _player ?: synchronized(this) {
            _player ?: createExoPlayer()?.also { _player = it }
        }

    val visualizerManager = AudioVisualizerManager(scope, contextProvider)
    val visualizerAmplitudes: StateFlow<List<Float>> = visualizerManager.visualizerAmplitudes

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private var currentTrack: Track? = null
    private var positionJob: Job? = null
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

                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) {
                            _isPlaying.value = false
                            visualizerManager.onIsPlayingChanged(false)
                            scope.launch(Dispatchers.Main) {
                                onTrackFinished?.invoke()
                            }
                        }
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        _isPlaying.value = false
                        visualizerManager.onIsPlayingChanged(false)
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

        player.setMediaItems(mediaItems, safeIndex, startFromMs)
        player.shuffleModeEnabled = shuffleEnabled
        player.repeatMode = repeatMode.toPlayerRepeatMode()
        player.prepare()
        player.play()
        _currentPositionMs.value = startFromMs
        startPositionMonitoring()
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
        if (player.playbackState == Player.STATE_IDLE || player.currentMediaItem == null) {
            currentTrack?.let { playTrack(it, startFromMs = _currentPositionMs.value) }
        } else {
            player.play()
        }
    }

    fun pause() {
        _player?.let { p ->
            _currentPositionMs.value = p.currentPosition.coerceAtLeast(0L)
            p.pause()
        }
        _isPlaying.value = false
        visualizerManager.onIsPlayingChanged(false)
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    fun seekTo(positionMs: Long) {
        val duration = currentTrack?.durationMs ?: 0L
        val target = if (duration > 0) positionMs.coerceIn(0L, duration) else positionMs.coerceAtLeast(0L)
        _currentPositionMs.value = target
        scope.launch(Dispatchers.Main) {
            try {
                exoPlayer?.seekTo(target)
            } catch (_: Exception) {}
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

        // Cache or generate synthesized PCM WAV file for bundled songs
        val file = File(context.cacheDir, "synth_${track.id}.wav")
        if (!file.exists() || file.length() < 1000L) {
            generateWavForTrack(file, track)
        }
        return Uri.fromFile(file)
    }

    private fun generateWavForTrack(file: File, track: Track) {
        try {
            val sampleRate = 44100
            val durationSeconds = 30
            val totalFrames = sampleRate * durationSeconds
            val rootHz = track.rootPitchHz
            val bpm = track.bpm.toFloat()

            val pcmData = ShortArray(totalFrames * 2)
            for (i in 0 until totalFrames) {
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

                val left = (pad + bass + melody * 1.1) * 20000.0
                val right = (pad + bass + melody * 0.9) * 20000.0

                pcmData[i * 2] = left.toInt().coerceIn(-32767, 32767).toShort()
                pcmData[i * 2 + 1] = right.toInt().coerceIn(-32767, 32767).toShort()
            }

            FileOutputStream(file).use { out ->
                val totalAudioLen = (totalFrames * 4).toLong()
                val totalDataLen = totalAudioLen + 36
                val channels = 2
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
                val byteBuffer = ByteBuffer.allocate(pcmData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
                for (sample in pcmData) {
                    byteBuffer.putShort(sample)
                }
                out.write(byteBuffer.array())
            }
        } catch (_: Exception) {}
    }

    private fun startPositionMonitoring() {
        positionJob?.cancel()
        updateVisualizerFallbackMetadata()
        positionJob = scope.launch(Dispatchers.Main) {
            while (isActive && _isPlaying.value) {
                val player = _player
                val currentPosition = player?.currentPosition ?: 0L
                _currentPositionMs.value = currentPosition
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
