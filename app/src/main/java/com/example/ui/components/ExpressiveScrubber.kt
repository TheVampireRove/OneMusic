package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.ProgressBarDesign
import com.example.model.ProgressBarThickness
import kotlin.math.PI
import kotlin.math.sin

/**
 * Expressive Scrubber supporting modern progress bar styles:
 * - Smooth: Sleek minimalist pill track with glowing handle
 * - Wavy: Undulating sinusoidal wave track (Android Expressive design)
 * - Squiggly: High-frequency playful musical oscillating squiggle
 * - Pulse: Segmented rhythmic pulsing neon blocks
 * - Gradient Glow: Multi-layered neon glow bar
 * - Segmented: High-tech equalizer blocks
 * - Minimal Dot: Hairline track with radiant orb
 * - Stepped Ladder: Rhythm tick ladder
 */
@Composable
fun ExpressiveScrubber(
    progress: Float, // 0f .. 1f
    onProgressChange: (Float) -> Unit,
    onProgressChangeFinished: () -> Unit,
    design: ProgressBarDesign,
    accentColor: Color,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    thickness: ProgressBarThickness = ProgressBarThickness.MEDIUM,
    inactiveColor: Color = Color(0x35FFFFFF),
    thumbColor: Color = Color.White
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(progress) }

    // Smooth gliding progress updates
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350, easing = LinearEasing),
        label = "scrubber_progress"
    )
    val effectiveProgress = if (isDragging) dragFraction else animatedProgress

    // Scale scrubber thumb from 12dp to 18dp while dragging
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.5f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "thumb_scale"
    )

    // Continuous wave animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "scrubber_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val squigglePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "squiggle_phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("expressive_scrubber")
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        dragFraction = fraction
                        onProgressChange(fraction)
                        onProgressChangeFinished()
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        dragFraction = fraction
                        onProgressChange(fraction)
                    },
                    onDragEnd = {
                        isDragging = false
                        onProgressChangeFinished()
                    },
                    onDragCancel = {
                        isDragging = false
                        onProgressChangeFinished()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        dragFraction = fraction
                        onProgressChange(fraction)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            val playedWidth = width * effectiveProgress

            when (design) {
                ProgressBarDesign.SMOOTH -> {
                    val trackHeight = 5.dp.toPx()
                    // Inactive track
                    drawLine(
                        color = inactiveColor,
                        start = Offset(0f, centerY),
                        end = Offset(width, centerY),
                        strokeWidth = trackHeight,
                        cap = StrokeCap.Round
                    )
                    // Active track
                    if (playedWidth > 0f) {
                        drawLine(
                            color = accentColor,
                            start = Offset(0f, centerY),
                            end = Offset(playedWidth, centerY),
                            strokeWidth = trackHeight + 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                    // Glow & Thumb
                    drawCircle(
                        color = accentColor.copy(alpha = 0.35f),
                        radius = 11.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, centerY)
                    )
                    drawCircle(
                        color = thumbColor,
                        radius = 6.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, centerY)
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 2.8.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, centerY)
                    )
                }

                ProgressBarDesign.WAVY -> {
                    // Inactive straight track
                    val trackHeight = 3.5.dp.toPx()
                    drawLine(
                        color = inactiveColor,
                        start = Offset(playedWidth, centerY),
                        end = Offset(width, centerY),
                        strokeWidth = trackHeight,
                        cap = StrokeCap.Round
                    )

                    // Active undulating wave track
                    val waveLength = 44.dp.toPx()
                    val amplitude = 5.5.dp.toPx()
                    val activePhase = if (isPlaying) phase else 0f

                    val path = Path()
                    path.moveTo(0f, centerY)

                    var lastWaveY = centerY
                    val stepPx = 3f
                    var x = 0f
                    while (x <= playedWidth) {
                        val angle = (x / waveLength) * 2 * PI + activePhase
                        val y = centerY + (sin(angle) * amplitude).toFloat()
                        path.lineTo(x, y)
                        lastWaveY = y
                        x += stepPx
                    }
                    if (playedWidth > 0f) {
                        val endAngle = (playedWidth / waveLength) * 2 * PI + activePhase
                        lastWaveY = centerY + (sin(endAngle) * amplitude).toFloat()
                        path.lineTo(playedWidth, lastWaveY)
                    }

                    drawPath(
                        path = path,
                        color = accentColor,
                        style = Stroke(
                            width = 4.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Thumb at current position
                    drawCircle(
                        color = accentColor.copy(alpha = 0.4f),
                        radius = 12.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, lastWaveY)
                    )
                    drawCircle(
                        color = thumbColor,
                        radius = 6.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, lastWaveY)
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 2.8.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, lastWaveY)
                    )
                }

                ProgressBarDesign.SQUIGGLY -> {
                    // Inactive straight line with subtle dashed feel
                    val trackHeight = 3.5.dp.toPx()
                    drawLine(
                        color = inactiveColor,
                        start = Offset(playedWidth, centerY),
                        end = Offset(width, centerY),
                        strokeWidth = trackHeight,
                        cap = StrokeCap.Round
                    )

                    // Active higher frequency fun squiggle
                    val waveLength = 22.dp.toPx()
                    val amplitude = 6.5.dp.toPx()
                    val activePhase = if (isPlaying) squigglePhase else 0f

                    val path = Path()
                    path.moveTo(0f, centerY)

                    var lastWaveY = centerY
                    val stepPx = 2.5f
                    var x = 0f
                    while (x <= playedWidth) {
                        val angle = (x / waveLength) * 2 * PI + activePhase
                        val y = centerY + (sin(angle) * amplitude).toFloat()
                        path.lineTo(x, y)
                        lastWaveY = y
                        x += stepPx
                    }
                    if (playedWidth > 0f) {
                        val endAngle = (playedWidth / waveLength) * 2 * PI + activePhase
                        lastWaveY = centerY + (sin(endAngle) * amplitude).toFloat()
                        path.lineTo(playedWidth, lastWaveY)
                    }

                    drawPath(
                        path = path,
                        color = accentColor,
                        style = Stroke(
                            width = 5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Squiggly glow & thumb
                    drawCircle(
                        color = accentColor.copy(alpha = 0.45f),
                        radius = 13.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, lastWaveY)
                    )
                    drawCircle(
                        color = thumbColor,
                        radius = 6.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, lastWaveY)
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 2.8.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, lastWaveY)
                    )
                }

                ProgressBarDesign.PULSE -> {
                    // Segmented rhythmic blocks
                    val totalSegments = 28
                    val spacing = 3.dp.toPx()
                    val totalSpacing = spacing * (totalSegments - 1)
                    val segWidth = (width - totalSpacing) / totalSegments
                    val segHeight = (thickness.thicknessDp.dp).toPx()
                    val cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())

                    for (i in 0 until totalSegments) {
                        val segStart = i * (segWidth + spacing)
                        val segEnd = segStart + segWidth
                        val isPlayed = segEnd <= playedWidth || (segStart < playedWidth)
                        val isCurrent = segStart <= playedWidth && playedWidth <= segEnd

                        val color = when {
                            isCurrent -> thumbColor
                            isPlayed -> accentColor
                            else -> inactiveColor
                        }

                        val h = if (isCurrent) segHeight * 1.6f else segHeight
                        val y = centerY - (h / 2f)

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(segStart, y),
                            size = Size(segWidth, h),
                            cornerRadius = cornerRadius
                        )
                    }

                    // Thumb dot
                    drawCircle(
                        color = thumbColor,
                        radius = 6.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, centerY)
                    )
                }

                ProgressBarDesign.GRADIENT_GLOW -> {
                    val trackHeight = (thickness.thicknessDp.dp).toPx()
                    // Ambient diffuse glow layer
                    drawLine(
                        color = accentColor.copy(alpha = 0.28f),
                        start = Offset(0f, centerY),
                        end = Offset(playedWidth.coerceAtLeast(1f), centerY),
                        strokeWidth = trackHeight * 2.8f,
                        cap = StrokeCap.Round
                    )
                    // Inactive track
                    drawLine(
                        color = inactiveColor,
                        start = Offset(0f, centerY),
                        end = Offset(width, centerY),
                        strokeWidth = trackHeight,
                        cap = StrokeCap.Round
                    )
                    // Active vibrant track with high-intensity gradient
                    if (playedWidth > 0f) {
                        drawLine(
                            color = accentColor,
                            start = Offset(0f, centerY),
                            end = Offset(playedWidth, centerY),
                            strokeWidth = trackHeight + 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                    // Radiant neon thumb
                    drawCircle(
                        color = accentColor.copy(alpha = 0.45f),
                        radius = (thickness.thicknessDp + 7).dp.toPx() * thumbScale,
                        center = Offset(playedWidth, centerY)
                    )
                    drawCircle(
                        color = thumbColor,
                        radius = 6.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, centerY)
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 2.8.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, centerY)
                    )
                }

                ProgressBarDesign.SEGMENTED -> {
                    val totalBlocks = 32
                    val gap = 2.5.dp.toPx()
                    val totalGap = gap * (totalBlocks - 1)
                    val blockW = (width - totalGap) / totalBlocks
                    val blockH = (thickness.thicknessDp.dp).toPx()

                    for (i in 0 until totalBlocks) {
                        val startX = i * (blockW + gap)
                        val endX = startX + blockW
                        val isPlayed = startX <= playedWidth
                        val isAtThumb = playedWidth in startX..endX

                        val blockColor = when {
                            isAtThumb -> thumbColor
                            isPlayed -> accentColor
                            else -> inactiveColor.copy(alpha = 0.22f)
                        }

                        val h = if (isAtThumb) blockH * 1.5f else blockH
                        drawRoundRect(
                            color = blockColor,
                            topLeft = Offset(startX, centerY - (h / 2f)),
                            size = Size(blockW, h),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }

                ProgressBarDesign.MINIMAL_DOT -> {
                    val hairlineTrack = 2.dp.toPx()
                    // Sleek minimalist hairline track
                    drawLine(
                        color = inactiveColor.copy(alpha = 0.25f),
                        start = Offset(0f, centerY),
                        end = Offset(width, centerY),
                        strokeWidth = hairlineTrack,
                        cap = StrokeCap.Round
                    )
                    if (playedWidth > 0f) {
                        drawLine(
                            color = accentColor,
                            start = Offset(0f, centerY),
                            end = Offset(playedWidth, centerY),
                            strokeWidth = hairlineTrack + 0.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                    // Prominent luminous orbital thumb
                    drawCircle(
                        color = accentColor.copy(alpha = 0.22f),
                        radius = 16.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, centerY)
                    )
                    drawCircle(
                        color = accentColor.copy(alpha = 0.5f),
                        radius = 10.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, centerY)
                    )
                    drawCircle(
                        color = thumbColor,
                        radius = 6.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, centerY)
                    )
                }

                ProgressBarDesign.STEPPED_LADDER -> {
                    val numTicks = 36
                    val tickSpacing = width / numTicks
                    val baseTickH = (thickness.thicknessDp * 0.7f).dp.toPx().coerceAtLeast(5.dp.toPx())

                    drawLine(
                        color = inactiveColor.copy(alpha = 0.2f),
                        start = Offset(0f, centerY),
                        end = Offset(width, centerY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    for (i in 0..numTicks) {
                        val tickX = i * tickSpacing
                        val isPlayed = tickX <= playedWidth
                        val isMajor = i % 4 == 0
                        val tickH = if (isMajor) baseTickH * 1.6f else baseTickH

                        drawLine(
                            color = if (isPlayed) accentColor else inactiveColor,
                            start = Offset(tickX, centerY - (tickH / 2f)),
                            end = Offset(tickX, centerY + (tickH / 2f)),
                            strokeWidth = if (isMajor) 2.2.dp.toPx() else 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Thumb diamond / dot
                    drawCircle(
                        color = thumbColor,
                        radius = 6.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, centerY)
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 2.8.dp.toPx() * thumbScale,
                        center = Offset(playedWidth, centerY)
                    )
                }
            }
        }
    }
}
