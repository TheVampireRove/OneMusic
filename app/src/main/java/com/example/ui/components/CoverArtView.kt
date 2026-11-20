package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.AlbumArtCache
import com.example.model.CoverArtType
import com.example.model.Track
import com.example.ui.theme.MotionSpecs
import com.example.ui.theme.appSharedElement

@Composable
fun CoverArtView(
    track: Track,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    isPlaying: Boolean = false,
    showVinylPeek: Boolean = false,
    cornerRadius: Dp = 16.dp,
    enableAnimatedCover: Boolean = false,
    autoFetchWifiOnly: Boolean = true,
    simulateMotionOnStaticCovers: Boolean = true,
    animatePlaybackScale: Boolean = false,
    artSharedElementKey: Any? = null,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var loadedBitmap by remember(track.id, track.albumArtKey, track.albumArtUri) {
        mutableStateOf(track.albumArtKey?.let { AlbumArtCache.get(it) })
    }

    LaunchedEffect(track.id, track.albumArtKey, track.albumArtUri) {
        if (!enableAnimatedCover && loadedBitmap == null && (track.albumArtKey != null || track.albumArtUri != null)) {
            val bmp = AlbumArtCache.loadArtwork(context, track.albumArtUri, track.albumArtKey)
            if (bmp != null) {
                loadedBitmap = bmp
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val vinylAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotate"
    )

    val playbackScale by animateFloatAsState(
        targetValue = if (!animatePlaybackScale || isPlaying) 1.0f else 0.92f,
        animationSpec = MotionSpecs.ExpressiveSpring,
        label = "cover_playback_scale"
    )
    val dynamicElevation by animateDpAsState(
        targetValue = when {
            !animatePlaybackScale -> 12.dp
            isPlaying -> 24.dp
            else -> 8.dp
        },
        animationSpec = MotionSpecs.expressiveSpring(),
        label = "cover_playback_elevation"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                if (animatePlaybackScale) {
                    scaleX = playbackScale
                    scaleY = playbackScale
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Optional Vinyl Disc peeking behind cover (GPU-accelerated rotation)
        if (showVinylPeek) {
            Box(
                modifier = Modifier
                    .size(size * 0.92f)
                    .offset(x = size * 0.28f)
                    .shadow(if (isPlaying) 18.dp else 10.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF111116))
                    .graphicsLayer {
                        rotationZ = if (isPlaying) vinylAngle else 0f
                    }
                    .border(1.dp, Color(0xFF333340), CircleShape)
            ) {
                // Vinyl grooves
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    for (r in 1..4) {
                        drawCircle(
                            color = Color(0x22FFFFFF),
                            radius = (this.size.width / 2f) * (0.35f + r * 0.12f),
                            center = center,
                            style = Stroke(width = 1.2f)
                        )
                    }
                    // Center label ring
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(track.primaryColor, track.secondaryColor)
                        ),
                        radius = (this.size.width / 2f) * 0.28f,
                        center = center
                    )
                    // Spindle hole
                    drawCircle(
                        color = Color(0xFF09090D),
                        radius = (this.size.width / 2f) * 0.08f,
                        center = center
                    )
                }
            }
        }

        // Main Album Art Box
        Box(
            modifier = Modifier
                .size(size)
                .then(
                    if (artSharedElementKey != null) {
                        Modifier.appSharedElement(
                            key = artSharedElementKey,
                            clipShape = shape
                        )
                    } else {
                        Modifier
                    }
                )
                .shadow(
                    elevation = dynamicElevation,
                    shape = shape,
                    ambientColor = track.primaryColor.copy(alpha = if (isPlaying) 0.9f else 0.55f),
                    spotColor = track.accentColor.copy(alpha = if (isPlaying) 0.9f else 0.5f)
                )
                .clip(shape)
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color(0x66FFFFFF), Color(0x1AFFFFFF))
                    ),
                    shape
                )
        ) {
            if (enableAnimatedCover) {
                AnimatedAlbumCover(
                    track = track,
                    size = size,
                    cornerRadius = cornerRadius,
                    isPlaying = isPlaying,
                    enableAnimatedCovers = true,
                    autoFetchWifiOnly = autoFetchWifiOnly,
                    simulateMotionOnStaticCovers = simulateMotionOnStaticCovers,
                    onClick = onClick,
                    onLongPress = onLongPress,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val bitmap = loadedBitmap
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "${track.title} Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CoverArtCanvas(
                        track = track,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun CoverArtCanvas(
    track: Track,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        when (track.coverType) {
            CoverArtType.COSMIC -> {
                // Cosmic Nebula and celestial rings
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            track.primaryColor,
                            track.secondaryColor,
                            Color(0xFF0F0B24)
                        ),
                        center = Offset(width * 0.4f, height * 0.35f),
                        radius = width * 0.85f
                    )
                )
                // Celestial ring
                drawCircle(
                    color = Color(0x55FFFFFF),
                    radius = width * 0.32f,
                    center = Offset(width * 0.5f, height * 0.5f),
                    style = Stroke(width = width * 0.02f)
                )
                drawCircle(
                    color = track.accentColor.copy(alpha = 0.8f),
                    radius = width * 0.18f,
                    center = Offset(width * 0.5f, height * 0.5f)
                )
                // Star specks
                drawCircle(Color.White, radius = width * 0.02f, center = Offset(width * 0.25f, height * 0.2f))
                drawCircle(Color.White, radius = width * 0.015f, center = Offset(width * 0.78f, height * 0.3f))
                drawCircle(Color.White, radius = width * 0.025f, center = Offset(width * 0.7f, height * 0.75f))
            }
            CoverArtType.LOFI -> {
                // Warm sunset window with rain streaks
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            track.primaryColor,
                            track.secondaryColor,
                            Color(0xFF2C1625)
                        )
                    )
                )
                // Window pane silhouette
                val strokeW = width * 0.04f
                drawLine(
                    color = Color(0x33000000),
                    start = Offset(width * 0.5f, 0f),
                    end = Offset(width * 0.5f, height),
                    strokeWidth = strokeW
                )
                drawLine(
                    color = Color(0x33000000),
                    start = Offset(0f, height * 0.5f),
                    end = Offset(width, height * 0.5f),
                    strokeWidth = strokeW
                )
                // Warm ambient moon
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(track.accentColor, Color(0x00FFFFFF)),
                        center = Offset(width * 0.72f, height * 0.28f),
                        radius = width * 0.25f
                    ),
                    radius = width * 0.25f,
                    center = Offset(width * 0.72f, height * 0.28f)
                )
            }
            CoverArtType.NEON -> {
                // Cyberpunk neon grid and holographic mountain
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF120324),
                            track.primaryColor.copy(alpha = 0.9f),
                            Color(0xFF070014)
                        )
                    )
                )
                // Glowing triangle / synth mountain
                val path = Path().apply {
                    moveTo(width * 0.5f, height * 0.25f)
                    lineTo(width * 0.85f, height * 0.75f)
                    lineTo(width * 0.15f, height * 0.75f)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        listOf(track.secondaryColor, track.accentColor)
                    )
                )
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.6f),
                    style = Stroke(width = width * 0.025f)
                )
            }
            CoverArtType.SUNSET -> {
                // Ocean sun dip
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            track.primaryColor,
                            track.secondaryColor,
                            Color(0xFF2E112D)
                        )
                    )
                )
                // Golden sun disc
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(track.accentColor, track.primaryColor),
                        center = Offset(width * 0.5f, height * 0.55f),
                        radius = width * 0.35f
                    ),
                    radius = width * 0.35f,
                    center = Offset(width * 0.5f, height * 0.55f)
                )
                // Ocean waves
                for (i in 0..3) {
                    val y = height * (0.68f + i * 0.08f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.25f - i * 0.05f),
                        start = Offset(width * 0.15f + i * 15f, y),
                        end = Offset(width * 0.85f - i * 15f, y),
                        strokeWidth = width * 0.02f
                    )
                }
            }
            CoverArtType.FOREST -> {
                // Emerald canopy & mystic circles
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            track.secondaryColor,
                            track.primaryColor,
                            Color(0xFF062211)
                        ),
                        center = Offset(width * 0.5f, height * 0.6f),
                        radius = width * 0.9f
                    )
                )
                // Overlapping organic leafy arches
                val p = Path().apply {
                    moveTo(width * 0.1f, height * 0.85f)
                    cubicTo(width * 0.3f, height * 0.2f, width * 0.7f, height * 0.2f, width * 0.9f, height * 0.85f)
                }
                drawPath(
                    p,
                    brush = Brush.verticalGradient(
                        listOf(track.accentColor, Color.Transparent)
                    ),
                    style = Stroke(width = width * 0.04f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = width * 0.08f,
                    center = Offset(width * 0.5f, height * 0.45f)
                )
            }
            CoverArtType.URBAN -> {
                // Seoul/Tokyo neon pulses
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF140827),
                            track.primaryColor,
                            track.secondaryColor
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(width, height)
                    )
                )
                // Abstract city lines
                for (j in 1..5) {
                    val x = width * (j * 0.16f)
                    drawLine(
                        color = track.accentColor.copy(alpha = 0.4f),
                        start = Offset(x, height * 0.9f),
                        end = Offset(x, height * (0.3f + (j % 3) * 0.15f)),
                        strokeWidth = width * 0.035f
                    )
                }
            }
        }
    }
}
