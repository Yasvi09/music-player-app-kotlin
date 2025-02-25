package com.example.musicplayerapp.ui.theme

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.database.Playlist
import com.example.musicplayerapp.ui.theme.database.PlaylistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaylistAdapter(
    private val mContext: Context,
    private val playlists: List<Playlist>,
    private val onPlaylistDeleted: () -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    private var onPlaylistClickListener: ((Playlist) -> Unit)? = null
    private lateinit var preferencesManager: PlaylistPreferencesManager

    init {
        preferencesManager = PlaylistPreferencesManager(mContext)
    }

    fun setOnPlaylistClickListener(listener: (Playlist) -> Unit) {
        onPlaylistClickListener = listener
    }

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playlistName: TextView = itemView.findViewById(R.id.playlist_name)
        val songCount: TextView = itemView.findViewById(R.id.song_count)
        val menuMore: ImageView = itemView.findViewById(R.id.menuMore)
        val playlistInitial: TextView = itemView.findViewById(R.id.playlist_initial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.playlist_item, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.playlistName.text = playlist.name

        val songCountText = "Songs: ${playlist.songCount}"
        holder.songCount.text = songCountText

        val firstletter = playlist.name.firstOrNull()?.toString()?.uppercase() ?: "?"
        holder.playlistInitial.text = firstletter

        holder.menuMore.setImageResource(R.drawable.ic_remove)

        holder.itemView.setOnClickListener {
            onPlaylistClickListener?.invoke(playlist)
        }

        holder.menuMore.setOnClickListener { view ->
            showPopupMenu(view, playlist)
        }
    }

    private fun showPopupMenu(view: View, playlist: Playlist) {
        val popupMenu = PopupMenu(mContext, view)
        popupMenu.menuInflater.inflate(R.menu.playlist_popup, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete_playlist -> {
                    showDeleteConfirmationDialog(playlist)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showDeleteConfirmationDialog(playlist: Playlist) {
        val dialog = Dialog(mContext)
        dialog.setContentView(R.layout.custom_dialogue)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val dialogMessage = dialog.findViewById<TextView>(R.id.dialogMessage)
        val yesButton = dialog.findViewById<Button>(R.id.yesButton)
        val noButton = dialog.findViewById<Button>(R.id.noButton)

        // Set message
        val message = if (playlist.songCount > 0) {
            "Delete playlist '${playlist.name}' and remove ${playlist.songCount} songs?"
        } else {
            "Delete playlist '${playlist.name}'?"
        }
        dialogMessage.text = message

        yesButton.setOnClickListener {
            dialog.dismiss()
            deletePlaylist(playlist)
        }

        noButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deletePlaylist(playlist: Playlist) {
        val playlistRepository = PlaylistRepository()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val success = playlistRepository.deletePlaylist(playlist.id)
                if (success) {
                    // Remove playlist order from SharedPreferences
                    preferencesManager.removePlaylistOrder(playlist.id)

                    Toast.makeText(mContext, "Playlist deleted successfully", Toast.LENGTH_SHORT).show()
                    onPlaylistDeleted()
                } else {
                    Toast.makeText(mContext, "Failed to delete playlist", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(mContext, "Error deleting playlist: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun getItemCount(): Int = playlists.size
}