package com.example.model

import androidx.compose.ui.graphics.Color

enum class RepeatMode {
    OFF, ALL, ONE
}

enum class SoundAlivePreset(val displayName: String, val bass: Float, val mid: Float, val treble: Float) {
    NORMAL("Normal", 0f, 0f, 0f),
    POP("Pop", 2f, 1f, 3f),
    CLASSIC("Classic", 1f, 3f, 2f),
    JAZZ("Jazz", 3f, 2f, 1f),
    ROCK("Rock", 4f, 1f, 4f),
    BASS_BOOST("Bass Boost", 6f, 0f, -1f),
    VOCAL("Vocal Clarity", -1f, 4f, 3f),
    CLUB("Club Live", 5f, 2f, 3f)
}

enum class MusicTab(val title: String) {
    TRACKS("Tracks"),
    PLAYLISTS("Playlists"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    FAVORITES("Favorites")
}

enum class CoverArtType {
    COSMIC, LOFI, NEON, SUNSET, FOREST, URBAN
}

data class LyricWord(
    val timeMs: Long,
    val text: String,
    val endTimeMs: Long? = null
)

data class LyricLine(
    val timeMs: Long,
    val text: String,
    val isSynced: Boolean = true,
    val words: List<LyricWord> = emptyList()
) {
    val hasWordSync: Boolean
        get() = words.isNotEmpty()
}

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val genre: String,
    val coverType: CoverArtType,
    val lyrics: List<LyricLine> = emptyList(),
    val bpm: Int = 120,
    val rootPitchHz: Float = 220f,
    val isFavorite: Boolean = false,
    val uriString: String? = null,
    val isLocalFile: Boolean = false,
    val albumArtUri: String? = null,
    val albumArtKey: String? = null,
    val hasEmbeddedArt: Boolean = false,
    val trackNumber: Int = 0,
    val discNumber: Int = 0
)

val AlbumTrackOrderComparator: Comparator<Track> = compareBy(
    { if (it.trackNumber > 0) 0 else 1 },
    { if (it.discNumber > 0) it.discNumber else 0 },
    { it.trackNumber },
    { it.title.lowercase() }
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String,
    val trackIds: List<String>,
    val gradientColors: List<Color>
)
