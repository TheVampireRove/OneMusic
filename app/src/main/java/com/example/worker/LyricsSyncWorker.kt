package com.example.worker

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.LocalTracksCache
import com.example.data.LrclibLookupResult
import com.example.data.LyricsFetchStatus
import com.example.data.LyricsRepository
import com.example.data.MusicRepository
import com.example.model.Track
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Rate-limit-safe WorkManager [CoroutineWorker] that sequentially fetches and caches
 * synchronized lyrics from LRCLIB (`https://lrclib.net/api/get`) for un-cached library tracks.
 */
class LyricsSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val repo = MusicRepository.getInstance().apply { initContext(applicationContext) }
        val cachedLocalTracks = LocalTracksCache.loadTracks(applicationContext)
        if (cachedLocalTracks.isNotEmpty()) {
            repo.addTracks(cachedLocalTracks)
        }

        val allTracks = repo.tracks.value
        val pendingTracks = LyricsRepository.getTracksNeedingSync(applicationContext, allTracks)

        val total = pendingTracks.size
        if (total == 0) {
            return@withContext Result.success(
                workDataOf(
                    KEY_CURRENT to 0,
                    KEY_TOTAL to 0,
                    KEY_FOUND to 0,
                    KEY_FAILED to 0
                )
            )
        }

        var foundCount = 0
        var failedCount = 0
        var networkErrorCount = 0
        val delayBetweenRequestsMs = interRequestDelayMsOverride ?: DEFAULT_REQUEST_DELAY_MS

        try {
            for ((index, track) in pendingTracks.withIndex()) {
                if (isStopped) {
                    return@withContext Result.failure(
                        workDataOf(
                            KEY_CURRENT to index,
                            KEY_TOTAL to total,
                            KEY_FOUND to foundCount,
                            KEY_FAILED to failedCount
                        )
                    )
                }

                setProgress(
                    workDataOf(
                        KEY_CURRENT to (index + 1),
                        KEY_TOTAL to total,
                        KEY_FOUND to foundCount,
                        KEY_FAILED to failedCount,
                        KEY_TRACK_TITLE to track.title
                    )
                )

                val lookup = fetchSingleTrackWithRetryAfter(track)
                when (lookup) {
                    is LrclibLookupResult.Found -> {
                        val parsed = LyricsRepository.parseLyricsContent(lookup.rawText, track.durationMs)
                        if (parsed.isNotEmpty()) {
                            LyricsRepository.writeCachedLyricsText(applicationContext, track.id, lookup.rawText)
                            LyricsRepository.setTrackStatus(applicationContext, track.id, LyricsFetchStatus.FOUND)
                            repo.updateTrackLyrics(track.id, parsed)
                            foundCount++
                        } else {
                            LyricsRepository.setTrackStatus(applicationContext, track.id, LyricsFetchStatus.NOT_FOUND)
                            failedCount++
                        }
                    }
                    is LrclibLookupResult.Instrumental -> {
                        LyricsRepository.setTrackStatus(applicationContext, track.id, LyricsFetchStatus.INSTRUMENTAL)
                    }
                    is LrclibLookupResult.NotFound -> {
                        LyricsRepository.setTrackStatus(applicationContext, track.id, LyricsFetchStatus.NOT_FOUND)
                        failedCount++
                    }
                    is LrclibLookupResult.RateLimited,
                    is LrclibLookupResult.NetworkError -> {
                        networkErrorCount++
                    }
                }

                if (index < pendingTracks.lastIndex && !isStopped && delayBetweenRequestsMs > 0L) {
                    delay(delayBetweenRequestsMs)
                }
            }

            if (networkErrorCount > 0 && foundCount == 0 && failedCount == 0 && runAttemptCount < MAX_WORK_RETRIES) {
                return@withContext Result.retry()
            }

            Result.success(
                workDataOf(
                    KEY_CURRENT to total,
                    KEY_TOTAL to total,
                    KEY_FOUND to foundCount,
                    KEY_FAILED to failedCount
                )
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Exception) {
            if (runAttemptCount < MAX_WORK_RETRIES) Result.retry() else Result.failure()
        }
    }

    private suspend fun fetchSingleTrackWithRetryAfter(track: Track): LrclibLookupResult {
        val fetcher = lrclibFetcherOverride ?: { t -> LyricsRepository.fetchFromLrclibDetailed(t) }
        var attempts = 0
        while (attempts <= MAX_RATE_LIMIT_RETRIES && !isStopped) {
            val result = fetcher(track)
            if (result is LrclibLookupResult.RateLimited) {
                attempts++
                if (attempts > MAX_RATE_LIMIT_RETRIES || isStopped) {
                    return result
                }
                delay(result.retryAfterMs)
            } else {
                return result
            }
        }
        return LrclibLookupResult.NetworkError
    }

    companion object {
        const val UNIQUE_WORK_NAME = "lyrics_batch_sync"
        const val WORK_TAG = "lyrics_batch_sync_tag"

        const val KEY_CURRENT = "current"
        const val KEY_TOTAL = "total"
        const val KEY_FOUND = "found"
        const val KEY_FAILED = "failed"
        const val KEY_TRACK_TITLE = "track_title"

        const val DEFAULT_REQUEST_DELAY_MS = 450L
        private const val MAX_RATE_LIMIT_RETRIES = 2
        private const val MAX_WORK_RETRIES = 3

        @VisibleForTesting
        @Volatile
        internal var lrclibFetcherOverride: ((Track) -> LrclibLookupResult)? = null

        @VisibleForTesting
        @Volatile
        internal var interRequestDelayMsOverride: Long? = null

        fun enqueue(
            context: Context,
            wifiOnly: Boolean,
            replaceExisting: Boolean = false
        ) {
            val appCtx = context.applicationContext
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(
                    if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
                )
                .build()

            val request = OneTimeWorkRequestBuilder<LyricsSyncWorker>()
                .setConstraints(constraints)
                .addTag(WORK_TAG)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            val policy = if (replaceExisting) {
                ExistingWorkPolicy.REPLACE
            } else {
                ExistingWorkPolicy.KEEP
            }

            try {
                WorkManager.getInstance(appCtx).enqueueUniqueWork(UNIQUE_WORK_NAME, policy, request)
            } catch (_: IllegalStateException) {
                // WorkManager may not be initialized in bare unit tests without WorkManagerTestInitHelper
            }
        }

        fun cancel(context: Context) {
            try {
                WorkManager.getInstance(context.applicationContext).cancelUniqueWork(UNIQUE_WORK_NAME)
            } catch (_: IllegalStateException) {}
        }
    }
}
