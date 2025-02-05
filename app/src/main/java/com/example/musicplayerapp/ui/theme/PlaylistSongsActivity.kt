package com.example.musicplayerapp.ui.theme

import MusicFiles
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.database.PlaylistSongsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistSongsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistSongsAdapter: PlaylistSongsAdapter
    private val playlistSongsRepository = PlaylistSongsRepository()
    private lateinit var playlistId: String
    private var playlistMusicFiles = ArrayList<MusicFiles>()

    companion object {
        // Static variable to hold the current playlist songs
        var currentPlaylistSongs = ArrayList<MusicFiles>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_songs)

        playlistId = intent.getStringExtra("playlistId") ?: return
        setupRecyclerView()
        loadSongs()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.playlistSongrecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadSongs() {
        GlobalScope.launch {
            try {
                val playlistSongs = playlistSongsRepository.getSongsOfPlaylist(playlistId)

                // Convert playlist songs to MusicFiles
                playlistMusicFiles.clear()
                for (playlistSong in playlistSongs) {
                    // Find the corresponding MusicFile from MainActivity's musicFiles
                    MainActivity.musicFiles.find { it.path == playlistSong.songId }?.let {
                        playlistMusicFiles.add(it)
                    }
                }

                withContext(Dispatchers.Main) {
                    if (playlistMusicFiles.isNotEmpty()) {
                        // Update the static currentPlaylistSongs
                        currentPlaylistSongs = playlistMusicFiles

                        playlistSongsAdapter = PlaylistSongsAdapter(
                            this@PlaylistSongsActivity,
                            playlistMusicFiles,
                            playlistId,
                            lifecycleScope
                        )
                        recyclerView.adapter = playlistSongsAdapter
                    } else {
                        Toast.makeText(
                            this@PlaylistSongsActivity,
                            "No songs in this playlist",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PlaylistSongsActivity,
                        "Failed to load songs: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the playlist when coming back to this activity
        if (::playlistId.isInitialized) {
            loadSongs()
        }
    }
}