package com.example.musicplayerapp.ui.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayerapp.R

class SongsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var musicAdapter: MusicAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_songs, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)

        if (MainActivity.musicFiles.isNotEmpty()) {
            musicAdapter = MusicAdapter(requireContext(), MainActivity.musicFiles, lifecycleScope)
            recyclerView.adapter = musicAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }
        return view
    }
}