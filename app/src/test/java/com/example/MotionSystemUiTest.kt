package com.example

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.example.model.CoverArtType
import com.example.model.RepeatMode
import com.example.model.Track
import com.example.ui.components.CoverArtView
import com.example.ui.components.MiniPlayer
import com.example.ui.components.NowPlayingScreen
import com.example.ui.theme.LocalAnimatedVisibilityScope
import com.example.ui.theme.LocalSharedTransitionScope
import com.example.ui.theme.MotionSpecs
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.bouncableClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h915dp")
class MotionSystemUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun sampleTrack(): Track = Track(
        id = "motion_test_track_1",
        title = "Midnight Velocity",
        artist = "Neon Horizon",
        album = "Hyperdrive",
        durationMs = 215_000L,
        primaryColor = Color(0xFF2848A8),
        secondaryColor = Color(0xFF151E3F),
        accentColor = Color(0xFF70D6FF),
        genre = "Synthwave",
        coverType = CoverArtType.NEON,
        lyrics = emptyList(),
        isLocalFile = true
    )

    @Test
    fun `motion specs define exact physics spring parameters`() {
        assertEquals(0.78f, MotionSpecs.ExpressiveSpring.dampingRatio, 0.0001f)
        assertEquals(Spring.StiffnessMediumLow, MotionSpecs.ExpressiveSpring.stiffness, 0.0001f)

        assertEquals(0.85f, MotionSpecs.SnappySpring.dampingRatio, 0.0001f)
        assertEquals(Spring.StiffnessMedium, MotionSpecs.SnappySpring.stiffness, 0.0001f)

        assertEquals(0.65f, MotionSpecs.TactilePressSpring.dampingRatio, 0.0001f)
        assertEquals(Spring.StiffnessHigh, MotionSpecs.TactilePressSpring.stiffness, 0.0001f)

        assertEquals(0.78f, MotionSpecs.LazyItemPlacementSpring.dampingRatio, 0.0001f)
        assertEquals(Spring.StiffnessMediumLow, MotionSpecs.LazyItemPlacementSpring.stiffness, 0.0001f)
    }

    @Test
    fun `bouncableClick dispatches click callback reliably`() {
        var clicks = 0
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("bouncable_target")
                        .bouncableClick(pressedScale = 0.95f) {
                            clicks++
                        }
                ) {
                    Text("Tap")
                }
            }
        }

        composeTestRule.onNodeWithTag("bouncable_target").performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, clicks)
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun `shared transition morphs mini player to now playing and handles playback state changes`() {
        val track = sampleTrack()
        var isPlayingState = false

        composeTestRule.setContent {
            var expanded by remember { mutableStateOf(false) }
            var playing by remember { mutableStateOf(false) }

            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        AnimatedContent(
                            targetState = expanded,
                            transitionSpec = {
                                fadeIn(animationSpec = MotionSpecs.snappySpring()) togetherWith
                                    fadeOut(animationSpec = MotionSpecs.snappySpring())
                            },
                            label = "test_player_shared_transition"
                        ) { showFullPlayer ->
                            CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                if (!showFullPlayer) {
                                    MiniPlayer(
                                        track = track,
                                        isPlaying = playing,
                                        currentPositionMs = 45_000L,
                                        onExpand = { expanded = true },
                                        onTogglePlayPause = {
                                            playing = !playing
                                            isPlayingState = playing
                                        },
                                        onNext = {}
                                    )
                                } else {
                                    NowPlayingScreen(
                                        track = track,
                                        isPlaying = playing,
                                        currentPositionMs = 45_000L,
                                        visualizerAmplitudes = List(16) { 0.3f },
                                        isShuffleActive = false,
                                        repeatMode = RepeatMode.OFF,
                                        volume = 0.8f,
                                        speed = 1.0f,
                                        sleepTimerMinutesLeft = null,
                                        onCollapse = { expanded = false },
                                        onTogglePlayPause = {
                                            playing = !playing
                                            isPlayingState = playing
                                        },
                                        onNext = {},
                                        onPrevious = {},
                                        onSeek = {},
                                        onToggleShuffle = {},
                                        onCycleRepeat = {},
                                        onToggleFavorite = {},
                                        onVolumeChange = {},
                                        onSpeedChange = {},
                                        onOpenEqualizer = {},
                                        onOpenSleepTimer = {},
                                        enableSharedPlayerTransition = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("mini_player").assertIsDisplayed()

        // Expand Mini-Player -> Full-Screen Now Playing via shared-element morph
        composeTestRule.onNodeWithTag("mini_player").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("hero_cover_art").assertIsDisplayed()

        // Toggle Play -> Pause (exercises AnimatedContent Play/Pause morph & CoverArtView dynamic scale)
        composeTestRule.onNodeWithContentDescription("Play").performClick()
        composeTestRule.waitForIdle()
        assertTrue(isPlayingState)
        composeTestRule.onNodeWithContentDescription("Pause").assertIsDisplayed()

        // Toggle back to paused
        composeTestRule.onNodeWithContentDescription("Pause").performClick()
        composeTestRule.waitForIdle()
        assertFalse(isPlayingState)
        composeTestRule.onNodeWithContentDescription("Play").assertIsDisplayed()

        // Collapse back to Mini-Player
        composeTestRule.onNodeWithContentDescription("Collapse Player").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("mini_player").assertIsDisplayed()
    }

    @Test
    fun `cover art view animates cleanly across playing and paused states`() {
        val track = sampleTrack()
        composeTestRule.setContent {
            var playing by remember { mutableStateOf(false) }
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Box(
                    modifier = Modifier
                        .testTag("toggle_cover_playback")
                        .bouncableClick { playing = !playing }
                ) {
                    CoverArtView(
                        track = track,
                        size = 200.dp,
                        isPlaying = playing,
                        animatePlaybackScale = true
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("toggle_cover_playback").assertIsDisplayed()
        composeTestRule.onNodeWithTag("toggle_cover_playback").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("toggle_cover_playback").assertIsDisplayed()
    }
}
