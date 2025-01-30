package com.example.musicplayerapp.ui.theme

import MusicFiles
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayerapp.R

class AlbumAdapter (private val mContext: Context, private val albumFiles: ArrayList<MusicFiles>) : RecyclerView.Adapter<AlbumAdapter.MyHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false)
        return MyHolder(view)
    }

    override fun getItemCount(): Int {
        return albumFiles.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.albumName.text = albumFiles[position].album
        val image = albumFiles[position].path?.let { getAlbumArt(it) }
        if (image != null) {
            Glide.with(mContext).asBitmap().load(image).into(holder.albumImage)
        } else {
            Glide.with(mContext).load(R.drawable.error_image).into(holder.albumImage)
        }


        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, AlbumDetailsActivity::class.java)
            intent.putExtra("albumName", albumFiles[position].album)
            mContext.startActivity(intent)
        }
    }

    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumImage: ImageView = itemView.findViewById(R.id.album_image)
        val albumName: TextView = itemView.findViewById(R.id.album_name)
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}