package com.example.musicplayerapp.ui.theme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayerapp.R

class AlbumFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var albumAdapter: AlbumAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_album, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)

        if (MainActivity.albums.isNotEmpty()) {
            albumAdapter = AlbumAdapter(requireContext(), MainActivity.albums)
            recyclerView.adapter = albumAdapter
            recyclerView.layoutManager = GridLayoutManager(requireContext(),2)
        }
        return view
    }
}
