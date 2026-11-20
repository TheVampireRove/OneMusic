package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.Normalizer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

sealed class AnimatedCoverMedia {
    data class VideoLoop(val uri: Uri, val localPath: String? = null) : AnimatedCoverMedia()
    data class AnimatedImage(val uri: Uri, val localPath: String? = null) : AnimatedCoverMedia()
    data class HighResArtwork(val uri: Uri, val localPath: String? = null) : AnimatedCoverMedia()
    data object None : AnimatedCoverMedia()
}

data class AnimatedCoverCandidate(
    val url: String,
    val kind: Kind
) {
    enum class Kind {
        VIDEO_MP4,
        VIDEO_WEBM,
        ANIMATED_WEBP,
        ANIMATED_GIF,
        HIGH_RES_IMAGE
    }
}

interface AnimatedCoverEndpoint {
    suspend fun findAnimatedCover(artist: String, album: String, title: String): AnimatedCoverCandidate?
}

object AnimatedArtRepository {

    private const val CACHE_DIR_NAME = "animated_covers"
    private const val PREFS_NAME = "animated_cover_prefs"
    private const val KEY_CACHE_SCHEMA_VERSION = "cache_schema_version"
    private const val CURRENT_CACHE_SCHEMA_VERSION = 2
    private const val KEY_PREFIX_CUSTOM = "custom_"
    private const val KEY_PREFIX_RESTORED_DEFAULT = "restored_default_"
    private const val MAX_DOWNLOAD_BYTES = 18L * 1024L * 1024L

    private val _cacheVersion = MutableStateFlow(0)
    val cacheVersion: StateFlow<Int> = _cacheVersion.asStateFlow()

    private val resolvedCache = ConcurrentHashMap<String, AnimatedCoverMedia>()
    private val failedFetchKeys = ConcurrentHashMap.newKeySet<String>()

    var customEndpoint: AnimatedCoverEndpoint? = null

    @androidx.annotation.VisibleForTesting
    internal var networkFetcherOverride: (suspend (Track) -> AnimatedCoverCandidate?)? = null

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val GENERIC_ARTISTS = setOf(
        "", "unknown", "<unknown>", "unknown artist", "various", "various artists",
        "va", "v.a.", "artist", "none", "n/a", "null"
    )

    private val GENERIC_ALBUMS = setOf(
        "", "unknown", "<unknown>", "unknown album", "device music", "device audio",
        "music", "downloads", "download", "audio", "my music", "songs", "single",
        "untitled", "none", "n/a", "internal storage", "null"
    )

    private val GENERIC_TITLES = setOf(
        "", "unknown", "<unknown>", "unknown track", "track", "untitled", "audio", "none", "n/a"
    )

    private val EDITION_PAREN_REGEX = Regex(
        """[\(\[]\s*(?:feat\.?|ft\.?|featuring|with|remaster(?:ed)?|deluxe|expanded|special|anniversary|collector'?s?|super\s+deluxe|bonus|explicit|clean|edition|version|disc\s*\d+|cd\s*\d+|ost|soundtrack|live|mono|stereo|remix|mix|\d{4}\s+remaster)[^\)\]]*[\)\]]""",
        RegexOption.IGNORE_CASE
    )
    private val TRAILING_FEAT_REGEX = Regex(
        """\s+(?:feat\.?|ft\.?|featuring)\s+.*$""",
        RegexOption.IGNORE_CASE
    )
    private val COMBINING_MARKS_REGEX = Regex("""\p{InCombiningDiacriticalMarks}+""")
    private val NON_ALNUM_REGEX = Regex("""[^\p{L}\p{N}\s]""")
    private val MULTI_SPACE_REGEX = Regex("""\s+""")

    private val ALBUM_BOILERPLATE_TOKENS = setOf(
        "the", "a", "an", "of", "and", "in", "on", "to", "for", "with", "at", "by",
        "best", "greatest", "hits", "hit", "collection", "collections", "essential",
        "essentials", "ultimate", "anthology", "complete", "very", "vol", "volume",
        "disc", "cd", "pt", "part", "edition", "deluxe", "expanded", "remastered",
        "remaster", "anniversary", "bonus", "track", "tracks", "explicit", "clean",
        "version", "album", "ost", "soundtrack", "original", "motion", "picture"
    )

    fun getCacheDir(context: Context): File {
        val dir = File(context.applicationContext.filesDir, CACHE_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun albumKey(artist: String, album: String): String {
        val raw = "${artist.trim().lowercase()}|${album.trim().lowercase()}"
        return sha256Short(raw)
    }

    internal fun coverCacheKey(track: Track): String {
        return if (!isGenericArtist(track.artist) && !isGenericAlbum(track.album)) {
            albumKey(track.artist, track.album)
        } else {
            val raw = "${track.artist.trim().lowercase()}|__track__|${track.album.trim().lowercase()}|${track.title.trim().lowercase()}|${track.id}"
            sha256Short(raw)
        }
    }

    fun trackSidecarKey(track: Track): String {
        val raw = "${track.id}|${track.title.trim().lowercase()}"
        return sha256Short(raw)
    }

    private fun sha256Short(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.take(12).joinToString("") { "%02x".format(it) }
    }

    fun bumpVersion() {
        _cacheVersion.value = _cacheVersion.value + 1
    }

    private fun ensureSchemaMigrated(context: Context) {
        val appCtx = context.applicationContext
        val sp = prefs(appCtx)
        val storedVersion = sp.getInt(KEY_CACHE_SCHEMA_VERSION, 0)
        if (storedVersion >= CURRENT_CACHE_SCHEMA_VERSION) return

        synchronized(this) {
            if (sp.getInt(KEY_CACHE_SCHEMA_VERSION, 0) >= CURRENT_CACHE_SCHEMA_VERSION) return
            val dir = getCacheDir(appCtx)
            dir.listFiles()?.forEach { file ->
                val lower = file.name.lowercase()
                if (file.isFile && (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png"))) {
                    val base = file.nameWithoutExtension.removePrefix("album_")
                    val isUserCustom = sp.getBoolean("$KEY_PREFIX_CUSTOM$base", false)
                    if (!isUserCustom) {
                        file.delete()
                    }
                }
            }
            resolvedCache.clear()
            failedFetchKeys.clear()
            sp.edit().putInt(KEY_CACHE_SCHEMA_VERSION, CURRENT_CACHE_SCHEMA_VERSION).apply()
        }
    }

    fun isRestoredToDefault(context: Context, track: Track): Boolean {
        val sp = prefs(context)
        val cKey = coverCacheKey(track)
        val aKey = albumKey(track.artist, track.album)
        return sp.getBoolean("$KEY_PREFIX_RESTORED_DEFAULT$cKey", false) ||
            sp.getBoolean("$KEY_PREFIX_RESTORED_DEFAULT$aKey", false)
    }

    /**
     * Resolves the best animated/high-res visual format for [track] using the hierarchy:
     * 1. Side-car local media next to the track file (`cover.mp4`, `animated_cover.mp4`, `cover.webp`, `cover.gif`, `[TrackName].mp4`)
     * 2. Internal cache (`filesDir/animated_covers/album_{key}.*`)
     * 3. Automatic metadata / animated cover endpoint fetch (respecting Wi-Fi setting & strict metadata verification)
     */
    suspend fun resolveAnimatedCover(
        context: Context,
        track: Track,
        enableAnimatedCovers: Boolean,
        autoFetchWifiOnly: Boolean
    ): AnimatedCoverMedia = withContext(Dispatchers.IO) {
        if (!enableAnimatedCovers) return@withContext AnimatedCoverMedia.None

        val appCtx = context.applicationContext
        ensureSchemaMigrated(appCtx)

        if (isRestoredToDefault(appCtx, track)) {
            return@withContext AnimatedCoverMedia.None
        }

        val cKey = coverCacheKey(track)

        // 1. Check side-car local files next to audio file
        findSidecarMedia(appCtx, track)?.let { sidecar ->
            resolvedCache[cKey] = sidecar
            return@withContext sidecar
        }

        // 2. Check internal cache
        findCachedAlbumMedia(appCtx, cKey)?.let { cached ->
            resolvedCache[cKey] = cached
            return@withContext cached
        }

        resolvedCache[cKey]?.let { inMem ->
            if (inMem != AnimatedCoverMedia.None) return@withContext inMem
        }

        // 3. Auto-fetch high-res / animated cover metadata over network if allowed
        if (failedFetchKeys.contains(cKey)) return@withContext AnimatedCoverMedia.None
        if (autoFetchWifiOnly && !isUnmeteredWifiConnected(appCtx)) {
            return@withContext AnimatedCoverMedia.None
        }

        val candidate = fetchBestRemoteCandidate(track)
        if (candidate != null) {
            val downloaded = downloadCandidateToCache(appCtx, cKey, candidate)
            if (downloaded != null) {
                resolvedCache[cKey] = downloaded
                return@withContext downloaded
            }
        }

        failedFetchKeys.add(cKey)
        AnimatedCoverMedia.None
    }

    /**
     * Fast non-network lookup for immediate UI render before launching background resolution.
     */
    fun getCachedOrSidecarImmediate(context: Context, track: Track): AnimatedCoverMedia {
        val appCtx = context.applicationContext
        ensureSchemaMigrated(appCtx)
        if (isRestoredToDefault(appCtx, track)) {
            return AnimatedCoverMedia.None
        }
        val cKey = coverCacheKey(track)
        findCachedAlbumMedia(appCtx, cKey)?.let { return it }
        return resolvedCache[cKey] ?: AnimatedCoverMedia.None
    }

    internal fun findCachedAlbumMedia(context: Context, albumKey: String): AnimatedCoverMedia? {
        val dir = getCacheDir(context)
        val priorityOrder = listOf(
            "album_${albumKey}.mp4" to { f: File -> AnimatedCoverMedia.VideoLoop(Uri.fromFile(f), f.absolutePath) },
            "album_${albumKey}.webm" to { f: File -> AnimatedCoverMedia.VideoLoop(Uri.fromFile(f), f.absolutePath) },
            "album_${albumKey}.webp" to { f: File -> AnimatedCoverMedia.AnimatedImage(Uri.fromFile(f), f.absolutePath) },
            "album_${albumKey}.gif" to { f: File -> AnimatedCoverMedia.AnimatedImage(Uri.fromFile(f), f.absolutePath) },
            "album_${albumKey}.jpg" to { f: File -> AnimatedCoverMedia.HighResArtwork(Uri.fromFile(f), f.absolutePath) },
            "album_${albumKey}.png" to { f: File -> AnimatedCoverMedia.HighResArtwork(Uri.fromFile(f), f.absolutePath) }
        )
        for ((name, factory) in priorityOrder) {
            val file = File(dir, name)
            if (file.exists() && file.length() > 0L) {
                return factory(file)
            }
        }
        return null
    }

    internal fun findSidecarMedia(context: Context, track: Track): AnimatedCoverMedia? {
        val uriStr = track.uriString ?: return null
        val sanitizedTitle = track.title.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()

        // Direct file:// URI directory scan
        if (uriStr.startsWith("file://")) {
            val audioFile = Uri.parse(uriStr).path?.let { File(it) }
            val parent = audioFile?.parentFile
            if (parent != null && parent.exists() && parent.isDirectory) {
                val baseAudioName = audioFile.nameWithoutExtension
                val candidates = listOf(
                    "cover.mp4",
                    "animated_cover.mp4",
                    "${baseAudioName}.mp4",
                    "${sanitizedTitle}.mp4",
                    "cover.webm",
                    "animated_cover.webm",
                    "cover.webp",
                    "animated_cover.webp",
                    "${baseAudioName}.webp",
                    "cover.gif",
                    "animated_cover.gif",
                    "${baseAudioName}.gif"
                )
                for (name in candidates) {
                    val f = File(parent, name)
                    if (f.exists() && f.length() > 0L) {
                        val lower = name.lowercase()
                        return when {
                            lower.endsWith(".mp4") || lower.endsWith(".webm") ->
                                AnimatedCoverMedia.VideoLoop(Uri.fromFile(f), f.absolutePath)
                            lower.endsWith(".webp") || lower.endsWith(".gif") ->
                                AnimatedCoverMedia.AnimatedImage(Uri.fromFile(f), f.absolutePath)
                            else -> null
                        }
                    }
                }
            }
        }

        // Scoped MediaStore scan for content:// local audio files matching sidecar names in the SAME directory
        if (uriStr.startsWith("content://") && track.isLocalFile) {
            try {
                val audioRelPath = resolveAudioRelativeDir(context, Uri.parse(uriStr))
                val hasTrackSpecificName = sanitizedTitle.isNotBlank() && !isGenericTitle(sanitizedTitle)
                val folderGenericNames = setOf(
                    "cover.mp4",
                    "animated_cover.mp4",
                    "cover.webm",
                    "cover.webp",
                    "cover.gif"
                )
                val trackSpecificNames = if (hasTrackSpecificName) {
                    val lowTitle = sanitizedTitle.lowercase()
                    setOf("$lowTitle.mp4", "$lowTitle.webm", "$lowTitle.gif", "$lowTitle.webp")
                } else {
                    emptySet()
                }
                if (audioRelPath == null && trackSpecificNames.isEmpty()) {
                    return null
                }

                val projCols = mutableListOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.MIME_TYPE
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    projCols.add(MediaStore.Files.FileColumns.RELATIVE_PATH)
                }
                val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'video/%' OR ${MediaStore.Files.FileColumns.MIME_TYPE} = 'image/gif' OR ${MediaStore.Files.FileColumns.MIME_TYPE} = 'image/webp'"
                context.contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projCols.toTypedArray(),
                    selection,
                    null,
                    null
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                    val relCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH)
                    } else -1
                    while (cursor.moveToNext()) {
                        val displayName = cursor.getString(nameCol)?.lowercase() ?: continue
                        val candidateRel = if (relCol >= 0) cursor.getString(relCol)?.trimEnd('/') else null
                        val sameFolder = audioRelPath != null && candidateRel != null &&
                            candidateRel.equals(audioRelPath, ignoreCase = true)
                        val isMatch = (sameFolder && displayName in folderGenericNames) ||
                            (displayName in trackSpecificNames && (audioRelPath == null || sameFolder))
                        if (isMatch) {
                            val id = cursor.getLong(idCol)
                            val mime = cursor.getString(mimeCol)?.lowercase().orEmpty()
                            val contentUri = Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), id.toString())
                            return if (mime.startsWith("video/") || displayName.endsWith(".mp4") || displayName.endsWith(".webm")) {
                                AnimatedCoverMedia.VideoLoop(contentUri)
                            } else {
                                AnimatedCoverMedia.AnimatedImage(contentUri)
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Scoped storage restrictions may disallow querying unrelated video files; gracefully ignore.
            }
        }

        return null
    }

    private fun resolveAudioRelativeDir(context: Context, audioUri: Uri): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return try {
            context.contentResolver.query(
                audioUri,
                arrayOf(MediaStore.Audio.Media.RELATIVE_PATH),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val col = cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                    if (col >= 0) cursor.getString(col)?.trimEnd('/') else null
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun importAnimatedCover(
        context: Context,
        track: Track,
        sourceUri: Uri
    ): AnimatedCoverMedia? = withContext(Dispatchers.IO) {
        val appCtx = context.applicationContext
        ensureSchemaMigrated(appCtx)
        val cKey = coverCacheKey(track)
        val aKey = albumKey(track.artist, track.album)
        val mime = appCtx.contentResolver.getType(sourceUri)?.lowercase().orEmpty()
        val pathHint = sourceUri.lastPathSegment?.lowercase().orEmpty()

        val ext = when {
            mime.contains("mp4") || pathHint.endsWith(".mp4") -> "mp4"
            mime.contains("webm") || pathHint.endsWith(".webm") -> "webm"
            mime.contains("webp") || pathHint.endsWith(".webp") -> "webp"
            mime.contains("gif") || pathHint.endsWith(".gif") -> "gif"
            mime.contains("png") || pathHint.endsWith(".png") -> "png"
            mime.contains("jpeg") || mime.contains("jpg") || pathHint.endsWith(".jpg") || pathHint.endsWith(".jpeg") -> "jpg"
            else -> "mp4"
        }

        deleteAlbumCacheFiles(appCtx, cKey)
        if (aKey != cKey) deleteAlbumCacheFiles(appCtx, aKey)

        val destFile = File(getCacheDir(appCtx), "album_${cKey}.$ext")
        try {
            appCtx.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null

            if (!destFile.exists() || destFile.length() == 0L) {
                destFile.delete()
                return@withContext null
            }

            val media = when (ext) {
                "mp4", "webm" -> AnimatedCoverMedia.VideoLoop(Uri.fromFile(destFile), destFile.absolutePath)
                "webp", "gif" -> AnimatedCoverMedia.AnimatedImage(Uri.fromFile(destFile), destFile.absolutePath)
                else -> AnimatedCoverMedia.HighResArtwork(Uri.fromFile(destFile), destFile.absolutePath)
            }
            prefs(appCtx).edit()
                .putBoolean("$KEY_PREFIX_CUSTOM$cKey", true)
                .remove("$KEY_PREFIX_RESTORED_DEFAULT$cKey")
                .remove("$KEY_PREFIX_RESTORED_DEFAULT$aKey")
                .apply()
            resolvedCache[cKey] = media
            failedFetchKeys.remove(cKey)
            bumpVersion()
            media
        } catch (_: Exception) {
            try { destFile.delete() } catch (_: Exception) {}
            null
        }
    }

    suspend fun removeCustomCover(context: Context, track: Track) = withContext(Dispatchers.IO) {
        val appCtx = context.applicationContext
        val cKey = coverCacheKey(track)
        val aKey = albumKey(track.artist, track.album)
        deleteAlbumCacheFiles(appCtx, cKey)
        if (aKey != cKey) {
            deleteAlbumCacheFiles(appCtx, aKey)
        }
        prefs(appCtx).edit()
            .remove("$KEY_PREFIX_CUSTOM$cKey")
            .remove("$KEY_PREFIX_CUSTOM$aKey")
            .putBoolean("$KEY_PREFIX_RESTORED_DEFAULT$cKey", true)
            .putBoolean("$KEY_PREFIX_RESTORED_DEFAULT$aKey", true)
            .apply()
        resolvedCache.remove(cKey)
        resolvedCache.remove(aKey)
        failedFetchKeys.remove(cKey)
        failedFetchKeys.remove(aKey)
        bumpVersion()
    }

    suspend fun clearCache(context: Context): Int = withContext(Dispatchers.IO) {
        val appCtx = context.applicationContext
        val dir = getCacheDir(appCtx)
        val files = dir.listFiles().orEmpty()
        var deleted = 0
        files.forEach { f ->
            if (f.delete()) deleted++
        }
        prefs(appCtx).edit()
            .clear()
            .putInt(KEY_CACHE_SCHEMA_VERSION, CURRENT_CACHE_SCHEMA_VERSION)
            .apply()
        resolvedCache.clear()
        failedFetchKeys.clear()
        bumpVersion()
        deleted
    }

    fun getCacheStats(context: Context): Pair<Int, Long> {
        val dir = getCacheDir(context.applicationContext)
        val files = dir.listFiles()?.filter { it.isFile }.orEmpty()
        return files.size to files.sumOf { it.length() }
    }

    private fun deleteAlbumCacheFiles(context: Context, albumKey: String) {
        val dir = getCacheDir(context)
        val prefix = "album_${albumKey}."
        dir.listFiles()?.forEach { file ->
            if (file.name.startsWith(prefix)) {
                file.delete()
            }
        }
    }

    internal fun isUnmeteredWifiConnected(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val active = cm.activeNetwork ?: return false
                val caps = cm.getNetworkCapabilities(active) ?: return false
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            } else {
                @Suppress("DEPRECATION")
                val info = cm.activeNetworkInfo
                @Suppress("DEPRECATION")
                info != null && info.isConnected && info.type == ConnectivityManager.TYPE_WIFI
            }
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun fetchBestRemoteCandidate(track: Track): AnimatedCoverCandidate? {
        networkFetcherOverride?.let { override ->
            return override(track)
        }
        customEndpoint?.let { endpoint ->
            try {
                endpoint.findAnimatedCover(track.artist, track.album, track.title)?.let { return it }
            } catch (_: Exception) {}
        }

        if (isGenericArtist(track.artist)) return null

        // 1. Primary lookup: strict verified album search when album metadata is meaningful
        val hasRealAlbum = !isGenericAlbum(track.album)
        if (hasRealAlbum) {
            val cleanAlbumQuery = "${cleanSearchTerm(track.artist)} ${cleanSearchTerm(track.album)}".trim()
            if (cleanAlbumQuery.isNotBlank()) {
                val albumJson = executeItunesSearch(cleanAlbumQuery, entity = "album", limit = 15)
                if (albumJson != null) {
                    pickBestVerifiedAlbumCandidate(track, albumJson)?.let { return it }
                }
            }
        }

        // 2. Fallback lookup: strict verified song search (requires artist + track match, and non-conflicting album)
        if (!isGenericTitle(track.title)) {
            val cleanSongQuery = "${cleanSearchTerm(track.artist)} ${cleanSearchTerm(track.title)}".trim()
            if (cleanSongQuery.isNotBlank()) {
                val songJson = executeItunesSearch(cleanSongQuery, entity = "song", limit = 15)
                if (songJson != null) {
                    pickBestVerifiedSongCandidate(track, songJson)?.let { return it }
                }
            }
        }

        return null
    }

    private fun executeItunesSearch(term: String, entity: String, limit: Int): String? {
        return try {
            val url = "https://itunes.apple.com/search".toHttpUrl().newBuilder()
                .addQueryParameter("term", term)
                .addQueryParameter("media", "music")
                .addQueryParameter("entity", entity)
                .addQueryParameter("limit", limit.toString())
                .build()
            val req = Request.Builder().url(url).build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                resp.body?.string()
            }
        } catch (_: Exception) {
            null
        }
    }

    @androidx.annotation.VisibleForTesting
    internal fun pickBestVerifiedAlbumCandidate(track: Track, responseJson: String): AnimatedCoverCandidate? {
        if (isGenericArtist(track.artist) || isGenericAlbum(track.album)) return null
        return try {
            val results = JSONObject(responseJson).optJSONArray("results") ?: return null
            var bestScore = 0.0
            var bestArtUrl: String? = null

            for (i in 0 until results.length()) {
                val obj = results.optJSONObject(i) ?: continue
                val artistName = obj.optString("artistName")
                val collectionName = obj.optString("collectionName")
                val art100 = obj.optString("artworkUrl100")
                if (art100.isBlank()) continue

                val artistScore = scoreArtistMatch(track.artist, artistName)
                if (artistScore <= 0.0) continue

                val albumScore = scoreTitleOrAlbumMatch(track.album, collectionName, isAlbum = true)
                if (albumScore <= 0.0) continue

                val combined = artistScore * 0.45 + albumScore * 0.55
                if (combined > bestScore) {
                    bestScore = combined
                    bestArtUrl = art100
                }
            }

            bestArtUrl?.let { url ->
                AnimatedCoverCandidate(
                    url = url.replace("100x100bb", "1000x1000bb"),
                    kind = AnimatedCoverCandidate.Kind.HIGH_RES_IMAGE
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    @androidx.annotation.VisibleForTesting
    internal fun pickBestVerifiedSongCandidate(track: Track, responseJson: String): AnimatedCoverCandidate? {
        if (isGenericArtist(track.artist) || isGenericTitle(track.title)) return null
        return try {
            val results = JSONObject(responseJson).optJSONArray("results") ?: return null
            val hasRealAlbum = !isGenericAlbum(track.album)
            var bestScore = 0.0
            var bestArtUrl: String? = null

            for (i in 0 until results.length()) {
                val obj = results.optJSONObject(i) ?: continue
                val artistName = obj.optString("artistName")
                val trackName = obj.optString("trackName")
                val collectionName = obj.optString("collectionName")
                val art100 = obj.optString("artworkUrl100")
                if (art100.isBlank()) continue

                val artistScore = scoreArtistMatch(track.artist, artistName)
                if (artistScore <= 0.0) continue

                val titleScore = scoreTitleOrAlbumMatch(track.title, trackName, isAlbum = false)
                if (titleScore <= 0.0) continue

                val albumScore = if (hasRealAlbum && collectionName.isNotBlank()) {
                    scoreTitleOrAlbumMatch(track.album, collectionName, isAlbum = true)
                } else {
                    0.5
                }

                // If the track already has a real album tag (and especially if it has embedded art),
                // do not replace it with artwork from an unrelated compilation/single album.
                if (hasRealAlbum && collectionName.isNotBlank() && albumScore <= 0.0) {
                    continue
                }

                val combined = artistScore * 0.40 + titleScore * 0.40 + albumScore * 0.20
                if (combined > bestScore) {
                    bestScore = combined
                    bestArtUrl = art100
                }
            }

            bestArtUrl?.let { url ->
                AnimatedCoverCandidate(
                    url = url.replace("100x100bb", "1000x1000bb"),
                    kind = AnimatedCoverCandidate.Kind.HIGH_RES_IMAGE
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    internal fun isGenericArtist(artist: String): Boolean {
        val norm = normalizeBasic(artist)
        return norm.isBlank() || artist.trim().lowercase() in GENERIC_ARTISTS || norm in GENERIC_ARTISTS
    }

    internal fun isGenericAlbum(album: String): Boolean {
        val norm = normalizeBasic(album)
        return norm.isBlank() || album.trim().lowercase() in GENERIC_ALBUMS || norm in GENERIC_ALBUMS
    }

    internal fun isGenericTitle(title: String): Boolean {
        val norm = normalizeBasic(title)
        return norm.isBlank() || title.trim().lowercase() in GENERIC_TITLES || norm in GENERIC_TITLES
    }

    private fun cleanSearchTerm(value: String): String {
        val stripped = value
            .replace(EDITION_PAREN_REGEX, " ")
            .replace(TRAILING_FEAT_REGEX, " ")
            .replace("...", " ")
            .replace("…", " ")
        return MULTI_SPACE_REGEX.replace(stripped, " ").trim()
    }

    private fun normalizeBasic(value: String): String {
        val decomposed = Normalizer.normalize(value, Normalizer.Form.NFD)
        val ascii = COMBINING_MARKS_REGEX.replace(decomposed, "")
            .lowercase()
            .replace("&", " and ")
            .replace("'", "")
            .replace("’", "")
        val alnum = NON_ALNUM_REGEX.replace(ascii, " ")
        return MULTI_SPACE_REGEX.replace(alnum, " ").trim()
    }

    private fun normalizeCore(value: String): String {
        val noEdition = EDITION_PAREN_REGEX.replace(value, " ")
            .replace(TRAILING_FEAT_REGEX, " ")
        val coreNorm = normalizeBasic(noEdition)
        return if (coreNorm.length >= 2) coreNorm else normalizeBasic(value)
    }

    private fun splitArtists(artist: String): List<String> {
        val withoutFeat = TRAILING_FEAT_REGEX.replace(artist, "")
        val parts = withoutFeat.split(Regex("""\s*(?:,|;|&|feat\.?|ft\.?|featuring|with|/|\bx\b)\s*""", RegexOption.IGNORE_CASE))
            .map { normalizeBasic(it).removePrefix("the ").trim() }
            .filter { it.length >= 2 && it !in GENERIC_ARTISTS }
        return if (parts.isNotEmpty()) parts else {
            val whole = normalizeBasic(artist).removePrefix("the ").trim()
            if (whole.length >= 2) listOf(whole) else emptyList()
        }
    }

    internal fun scoreArtistMatch(expectedArtist: String, candidateArtist: String): Double {
        if (isGenericArtist(expectedArtist) || isGenericArtist(candidateArtist)) return 0.0

        val expFull = normalizeBasic(expectedArtist).removePrefix("the ").trim()
        val candFull = normalizeBasic(candidateArtist).removePrefix("the ").trim()
        if (expFull.length < 2 || candFull.length < 2) return 0.0
        if (expFull == candFull) return 1.0

        val expParts = splitArtists(expectedArtist)
        val candParts = splitArtists(candidateArtist)
        if (expParts.isEmpty() || candParts.isEmpty()) return 0.0

        // Primary artist exact match (e.g. "Luther Vandross" vs "Luther Vandross & Mariah Carey")
        if (expParts[0] == candParts[0]) return 0.95

        // Any listed artist matches cleanly
        if (expParts.any { ep -> candParts.any { cp -> ep == cp } }) return 0.85

        // Multi-word whole-phrase containment (e.g. "luther vandross" inside "luther vandross gregory hines")
        val expTokens = expFull.split(" ").filter { it.length >= 2 && it != "and" }
        val candTokens = candFull.split(" ").filter { it.length >= 2 && it != "and" }
        if (expTokens.size >= 2 && candTokens.size >= 2) {
            val expSet = expTokens.toSet()
            val candSet = candTokens.toSet()
            if (expSet == candSet) return 0.92
            val minSize = minOf(expSet.size, candSet.size)
            val intersection = expSet.intersect(candSet).size
            if (intersection == minSize && minSize >= 2) return 0.80
        }

        return 0.0
    }

    internal fun scoreTitleOrAlbumMatch(expected: String, candidate: String, isAlbum: Boolean): Double {
        val expRaw = normalizeBasic(expected)
        val candRaw = normalizeBasic(candidate)
        if (expRaw.isBlank() || candRaw.isBlank()) return 0.0
        if (expRaw == candRaw) return 1.0

        val expCore = normalizeCore(expected)
        val candCore = normalizeCore(candidate)
        if (expCore == candCore && expCore.isNotBlank()) return 0.96

        // For very short titles/albums (e.g. "25", "19", "Me"), require core exact match above
        if (expCore.length <= 3 || candCore.length <= 3) return 0.0

        // Whole-word containment check (e.g. "The Best of Luther Vandross" inside "The Best of Luther Vandross The Best of Love")
        val (shorter, longer) = if (expCore.length <= candCore.length) expCore to candCore else candCore to expCore
        val lengthRatio = shorter.length.toDouble() / longer.length.toDouble()
        val shorterTokens = shorter.split(" ").filter { it.isNotBlank() }
        if ((shorterTokens.size >= 2 || shorter.length >= 6) && lengthRatio >= 0.42) {
            val boundaryRegex = Regex("""(?:^|\s)${Regex.escape(shorter)}(?:\s|$)""")
            if (boundaryRegex.containsMatchIn(longer)) {
                return 0.88
            }
        }

        // Token overlap check (excluding generic boilerplate for album comparisons)
        val stopWords = if (isAlbum) ALBUM_BOILERPLATE_TOKENS else setOf("the", "a", "an", "of", "and")
        val expAll = expCore.split(" ").filter { it.length >= 2 }.toSet()
        val candAll = candCore.split(" ").filter { it.length >= 2 }.toSet()
        if (expAll.isEmpty() || candAll.isEmpty()) return 0.0

        val expMeaningful = expAll.filterNot { it in stopWords }.toSet()
        val candMeaningful = candAll.filterNot { it in stopWords }.toSet()
        if (expMeaningful.isEmpty() || candMeaningful.isEmpty()) {
            val diceAll = (2.0 * expAll.intersect(candAll).size) / (expAll.size + candAll.size)
            return if (diceAll >= 0.85) diceAll else 0.0
        }

        val meaningfulIntersection = expMeaningful.intersect(candMeaningful).size
        if (meaningfulIntersection == 0) return 0.0

        val meaningfulDice = (2.0 * meaningfulIntersection) / (expMeaningful.size + candMeaningful.size)
        val allDice = (2.0 * expAll.intersect(candAll).size) / (expAll.size + candAll.size)
        return if (meaningfulDice >= 0.72 && allDice >= 0.50) {
            (meaningfulDice * 0.7 + allDice * 0.3).coerceAtMost(0.90)
        } else {
            0.0
        }
    }

    private fun downloadCandidateToCache(
        context: Context,
        albumKey: String,
        candidate: AnimatedCoverCandidate
    ): AnimatedCoverMedia? {
        val ext = when (candidate.kind) {
            AnimatedCoverCandidate.Kind.VIDEO_MP4 -> "mp4"
            AnimatedCoverCandidate.Kind.VIDEO_WEBM -> "webm"
            AnimatedCoverCandidate.Kind.ANIMATED_WEBP -> "webp"
            AnimatedCoverCandidate.Kind.ANIMATED_GIF -> "gif"
            AnimatedCoverCandidate.Kind.HIGH_RES_IMAGE -> "jpg"
        }
        val dest = File(getCacheDir(context), "album_${albumKey}.$ext")
        return try {
            val req = Request.Builder().url(candidate.url).build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body ?: return null
                if (body.contentLength() > MAX_DOWNLOAD_BYTES) return null
                FileOutputStream(dest).use { out ->
                    body.byteStream().copyTo(out)
                }
            }
            if (!dest.exists() || dest.length() == 0L) {
                dest.delete()
                return null
            }
            when (candidate.kind) {
                AnimatedCoverCandidate.Kind.VIDEO_MP4,
                AnimatedCoverCandidate.Kind.VIDEO_WEBM ->
                    AnimatedCoverMedia.VideoLoop(Uri.fromFile(dest), dest.absolutePath)
                AnimatedCoverCandidate.Kind.ANIMATED_WEBP,
                AnimatedCoverCandidate.Kind.ANIMATED_GIF ->
                    AnimatedCoverMedia.AnimatedImage(Uri.fromFile(dest), dest.absolutePath)
                AnimatedCoverCandidate.Kind.HIGH_RES_IMAGE ->
                    AnimatedCoverMedia.HighResArtwork(Uri.fromFile(dest), dest.absolutePath)
            }
        } catch (_: Exception) {
            try { dest.delete() } catch (_: Exception) {}
            null
        }
    }
}
