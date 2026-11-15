package com.example.data

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.Color
import com.example.model.CoverArtType
import com.example.model.Track
import java.util.UUID

object LocalMediaManager {

    private val colorPalettes = listOf(
        Triple(Color(0xFF7C4DFF), Color(0xFF00E5FF), Color(0xFFD1C4E9)),
        Triple(Color(0xFFFF6E6C), Color(0xFFFFA726), Color(0xFFFFE082)),
        Triple(Color(0xFFFF2A85), Color(0xFF00D2FF), Color(0xFFE040FB)),
        Triple(Color(0xFFFF9100), Color(0xFFFF5252), Color(0xFFFFE57F)),
        Triple(Color(0xFF00C853), Color(0xFF00B0FF), Color(0xFFB9F6CA)),
        Triple(Color(0xFF651FFF), Color(0xFFF50057), Color(0xFF80D8FF)),
        Triple(Color(0xFF00B0FF), Color(0xFF7C4DFF), Color(0xFFB388FF)),
        Triple(Color(0xFFFF4081), Color(0xFFFFAB40), Color(0xFFFFD180))
    )

    fun parseTrackNumber(raw: String?): Int {
        if (raw.isNullOrBlank()) return 0
        val head = raw.trim().substringBefore('/').trim()
        head.toIntOrNull()?.let { return it.coerceAtLeast(0) }
        return Regex("\\d+").find(head)?.value?.toIntOrNull()?.coerceAtLeast(0) ?: 0
    }

    fun parseTrackNumberFromFilename(name: String?): Int {
        if (name.isNullOrBlank()) return 0
        val base = name.trim().substringAfterLast('/').substringBeforeLast('.')
        // Match leading track prefixes like "01 - Title", "1. Title", "02_Title", or zero-padded "03 Title"
        val delimitedMatch = Regex("^0?([1-9][0-9]?)\\s*[-._)]+\\s*.+").find(base)
        if (delimitedMatch != null) {
            return delimitedMatch.groupValues[1].toIntOrNull()?.coerceAtLeast(0) ?: 0
        }
        val zeroPaddedMatch = Regex("^0([1-9])\\s+.+").find(base)
        if (zeroPaddedMatch != null) {
            return zeroPaddedMatch.groupValues[1].toIntOrNull()?.coerceAtLeast(0) ?: 0
        }
        return 0
    }

    fun parseMediaStoreTrack(rawTrack: Int): Pair<Int, Int> {
        if (rawTrack <= 0) return 0 to 0
        return if (rawTrack >= 1000) {
            (rawTrack / 1000) to (rawTrack % 1000)
        } else {
            0 to rawTrack
        }
    }

    fun importAudioFromUris(context: Context, uris: List<Uri>): List<Track> {
        val importedTracks = mutableListOf<Track>()

        for (uri in uris) {
            try {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}

                var displayName: String? = null
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex != -1) {
                                displayName = cursor.getString(nameIndex)
                            }
                        }
                    }
                } catch (_: Exception) {}

                var title = displayName?.substringBeforeLast(".") ?: "Local Track"
                var artist = "Local Artist"
                var album = "Device Storage"
                var durationMs = 180000L
                var genre = "Audio File"
                var trackNumber = 0
                var discNumber = 0

                val trackId = "local_${UUID.randomUUID()}"
                var hasArt = false
                val artKey = "art_$trackId"
                var extractedPrimary: Color? = null
                var extractedSecondary: Color? = null
                var extractedAccent: Color? = null

                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)
                    val metaTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    if (!metaTitle.isNullOrBlank()) title = metaTitle

                    val metaArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    if (!metaArtist.isNullOrBlank()) artist = metaArtist

                    val metaAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                    if (!metaAlbum.isNullOrBlank()) album = metaAlbum

                    val metaDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    metaDuration?.toLongOrNull()?.let {
                        if (it > 0) durationMs = it
                    }

                    val metaGenre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                    if (!metaGenre.isNullOrBlank()) genre = metaGenre

                    trackNumber = parseTrackNumber(
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                    )
                    discNumber = parseTrackNumber(
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                    )

                    val picture = retriever.embeddedPicture
                    if (picture != null) {
                        try {
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(picture, 0, picture.size)
                            if (bitmap != null) {
                                AlbumArtCache.put(artKey, bitmap)
                                hasArt = true
                                val (p, s, a) = AlbumArtCache.extractExpressivePalette(bitmap)
                                extractedPrimary = p
                                extractedSecondary = s
                                extractedAccent = a
                            }
                        } catch (_: Exception) {}
                    }
                } catch (_: Exception) {
                } finally {
                    try {
                        retriever.release()
                    } catch (_: Exception) {}
                }

                if (trackNumber <= 0) {
                    trackNumber = parseTrackNumberFromFilename(displayName)
                }

                val paletteIndex = kotlin.math.abs(title.hashCode()) % colorPalettes.size
                val (defaultPrimary, defaultSecondary, defaultAccent) = colorPalettes[paletteIndex]
                val coverTypes = CoverArtType.entries.toTypedArray()
                val coverType = coverTypes[kotlin.math.abs(title.hashCode()) % coverTypes.size]

                val baseTrack = Track(
                    id = trackId,
                    title = title,
                    artist = artist,
                    album = album,
                    durationMs = durationMs,
                    primaryColor = extractedPrimary ?: defaultPrimary,
                    secondaryColor = extractedSecondary ?: defaultSecondary,
                    accentColor = extractedAccent ?: defaultAccent,
                    genre = genre,
                    coverType = coverType,
                    lyrics = emptyList(),
                    bpm = 120,
                    rootPitchHz = 220f,
                    isFavorite = false,
                    uriString = uri.toString(),
                    isLocalFile = true,
                    albumArtUri = uri.toString(),
                    albumArtKey = if (hasArt) artKey else null,
                    hasEmbeddedArt = hasArt,
                    trackNumber = trackNumber,
                    discNumber = discNumber
                )
                val tier1Lyrics = LyricsRepository.loadCachedOrSideCarLyrics(context, baseTrack)
                importedTracks.add(baseTrack.copy(lyrics = tier1Lyrics))
            } catch (_: Exception) {
                // Continue with other files if one fails
            }
        }

        return importedTracks
    }

    fun scanDeviceAudio(context: Context): List<Track> {
        val tracks = mutableListOf<Track>()
        val projectionList = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DISPLAY_NAME
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projectionList.add(MediaStore.Audio.Media.CD_TRACK_NUMBER)
            projectionList.add(MediaStore.Audio.Media.DISC_NUMBER)
        }
        val projection = projectionList.toTypedArray()

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val displayNameCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val cdTrackCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.CD_TRACK_NUMBER)
                } else -1
                val discNumCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.DISC_NUMBER)
                } else -1

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Track"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: "Device Music"
                    val duration = cursor.getLong(durationCol)
                    val albumId = if (albumIdCol != -1) cursor.getLong(albumIdCol) else -1L
                    val rawMediaStoreTrack = if (trackCol != -1) cursor.getInt(trackCol) else 0
                    val displayName = if (displayNameCol != -1) cursor.getString(displayNameCol) else null
                    var (discNumber, trackNumber) = parseMediaStoreTrack(rawMediaStoreTrack)
                    if (trackNumber <= 0 && cdTrackCol != -1) {
                        trackNumber = parseTrackNumber(cursor.getString(cdTrackCol))
                    }
                    if (discNumber <= 0 && discNumCol != -1) {
                        discNumber = parseTrackNumber(cursor.getString(discNumCol))
                    }

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val albumArtUri = if (albumId > 0) {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()
                    } else contentUri.toString()

                    val trackId = "device_media_$id"
                    val artKey = "art_$trackId"
                    var hasArt = false
                    var extractedPrimary: Color? = null
                    var extractedSecondary: Color? = null
                    var extractedAccent: Color? = null

                    // Extract album art thumbnail and fallback track/disc metadata if absent from MediaStore
                    try {
                        val retriever = MediaMetadataRetriever()
                        try {
                            retriever.setDataSource(context, contentUri)
                            if (trackNumber <= 0) {
                                trackNumber = parseTrackNumber(
                                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                                )
                            }
                            if (discNumber <= 0) {
                                discNumber = parseTrackNumber(
                                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                                )
                            }
                            val picture = retriever.embeddedPicture
                            if (picture != null) {
                                val bitmap = android.graphics.BitmapFactory.decodeByteArray(picture, 0, picture.size)
                                if (bitmap != null) {
                                    AlbumArtCache.put(artKey, bitmap)
                                    hasArt = true
                                    val (p, s, a) = AlbumArtCache.extractExpressivePalette(bitmap)
                                    extractedPrimary = p
                                    extractedSecondary = s
                                    extractedAccent = a
                                }
                            }
                        } finally {
                            try { retriever.release() } catch (_: Exception) {}
                        }
                    } catch (_: Exception) {}

                    if (trackNumber <= 0) {
                        trackNumber = parseTrackNumberFromFilename(displayName ?: title)
                    }

                    val paletteIndex = kotlin.math.abs(title.hashCode()) % colorPalettes.size
                    val (defaultPrimary, defaultSecondary, defaultAccent) = colorPalettes[paletteIndex]
                    val coverTypes = CoverArtType.entries.toTypedArray()
                    val coverType = coverTypes[kotlin.math.abs(title.hashCode()) % coverTypes.size]

                    val baseTrack = Track(
                        id = trackId,
                        title = title,
                        artist = artist,
                        album = album,
                        durationMs = if (duration > 0) duration else 180000L,
                        primaryColor = extractedPrimary ?: defaultPrimary,
                        secondaryColor = extractedSecondary ?: defaultSecondary,
                        accentColor = extractedAccent ?: defaultAccent,
                        genre = "Device Audio",
                        coverType = coverType,
                        lyrics = emptyList(),
                        bpm = 120,
                        rootPitchHz = 220f,
                        isFavorite = false,
                        uriString = contentUri.toString(),
                        isLocalFile = true,
                        albumArtUri = albumArtUri,
                        albumArtKey = if (hasArt) artKey else null,
                        hasEmbeddedArt = hasArt,
                        trackNumber = trackNumber,
                        discNumber = discNumber
                    )
                    val tier1Lyrics = LyricsRepository.loadCachedOrSideCarLyrics(context, baseTrack)
                    tracks.add(baseTrack.copy(lyrics = tier1Lyrics))
                }
            }
        } catch (_: Exception) {}

        return tracks
    }

    /**
     * Fast metadata backfill for previously cached local tracks that have `trackNumber <= 0`.
     * Queries MediaStore columns in a single cursor pass first, then falls back to MediaMetadataRetriever
     * and filename prefixes for any remaining unresolved tracks.
     */
    fun backfillMissingTrackNumbers(context: Context, tracks: List<Track>): Map<String, Pair<Int, Int>> {
        val missingById = tracks.filter { it.isLocalFile && it.trackNumber <= 0 }.associateBy { it.id }
        if (missingById.isEmpty()) return emptyMap()

        val updates = mutableMapOf<String, Pair<Int, Int>>()

        // Step 1: Fast single-pass MediaStore query (no bitmap decoding)
        val projectionList = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DISPLAY_NAME
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projectionList.add(MediaStore.Audio.Media.CD_TRACK_NUMBER)
            projectionList.add(MediaStore.Audio.Media.DISC_NUMBER)
        }
        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projectionList.toTypedArray(),
                "${MediaStore.Audio.Media.IS_MUSIC} != 0",
                null,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val displayNameCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val cdTrackCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.CD_TRACK_NUMBER)
                } else -1
                val discNumCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.DISC_NUMBER)
                } else -1

                while (idCol != -1 && cursor.moveToNext()) {
                    val mediaId = cursor.getLong(idCol)
                    val trackId = "device_media_$mediaId"
                    val target = missingById[trackId] ?: continue

                    val rawTrack = if (trackCol != -1) cursor.getInt(trackCol) else 0
                    var (discNum, trackNum) = parseMediaStoreTrack(rawTrack)
                    if (trackNum <= 0 && cdTrackCol != -1) {
                        trackNum = parseTrackNumber(cursor.getString(cdTrackCol))
                    }
                    if (discNum <= 0 && discNumCol != -1) {
                        discNum = parseTrackNumber(cursor.getString(discNumCol))
                    }
                    if (trackNum <= 0 && displayNameCol != -1) {
                        trackNum = parseTrackNumberFromFilename(cursor.getString(displayNameCol))
                    }
                    if (trackNum > 0 || (discNum > 0 && target.discNumber <= 0)) {
                        updates[trackId] = discNum to trackNum
                    }
                }
            }
        } catch (_: Exception) {}

        // Step 2: MediaMetadataRetriever & filename fallback for SAF-imported tracks or rows missing MediaStore TRACK
        for ((trackId, track) in missingById) {
            val currentUpdate = updates[trackId]
            if (currentUpdate != null && currentUpdate.second > 0) continue

            var discNum = currentUpdate?.first ?: track.discNumber
            var trackNum = 0
            val uriStr = track.uriString
            if (!uriStr.isNullOrBlank()) {
                try {
                    val uri = Uri.parse(uriStr)
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, uri)
                        trackNum = parseTrackNumber(
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                        )
                        if (discNum <= 0) {
                            discNum = parseTrackNumber(
                                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                            )
                        }
                    } finally {
                        try { retriever.release() } catch (_: Exception) {}
                    }
                } catch (_: Exception) {}
            }
            if (trackNum <= 0) {
                val uriName = uriStr?.let { runCatching { Uri.parse(it).lastPathSegment }.getOrNull() }
                trackNum = parseTrackNumberFromFilename(uriName)
                    .takeIf { it > 0 } ?: parseTrackNumberFromFilename(track.title)
            }
            if (trackNum > 0 || (discNum > 0 && track.discNumber <= 0)) {
                updates[trackId] = discNum to trackNum
            }
        }

        return updates
    }
}
