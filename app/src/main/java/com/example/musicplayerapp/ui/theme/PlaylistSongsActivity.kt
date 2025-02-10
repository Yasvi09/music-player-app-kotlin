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

    companion object {
        var currentPlaylistSongs = ArrayList<MusicFiles>()
        const val RESULT_PLAYLIST_MODIFIED = 100
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistSongsAdapter: PlaylistSongsAdapter
    private val playlistSongsRepository = PlaylistSongsRepository()
    private lateinit var playlistId: String
    private var playlistMusicFiles = ArrayList<MusicFiles>()
    private var isPlaylistModified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_songs)

        playlistId = intent.getStringExtra("playlistId") ?: run {
            Toast.makeText(this, "Invalid playlist", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        loadSongs()
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

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.playlistSongrecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
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

                        playlistSongsAdapter = PlaylistSongsAdapter(this@PlaylistSongsActivity, playlistMusicFiles, playlistId, lifecycleScope)
                        recyclerView.adapter = playlistSongsAdapter
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PlaylistSongsActivity, "Failed to load songs: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
}