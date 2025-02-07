package com.example.musicplayerapp.ui.theme

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.database.MySQLDatabase
import com.example.musicplayerapp.ui.theme.database.Playlist
import com.example.musicplayerapp.ui.theme.database.PlaylistRepository
import com.example.musicplayerapp.ui.theme.database.PlaylistSongsRepository
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistFragment : Fragment() {
    private lateinit var playlistRecyclerView: RecyclerView
    private lateinit var addPlaylistBtn: FloatingActionButton
    private lateinit var playlistRepository: PlaylistRepository
    private lateinit var playlistSongsRepository: PlaylistSongsRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)
        playlistRecyclerView = view.findViewById(R.id.recyclerView)
        addPlaylistBtn = view.findViewById(R.id.add_playlist)

        playlistRepository = PlaylistRepository()
        playlistSongsRepository = PlaylistSongsRepository()
        playlistRecyclerView.layoutManager = LinearLayoutManager(context)

        addPlaylistBtn.setOnClickListener {
            showCreatePlaylistDialog()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadPlaylists()
    }

        private fun createPlaylistAdapter(allPlaylists: List<Playlist>) {
            val adapter = PlaylistAdapter(allPlaylists) {
                // Playlist delete callback
                loadPlaylists()
            }
            adapter.setOnPlaylistClickListener { playlist ->
                val intent = Intent(context, PlaylistSongsActivity::class.java)
                intent.putExtra("playlistId", playlist.id)
                startActivityForResult(intent, PlaylistSongsActivity.RESULT_PLAYLIST_MODIFIED)
            }
            playlistRecyclerView.adapter = adapter
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == PlaylistSongsActivity.RESULT_PLAYLIST_MODIFIED) {
            loadPlaylists()
        }
    }

    private fun showCreatePlaylistDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val etPlaylistName = bottomSheetView.findViewById<EditText>(R.id.et_playlist_name)
        val btnCreate = bottomSheetView.findViewById<Button>(R.id.btn_create)
        val btnCancel = bottomSheetView.findViewById<Button>(R.id.btn_cancel)

        btnCreate.setOnClickListener {
            val name = etPlaylistName.text.toString()
            if (name.isNotBlank()) {
                lifecycleScope.launch {
                    try {
                        val newPlaylist = playlistRepository.createPlaylist(name)
                        if (newPlaylist != null) {
                            Log.d("PlaylistFragment", "Playlist created: $newPlaylist")
                            loadPlaylists()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Playlist created successfully", Toast.LENGTH_SHORT).show()
                                bottomSheetDialog.dismiss()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Failed to create playlist", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Failed to create playlist: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun loadPlaylists() {
        lifecycleScope.launch {
            try {
                val allPlaylists = playlistRepository.getAllPlaylists()
                withContext(Dispatchers.Main) {
                    val noPlaylistTextView = view?.findViewById<TextView>(R.id.no_playlist_text)
                    if (allPlaylists.isEmpty()) {
                        noPlaylistTextView?.visibility = View.VISIBLE
                    } else {
                        noPlaylistTextView?.visibility = View.GONE
                    }

                    val adapter = PlaylistAdapter(allPlaylists) {
                        loadPlaylists()
                    }

                    adapter.setOnPlaylistClickListener { playlist ->
                        checkPlaylistAndOpenActivity(playlist.id)
                    }

                    playlistRecyclerView.adapter = adapter
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load playlists: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkPlaylistAndOpenActivity(playlistId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val songs = playlistSongsRepository.getSongsOfPlaylist(playlistId)
            withContext(Dispatchers.Main) {
                if (songs.isNotEmpty()) {
                    val intent = Intent(requireContext(), PlaylistSongsActivity::class.java)
                    intent.putExtra("playlistId", playlistId)
                    startActivityForResult(intent, PlaylistSongsActivity.RESULT_PLAYLIST_MODIFIED)
                } else {

                    Toast.makeText(requireContext(), "Playlist is empty", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}