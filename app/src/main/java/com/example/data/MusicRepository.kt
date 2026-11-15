package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.model.CoverArtType
import com.example.model.LyricLine
import com.example.model.Playlist
import com.example.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicRepository {

    private val initialTracks = listOf(
        Track(
            id = "track_1",
            title = "Cosmic Odyssey",
            artist = "Aura Nebula",
            album = "Stellar Journey",
            durationMs = 184000L,
            primaryColor = Color(0xFF7C4DFF),
            secondaryColor = Color(0xFF00E5FF),
            accentColor = Color(0xFFD1C4E9),
            genre = "Synthwave",
            coverType = CoverArtType.COSMIC,
            bpm = 118,
            rootPitchHz = 220f,
            isFavorite = true,
            trackNumber = 1,
            lyrics = listOf(
                LyricLine(0L, "Floating through the violet void"),
                LyricLine(12000L, "Star dust spiraling on solar winds"),
                LyricLine(26000L, "Beyond the horizon of ancient light"),
                LyricLine(42000L, "Every frequency whispers your name"),
                LyricLine(60000L, "Across celestial galaxies we soar"),
                LyricLine(85000L, "Pulsars beating like an open heart"),
                LyricLine(115000L, "In the quiet stillness of deep space"),
                LyricLine(140000L, "We dissolve into the eternal dawn"),
                LyricLine(165000L, "Returning home through starlight")
            )
        ),
        Track(
            id = "track_2",
            title = "Midnight Cafe",
            artist = "Solstice Lo-Fi",
            album = "Rainy Windows",
            durationMs = 162000L,
            primaryColor = Color(0xFFFF6E6C),
            secondaryColor = Color(0xFFFFA726),
            accentColor = Color(0xFFFFE082),
            genre = "Lo-Fi Beats",
            coverType = CoverArtType.LOFI,
            bpm = 86,
            rootPitchHz = 174.6f,
            isFavorite = true,
            trackNumber = 1,
            lyrics = listOf(
                LyricLine(0L, "Raindrops tapping on the glass pane"),
                LyricLine(15000L, "Steam rising from a warm cup of coffee"),
                LyricLine(32000L, "Vinyl scratches spinning slow in the corner"),
                LyricLine(50000L, "Pages turning under amber streetlamps"),
                LyricLine(72000L, "A quiet sanctuary away from the world"),
                LyricLine(98000L, "Where time forgets to rush ahead"),
                LyricLine(124000L, "Just mellow chords and gentle sighs"),
                LyricLine(145000L, "Fading into midnight peace")
            )
        ),
        Track(
            id = "track_3",
            title = "Neon Horizon",
            artist = "Cyberwave 2099",
            album = "Future City",
            durationMs = 198000L,
            primaryColor = Color(0xFFFF2A85),
            secondaryColor = Color(0xFF00D2FF),
            accentColor = Color(0xFFE040FB),
            genre = "Electronic",
            coverType = CoverArtType.NEON,
            bpm = 126,
            rootPitchHz = 261.6f,
            isFavorite = false,
            trackNumber = 1,
            lyrics = listOf(
                LyricLine(0L, "Reflections in the chrome and rain"),
                LyricLine(18000L, "Skyscrapers piercing holographic skies"),
                LyricLine(38000L, "Speeding down the elevated neon highway"),
                LyricLine(62000L, "Electric synths igniting the night"),
                LyricLine(90000L, "We are the architects of the new world"),
                LyricLine(120000L, "Bass resonance vibrating the asphalt"),
                LyricLine(150000L, "Blinding lights guiding our trajectory"),
                LyricLine(175000L, "Infinite horizons ahead")
            )
        ),
        Track(
            id = "track_4",
            title = "Golden Hour",
            artist = "Lumina Coast",
            album = "Warm Breeze",
            durationMs = 175000L,
            primaryColor = Color(0xFFFF9100),
            secondaryColor = Color(0xFFFF5252),
            accentColor = Color(0xFFFFE57F),
            genre = "Indie Chill",
            coverType = CoverArtType.SUNSET,
            bpm = 104,
            rootPitchHz = 196f,
            isFavorite = false,
            trackNumber = 1,
            lyrics = listOf(
                LyricLine(0L, "Golden rays melting into the ocean"),
                LyricLine(16000L, "Barefoot in the cooling sand"),
                LyricLine(34000L, "Warm breezes whispering coastal melodies"),
                LyricLine(56000L, "Watching the sun paint the clouds in gold"),
                LyricLine(82000L, "Moments suspended in peaceful harmony"),
                LyricLine(110000L, "Laughter carried along the shoreline"),
                LyricLine(138000L, "Twilight slowly claiming the horizon"),
                LyricLine(160000L, "Sunsets remember what days forget")
            )
        ),
        Track(
            id = "track_5",
            title = "Ethereal Forest",
            artist = "Moss & Echo",
            album = "Ancient Woodlands",
            durationMs = 210000L,
            primaryColor = Color(0xFF00C853),
            secondaryColor = Color(0xFF00B0FF),
            accentColor = Color(0xFFB9F6CA),
            genre = "Ambient",
            coverType = CoverArtType.FOREST,
            bpm = 90,
            rootPitchHz = 164.8f,
            isFavorite = true,
            trackNumber = 1,
            lyrics = listOf(
                LyricLine(0L, "Morning mist rising from emerald moss"),
                LyricLine(22000L, "Canopy filtering rays of pure emerald"),
                LyricLine(46000L, "Rippling streams singing to aged stones"),
                LyricLine(75000L, "Echoes of woodthrush echoing through leaves"),
                LyricLine(105000L, "Breathe in the timeless scent of cedar"),
                LyricLine(135000L, "Ancient roots cradling quiet earth"),
                LyricLine(165000L, "Drifting deep within nature's cathedral"),
                LyricLine(190000L, "Peace found in leafy shadows")
            )
        ),
        Track(
            id = "track_6",
            title = "Urban Pulse",
            artist = "K-Vibe Syndicate",
            album = "Seoul Nights",
            durationMs = 152000L,
            primaryColor = Color(0xFF651FFF),
            secondaryColor = Color(0xFFF50057),
            accentColor = Color(0xFF80D8FF),
            genre = "K-Pop Beat",
            coverType = CoverArtType.URBAN,
            bpm = 130,
            rootPitchHz = 246.9f,
            isFavorite = false,
            trackNumber = 1,
            lyrics = listOf(
                LyricLine(0L, "Crosswalk countdown flashing green"),
                LyricLine(14000L, "Neon signage illuminating the bustling avenue"),
                LyricLine(30000L, "Headphones on, tuned to the city frequency"),
                LyricLine(52000L, "Fast steps marching to synchronized 808s"),
                LyricLine(76000L, "Energy surging through high-speed lines"),
                LyricLine(102000L, "Midnight markets alive with vivid chatter"),
                LyricLine(126000L, "Electrifying rhythm that never sleeps"),
                LyricLine(142000L, "Tokyo to Seoul in a heartbeat")
            )
        )
    )

    private val _tracks = MutableStateFlow(initialTracks)
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _playlists = MutableStateFlow(
        listOf(
            Playlist(
                id = "pl_1",
                title = "One UI Chill Lounge",
                description = "Smooth ambient tunes and cozy lo-fi vibes",
                trackIds = listOf("track_2", "track_1", "track_5"),
                gradientColors = listOf(Color(0xFFFF6E6C), Color(0xFF7C4DFF))
            ),
            Playlist(
                id = "pl_2",
                title = "Cyber Expressive",
                description = "High energy synthwave and futuristic beats",
                trackIds = listOf("track_3", "track_6", "track_1"),
                gradientColors = listOf(Color(0xFFFF2A85), Color(0xFF00D2FF))
            ),
            Playlist(
                id = "pl_3",
                title = "Nature & Solitude",
                description = "Organic acoustic textures and golden sunsets",
                trackIds = listOf("track_5", "track_4"),
                gradientColors = listOf(Color(0xFF00C853), Color(0xFFFF9100))
            )
        )
    )
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    fun toggleFavorite(trackId: String) {
        var changedLocal = false
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) {
                if (track.isLocalFile) changedLocal = true
                track.copy(isFavorite = !track.isFavorite)
            } else {
                track
            }
        }
        if (changedLocal) {
            appContext?.let { ctx ->
                LocalTracksCache.saveTracks(ctx, _tracks.value)
            }
        }
    }

    fun updateTrackLyrics(trackId: String, lyrics: List<LyricLine>) {
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) track.copy(lyrics = lyrics) else track
        }
    }

    fun addTracks(newTracks: List<Track>) {
        if (newTracks.isEmpty()) return
        val incomingById = newTracks.associateBy { it.id }
        val existingIds = _tracks.value.map { it.id }.toSet()
        val updatedExisting = _tracks.value.map { existing ->
            val incoming = incomingById[existing.id]
            if (incoming != null) {
                val resolvedTrackNumber = if (existing.trackNumber > 0) existing.trackNumber else incoming.trackNumber
                val resolvedDiscNumber = if (existing.discNumber > 0) existing.discNumber else incoming.discNumber
                val resolvedArtKey = existing.albumArtKey ?: incoming.albumArtKey
                val resolvedArtUri = existing.albumArtUri ?: incoming.albumArtUri
                val resolvedHasArt = existing.hasEmbeddedArt || incoming.hasEmbeddedArt
                val resolvedLyrics = existing.lyrics.ifEmpty { incoming.lyrics }
                if (resolvedTrackNumber != existing.trackNumber ||
                    resolvedDiscNumber != existing.discNumber ||
                    resolvedArtKey != existing.albumArtKey ||
                    resolvedArtUri != existing.albumArtUri ||
                    resolvedHasArt != existing.hasEmbeddedArt ||
                    resolvedLyrics != existing.lyrics
                ) {
                    existing.copy(
                        trackNumber = resolvedTrackNumber,
                        discNumber = resolvedDiscNumber,
                        albumArtKey = resolvedArtKey,
                        albumArtUri = resolvedArtUri,
                        hasEmbeddedArt = resolvedHasArt,
                        lyrics = resolvedLyrics
                    )
                } else {
                    existing
                }
            } else {
                existing
            }
        }
        val uniqueNew = newTracks.filter { !existingIds.contains(it.id) }
        if (uniqueNew.isNotEmpty() || updatedExisting != _tracks.value) {
            _tracks.value = uniqueNew + updatedExisting
        }
    }

    fun applyTrackNumberUpdates(updates: Map<String, Pair<Int, Int>>): Boolean {
        if (updates.isEmpty()) return false
        var changed = false
        val next = _tracks.value.map { track ->
            val update = updates[track.id]
            if (update != null) {
                val (newDisc, newTrackNum) = update
                val resolvedTrack = if (track.trackNumber > 0) track.trackNumber else newTrackNum
                val resolvedDisc = if (track.discNumber > 0) track.discNumber else newDisc
                if (resolvedTrack != track.trackNumber || resolvedDisc != track.discNumber) {
                    changed = true
                    track.copy(trackNumber = resolvedTrack, discNumber = resolvedDisc)
                } else {
                    track
                }
            } else {
                track
            }
        }
        if (changed) {
            _tracks.value = next
        }
        return changed
    }

    @androidx.annotation.VisibleForTesting
    internal fun removeTracksByIds(ids: Set<String>) {
        if (ids.isEmpty()) return
        _tracks.value = _tracks.value.filterNot { it.id in ids }
    }

    private var appContext: android.content.Context? = null

    private val _settings = MutableStateFlow(com.example.model.AppSettings())
    val settings: StateFlow<com.example.model.AppSettings> = _settings.asStateFlow()

    fun initContext(context: android.content.Context) {
        synchronized(this) {
            val incoming = context.applicationContext
            if (appContext !== incoming) {
                appContext = incoming
                val cached = AppSettingsCache.loadSettings(incoming)
                _settings.value = cached
            }
        }
    }

    fun updateSettings(transform: (com.example.model.AppSettings) -> com.example.model.AppSettings) {
        synchronized(this) {
            val updated = transform(_settings.value)
            _settings.value = updated
            appContext?.let { ctx ->
                AppSettingsCache.saveSettings(ctx, updated)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: MusicRepository? = null

        fun getInstance(): MusicRepository {
            return instance ?: synchronized(this) {
                instance ?: MusicRepository().also { instance = it }
            }
        }
    }
}

