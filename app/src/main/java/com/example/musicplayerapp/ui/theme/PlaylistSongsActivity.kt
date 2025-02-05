package com.example.musicplayerapp.ui.theme

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_songs)

        // Get playlist ID from Intent
        playlistId = intent.getStringExtra("playlistId") ?: return

        recyclerView = findViewById(R.id.playlistSongrecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadSongs()
    }

    private fun loadSongs() {
        GlobalScope.launch {
            try {
                val songs = playlistSongsRepository.getSongsOfPlaylist(playlistId)
                withContext(Dispatchers.Main) {
                    if (songs.isNotEmpty()) {
                        playlistSongsAdapter = PlaylistSongsAdapter(songs)
                        recyclerView.adapter = playlistSongsAdapter
                    } else {
                        Toast.makeText(this@PlaylistSongsActivity, "No songs in this playlist", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PlaylistSongsActivity, "Failed to load songs: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
