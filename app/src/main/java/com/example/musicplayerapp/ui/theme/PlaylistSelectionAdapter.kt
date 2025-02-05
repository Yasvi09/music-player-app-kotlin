package com.example.musicplayerapp.ui.theme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.database.Playlist

class PlaylistSelectionAdapter(private val playlists: List<Playlist>, private val onPlaylistSelected: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistSelectionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playlistName: TextView = itemView.findViewById(R.id.playlist_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.playlistName.text = playlist.name
        holder.itemView.setOnClickListener {
            onPlaylistSelected(playlist)
        }
    }

    override fun getItemCount() = playlists.size
}