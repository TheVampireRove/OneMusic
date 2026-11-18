package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.example.model.LyricLine
import com.example.model.LyricWord
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

enum class LyricsFetchStatus {
    NOT_FETCHED,
    FOUND,
    NOT_FOUND,
    INSTRUMENTAL
}

data class LyricsStatusSummary(
    val totalTracks: Int,
    val foundCount: Int,
    val notFoundCount: Int,
    val instrumentalCount: Int,
    val pendingCount: Int
)

sealed class LrclibLookupResult {
    data class Found(val rawText: String) : LrclibLookupResult()
    data object Instrumental : LrclibLookupResult()
    data object NotFound : LrclibLookupResult()
    data class RateLimited(val retryAfterMs: Long) : LrclibLookupResult()
    data object NetworkError : LrclibLookupResult()
}

/**
 * 3-Tier Lyrics Fallback Pipeline & Persistent Status Cache:
 * 1. Tier 1: Local internal cache (`filesDir/lyrics_cache/`) & side-car `.lrc` lookup (same folder / MediaStore).
 * 2. Tier 2: Free online synced/plain lyrics via LRCLIB API (`https://lrclib.net/api/get`) using OkHttp on Dispatchers.IO.
 * 3. Tier 3: Manual `.lrc` / `.txt` file import and pasted lyrics persistence to `lyrics_cache/`.
 */
object LyricsRepository {

    private const val CACHE_DIR_NAME = "lyrics_cache"
    private const val STATUS_FILE_NAME = "lyrics_status.json"
    private const val USER_AGENT = "OneMusic/1.0 (Android)"
    private const val LRCLIB_HOST = "lrclib.net"
    private const val DEFAULT_RETRY_AFTER_MS = 30_000L

    private val statusLock = Any()

    private val audioExtensions = listOf(".mp3", ".m4a", ".flac", ".ogg", ".wav", ".aac", ".opus", ".wma")

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Resolves lyrics for [track] using Tier 1 (cache + side-car .lrc) then Tier 2 (LRCLIB API).
     * Returns emptyList() if no lyrics are found (triggering Tier 3 manual import/paste UI).
     */
    suspend fun resolveLyrics(context: Context, track: Track, forceRefresh: Boolean = false): List<LyricLine> = withContext(Dispatchers.IO) {
        // Tier 1a: Check internal lyrics_cache/ folder keyed by track ID
        val cachedText = readCachedLyricsText(context, track.id)
        if (!cachedText.isNullOrBlank()) {
            val parsed = parseLyricsContent(cachedText, track.durationMs)
            if (parsed.isNotEmpty()) {
                setTrackStatus(context, track.id, LyricsFetchStatus.FOUND)
                return@withContext parsed
            }
        }

        // Tier 1b: Check side-car .lrc file matching track base filename (same folder / MediaStore)
        val sideCarText = findSideCarLrcText(context, track)
        if (!sideCarText.isNullOrBlank()) {
            val parsed = parseLyricsContent(sideCarText, track.durationMs)
            if (parsed.isNotEmpty()) {
                writeCachedLyricsText(context, track.id, sideCarText)
                setTrackStatus(context, track.id, LyricsFetchStatus.FOUND)
                return@withContext parsed
            }
        }

        if (!forceRefresh) {
            val existingStatus = getTrackStatus(context, track.id)
            if (existingStatus == LyricsFetchStatus.NOT_FOUND || existingStatus == LyricsFetchStatus.INSTRUMENTAL) {
                return@withContext emptyList()
            }
        }

        // Tier 2: Query LRCLIB online API
        when (val lookup = fetchFromLrclibDetailed(track)) {
            is LrclibLookupResult.Found -> {
                val parsed = parseLyricsContent(lookup.rawText, track.durationMs)
                if (parsed.isNotEmpty()) {
                    writeCachedLyricsText(context, track.id, lookup.rawText)
                    setTrackStatus(context, track.id, LyricsFetchStatus.FOUND)
                    return@withContext parsed
                } else {
                    setTrackStatus(context, track.id, LyricsFetchStatus.NOT_FOUND)
                }
            }
            is LrclibLookupResult.Instrumental -> {
                setTrackStatus(context, track.id, LyricsFetchStatus.INSTRUMENTAL)
            }
            is LrclibLookupResult.NotFound -> {
                setTrackStatus(context, track.id, LyricsFetchStatus.NOT_FOUND)
            }
            is LrclibLookupResult.RateLimited,
            is LrclibLookupResult.NetworkError -> {
                // Leave status as NOT_FETCHED so a future attempt or worker can retry
            }
        }

        emptyList()
    }

    /**
     * Tier 1 fast check (cache + side-car .lrc only, no network).
     */
    fun loadCachedOrSideCarLyrics(context: Context, track: Track): List<LyricLine> {
        val cachedText = readCachedLyricsText(context, track.id)
        if (!cachedText.isNullOrBlank()) {
            val parsed = parseLyricsContent(cachedText, track.durationMs)
            if (parsed.isNotEmpty()) {
                setTrackStatus(context, track.id, LyricsFetchStatus.FOUND)
                return parsed
            }
        }

        val sideCarText = findSideCarLrcText(context, track)
        if (!sideCarText.isNullOrBlank()) {
            val parsed = parseLyricsContent(sideCarText, track.durationMs)
            if (parsed.isNotEmpty()) {
                writeCachedLyricsText(context, track.id, sideCarText)
                setTrackStatus(context, track.id, LyricsFetchStatus.FOUND)
                return parsed
            }
        }

        return emptyList()
    }

    /**
     * Tier 3a: Import lyrics from a user-selected `.lrc` or `.txt` document Uri and persist to cache.
     */
    suspend fun importLyricsFromUri(context: Context, track: Track, uri: Uri): List<LyricLine> = withContext(Dispatchers.IO) {
        try {
            val rawText = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: return@withContext emptyList()

            if (rawText.isBlank()) return@withContext emptyList()
            val parsed = parseLyricsContent(rawText, track.durationMs)
            if (parsed.isNotEmpty()) {
                writeCachedLyricsText(context, track.id, rawText)
                setTrackStatus(context, track.id, LyricsFetchStatus.FOUND)
            }
            parsed
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Tier 3b: Save manually pasted or edited lyrics text to local cache and parse into [LyricLine]s.
     */
    suspend fun saveManualLyrics(context: Context, track: Track, rawText: String): List<LyricLine> = withContext(Dispatchers.IO) {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()
        val parsed = parseLyricsContent(trimmed, track.durationMs)
        if (parsed.isNotEmpty()) {
            writeCachedLyricsText(context, track.id, trimmed)
            setTrackStatus(context, track.id, LyricsFetchStatus.FOUND)
        }
        parsed
    }

    /**
     * Retrieves raw editable lyrics text for [track] so users can replace, fix timing, or upgrade to word-synced lyrics.
     * Prefers cached `.lrc` file (preserving exact tags/timestamps), then side-car `.lrc`, then serializes in-memory lines.
     */
    fun getEditableRawLyrics(context: Context, track: Track): String {
        val cached = readCachedLyricsText(context, track.id)
        if (!cached.isNullOrBlank()) return cached

        val sideCar = findSideCarLrcText(context, track)
        if (!sideCar.isNullOrBlank()) return sideCar

        if (hasUsableInMemoryLyrics(track)) {
            return serializeLyricsToLrc(track.lyrics)
        }
        return ""
    }

    /**
     * Deletes locally cached lyrics for [trackId] and resets status to [LyricsFetchStatus.NOT_FETCHED].
     */
    fun clearCachedLyrics(context: Context, trackId: String) {
        try {
            val file = getCacheFile(context, trackId)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}
        setTrackStatus(context, trackId, LyricsFetchStatus.NOT_FETCHED)
    }

    fun getCacheDir(context: Context): File {
        val dir = File(context.filesDir, CACHE_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getCacheFile(context: Context, trackId: String): File {
        val safeId = trackId.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return File(getCacheDir(context), "$safeId.lrc")
    }

    private fun getStatusFile(context: Context): File {
        return File(getCacheDir(context), STATUS_FILE_NAME)
    }

    fun hasUsableInMemoryLyrics(track: Track): Boolean {
        return track.lyrics.isNotEmpty() &&
            !(track.lyrics.size == 1 && track.lyrics[0].text.contains("No embedded lyrics", ignoreCase = true))
    }

    private fun loadStatusMapInternal(context: Context): MutableMap<String, LyricsFetchStatus> {
        val map = mutableMapOf<String, LyricsFetchStatus>()
        try {
            val file = getStatusFile(context)
            if (!file.exists()) return map
            val raw = file.readText(Charsets.UTF_8)
            if (raw.isBlank()) return map
            val obj = JSONObject(raw)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val trackId = keys.next()
                val statusName = obj.optString(trackId, "")
                val parsed = try {
                    LyricsFetchStatus.valueOf(statusName)
                } catch (_: Exception) {
                    null
                }
                if (parsed != null) {
                    map[trackId] = parsed
                }
            }
        } catch (_: Exception) {}
        return map
    }

    private fun saveStatusMapInternal(context: Context, map: Map<String, LyricsFetchStatus>) {
        try {
            val obj = JSONObject()
            map.forEach { (k, v) -> obj.put(k, v.name) }
            getStatusFile(context).writeText(obj.toString(), Charsets.UTF_8)
        } catch (_: Exception) {}
    }

    fun getTrackStatus(context: Context, trackId: String): LyricsFetchStatus {
        synchronized(statusLock) {
            val cacheFile = getCacheFile(context, trackId)
            if (cacheFile.exists() && cacheFile.length() > 0L) {
                return LyricsFetchStatus.FOUND
            }
            val map = loadStatusMapInternal(context)
            return map[trackId] ?: LyricsFetchStatus.NOT_FETCHED
        }
    }

    fun setTrackStatus(context: Context, trackId: String, status: LyricsFetchStatus) {
        synchronized(statusLock) {
            val map = loadStatusMapInternal(context)
            if (status == LyricsFetchStatus.NOT_FETCHED) {
                map.remove(trackId)
            } else {
                map[trackId] = status
            }
            saveStatusMapInternal(context, map)
        }
    }

    /**
     * Clears NOT_FOUND (and optionally INSTRUMENTAL) statuses so tracks become eligible for retry.
     * Returns the count of reset track entries.
     */
    fun clearFailedStatuses(context: Context, includeInstrumental: Boolean = true): Int {
        synchronized(statusLock) {
            val map = loadStatusMapInternal(context)
            val toReset = map.filterValues { status ->
                status == LyricsFetchStatus.NOT_FOUND ||
                    (includeInstrumental && status == LyricsFetchStatus.INSTRUMENTAL)
            }.keys.toList()
            toReset.forEach { map.remove(it) }
            saveStatusMapInternal(context, map)
            return toReset.size
        }
    }

    /**
     * Returns un-cached tracks whose status is still [LyricsFetchStatus.NOT_FETCHED].
     * Also opportunistically imports readable side-car `.lrc` files into `lyrics_cache/` and marks them [LyricsFetchStatus.FOUND].
     */
    fun getTracksNeedingSync(context: Context, tracks: List<Track>): List<Track> {
        val statusSnapshot = synchronized(statusLock) { loadStatusMapInternal(context) }
        val result = mutableListOf<Track>()
        for (track in tracks) {
            if (hasUsableInMemoryLyrics(track)) {
                if (statusSnapshot[track.id] != LyricsFetchStatus.FOUND) {
                    setTrackStatus(context, track.id, LyricsFetchStatus.FOUND)
                }
                continue
            }
            val cachedFile = getCacheFile(context, track.id)
            if (cachedFile.exists() && cachedFile.length() > 0L) {
                if (statusSnapshot[track.id] != LyricsFetchStatus.FOUND) {
                    setTrackStatus(context, track.id, LyricsFetchStatus.FOUND)
                }
                continue
            }
            val recorded = statusSnapshot[track.id] ?: LyricsFetchStatus.NOT_FETCHED
            if (recorded == LyricsFetchStatus.FOUND ||
                recorded == LyricsFetchStatus.NOT_FOUND ||
                recorded == LyricsFetchStatus.INSTRUMENTAL
            ) {
                continue
            }
            // Check local side-car .lrc before hitting the network
            val sideCarText = findSideCarLrcText(context, track)
            if (!sideCarText.isNullOrBlank()) {
                val parsed = parseLyricsContent(sideCarText, track.durationMs)
                if (parsed.isNotEmpty()) {
                    writeCachedLyricsText(context, track.id, sideCarText)
                    setTrackStatus(context, track.id, LyricsFetchStatus.FOUND)
                    continue
                }
            }
            result.add(track)
        }
        return result
    }

    fun getStatusSummary(context: Context, tracks: List<Track>): LyricsStatusSummary {
        val statusSnapshot = synchronized(statusLock) { loadStatusMapInternal(context) }
        var found = 0
        var notFound = 0
        var instrumental = 0
        var pending = 0
        for (track in tracks) {
            val hasCache = hasUsableInMemoryLyrics(track) ||
                (getCacheFile(context, track.id).let { it.exists() && it.length() > 0L })
            val status = if (hasCache) {
                LyricsFetchStatus.FOUND
            } else {
                statusSnapshot[track.id] ?: LyricsFetchStatus.NOT_FETCHED
            }
            when (status) {
                LyricsFetchStatus.FOUND -> found++
                LyricsFetchStatus.NOT_FOUND -> notFound++
                LyricsFetchStatus.INSTRUMENTAL -> instrumental++
                LyricsFetchStatus.NOT_FETCHED -> pending++
            }
        }
        return LyricsStatusSummary(
            totalTracks = tracks.size,
            foundCount = found,
            notFoundCount = notFound,
            instrumentalCount = instrumental,
            pendingCount = pending
        )
    }

    fun readCachedLyricsText(context: Context, trackId: String): String? {
        return try {
            val lrcFile = getCacheFile(context, trackId)
            if (lrcFile.exists() && lrcFile.canRead()) {
                lrcFile.readText(Charsets.UTF_8).takeIf { it.isNotBlank() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun writeCachedLyricsText(context: Context, trackId: String, rawText: String) {
        try {
            val file = getCacheFile(context, trackId)
            file.writeText(rawText, Charsets.UTF_8)
        } catch (_: Exception) {}
    }

    private fun findSideCarLrcText(context: Context, track: Track): String? {
        val uriString = track.uriString ?: return null
        val uri = try {
            Uri.parse(uriString)
        } catch (_: Exception) {
            return null
        }

        // 1. Direct file path lookup if file:// or raw filesystem path
        if (uri.scheme == "file" || uri.scheme == null) {
            val path = uri.path
            if (!path.isNullOrBlank()) {
                val base = stripAudioExtension(path)
                for (ext in listOf(".lrc", ".LRC")) {
                    val candidate = File(base + ext)
                    if (candidate.exists() && candidate.canRead()) {
                        return try {
                            candidate.readText(Charsets.UTF_8).takeIf { it.isNotBlank() }
                        } catch (_: Exception) {
                            null
                        }
                    }
                }
            }
        }

        // 2. Inspect MediaStore / OpenableColumns for display name, relative path, or _DATA
        var displayName: String? = null
        var relativePath: String? = null
        var dataPath: String? = null

        try {
            val audioCols = arrayOf(
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.RELATIVE_PATH,
                MediaStore.Audio.Media.DATA
            )
            context.contentResolver.query(uri, audioCols, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                    val relIdx = cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                    val dataIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    if (nameIdx != -1) displayName = cursor.getString(nameIdx)
                    if (relIdx != -1) relativePath = cursor.getString(relIdx)
                    if (dataIdx != -1) dataPath = cursor.getString(dataIdx)
                }
            }
        } catch (_: Exception) {}

        if (displayName.isNullOrBlank()) {
            try {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx != -1) displayName = cursor.getString(nameIdx)
                    }
                }
            } catch (_: Exception) {}
        }

        // Check sibling .lrc via _DATA filesystem path if readable
        if (!dataPath.isNullOrBlank()) {
            val base = stripAudioExtension(dataPath!!)
            for (ext in listOf(".lrc", ".LRC")) {
                val sibling = File(base + ext)
                if (sibling.exists() && sibling.canRead()) {
                    try {
                        val content = sibling.readText(Charsets.UTF_8)
                        if (content.isNotBlank()) return content
                    } catch (_: Exception) {}
                }
            }
        }

        // Query MediaStore.Files for a matching `<baseName>.lrc`
        val resolvedName = displayName ?: uri.lastPathSegment?.substringAfterLast('/')
        if (!resolvedName.isNullOrBlank()) {
            val baseName = stripAudioExtension(resolvedName).substringBeforeLast('.')
            if (baseName.isNotBlank()) {
                val targetLrcName = "$baseName.lrc"
                try {
                    val filesUri = MediaStore.Files.getContentUri("external")
                    val projection = arrayOf(
                        MediaStore.Files.FileColumns._ID,
                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                        MediaStore.Files.FileColumns.RELATIVE_PATH
                    )
                    val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} = ? COLLATE NOCASE"
                    context.contentResolver.query(
                        filesUri,
                        projection,
                        selection,
                        arrayOf(targetLrcName),
                        null
                    )?.use { cursor ->
                        val idCol = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                        val relCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH)
                        var fallbackId: Long? = null
                        var exactFolderId: Long? = null

                        while (cursor.moveToNext()) {
                            if (idCol == -1) continue
                            val fileId = cursor.getLong(idCol)
                            val rowRel = if (relCol != -1) cursor.getString(relCol) else null
                            if (!relativePath.isNullOrBlank() && rowRel == relativePath) {
                                exactFolderId = fileId
                                break
                            }
                            if (fallbackId == null) {
                                fallbackId = fileId
                            }
                        }

                        val chosenId = exactFolderId ?: fallbackId
                        if (chosenId != null) {
                            val lrcUri = android.content.ContentUris.withAppendedId(filesUri, chosenId)
                            context.contentResolver.openInputStream(lrcUri)?.use { stream ->
                                val content = stream.bufferedReader(Charsets.UTF_8).readText()
                                if (content.isNotBlank()) return content
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        return null
    }

    private fun stripAudioExtension(nameOrPath: String): String {
        for (ext in audioExtensions) {
            if (nameOrPath.endsWith(ext, ignoreCase = true)) {
                return nameOrPath.substring(0, nameOrPath.length - ext.length)
            }
        }
        return nameOrPath
    }

    /**
     * Tier 2: Async HTTP GET to `https://lrclib.net/api/get` with URL-encoded parameters.
     */
    internal fun fetchFromLrclib(track: Track, baseUrl: HttpUrl? = null): String? {
        return when (val result = fetchFromLrclibDetailed(track, baseUrl)) {
            is LrclibLookupResult.Found -> result.rawText
            else -> null
        }
    }

    /**
     * Tier 2 / Batch Worker HTTP GET to `https://lrclib.net/api/get` with URL-encoded parameters
     * and structured HTTP 429 `Retry-After` & `instrumental` handling.
     */
    internal fun fetchFromLrclibDetailed(track: Track, baseUrl: HttpUrl? = null): LrclibLookupResult {
        if (track.title.isBlank()) return LrclibLookupResult.NotFound

        val cleanArtist = if (track.artist.equals("Unknown Artist", ignoreCase = true) ||
            track.artist.equals("Local Artist", ignoreCase = true)
        ) "" else track.artist

        val cleanAlbum = if (track.album.equals("Device Storage", ignoreCase = true) ||
            track.album.equals("Device Music", ignoreCase = true)
        ) "" else track.album

        val durationSeconds = (track.durationMs / 1000L).coerceAtLeast(1L)

        val urlBuilder = if (baseUrl != null) {
            baseUrl.newBuilder()
        } else {
            HttpUrl.Builder()
                .scheme("https")
                .host(LRCLIB_HOST)
                .addPathSegments("api/get")
        }

        urlBuilder.addQueryParameter("track_name", track.title)
        if (cleanArtist.isNotBlank()) {
            urlBuilder.addQueryParameter("artist_name", cleanArtist)
        }
        if (cleanAlbum.isNotBlank()) {
            urlBuilder.addQueryParameter("album_name", cleanAlbum)
        }
        urlBuilder.addQueryParameter("duration", durationSeconds.toString())

        val request = Request.Builder()
            .url(urlBuilder.build())
            .header("User-Agent", USER_AGENT)
            .get()
            .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                when {
                    response.code == 429 -> {
                        val retryMs = parseRetryAfterMs(response.header("Retry-After"))
                        LrclibLookupResult.RateLimited(retryMs)
                    }
                    response.code == 404 -> LrclibLookupResult.NotFound
                    !response.isSuccessful -> {
                        if (response.code in 500..599) {
                            LrclibLookupResult.NetworkError
                        } else {
                            LrclibLookupResult.NotFound
                        }
                    }
                    else -> {
                        val body = response.body?.string() ?: return LrclibLookupResult.NotFound
                        parseLrclibJsonDetailed(body)
                    }
                }
            }
        } catch (_: Exception) {
            LrclibLookupResult.NetworkError
        }
    }

    internal fun parseRetryAfterMs(headerValue: String?): Long {
        if (headerValue.isNullOrBlank()) return DEFAULT_RETRY_AFTER_MS
        val trimmed = headerValue.trim()
        val seconds = trimmed.toLongOrNull()
        if (seconds != null) {
            return (seconds * 1000L).coerceIn(1_000L, 120_000L)
        }
        return DEFAULT_RETRY_AFTER_MS
    }

    internal fun parseLrclibJsonDetailed(jsonString: String): LrclibLookupResult {
        return try {
            val obj = JSONObject(jsonString)
            if (obj.optBoolean("instrumental", false)) {
                return LrclibLookupResult.Instrumental
            }
            val extracted = parseLrclibJson(jsonString)
            if (!extracted.isNullOrBlank()) {
                LrclibLookupResult.Found(extracted)
            } else {
                LrclibLookupResult.NotFound
            }
        } catch (_: Exception) {
            LrclibLookupResult.NotFound
        }
    }

    /**
     * Parses the JSON payload from LRCLIB API, preferring `syncedLyrics` over `plainLyrics`.
     */
    fun parseLrclibJson(jsonString: String): String? {
        return try {
            val obj = JSONObject(jsonString)
            val synced = obj.optString("syncedLyrics", "").takeIf {
                !obj.isNull("syncedLyrics") && it.isNotBlank() && it != "null"
            }
            if (synced != null) return synced

            val plain = obj.optString("plainLyrics", "").takeIf {
                !obj.isNull("plainLyrics") && it.isNotBlank() && it != "null"
            }
            plain
        } catch (_: Exception) {
            null
        }
    }

    private val lrcTimestampRegex = Regex("\\[(\\d{1,3}):(\\d{2})(?:[.:](\\d{1,3}))?\\]")
    private val wordTimestampRegex = Regex("<(\\d{1,3}):(\\d{2})(?:[.:](\\d{1,3}))?>")
    private val metadataTagRegex = Regex("^\\[(ar|ti|al|au|lr|length|by|offset|re|ve):.*\\]$", RegexOption.IGNORE_CASE)

    private fun parseTimestampMs(minutesStr: String, secondsStr: String, fractionRaw: String): Long {
        val minutes = minutesStr.toLongOrNull() ?: 0L
        val seconds = secondsStr.toLongOrNull() ?: 0L
        val millis = when (fractionRaw.length) {
            0 -> 0L
            1 -> (fractionRaw.toLongOrNull() ?: 0L) * 100L
            2 -> (fractionRaw.toLongOrNull() ?: 0L) * 10L
            else -> fractionRaw.take(3).toLongOrNull() ?: 0L
        }
        return (minutes * 60L + seconds) * 1000L + millis
    }

    fun formatLrcTimestamp(timeMs: Long): String {
        val safe = timeMs.coerceAtLeast(0L)
        val minutes = safe / 60_000L
        val seconds = (safe % 60_000L) / 1000L
        val centis = (safe % 1000L) / 10L
        return String.format(java.util.Locale.US, "[%02d:%02d.%02d]", minutes, seconds, centis)
    }

    fun formatWordTimestamp(timeMs: Long): String {
        val safe = timeMs.coerceAtLeast(0L)
        val minutes = safe / 60_000L
        val seconds = (safe % 60_000L) / 1000L
        val centis = (safe % 1000L) / 10L
        return String.format(java.util.Locale.US, "<%02d:%02d.%02d>", minutes, seconds, centis)
    }

    /**
     * Serializes parsed [LyricLine] items back into LRC or Enhanced Word-Synced LRC text.
     */
    fun serializeLyricsToLrc(lyrics: List<LyricLine>): String {
        if (lyrics.isEmpty()) return ""
        return lyrics.joinToString("\n") { line ->
            when {
                !line.isSynced -> line.text
                line.words.isNotEmpty() -> {
                    val wordBody = line.words.joinToString("") { word ->
                        "${formatWordTimestamp(word.timeMs)}${word.text}"
                    }
                    val lastWord = line.words.lastOrNull()
                    val endTag = if (lastWord?.endTimeMs != null && lastWord.endTimeMs > lastWord.timeMs) {
                        formatWordTimestamp(lastWord.endTimeMs)
                    } else {
                        ""
                    }
                    "${formatLrcTimestamp(line.timeMs)}$wordBody$endTag"
                }
                else -> "${formatLrcTimestamp(line.timeMs)}${line.text}"
            }
        }
    }

    /**
     * Shifts all `[mm:ss.xx]` and `<mm:ss.xx>` timestamps inside [rawText] by [deltaMs].
     */
    fun shiftLrcTimestamps(rawText: String, deltaMs: Long): String {
        if (rawText.isBlank() || deltaMs == 0L) return rawText
        val afterLineTags = lrcTimestampRegex.replace(rawText) { match ->
            val current = parseTimestampMs(
                match.groupValues[1],
                match.groupValues[2],
                match.groupValues.getOrNull(3).orEmpty()
            )
            formatLrcTimestamp((current + deltaMs).coerceAtLeast(0L))
        }
        return wordTimestampRegex.replace(afterLineTags) { match ->
            val current = parseTimestampMs(
                match.groupValues[1],
                match.groupValues[2],
                match.groupValues.getOrNull(3).orEmpty()
            )
            formatWordTimestamp((current + deltaMs).coerceAtLeast(0L))
        }
    }

    /**
     * Strips all LRC line/word timestamps and metadata headers to leave clean lyric text lines for re-syncing.
     */
    fun stripTimestampsForTapSync(rawText: String): List<String> {
        if (rawText.isBlank()) return emptyList()
        return rawText.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !metadataTagRegex.matches(it) }
            .map { line ->
                line.replace(lrcTimestampRegex, "")
                    .replace(wordTimestampRegex, "")
                    .replace(Regex("\\s+"), " ")
                    .trim()
            }
            .filter { it.isNotEmpty() }
            .toList()
    }

    private fun extractWordSegments(
        body: String,
        matches: List<MatchResult>,
        defaultStartMs: Long
    ): List<LyricWord> {
        if (matches.isEmpty()) return emptyList()
        val rawWords = mutableListOf<LyricWord>()

        // Any leading text before the first inline/word tag starts at line timestamp
        val leadingText = body.substring(0, matches.first().range.first)
        if (leadingText.isNotBlank()) {
            val firstTagMs = parseTimestampMs(
                matches.first().groupValues[1],
                matches.first().groupValues[2],
                matches.first().groupValues.getOrNull(3).orEmpty()
            )
            rawWords.add(
                LyricWord(
                    timeMs = defaultStartMs,
                    text = leadingText.trimStart(),
                    endTimeMs = firstTagMs.coerceAtLeast(defaultStartMs)
                )
            )
        }

        for (i in matches.indices) {
            val match = matches[i]
            val startMs = parseTimestampMs(
                match.groupValues[1],
                match.groupValues[2],
                match.groupValues.getOrNull(3).orEmpty()
            )
            val tokenStart = match.range.last + 1
            val tokenEnd = if (i + 1 < matches.size) matches[i + 1].range.first else body.length
            val slice = body.substring(tokenStart, tokenEnd)

            if (slice.isBlank()) {
                // Trailing or standalone timestamp acts as explicit endTimeMs of previous word
                if (rawWords.isNotEmpty()) {
                    val prev = rawWords.last()
                    if (prev.endTimeMs == null || startMs >= prev.timeMs) {
                        rawWords[rawWords.lastIndex] = prev.copy(endTimeMs = startMs.coerceAtLeast(prev.timeMs))
                    }
                }
            } else {
                val normalized = if (rawWords.isEmpty()) slice.trimStart() else slice
                rawWords.add(
                    LyricWord(
                        timeMs = startMs,
                        text = normalized,
                        endTimeMs = null
                    )
                )
            }
        }

        // Trim trailing whitespace on the final word and fill in consecutive word endTimeMs
        if (rawWords.isEmpty()) return emptyList()
        val lastTrimmed = rawWords.last().text.trimEnd()
        if (lastTrimmed.isEmpty()) {
            rawWords.removeAt(rawWords.lastIndex)
        } else {
            rawWords[rawWords.lastIndex] = rawWords.last().copy(text = lastTrimmed)
        }

        return rawWords.mapIndexed { index, word ->
            val resolvedEnd = word.endTimeMs ?: rawWords.getOrNull(index + 1)?.timeMs?.coerceAtLeast(word.timeMs)
            word.copy(endTimeMs = resolvedEnd)
        }
    }

    /**
     * Parses line-synced LRC (`[mm:ss.xx]`), Enhanced word-synced LRC (`<mm:ss.xx>word`),
     * inline `[mm:ss.xx]word` lines, or plain unsynced lyrics text into [LyricLine] items.
     */
    fun parseLyricsContent(rawText: String, durationMs: Long): List<LyricLine> {
        if (rawText.isBlank()) return emptyList()

        val syncedLines = mutableListOf<LyricLine>()
        val plainCandidateLines = mutableListOf<String>()

        rawText.lineSequence().forEach { rawLine ->
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) return@forEach
            if (metadataTagRegex.matches(trimmed)) return@forEach

            val bracketMatches = lrcTimestampRegex.findAll(trimmed).toList()
            val angleMatches = wordTimestampRegex.findAll(trimmed).toList()

            when {
                // Case 1: Inline bracket timestamps interleaved with text (e.g. "[00:12.00]Hello [00:12.50]world")
                bracketMatches.size >= 2 && hasTextBetweenBracketTags(trimmed, bracketMatches) -> {
                    val firstTimeMs = parseTimestampMs(
                        bracketMatches.first().groupValues[1],
                        bracketMatches.first().groupValues[2],
                        bracketMatches.first().groupValues.getOrNull(3).orEmpty()
                    )
                    val words = extractWordSegments(trimmed, bracketMatches, firstTimeMs)
                    val cleanText = trimmed.replace(lrcTimestampRegex, "").replace(wordTimestampRegex, "").trim()
                    if (cleanText.isNotEmpty()) {
                        syncedLines.add(
                            LyricLine(
                                timeMs = firstTimeMs,
                                text = cleanText,
                                isSynced = true,
                                words = words
                            )
                        )
                    }
                }

                // Case 2: Enhanced LRC line with `[mm:ss.xx]` line tag(s) and `<mm:ss.xx>` word tags
                bracketMatches.isNotEmpty() && angleMatches.isNotEmpty() -> {
                    val bodyWithoutBrackets = trimmed.replace(lrcTimestampRegex, "").trim()
                    val bodyAngleMatches = wordTimestampRegex.findAll(bodyWithoutBrackets).toList()
                    val cleanText = bodyWithoutBrackets.replace(wordTimestampRegex, "").trim()
                    if (cleanText.isNotEmpty()) {
                        val firstLineTimeMs = parseTimestampMs(
                            bracketMatches.first().groupValues[1],
                            bracketMatches.first().groupValues[2],
                            bracketMatches.first().groupValues.getOrNull(3).orEmpty()
                        )
                        val baseWords = extractWordSegments(bodyWithoutBrackets, bodyAngleMatches, firstLineTimeMs)
                        for (match in bracketMatches) {
                            val lineTimeMs = parseTimestampMs(
                                match.groupValues[1],
                                match.groupValues[2],
                                match.groupValues.getOrNull(3).orEmpty()
                            )
                            val deltaMs = lineTimeMs - firstLineTimeMs
                            val shiftedWords = if (deltaMs == 0L) {
                                baseWords
                            } else {
                                baseWords.map { w ->
                                    w.copy(
                                        timeMs = (w.timeMs + deltaMs).coerceAtLeast(0L),
                                        endTimeMs = w.endTimeMs?.let { (it + deltaMs).coerceAtLeast(0L) }
                                    )
                                }
                            }
                            syncedLines.add(
                                LyricLine(
                                    timeMs = lineTimeMs,
                                    text = cleanText,
                                    isSynced = true,
                                    words = shiftedWords
                                )
                            )
                        }
                    }
                }

                // Case 3: Word tags `<mm:ss.xx>` only without leading `[mm:ss.xx]` bracket
                bracketMatches.isEmpty() && angleMatches.isNotEmpty() -> {
                    val firstTimeMs = parseTimestampMs(
                        angleMatches.first().groupValues[1],
                        angleMatches.first().groupValues[2],
                        angleMatches.first().groupValues.getOrNull(3).orEmpty()
                    )
                    val words = extractWordSegments(trimmed, angleMatches, firstTimeMs)
                    val cleanText = trimmed.replace(wordTimestampRegex, "").trim()
                    if (cleanText.isNotEmpty()) {
                        syncedLines.add(
                            LyricLine(
                                timeMs = firstTimeMs,
                                text = cleanText,
                                isSynced = true,
                                words = words
                            )
                        )
                    }
                }

                // Case 4: Standard line-synced LRC `[mm:ss.xx]` (supports repeated chorus timestamps)
                bracketMatches.isNotEmpty() -> {
                    val text = trimmed.replace(lrcTimestampRegex, "").trim()
                    if (text.isNotEmpty()) {
                        for (match in bracketMatches) {
                            val timeMs = parseTimestampMs(
                                match.groupValues[1],
                                match.groupValues[2],
                                match.groupValues.getOrNull(3).orEmpty()
                            )
                            syncedLines.add(LyricLine(timeMs = timeMs, text = text, isSynced = true))
                        }
                    }
                }

                // Case 5: Plain text line
                else -> {
                    plainCandidateLines.add(trimmed)
                }
            }
        }

        if (syncedLines.isNotEmpty()) {
            return syncedLines.sortedBy { it.timeMs }
        }

        if (plainCandidateLines.isEmpty()) return emptyList()

        val safeDuration = durationMs.coerceAtLeast(30_000L)
        val stepMs = (safeDuration / plainCandidateLines.size.coerceAtLeast(1)).coerceAtLeast(1000L)
        return plainCandidateLines.mapIndexed { index, line ->
            LyricLine(
                timeMs = index * stepMs,
                text = line,
                isSynced = false
            )
        }
    }

    private fun hasTextBetweenBracketTags(line: String, matches: List<MatchResult>): Boolean {
        for (i in 0 until matches.size - 1) {
            val between = line.substring(matches[i].range.last + 1, matches[i + 1].range.first)
            if (between.isNotBlank()) return true
        }
        return false
    }
}
