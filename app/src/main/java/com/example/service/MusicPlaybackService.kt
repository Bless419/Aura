package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.Track

class MusicPlaybackService : Service() {

    private val binder = LocalBinder()
    private var currentTrack: Track? = null
    private var isPlaying = false

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlaybackService = this@MusicPlaybackService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                isPlaying = true
                updateNotification()
            }
            ACTION_PAUSE -> {
                isPlaying = false
                updateNotification()
            }
            ACTION_NEXT -> {
                sendBroadcast(Intent(BROADCAST_TRACK_NEXT))
            }
            ACTION_PREV -> {
                sendBroadcast(Intent(BROADCAST_TRACK_PREV))
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    fun updateTrackState(track: Track?, playing: Boolean) {
        this.currentTrack = track
        this.isPlaying = playing
        if (track != null) {
            val notification = buildNotification()
            startForeground(NOTIFICATION_ID, notification)
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    private fun buildNotification(): Notification {
        val title = currentTrack?.title ?: "Aura Music"
        val artist = currentTrack?.artist ?: "High-Res Offline Audio"

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_pause, "Pause",
                getPendingIntent(ACTION_PAUSE)
            ).build()
        } else {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_play, "Play",
                getPendingIntent(ACTION_PLAY)
            ).build()
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("$artist • ${currentTrack?.format?.displayName ?: "FLAC 24-bit"}")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingOpenApp)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_media_previous, "Previous", getPendingIntent(ACTION_PREV))
            .addAction(playPauseAction)
            .addAction(android.R.drawable.ic_media_next, "Next", getPendingIntent(ACTION_NEXT))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aura Background Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows now playing controls during audio playback"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "aura_music_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.example.aura.PLAY"
        const val ACTION_PAUSE = "com.example.aura.PAUSE"
        const val ACTION_NEXT = "com.example.aura.NEXT"
        const val ACTION_PREV = "com.example.aura.PREV"
        const val ACTION_STOP = "com.example.aura.STOP"

        const val BROADCAST_TRACK_NEXT = "com.example.aura.BROADCAST_NEXT"
        const val BROADCAST_TRACK_PREV = "com.example.aura.BROADCAST_PREV"
    }
}
