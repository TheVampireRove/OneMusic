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

}
