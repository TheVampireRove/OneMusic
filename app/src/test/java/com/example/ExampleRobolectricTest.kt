package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("OneMusic", appName)
  }

  @Test
  fun `music repository returns default curated tracks`() {
    val repo = com.example.data.MusicRepository()
    val tracks = repo.tracks.value
    org.junit.Assert.assertTrue(tracks.isNotEmpty())
    val firstTrack = tracks.first()
    assertEquals("Cosmic Odyssey", firstTrack.title)
    assertEquals("Aura Nebula", firstTrack.artist)
    org.junit.Assert.assertTrue(firstTrack.lyrics.isNotEmpty())
  }

  @Test
  fun `music repository can add local tracks`() {
    val repo = com.example.data.MusicRepository()
    val initialCount = repo.tracks.value.size
    val localTrack = com.example.model.Track(
      id = "local_test_1",
      title = "My Local Audio",
      artist = "Device Artist",
      album = "Local Downloads",
      durationMs = 180000L,
      primaryColor = androidx.compose.ui.graphics.Color(0xFF5E35B1),
      secondaryColor = androidx.compose.ui.graphics.Color(0xFF26C6DA),
      accentColor = androidx.compose.ui.graphics.Color(0xFF80CBC4),
      genre = "Local Audio",
      coverType = com.example.model.CoverArtType.NEON,
      isLocalFile = true,
      uriString = "content://media/external/audio/media/999"
    )
    repo.addTracks(listOf(localTrack))
    assertEquals(initialCount + 1, repo.tracks.value.size)
    assertEquals("My Local Audio", repo.tracks.value.first().title)
  }

  @Test
  fun `app settings default values and updates`() {
    val repo = com.example.data.MusicRepository.getInstance()
    val initialSettings = repo.settings.value
    org.junit.Assert.assertTrue(initialSettings.dynamicThemeEnabled)
    assertEquals(com.example.model.DualPaneSetting.AUTO, initialSettings.dualPaneMode)
    assertEquals(com.example.model.AlbumLayoutSetting.GRID_2, initialSettings.albumLayout)
    assertEquals(com.example.model.MusicTab.TRACKS, initialSettings.startupTab)

    repo.updateSettings {
      it.copy(
        dynamicThemeEnabled = false,
        dualPaneMode = com.example.model.DualPaneSetting.ALWAYS,
        albumLayout = com.example.model.AlbumLayoutSetting.LIST,
        startupTab = com.example.model.MusicTab.ALBUMS
      )
    }

    val updatedSettings = repo.settings.value
    org.junit.Assert.assertFalse(updatedSettings.dynamicThemeEnabled)
    assertEquals(com.example.model.DualPaneSetting.ALWAYS, updatedSettings.dualPaneMode)
    assertEquals(com.example.model.AlbumLayoutSetting.LIST, updatedSettings.albumLayout)
    assertEquals(com.example.model.MusicTab.ALBUMS, updatedSettings.startupTab)
  }

  @Test
  fun `audio playback manager coordinates tracks`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = com.example.audio.AudioPlaybackManager.getInstance()
    manager.initContext(context)
    org.junit.Assert.assertNotNull(manager.currentTrack.value)
    val initialTrack = manager.currentTrack.value
    manager.nextTrack()
    val nextTrack = manager.currentTrack.value
    org.junit.Assert.assertNotNull(nextTrack)
  }

  @Test
  fun `settings support android 17 monet and auto flex mode`() {
    val repo = com.example.data.MusicRepository.getInstance()
    val settings = repo.settings.value
    org.junit.Assert.assertTrue(settings.autoFlexMode)
    assertEquals(com.example.model.DynamicThemeEngine.ANDROID_17_MONET, settings.themeEngine)

    repo.updateSettings {
      it.copy(
        themeEngine = com.example.model.DynamicThemeEngine.TRACK_ALBUM_ART,
        autoFlexMode = false
      )
    }
    assertEquals(com.example.model.DynamicThemeEngine.TRACK_ALBUM_ART, repo.settings.value.themeEngine)
    org.junit.Assert.assertFalse(repo.settings.value.autoFlexMode)
  }

  @Test
  fun `album art cache stores and retrieves bitmaps`() {
    val bitmap = android.graphics.Bitmap.createBitmap(10, 10, android.graphics.Bitmap.Config.ARGB_8888)
    com.example.data.AlbumArtCache.put("test_key", bitmap)
    val retrieved = com.example.data.AlbumArtCache.get("test_key")
    assertEquals(10, retrieved?.width)
  }

  @Test
  fun `main activity intent is configured`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = android.content.Intent(context, MainActivity::class.java)
    org.junit.Assert.assertEquals(MainActivity::class.java.name, intent.component?.className)
  }

  @Test
  fun `viewmodel playFromContext sets contextual playback queue and active track`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = com.example.viewmodel.MusicPlayerViewModel()
    viewModel.initContext(context)

    val allTracks = viewModel.tracks.value
    org.junit.Assert.assertTrue(allTracks.size >= 3)
    val albumQueue = allTracks.take(3)

    viewModel.playFromContext(albumQueue, 1)
    assertEquals(albumQueue, viewModel.playbackQueue.value)
    assertEquals(albumQueue[1].id, viewModel.currentTrack.value.id)

    viewModel.nextTrack()
    assertEquals(albumQueue[2].id, viewModel.currentTrack.value.id)

    viewModel.previousTrack()
    assertEquals(albumQueue[1].id, viewModel.currentTrack.value.id)
  }

  @Test
  fun `viewmodel detail selection and tab state remain persistent and independent`() {
    val viewModel = com.example.viewmodel.MusicPlayerViewModel()
    val initialTracks = viewModel.filteredTracks.value
    org.junit.Assert.assertTrue(initialTracks.isNotEmpty())

    viewModel.selectAlbumForDetail("Nebula Horizons")
    assertEquals("Nebula Horizons", viewModel.selectedAlbumId.value)

    viewModel.selectTab(com.example.model.MusicTab.ALBUMS)
    assertEquals(com.example.model.MusicTab.ALBUMS, viewModel.selectedTab.value)
    assertEquals("Nebula Horizons", viewModel.selectedAlbumId.value)
    assertEquals(initialTracks, viewModel.filteredTracks.value)

    viewModel.selectArtistForDetail("Aura Nebula")
    assertEquals("Aura Nebula", viewModel.selectedArtistId.value)
    viewModel.selectAlbumForDetail(null)
    assertEquals(null, viewModel.selectedAlbumId.value)
    assertEquals("Aura Nebula", viewModel.selectedArtistId.value)
  }

  @Test
  fun `record playing visualizer style is registered`() {
    org.junit.Assert.assertTrue(
      com.example.model.VisualizerStyle.entries.contains(com.example.model.VisualizerStyle.RECORD_PLAYING)
    )
  }

  @Test
  fun `audio visualizer manager processes fft into 24 normalized bands and handles missing permission safely`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)
    val manager = com.example.audio.AudioVisualizerManager(scope) { context }

    // Construct synthetic 1024-byte FFT payload with strong low/mid energy
    val fft = ByteArray(1024)
    for (i in 2 until 64 step 2) {
      fft[i] = 48
      fft[i + 1] = 24
    }
    manager.processFftData(fft)

    val bands = manager.visualizerAmplitudes.value
    assertEquals(com.example.audio.AudioVisualizerManager.BAND_COUNT, bands.size)
    org.junit.Assert.assertTrue(bands.all { it in 0.08f..1.0f })
    // Low/mid bands should exceed idle floor due to injected energy
    org.junit.Assert.assertTrue(bands.take(8).any { it > 0.15f })

    // Permission is not granted by default in test context; rebinding with session should safely avoid crashing
    org.junit.Assert.assertFalse(manager.hasRecordAudioPermission(context))
    manager.onAudioSessionIdChanged(123)
    manager.onIsPlayingChanged(true)
    manager.onIsPlayingChanged(false)
    manager.release()
    assertEquals(List(24) { 0.08f }, manager.visualizerAmplitudes.value)
  }

  @Test
  fun `album tracks sort by disc and track number instead of alphabetically`() {
    val t3 = com.example.model.Track(
      id = "a3", title = "Alpha", artist = "Band", album = "LP",
      durationMs = 120000L, primaryColor = androidx.compose.ui.graphics.Color.Cyan,
      secondaryColor = androidx.compose.ui.graphics.Color.Blue, accentColor = androidx.compose.ui.graphics.Color.White,
      genre = "Rock", coverType = com.example.model.CoverArtType.NEON,
      trackNumber = 3, discNumber = 1
    )
    val t1 = t3.copy(id = "a1", title = "Zebra", trackNumber = 1, discNumber = 1)
    val t2 = t3.copy(id = "a2", title = "Middle", trackNumber = 2, discNumber = 1)
    val d2t1 = t3.copy(id = "d2_1", title = "Bonus", trackNumber = 1, discNumber = 2)

    val sorted = listOf(t3, d2t1, t2, t1).sortedWith(com.example.model.AlbumTrackOrderComparator)
    assertEquals(listOf("a1", "a2", "a3", "d2_1"), sorted.map { it.id })

    assertEquals(4, com.example.data.LocalMediaManager.parseTrackNumber("4/12"))
    assertEquals(1 to 4, com.example.data.LocalMediaManager.parseMediaStoreTrack(1004))
    assertEquals(0 to 7, com.example.data.LocalMediaManager.parseMediaStoreTrack(7))
  }

  @Test
  fun `lyrics repository parses synced lrc, plain text, lrclib json, and caches manually saved lyrics`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val track = com.example.model.Track(
      id = "lyrics_test_track_1",
      title = "Test Song",
      artist = "Test Artist",
      album = "Test Album",
      durationMs = 180000L,
      primaryColor = androidx.compose.ui.graphics.Color.Cyan,
      secondaryColor = androidx.compose.ui.graphics.Color.Blue,
      accentColor = androidx.compose.ui.graphics.Color.White,
      genre = "Pop",
      coverType = com.example.model.CoverArtType.COSMIC,
      lyrics = emptyList(),
      isLocalFile = true
    )

    val lrcSample = """
      [ti:Test Song]
      [00:05.50]First synced line
      [00:12.00]Second synced line
    """.trimIndent()
    val parsedSynced = com.example.data.LyricsRepository.parseLyricsContent(lrcSample, track.durationMs)
    assertEquals(2, parsedSynced.size)
    assertEquals(5500L, parsedSynced[0].timeMs)
    assertEquals("First synced line", parsedSynced[0].text)
    org.junit.Assert.assertTrue(parsedSynced[0].isSynced)

    val lrclibJson = """
      {
        "id": 42,
        "trackName": "Test Song",
        "plainLyrics": "Fallback plain line",
        "syncedLyrics": "[00:02.00]Synced from LRCLIB\n[00:08.25]Next line"
      }
    """.trimIndent()
    val extracted = com.example.data.LyricsRepository.parseLrclibJson(lrclibJson)
    org.junit.Assert.assertTrue(extracted?.contains("Synced from LRCLIB") == true)

    val saved = com.example.data.LyricsRepository.saveManualLyrics(context, track, lrcSample)
    assertEquals(2, saved.size)

    val loadedFromTier1 = com.example.data.LyricsRepository.loadCachedOrSideCarLyrics(context, track)
    assertEquals(2, loadedFromTier1.size)
    assertEquals("Second synced line", loadedFromTier1[1].text)
  }

  @Test
  fun `reopening restores last played track, position, and queue instead of defaulting to favorites`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.MusicRepository.getInstance()
    repo.initContext(context)

    val nonFavoriteTrack = repo.tracks.value.first { !it.isFavorite }
    val customQueueIds = listOf(repo.tracks.value.last().id, nonFavoriteTrack.id)
    val manager = com.example.audio.AudioPlaybackManager.getInstance()
    manager.pause()

    repo.updateSettings {
      it.copy(
        lastPlayedTrackId = nonFavoriteTrack.id,
        lastPlayedPositionMs = 45000L,
        lastPlayedQueueIds = customQueueIds
      )
    }

    val loadedSettings = com.example.data.AppSettingsCache.loadSettings(context)
    assertEquals(nonFavoriteTrack.id, loadedSettings.lastPlayedTrackId)
    assertEquals(45000L, loadedSettings.lastPlayedPositionMs)
    assertEquals(customQueueIds, loadedSettings.lastPlayedQueueIds)

    manager.resetForProcessReopenTest()
    // Starting fresh defaults to track_1 (a favorite), then initContext must restore track_3
    assertEquals("track_1", manager.currentTrack.value.id)

    manager.initContext(context)
    assertEquals(nonFavoriteTrack.id, manager.currentTrack.value.id)
    assertEquals(45000L, manager.currentPositionMs.value)
    assertEquals(customQueueIds, manager.playbackQueue.value.map { it.id })
  }

  @Test
  fun `lyrics status tracking skips found, not_found, instrumental and clearFailedStatuses resets them`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.LyricsRepository.getCacheDir(context).listFiles()?.forEach { it.delete() }

    val base = com.example.model.Track(
      id = "status_t1",
      title = "Song One",
      artist = "Artist",
      album = "Album",
      durationMs = 150_000L,
      primaryColor = androidx.compose.ui.graphics.Color.Cyan,
      secondaryColor = androidx.compose.ui.graphics.Color.Blue,
      accentColor = androidx.compose.ui.graphics.Color.White,
      genre = "Pop",
      coverType = com.example.model.CoverArtType.COSMIC,
      lyrics = emptyList()
    )
    val tNotFound = base.copy(id = "status_t2", title = "Missing Song")
    val tInstrumental = base.copy(id = "status_t3", title = "Instrumental Song")
    val tAlreadyLoaded = base.copy(
      id = "status_t4",
      title = "Curated",
      lyrics = listOf(com.example.model.LyricLine(0L, "Already here"))
    )

    com.example.data.LyricsRepository.setTrackStatus(context, tNotFound.id, com.example.data.LyricsFetchStatus.NOT_FOUND)
    com.example.data.LyricsRepository.setTrackStatus(context, tInstrumental.id, com.example.data.LyricsFetchStatus.INSTRUMENTAL)

    val list = listOf(base, tNotFound, tInstrumental, tAlreadyLoaded)
    val needingSync = com.example.data.LyricsRepository.getTracksNeedingSync(context, list)
    assertEquals(listOf("status_t1"), needingSync.map { it.id })

    val summaryBefore = com.example.data.LyricsRepository.getStatusSummary(context, list)
    assertEquals(1, summaryBefore.foundCount)
    assertEquals(1, summaryBefore.notFoundCount)
    assertEquals(1, summaryBefore.instrumentalCount)
    assertEquals(1, summaryBefore.pendingCount)

    val resetCount = com.example.data.LyricsRepository.clearFailedStatuses(context, includeInstrumental = true)
    assertEquals(2, resetCount)

    val needingAfterReset = com.example.data.LyricsRepository.getTracksNeedingSync(context, list)
    assertEquals(listOf("status_t1", "status_t2", "status_t3"), needingAfterReset.map { it.id })
  }

  @Test
  fun `lyrics sync worker fetches sequentially, handles 429 Retry-After, caches lyrics, and reports progress`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.LyricsRepository.getCacheDir(context).listFiles()?.forEach { it.delete() }

    val repo = com.example.data.MusicRepository.getInstance().apply { initContext(context) }
    val trackA = com.example.model.Track(
      id = "worker_track_a",
      title = "Track A",
      artist = "Artist A",
      album = "Album A",
      durationMs = 120_000L,
      primaryColor = androidx.compose.ui.graphics.Color.Cyan,
      secondaryColor = androidx.compose.ui.graphics.Color.Blue,
      accentColor = androidx.compose.ui.graphics.Color.White,
      genre = "Indie",
      coverType = com.example.model.CoverArtType.NEON,
      lyrics = emptyList(),
      isLocalFile = true
    )
    val trackB = trackA.copy(id = "worker_track_b", title = "Track B")
    val trackC = trackA.copy(id = "worker_track_c", title = "Track C")
    repo.addTracks(listOf(trackA, trackB, trackC))

    assertEquals(5000L, com.example.data.LyricsRepository.parseRetryAfterMs("5"))

    val callOrder = mutableListOf<String>()
    var trackAAttempts = 0
    val progressUpdates = mutableListOf<Pair<Int, Int>>()

    try {
      com.example.worker.LyricsSyncWorker.interRequestDelayMsOverride = 1L
      com.example.worker.LyricsSyncWorker.lrclibFetcherOverride = { track ->
        callOrder.add(track.id)
        when (track.id) {
          "worker_track_a" -> {
            trackAAttempts++
            if (trackAAttempts == 1) {
              com.example.data.LrclibLookupResult.RateLimited(1L)
            } else {
              com.example.data.LrclibLookupResult.Found("[00:01.00]Synced line A")
            }
          }
          "worker_track_b" -> com.example.data.LrclibLookupResult.Instrumental
          else -> com.example.data.LrclibLookupResult.NotFound
        }
      }

      val worker = androidx.work.testing.TestListenableWorkerBuilder<com.example.worker.LyricsSyncWorker>(context)
        .setProgressUpdater(androidx.work.ProgressUpdater { _, _, data ->
          progressUpdates.add(
            data.getInt(com.example.worker.LyricsSyncWorker.KEY_CURRENT, 0) to
              data.getInt(com.example.worker.LyricsSyncWorker.KEY_TOTAL, 0)
          )
          object : com.google.common.util.concurrent.ListenableFuture<Void?> {
            override fun addListener(listener: Runnable, executor: java.util.concurrent.Executor) {
              executor.execute(listener)
            }
            override fun cancel(mayInterruptIfRunning: Boolean): Boolean = false
            override fun isCancelled(): Boolean = false
            override fun isDone(): Boolean = true
            override fun get(): Void? = null
            override fun get(timeout: Long, unit: java.util.concurrent.TimeUnit): Void? = null
          }
        })
        .build()

      val result = worker.doWork()
      org.junit.Assert.assertTrue(result is androidx.work.ListenableWorker.Result.Success)
      val output = (result as androidx.work.ListenableWorker.Result.Success).outputData
      assertEquals(1, output.getInt(com.example.worker.LyricsSyncWorker.KEY_FOUND, -1))
      assertEquals(1, output.getInt(com.example.worker.LyricsSyncWorker.KEY_FAILED, -1))

      // Sequential execution: Track A (rate limited then retried), then Track B, then Track C
      assertEquals(listOf("worker_track_a", "worker_track_a", "worker_track_b", "worker_track_c"), callOrder)
      assertEquals(3, progressUpdates.size)
      assertEquals(1 to 3, progressUpdates[0])
      assertEquals(3 to 3, progressUpdates[2])

      assertEquals(
        com.example.data.LyricsFetchStatus.FOUND,
        com.example.data.LyricsRepository.getTrackStatus(context, "worker_track_a")
      )
      assertEquals(
        com.example.data.LyricsFetchStatus.INSTRUMENTAL,
        com.example.data.LyricsRepository.getTrackStatus(context, "worker_track_b")
      )
      assertEquals(
        com.example.data.LyricsFetchStatus.NOT_FOUND,
        com.example.data.LyricsRepository.getTrackStatus(context, "worker_track_c")
      )

      // Subsequent scan skips all three (FOUND, INSTRUMENTAL, NOT_FOUND)
      val remaining = com.example.data.LyricsRepository.getTracksNeedingSync(context, listOf(trackA, trackB, trackC))
      assertEquals(0, remaining.size)
    } finally {
      com.example.worker.LyricsSyncWorker.lrclibFetcherOverride = null
      com.example.worker.LyricsSyncWorker.interRequestDelayMsOverride = null
      repo.removeTracksByIds(setOf("worker_track_a", "worker_track_b", "worker_track_c"))
    }
  }

  @Test
  fun `track number parsing and repository merge backfill missing cached track numbers`() {
    assertEquals(3, com.example.data.LocalMediaManager.parseTrackNumber("3/12"))
    assertEquals(7, com.example.data.LocalMediaManager.parseTrackNumber("Track 07"))
    assertEquals(1, com.example.data.LocalMediaManager.parseTrackNumberFromFilename("01 - All My Life.mp3"))
    assertEquals(12, com.example.data.LocalMediaManager.parseTrackNumberFromFilename("12. Never Too Far.flac"))
    assertEquals(4, com.example.data.LocalMediaManager.parseTrackNumberFromFilename("04 Glitter Medley.m4a"))
    assertEquals(0, com.example.data.LocalMediaManager.parseTrackNumberFromFilename("7 Rings.mp3"))
    assertEquals(0, com.example.data.LocalMediaManager.parseTrackNumberFromFilename("1999.mp3"))

    val repo = com.example.data.MusicRepository()
    val cachedWithoutTrackNum = com.example.model.Track(
      id = "device_media_5001",
      title = "Never Too Far",
      artist = "Mariah Carey",
      album = "Glitter",
      durationMs = 261_000L,
      primaryColor = androidx.compose.ui.graphics.Color.Magenta,
      secondaryColor = androidx.compose.ui.graphics.Color.Cyan,
      accentColor = androidx.compose.ui.graphics.Color.White,
      genre = "R&B",
      coverType = com.example.model.CoverArtType.NEON,
      lyrics = listOf(com.example.model.LyricLine(0L, "Cached lyric")),
      isFavorite = true,
      isLocalFile = true,
      trackNumber = 0,
      discNumber = 0
    )
    repo.addTracks(listOf(cachedWithoutTrackNum))

    // Rescanning supplies trackNumber & discNumber while preserving favorite & lyrics
    val rescanned = cachedWithoutTrackNum.copy(
      isFavorite = false,
      lyrics = emptyList(),
      trackNumber = 10,
      discNumber = 1
    )
    repo.addTracks(listOf(rescanned))

    val merged = repo.tracks.value.first { it.id == "device_media_5001" }
    assertEquals(10, merged.trackNumber)
    assertEquals(1, merged.discNumber)
    org.junit.Assert.assertTrue(merged.isFavorite)
    assertEquals(1, merged.lyrics.size)

    // Direct backfill update also updates trackNumber when initially 0
    val anotherMissing = cachedWithoutTrackNum.copy(id = "device_media_5002", trackNumber = 0, discNumber = 0)
    repo.addTracks(listOf(anotherMissing))
    org.junit.Assert.assertTrue(repo.applyTrackNumberUpdates(mapOf("device_media_5002" to (2 to 5))))
    val updated = repo.tracks.value.first { it.id == "device_media_5002" }
    assertEquals(5, updated.trackNumber)
    assertEquals(2, updated.discNumber)
  }

  @Test
  fun `new visualizer styles and lyrics animation settings persist cleanly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val expectedStyles = listOf(
      com.example.model.VisualizerStyle.KALEIDOSCOPE_MANDALA,
      com.example.model.VisualizerStyle.DNA_DOUBLE_HELIX,
      com.example.model.VisualizerStyle.FIREFLY_SWARM,
      com.example.model.VisualizerStyle.GALAXY_SUPERNOVA,
      com.example.model.VisualizerStyle.OSCILLOSCOPE_LISSAJOUS,
      com.example.model.VisualizerStyle.CASSETTE_TAPE
    )
    org.junit.Assert.assertTrue(com.example.model.VisualizerStyle.entries.containsAll(expectedStyles))
    assertEquals(6, com.example.model.LyricsAnimationStyle.entries.size)
    assertEquals(3, com.example.model.LyricsAnimationSpeed.entries.size)

    val customSettings = com.example.model.AppSettings(
      visualizerStyle = com.example.model.VisualizerStyle.KALEIDOSCOPE_MANDALA,
      lyricsAnimationStyle = com.example.model.LyricsAnimationStyle.KARAOKE_SWEEP,
      lyricsAnimationSpeed = com.example.model.LyricsAnimationSpeed.SNAPPY,
      lyricsGlowEnabled = false
    )
    com.example.data.AppSettingsCache.saveSettings(context, customSettings)
    val loaded = com.example.data.AppSettingsCache.loadSettings(context)

    assertEquals(com.example.model.VisualizerStyle.KALEIDOSCOPE_MANDALA, loaded.visualizerStyle)
    assertEquals(com.example.model.LyricsAnimationStyle.KARAOKE_SWEEP, loaded.lyricsAnimationStyle)
    assertEquals(com.example.model.LyricsAnimationSpeed.SNAPPY, loaded.lyricsAnimationSpeed)
    org.junit.Assert.assertFalse(loaded.lyricsGlowEnabled)
  }

  @Test
  fun `lyrics repository parses enhanced word-synced lrc, inline brackets, shifts offsets, replaces, and clears`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.LyricsRepository.clearCachedLyrics(context, "word_sync_test_1")

    val track = com.example.model.Track(
      id = "word_sync_test_1",
      title = "Word Sync Song",
      artist = "Artist",
      album = "Album",
      durationMs = 180_000L,
      primaryColor = androidx.compose.ui.graphics.Color.Cyan,
      secondaryColor = androidx.compose.ui.graphics.Color.Blue,
      accentColor = androidx.compose.ui.graphics.Color.White,
      genre = "Pop",
      coverType = com.example.model.CoverArtType.NEON,
      lyrics = emptyList(),
      isLocalFile = true
    )

    // 1. Enhanced LRC `<mm:ss.xx>` word sync + trailing end timestamp + repeated chorus line
    val enhancedLrc = """
      [00:10.00]<00:10.00>Hello <00:10.50>bright <00:11.10>world<00:11.80>
      [00:15.00][00:45.00]<00:15.00>Chorus <00:15.60>line<00:16.20>
    """.trimIndent()
    val parsedEnhanced = com.example.data.LyricsRepository.parseLyricsContent(enhancedLrc, track.durationMs)
    assertEquals(3, parsedEnhanced.size)
    assertEquals(10_000L, parsedEnhanced[0].timeMs)
    assertEquals("Hello bright world", parsedEnhanced[0].text)
    assertEquals(3, parsedEnhanced[0].words.size)
    assertEquals("Hello ", parsedEnhanced[0].words[0].text)
    assertEquals(10_000L, parsedEnhanced[0].words[0].timeMs)
    assertEquals(10_500L, parsedEnhanced[0].words[0].endTimeMs)
    assertEquals("world", parsedEnhanced[0].words[2].text)
    assertEquals(11_100L, parsedEnhanced[0].words[2].timeMs)
    assertEquals(11_800L, parsedEnhanced[0].words[2].endTimeMs)

    // Second repeated chorus occurrence shifts word timestamps by +30_000ms
    assertEquals(45_000L, parsedEnhanced[2].timeMs)
    assertEquals(45_000L, parsedEnhanced[2].words[0].timeMs)
    assertEquals(45_600L, parsedEnhanced[2].words[1].timeMs)
    assertEquals(46_200L, parsedEnhanced[2].words[1].endTimeMs)

    // 2. Inline `[mm:ss.xx]word` format without `<...>` tags vs repeated chorus `[00:05.00][00:35.00]Same`
    val inlineAndChorus = """
      [00:02.00]One [00:02.40]two [00:02.90]three [00:03.40]
      [00:05.00][00:35.00]Repeated line
    """.trimIndent()
    val parsedInline = com.example.data.LyricsRepository.parseLyricsContent(inlineAndChorus, track.durationMs)
    assertEquals(3, parsedInline.size)
    assertEquals("One two three", parsedInline[0].text)
    assertEquals(3, parsedInline[0].words.size)
    assertEquals(2_000L, parsedInline[0].words[0].timeMs)
    assertEquals(2_400L, parsedInline[0].words[1].timeMs)
    assertEquals(3_400L, parsedInline[0].words[2].endTimeMs)
    assertEquals("Repeated line", parsedInline[1].text)
    assertEquals(0, parsedInline[1].words.size)
    assertEquals(35_000L, parsedInline[2].timeMs)

    // 3. Offset shifting updates both line and word timestamps
    val shifted = com.example.data.LyricsRepository.shiftLrcTimestamps("[00:10.00]<00:10.00>Hi <00:10.50>there", 500L)
    assertEquals("[00:10.50]<00:10.50>Hi <00:11.00>there", shifted)

    // 4. Saving wrong lyrics, retrieving editable raw text, replacing with word-synced lyrics, then clearing
    com.example.data.LyricsRepository.saveManualLyrics(context, track, "[00:01.00]Wrong old lyrics")
    assertEquals("[00:01.00]Wrong old lyrics", com.example.data.LyricsRepository.getEditableRawLyrics(context, track))

    val replaced = com.example.data.LyricsRepository.saveManualLyrics(context, track, enhancedLrc)
    assertEquals(3, replaced.size)
    assertEquals(enhancedLrc, com.example.data.LyricsRepository.getEditableRawLyrics(context, track))
    assertEquals(com.example.data.LyricsFetchStatus.FOUND, com.example.data.LyricsRepository.getTrackStatus(context, track.id))

    com.example.data.LyricsRepository.clearCachedLyrics(context, track.id)
    assertEquals(com.example.data.LyricsFetchStatus.NOT_FETCHED, com.example.data.LyricsRepository.getTrackStatus(context, track.id))
    assertEquals("", com.example.data.LyricsRepository.getEditableRawLyrics(context, track))
  }

  @Test
  fun `audio engine auto-recovers after released player and preserves playback state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = com.example.audio.AudioPlaybackManager.getInstance()
    manager.initContext(context)
    manager.ensureActivePlayer()

    val firstPlayer = manager.audioEngine.exoPlayer
    org.junit.Assert.assertNotNull(firstPlayer)
    org.junit.Assert.assertTrue(manager.audioEngine.isCurrentlyAlive())

    // Simulate OS / service killing the underlying ExoPlayer instance after inactivity
    manager.audioEngine.markCurrentPlayerDeadForRecovery()
    org.junit.Assert.assertFalse(manager.audioEngine.isCurrentlyAlive())

    // Returning to app calls ensureActivePlayer(), which must seamlessly recreate a live ExoPlayer
    manager.ensureActivePlayer()
    val recoveredPlayer = manager.audioEngine.exoPlayer
    org.junit.Assert.assertNotNull(recoveredPlayer)
    org.junit.Assert.assertNotSame(firstPlayer, recoveredPlayer)
    org.junit.Assert.assertTrue(manager.audioEngine.isCurrentlyAlive())
  }

  @Test
  fun `android auto service paginates safely, searches tracks, and onDestroy does not kill in-app player`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = com.example.audio.AudioPlaybackManager.getInstance()
    manager.initContext(context)
    manager.ensureActivePlayer()

    val controller = org.robolectric.Robolectric.buildService(com.example.service.OneMusicAutoService::class.java)
    val service = controller.create().get()

    val largeList = (1..400).toList()
    val maxSizedPage = service.paginateList(largeList, 0, Int.MAX_VALUE)
    assertEquals(150, maxSizedPage.size)

    val pageTwo = service.paginateList(largeList, 1, 100)
    assertEquals(100, pageTwo.size)
    assertEquals(101, pageTwo.first())

    val rootChildren = service.buildChildrenPage(com.example.service.OneMusicAutoService.ROOT_ID, 0, 20)
    assertEquals(4, rootChildren.size)

    val searchMatches = service.findMatchingTracks("Cosmic")
    org.junit.Assert.assertTrue(searchMatches.any { it.title.contains("Cosmic", ignoreCase = true) })

    // Destroying the media service after idle disconnects must leave in-app ExoPlayer alive
    controller.destroy()
    org.junit.Assert.assertTrue(manager.audioEngine.isCurrentlyAlive())
  }

  @Test
  fun `animated art repository resolves sidecar mp4, imports gif cover, clears cache, and persists settings`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.AnimatedArtRepository.clearCache(context)

    val tempDir = java.io.File(context.cacheDir, "sidecar_art_test").apply {
      deleteRecursively()
      mkdirs()
    }
    try {
      val audioFile = java.io.File(tempDir, "song.mp3").apply { writeBytes(byteArrayOf(1, 2, 3)) }
      val sidecarMp4 = java.io.File(tempDir, "cover.mp4").apply { writeBytes(byteArrayOf(0, 0, 0, 24)) }
      val trackWithSidecar = com.example.model.Track(
        id = "anim_sidecar_1",
        title = "Sidecar Track",
        artist = "Folder Artist",
        album = "Folder Album",
        durationMs = 120_000L,
        primaryColor = androidx.compose.ui.graphics.Color.Cyan,
        secondaryColor = androidx.compose.ui.graphics.Color.Blue,
        accentColor = androidx.compose.ui.graphics.Color.White,
        genre = "Electronic",
        coverType = com.example.model.CoverArtType.NEON,
        isLocalFile = true,
        uriString = android.net.Uri.fromFile(audioFile).toString()
      )

      val resolvedSidecar = com.example.data.AnimatedArtRepository.resolveAnimatedCover(
        context = context,
        track = trackWithSidecar,
        enableAnimatedCovers = true,
        autoFetchWifiOnly = true
      )
      org.junit.Assert.assertTrue(resolvedSidecar is com.example.data.AnimatedCoverMedia.VideoLoop)
      assertEquals(sidecarMp4.absolutePath, (resolvedSidecar as com.example.data.AnimatedCoverMedia.VideoLoop).localPath)

      val disabledResult = com.example.data.AnimatedArtRepository.resolveAnimatedCover(
        context = context,
        track = trackWithSidecar,
        enableAnimatedCovers = false,
        autoFetchWifiOnly = true
      )
      assertEquals(com.example.data.AnimatedCoverMedia.None, disabledResult)

      // Import custom animated GIF cover for an album without sidecar
      val gifSource = java.io.File(tempDir, "loop.gif").apply {
        writeBytes("GIF89a".toByteArray())
      }
      val customTrack = trackWithSidecar.copy(
        id = "anim_import_2",
        album = "Imported GIF Album",
        uriString = null
      )
      val imported = com.example.data.AnimatedArtRepository.importAnimatedCover(
        context = context,
        track = customTrack,
        sourceUri = android.net.Uri.fromFile(gifSource)
      )
      org.junit.Assert.assertTrue(imported is com.example.data.AnimatedCoverMedia.AnimatedImage)
      org.junit.Assert.assertTrue(
        com.example.data.AnimatedArtRepository.getCachedOrSidecarImmediate(context, customTrack)
          is com.example.data.AnimatedCoverMedia.AnimatedImage
      )

      val statsBeforeClear = com.example.data.AnimatedArtRepository.getCacheStats(context)
      assertEquals(1, statsBeforeClear.first)
      org.junit.Assert.assertTrue(statsBeforeClear.second > 0L)

      val cleared = com.example.data.AnimatedArtRepository.clearCache(context)
      assertEquals(1, cleared)
      assertEquals(0, com.example.data.AnimatedArtRepository.getCacheStats(context).first)

      // Settings persistence for animated covers
      val customSettings = com.example.model.AppSettings(
        enableAnimatedCovers = false,
        autoFetchAnimatedCoversWifiOnly = false,
        simulateMotionOnStaticCovers = false
      )
      com.example.data.AppSettingsCache.saveSettings(context, customSettings)
      val loaded = com.example.data.AppSettingsCache.loadSettings(context)
      org.junit.Assert.assertFalse(loaded.enableAnimatedCovers)
      org.junit.Assert.assertFalse(loaded.autoFetchAnimatedCoversWifiOnly)
      org.junit.Assert.assertFalse(loaded.simulateMotionOnStaticCovers)
    } finally {
      tempDir.deleteRecursively()
      com.example.data.AnimatedArtRepository.clearCache(context)
    }
  }

  @Test
  fun `viewmodel preserves display mode seamlessly when entering and exiting tabletop mode`() {
    val viewModel = com.example.viewmodel.MusicPlayerViewModel()
    assertEquals(com.example.ui.components.DisplayMode.ARTWORK, viewModel.displayMode.value)

    viewModel.setDisplayMode(com.example.ui.components.DisplayMode.LYRICS)
    viewModel.setFlexMode(true)
    assertEquals(com.example.ui.components.DisplayMode.LYRICS, viewModel.displayMode.value)

    viewModel.setDisplayMode(com.example.ui.components.DisplayMode.VISUALIZER)
    viewModel.setFlexMode(false)
    assertEquals(com.example.ui.components.DisplayMode.VISUALIZER, viewModel.displayMode.value)
  }

  @Test
  fun `artwork matcher strictly verifies artist album and track and restore default permanently prevents re-fetch`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.AnimatedArtRepository.clearCache(context)

    val lutherTrack = com.example.model.Track(
      id = "luther_1",
      title = "Love Won't Let Me Wait",
      artist = "Luther Vandross",
      album = "The Best of Luther Vandross... The Best of Love",
      durationMs = 439_000L,
      primaryColor = androidx.compose.ui.graphics.Color.Cyan,
      secondaryColor = androidx.compose.ui.graphics.Color.Blue,
      accentColor = androidx.compose.ui.graphics.Color.White,
      genre = "R&B",
      coverType = com.example.model.CoverArtType.NEON,
      isLocalFile = true,
      hasEmbeddedArt = true
    )

    // 1. Mismatched artist & album (Best of Alexander O'Neal) MUST be rejected
    val wrongOnlyJson = """
      {
        "resultCount": 1,
        "results": [
          {
            "artistName": "Alexander O'Neal",
            "collectionName": "Best of Alexander O'Neal",
            "artworkUrl100": "https://example.com/alexander_100x100bb.jpg"
          }
        ]
      }
    """.trimIndent()
    org.junit.Assert.assertNull(
      com.example.data.AnimatedArtRepository.pickBestVerifiedAlbumCandidate(lutherTrack, wrongOnlyJson)
    )

    // 2. Same artist with wrong album MUST be rejected
    val wrongAlbumSameArtistJson = """
      {
        "resultCount": 1,
        "results": [
          {
            "artistName": "Luther Vandross",
            "collectionName": "Dance with My Father",
            "artworkUrl100": "https://example.com/dance_100x100bb.jpg"
          }
        ]
      }
    """.trimIndent()
    org.junit.Assert.assertNull(
      com.example.data.AnimatedArtRepository.pickBestVerifiedAlbumCandidate(lutherTrack, wrongAlbumSameArtistJson)
    )

    // 3. Mixed results: skips mismatched Alexander O'Neal at index 0 and selects verified Luther Vandross at index 1
    val mixedJson = """
      {
        "resultCount": 2,
        "results": [
          {
            "artistName": "Alexander O'Neal",
            "collectionName": "Best of Alexander O'Neal",
            "artworkUrl100": "https://example.com/alexander_100x100bb.jpg"
          },
          {
            "artistName": "Luther Vandross",
            "collectionName": "The Best of Luther Vandross",
            "artworkUrl100": "https://example.com/luther_100x100bb.jpg"
          }
        ]
      }
    """.trimIndent()
    val picked = com.example.data.AnimatedArtRepository.pickBestVerifiedAlbumCandidate(lutherTrack, mixedJson)
    org.junit.Assert.assertNotNull(picked)
    assertEquals("https://example.com/luther_1000x1000bb.jpg", picked?.url)

    // 4. Song fallback verifies artist + track title and refuses unrelated compilation albums when track has a real album
    val songFallbackJson = """
      {
        "resultCount": 2,
        "results": [
          {
            "artistName": "Luther Vandross",
            "trackName": "Love Won't Let Me Wait",
            "collectionName": "80s Soul Classics Compilation",
            "artworkUrl100": "https://example.com/comp_100x100bb.jpg"
          },
          {
            "artistName": "Luther Vandross",
            "trackName": "Love Won't Let Me Wait",
            "collectionName": "The Best of Luther Vandross... The Best of Love",
            "artworkUrl100": "https://example.com/luther_song_100x100bb.jpg"
          }
        ]
      }
    """.trimIndent()
    val songPicked = com.example.data.AnimatedArtRepository.pickBestVerifiedSongCandidate(lutherTrack, songFallbackJson)
    org.junit.Assert.assertNotNull(songPicked)
    assertEquals("https://example.com/luther_song_1000x1000bb.jpg", songPicked?.url)

    // Unknown album tracks must not share the same cache key
    val genericA = lutherTrack.copy(id = "g1", title = "Song A", album = "Unknown Album")
    val genericB = lutherTrack.copy(id = "g2", title = "Song B", album = "Unknown Album")
    org.junit.Assert.assertNotEquals(
      com.example.data.AnimatedArtRepository.coverCacheKey(genericA),
      com.example.data.AnimatedArtRepository.coverCacheKey(genericB)
    )

    // 5. Restore Default Artwork deletes custom/cached cover AND prevents auto-fetch from re-downloading it
    val tempDir = java.io.File(context.cacheDir, "restore_default_test").apply {
      deleteRecursively()
      mkdirs()
    }
    try {
      val customJpg = java.io.File(tempDir, "custom.jpg").apply { writeBytes(byteArrayOf(1, 2, 3, 4)) }
      val imported = com.example.data.AnimatedArtRepository.importAnimatedCover(
        context = context,
        track = lutherTrack,
        sourceUri = android.net.Uri.fromFile(customJpg)
      )
      org.junit.Assert.assertTrue(imported is com.example.data.AnimatedCoverMedia.HighResArtwork)
      org.junit.Assert.assertNotEquals(
        com.example.data.AnimatedCoverMedia.None,
        com.example.data.AnimatedArtRepository.getCachedOrSidecarImmediate(context, lutherTrack)
      )

      var networkFetchInvoked = false
      com.example.data.AnimatedArtRepository.networkFetcherOverride = {
        networkFetchInvoked = true
        com.example.data.AnimatedCoverCandidate(
          url = "https://example.com/should_not_refetch.jpg",
          kind = com.example.data.AnimatedCoverCandidate.Kind.HIGH_RES_IMAGE
        )
      }

      com.example.data.AnimatedArtRepository.removeCustomCover(context, lutherTrack)
      org.junit.Assert.assertTrue(com.example.data.AnimatedArtRepository.isRestoredToDefault(context, lutherTrack))
      assertEquals(
        com.example.data.AnimatedCoverMedia.None,
        com.example.data.AnimatedArtRepository.getCachedOrSidecarImmediate(context, lutherTrack)
      )

      // Resolving again (as AnimatedAlbumCover's LaunchedEffect does on cacheVersion bump) must stay None
      val afterRestore = com.example.data.AnimatedArtRepository.resolveAnimatedCover(
        context = context,
        track = lutherTrack,
        enableAnimatedCovers = true,
        autoFetchWifiOnly = false
      )
      assertEquals(com.example.data.AnimatedCoverMedia.None, afterRestore)
      org.junit.Assert.assertFalse(networkFetchInvoked)

      // Explicitly importing a new custom file later clears the restore-default suppression
      val reImported = com.example.data.AnimatedArtRepository.importAnimatedCover(
        context = context,
        track = lutherTrack,
        sourceUri = android.net.Uri.fromFile(customJpg)
      )
      org.junit.Assert.assertTrue(reImported is com.example.data.AnimatedCoverMedia.HighResArtwork)
      org.junit.Assert.assertFalse(com.example.data.AnimatedArtRepository.isRestoredToDefault(context, lutherTrack))
    } finally {
      com.example.data.AnimatedArtRepository.networkFetcherOverride = null
      tempDir.deleteRecursively()
      com.example.data.AnimatedArtRepository.clearCache(context)
    }
  }

  @Test
  fun `import track id resolution is deterministic and avoids duplicate local and scanned tracks`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // 1. Android MediaStore audio URI resolves directly to canonical device_media_<id>
    val mediaStoreUri = android.net.Uri.parse("content://media/external/audio/media/918273")
    assertEquals(
      "device_media_918273",
      com.example.data.LocalMediaManager.resolveImportTrackId(context, mediaStoreUri)
    )

    // 2. DocumentsProvider media document URI (com.android.providers.media.documents audio:<id>) resolves canonically
    val mediaDocUri = android.provider.DocumentsContract.buildDocumentUri(
      "com.android.providers.media.documents",
      "audio:556677"
    )
    assertEquals(
      "device_media_556677",
      com.example.data.LocalMediaManager.resolveImportTrackId(context, mediaDocUri)
    )

    // 3. Generic storage document URI produces a deterministic local_<hash> instead of random UUIDs
    val externalUri = android.net.Uri.parse("content://com.android.externalstorage.documents/document/primary%3AMusic%2FTrack.mp3")
    val firstHashId = com.example.data.LocalMediaManager.resolveImportTrackId(context, externalUri)
    val secondHashId = com.example.data.LocalMediaManager.resolveImportTrackId(context, externalUri)
    org.junit.Assert.assertTrue(firstHashId.startsWith("local_"))
    assertEquals(firstHashId, secondHashId)
  }

  @Test
  fun `repository deduplicates cross-source imports, upgrades local to canonical device id, and supports undo removal`() {
    val repo = com.example.data.MusicRepository.getInstance()
    val pickedId = "local_deadbeef1234567890abcd"
    val scannedId = "device_media_88001"
    val legacyDupId = "local_legacy_dup_88001"

    try {
      // Ensure clean starting state for test IDs
      repo.removeTracksByIds(setOf(pickedId, scannedId, legacyDupId))

      val pickedTrack = com.example.model.Track(
        id = pickedId,
        title = "midnight drive",
        artist = "Neon Echoes",
        album = "Device Storage",
        durationMs = 204_000L,
        primaryColor = androidx.compose.ui.graphics.Color.Cyan,
        secondaryColor = androidx.compose.ui.graphics.Color.Blue,
        accentColor = androidx.compose.ui.graphics.Color.White,
        genre = "Audio File",
        coverType = com.example.model.CoverArtType.NEON,
        uriString = "content://com.android.externalstorage.documents/document/primary%3AMidnightDrive.flac",
        lyrics = listOf(com.example.model.LyricLine(0L, "Saved user lyric")),
        isFavorite = true,
        isLocalFile = true
      )

      // First import succeeds cleanly
      val firstImport = repo.addTracksWithReport(listOf(pickedTrack))
      assertEquals(1, firstImport.addedTracks.size)
      assertEquals(0, firstImport.skippedDuplicates)

      // Re-importing the exact same track skips creating a duplicate
      val duplicateImport = repo.addTracksWithReport(listOf(pickedTrack))
      assertEquals(0, duplicateImport.addedTracks.size)
      assertEquals(1, duplicateImport.skippedDuplicates)

      // Scanning Storage later discovers the same audio file via MediaStore ID `device_media_88001`
      val scannedMatch = pickedTrack.copy(
        id = scannedId,
        title = "Midnight Drive",
        album = "City Lights EP",
        durationMs = 204_800L,
        uriString = "content://media/external/audio/media/88001",
        lyrics = emptyList(),
        isFavorite = false,
        trackNumber = 3,
        discNumber = 1
      )
      val scanResult = repo.addTracksWithReport(listOf(scannedMatch))
      assertEquals(0, scanResult.addedTracks.size)
      assertEquals(scannedId, scanResult.remappedIds[pickedId])

      // Upheld canonical track preserves user favorite + lyrics while adopting canonical ID & metadata
      org.junit.Assert.assertNull(repo.tracks.value.find { it.id == pickedId })
      val upgraded = repo.tracks.value.first { it.id == scannedId }
      org.junit.Assert.assertTrue(upgraded.isFavorite)
      assertEquals(1, upgraded.lyrics.size)
      assertEquals("City Lights EP", upgraded.album)
      assertEquals(3, upgraded.trackNumber)

      // Simulate legacy pre-fix duplicate entry and verify findDuplicateLocalTracks + removeDuplicateTracks merges it
      repo.seedLegacyTracksRaw(
        listOf(
          pickedTrack.copy(
            id = legacyDupId,
            title = "Midnight Drive",
            album = "City Lights EP",
            durationMs = 204_000L,
            isFavorite = false,
            lyrics = emptyList()
          )
        )
      )
      val detectedDups = repo.findDuplicateLocalTracks()
      org.junit.Assert.assertTrue(detectedDups.isNotEmpty())

      val dedupResult = repo.removeDuplicateTracks()
      org.junit.Assert.assertTrue(dedupResult.removedIds.contains(legacyDupId))
      org.junit.Assert.assertNull(repo.tracks.value.find { it.id == legacyDupId })
      org.junit.Assert.assertNotNull(repo.tracks.value.find { it.id == scannedId })

      // Undo / explicit removal removes the imported track from the repository cleanly
      val removed = repo.removeTracksByIds(setOf(scannedId))
      assertEquals(1, removed.size)
      org.junit.Assert.assertNull(repo.tracks.value.find { it.id == scannedId })
    } finally {
      repo.removeTracksByIds(setOf(pickedId, scannedId, legacyDupId))
    }
  }

  @Test
  fun `playback manager remaps upgraded ids and removes deleted imported tracks from queue`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.MusicRepository.getInstance()
    val manager = com.example.audio.AudioPlaybackManager.getInstance()
    manager.initContext(context)

    val tempLocalId = "local_queue_test_11"
    val canonicalId = "device_media_77441"

    try {
      repo.removeTracksByIds(setOf(tempLocalId, canonicalId))
      val imported = com.example.model.Track(
        id = tempLocalId,
        title = "Queue Test Song",
        artist = "Queue Artist",
        album = "Device Storage",
        durationMs = 180_000L,
        primaryColor = androidx.compose.ui.graphics.Color.Magenta,
        secondaryColor = androidx.compose.ui.graphics.Color.Cyan,
        accentColor = androidx.compose.ui.graphics.Color.White,
        genre = "Audio File",
        coverType = com.example.model.CoverArtType.NEON,
        isLocalFile = true
      )
      repo.addTracksWithReport(listOf(imported))
      manager.playTrack(imported)
      assertEquals(tempLocalId, manager.currentTrack.value.id)

      // Scan Storage upgrades ID to canonicalId and remaps playback queue + active track
      val scanned = imported.copy(id = canonicalId, album = "Real Album")
      val result = repo.addTracksWithReport(listOf(scanned))
      manager.removeOrRemapTracks(removedIds = emptySet(), remappedIds = result.remappedIds)
      assertEquals(canonicalId, manager.currentTrack.value.id)
      org.junit.Assert.assertTrue(manager.playbackQueue.value.any { it.id == canonicalId })
      org.junit.Assert.assertFalse(manager.playbackQueue.value.any { it.id == tempLocalId })

      // Removing/undoing the track drops it from queue and falls back safely
      repo.removeTracksByIds(setOf(canonicalId))
      manager.removeOrRemapTracks(removedIds = setOf(canonicalId))
      org.junit.Assert.assertNotEquals(canonicalId, manager.currentTrack.value.id)
      org.junit.Assert.assertFalse(manager.playbackQueue.value.any { it.id == canonicalId })
    } finally {
      repo.removeTracksByIds(setOf(tempLocalId, canonicalId))
    }
  }

}

