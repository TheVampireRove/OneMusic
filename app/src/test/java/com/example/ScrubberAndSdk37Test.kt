package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AudioEngine
import com.example.audio.AudioPlaybackManager
import com.example.model.CoverArtType
import com.example.model.ProgressBarDesign
import com.example.model.Track
import com.example.service.OneMusicAutoService
import com.example.ui.components.ExpressiveScrubber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScrubberAndSdk37Test {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `expressive scrubber uses updated track duration and seeks accurately on tap`() {
        var durationMs by mutableLongStateOf(200_000L)
        var dragFraction by mutableStateOf(0f)
        val soughtPositions = mutableListOf<Long>()

        composeTestRule.setContent {
            ExpressiveScrubber(
                progress = 0.1f,
                onProgressChange = { frac -> dragFraction = frac },
                onProgressChangeFinished = {
                    soughtPositions.add((dragFraction * durationMs).toLong())
                },
                design = ProgressBarDesign.WAVY,
                accentColor = Color.Cyan,
                isPlaying = true
            )
        }

        // First tap at 75% on a 200,000 ms track -> ~150,000 ms
        composeTestRule.onNodeWithTag("expressive_scrubber").performTouchInput {
            click(Offset(x = width * 0.75f, y = height / 2f))
        }
        composeTestRule.waitForIdle()
        assertEquals(1, soughtPositions.size)
        assertTrue(
            "Expected first tap seek near 150000ms, got ${soughtPositions[0]}",
            soughtPositions[0] in 145_000L..155_000L
        )

        // Switch to a shorter 100,000 ms track without recreating ExpressiveScrubber
        durationMs = 100_000L
        composeTestRule.waitForIdle()

        // Second tap at 50% on the 100,000 ms track -> ~50,000 ms (NOT 100,000 ms!)
        composeTestRule.onNodeWithTag("expressive_scrubber").performTouchInput {
            click(Offset(x = width * 0.50f, y = height / 2f))
        }
        composeTestRule.waitForIdle()
        assertEquals(2, soughtPositions.size)
        assertTrue(
            "Expected second tap seek near 50000ms using updated duration, got ${soughtPositions[1]}",
            soughtPositions[1] in 47_000L..53_000L
        )
    }

    @Test
    fun `audio engine synthesizes full duration wav and clamps near-eof seek safely`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val legacyFile = File(context.cacheDir, "synth_full_test_1.wav").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
        }

        val track = Track(
            id = "full_test_1",
            title = "Full Length Synth",
            artist = "Test Artist",
            album = "Test Album",
            durationMs = 184_000L,
            primaryColor = Color.Magenta,
            secondaryColor = Color.Blue,
            accentColor = Color.Cyan,
            genre = "Synthwave",
            coverType = CoverArtType.NEON
        )

        val engine = AudioEngine(CoroutineScope(Dispatchers.Main)) { context }
        val uri = engine.getTrackUri(context, track)
        val generatedFile = File(requireNotNull(uri.path))

        try {
            // Legacy 30s cache file must be purged
            assertFalse("Legacy 30s cache file should be deleted", legacyFile.exists())
            assertTrue("Generated WAV file should exist", generatedFile.exists())

            // Expect 184s @ 11025 Hz mono 16-bit PCM (~4,057,244 bytes) instead of 30s
            val expectedMinBytes = 44L + (184L * 11025L * 2L)
            assertTrue(
                "WAV size ${generatedFile.length()} should be >= $expectedMinBytes for full 184s track",
                generatedFile.length() >= expectedMinBytes
            )

            // Seeking near or past track end via AudioPlaybackManager clamps safely without overshooting EOF
            val manager = AudioPlaybackManager.getInstance()
            manager.initContext(context)
            manager.playTrack(track)
            manager.seekTo(184_000L)
            assertEquals(183_800L, manager.currentPositionMs.value)

            manager.seekTo(92_000L)
            assertEquals(92_000L, manager.currentPositionMs.value)
        } finally {
            engine.release()
            generatedFile.delete()
            legacyFile.delete()
        }
    }

    @Test
    fun `manifest declares explicit exports, mediaPlayback FGS, and required audio permissions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pm = context.packageManager
        val pkgInfo = pm.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS or PackageManager.GET_SERVICES or PackageManager.GET_ACTIVITIES
        )

        val permissions = pkgInfo.requestedPermissions?.toSet() ?: emptySet()
        assertTrue(permissions.contains(Manifest.permission.READ_MEDIA_AUDIO))
        assertTrue(permissions.contains(Manifest.permission.POST_NOTIFICATIONS))
        assertTrue(permissions.contains(Manifest.permission.RECORD_AUDIO))
        assertTrue(permissions.contains(Manifest.permission.MODIFY_AUDIO_SETTINGS))
        assertTrue(permissions.contains(Manifest.permission.WAKE_LOCK))
        assertTrue(permissions.contains(Manifest.permission.FOREGROUND_SERVICE))
        assertTrue(permissions.contains(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK))

        val mainActivityInfo = pkgInfo.activities?.firstOrNull { it.name == MainActivity::class.java.name }
        assertTrue("MainActivity must be exported", mainActivityInfo?.exported == true)

        val autoServiceInfo = pkgInfo.services?.firstOrNull { it.name == OneMusicAutoService::class.java.name }
        assertTrue("OneMusicAutoService must be exported", autoServiceInfo?.exported == true)
        assertTrue(
            "OneMusicAutoService must declare foregroundServiceType mediaPlayback",
            (autoServiceInfo?.foregroundServiceType ?: 0) and ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK != 0
        )
    }

    @Test
    fun `media session next and previous skip commands advance sequentially without double skipping`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = AudioPlaybackManager.getInstance()
        manager.initContext(context)
        val queue = manager.repository.tracks.value.take(5)
        assertTrue(queue.size >= 5)
        manager.playFromContext(queue, 0)

        val controller = org.robolectric.Robolectric.buildService(OneMusicAutoService::class.java).create()
        val service = controller.get()
        try {
            val session = service.ensureMediaLibrarySession()
            requireNotNull(session)
            val sessionPlayer = session.player

            // Simulate Media3 MediaSessionStub flow: onPlayerCommandRequest authorization + player.seekToNext()
            assertEquals(queue[0].id, manager.currentTrack.value.id)
            sessionPlayer.seekToNext()
            assertEquals("First Next must advance from 0 to 1 sequentially", queue[1].id, manager.currentTrack.value.id)

            sessionPlayer.seekToNextMediaItem()
            assertEquals("Second Next must advance from 1 to 2 sequentially", queue[2].id, manager.currentTrack.value.id)

            sessionPlayer.seekToNext()
            assertEquals("Third Next must advance from 2 to 3 sequentially", queue[3].id, manager.currentTrack.value.id)

            // Simulate Previous from track 3 -> track 2 -> track 1
            sessionPlayer.seekToPreviousMediaItem()
            assertEquals("First Previous must step back from 3 to 2 sequentially", queue[2].id, manager.currentTrack.value.id)

            sessionPlayer.seekToPrevious()
            assertEquals("Second Previous must step back from 2 to 1 sequentially", queue[1].id, manager.currentTrack.value.id)

            // Verify wrap-around from track 0 -> track 4 and track 4 -> track 0
            sessionPlayer.seekToPreviousMediaItem()
            assertEquals(queue[0].id, manager.currentTrack.value.id)
            sessionPlayer.seekToPreviousMediaItem()
            assertEquals("Previous at index 0 should wrap to last item", queue[4].id, manager.currentTrack.value.id)
            sessionPlayer.seekToNext()
            assertEquals("Next at last index should wrap to index 0", queue[0].id, manager.currentTrack.value.id)
        } finally {
            controller.destroy()
        }
    }
}
