package com.example.musicplayerapp.ui.theme

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
import com.example.musicplayerapp.ui.theme.database.PlaylistRepository
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistFragment : Fragment() {
    private lateinit var playlistRecyclerView: RecyclerView
    private lateinit var addPlaylistBtn: FloatingActionButton
    private lateinit var playlistRepository: PlaylistRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        lifecycleScope.launch {
            try {
                MySQLDatabase.connect()
                loadPlaylists()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to connect to database: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val view = inflater.inflate(R.layout.fragment_playlist, container, false)
        playlistRecyclerView = view.findViewById(R.id.recyclerView)
        addPlaylistBtn = view.findViewById(R.id.add_playlist)

        playlistRepository = PlaylistRepository()

        playlistRecyclerView.layoutManager = LinearLayoutManager(context)

        addPlaylistBtn.setOnClickListener {
            showCreatePlaylistDialog()
        }

        return view
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
                    }
                    else{
                        noPlaylistTextView?.visibility = View.GONE
                        allPlaylists.forEach {
                            Log.d("Playlist", "ID: ${it.id}, Name: ${it.name}")
                        }
                    }

                    val adapter = PlaylistAdapter(allPlaylists)
                    playlistRecyclerView.adapter = adapter
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load playlists: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}