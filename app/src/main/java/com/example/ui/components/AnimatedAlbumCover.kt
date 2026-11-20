package com.example.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import androidx.annotation.OptIn
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.AlbumArtCache
import com.example.data.AnimatedArtRepository
import com.example.data.AnimatedCoverMedia
import com.example.model.Track
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.OneUISquircleLarge

/**
 * Multi-format Animated Album Cover container for hero player surfaces.
 *
 * Hierarchy:
 * 1. Video Loop (`.mp4` / `.webm`) via secondary muted ExoPlayer with audioFocus disabled.
 * 2. Animated Image (`.webp` / `.gif`) via Coil ImageDecoder / GifDecoder.
 * 3. Living Artwork enhancement (subtle Ken Burns drift & sheen) on high-res or embedded static art.
 *
 * Automatically pauses animations when paused, app backgrounds, or Battery Saver is enabled.
 */
@kotlin.OptIn(ExperimentalFoundationApi::class)
@OptIn(UnstableApi::class)
@Composable
fun AnimatedAlbumCover(
    track: Track,
    size: Dp = 240.dp,
    cornerRadius: Dp = 24.dp,
    isPlaying: Boolean = true,
    enableAnimatedCovers: Boolean = true,
    autoFetchWifiOnly: Boolean = true,
    simulateMotionOnStaticCovers: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cacheVersion by AnimatedArtRepository.cacheVersion.collectAsStateWithLifecycle()

    var isResumed by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> isResumed = true
                Lifecycle.Event.ON_STOP -> isResumed = false
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var isPowerSaveMode by remember(context) {
        mutableStateOf(
            try {
                (context.getSystemService(Context.POWER_SERVICE) as? PowerManager)?.isPowerSaveMode == true
            } catch (_: Exception) {
                false
            }
        )
    }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                isPowerSaveMode = try {
                    (context.getSystemService(Context.POWER_SERVICE) as? PowerManager)?.isPowerSaveMode == true
                } catch (_: Exception) {
                    false
                }
            }
        }
        try {
            val filter = IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                context.registerReceiver(receiver, filter)
            }
        } catch (_: Exception) {}
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    val shouldAnimate = enableAnimatedCovers && isPlaying && isResumed && !isPowerSaveMode

    var animatedMedia by remember(track.id, track.artist, track.album, track.title, cacheVersion, enableAnimatedCovers) {
        mutableStateOf(
            if (enableAnimatedCovers) {
                AnimatedArtRepository.getCachedOrSidecarImmediate(context, track)
            } else {
                AnimatedCoverMedia.None
            }
        )
    }

    LaunchedEffect(track.id, track.artist, track.album, track.title, cacheVersion, enableAnimatedCovers, autoFetchWifiOnly) {
        animatedMedia = AnimatedArtRepository.resolveAnimatedCover(
            context = context,
            track = track,
            enableAnimatedCovers = enableAnimatedCovers,
            autoFetchWifiOnly = autoFetchWifiOnly
        )
    }

    var loadedBitmap by remember(track.id, track.albumArtKey, track.albumArtUri) {
        mutableStateOf(track.albumArtKey?.let { AlbumArtCache.get(it) })
    }
    LaunchedEffect(track.id, track.albumArtKey, track.albumArtUri) {
        if (loadedBitmap == null && (track.albumArtKey != null || track.albumArtUri != null)) {
            val bmp = AlbumArtCache.loadArtwork(context, track.albumArtUri, track.albumArtKey)
            if (bmp != null) {
                loadedBitmap = bmp
            }
        }
    }

    val interactiveModifier = if (onClick != null || onLongPress != null) {
        Modifier.combinedClickable(
            onClick = { onClick?.invoke() },
            onLongClick = onLongPress
        )
    } else {
        Modifier
    }

    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .then(interactiveModifier)
    ) {
        // Always keep static artwork underneath as immediate placeholder / paused backdrop
        StaticArtworkSurface(
            track = track,
            loadedBitmap = loadedBitmap,
            highResUri = (animatedMedia as? AnimatedCoverMedia.HighResArtwork)?.uri,
            cacheVersion = cacheVersion,
            enableLivingMotion = enableAnimatedCovers && simulateMotionOnStaticCovers &&
                (animatedMedia is AnimatedCoverMedia.None || animatedMedia is AnimatedCoverMedia.HighResArtwork) &&
                shouldAnimate,
            modifier = Modifier.fillMaxSize()
        )

        if (enableAnimatedCovers) {
            when (val media = animatedMedia) {
                is AnimatedCoverMedia.VideoLoop -> {
                    AnimatedVideoCoverLayer(
                        uri = media.uri,
                        shouldPlay = shouldAnimate,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is AnimatedCoverMedia.AnimatedImage -> {
                    AnimatedGifWebpLayer(
                        uri = media.uri,
                        cacheVersion = cacheVersion,
                        shouldAnimate = shouldAnimate,
                        contentDescription = "${track.title} Animated Cover",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> Unit
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun AnimatedVideoCoverLayer(
    uri: android.net.Uri,
    shouldPlay: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoPlayer = remember(context) {
        ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                /* handleAudioFocus = */ false
            )
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ALL
                volume = 0f
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            }
    }

    LaunchedEffect(uri) {
        videoPlayer.setMediaItem(MediaItem.fromUri(uri))
        videoPlayer.prepare()
    }

    LaunchedEffect(shouldPlay) {
        videoPlayer.playWhenReady = shouldPlay
    }

    DisposableEffect(videoPlayer) {
        onDispose {
            try {
                videoPlayer.release()
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                player = videoPlayer
            }
        },
        update = { view ->
            if (view.player !== videoPlayer) {
                view.player = videoPlayer
            }
        },
        modifier = modifier.testTag("animated_video_cover")
    )
}

@Composable
private fun AnimatedGifWebpLayer(
    uri: android.net.Uri,
    cacheVersion: Int,
    shouldAnimate: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val animatedImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
    val staticImageLoader = remember(context) {
        ImageLoader.Builder(context).build()
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(uri)
            .memoryCacheKey("${uri}_$cacheVersion")
            .diskCachePolicy(CachePolicy.DISABLED)
            .crossfade(true)
            .build(),
        imageLoader = if (shouldAnimate) animatedImageLoader else staticImageLoader,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.testTag("animated_image_cover")
    )
}

@Composable
private fun StaticArtworkSurface(
    track: Track,
    loadedBitmap: android.graphics.Bitmap?,
    highResUri: android.net.Uri?,
    cacheVersion: Int,
    enableLivingMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "living_artwork_motion")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11_000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ken_burns_progress"
    )

    val activeZoom by animateFloatAsState(
        targetValue = if (enableLivingMotion) 1.015f + progress * 0.065f else 1f,
        animationSpec = tween(durationMillis = 600),
        label = "living_zoom"
    )
    val driftX by animateFloatAsState(
        targetValue = if (enableLivingMotion) (progress - 0.5f) * 14f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "living_drift_x"
    )
    val driftY by animateFloatAsState(
        targetValue = if (enableLivingMotion) (0.5f - progress) * 10f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "living_drift_y"
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = activeZoom
                    scaleY = activeZoom
                    translationX = driftX
                    translationY = driftY
                }
        ) {
            when {
                highResUri != null -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(highResUri)
                            .memoryCacheKey("${highResUri}_$cacheVersion")
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .crossfade(true)
                            .build(),
                        contentDescription = "${track.title} Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                loadedBitmap != null -> {
                    Image(
                        bitmap = loadedBitmap.asImageBitmap(),
                        contentDescription = "${track.title} Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    CoverArtCanvas(
                        track = track,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        if (enableLivingMotion) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.09f),
                                Color.Transparent
                            ),
                            start = Offset(x = progress * 400f - 120f, y = 0f),
                            end = Offset(x = progress * 400f + 220f, y = 600f)
                        )
                    )
            )
        }
    }
}

@Composable
fun AnimatedCoverOptionsDialog(
    track: Track,
    hasCustomCover: Boolean,
    accentColor: Color,
    onDismiss: () -> Unit,
    onPickAnimatedFile: () -> Unit,
    onRemoveCustomCover: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Animated Album Cover",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${track.album} • ${track.artist}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0x99FFFFFF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Attach a local .mp4, .webm, .gif, or animated .webp loop for this album. Side-car cover.mp4 / cover.gif files in your music folder are also detected automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xCCFFFFFF)
                )

                Button(
                    onClick = {
                        onPickAnimatedFile()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color(0xFF0C0E1A)
                    ),
                    shape = OneUIPillShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pick_animated_cover_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MovieFilter,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Choose Video or GIF Loop",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (hasCustomCover) {
                    OutlinedButton(
                        onClick = {
                            onRemoveCustomCover()
                            onDismiss()
                        },
                        shape = OneUIPillShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("remove_animated_cover_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = Color(0xFFFF6E6E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Restore Default Artwork",
                            color = Color(0xFFFF8A80)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xCCFFFFFF))
            }
        },
        containerColor = Color(0xFF16182C),
        shape = OneUISquircleLarge
    )
}
