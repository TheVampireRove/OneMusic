package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FoldableAutoFlexTracker(
    enabled: Boolean,
    onFlexModeChange: (Boolean) -> Unit
) {
    if (!enabled) return

    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val configuration = LocalConfiguration.current

    // Tabletop mode is strictly valid in LANDSCAPE on the INNER/EXPANDED display.
    // Deactivates on cover screen and in portrait orientation when half opened.
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isInnerDisplay = configuration.smallestScreenWidthDp >= 580 || configuration.screenWidthDp >= 600

    LaunchedEffect(isLandscape, isInnerDisplay) {
        val displayState = if (isInnerDisplay) "Inner Screen" else "Cover Screen"
        val orientationState = if (isLandscape) "Landscape" else "Portrait"
        com.example.diagnostics.CrashReportManager.currentPostureState = "$displayState ($orientationState)"
        if (!isLandscape || !isInnerDisplay) {
            onFlexModeChange(false)
        }
    }

    // 1. Jetpack WindowManager FoldingFeature detection
    LaunchedEffect(activity, enabled, isLandscape, isInnerDisplay) {
        if (!isLandscape || !isInnerDisplay) return@LaunchedEffect

        try {
            WindowInfoTracker.getOrCreate(activity)
                .windowLayoutInfo(activity)
                .collectLatest { layoutInfo ->
                    val foldingFeature = layoutInfo.displayFeatures
                        .filterIsInstance<FoldingFeature>()
                        .firstOrNull()

                    if (foldingFeature != null) {
                        if (foldingFeature.state == FoldingFeature.State.HALF_OPENED) {
                            com.example.diagnostics.CrashReportManager.currentPostureState = "Inner Screen Tabletop (Half-Opened)"
                            onFlexModeChange(true)
                        } else if (foldingFeature.state == FoldingFeature.State.FLAT) {
                            com.example.diagnostics.CrashReportManager.currentPostureState = "Inner Screen Flat (Unfolded)"
                            onFlexModeChange(false)
                        }
                    }
                }
        } catch (_: Exception) {}
    }

    // 2. Hardware Hinge Angle Sensor (Galaxy Z Fold, Z Flip, Pixel Fold)
    DisposableEffect(context, enabled, isLandscape, isInnerDisplay) {
        if (!isLandscape || !isInnerDisplay) {
            onDispose {}
        } else {
            val sensorManager = try {
                context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            } catch (_: Exception) {
                null
            }
            val hingeSensor = try {
                sensorManager?.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE)
            } catch (_: Exception) {
                null
            }

            var listener: SensorEventListener? = null
            if (hingeSensor != null && sensorManager != null) {
                listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        try {
                            val angle = event?.values?.getOrNull(0) ?: return
                            if (angle in 60f..135f) {
                                onFlexModeChange(true)
                            } else if (angle > 165f) {
                                onFlexModeChange(false)
                            }
                        } catch (_: Exception) {}
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
                try {
                    sensorManager.registerListener(listener, hingeSensor, SensorManager.SENSOR_DELAY_NORMAL)
                } catch (_: Exception) {}
            }

            onDispose {
                try {
                    listener?.let { sensorManager?.unregisterListener(it) }
                } catch (_: Exception) {}
            }
        }
    }
}
