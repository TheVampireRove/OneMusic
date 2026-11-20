package com.example.ui.theme

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset

/**
 * Global physics-based motion tokens tuned for high-refresh-rate (120Hz) Compose rendering.
 * Replaces rigid duration-based timers with interruptible, velocity-preserving spring physics.
 */
object MotionSpecs {

    /**
     * Layout expansions, sheet drags, shared-element bounds morphs, and list placement glides.
     */
    val ExpressiveSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.78f,
        stiffness = Spring.StiffnessMediumLow
    )

    /**
     * Tab switches, toggle pills, color highlights, and playback icon state morphs.
     */
    val SnappySpring: SpringSpec<Float> = spring(
        dampingRatio = 0.85f,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * Tactile press-and-release compression feedback for buttons, cards, and track rows.
     */
    val TactilePressSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.65f,
        stiffness = Spring.StiffnessHigh
    )

    fun <T> expressiveSpring(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = 0.78f,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = visibilityThreshold
    )

    fun <T> snappySpring(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = 0.85f,
        stiffness = Spring.StiffnessMedium,
        visibilityThreshold = visibilityThreshold
    )

    fun <T> tactilePressSpring(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = 0.65f,
        stiffness = Spring.StiffnessHigh,
        visibilityThreshold = visibilityThreshold
    )

    /**
     * Physics spring spec for LazyColumn / LazyVerticalGrid `Modifier.animateItem` placement.
     */
    val LazyItemPlacementSpring: SpringSpec<IntOffset> = expressiveSpring(
        visibilityThreshold = IntOffset.VisibilityThreshold
    )

    /**
     * Shared-element bounds transform using ExpressiveSpring for organic morphing across screens.
     */
    @OptIn(ExperimentalSharedTransitionApi::class)
    val ExpressiveBoundsTransform: BoundsTransform = BoundsTransform { _: Rect, _: Rect ->
        expressiveSpring()
    }

    /**
     * Smooth scale + fade cross-morph for Play/Pause, Shuffle, Repeat, and Favorite icons.
     */
    fun iconMorphTransition(): ContentTransform =
        (fadeIn(animationSpec = snappySpring()) + scaleIn(
            initialScale = 0.76f,
            animationSpec = snappySpring()
        )) togetherWith (fadeOut(animationSpec = snappySpring()) + scaleOut(
            targetScale = 0.76f,
            animationSpec = snappySpring()
        ))
}

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Attaches a Compose SharedTransitionLayout `sharedElement` when both SharedTransitionScope
 * and AnimatedVisibilityScope are available in the composition hierarchy; safely no-ops otherwise.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.appSharedElement(
    key: Any,
    enabled: Boolean = true,
    clipShape: Shape? = null
): Modifier = composed {
    if (!enabled) return@composed this
    val sharedScope = LocalSharedTransitionScope.current ?: return@composed this
    val visibilityScope = LocalAnimatedVisibilityScope.current ?: return@composed this
    with(sharedScope) {
        if (clipShape != null) {
            this@composed.sharedElement(
                state = rememberSharedContentState(key = key),
                animatedVisibilityScope = visibilityScope,
                boundsTransform = MotionSpecs.ExpressiveBoundsTransform,
                clipInOverlayDuringTransition = OverlayClip(clipShape)
            )
        } else {
            this@composed.sharedElement(
                state = rememberSharedContentState(key = key),
                animatedVisibilityScope = visibilityScope,
                boundsTransform = MotionSpecs.ExpressiveBoundsTransform
            )
        }
    }
}

/**
 * Attaches a Compose SharedTransitionLayout `sharedBounds` morph when both scopes are active.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.appSharedBounds(
    key: Any,
    enabled: Boolean = true,
    clipShape: Shape? = null,
    useScaleToBounds: Boolean = false
): Modifier = composed {
    if (!enabled) return@composed this
    val sharedScope = LocalSharedTransitionScope.current ?: return@composed this
    val visibilityScope = LocalAnimatedVisibilityScope.current ?: return@composed this
    with(sharedScope) {
        val resizeMode = if (useScaleToBounds) {
            SharedTransitionScope.ResizeMode.ScaleToBounds()
        } else {
            SharedTransitionScope.ResizeMode.RemeasureToBounds
        }
        if (clipShape != null) {
            this@composed.sharedBounds(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = visibilityScope,
                enter = fadeIn(animationSpec = MotionSpecs.snappySpring()),
                exit = fadeOut(animationSpec = MotionSpecs.snappySpring()),
                boundsTransform = MotionSpecs.ExpressiveBoundsTransform,
                resizeMode = resizeMode,
                clipInOverlayDuringTransition = OverlayClip(clipShape)
            )
        } else {
            this@composed.sharedBounds(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = visibilityScope,
                enter = fadeIn(animationSpec = MotionSpecs.snappySpring()),
                exit = fadeOut(animationSpec = MotionSpecs.snappySpring()),
                boundsTransform = MotionSpecs.ExpressiveBoundsTransform,
                resizeMode = resizeMode
            )
        }
    }
}

/**
 * GPU-accelerated tactile press-and-release modifier.
 * Compresses scale to [pressedScale] (default 0.95f) inside `graphicsLayer` so layout
 * measurement and placement passes are never triggered during touch interactions.
 */
fun Modifier.bouncableClick(
    enabled: Boolean = true,
    pressedScale: Float = 0.95f,
    onClickLabel: String? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    useDefaultIndication: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by resolvedInteractionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = MotionSpecs.TactilePressSpring,
        label = "bouncable_click_scale"
    )
    val resolvedIndication = if (useDefaultIndication && indication == null) {
        LocalIndication.current
    } else {
        indication
    }
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = resolvedInteractionSource,
            indication = resolvedIndication,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onClick = onClick
        )
}

/**
 * GPU-accelerated tactile press-and-release modifier supporting both click and long-click.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bouncableCombinedClick(
    enabled: Boolean = true,
    pressedScale: Float = 0.95f,
    onClickLabel: String? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    useDefaultIndication: Boolean = true,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by resolvedInteractionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = MotionSpecs.TactilePressSpring,
        label = "bouncable_combined_click_scale"
    )
    val resolvedIndication = if (useDefaultIndication && indication == null) {
        LocalIndication.current
    } else {
        indication
    }
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .combinedClickable(
            interactionSource = resolvedInteractionSource,
            indication = resolvedIndication,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onLongClickLabel = onLongClickLabel,
            onLongClick = onLongClick,
            onClick = onClick
        )
}
