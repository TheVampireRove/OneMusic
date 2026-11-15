package com.example.diagnostics

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.example.audio.AudioPlaybackManager
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Model representing a captured diagnostic crash report file.
 */
data class CrashReportItem(
    val file: File,
    val fileName: String,
    val timestampFormatted: String,
    val sizeBytes: Long,
    val previewText: String
)

/**
 * Crash interceptor, diagnostics aggregator, and report file manager.
 */
object CrashReportManager {

    private const val DIRECTORY_NAME = "crash_reports"
    private var isHandlerInstalled = false
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    // Track posture state dynamically
    var currentPostureState: String = "Normal Handheld"

    fun init(context: Context) {
        if (isHandlerInstalled) return
        isHandlerInstalled = true

        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleUncaughtException(context.applicationContext, thread, throwable)
        }
    }

    private fun handleUncaughtException(context: Context, thread: Thread, throwable: Throwable) {
        try {
            val prefs = CrashDiagnosticsPreferences.getInstance(context)
            if (prefs.isCrashLoggingEnabledSync()) {
                writeCrashReport(context, thread, throwable)
            }
        } catch (e: Exception) {
            Log.e("CrashReportManager", "Failed to write diagnostic crash report", e)
        } finally {
            // Forward to default system handler so OS handles graceful termination/dialog
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun writeCrashReport(context: Context, thread: Thread, throwable: Throwable) {
        val dir = File(context.filesDir, DIRECTORY_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val dateFmt = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val readableDateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z", Locale.US)
        val now = Date()
        val fileName = "crash_report_${dateFmt.format(now)}.txt"
        val reportFile = File(dir, fileName)

        // Gather system diagnostics
        val appVersionName = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${pInfo.versionName} (${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo.longVersionCode else @Suppress("DEPRECATION") pInfo.versionCode})"
        } catch (_: Exception) {
            "Unknown"
        }

        val playbackManager = try { AudioPlaybackManager.getInstance() } catch (_: Exception) { null }
        val currentTrack = playbackManager?.currentTrack?.value
        val isPlaying = playbackManager?.isPlaying?.value ?: false
        val currentPos = playbackManager?.currentPositionMs?.value ?: 0L
        val playerState = when {
            isPlaying -> "PLAYING (Position: ${currentPos}ms)"
            playbackManager?.audioEngine?.exoPlayer?.playbackState == androidx.media3.common.Player.STATE_BUFFERING -> "BUFFERING"
            else -> "IDLE / PAUSED"
        }

        val trackInfo = if (currentTrack != null) {
            "ID: ${currentTrack.id}\nTitle: ${currentTrack.title}\nArtist: ${currentTrack.artist}\nAlbum: ${currentTrack.album}\nDuration: ${currentTrack.durationMs}ms"
        } else {
            "None (No active track)"
        }

        val stackTrace = Log.getStackTraceString(throwable)

        val reportContent = buildString {
            appendLine("=================================================================")
            appendLine("                ONEMUSIC CRASH & DIAGNOSTIC REPORT               ")
            appendLine("=================================================================")
            appendLine("Timestamp:         ${readableDateFmt.format(now)}")
            appendLine("Thread:            ${thread.name} (ID: ${thread.id}, Priority: ${thread.priority})")
            appendLine("App Version:       $appVersionName")
            appendLine("Package:           ${context.packageName}")
            appendLine("-----------------------------------------------------------------")
            appendLine("DEVICE & SYSTEM:")
            appendLine("Manufacturer:      ${Build.MANUFACTURER}")
            appendLine("Model:             ${Build.MODEL} (${Build.DEVICE})")
            appendLine("Product / Board:   ${Build.PRODUCT} / ${Build.BOARD}")
            appendLine("Android OS:        ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}, Build ${Build.DISPLAY})")
            appendLine("Fold/Screen State: $currentPostureState")
            appendLine("-----------------------------------------------------------------")
            appendLine("AUDIO & PLAYER STATE:")
            appendLine("Player State:      $playerState")
            appendLine("Current Track:")
            appendLine(trackInfo)
            appendLine("-----------------------------------------------------------------")
            appendLine("EXCEPTION SUMMARY:")
            appendLine("Exception Type:    ${throwable.javaClass.name}")
            appendLine("Message:           ${throwable.message ?: "(null message)"}")
            appendLine("Cause:             ${throwable.cause?.toString() ?: "(none)"}")
            appendLine("-----------------------------------------------------------------")
            appendLine("FULL STACK TRACE:")
            appendLine(stackTrace.ifBlank { "(No stack trace available)" })
            appendLine("=================================================================")
        }

        FileWriter(reportFile).use { writer ->
            writer.write(reportContent)
        }

        Log.i("CrashReportManager", "Diagnostic crash report saved: ${reportFile.absolutePath}")
    }

    /**
     * Retrieves all saved crash reports sorted by newest first.
     */
    fun getSavedCrashReports(context: Context): List<CrashReportItem> {
        val dir = File(context.filesDir, DIRECTORY_NAME)
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        val files = dir.listFiles { file -> file.isFile && file.name.startsWith("crash_report_") && file.name.endsWith(".txt") }
            ?: return emptyList()

        val displayFmt = SimpleDateFormat("MMM d, yyyy  h:mm:ss a", Locale.getDefault())

        return files.sortedByDescending { it.lastModified() }.map { file ->
            val preview = try {
                file.bufferedReader().use { reader ->
                    // Read first few lines for quick preview
                    val lines = mutableListOf<String>()
                    var line: String? = reader.readLine()
                    var count = 0
                    while (line != null && count < 25) {
                        lines.add(line)
                        count++
                        line = reader.readLine()
                    }
                    lines.joinToString("\n")
                }
            } catch (_: Exception) {
                "(Unable to read preview)"
            }

            CrashReportItem(
                file = file,
                fileName = file.name,
                timestampFormatted = displayFmt.format(Date(file.lastModified())),
                sizeBytes = file.length(),
                previewText = preview
            )
        }
    }

    /**
     * Read the complete text of a crash file.
     */
    fun readReportText(file: File): String {
        return try {
            file.readText()
        } catch (e: Exception) {
            "Failed to load crash report content: ${e.message}"
        }
    }

    /**
     * Delete a single crash report file.
     */
    fun deleteReport(file: File): Boolean {
        return try {
            file.delete()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Cleans up all saved diagnostic crash reports.
     */
    fun deleteAllReports(context: Context): Int {
        val dir = File(context.filesDir, DIRECTORY_NAME)
        if (!dir.exists()) return 0
        val files = dir.listFiles { file -> file.isFile && file.name.startsWith("crash_report_") && file.name.endsWith(".txt") }
            ?: return 0
        var count = 0
        for (f in files) {
            if (f.delete()) count++
        }
        return count
    }

    /**
     * Creates an Intent to share or export the crash file via the system share sheet.
     */
    fun createShareIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "OneMusic Diagnostic Crash Report: ${file.name}")
            putExtra(Intent.EXTRA_TEXT, "Attached is the diagnostic crash log from OneMusic for technical analysis.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Convenience method to write a manual diagnostic snapshot (e.g. for testing the pipeline).
     */
    fun generateTestReport(context: Context) {
        val fakeException = RuntimeException("User-triggered diagnostic snapshot for testing crash pipeline")
        writeCrashReport(context, Thread.currentThread(), fakeException)
    }
}
