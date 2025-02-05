package com.example.musicplayerapp.ui.theme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.database.PlaylistSong

class PlaylistSongsAdapter(private val songs: List<PlaylistSong>) : RecyclerView.Adapter<PlaylistSongsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val musicImg: ImageView = itemView.findViewById(R.id.music_img)
        val musicFileName: TextView = itemView.findViewById(R.id.music_file_name)
        val menuMore: ImageView = itemView.findViewById(R.id.menuMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.music_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]

        // Update the UI elements with song data
        holder.musicFileName.text = song.songId  // You can replace with actual song name or title
        // Use Glide or Picasso to load image if available, for now, we will use a placeholder
        holder.musicImg.setImageResource(R.drawable.ic_launcher_background)  // Set appropriate image

        // Set click listener for "menuMore" (You can customize this as per your need)
        holder.menuMore.setOnClickListener {
            // Handle menu actions (e.g., remove from playlist, etc.)
        }
    }

    override fun getItemCount() = songs.size
}
