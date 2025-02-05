package com.example.musicplayerapp.ui.theme

import MusicFiles
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.MainActivity.Companion.musicFiles

class AlbumDetailsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var albumPhoto: ImageView
    private lateinit var albumName: String
    private var albumSongs: ArrayList<MusicFiles> = ArrayList()
    private var albumDetailsAdapter: AlbumDetailsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_details)

        recyclerView = findViewById(R.id.recyclerView)
        albumPhoto = findViewById(R.id.albumPhoto)
        albumName = intent.getStringExtra("albumName") ?: ""

        for (i in musicFiles.indices) {
            if (albumName == musicFiles[i].album) {
                albumSongs.add(musicFiles[i])
            }
        }

        val image = albumSongs[0].path?.let { getAlbumArt(it) }
        if (image != null) {
            Glide.with(this)
                .load(image)
                .into(albumPhoto)
        } else {
            Glide.with(this)
                .load(R.drawable.bewedoc)
                .into(albumPhoto)
        }
    }

    override fun onResume() {
        super.onResume()
        if (albumSongs.isNotEmpty()) {
            albumDetailsAdapter = AlbumDetailsAdapter(this, albumSongs,lifecycleScope)
            recyclerView.adapter = albumDetailsAdapter
            recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        }
    }

    private fun getAlbumArt(uri: String): ByteArray? {
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
}
