package com.example.musicplayerapp.ui.theme

import MusicFiles
import com.example.musicplayerapp.R
import android.annotation.SuppressLint
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
import java.io.IOException

class MusicAdapter(private val mContext: Context, private val mFiles: ArrayList<MusicFiles>) :
    RecyclerView.Adapter<MusicAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
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
            mContext.startActivity(intent)
        }

        holder.menuMore.setOnClickListener {
            val popupMenu = PopupMenu(mContext, it)
            popupMenu.menuInflater.inflate(R.menu.popup, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.playlist ->{
                        Toast.makeText(mContext, "Add to Playlist", Toast.LENGTH_SHORT).show()
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
        val menuMore:ImageView=itemView.findViewById(R.id.menuMore)
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
