package com.example.musicplayerapp.ui.theme

import MusicFiles
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.musicplayerapp.R

interface MusicServiceCallback {
    fun onPlaybackStateChanged(isPlaying: Boolean)
}
class MusicService : Service(), MediaPlayer.OnCompletionListener {

    private val mBinder: IBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    var musicFiles: ArrayList<MusicFiles> = ArrayList()
    private var uri: Uri? = null
    var position: Int = -1
    private var actionPlaying: ActionPlaying? = null
    private var serviceCallback: MusicServiceCallback? = null
    private var callbacks = mutableListOf<MusicServiceCallback>()

    fun addCallback(callback: MusicServiceCallback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback)
        }
    }

    fun removeCallback(callback: MusicServiceCallback) {
        callbacks.remove(callback)
    }

    private fun notifyPlaybackStateChanged(isPlaying: Boolean) {
        callbacks.forEach { it.onPlaybackStateChanged(isPlaying) }
    }

    fun start() {
        try {
            isHandlingStateChange = true
            mediaPlayer?.start()
            notifyPlaybackStateChanged(true)
            showNotification(R.drawable.ic_pause)
        } finally {
            isHandlingStateChange = false
        }
    }

    fun pause() {
        try {
            isHandlingStateChange = true
            mediaPlayer?.pause()
            notifyPlaybackStateChanged(false)
            showNotification(R.drawable.ic_play)
        } finally {
            isHandlingStateChange = false
        }
    }

    fun playPauseBtnClicked() {
        println("isHandlingStateChange: $isHandlingStateChange")
        if (isHandlingStateChange) return
        try {
            isHandlingStateChange = true
            println("mediaPlayer?.isPlaying: ${mediaPlayer?.isPlaying}")
            if (mediaPlayer?.isPlaying == true) {
                pause()
            } else {
                start()
            }
        } finally {
            isHandlingStateChange = false
        }
    }

    fun setCallback(callback: MusicServiceCallback) {
        serviceCallback = callback
    }
    companion object {
        const val MUSIC_LAST_PLAYED = "LAST_PLAYED"
        const val MUSIC_FILE = "STORED_MUSIC"
        const val ARTIST_NAME = "ARTIST NAME"
        const val SONG_NAME = "SONG NAME"
    }

    private var isHandlingStateChange = false
    private lateinit var mediaSessionCompat: MediaSessionCompat

    override fun onCreate() {
        super.onCreate()
        mediaSessionCompat = MediaSessionCompat(baseContext, "My Audio").apply {
            setActive(true)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.e("Bind", "Method")
        return mBinder
    }

    inner class MyBinder : Binder() {
        fun getService(): MusicService {
            return this@MusicService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val myPosition = intent?.getIntExtra("servicePosition", -1) ?: -1
        val seekTo = intent?.getIntExtra("seekTo", 0) ?: 0
        val actionName = intent?.getStringExtra("ActionName")

        if (myPosition != -1) {
            playMedia(myPosition)
            // Seek to the saved position after creating the media player
            mediaPlayer?.let {
                if (seekTo > 0) {
                    it.seekTo(seekTo)
                }
            }
        }

        actionName?.run {
            when (this) {
                "playPause" -> playPauseBtnClicked()
                "next" -> nextBtnCicked()
                "previous" -> previousBtnClicked()
                else -> {}
            }
        }

        return START_STICKY
    }

    private fun playMedia(startPosition: Int) {
        musicFiles = PlayerActivity.listSongs!!
        position = startPosition

        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            if (musicFiles.isNotEmpty()) {
                createMediaPlayer(position)
                mediaPlayer?.start()
            }
        } else {
            createMediaPlayer(position)
            mediaPlayer?.start()
        }
    }

    private fun updatePlaybackState(playing: Boolean) {
        // Update UI elements
        actionPlaying?.playPauseBtnClicked()

        // Update notification only if media player exists and is in the expected state
        if (mediaPlayer != null && playing == mediaPlayer!!.isPlaying) {
            val icon = if (playing) R.drawable.ic_pause else R.drawable.ic_play
            showNotification(icon)
        }
    }

    private fun showNotification(playPauseBtn: Int) {
        try {
            // Get album art only once per song
            val picture: ByteArray? = getAlbumArt(musicFiles[position].path)
            val thumb = if (picture != null) {
                BitmapFactory.decodeByteArray(picture, 0, picture.size)
            } else {
                BitmapFactory.decodeResource(resources, R.drawable.bewedoc)
            }

            // Create notification
            val notification = createNotification(playPauseBtn, thumb)

            // Show notification
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, notification)
        } catch (e: Exception) {
            Log.e("MusicService", "Error showing notification", e)
        }
    }

    private fun createNotification(playPauseBtn: Int, albumArt: Bitmap): Notification {
        // Create intent for clicking the notification
        val contentIntent = Intent(this, PlayerActivity::class.java).let { intent ->
            PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Create intents for media controls
        val prevIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = ApplicationClass.ACTION_PREVIOUS
        }
        val prevPending = PendingIntent.getBroadcast(
            this, 0, prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = ApplicationClass.ACTION_PLAY
        }
        val playPending = PendingIntent.getBroadcast(
            this, 0, playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = ApplicationClass.ACTION_NEXT
        }
        val nextPending = PendingIntent.getBroadcast(
            this, 0, nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create the notification
        return NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID_2)
            .setSmallIcon(playPauseBtn)
            .setLargeIcon(albumArt)
            .setContentTitle(musicFiles[position].title)
            .setContentText(musicFiles[position].artist)
            .addAction(R.drawable.ic_skip_previous, "Previous", prevPending)
            .addAction(playPauseBtn, "Play/Pause", playPending)
            .addAction(R.drawable.ic_skip_next, "Next", nextPending)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSessionCompat.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(mediaPlayer?.isPlaying == true)
            .setContentIntent(contentIntent)
            .build()
    }


    private fun getAlbumArt(uri: String?): ByteArray? {
        if (uri == null) return null
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(uri)
            retriever.embeddedPicture
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e("MusicService", "Error releasing MediaMetadataRetriever", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        mediaSessionCompat.release()
    }

    private fun notifyStateChanged() {
        actionPlaying?.playPauseBtnClicked()
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun stop() {
        mediaPlayer?.stop()
    }

    fun release() {
        mediaPlayer?.release()
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun createMediaPlayer(positionInner: Int) {
        try {
            position = positionInner
            uri = Uri.parse(musicFiles[position].path)

            // Save last played info
            val editor = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit()
            editor.putString(MUSIC_FILE, uri.toString())
            editor.putString(ARTIST_NAME, musicFiles[position].artist)
            editor.putString(SONG_NAME, musicFiles[position].title)
            editor.apply()

            // Create new media player
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(baseContext, uri)
            mediaPlayer?.setOnCompletionListener(this)
        } catch (e: Exception) {
            Log.e("MusicService", "Error creating media player", e)
        }
    }
    fun onCompleted() {
        mediaPlayer?.setOnCompletionListener(this)
    }

    interface OnSongChangedListener {
        fun onSongChanged()
    }

    private val songChangedListeners = mutableListOf<OnSongChangedListener>()

    fun addSongChangedListener(listener: OnSongChangedListener) {
        if (!songChangedListeners.contains(listener)) {
            songChangedListeners.add(listener)
        }
    }

    fun removeSongChangedListener(listener: OnSongChangedListener) {
        songChangedListeners.remove(listener)
    }

    private fun notifySongChanged() {
        songChangedListeners.forEach { it.onSongChanged() }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        try {
            if (position < musicFiles.size - 1) {
                position++ // Move to next song
            } else {
                position = 0 // Loop back to first song
            }

            // Update the path, artist, and song name in MainActivity's companion object
            MainActivity.PATH_TO_FRAG = musicFiles[position].path
            MainActivity.ARTIST_TO_FRAG = musicFiles[position].artist
            MainActivity.SONG_NAME_TO_FRAG = musicFiles[position].title
            MainActivity.SHOW_MINI_PLAYER = true

            // Save to SharedPreferences
            val editor = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit()
            editor.putString(MUSIC_FILE, musicFiles[position].path)
            editor.putString(ARTIST_NAME, musicFiles[position].artist)
            editor.putString(SONG_NAME, musicFiles[position].title)
            editor.apply()

            mediaPlayer?.reset() // Reset current media player
            createMediaPlayer(position) // Create new media player instance
            mediaPlayer?.start() // Start playing next song
            notifyPlaybackStateChanged(true) // Notify UI to update playback state

            // Notify action playing interface for UI updates
            actionPlaying?.nextBtnClicked()

            // Notify all listeners about song change
            notifySongChanged()
        } catch (e: Exception) {
            Log.e("MusicService", "Error in onCompletion", e)
        }
    }


    fun setCallBack(actionPlaying: ActionPlaying) {
        this.actionPlaying = actionPlaying
    }


    fun previousBtnClicked() {
        actionPlaying?.prevBtnClicked()
    }

    fun nextBtnCicked() {
        actionPlaying?.nextBtnClicked()
    }


}
