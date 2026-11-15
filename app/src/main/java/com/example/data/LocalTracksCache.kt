package com.example.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.model.CoverArtType
import com.example.model.Track
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object LocalTracksCache {
    private const val CACHE_FILE_NAME = "cached_local_tracks.json"

    fun saveTracks(context: Context, tracks: List<Track>) {
        try {
            val localTracks = tracks.filter { it.isLocalFile }
            val jsonArray = JSONArray()
            for (track in localTracks) {
                val obj = JSONObject().apply {
                    put("id", track.id)
                    put("title", track.title)
                    put("artist", track.artist)
                    put("album", track.album)
                    put("durationMs", track.durationMs)
                    put("genre", track.genre)
                    put("uriString", track.uriString ?: "")
                    put("albumArtUri", track.albumArtUri ?: "")
                    put("albumArtKey", track.albumArtKey ?: "")
                    put("hasEmbeddedArt", track.hasEmbeddedArt)
                    put("coverType", track.coverType.name)
                    put("isFavorite", track.isFavorite)
                    put("trackNumber", track.trackNumber)
                    put("discNumber", track.discNumber)
                    put("primaryColor", track.primaryColor.value.toLong())
                    put("secondaryColor", track.secondaryColor.value.toLong())
                    put("accentColor", track.accentColor.value.toLong())
                }
                jsonArray.put(obj)
            }
            val file = File(context.filesDir, CACHE_FILE_NAME)
            file.writeText(jsonArray.toString())
        } catch (_: Exception) {}
    }

    fun loadTracks(context: Context): List<Track> {
        try {
            val file = File(context.filesDir, CACHE_FILE_NAME)
            if (!file.exists()) return emptyList()
            val text = file.readText()
            if (text.isBlank()) return emptyList()
            val jsonArray = JSONArray(text)
            val list = mutableListOf<Track>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val coverTypeName = obj.optString("coverType", CoverArtType.COSMIC.name)
                val coverType = try {
                    CoverArtType.valueOf(coverTypeName)
                } catch (_: Exception) {
                    CoverArtType.COSMIC
                }
                val primaryVal = obj.optLong("primaryColor", 0xFF7C4DFFL)
                val secondaryVal = obj.optLong("secondaryColor", 0xFF00E5FFL)
                val accentVal = obj.optLong("accentColor", 0xFFD1C4E9L)

                list.add(
                    Track(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        artist = obj.optString("artist", "Unknown Artist"),
                        album = obj.optString("album", "Device Storage"),
                        durationMs = obj.optLong("durationMs", 180000L),
                        genre = obj.optString("genre", "Audio File"),
                        uriString = obj.optString("uriString").takeIf { it.isNotBlank() },
                        isLocalFile = true,
                        albumArtUri = obj.optString("albumArtUri").takeIf { it.isNotBlank() },
                        albumArtKey = obj.optString("albumArtKey").takeIf { it.isNotBlank() },
                        hasEmbeddedArt = obj.optBoolean("hasEmbeddedArt", false),
                        coverType = coverType,
                        isFavorite = obj.optBoolean("isFavorite", false),
                        trackNumber = obj.optInt("trackNumber", 0),
                        discNumber = obj.optInt("discNumber", 0),
                        primaryColor = Color(primaryVal.toULong()),
                        secondaryColor = Color(secondaryVal.toULong()),
                        accentColor = Color(accentVal.toULong())
                    )
                )
            }
            return list
        } catch (_: Exception) {
            return emptyList()
        }
    }
}
