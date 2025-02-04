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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayerapp.R
import com.google.android.material.bottomsheet.BottomSheetDialog

class AlbumDetailsAdapter(private val mContext: Context, private val albumFiles: ArrayList<MusicFiles>) : RecyclerView.Adapter<AlbumDetailsAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false)
        return MyHolder(view)
    }

    override fun getItemCount(): Int {
        return albumFiles.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.albumName.text = albumFiles[position].title
        val image = albumFiles[position].path?.let { getAlbumArt(it) }
        if (image != null) {
            Glide.with(mContext).asBitmap().load(image).into(holder.albumImage)
        } else {
            Glide.with(mContext).load(R.drawable.error_image).into(holder.albumImage)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, PlayerActivity::class.java).apply {
                putExtra("sender", "albumDetails")
                putExtra("position", position)
                putParcelableArrayListExtra("albumFiles", albumFiles) // Pass albumFiles here
            }
            mContext.startActivity(intent)
        }

        holder.menuMore.setOnClickListener {
            val popupMenu = PopupMenu(mContext, it)
            popupMenu.menuInflater.inflate(R.menu.popup, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.playlist ->{
                        showBottomSheetDialog()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumImage: ImageView = itemView.findViewById(R.id.music_img)
        val albumName: TextView = itemView.findViewById(R.id.music_file_name)
        val menuMore:ImageView=itemView.findViewById(R.id.menuMore)
    }

    private fun getAlbumArt(uri: String?): ByteArray? {
        if (uri.isNullOrEmpty()) {
            return null
        }
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

    private fun showBottomSheetDialog() {

        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val bottomSheetView = inflater.inflate(R.layout.playlist_bottom_sheet, null)

        val bottomSheetDialog = BottomSheetDialog(mContext)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }


}