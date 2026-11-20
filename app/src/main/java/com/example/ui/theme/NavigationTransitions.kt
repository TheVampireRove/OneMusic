package com.example.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

/**
 * Shared physics-based navigation transition specs backed by [MotionSpecs] springs
 * for hierarchical pages, full-screen player sheet, top-level tab fade-through,
 * and multi-layout foldable posture shifts.
 */
object NavigationTransitions {

    // 1. Hierarchical Page Transitions (Forward & Back) for NavHost / AnimatedContent
    fun <S> hierarchicalEnter(
        scope: AnimatedContentTransitionScope<S>
    ): EnterTransition = with(scope) {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = MotionSpecs.expressiveSpring(visibilityThreshold = IntOffset.VisibilityThreshold)
        ) + fadeIn(animationSpec = MotionSpecs.snappySpring())
    }

    fun <S> hierarchicalExit(
        scope: AnimatedContentTransitionScope<S>
    ): ExitTransition = with(scope) {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = MotionSpecs.expressiveSpring(visibilityThreshold = IntOffset.VisibilityThreshold),
            targetOffset = { it / 4 }
        ) + fadeOut(animationSpec = MotionSpecs.snappySpring())
    }

    fun <S> hierarchicalPopEnter(
        scope: AnimatedContentTransitionScope<S>
    ): EnterTransition = with(scope) {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = MotionSpecs.expressiveSpring(visibilityThreshold = IntOffset.VisibilityThreshold),
            initialOffset = { it / 4 }
        ) + fadeIn(animationSpec = MotionSpecs.snappySpring())
    }

    fun <S> hierarchicalPopExit(
        scope: AnimatedContentTransitionScope<S>
    ): ExitTransition = with(scope) {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = MotionSpecs.expressiveSpring(visibilityThreshold = IntOffset.VisibilityThreshold)
        ) + fadeOut(animationSpec = MotionSpecs.snappySpring())
    }

    fun <S> hierarchicalContentTransform(
        scope: AnimatedContentTransitionScope<S>,
        isForward: Boolean
    ): ContentTransform = if (isForward) {
        hierarchicalEnter(scope) togetherWith hierarchicalExit(scope)
    } else {
        hierarchicalPopEnter(scope) togetherWith hierarchicalPopExit(scope)
    }

    // 2. Expandable Now Playing Sheet / Full Player Transition
    fun <S> playerSheetEnter(
        scope: AnimatedContentTransitionScope<S>
    ): EnterTransition = with(scope) {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Up,
            animationSpec = MotionSpecs.expressiveSpring(visibilityThreshold = IntOffset.VisibilityThreshold)
        ) + fadeIn(animationSpec = MotionSpecs.expressiveSpring())
    }

    fun <S> playerSheetExit(
        scope: AnimatedContentTransitionScope<S>
    ): ExitTransition = with(scope) {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Down,
            animationSpec = MotionSpecs.expressiveSpring(visibilityThreshold = IntOffset.VisibilityThreshold)
        ) + fadeOut(animationSpec = MotionSpecs.snappySpring())
    }

    // 3. Top-Level Sibling Tab Fade-Through Transition
    fun tabFadeThrough(): ContentTransform =
        (fadeIn(animationSpec = MotionSpecs.snappySpring()) +
            scaleIn(
                initialScale = 0.96f,
                animationSpec = MotionSpecs.snappySpring()
            ))
            .togetherWith(
                fadeOut(animationSpec = MotionSpecs.snappySpring()) +
                    scaleOut(
                        targetScale = 0.98f,
                        animationSpec = MotionSpecs.snappySpring()
                    )
            )

    // 4. Smooth Layout Posture Transition (Cover <-> Dual-Pane <-> Tabletop)
    fun postureFade(): ContentTransform =
        (fadeIn(animationSpec = MotionSpecs.expressiveSpring()) +
            scaleIn(initialScale = 0.98f, animationSpec = MotionSpecs.expressiveSpring()))
            .togetherWith(fadeOut(animationSpec = MotionSpecs.snappySpring()))
}
