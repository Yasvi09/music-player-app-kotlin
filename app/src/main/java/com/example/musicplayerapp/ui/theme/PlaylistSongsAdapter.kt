package com.example.musicplayerapp.ui.theme

import MusicFiles
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.database.PlaylistSongsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistSongsAdapter(private val mContext: Context, private val songs: MutableList<MusicFiles>, private val playlistId: String, private val lifecycleScope: LifecycleCoroutineScope) : RecyclerView.Adapter<PlaylistSongsAdapter.ViewHolder>() {

    private val playlistSongsRepository = PlaylistSongsRepository()
    private lateinit var preferencesManager: PlaylistPreferencesManager


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val musicImg: ImageView = itemView.findViewById(R.id.music_img)
        val musicFileName: TextView = itemView.findViewById(R.id.music_file_name)
        val menuMore: ImageView = itemView.findViewById(R.id.menuMore)


        init {
            itemView.setOnLongClickListener {
                preferencesManager = PlaylistPreferencesManager(mContext)
                val vibrator = mContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false)
        return ViewHolder(view)
    }

    // In PlaylistSongsAdapter.kt, update the onBindViewHolder method

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]
        holder.musicFileName.text = song.title

        holder.menuMore.setImageResource(R.drawable.ic_remove)

        val image = getAlbumArt(song.path)
        if (image != null) {
            Glide.with(mContext).asBitmap().load(image).into(holder.musicImg)
        } else {
            Glide.with(mContext).load(R.drawable.bewedoc).into(holder.musicImg)
        }

        holder.menuMore.setOnClickListener { view ->
            val popupMenu = PopupMenu(mContext, view)
            popupMenu.menuInflater.inflate(R.menu.playlist_songs_popup, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.remove_playlist -> {
                        showRemoveConfirmationDialog(song, position)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        holder.itemView.setOnClickListener {
            // First, ensure PlayerActivity's list is updated with current order
            PlayerActivity.listSongs = ArrayList(songs)

            // Find the actual position of this song in the main music files list
            val currentSong = songs[position]
            val intent = Intent(mContext, PlayerActivity::class.java).apply {
                putExtra("position", position)
                putExtra("sender", "playlistSongs")
            }

            // Update the current playlist songs with current order
            PlaylistSongsActivity.currentPlaylistSongs = ArrayList(songs)

            MainActivity.SHOW_MINI_PLAYER = true
            MainActivity.PATH_TO_FRAG = currentSong.path
            MainActivity.ARTIST_TO_FRAG = currentSong.artist
            MainActivity.SONG_NAME_TO_FRAG = currentSong.title

            // Set the source for the mini player
            NowPlayingFragmentBottom.CURRENT_SONG_SOURCE = "playlist"

            // Start the PlayerActivity
            mContext.startActivity(intent)
        }
    }

    private fun showRemoveConfirmationDialog(song: MusicFiles, position: Int) {
        val dialog = Dialog(mContext)
        dialog.setContentView(R.layout.custom_dialogue)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val dialogMessage = dialog.findViewById<TextView>(R.id.dialogMessage)
        val yesButton = dialog.findViewById<Button>(R.id.yesButton)
        val noButton = dialog.findViewById<Button>(R.id.noButton)

        dialogMessage.text = "Remove ${song.title} from playlist?"

        yesButton.setOnClickListener {
            dialog.dismiss()
            removeSongFromPlaylist(song, position)
        }

        noButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun removeSongFromPlaylist(song: MusicFiles, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val success = playlistSongsRepository.removeSongFromPlaylist(song.path ?: "", playlistId)
                withContext(Dispatchers.Main) {
                    if (success) {
                        // Remove song from SharedPreferences order
                        song.path?.let {
                            preferencesManager.removeSongFromOrder(playlistId, it)
                        }

                        songs.removeAt(position)
                        PlaylistSongsActivity.currentPlaylistSongs.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, songs.size)
                        Toast.makeText(mContext, "Song removed from playlist", Toast.LENGTH_SHORT).show()

                        if (songs.isEmpty()) {
                            (mContext as? PlaylistSongsActivity)?.onPlaylistEmpty()
                        }
                    } else {
                        Toast.makeText(mContext, "Failed to remove song", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(mContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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