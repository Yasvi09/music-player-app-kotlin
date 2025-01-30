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
    private var mediaPlayer: MediaPlayer? = null
    private var musicFiles: ArrayList<MusicFiles> = ArrayList()
    private var uri: Uri? = null
    private var position: Int = -1
    private var actionPlaying: ActionPlaying? = null

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
        val actionName = intent?.getStringExtra("ActionName")

        if (myPosition != -1) {
            playMedia(myPosition)
        }

        actionName?.let {
            when (it) {
                "playPause" -> {
                    Toast.makeText(this, "PlayPause", Toast.LENGTH_SHORT).show()
                    actionPlaying?.let { action ->
                        Log.e("Inside", "Action")
                        action.playPauseBtnClicked()
                    }
                }
                "next" -> {
                    Toast.makeText(this, "Next", Toast.LENGTH_SHORT).show()
                    actionPlaying?.let { action ->
                        Log.e("Inside", "Action")
                        action.nextBtnClicked()
                    }
                }
                "previous" -> {
                    Toast.makeText(this, "Previous", Toast.LENGTH_SHORT).show()
                    actionPlaying?.let { action ->
                        Log.e("Inside", "Action")
                        action.prevBtnClicked()
                    }
                }

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
}
