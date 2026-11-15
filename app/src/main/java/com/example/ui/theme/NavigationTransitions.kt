package com.example.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith

/**
 * Shared Material 3-inspired navigation transition specs for hierarchical pages,
 * full-screen player sheet, top-level tab fade-through, and multi-layout posture shifts.
 */
object NavigationTransitions {

    // 1. Hierarchical Page Transitions (Forward & Back) for NavHost / AnimatedContent
    fun <S> hierarchicalEnter(
        scope: AnimatedContentTransitionScope<S>
    ): EnterTransition = with(scope) {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(durationMillis = 250))
    }

    fun <S> hierarchicalExit(
        scope: AnimatedContentTransitionScope<S>
    ): ExitTransition = with(scope) {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            targetOffset = { it / 4 }
        ) + fadeOut(tween(durationMillis = 200))
    }

    fun <S> hierarchicalPopEnter(
        scope: AnimatedContentTransitionScope<S>
    ): EnterTransition = with(scope) {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            initialOffset = { it / 4 }
        ) + fadeIn(tween(durationMillis = 250))
    }

    fun <S> hierarchicalPopExit(
        scope: AnimatedContentTransitionScope<S>
    ): ExitTransition = with(scope) {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeOut(tween(durationMillis = 200))
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
            animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(durationMillis = 260))
    }

    fun <S> playerSheetExit(
        scope: AnimatedContentTransitionScope<S>
    ): ExitTransition = with(scope) {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Down,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeOut(tween(durationMillis = 220))
    }

    // 3. Top-Level Sibling Tab Fade-Through Transition
    fun tabFadeThrough(): ContentTransform =
        (fadeIn(tween(durationMillis = 220, delayMillis = 50)) +
            scaleIn(
                initialScale = 0.96f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            ))
            .togetherWith(fadeOut(tween(durationMillis = 150)))

    // 4. Smooth Layout Posture Transition (Cover <-> Dual-Pane <-> Tabletop)
    fun postureFade(): ContentTransform =
        fadeIn(tween(durationMillis = 220, easing = FastOutSlowInEasing))
            .togetherWith(fadeOut(tween(durationMillis = 160)))
}
