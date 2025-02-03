package com.example.musicplayerapp.ui.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayerapp.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PlaylistFragment : Fragment() {

    private lateinit var playlistRecyclerView: RecyclerView
    private lateinit var addPlaylistBtn: FloatingActionButton
    private lateinit var  dbHelper : MusicPlayerDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)
        playlistRecyclerView = view.findViewById(R.id.recyclerView)
        addPlaylistBtn = view.findViewById(R.id.add_playlist)

        playlistRecyclerView.layoutManager = LinearLayoutManager(context)
        loadPlaylists()

        addPlaylistBtn.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet, null)
            bottomSheetDialog.setContentView(bottomSheetView)

            val etPlaylistName = bottomSheetView.findViewById<EditText>(R.id.et_playlist_name)
            val btnCreate = bottomSheetView.findViewById<Button>(R.id.btn_create)
            val btnCancel = bottomSheetView.findViewById<Button>(R.id.btn_cancel)

            btnCreate.setOnClickListener {
                val name = etPlaylistName.text.toString()
                if (name.isNotBlank()) {
                    try {
                        val playlistId = dbHelper.createPlaylist(name)
                        if (playlistId != -1L) {
                            loadPlaylists()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // You can show a toast here if you want to notify the user about the error
                    }
                }
                bottomSheetDialog.dismiss()
            }

            btnCancel.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        return view
    }

    private fun loadPlaylists() {
        try {
            val playlists = dbHelper.getAllPlaylists()
            val adapter = PlaylistAdapter(playlists)
            playlistRecyclerView.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
            // You can show a toast here if you want to notify the user about the error
        }
    }
}
