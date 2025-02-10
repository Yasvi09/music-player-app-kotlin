package com.example.musicplayerapp.ui.theme

import MusicFiles
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.database.PlaylistRepository
import com.example.musicplayerapp.ui.theme.database.PlaylistSongsRepository
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MusicAdapter(private val mContext: Context, private val mFiles: ArrayList<MusicFiles>, private val lifecycleScope: LifecycleCoroutineScope)
    : RecyclerView.Adapter<MusicAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.fileName.text = mFiles[position].title
        val image = mFiles[position].path?.let { getAlbumArt(it) }
        if (image != null) {
            Glide.with(mContext).asBitmap().load(image).into(holder.albumArt)
        } else {
            Glide.with(mContext).load(R.drawable.error_image).into(holder.albumArt)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, PlayerActivity::class.java)
            intent.putExtra("position", position)
            // Set the source for the mini player
            NowPlayingFragmentBottom.CURRENT_SONG_SOURCE = "mainList"
            mContext.startActivity(intent)
        }

        holder.menuMore.setOnClickListener {
            val popupMenu = PopupMenu(mContext, it)
            popupMenu.menuInflater.inflate(R.menu.popup, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.playlist -> {
                        showPlaylistSelectionDialog(position)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    override fun getItemCount(): Int = mFiles.size

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.music_file_name)
        val albumArt: ImageView = itemView.findViewById(R.id.music_img)
        val menuMore: ImageView = itemView.findViewById(R.id.menuMore)
    }

    private fun showPlaylistSelectionDialog(position: Int) {
        val bottomSheetDialog = BottomSheetDialog(mContext)
        val view = LayoutInflater.from(mContext).inflate(R.layout.playlist_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.playlist_selection_recycler)
        recyclerView.layoutManager = LinearLayoutManager(mContext)

        val playlistRepository = PlaylistRepository()
        val playlistSongsRepository = PlaylistSongsRepository()

        lifecycleScope.launch {
            try {
                val playlists = playlistRepository.getAllPlaylists()
                Log.d("Playlist","Playlists:${playlists}")

                val adapter = PlaylistSelectionAdapter(playlists, { selectedPlaylist ->
                    lifecycleScope.launch {
                        try {
                            val song = mFiles[position]
                            val success = playlistSongsRepository.addSongToPlaylist(
                                selectedPlaylist.id,
                                song.path ?: "",

                                )

                            withContext(Dispatchers.Main) {
                                if (success) {
                                    Toast.makeText(mContext, "Added to playlist: ${selectedPlaylist.name}", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(mContext, "Failed to add to playlist", Toast.LENGTH_SHORT).show()
                                }
                                bottomSheetDialog.dismiss()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(mContext, "Error adding song to playlist: ${e.message}", Toast.LENGTH_SHORT).show()
                                bottomSheetDialog.dismiss()
                            }
                        }
                    }
                },hideMenu = true)

                withContext(Dispatchers.Main) {
                    recyclerView.adapter = adapter
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(mContext, "Error loading playlists: ${e.message}", Toast.LENGTH_LONG).show()
                    bottomSheetDialog.dismiss()
                }
            }
        }

        bottomSheetDialog.show()
    }

    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(uri)
            retriever.embeddedPicture
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }
}
