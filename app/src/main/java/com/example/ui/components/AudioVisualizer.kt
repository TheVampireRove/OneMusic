package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.VisualizerStyle
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun AudioVisualizer(
    amplitudes: List<Float>,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    style: VisualizerStyle = VisualizerStyle.BARS,
    maxHeight: Dp = 48.dp,
    barWidth: Dp = 3.5.dp,
    isPlaying: Boolean = false,
    peakColor: Color? = null,
    barBodyColor: Color? = null,
    baseColor: Color? = null,
    glowColor: Color? = null
) {
    val effectivePeak = peakColor ?: primaryColor
    val effectiveBody = barBodyColor ?: primaryColor
    val effectiveBase = baseColor ?: secondaryColor
    val effectiveGlow = glowColor ?: effectiveBody.copy(alpha = 0.25f)

    val gradientColors = if (peakColor != null || barBodyColor != null || baseColor != null) {
        listOf(effectivePeak, effectiveBody, effectiveBase)
    } else {
        listOf(primaryColor, secondaryColor)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = maxHeight),
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying && glowColor != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(maxHeight * 0.7f)
                    .drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(effectiveGlow.copy(alpha = 0.35f), Color.Transparent),
                                radius = size.width * 0.45f
                            )
                        )
                    }
            )
        }

        when (style) {
            VisualizerStyle.BARS -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxHeight),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    amplitudes.forEachIndexed { index, amp ->
                        val animatedHeightFraction by animateFloatAsState(
                            targetValue = if (isPlaying) amp.coerceIn(0.1f, 1f) else 0.12f,
                            animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                            label = "bar_$index"
                        )

                        val currentBarHeight = maxHeight * animatedHeightFraction

                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .height(currentBarHeight.coerceAtLeast(4.dp))
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    brush = Brush.verticalGradient(gradientColors)
                                )
                        )
                    }
                }
            }

            VisualizerStyle.LINES -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxHeight),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    amplitudes.forEachIndexed { index, amp ->
                        val animatedHeightFraction by animateFloatAsState(
                            targetValue = if (isPlaying) amp.coerceIn(0.08f, 1f) else 0.1f,
                            animationSpec = spring(dampingRatio = 0.55f, stiffness = 450f),
                            label = "line_$index"
                        )

                        val currentBarHeight = maxHeight * animatedHeightFraction

                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(currentBarHeight.coerceAtLeast(3.dp))
                                .background(
                                    brush = Brush.verticalGradient(listOf(effectivePeak, effectiveBody))
                                )
                        )
                    }
                }
            }

            VisualizerStyle.WAVEFORM -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(maxHeight)
                ) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2f
                    val pointsCount = amplitudes.size

                    if (pointsCount > 1) {
                        val step = width / (pointsCount - 1)
                        val path = Path()
                        val fillPath = Path()

                        path.moveTo(0f, centerY)
                        fillPath.moveTo(0f, height)
                        fillPath.lineTo(0f, centerY)

                        for (i in 0 until pointsCount) {
                            val amp = if (isPlaying) amplitudes[i].coerceIn(0.1f, 1f) else 0.12f
                            val x = i * step
                            val y = centerY - (amp - 0.5f) * height * 0.85f

                            if (i == 0) {
                                path.moveTo(x, y)
                            } else {
                                val prevX = (i - 1) * step
                                val prevAmp = if (isPlaying) amplitudes[i - 1].coerceIn(0.1f, 1f) else 0.12f
                                val prevY = centerY - (prevAmp - 0.5f) * height * 0.85f
                                val cx = (prevX + x) / 2f
                                path.quadraticBezierTo(prevX, prevY, cx, (prevY + y) / 2f)
                            }
                            fillPath.lineTo(x, y)
                        }

                        fillPath.lineTo(width, height)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(effectiveBody.copy(alpha = 0.28f), Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )

                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(listOf(effectiveBody, effectivePeak, effectiveBase)),
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            VisualizerStyle.MIRRORED_BARS -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxHeight),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    amplitudes.forEachIndexed { index, amp ->
                        val animatedHeightFraction by animateFloatAsState(
                            targetValue = if (isPlaying) amp.coerceIn(0.1f, 1f) else 0.12f,
                            animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f),
                            label = "mirror_bar_$index"
                        )

                        val currentBarHeight = maxHeight * animatedHeightFraction

                        Box(
                            modifier = Modifier
                                .width((barWidth.value * 1.1f).dp)
                                .height(currentBarHeight.coerceAtLeast(6.dp))
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(effectivePeak, effectiveBody, effectivePeak)
                                    )
                                )
                        )
                    }
                }
            }

            VisualizerStyle.CIRCULAR_DOTS -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxHeight),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    amplitudes.forEachIndexed { index, amp ->
                        val animatedHeightFraction by animateFloatAsState(
                            targetValue = if (isPlaying) amp.coerceIn(0.15f, 1f) else 0.2f,
                            animationSpec = spring(dampingRatio = 0.6f, stiffness = 450f),
                            label = "dot_$index"
                        )

                        val dotSize = (4.5f + animatedHeightFraction * 9f).dp

                        Box(
                            modifier = Modifier
                                .width(dotSize)
                                .height(dotSize)
                                .clip(CircleShape)
                                .background(
                                    if (index % 2 == 0) effectivePeak else effectiveBody
                                )
                        )
                    }
                }
            }

            VisualizerStyle.DIGITAL_FLAMES -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(maxHeight)
                ) {
                    val width = size.width
                    val height = size.height
                    val pointsCount = amplitudes.size.coerceAtLeast(1)
                    val tongueWidth = (width / pointsCount).coerceAtLeast(3.dp.toPx())

                    for (i in 0 until pointsCount) {
                        val amp = if (isPlaying) amplitudes[i].coerceIn(0.12f, 1f) else 0.15f
                        val flicker = if (isPlaying) (kotlin.math.sin(i * 1.7 + amp * 6.0).toFloat() * 0.12f) else 0f
                        val flameHeight = ((amp + flicker).coerceIn(0.1f, 1f)) * height

                        val x = i * (width / pointsCount) + tongueWidth / 2f
                        val baseY = height
                        val tipY = height - flameHeight

                        val flamePath = Path().apply {
                            moveTo(x - tongueWidth * 0.45f, baseY)
                            quadraticBezierTo(
                                x - tongueWidth * 0.6f, baseY - flameHeight * 0.55f,
                                x, tipY
                            )
                            quadraticBezierTo(
                                x + tongueWidth * 0.6f, baseY - flameHeight * 0.55f,
                                x + tongueWidth * 0.45f, baseY
                            )
                            close()
                        }

                        val flameBrush = Brush.verticalGradient(
                            colors = listOf(
                                effectivePeak,
                                effectiveBody,
                                Color(0xFFFF3D00),
                                effectiveBase
                            ),
                            startY = tipY,
                            endY = baseY
                        )

                        drawPath(path = flamePath, brush = flameBrush)

                        if (amp > 0.45f) {
                            val innerHeight = flameHeight * 0.6f
                            val innerTipY = height - innerHeight
                            val innerFlamePath = Path().apply {
                                moveTo(x - tongueWidth * 0.22f, baseY)
                                quadraticBezierTo(
                                    x - tongueWidth * 0.28f, baseY - innerHeight * 0.5f,
                                    x, innerTipY
                                )
                                quadraticBezierTo(
                                    x + tongueWidth * 0.28f, baseY - innerHeight * 0.5f,
                                    x + tongueWidth * 0.22f, baseY
                                )
                                close()
                            }
                            drawPath(
                                path = innerFlamePath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White, Color(0xFFFFD54F), Color.Transparent),
                                    startY = innerTipY,
                                    endY = baseY
                                )
                            )
                        }
                    }
                }
            }

            VisualizerStyle.LIQUID_WAVES -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(maxHeight)
                ) {
                    val width = size.width
                    val height = size.height
                    val pointsCount = amplitudes.size.coerceAtLeast(2)
                    val step = width / (pointsCount - 1)

                    val backWave = Path().apply {
                        moveTo(0f, height)
                        for (i in 0 until pointsCount) {
                            val amp = if (isPlaying) amplitudes[i].coerceIn(0.1f, 1f) else 0.12f
                            val x = i * step
                            val waveY = height - (amp * height * 0.7f) - (kotlin.math.sin(i * 0.8).toFloat() * 6.dp.toPx())
                            if (i == 0) {
                                lineTo(x, waveY)
                            } else {
                                val prevX = (i - 1) * step
                                val prevAmp = if (isPlaying) amplitudes[i - 1].coerceIn(0.1f, 1f) else 0.12f
                                val prevY = height - (prevAmp * height * 0.7f) - (kotlin.math.sin((i - 1) * 0.8).toFloat() * 6.dp.toPx())
                                quadraticBezierTo(prevX, prevY, (prevX + x) / 2f, (prevY + waveY) / 2f)
                            }
                        }
                        lineTo(width, height)
                        close()
                    }

                    drawPath(
                        path = backWave,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                effectiveBase.copy(alpha = 0.35f),
                                effectiveBody.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    val frontWave = Path().apply {
                        moveTo(0f, height)
                        for (i in 0 until pointsCount) {
                            val amp = if (isPlaying) amplitudes[i].coerceIn(0.15f, 1f) else 0.15f
                            val x = i * step
                            val waveY = height - (amp * height * 0.9f)
                            if (i == 0) {
                                lineTo(x, waveY)
                            } else {
                                val prevX = (i - 1) * step
                                val prevAmp = if (isPlaying) amplitudes[i - 1].coerceIn(0.15f, 1f) else 0.15f
                                val prevY = height - (prevAmp * height * 0.9f)
                                quadraticBezierTo(prevX, prevY, (prevX + x) / 2f, (prevY + waveY) / 2f)
                            }
                        }
                        lineTo(width, height)
                        close()
                    }

                    drawPath(
                        path = frontWave,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                effectiveBody.copy(alpha = 0.65f),
                                effectivePeak.copy(alpha = 0.45f),
                                effectiveBase.copy(alpha = 0.85f)
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    val rippleCrest = Path().apply {
                        for (i in 0 until pointsCount) {
                            val amp = if (isPlaying) amplitudes[i].coerceIn(0.15f, 1f) else 0.15f
                            val x = i * step
                            val waveY = height - (amp * height * 0.9f)
                            if (i == 0) {
                                moveTo(x, waveY)
                            } else {
                                val prevX = (i - 1) * step
                                val prevAmp = if (isPlaying) amplitudes[i - 1].coerceIn(0.15f, 1f) else 0.15f
                                val prevY = height - (prevAmp * height * 0.9f)
                                quadraticBezierTo(prevX, prevY, (prevX + x) / 2f, (prevY + waveY) / 2f)
                            }
                        }
                    }

                    drawPath(
                        path = rippleCrest,
                        brush = Brush.horizontalGradient(
                            listOf(Color.White.copy(alpha = 0.85f), effectivePeak, Color.White.copy(alpha = 0.85f))
                        ),
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            VisualizerStyle.NEON_AURORA -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .heightIn(min = maxHeight)
                ) {
                    val width = size.width
                    val height = size.height
                    val pointsCount = amplitudes.size.coerceAtLeast(3)
                    val step = width / (pointsCount - 1)

                    // Draw 3 flowing aurora curtain layers (background, mid, foreground)
                    val layers = listOf(
                        Triple(0.35f, 0.5f, effectiveBase.copy(alpha = 0.35f)),
                        Triple(0.55f, 1.2f, effectiveBody.copy(alpha = 0.45f)),
                        Triple(0.85f, 2.1f, effectivePeak.copy(alpha = 0.65f))
                    )

                    layers.forEach { (layerScale, phaseOffset, layerColor) ->
                        val auroraPath = Path().apply {
                            moveTo(0f, height)
                            for (i in 0 until pointsCount) {
                                val amp = if (isPlaying) amplitudes[i].coerceIn(0.1f, 1f) else 0.12f
                                val x = i * step
                                val waveMod = kotlin.math.sin(i * 0.45 + phaseOffset).toFloat() * 0.15f
                                val y = height - ((amp + waveMod).coerceIn(0.08f, 1f) * height * layerScale)
                                if (i == 0) {
                                    lineTo(x, y)
                                } else {
                                    val prevX = (i - 1) * step
                                    val prevAmp = if (isPlaying) amplitudes[i - 1].coerceIn(0.1f, 1f) else 0.12f
                                    val prevWaveMod = kotlin.math.sin((i - 1) * 0.45 + phaseOffset).toFloat() * 0.15f
                                    val prevY = height - ((prevAmp + prevWaveMod).coerceIn(0.08f, 1f) * height * layerScale)
                                    quadraticBezierTo(prevX, prevY, (prevX + x) / 2f, (prevY + y) / 2f)
                                }
                            }
                            lineTo(width, height)
                            close()
                        }

                        drawPath(
                            path = auroraPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(layerColor, layerColor.copy(alpha = 0.1f), Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )
                    }
                }
            }

            VisualizerStyle.RADIAL_ORBIT -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .heightIn(min = maxHeight)
                ) {
                    val width = size.width
                    val height = size.height
                    val center = Offset(width / 2f, height / 2f)
                    val baseRadius = (kotlin.math.min(width, height) * 0.22f).coerceAtLeast(18f)
                    val maxRayLength = (kotlin.math.min(width, height) * 0.26f).coerceAtLeast(24f)
                    val pointsCount = amplitudes.size.coerceAtLeast(8)
                    val angleStep = (2 * Math.PI / pointsCount)

                    // Central glowing core
                    val avgAmp = if (isPlaying) amplitudes.average().toFloat().coerceIn(0.1f, 1f) else 0.15f
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(effectivePeak.copy(alpha = 0.55f), Color.Transparent),
                            center = center,
                            radius = baseRadius * (1f + avgAmp * 0.4f)
                        ),
                        radius = baseRadius * (1f + avgAmp * 0.4f),
                        center = center
                    )

                    // Orbital ring
                    drawCircle(
                        color = effectiveBody.copy(alpha = 0.6f),
                        radius = baseRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Radiating audio spokes
                    for (i in 0 until pointsCount) {
                        val amp = if (isPlaying) amplitudes[i].coerceIn(0.12f, 1f) else 0.15f
                        val angle = i * angleStep
                        val rayLen = baseRadius + (amp * maxRayLength)

                        val startX = (center.x + kotlin.math.cos(angle) * baseRadius).toFloat()
                        val startY = (center.y + kotlin.math.sin(angle) * baseRadius).toFloat()
                        val endX = (center.x + kotlin.math.cos(angle) * rayLen).toFloat()
                        val endY = (center.y + kotlin.math.sin(angle) * rayLen).toFloat()

                        drawLine(
                            brush = Brush.linearGradient(
                                listOf(effectiveBody, effectivePeak),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY)
                            ),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Outer glowing particle tip
                        drawCircle(
                            color = if (amp > 0.6f) Color.White else effectivePeak,
                            radius = (2.dp + (2.5.dp * amp)).toPx(),
                            center = Offset(endX, endY)
                        )
                    }
                }
            }

            VisualizerStyle.PARTICLE_RAIN -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .heightIn(min = maxHeight)
                ) {
                    val width = size.width
                    val height = size.height
                    val pointsCount = amplitudes.size.coerceAtLeast(4)
                    val colWidth = width / pointsCount

                    for (i in 0 until pointsCount) {
                        val amp = if (isPlaying) amplitudes[i].coerceIn(0.1f, 1f) else 0.15f
                        val streamX = i * colWidth + colWidth / 2f
                        val streamHeight = amp * height * 0.88f
                        val streamStartY = height - streamHeight

                        // Rain stream line
                        drawLine(
                            brush = Brush.verticalGradient(
                                listOf(Color.Transparent, effectiveBody.copy(alpha = 0.75f), effectivePeak),
                                startY = streamStartY,
                                endY = height
                            ),
                            start = Offset(streamX, streamStartY),
                            end = Offset(streamX, height),
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Glowing drop bead
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color.White, effectivePeak, Color.Transparent),
                                center = Offset(streamX, streamStartY),
                                radius = 6.dp.toPx()
                            ),
                            radius = (3.dp + (3.dp * amp)).toPx(),
                            center = Offset(streamX, streamStartY)
                        )
                    }
                }
            }

            VisualizerStyle.RETRO_GRID -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .heightIn(min = maxHeight)
                ) {
                    val width = size.width
                    val height = size.height
                    val horizonY = height * 0.25f
                    val centerVanishing = Offset(width / 2f, horizonY)

                    // Perspective horizon glow
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.Transparent, effectivePeak.copy(alpha = 0.25f), effectiveBody.copy(alpha = 0.15f)),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Glowing horizon line
                    drawLine(
                        color = effectivePeak,
                        start = Offset(0f, horizonY),
                        end = Offset(width, horizonY),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Radiating perspective lines
                    val perspectiveSteps = 12
                    for (i in 0..perspectiveSteps) {
                        val bottomX = (width / perspectiveSteps) * i
                        drawLine(
                            color = effectiveBody.copy(alpha = 0.5f),
                            start = centerVanishing,
                            end = Offset(bottomX, height),
                            strokeWidth = 1.2.dp.toPx()
                        )
                    }

                    // Pulsing horizontal crossbars reactive to amplitudes
                    val rungsCount = amplitudes.size.coerceAtLeast(4)
                    for (i in 0 until rungsCount) {
                        val progress = (i + 1).toFloat() / (rungsCount + 1)
                        // Perspective compression
                        val rungY = horizonY + (height - horizonY) * (progress * progress)
                        val amp = if (isPlaying) amplitudes[i].coerceIn(0.1f, 1f) else 0.15f
                        val waveOffset = (amp - 0.5f) * 12.dp.toPx()

                        drawLine(
                            color = if (amp > 0.6f) effectivePeak else effectiveBase.copy(alpha = 0.65f),
                            start = Offset(0f, (rungY + waveOffset).coerceIn(horizonY, height)),
                            end = Offset(width, (rungY + waveOffset).coerceIn(horizonY, height)),
                            strokeWidth = (1.5f + amp * 2.5f).dp.toPx()
                        )
                    }
                }
            }

            VisualizerStyle.SOUND_BLOX -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .heightIn(min = maxHeight)
                ) {
                    val width = size.width
                    val height = size.height
                    val colCount = amplitudes.size.coerceAtLeast(4)
                    val colWidth = width / colCount
                    val barW = (colWidth * 0.7f).coerceAtLeast(4f)
                    val totalBlocksPerCol = 10
                    val blockGap = 3.dp.toPx()
                    val totalGap = blockGap * (totalBlocksPerCol - 1)
                    val blockH = ((height - totalGap) / totalBlocksPerCol).coerceAtLeast(2f)

                    for (i in 0 until colCount) {
                        val amp = if (isPlaying) amplitudes[i].coerceIn(0.1f, 1f) else 0.15f
                        val activeBlocks = (amp * totalBlocksPerCol).toInt().coerceIn(1, totalBlocksPerCol)
                        val x = i * colWidth + (colWidth - barW) / 2f

                        for (b in 0 until totalBlocksPerCol) {
                            val isLit = b < activeBlocks
                            val y = height - (b + 1) * (blockH + blockGap)
                            val blockColor = when {
                                !isLit -> Color(0x18FFFFFF)
                                b >= 8 -> effectivePeak
                                b >= 4 -> effectiveBody
                                else -> effectiveBase
                            }
                            drawRoundRect(
                                color = blockColor,
                                topLeft = Offset(x, y),
                                size = Size(barW, blockH),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }
                }
            }

            VisualizerStyle.RECORD_PLAYING -> {
                var platterAngle by remember { mutableFloatStateOf(0f) }
                LaunchedEffect(isPlaying) {
                    if (isPlaying) {
                        var lastFrameTime = withFrameNanos { it }
                        while (true) {
                            val now = withFrameNanos { it }
                            val dtSeconds = ((now - lastFrameTime) / 1_000_000_000f).coerceIn(0f, 0.1f)
                            lastFrameTime = now
                            // Classic 33.3 RPM (~95 deg/sec visual speed)
                            platterAngle = (platterAngle + dtSeconds * 95f) % 360f
                        }
                    }
                }

                val bassEnergy = if (isPlaying && amplitudes.isNotEmpty()) {
                    amplitudes.take(6).average().toFloat().coerceIn(0.08f, 1f)
                } else 0.1f
                val trebleEnergy = if (isPlaying && amplitudes.size >= 16) {
                    amplitudes.drop(16).average().toFloat().coerceIn(0.08f, 1f)
                } else 0.1f

                val tonearmAngle by animateFloatAsState(
                    targetValue = if (isPlaying) 24f + (trebleEnergy - 0.15f) * 3f else -6f,
                    animationSpec = spring(dampingRatio = 0.72f, stiffness = 220f),
                    label = "tonearm_angle"
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .heightIn(min = maxHeight)
                ) {
                    val width = size.width
                    val height = size.height
                    val minDim = min(width, height)
                    val center = Offset(width * 0.46f, height / 2f)
                    val outerSpectrumRadius = (minDim * 0.46f).coerceAtLeast(24f)
                    val recordRadius = (minDim * 0.36f).coerceAtLeast(20f)
                    val labelRadius = recordRadius * 0.36f

                    // 1. Bass-reactive outer ambient halo
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                effectiveBody.copy(alpha = (0.18f + bassEnergy * 0.35f).coerceIn(0.1f, 0.6f)),
                                effectivePeak.copy(alpha = (0.10f + bassEnergy * 0.18f).coerceIn(0.05f, 0.3f)),
                                Color.Transparent
                            ),
                            center = center,
                            radius = outerSpectrumRadius * (1.05f + bassEnergy * 0.16f)
                        ),
                        radius = outerSpectrumRadius * (1.05f + bassEnergy * 0.16f),
                        center = center
                    )

                    // 2. Audio-reactive equalizer ring radiating around the spinning vinyl record
                    val bandCount = amplitudes.size.coerceAtLeast(8)
                    val totalSpokes = bandCount * 2
                    val maxSpokeExtend = (outerSpectrumRadius - recordRadius).coerceAtLeast(10f)
                    for (i in 0 until totalSpokes) {
                        val ampIdx = i % bandCount
                        val amp = if (isPlaying) amplitudes[ampIdx].coerceIn(0.08f, 1f) else 0.10f
                        val angleRad = (i.toDouble() / totalSpokes) * (2.0 * Math.PI)
                        val startR = recordRadius + 2.dp.toPx()
                        val endR = startR + (amp * maxSpokeExtend * 1.15f)

                        val sx = (center.x + cos(angleRad) * startR).toFloat()
                        val sy = (center.y + sin(angleRad) * startR).toFloat()
                        val ex = (center.x + cos(angleRad) * endR).toFloat()
                        val ey = (center.y + sin(angleRad) * endR).toFloat()

                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(effectiveBase, effectiveBody, effectivePeak),
                                start = Offset(sx, sy),
                                end = Offset(ex, ey)
                            ),
                            start = Offset(sx, sy),
                            end = Offset(ex, ey),
                            strokeWidth = 2.6.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // 3. Rotating Vinyl Disc, Grooves, Specular Sheen & Center Label
                    rotate(degrees = platterAngle, pivot = center) {
                        // Platter base
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF1D1E2C),
                                    Color(0xFF0E0F17),
                                    Color(0xFF181A27),
                                    Color(0xFF090A10)
                                ),
                                center = center,
                                radius = recordRadius
                            ),
                            radius = recordRadius,
                            center = center
                        )

                        // Light reflection arcs rotating with the vinyl
                        drawArc(
                            color = Color.White.copy(alpha = 0.10f),
                            startAngle = -35f,
                            sweepAngle = 55f,
                            useCenter = true,
                            topLeft = Offset(center.x - recordRadius, center.y - recordRadius),
                            size = Size(recordRadius * 2f, recordRadius * 2f)
                        )
                        drawArc(
                            color = Color.White.copy(alpha = 0.08f),
                            startAngle = 145f,
                            sweepAngle = 55f,
                            useCenter = true,
                            topLeft = Offset(center.x - recordRadius, center.y - recordRadius),
                            size = Size(recordRadius * 2f, recordRadius * 2f)
                        )

                        // Dynamic audio-reactive vinyl micro-grooves
                        val grooveCount = 7
                        for (g in 1..grooveCount) {
                            val frac = g.toFloat() / (grooveCount + 1)
                            val r = labelRadius + (recordRadius - labelRadius) * frac
                            val grooveAmp = if (isPlaying && amplitudes.isNotEmpty()) {
                                amplitudes[(g * 3) % amplitudes.size].coerceIn(0.08f, 1f)
                            } else 0.1f
                            drawCircle(
                                color = if (g % 2 == 0) {
                                    effectivePeak.copy(alpha = 0.12f + grooveAmp * 0.28f)
                                } else {
                                    Color.White.copy(alpha = 0.08f + grooveAmp * 0.16f)
                                },
                                radius = r,
                                center = center,
                                style = Stroke(width = (0.9f + grooveAmp * 0.8f).dp.toPx())
                            )
                        }

                        // Outer vinyl rim highlight
                        drawCircle(
                            color = effectivePeak.copy(alpha = 0.35f + bassEnergy * 0.45f),
                            radius = recordRadius,
                            center = center,
                            style = Stroke(width = 1.6.dp.toPx())
                        )

                        // Center record label
                        drawCircle(
                            brush = Brush.linearGradient(
                                colors = listOf(effectivePeak, effectiveBody, effectiveBase),
                                start = Offset(center.x - labelRadius, center.y - labelRadius),
                                end = Offset(center.x + labelRadius, center.y + labelRadius)
                            ),
                            radius = labelRadius,
                            center = center
                        )

                        // Label printed index dot & ring (visually emphasizes vinyl rotation)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.4f),
                            radius = labelRadius * 0.72f,
                            center = center,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = (2.dp.toPx()).coerceAtLeast(2f),
                            center = Offset(center.x + labelRadius * 0.48f, center.y)
                        )
                    }

                    // Center metallic spindle pin (stationary)
                    drawCircle(
                        color = Color(0xFF0B0C14),
                        radius = (labelRadius * 0.22f).coerceAtLeast(4f),
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFFE0E4F5),
                        radius = (labelRadius * 0.11f).coerceAtLeast(2f),
                        center = center
                    )

                    // 4. Articulated Tonearm mounted at top-right of platter
                    val pivot = Offset(
                        x = (center.x + recordRadius * 1.18f).coerceAtMost(width - 14.dp.toPx()),
                        y = (center.y - recordRadius * 0.78f).coerceAtLeast(14.dp.toPx())
                    )
                    val armLength = (recordRadius * 0.98f).coerceAtLeast(24f)

                    rotate(degrees = tonearmAngle, pivot = pivot) {
                        val elbow = Offset(pivot.x - armLength * 0.12f, pivot.y + armLength * 0.65f)
                        val tip = Offset(pivot.x - armLength * 0.38f, pivot.y + armLength * 0.96f)

                        // Counterweight behind pivot
                        drawLine(
                            color = Color(0xFFB0B8D0),
                            start = Offset(pivot.x + armLength * 0.04f, pivot.y - armLength * 0.16f),
                            end = pivot,
                            strokeWidth = 4.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Tonearm tube
                        val armPath = Path().apply {
                            moveTo(pivot.x, pivot.y)
                            lineTo(elbow.x, elbow.y)
                            lineTo(tip.x, tip.y)
                        }
                        drawPath(
                            path = armPath,
                            color = Color(0xFFE2E8F8),
                            style = Stroke(
                                width = 2.6.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Cartridge & glowing stylus tip
                        drawLine(
                            color = Color(0xFF282D42),
                            start = elbow,
                            end = tip,
                            strokeWidth = 5.dp.toPx(),
                            cap = StrokeCap.Square
                        )
                        drawCircle(
                            color = if (isPlaying) effectivePeak else Color(0xFF8892B0),
                            radius = 3.dp.toPx(),
                            center = tip
                        )
                    }

                    // Tonearm pivot housing
                    drawCircle(
                        color = Color(0xFF24283D),
                        radius = 8.dp.toPx(),
                        center = pivot
                    )
                    drawCircle(
                        color = effectivePeak.copy(alpha = 0.7f),
                        radius = 4.dp.toPx(),
                        center = pivot
                    )
                }
            }
        }
    }
}
