package com.example.musicplayerapp.ui.theme

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.service.media.MediaBrowserService
import androidx.core.app.NotificationCompat
import com.example.musicplayerapp.R

class MusicService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private var position = -1
    private val notificationManager: NotificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "music_channel",
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        position = intent.getIntExtra("position", -1)
        val songPath = intent.getStringExtra("songPath")
        if (songPath != null) {
            startMusicPlayback(songPath)
        }
        return START_STICKY
    }

    private fun startMusicPlayback(songPath: String) {
        val uri = Uri.parse(songPath)
        mediaPlayer = MediaPlayer.create(this, uri)
        mediaPlayer.setOnCompletionListener {
            stopForeground(true)
            stopSelf()
        }
        mediaPlayer.start()

        // Create a notification
        val notification = buildNotification(songPath)
        startForeground(1, notification)
    }

    private fun buildNotification(songPath: String): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, "music_channel")
            .setContentTitle("Now Playing")
            .setContentText(songPath) // Set song title dynamically here
            .setSmallIcon(R.drawable.ic_menu)
            .setOngoing(true)

        return notificationBuilder.build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
        super.onDestroy()
    }
}
