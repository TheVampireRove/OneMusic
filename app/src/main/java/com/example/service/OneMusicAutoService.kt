package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.example.MainActivity
import com.example.R
import com.example.audio.AudioPlaybackManager
import com.example.audio.BluetoothAudioHelper
import com.example.data.AlbumArtCache
import com.example.model.Track
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Modern Media3 MediaLibraryService for Wireless Android Auto, wired head units, and lockscreen playback.
 * 
 * Key capabilities:
 * 1. Hosts MediaLibrarySession with unified ExoPlayer audio routing, C.USAGE_MEDIA focus, and C.WAKE_MODE_NETWORK.
 * 2. Full MediaBrowser tree hierarchy (All Tracks, Favorites, Albums, Playlists) for vehicle screens.
 * 3. Foreground service with ongoing MediaStyle notification to prevent process killing during wireless projection.
 * 4. Backward compatible with legacy MediaBrowserCompat / Android Auto vehicle controllers.
 */
@OptIn(UnstableApi::class)
class OneMusicAutoService : MediaLibraryService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var mediaLibrarySession: MediaLibrarySession? = null
    private val playbackManager = AudioPlaybackManager.getInstance()
    private var updatesJob: Job? = null

    companion object {
        const val ROOT_ID = "root_one_music"
        const val MEDIA_ID_TRACKS = "tracks_root"
        const val MEDIA_ID_FAVORITES = "favorites_root"
        const val MEDIA_ID_ALBUMS = "albums_root"
        const val MEDIA_ID_PLAYLISTS = "playlists_root"

        const val NOTIFICATION_CHANNEL_ID = "one_music_live_playback"
        const val NOTIFICATION_ID = 2099

        const val ACTION_PLAY = "com.example.action.PLAY"
        const val ACTION_PAUSE = "com.example.action.PAUSE"
        const val ACTION_PLAY_PAUSE = "com.example.action.PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.action.NEXT"
        const val ACTION_PREV = "com.example.action.PREV"
        const val ACTION_TOGGLE_FAVORITE = "com.example.action.TOGGLE_FAVORITE"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_SYNC = "com.example.action.SYNC"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        try {
            playbackManager.initContext(this)

            val player = playbackManager.audioEngine.exoPlayer
            if (player != null) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("OPEN_NOW_PLAYING", true)
                }
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                mediaLibrarySession = MediaLibrarySession.Builder(this, player, LibrarySessionCallback())
                    .setSessionActivity(pendingIntent)
                    .setId("OneMusicAutoSession")
                    .build()

                observePlaybackState()
            }
        } catch (_: Exception) {
        } finally {
            publishOngoingNotification()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> playbackManager.resume()
            ACTION_PAUSE -> playbackManager.pause()
            ACTION_PLAY_PAUSE -> playbackManager.togglePlayPause()
            ACTION_NEXT -> playbackManager.nextTrack()
            ACTION_PREV -> playbackManager.previousTrack()
            ACTION_TOGGLE_FAVORITE -> {
                playbackManager.toggleFavorite(playbackManager.currentTrack.value.id)
            }
            ACTION_STOP -> {
                playbackManager.pause()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
                return START_NOT_STICKY
            }
        }

        publishOngoingNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "OneMusic Live Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Live interactive music playback notifications with controls & Now Bar updates"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun observePlaybackState() {
        updatesJob = serviceScope.launch {
            launch {
                playbackManager.currentTrack.collect {
                    publishOngoingNotification()
                }
            }
            launch {
                playbackManager.isPlaying.collect {
                    publishOngoingNotification()
                }
            }
        }
    }

    private fun publishOngoingNotification() {
        val track = playbackManager.currentTrack.value
        val isPlaying = playbackManager.isPlaying.value
        val notif = buildLiveNotification(track, isPlaying)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notif)
            }
        } catch (_: Exception) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notif)
        }
    }

    private fun buildLiveNotification(track: Track, isPlaying: Boolean): Notification {
        val session = mediaLibrarySession
        val settings = playbackManager.repository.settings.value

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_NOW_PLAYING", true)
        }
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, OneMusicAutoService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, OneMusicAutoService::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, OneMusicAutoService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val favIntent = PendingIntent.getService(
            this,
            4,
            Intent(this, OneMusicAutoService::class.java).apply { action = ACTION_TOGGLE_FAVORITE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            5,
            Intent(this, OneMusicAutoService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif_music)
            .setContentTitle(track.title)
            .setContentText("${track.artist} • ${track.album}")
            .setContentIntent(contentIntent)
            .setDeleteIntent(stopIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(track.primaryColor.toArgb())
            .setColorized(true)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)

        if (settings.liveNotificationSoundBadge) {
            val bluetoothStatus = if (BluetoothAudioHelper.isBluetoothConnected.value) {
                " • " + BluetoothAudioHelper.connectedDeviceName.value
            } else ""
            builder.setSubText("Live • High-Fidelity 320kbps$bluetoothStatus")
        }

        val cachedArt = track.albumArtKey?.let { AlbumArtCache.get(it) }
        if (cachedArt != null) {
            builder.setLargeIcon(cachedArt)
        }

        // Actions
        builder.addAction(R.drawable.ic_notif_prev, "Previous", prevIntent)

        val playPauseIcon = if (isPlaying) R.drawable.ic_notif_pause else R.drawable.ic_notif_play
        val playPauseTitle = if (isPlaying) "Pause" else "Play"
        builder.addAction(playPauseIcon, playPauseTitle, playPauseIntent)

        builder.addAction(R.drawable.ic_notif_next, "Next", nextIntent)

        if (settings.liveNotificationShowFavorite) {
            val favIcon = if (track.isFavorite) R.drawable.ic_notif_favorite else R.drawable.ic_notif_favorite_border
            val favTitle = if (track.isFavorite) "Favorited" else "Favorite"
            builder.addAction(favIcon, favTitle, favIntent)
        }

        builder.addAction(R.drawable.ic_notif_close, "Close", stopIntent)

        if (session != null) {
            val mediaStyle = MediaStyleNotificationHelper.MediaStyle(session)
                .setShowActionsInCompactView(0, 1, 2)
            builder.setStyle(mediaStyle)
        }

        return builder.build()
    }

    private inner class LibrarySessionCallback : MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootItem = MediaItem.Builder()
                .setMediaId(ROOT_ID)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("OneMusic")
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val items = mutableListOf<MediaItem>()
            val tracks = playbackManager.repository.tracks.value
            val playlists = playbackManager.repository.playlists.value

            when (parentId) {
                ROOT_ID -> {
                    items.add(createCategoryItem(MEDIA_ID_TRACKS, "All Tracks", "${tracks.size} songs"))
                    items.add(createCategoryItem(MEDIA_ID_FAVORITES, "Favorites", "${tracks.count { it.isFavorite }} saved tracks"))
                    items.add(createCategoryItem(MEDIA_ID_ALBUMS, "Albums", "${tracks.map { it.album }.distinct().size} albums"))
                    items.add(createCategoryItem(MEDIA_ID_PLAYLISTS, "Playlists", "${playlists.size} playlists"))
                }

                MEDIA_ID_TRACKS -> {
                    tracks.forEach { items.add(playbackManager.trackToMediaItem(it)) }
                }

                MEDIA_ID_FAVORITES -> {
                    tracks.filter { it.isFavorite }.forEach { items.add(playbackManager.trackToMediaItem(it)) }
                }

                MEDIA_ID_ALBUMS -> {
                    val albums = tracks.groupBy { it.album }
                    albums.forEach { (albumName, albumTracks) ->
                        val artist = albumTracks.firstOrNull()?.artist ?: "Artist"
                        items.add(createCategoryItem("album_$albumName", albumName, "$artist • ${albumTracks.size} tracks"))
                    }
                }

                MEDIA_ID_PLAYLISTS -> {
                    playlists.forEach { playlist ->
                        items.add(
                            createCategoryItem(
                                "playlist_${playlist.id}",
                                playlist.title,
                                "${playlist.trackIds.size} songs • ${playlist.description}"
                            )
                        )
                    }
                }

                else -> {
                    if (parentId.startsWith("album_")) {
                        val albumName = parentId.removePrefix("album_")
                        tracks.filter { it.album == albumName }.forEach {
                            items.add(playbackManager.trackToMediaItem(it))
                        }
                    } else if (parentId.startsWith("playlist_")) {
                        val playlistId = parentId.removePrefix("playlist_")
                        val playlist = playlists.find { it.id == playlistId }
                        if (playlist != null) {
                            tracks.filter { playlist.trackIds.contains(it.id) }.forEach {
                                items.add(playbackManager.trackToMediaItem(it))
                            }
                        }
                    }
                }
            }

            return Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.copyOf(items), params))
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val track = playbackManager.repository.tracks.value.find { it.id == mediaId }
            return if (track != null) {
                Futures.immediateFuture(LibraryResult.ofItem(playbackManager.trackToMediaItem(track), null))
            } else {
                Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
            }
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updated = mediaItems.map { item ->
                val track = playbackManager.repository.tracks.value.find { it.id == item.mediaId }
                if (track != null) {
                    playbackManager.trackToMediaItem(track)
                } else {
                    item
                }
            }.toMutableList()
            return Futures.immediateFuture(updated)
        }

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            val updated = mediaItems.map { item ->
                val track = playbackManager.repository.tracks.value.find { it.id == item.mediaId }
                if (track != null) {
                    playbackManager.trackToMediaItem(track)
                } else {
                    item
                }
            }.toMutableList()

            if (updated.isNotEmpty() && startIndex in updated.indices) {
                val selectedId = updated[startIndex].mediaId
                playbackManager.playTrackById(selectedId)
            }

            return Futures.immediateFuture(
                MediaSession.MediaItemsWithStartPosition(updated, startIndex, startPositionMs)
            )
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            playbackManager.initContext(this@OneMusicAutoService)
            val current = playbackManager.currentTrack.value
            val queue = playbackManager.playbackQueue.value.ifEmpty { listOf(current) }
            val startIndex = queue.indexOfFirst { it.id == current.id }.let { if (it >= 0) it else 0 }
            val items = queue.map { playbackManager.trackToMediaItem(it) }
            val pos = playbackManager.currentPositionMs.value
            return Futures.immediateFuture(
                MediaSession.MediaItemsWithStartPosition(items, startIndex, pos)
            )
        }

        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int
        ): Int {
            when (playerCommand) {
                Player.COMMAND_SEEK_TO_NEXT,
                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> {
                    playbackManager.nextTrack()
                    return SessionResult.RESULT_SUCCESS
                }
                Player.COMMAND_SEEK_TO_PREVIOUS,
                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> {
                    playbackManager.previousTrack()
                    return SessionResult.RESULT_SUCCESS
                }
            }
            return SessionResult.RESULT_SUCCESS
        }
    }

    private fun createCategoryItem(id: String, title: String, subtitle: String): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setIsBrowsable(true)
            .setIsPlayable(false)
            .build()
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(metadata)
            .build()
    }

    override fun onDestroy() {
        playbackManager.persistPlaybackState()
        updatesJob?.cancel()
        serviceScope.cancel()
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }
}
