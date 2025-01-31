package com.example.musicplayerapp.ui.theme

import MusicFiles
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.ApplicationClass.Companion.ACTION_NEXT
import com.example.musicplayerapp.ui.theme.ApplicationClass.Companion.ACTION_PLAY
import com.example.musicplayerapp.ui.theme.ApplicationClass.Companion.ACTION_PREVIOUS
import com.example.musicplayerapp.ui.theme.ApplicationClass.Companion.CHANNEL_ID_2
import com.example.musicplayerapp.ui.theme.MainActivity.Companion.ARTIST_NAME
import com.example.musicplayerapp.ui.theme.MainActivity.Companion.MUSIC_FILE
import com.example.musicplayerapp.ui.theme.MainActivity.Companion.MUSIC_LAST_PLAYED
import com.example.musicplayerapp.ui.theme.MainActivity.Companion.SONG_NAME
import com.example.musicplayerapp.ui.theme.MainActivity.Companion.musicFiles
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class PlayerActivity : AppCompatActivity() ,  ActionPlaying, ServiceConnection {

    var song_name: TextView? = null
    var artist_name: TextView? = null
    var duration_played: TextView? = null
    var duration_total: TextView? = null
    private lateinit var cover_art: ImageView
    private lateinit var nextBtn: ImageView
    private lateinit var prevBtn: ImageView
    private lateinit var backBtn: ImageView
    private lateinit var shuffleBtn: ImageView
    private lateinit var repeatBtn: ImageView
    private lateinit var playPauseBtn: FloatingActionButton
    private lateinit var seekBar: SeekBar

    private var position: Int = -1
    companion object {
        var listSongs: ArrayList<MusicFiles> = ArrayList()
        var uri: Uri? = null
    }

    private val handler = Handler()
    private var playThread: Thread? = null
    private var prevThread: Thread? = null
    private var nextThread: Thread? = null

    private var musicService: MusicService? = null
    private lateinit var mediaSessionCompat: MediaSessionCompat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        mediaSessionCompat = MediaSessionCompat(applicationContext, "My Audio")
        mediaSessionCompat.isActive = true
        initViews()
        getIntentMethod()


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (musicService != null && fromUser) {
                    musicService!!.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })


        runOnUiThread(object : Runnable {
            override fun run() {
                if (musicService != null) {
                    val mCurrentPosition = musicService!!.getCurrentPosition() / 1000
                    seekBar.progress = mCurrentPosition
                    duration_played!!.text = formattedTime(mCurrentPosition)
                }
                handler.postDelayed(this, 1000)
            }
        })

    }

    override fun onResume() {
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        playThreadBtn()
        nextThreadBtn()
        prevThreadBtn()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        unbindService(this)
    }

    private fun prevThreadBtn() {
        prevThread = Thread {
            run {
                prevBtn.setOnClickListener {
                    prevBtnClicked()
                }
            }
        }
        prevThread!!.start()
    }

    override fun prevBtnClicked() {
        if (musicService!!.isPlaying()) {
            musicService!!.stop()
            musicService!!.release()
            position = if (position - 1 < 0) (listSongs.size - 1) else position - 1
            uri = Uri.parse(listSongs[position].path)
            musicService!!.createMediaPlayer(position)
            uri?.let { metaData(it) }
            song_name!!.text = listSongs[position].title
            artist_name!!.text = listSongs[position].artist
            seekBar.max = musicService!!.getDuration() / 1000
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (musicService != null) {
                        val mCurrentPosition = musicService!!.getCurrentPosition() / 1000
                        seekBar.progress = mCurrentPosition
                    }
                    handler.postDelayed(this, 1000)
                }
            })
            showNotification(R.drawable.ic_pause)
            musicService!!.onCompleted()
            playPauseBtn.setImageResource(R.drawable.ic_pause)
            musicService!!.start()
        } else {
            musicService!!.stop()
            musicService!!.release()
            position = if (position - 1 < 0) (listSongs.size - 1) else position - 1
            uri = Uri.parse(listSongs[position].path)
            musicService!!.createMediaPlayer(position)
            uri?.let { metaData(it) }
            song_name!!.text = listSongs[position].title
            artist_name!!.text = listSongs[position].artist
            seekBar.max = musicService!!.getDuration() / 1000
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (musicService != null) {
                        val mCurrentPosition = musicService!!.getCurrentPosition() / 1000
                        seekBar.progress = mCurrentPosition
                    }
                    handler.postDelayed(this, 1000)
                }
            })
            showNotification(R.drawable.ic_play)
            musicService!!.onCompleted()
            playPauseBtn.setImageResource(R.drawable.ic_play)
        }
    }

    private fun nextThreadBtn() {
        nextThread = Thread {
            nextBtn.setOnClickListener {
                nextBtnClicked()
            }
        }
        nextThread!!.start()
    }


    override fun nextBtnClicked() {
        if (musicService!!.isPlaying()) {
            musicService!!.stop()
            musicService!!.release()
            position = (position + 1) % listSongs.size
            uri = Uri.parse(listSongs[position].path)
            musicService!!.createMediaPlayer(position)
            uri?.let { metaData(it) }
            song_name!!.text = listSongs[position].title
            artist_name!!.text = listSongs[position].artist
            seekBar.max = musicService!!.getDuration() / 1000
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (musicService != null) {
                        val mCurrentPosition = musicService!!.getCurrentPosition() / 1000
                        seekBar.progress = mCurrentPosition
                    }
                    handler.postDelayed(this, 1000)
                }
            })

            showNotification(R.drawable.ic_pause)
            musicService!!.onCompleted()
            playPauseBtn.setImageResource(R.drawable.ic_pause)
            musicService!!.start()
        } else {
            musicService!!.stop()
            musicService!!.release()
            position = (position + 1) % listSongs.size
            uri = Uri.parse(listSongs[position].path)
            musicService!!.createMediaPlayer(position)
            uri?.let { metaData(it) }
            song_name!!.text = listSongs[position].title
            artist_name!!.text = listSongs[position].artist
            seekBar.max = musicService!!.getDuration() / 1000
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (musicService != null) {
                        val mCurrentPosition = musicService!!.getCurrentPosition() / 1000
                        seekBar.progress = mCurrentPosition
                    }
                    handler.postDelayed(this, 1000)
                }
            })
            showNotification(R.drawable.ic_play)
            musicService!!.onCompleted()
            playPauseBtn.setImageResource(R.drawable.ic_play)
        }
    }

    private fun playThreadBtn() {
        playThread = Thread {
            playPauseBtn.setOnClickListener {
                playPauseBtnClicked()
            }
        }
        playThread!!.start()
    }

    override fun playPauseBtnClicked() {
        if (musicService!!.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.ic_play)
            showNotification(R.drawable.ic_play)
            musicService!!.pause()
            seekBar.max = musicService!!.getDuration() / 1000
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (musicService != null) {
                        val mCurrentPosition = musicService!!.getCurrentPosition() / 1000
                        seekBar.progress = mCurrentPosition
                    }
                    handler.postDelayed(this, 1000)
                }
            })

        } else {
            showNotification(R.drawable.ic_pause)
            playPauseBtn.setImageResource(R.drawable.ic_pause)
            musicService!!.start()
            seekBar.max = musicService!!.getDuration() / 1000
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (musicService != null) {
                        val mCurrentPosition = musicService!!.getCurrentPosition() / 1000
                        seekBar.progress = mCurrentPosition
                    }
                    handler.postDelayed(this, 1000)
                }
            })

        }
    }

    private fun formattedTime(mCurrentPosition: Int): String {
        var totalOut = ""
        var totalNew = ""
        val seconds = (mCurrentPosition % 60).toString()
        val minutes = (mCurrentPosition / 60).toString()
        totalOut = "$minutes:$seconds"
        totalNew = "$minutes:0$seconds"
        return if (seconds.length == 1) {
            totalNew
        } else {
            totalOut
        }
    }

    private fun getIntentMethod() {
        position = intent.getIntExtra("position", -1)
        val currentPosition = intent.getIntExtra("current_position", 0)
        val sender = intent.getStringExtra("sender")
        listSongs = if (sender != null && sender == "albumDetails") {
            intent.getParcelableArrayListExtra<MusicFiles>("albumFiles") ?: arrayListOf()
        } else {
            musicFiles
        }

        if (listSongs != null && position != -1) {

            MainActivity.SHOW_MINI_PLAYER = true
            MainActivity.PATH_TO_FRAG = listSongs[position].path
            MainActivity.ARTIST_TO_FRAG = listSongs[position].artist
            MainActivity.SONG_NAME_TO_FRAG = listSongs[position].title

            playPauseBtn.setImageResource(R.drawable.ic_pause)
            uri = Uri.parse(listSongs[position].path)

            val intent = Intent(this, MusicService::class.java)
            intent.putExtra("servicePosition", position)
            intent.putExtra("seekTo", currentPosition) // Pass the position to seek to
            startService(intent)

            val editor = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit()
            editor.putString(MUSIC_FILE, uri.toString())
            editor.putString(ARTIST_NAME, listSongs[position].artist)
            editor.putString(SONG_NAME, listSongs[position].title)
            editor.apply()

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

        backBtn.setOnClickListener {
            super.onBackPressed()
        }

    }

    private fun metaData(uri: Uri) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(applicationContext, uri)
            val durationTotal = listSongs[position].duration!!.toInt() / 1000
            duration_total!!.text = formattedTime(durationTotal)

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

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val myBinder = service as MusicService.MyBinder
        musicService = myBinder.getService()
        musicService!!.setCallBack(this)
       //  Toast.makeText(this, "Connected $musicService", Toast.LENGTH_SHORT).show()

        musicService?.let {
            seekBar.max = it.getDuration() / 1000
        }

        uri?.let { metaData(it) }
        song_name!!.text = listSongs[position].title
        artist_name!!.text = listSongs[position].artist
        musicService!!.onCompleted()
    }

    override fun onServiceDisconnected(name: ComponentName) {
        musicService = null
    }

    fun showNotification(playPauseBtn: Int) {
        val intent = Intent(this, PlayerActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val prevIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_PREVIOUS)
        val prevPending = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val pauseIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_PLAY)
        val pausePending = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_NEXT)
        val nextPending = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val picture: ByteArray? = listSongs[position].path?.let { getAlbumArt(it) }
        val thumb: Bitmap = if (picture != null) {
            BitmapFactory.decodeByteArray(picture, 0, picture.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.bewedoc)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_2)
            .setSmallIcon(playPauseBtn)
            .setLargeIcon(thumb)
            .setContentTitle(listSongs[position].title)
            .setContentText(listSongs[position].artist)
            .addAction(R.drawable.ic_skip_previous, "Previous", prevPending)
            .addAction(playPauseBtn, "Pause", pausePending)
            .addAction(R.drawable.ic_skip_next, "Next", nextPending)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSessionCompat.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notification)
        Log.d("NotificationDebug", "Attempting to show notification")
        try {
            notificationManager.notify(0, notification)
            Log.d("NotificationDebug", "Notification sent to system")
        } catch (e: Exception) {
            Log.e("NotificationDebug", "Error showing notification: ${e.message}")
        }
    }

    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        return retriever.embeddedPicture
    }

}
