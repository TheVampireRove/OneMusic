package com.example.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Manages live audio FFT frequency capture from ExoPlayer's active audioSessionId
 * using Android's [android.media.audiofx.Visualizer] API (capturing post-equalizer audio mix).
 *
 * Safely validates [Manifest.permission.RECORD_AUDIO] at runtime before creating the Visualizer,
 * and falls back smoothly to an idle or gentle beat-reactive wave when permission is denied,
 * playback is paused, or hardware Visualizer initialization is unavailable.
 */
@OptIn(UnstableApi::class)
class AudioVisualizerManager(
    private val scope: CoroutineScope,
    private val contextProvider: () -> Context?
) {

    companion object {
        const val BAND_COUNT = 24
        private const val IDLE_LEVEL = 0.08f
        private const val ATTACK_LERP = 0.72f
        private const val DECAY_LERP = 0.34f
    }

    private val _visualizerAmplitudes = MutableStateFlow(List(BAND_COUNT) { IDLE_LEVEL })
    val visualizerAmplitudes: StateFlow<List<Float>> = _visualizerAmplitudes.asStateFlow()

    private val _isLiveFftActive = MutableStateFlow(false)
    val isLiveFftActive: StateFlow<Boolean> = _isLiveFftActive.asStateFlow()

    private var visualizer: Visualizer? = null
    private var attachedPlayer: Player? = null
    private var currentSessionId: Int = C.AUDIO_SESSION_ID_UNSET
    private var isPlayingState: Boolean = false
    private var hasReceivedRealFft: Boolean = false

    private val smoothedBands = FloatArray(BAND_COUNT) { IDLE_LEVEL }
    private var fallbackJob: Job? = null

    // Fallback metadata parameters when RECORD_AUDIO permission is denied or hardware FFT is unavailable
    private var fallbackBpm: Int = 120
    private var fallbackBassGain: Float = 1.0f
    private var fallbackMidGain: Float = 1.0f
    private var fallbackTrebleGain: Float = 1.0f
    private var positionProvider: () -> Long = { 0L }

    private val playerListener = object : Player.Listener {
        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            this@AudioVisualizerManager.onAudioSessionIdChanged(audioSessionId)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            this@AudioVisualizerManager.onIsPlayingChanged(isPlaying)
        }
    }

    fun attachToPlayer(player: Player) {
        if (attachedPlayer === player) return
        attachedPlayer?.removeListener(playerListener)
        attachedPlayer = player
        player.addListener(playerListener)

        val sessionId = try {
            (player as? ExoPlayer)?.audioSessionId ?: C.AUDIO_SESSION_ID_UNSET
        } catch (_: Exception) {
            C.AUDIO_SESSION_ID_UNSET
        }
        isPlayingState = try {
            player.isPlaying
        } catch (_: Exception) {
            false
        }
        onAudioSessionIdChanged(sessionId)
    }

    fun updateFallbackContext(
        bpm: Int,
        bassGain: Float,
        midGain: Float,
        trebleGain: Float,
        positionMsProvider: () -> Long
    ) {
        fallbackBpm = bpm.coerceIn(60, 200)
        fallbackBassGain = bassGain
        fallbackMidGain = midGain
        fallbackTrebleGain = trebleGain
        positionProvider = positionMsProvider
    }

    fun onAudioSessionIdChanged(audioSessionId: Int) {
        currentSessionId = audioSessionId
        rebindOrFallback()
    }

    fun onIsPlayingChanged(isPlaying: Boolean) {
        isPlayingState = isPlaying
        rebindOrFallback()
    }

    fun refreshPermissionState() {
        rebindOrFallback()
    }

    fun hasRecordAudioPermission(context: Context? = contextProvider()): Boolean {
        val ctx = context ?: return false
        return try {
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }

    private fun rebindOrFallback() {
        if (!isPlayingState) {
            releaseNativeVisualizer()
            stopFallbackLoop()
            emitIdleState()
            return
        }

        val ctx = contextProvider()
        val sessionValid = currentSessionId != C.AUDIO_SESSION_ID_UNSET && currentSessionId > 0
        if (sessionValid && hasRecordAudioPermission(ctx)) {
            val initialized = initNativeVisualizer(currentSessionId)
            if (initialized) {
                // Keep a lightweight watchdog/fallback active only until real FFT callbacks arrive
                startFallbackLoopIfNeeded(onlyWhileNoRealFft = true)
                return
            }
        } else {
            releaseNativeVisualizer()
        }

        startFallbackLoopIfNeeded(onlyWhileNoRealFft = false)
    }

    private fun initNativeVisualizer(audioSessionId: Int): Boolean {
        if (visualizer != null && _isLiveFftActive.value) {
            return true
        }
        releaseNativeVisualizer()

        return try {
            val newVisualizer = Visualizer(audioSessionId)
            val sizeRange = Visualizer.getCaptureSizeRange()
            val maxSize = if (sizeRange.size >= 2) sizeRange[1] else 1024
            newVisualizer.enabled = false
            newVisualizer.captureSize = maxSize

            val captureRate = (Visualizer.getMaxCaptureRate() / 2).coerceIn(4000, 20000)
            newVisualizer.setDataCaptureListener(
                object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {
                        // FFT capture is used directly
                    }

                    override fun onFftDataCapture(
                        visualizer: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        if (fft != null && fft.size >= 4 && isPlayingState) {
                            hasReceivedRealFft = true
                            processFftData(fft)
                        }
                    }
                },
                captureRate,
                /* waveform = */ false,
                /* fft = */ true
            )

            newVisualizer.enabled = true
            visualizer = newVisualizer
            _isLiveFftActive.value = true
            true
        } catch (_: Throwable) {
            releaseNativeVisualizer()
            false
        }
    }

    /**
     * Converts raw FFT ByteArray pairs into 24 perceptually distributed frequency bands
     * spanning Sub-bass (0..3), Bass (4..7), Midrange (8..15), and Highs (16..23),
     * smoothed via asymmetric attack/decay interpolation.
     */
    internal fun processFftData(fft: ByteArray) {
        val n = fft.size
        val numBins = n / 2
        if (numBins <= 2) return

        val rawBands = FloatArray(BAND_COUNT)
        val minBin = 1.0
        val maxBin = (numBins - 1).toDouble()

        for (band in 0 until BAND_COUNT) {
            // Logarithmic distribution across FFT frequency bins
            val startFrac = band.toDouble() / BAND_COUNT
            val endFrac = (band + 1).toDouble() / BAND_COUNT

            val binStart = (minBin * (maxBin / minBin).pow(startFrac)).roundToInt().coerceIn(1, numBins - 1)
            val binEnd = (minBin * (maxBin / minBin).pow(endFrac)).roundToInt().coerceIn(binStart, numBins - 1)

            var sumMag = 0f
            var count = 0
            for (k in binStart..binEnd) {
                val real = fft[2 * k].toFloat()
                val imag = fft[2 * k + 1].toFloat()
                sumMag += hypot(real, imag)
                count++
            }

            val avgMag = if (count > 0) sumMag / count else 0f
            // Logarithmic perceptual normalization into 0.08..1.0 range
            val normalized = (ln(1f + avgMag * 2.4f) / ln(1f + 96f)).coerceIn(0.08f, 1.0f)
            rawBands[band] = normalized
        }

        val updated = List(BAND_COUNT) { i ->
            val target = rawBands[i]
            val prev = smoothedBands[i]
            val factor = if (target > prev) ATTACK_LERP else DECAY_LERP
            val next = lerp(prev, target, factor).coerceIn(0.08f, 1.0f)
            smoothedBands[i] = next
            next
        }
        _visualizerAmplitudes.value = updated
    }

    private fun startFallbackLoopIfNeeded(onlyWhileNoRealFft: Boolean) {
        if (fallbackJob?.isActive == true) return
        fallbackJob = scope.launch(Dispatchers.Main) {
            while (isActive && isPlayingState) {
                if (onlyWhileNoRealFft && hasReceivedRealFft && _isLiveFftActive.value) {
                    delay(150L)
                    continue
                }

                val currentPosition = positionProvider()
                val beatDurationMs = (60_000.0 / fallbackBpm).coerceIn(300.0, 1000.0)
                val beatPhase = ((currentPosition % beatDurationMs) / beatDurationMs).toFloat()
                val kickTransient = exp(-beatPhase * 4.5f).coerceIn(0f, 1f)
                val snarePhase = ((beatPhase + 0.5f) % 1.0f)
                val snareTransient = exp(-snarePhase * 5.8f).coerceIn(0f, 1f)

                val fallbackList = List(BAND_COUNT) { i ->
                    val target = when (i) {
                        in 0..3 -> {
                            val wobble = sin(currentPosition / 190.0 + i).toFloat() * 0.08f
                            (0.24f + kickTransient * 0.52f * fallbackBassGain + wobble).coerceIn(0.12f, 0.96f)
                        }
                        in 4..7 -> {
                            val bass = sin(currentPosition / 145.0 + i * 0.6).toFloat() * 0.14f
                            (0.22f + kickTransient * 0.44f * fallbackBassGain + bass).coerceIn(0.12f, 0.92f)
                        }
                        in 8..15 -> {
                            val midWave = (sin(currentPosition / 115.0 + i * 0.72).toFloat() * 0.24f + 0.34f) * fallbackMidGain
                            (midWave + kickTransient * 0.14f).coerceIn(0.10f, 0.85f)
                        }
                        else -> {
                            val highWave = (sin(currentPosition / 82.0 + i * 0.95).toFloat() * 0.22f + 0.26f) * fallbackTrebleGain
                            (highWave + snareTransient * 0.32f).coerceIn(0.08f, 0.84f)
                        }
                    }

                    val prev = smoothedBands[i]
                    val factor = if (target > prev) ATTACK_LERP else DECAY_LERP
                    val next = lerp(prev, target, factor).coerceIn(0.08f, 1.0f)
                    smoothedBands[i] = next
                    next
                }
                _visualizerAmplitudes.value = fallbackList
                delay(33L)
            }
        }
    }

    private fun stopFallbackLoop() {
        fallbackJob?.cancel()
        fallbackJob = null
    }

    private fun emitIdleState() {
        for (i in 0 until BAND_COUNT) {
            smoothedBands[i] = IDLE_LEVEL
        }
        _visualizerAmplitudes.value = List(BAND_COUNT) { IDLE_LEVEL }
    }

    private fun releaseNativeVisualizer() {
        hasReceivedRealFft = false
        _isLiveFftActive.value = false
        visualizer?.let { v ->
            try {
                v.enabled = false
            } catch (_: Exception) {}
            try {
                v.release()
            } catch (_: Exception) {}
        }
        visualizer = null
    }

    fun release() {
        attachedPlayer?.removeListener(playerListener)
        attachedPlayer = null
        releaseNativeVisualizer()
        stopFallbackLoop()
        emitIdleState()
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction
    }
}
