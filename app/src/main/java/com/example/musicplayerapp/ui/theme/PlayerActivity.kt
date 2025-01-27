package com.example.musicplayerapp.ui.theme

import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.MainActivity.Companion.musicFiles

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException
import java.util.ArrayList

class PlayerActivity : AppCompatActivity() {

    lateinit var song_name: TextView
    lateinit var artist_name: TextView
    lateinit var duration_played: TextView
    lateinit var duration_total: TextView
    lateinit var cover_art: ImageView
    lateinit var nextBtn: ImageView
    lateinit var prevBtn: ImageView
    lateinit var backBtn: ImageView
    lateinit var shuffleBtn: ImageView
    lateinit var repeatBtn: ImageView
    lateinit var playPauseBtn: FloatingActionButton
    lateinit var seekBar: SeekBar
    var position = -1
    companion object {
        var listSongs = ArrayList<MusicFiles>()
        var uri: Uri? = null
        var mediaPlayer: MediaPlayer? = null
    }
    private val handler = Handler()
    private var playThread: Thread? = null
    private var prevThread: Thread? = null
    private var nextThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        initViews()
        getIntentMethod()
        song_name.text = listSongs[position].title
        artist_name.text = listSongs[position].artist

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer!!.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        runOnUiThread(object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                    seekBar.progress = mCurrentPosition
                    duration_played.text = formattedTime(mCurrentPosition)
                }
                handler.postDelayed(this, 1000)
            }
        })

    }

    override fun onResume() {
        playThreadBtn()
        nextThreadBtn()
        prevThreadBtn()
        super.onResume()
    }

    private fun prevThreadBtn() {
        prevThread = Thread {
            prevBtn.setOnClickListener {
                prevBtnClicked()
            }
        }
        prevThread?.start()
    }

    private fun prevBtnClicked() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            position = if ((position - 1) < 0) (listSongs.size - 1) else (position - 1)
            uri = Uri.parse(listSongs[position].path)
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            metaData(uri)
            song_name.text = listSongs[position].title
            artist_name.text = listSongs[position].artist
            seekBar.max = mediaPlayer!!.duration / 1000
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (mediaPlayer != null) {
                        val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                        seekBar.progress = mCurrentPosition
                        duration_played.text = formattedTime(mCurrentPosition)
                    }
                    handler.postDelayed(this, 1000)
                }
            })
            playPauseBtn.setImageResource(R.drawable.ic_pause)
            mediaPlayer!!.start()
        } else {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            position = if ((position - 1) < 0) (listSongs.size - 1) else (position - 1)
            uri = Uri.parse(listSongs[position].path)
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            metaData(uri)
            song_name.text = listSongs[position].title
            artist_name.text = listSongs[position].artist
            seekBar.max = mediaPlayer!!.duration / 1000
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (mediaPlayer != null) {
                        val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                        seekBar.progress = mCurrentPosition
                        duration_played.text = formattedTime(mCurrentPosition)
                    }
                    handler.postDelayed(this, 1000)
                }
            })

            playPauseBtn.setImageResource(R.drawable.ic_play)
        }
    }

    private fun nextThreadBtn() {
        nextThread = Thread {
            nextBtn.setOnClickListener {
                nextBtnClicked()
            }
        }
        nextThread?.start()
    }

    private fun nextBtnClicked() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            position = (position + 1) % listSongs.size
            uri = Uri.parse(listSongs[position].path)
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            metaData(uri)
            song_name.text = listSongs[position].title
            artist_name.text = listSongs[position].artist
            seekBar.max = mediaPlayer!!.duration / 1000
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (mediaPlayer != null) {
                        val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                        seekBar.progress = mCurrentPosition
                        duration_played.text = formattedTime(mCurrentPosition)
                    }
                    handler.postDelayed(this, 1000)
                }
            })

            playPauseBtn.setImageResource(R.drawable.ic_pause)
            mediaPlayer!!.start()
        } else {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            position = (position + 1) % listSongs.size
            uri = Uri.parse(listSongs[position].path)
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            metaData(uri)
            song_name.text = listSongs[position].title
            artist_name.text = listSongs[position].artist
            seekBar.max = mediaPlayer!!.duration / 1000
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (mediaPlayer != null) {
                        val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                        seekBar.progress = mCurrentPosition
                        duration_played.text = formattedTime(mCurrentPosition)
                    }
                    handler.postDelayed(this, 1000)
                }
            })

            playPauseBtn.setImageResource(R.drawable.ic_play)
        }
    }

    private fun playThreadBtn() {
        playThread = Thread {
            playPauseBtn.setOnClickListener {
                playPauseBtnClicked()
            }
        }
        playThread?.start()
    }

    private fun playPauseBtnClicked() {
        if (mediaPlayer!!.isPlaying) {
            playPauseBtn.setImageResource(R.drawable.ic_play)
            mediaPlayer!!.pause()
            seekBar.max = mediaPlayer!!.duration / 1000
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (mediaPlayer != null) {
                        val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                        seekBar.progress = mCurrentPosition
                        duration_played.text = formattedTime(mCurrentPosition)
                    }
                    handler.postDelayed(this, 1000)
                }
            })

        } else {
            playPauseBtn.setImageResource(R.drawable.ic_pause)
            mediaPlayer!!.start()
            seekBar.max = mediaPlayer!!.duration / 1000
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (mediaPlayer != null) {
                        val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                        seekBar.progress = mCurrentPosition
                        duration_played.text = formattedTime(mCurrentPosition)
                    }
                    handler.postDelayed(this, 1000)
                }
            })

        }
    }

    private fun formattedTime(mCurrentPosition: Int): String {
        var totalout = ""
        var totalNew = ""
        val seconds = (mCurrentPosition % 60).toString()
        val minutes = (mCurrentPosition / 60).toString()
        totalout = "$minutes:$seconds"
        totalNew = "$minutes:0$seconds"
        return if (seconds.length == 1) totalNew else totalout
    }

    private fun getIntentMethod() {
        position = intent.getIntExtra("position", -1)
        listSongs = musicFiles

        if (listSongs != null && position != -1) {

            playPauseBtn.setImageResource(R.drawable.ic_pause)
            val fullPath = listSongs[position].path
            Log.d("PlayerActivity", "Full Path: $fullPath")
            val file = File(fullPath)

            if (!file.exists()) {
                Toast.makeText(this, "File does not exist: $fullPath", Toast.LENGTH_SHORT).show()
                return
            }

            uri = Uri.fromFile(file)

            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(applicationContext, uri)
                mediaPlayer?.start()
            } catch (e: Exception) {
                Log.e("PlayerActivity", "Error initializing MediaPlayer: ${e.message}")
                Toast.makeText(this, "Error initializing player", Toast.LENGTH_SHORT).show()
            }

            mediaPlayer?.let {
                seekBar.max = it.duration / 1000
            }
            metaData(uri)
        }
    }

    private fun initViews() {
        song_name = findViewById(R.id.song_name)
        artist_name = findViewById(R.id.song_artist)
        duration_played = findViewById(R.id.durationplayed)
        duration_total = findViewById(R.id.durationTotal)
        cover_art = findViewById(R.id.cover_art)
        nextBtn = findViewById(R.id.id_next)
        prevBtn = findViewById(R.id.id_prev)
        backBtn = findViewById(R.id.back_btn)
        shuffleBtn = findViewById(R.id.id_shuffle)
        repeatBtn = findViewById(R.id.id_repeat)
        playPauseBtn = findViewById(R.id.play_pause)
        seekBar = findViewById(R.id.seekBar)
    }

    private fun metaData(uri: Uri?) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(applicationContext, uri)
            val durationTotal = (listSongs[position].duration!!.toInt() / 1000)
            duration_total.text = formattedTime(durationTotal)

            val art = retriever.embeddedPicture
            if (art != null) {
                Glide.with(this)
                    .asBitmap()
                    .load(art)
                    .into(cover_art)
            } else {
                Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.bewedoc)
                    .into(cover_art)
            }
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error retrieving metadata: ${e.message}")
        }
    }
}

