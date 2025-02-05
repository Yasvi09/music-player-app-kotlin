package com.example.musicplayerapp.ui.theme

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.database.Playlist
import com.example.musicplayerapp.ui.theme.database.PlaylistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaylistAdapter(
    private val playlists: List<Playlist>,
    private val onPlaylistDeleted: () -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    private var onPlaylistClickListener: ((Playlist) -> Unit)? = null

    fun setOnPlaylistClickListener(listener: (Playlist) -> Unit) {
        onPlaylistClickListener = listener
    }

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playlistName: TextView = itemView.findViewById(R.id.playlist_name)
        val songCount: TextView = itemView.findViewById(R.id.song_count)
        val musicImg: ImageView = itemView.findViewById(R.id.music_img)
        val menuMore: ImageView = itemView.findViewById(R.id.menuMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_item, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.playlistName.text = playlist.name

        // Format song count text
        val songCountText = when (playlist.songCount) {
            0 -> "No songs"
            1 -> "1 song"
            else -> "${playlist.songCount} songs"
        }
        holder.songCount.text = songCountText

        // Handle item click for opening playlist
        holder.itemView.setOnClickListener {
            onPlaylistClickListener?.invoke(playlist)
        }

        // Handle three-dot menu click
        holder.menuMore.setOnClickListener { view ->
            showPopupMenu(view, playlist, holder.itemView.context)
        }
    }

    private fun showPopupMenu(view: View, playlist: Playlist, context: Context) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.playlist_popup, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete_playlist -> {
                    deletePlaylist(playlist, context)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun deletePlaylist(playlist: Playlist, context: Context) {
        val playlistRepository = PlaylistRepository()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val success = playlistRepository.deletePlaylist(playlist.id)
                if (success) {
                    Toast.makeText(context, "Playlist deleted successfully", Toast.LENGTH_SHORT).show()
                    onPlaylistDeleted()
                } else {
                    Toast.makeText(context, "Failed to delete playlist", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error deleting playlist: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun getItemCount(): Int = playlists.size
}