package com.example.musicplayerapp.ui.theme

import MusicFiles
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.database.PlaylistSongsRepository
import kotlinx.coroutines.launch

class PlaylistSongsAdapter(
    private val mContext: Context,
    private val songs: List<MusicFiles>,
    private val playlistId: String,
    private val lifecycleScope: LifecycleCoroutineScope
) : RecyclerView.Adapter<PlaylistSongsAdapter.ViewHolder>() {

    private val playlistSongsRepository = PlaylistSongsRepository()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val musicImg: ImageView = itemView.findViewById(R.id.music_img)
        val musicFileName: TextView = itemView.findViewById(R.id.music_file_name)
        val menuMore: ImageView = itemView.findViewById(R.id.menuMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]

        holder.musicFileName.text = song.title

        // Load album art
        val image = getAlbumArt(song.path)
        if (image != null) {
            Glide.with(mContext).asBitmap().load(image).into(holder.musicImg)
        } else {
            Glide.with(mContext).load(R.drawable.bewedoc).into(holder.musicImg)
        }

        // Handle item click to play music
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, PlayerActivity::class.java).apply {
                // Set the static playlist in PlayerActivity
                PlayerActivity.listSongs = ArrayList(PlaylistSongsActivity.currentPlaylistSongs)
                putExtra("position", position)
                putExtra("sender", "playlistSongs")
            }

            // Set the mini player data
            MainActivity.SHOW_MINI_PLAYER = true
            MainActivity.PATH_TO_FRAG = song.path
            MainActivity.ARTIST_TO_FRAG = song.artist
            MainActivity.SONG_NAME_TO_FRAG = song.title

            mContext.startActivity(intent)
        }

        // Handle menu click
        holder.menuMore.setOnClickListener { view ->
            val popupMenu = PopupMenu(mContext, view)
            popupMenu.menuInflater.inflate(R.menu.playlist_songs_popup, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.remove_playlist -> {
                        removeSongFromPlaylist(song.path ?: "", position)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    private fun removeSongFromPlaylist(songPath: String, position: Int) {
        lifecycleScope.launch {
            val success = playlistSongsRepository.removeSongFromPlaylist(songPath, playlistId)
            if (success) {
                // Update both the adapter's list and the static currentPlaylistSongs
                (songs as MutableList).removeAt(position)
                PlaylistSongsActivity.currentPlaylistSongs.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, songs.size)
                Toast.makeText(mContext, "Song removed from playlist", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mContext, "Failed to remove song", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAlbumArt(uri: String?): ByteArray? {
        if (uri == null) return null
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(uri)
            retriever.embeddedPicture
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    override fun getItemCount() = songs.size
}