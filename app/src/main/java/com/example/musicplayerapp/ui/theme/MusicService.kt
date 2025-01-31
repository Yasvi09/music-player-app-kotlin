package com.example.musicplayerapp.ui.theme

import MusicFiles
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast

class MusicService : Service(), MediaPlayer.OnCompletionListener {

    private val mBinder: IBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    var musicFiles: ArrayList<MusicFiles> = ArrayList()
    private var uri: Uri? = null
    var position: Int = -1
    private var actionPlaying: ActionPlaying? = null
    companion object {
        const val MUSIC_LAST_PLAYED = "LAST_PLAYED"
        const val MUSIC_FILE = "STORED_MUSIC"
        const val ARTIST_NAME = "ARTIST NAME"
        const val SONG_NAME = "SONG NAME"
    }



    override fun onCreate() {
        super.onCreate()
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

    fun start() {
        mediaPlayer?.start()
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
        position = positionInner
        uri = Uri.parse(musicFiles[position].path)

        val editor = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit()
        editor.putString(MUSIC_FILE, uri.toString())
        editor.putString(ARTIST_NAME, musicFiles[position].artist)
        editor.putString(SONG_NAME, musicFiles[position].title)
        editor.apply()

        mediaPlayer = MediaPlayer.create(baseContext, uri)
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun onCompleted() {
        mediaPlayer?.setOnCompletionListener(this)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        actionPlaying?.nextBtnClicked()

        if (mediaPlayer != null) {
            createMediaPlayer(position)
            mediaPlayer?.start()
            onCompleted()
        }
    }

    fun setCallBack(actionPlaying: ActionPlaying) {
        this.actionPlaying = actionPlaying
    }

    fun playPauseBtnClicked() {
        actionPlaying?.playPauseBtnClicked()
    }

    fun previousBtnClicked() {
        actionPlaying?.prevBtnClicked()
    }

    fun nextBtnCicked() {
        actionPlaying?.nextBtnClicked()
    }


}
