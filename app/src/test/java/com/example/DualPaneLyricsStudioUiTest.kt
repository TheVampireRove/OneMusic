package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.data.LyricsRepository
import com.example.model.CoverArtType
import com.example.model.MusicTab
import com.example.model.RepeatMode
import com.example.model.Track
import com.example.ui.components.DisplayMode
import com.example.ui.components.ExpandedDualPaneScreen
import com.example.ui.theme.ExpressivePalette
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1280dp-h800dp")
class DualPaneLyricsStudioUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `dual pane docks add lyrics beside shifted player and restores layout on close or save`() {
        val sampleTrack = Track(
            id = "dual_pane_test_1",
            title = "Vision Of Love",
            artist = "Mariah Carey",
            album = "Mariah Carey",
            durationMs = 196_000L,
            primaryColor = Color(0xFF3E1E38),
            secondaryColor = Color(0xFF1F1224),
            accentColor = Color(0xFFCF94A6),
            genre = "R&B",
            coverType = CoverArtType.NEON,
            lyrics = emptyList(),
            isLocalFile = true
        )
        val palette = ExpressivePalette(
            primary = sampleTrack.primaryColor,
            secondary = sampleTrack.secondaryColor,
            accent = sampleTrack.accentColor,
            surface = Color(0xFF131422),
            onSurface = Color(0xFFF0F0F8),
            ambientGlow = sampleTrack.primaryColor.copy(alpha = 0.35f)
        )

        var savedLyricsText = ""
        var togglePlayCount = 0

        composeTestRule.setContent {
            var activeTrack by remember { mutableStateOf(sampleTrack) }
            var displayMode by remember { mutableStateOf(DisplayMode.LYRICS) }
            var selectedTab by remember { mutableStateOf(MusicTab.TRACKS) }

            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ExpandedDualPaneScreen(
                        currentTrack = activeTrack,
                        isPlaying = false,
                        currentPositionMs = 12_500L,
                        visualizerAmplitudes = List(24) { 0.2f },
                        tracks = listOf(activeTrack),
                        allTracks = listOf(activeTrack),
                        playlists = emptyList(),
                        selectedTab = selectedTab,
                        searchQuery = "",
                        isShuffleActive = false,
                        repeatMode = RepeatMode.OFF,
                        volume = 0.8f,
                        speed = 1.0f,
                        sleepTimerMinutesLeft = null,
                        dolbyAtmosEnabled = true,
                        palette = palette,
                        onTabSelected = { selectedTab = it },
                        onSearchQueryChange = {},
                        onTrackClick = {},
                        onTogglePlayPause = { togglePlayCount++ },
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
                        onEnterFlexMode = {},
                        displayMode = displayMode,
                        onDisplayModeChange = { displayMode = it },
                        onSavePastedLyrics = { raw ->
                            savedLyricsText = raw
                            activeTrack = activeTrack.copy(
                                lyrics = LyricsRepository.parseLyricsContent(raw, activeTrack.durationMs)
                            )
                        }
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        // Initially: Player sits in the right pane, no docked lyrics editor open
        composeTestRule.onNodeWithTag("dual_pane_right_player_pane").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dual_pane_lyrics_editor_pane").assertDoesNotExist()
        composeTestRule.onNodeWithTag("dual_pane_player_shifted_pane").assertDoesNotExist()

        // Tap "Paste Lyrics" -> Player shifts left to the middle pane, Add Lyrics docks on the right
        composeTestRule.onNodeWithTag("paste_lyrics_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dual_pane_player_shifted_pane").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dual_pane_lyrics_editor_pane").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dual_pane_right_player_pane").assertDoesNotExist()

        // Playback controls remain accessible alongside the docked Lyrics Studio (no blocking modal)
        composeTestRule.onNodeWithContentDescription("Play").performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, togglePlayCount)

        // Type lyrics, switch to Tap-Sync Studio, verify stamp button renders, and save
        composeTestRule.onNodeWithTag("paste_lyrics_input").performTextInput("First test line")
        composeTestRule.onNodeWithTag("lyrics_tab_tap_sync").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tap_sync_stamp_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("save_pasted_lyrics_button").performClick()
        composeTestRule.waitForIdle()

        assertTrue(savedLyricsText.contains("First test line"))
        composeTestRule.onNodeWithTag("dual_pane_lyrics_editor_pane").assertDoesNotExist()
        composeTestRule.onNodeWithTag("dual_pane_player_shifted_pane").assertDoesNotExist()
        composeTestRule.onNodeWithTag("dual_pane_right_player_pane").assertIsDisplayed()
    }
}
