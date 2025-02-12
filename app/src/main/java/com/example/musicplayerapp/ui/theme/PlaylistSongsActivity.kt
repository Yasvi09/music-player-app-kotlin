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

    class PlaylistSongsActivity : AppCompatActivity(), ServiceConnection, MusicServiceCallback {

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
    private lateinit var preferencesManager: PlaylistPreferencesManager

    // UI Elements
    private lateinit var backBtn: ImageView
    private lateinit var playAllBtn: FloatingActionButton
    private lateinit var playlistNameText: TextView
    private var miniPlayer: NowPlayingFragmentBottom? = null


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_playlist_songs)

            preferencesManager = PlaylistPreferencesManager(this)

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
        }

        private fun loadSongs() {
            lifecycleScope.launch {
                try {
                    val playlistSongs = playlistSongsRepository.getSongsOfPlaylist(playlistId)

                    withContext(Dispatchers.Main) {
                        playlistMusicFiles.clear()

                        // Get saved order from preferences
                        val savedOrder = preferencesManager.getSongOrder(playlistId)

                        // If we have a saved order, use it to arrange the songs
                        if (savedOrder.isNotEmpty()) {
                            // Create a map of path to MusicFiles for quick lookup
                            val songMap = mutableMapOf<String, MusicFiles>()
                            playlistSongs.forEach { playlistSong ->
                                MainActivity.musicFiles.find { it.path == playlistSong.songId }?.let {
                                    songMap[it.path!!] = it
                                }
                            }

                            // Add songs in the saved order
                            savedOrder.forEach { path ->
                                songMap[path]?.let { playlistMusicFiles.add(it) }
                            }

                            // Add any new songs that weren't in the saved order
                            playlistSongs.forEach { playlistSong ->
                                MainActivity.musicFiles.find { it.path == playlistSong.songId }?.let {
                                    if (!savedOrder.contains(it.path)) {
                                        playlistMusicFiles.add(it)
                                    }
                                }
                            }
                        } else {
                            // If no saved order exists, add songs in default order
                            playlistSongs.forEach { playlistSong ->
                                MainActivity.musicFiles.find { it.path == playlistSong.songId }?.let {
                                    playlistMusicFiles.add(it)
                                }
                            }

                            // Save initial order
                            preferencesManager.saveSongOrder(playlistId, playlistMusicFiles.mapNotNull { it.path })
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
                            setupRecyclerView()
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

        private fun setupRecyclerView() {
            recyclerView.layoutManager = LinearLayoutManager(this)

            val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    source: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val fromPosition = source.adapterPosition
                    val toPosition = target.adapterPosition

                    Collections.swap(playlistMusicFiles, fromPosition, toPosition)
                    Collections.swap(currentPlaylistSongs, fromPosition, toPosition)

                    if (musicService?.mediaPlayer != null &&
                        NowPlayingFragmentBottom.CURRENT_SONG_SOURCE == "playlist") {
                        PlayerActivity.listSongs = ArrayList(playlistMusicFiles)
                    }

                    playlistSongsAdapter.notifyItemMoved(fromPosition, toPosition)

                    // Save the new order to SharedPreferences
                    preferencesManager.saveSongOrder(playlistId, playlistMusicFiles.mapNotNull { it.path })

                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // Not handling swipe
                }

                override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                    super.onSelectedChanged(viewHolder, actionState)
                    if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                        viewHolder?.itemView?.alpha = 0.5f
                    }
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    viewHolder.itemView.alpha = 1.0f

                    currentPlaylistSongs = ArrayList(playlistMusicFiles)
                    if (NowPlayingFragmentBottom.CURRENT_SONG_SOURCE == "playlist") {
                        PlayerActivity.listSongs = ArrayList(playlistMusicFiles)
                    }

                    // Save the final order after drag and drop
                    preferencesManager.saveSongOrder(playlistId, playlistMusicFiles.mapNotNull { it.path })
                }
            })

            itemTouchHelper.attachToRecyclerView(recyclerView)
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
                // Update mini player
                miniPlayer?.updatePlayPauseButton()
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


    // Add this method to handle updates from mini player
    fun onMiniPlayerStateChanged() {
        updatePlayButtonState()
    }




    override fun finish() {
        if (isPlaylistModified) {
            setResult(RESULT_PLAYLIST_MODIFIED)
        }
        super.finish()
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val binder = service as MusicService.MyBinder
        musicService = binder.getService()
        musicService?.addCallback(this)

        // Update play button state when service connects
        updatePlayButtonState()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService?.removeCallback(this)
        musicService = null
    }

    override fun onDestroy() {
        super.onDestroy()
        musicService?.removeCallback(this)
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        runOnUiThread {
            updatePlayButtonState()
        }
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
}