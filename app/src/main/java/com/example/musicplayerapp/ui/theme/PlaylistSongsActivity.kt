package com.example.musicplayerapp.ui.theme

import MusicFiles
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.database.PlaylistRepository
import com.example.musicplayerapp.ui.theme.database.PlaylistSongsRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

class PlaylistSongsActivity : AppCompatActivity(), ServiceConnection {

    companion object {
        var currentPlaylistSongs = ArrayList<MusicFiles>()
        const val RESULT_PLAYLIST_MODIFIED = 100
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistSongsAdapter: PlaylistSongsAdapter
    private val playlistSongsRepository = PlaylistSongsRepository()
    private val playlistRepository = PlaylistRepository()
    private lateinit var playlistId: String
    private var playlistMusicFiles = ArrayList<MusicFiles>()
    private var isPlaylistModified = false
    private var musicService: MusicService? = null
    private var isPlaying = false

    // UI Elements
    private lateinit var backBtn: ImageView
    private lateinit var playAllBtn: FloatingActionButton
    private lateinit var playlistNameText: TextView
    private var miniPlayer: NowPlayingFragmentBottom? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_songs)

        playlistId = intent.getStringExtra("playlistId") ?: run {
            Toast.makeText(this, "Invalid playlist", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupClickListeners()
        loadPlaylistDetails()
        setupRecyclerView()
        loadSongs()

        miniPlayer = supportFragmentManager.findFragmentById(R.id.frag_bottom_player) as? NowPlayingFragmentBottom


    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.playlistSongrecyclerView)
        backBtn = findViewById(R.id.back_btn)
        playAllBtn = findViewById(R.id.play_all_btn)
        playlistNameText = findViewById(R.id.playlist_name)
    }

    private fun setupClickListeners() {
        backBtn.setOnClickListener {
            onBackPressed()
        }

        playAllBtn.setOnClickListener {
            if (playlistMusicFiles.isNotEmpty()) {
                if (isPlaying) {
                    musicService?.pause()
                    playAllBtn.setImageResource(R.drawable.ic_play)
                    isPlaying = false
                } else {
                    if (musicService != null &&
                        musicService?.mediaPlayer != null &&
                        PlayerActivity.listSongs == ArrayList(playlistMusicFiles)) {

                        musicService?.start()
                        playAllBtn.setImageResource(R.drawable.ic_pause)
                        isPlaying = true
                    } else {

                        startPlaylist()
                    }
                }
            } else {
                Toast.makeText(this, "No songs in playlist", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startPlaylist() {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("position", 0)
            putExtra("sender", "playlistSongs")
        }

        // Update the current playlist songs
        currentPlaylistSongs = ArrayList(playlistMusicFiles)

        // Set the source for the mini player
        NowPlayingFragmentBottom.CURRENT_SONG_SOURCE = "playlist"

        MainActivity.SHOW_MINI_PLAYER = true
        MainActivity.PATH_TO_FRAG = playlistMusicFiles[0].path
        MainActivity.ARTIST_TO_FRAG = playlistMusicFiles[0].artist
        MainActivity.SONG_NAME_TO_FRAG = playlistMusicFiles[0].title

        startActivity(intent)
        playAllBtn.setImageResource(R.drawable.ic_pause)
        isPlaying = true
    }

    private fun loadPlaylistDetails() {
        lifecycleScope.launch {
            try {
                val playlists = playlistRepository.getAllPlaylists()
                val playlist = playlists.find { it.id == playlistId }

                withContext(Dispatchers.Main) {
                    playlist?.let {
                        playlistNameText.text = it.name
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PlaylistSongsActivity, "Error loading playlist details", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = source.adapterPosition
                val toPosition = target.adapterPosition

                Collections.swap(playlistMusicFiles, fromPosition, toPosition)
                Collections.swap(currentPlaylistSongs, fromPosition, toPosition)

                playlistSongsAdapter.notifyItemMoved(fromPosition, toPosition)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun onPlaylistEmpty() {
        isPlaylistModified = true
        Toast.makeText(this, "Playlist is empty", Toast.LENGTH_SHORT).show()
        setResult(RESULT_PLAYLIST_MODIFIED)
        finish()
    }

    fun onSongRemoved() {
        isPlaylistModified = true
    }

    override fun onResume() {
        super.onResume()
        // Bind to the music service
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)

        // Update play button state
        updatePlayButtonState()
    }

    override fun onPause() {
        super.onPause()
        unbindService(this)
    }

    private fun updatePlayButtonState() {
        if (musicService?.isPlaying() == true &&
            PlayerActivity.listSongs == ArrayList(playlistMusicFiles)) {
            playAllBtn.setImageResource(R.drawable.ic_pause)
            isPlaying = true
        } else {
            playAllBtn.setImageResource(R.drawable.ic_play)
            isPlaying = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentPlaylistSongs.clear()
    }

    override fun finish() {
        if (isPlaylistModified) {
            setResult(RESULT_PLAYLIST_MODIFIED)
        }
        super.finish()
    }

    private fun loadSongs() {
        lifecycleScope.launch {
            try {
                val playlistSongs = playlistSongsRepository.getSongsOfPlaylist(playlistId)

                withContext(Dispatchers.Main) {
                    playlistMusicFiles.clear()
                    for (playlistSong in playlistSongs) {
                        MainActivity.musicFiles.find { it.path == playlistSong.songId }?.let {
                            playlistMusicFiles.add(it)
                        }
                    }

                    if (playlistMusicFiles.isEmpty()) {
                        onPlaylistEmpty()
                    } else {
                        currentPlaylistSongs = ArrayList(playlistMusicFiles)
                        playlistSongsAdapter = PlaylistSongsAdapter(
                            this@PlaylistSongsActivity,
                            playlistMusicFiles,
                            playlistId,
                            lifecycleScope
                        )
                        recyclerView.adapter = playlistSongsAdapter
                        setupRecyclerView() // Add this line to enable drag-drop
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PlaylistSongsActivity,
                        "Failed to load songs: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.getService()
        updatePlayButtonState()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }
}