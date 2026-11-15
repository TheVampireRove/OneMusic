package com.example.data

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AlbumArtCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8 // Use 1/8th of available memory

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun get(key: String): Bitmap? = synchronized(memoryCache) {
        memoryCache.get(key)
    }

    fun put(key: String, bitmap: Bitmap) = synchronized(memoryCache) {
        memoryCache.put(key, bitmap)
    }

    suspend fun loadArtwork(context: Context, uriString: String?, key: String?): Bitmap? = withContext(Dispatchers.IO) {
        if (key != null) {
            val cached = get(key)
            if (cached != null) return@withContext cached
        }

        if (uriString == null) return@withContext null

        try {
            val uri = Uri.parse(uriString)
            // Try ContentResolver thumbnail first on Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val thumb = context.contentResolver.loadThumbnail(uri, Size(512, 512), null)
                    if (thumb != null) {
                        if (key != null) put(key, thumb)
                        return@withContext thumb
                    }
                } catch (_: Exception) {}
            }

            // Fallback to MediaMetadataRetriever
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val picture = retriever.embeddedPicture
                if (picture != null) {
                    val bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.size)
                    if (bitmap != null) {
                        if (key != null) put(key, bitmap)
                        return@withContext bitmap
                    }
                }
            } catch (_: Exception) {
            } finally {
                try {
                    retriever.release()
                } catch (_: Exception) {}
            }

            // Direct input stream attempt
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        if (key != null) put(key, bitmap)
                        return@withContext bitmap
                    }
                }
            } catch (_: Exception) {}

        } catch (_: Exception) {}

        null
    }

    fun extractExpressivePalette(bitmap: Bitmap): Triple<Color, Color, Color> {
        val width = bitmap.width
        val height = bitmap.height
        val stepX = kotlin.math.max(1, width / 20)
        val stepY = kotlin.math.max(1, height / 20)

        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        var count = 0

        var maxSat = 0f
        var vibrantColor = 0

        var lightSat = 0f
        var accentColor = 0

        for (x in 0 until width step stepX) {
            for (y in 0 until height step stepY) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                rSum += r
                gSum += g
                bSum += b
                count++

                val hsv = FloatArray(3)
                android.graphics.Color.RGBToHSV(r, g, b, hsv)
                val sat = hsv[1]
                val value = hsv[2]

                // Vibrant saturated color
                if (sat > maxSat && value in 0.25f..0.95f) {
                    maxSat = sat
                    vibrantColor = pixel
                }

                // Accent / light secondary
                if (sat in 0.3f..0.85f && value > 0.7f && sat > lightSat) {
                    lightSat = sat
                    accentColor = pixel
                }
            }
        }

        if (count == 0) {
            return Triple(Color(0xFF7C4DFF), Color(0xFF00E5FF), Color(0xFFD1C4E9))
        }

        val avgR = ((rSum / count).coerceIn(0, 255)).toInt()
        val avgG = ((gSum / count).coerceIn(0, 255)).toInt()
        val avgB = ((bSum / count).coerceIn(0, 255)).toInt()

        val primary = if (vibrantColor != 0) Color(vibrantColor) else Color(avgR, avgG, avgB)
        val secondary = if (accentColor != 0) Color(accentColor) else Color(
            ((avgR + 50).coerceAtMost(255)),
            ((avgG + 80).coerceAtMost(255)),
            ((avgB + 120).coerceAtMost(255))
        )
        val accent = Color(
            ((primary.red * 255 * 0.7f + 255 * 0.3f).toInt().coerceIn(0, 255)),
            ((primary.green * 255 * 0.7f + 255 * 0.3f).toInt().coerceIn(0, 255)),
            ((primary.blue * 255 * 0.7f + 255 * 0.3f).toInt().coerceIn(0, 255))
        )

        return Triple(primary, secondary, accent)
    }
}
