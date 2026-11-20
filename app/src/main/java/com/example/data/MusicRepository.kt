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

    data class AddTracksResult(
        val addedTracks: List<Track>,
        val skippedDuplicates: Int,
        val remappedIds: Map<String, String>
    )

    data class DeduplicateResult(
        val removedIds: Set<String>,
        val remappedIds: Map<String, String>
    )

    fun addTracks(newTracks: List<Track>) {
        addTracksWithReport(newTracks)
    }

    fun addTracksWithReport(newTracks: List<Track>): AddTracksResult {
        if (newTracks.isEmpty()) return AddTracksResult(emptyList(), 0, emptyMap())

        // Deduplicate incoming batch against itself first
        val distinctIncoming = mutableListOf<Track>()
        for (candidate in newTracks) {
            val existingIdx = distinctIncoming.indexOfFirst { it.id == candidate.id || areCrossSourceDuplicate(it, candidate) }
            if (existingIdx >= 0) {
                val prev = distinctIncoming[existingIdx]
                val winner = choosePreferredTrack(prev, candidate)
                val loser = if (winner === prev) candidate else prev
                distinctIncoming[existingIdx] = mergeTrackFields(winner, loser)
            } else {
                distinctIncoming.add(candidate)
            }
        }

        val currentList = _tracks.value.toMutableList()
        val addedTracks = mutableListOf<Track>()
        var skippedDuplicates = newTracks.size - distinctIncoming.size
        val remappedIds = mutableMapOf<String, String>()
        var changed = false

        for (incoming in distinctIncoming) {
            val matchIndex = currentList.indexOfFirst { existing ->
                existing.id == incoming.id || areCrossSourceDuplicate(existing, incoming)
            }
            if (matchIndex >= 0) {
                val existing = currentList[matchIndex]
                val shouldUpgradeIdToCanonical =
                    existing.id != incoming.id &&
                        existing.id.startsWith("local_") &&
                        incoming.id.startsWith("device_media_")

                val merged = if (shouldUpgradeIdToCanonical) {
                    remappedIds[existing.id] = incoming.id
                    mergeTrackFields(incoming, existing)
                } else {
                    if (existing.id != incoming.id) {
                        remappedIds[incoming.id] = existing.id
                    }
                    mergeTrackFields(existing, incoming)
                }

                if (merged != existing) {
                    currentList[matchIndex] = merged
                    changed = true
                }
                if (existing.id != incoming.id || existing.isLocalFile) {
                    skippedDuplicates++
                }
            } else {
                addedTracks.add(incoming)
            }
        }

        if (addedTracks.isNotEmpty() || changed) {
            _tracks.value = addedTracks + currentList
        }
        return AddTracksResult(
            addedTracks = addedTracks,
            skippedDuplicates = skippedDuplicates,
            remappedIds = remappedIds
        )
    }

    fun findDuplicateLocalTracks(): List<Track> {
        val current = _tracks.value
        val keepers = mutableListOf<Track>()
        val duplicates = mutableListOf<Track>()
        for (track in current) {
            if (!track.isLocalFile) continue
            val dupOf = keepers.firstOrNull { keeper ->
                keeper.id == track.id || areManualDuplicateCandidate(keeper, track)
            }
            if (dupOf != null) {
                duplicates.add(track)
            } else {
                keepers.add(track)
            }
        }
        return duplicates
    }

    fun removeDuplicateTracks(): DeduplicateResult {
        val current = _tracks.value
        if (current.isEmpty()) return DeduplicateResult(emptySet(), emptyMap())

        val resultList = mutableListOf<Track>()
        val removedIds = mutableSetOf<String>()
        val remappedIds = mutableMapOf<String, String>()

        for (track in current) {
            if (!track.isLocalFile) {
                resultList.add(track)
                continue
            }
            val matchIdx = resultList.indexOfFirst { existing ->
                existing.isLocalFile && (existing.id == track.id || areManualDuplicateCandidate(existing, track))
            }
            if (matchIdx >= 0) {
                val existing = resultList[matchIdx]
                val winner = choosePreferredTrack(existing, track)
                val loser = if (winner.id == existing.id) track else existing
                val mergedWinner = mergeTrackFields(winner, loser)
                resultList[matchIdx] = mergedWinner
                removedIds.add(loser.id)
                if (loser.id != mergedWinner.id) {
                    remappedIds[loser.id] = mergedWinner.id
                }
            } else {
                resultList.add(track)
            }
        }

        if (removedIds.isNotEmpty()) {
            _tracks.value = resultList
        }
        return DeduplicateResult(removedIds = removedIds, remappedIds = remappedIds)
    }

    private fun mergeTrackFields(primary: Track, secondary: Track): Track {
        val resolvedTrackNumber = if (primary.trackNumber > 0) primary.trackNumber else secondary.trackNumber
        val resolvedDiscNumber = if (primary.discNumber > 0) primary.discNumber else secondary.discNumber
        val resolvedArtKey = primary.albumArtKey ?: secondary.albumArtKey
        val resolvedArtUri = primary.albumArtUri ?: secondary.albumArtUri
        val resolvedHasArt = primary.hasEmbeddedArt || secondary.hasEmbeddedArt
        val resolvedLyrics = primary.lyrics.ifEmpty { secondary.lyrics }
        val resolvedFavorite = primary.isFavorite || secondary.isFavorite
        val resolvedArtist = if (!isPlaceholderArtist(primary.artist)) primary.artist else secondary.artist
        val resolvedAlbum = if (!isPlaceholderAlbum(primary.album)) primary.album else secondary.album

        return if (resolvedTrackNumber != primary.trackNumber ||
            resolvedDiscNumber != primary.discNumber ||
            resolvedArtKey != primary.albumArtKey ||
            resolvedArtUri != primary.albumArtUri ||
            resolvedHasArt != primary.hasEmbeddedArt ||
            resolvedLyrics != primary.lyrics ||
            resolvedFavorite != primary.isFavorite ||
            resolvedArtist != primary.artist ||
            resolvedAlbum != primary.album
        ) {
            primary.copy(
                artist = resolvedArtist,
                album = resolvedAlbum,
                trackNumber = resolvedTrackNumber,
                discNumber = resolvedDiscNumber,
                albumArtKey = resolvedArtKey,
                albumArtUri = resolvedArtUri,
                hasEmbeddedArt = resolvedHasArt,
                lyrics = resolvedLyrics,
                isFavorite = resolvedFavorite
            )
        } else {
            primary
        }
    }

    private fun choosePreferredTrack(a: Track, b: Track): Track {
        fun score(t: Track): Int {
            var s = 0
            if (t.id.startsWith("device_media_")) s += 100
            if (t.hasEmbeddedArt) s += 20
            if (t.isFavorite) s += 10
            if (t.lyrics.isNotEmpty()) s += 5
            if (t.trackNumber > 0) s += 3
            if (!isPlaceholderArtist(t.artist)) s += 2
            if (!isPlaceholderAlbum(t.album)) s += 2
            return s
        }
        return if (score(a) >= score(b)) a else b
    }

    private fun areCrossSourceDuplicate(a: Track, b: Track): Boolean {
        if (!a.isLocalFile || !b.isLocalFile) return false
        if (!a.uriString.isNullOrBlank() && a.uriString == b.uriString) return true
        // Never collapse two distinct MediaStore IDs unless their URI matches
        if (a.id.startsWith("device_media_") && b.id.startsWith("device_media_")) return false
        return matchesMetadataFingerprint(a, b)
    }

    private fun areManualDuplicateCandidate(a: Track, b: Track): Boolean {
        if (!a.isLocalFile || !b.isLocalFile) return false
        if (!a.uriString.isNullOrBlank() && a.uriString == b.uriString) return true
        if (a.id.startsWith("device_media_") && b.id.startsWith("device_media_")) return false
        return matchesMetadataFingerprint(a, b)
    }

    private fun matchesMetadataFingerprint(a: Track, b: Track): Boolean {
        val titleA = normalizeTag(a.title)
        val titleB = normalizeTag(b.title)
        if (titleA.isBlank() || titleA != titleB) return false
        if (titleA in setOf("local track", "unknown track", "track", "untitled")) return false

        val durationDiff = kotlin.math.abs(a.durationMs - b.durationMs)
        if (durationDiff > 2500L) return false

        val artistGenericA = isPlaceholderArtist(a.artist)
        val artistGenericB = isPlaceholderArtist(b.artist)
        val artistMatches = when {
            artistGenericA || artistGenericB -> true
            else -> normalizeTag(a.artist) == normalizeTag(b.artist)
        }
        if (!artistMatches) return false

        val albumGenericA = isPlaceholderAlbum(a.album)
        val albumGenericB = isPlaceholderAlbum(b.album)
        return if (!albumGenericA && !albumGenericB) {
            normalizeTag(a.album) == normalizeTag(b.album)
        } else {
            !artistGenericA && !artistGenericB
        }
    }

    private fun normalizeTag(value: String): String =
        value.trim().lowercase().replace(Regex("\\s+"), " ")

    private fun isPlaceholderArtist(artist: String): Boolean =
        normalizeTag(artist) in setOf("", "unknown", "<unknown>", "unknown artist", "local artist")

    private fun isPlaceholderAlbum(album: String): Boolean =
        normalizeTag(album) in setOf("", "unknown", "<unknown>", "unknown album", "device storage", "device music", "device audio")

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

    fun removeTracksByIds(ids: Set<String>): List<Track> {
        if (ids.isEmpty()) return emptyList()
        val removed = _tracks.value.filter { it.id in ids }
        if (removed.isNotEmpty()) {
            _tracks.value = _tracks.value.filterNot { it.id in ids }
        }
        return removed
    }

    fun restoreTracks(tracksToRestore: List<Track>) {
        if (tracksToRestore.isEmpty()) return
        addTracksWithReport(tracksToRestore)
    }

    internal fun seedLegacyTracksRaw(legacyTracks: List<Track>) {
        if (legacyTracks.isEmpty()) return
        _tracks.value = legacyTracks + _tracks.value
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

