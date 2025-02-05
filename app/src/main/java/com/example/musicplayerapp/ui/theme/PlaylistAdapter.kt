package com.example.musicplayerapp.ui.theme

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.database.Playlist


class PlaylistAdapter(private val playlists: List<Playlist>) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_item, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist)
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, PlaylistSongsActivity::class.java)
            intent.putExtra("playlistId", playlist.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = playlists.size

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playlistName: TextView = itemView.findViewById(R.id.playlist_name)
        private val songCount: TextView = itemView.findViewById(R.id.song_count)
        private val musicImg: ImageView = itemView.findViewById(R.id.music_img)
        fun bind(playlist: Playlist) {
            playlistName.text = playlist.name
            songCount.text = "Songs: ${playlist.timestamp}"
        }
    }
}
