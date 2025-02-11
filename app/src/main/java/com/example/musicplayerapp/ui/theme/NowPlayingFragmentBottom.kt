package com.example.musicplayerapp.ui.theme

import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.musicplayerapp.R
import com.example.musicplayerapp.ui.theme.MainActivity.Companion.ARTIST_TO_FRAG
import com.example.musicplayerapp.ui.theme.MainActivity.Companion.PATH_TO_FRAG
import com.example.musicplayerapp.ui.theme.MainActivity.Companion.SHOW_MINI_PLAYER
import com.example.musicplayerapp.ui.theme.MainActivity.Companion.SONG_NAME_TO_FRAG
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.IOException

class NowPlayingFragmentBottom : Fragment(), ServiceConnection, MusicServiceCallback  {

    private lateinit var nextBtn: ImageView
    private lateinit var albumArt: ImageView
    private lateinit var artist: TextView
    private lateinit var songName: TextView
    private lateinit var playPauseBtn: FloatingActionButton
    private lateinit var view: View
    var musicService: MusicService? = null


    companion object {
        const val MUSIC_LAST_PLAYED = "LAST_PLAYED"
        const val MUSIC_FILE = "STORED_MUSIC"
        const val ARTIST_NAME = "ARTIST NAME"
        const val SONG_NAME = "SONG NAME"
        var CURRENT_SONG_SOURCE = "mainList"

    }

    private var isServiceBound = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_now_playing_bottom, container, false)
        artist = view.findViewById(R.id.song_artist_miniPlayer)
        songName = view.findViewById(R.id.song_name_miniPlayer)
        albumArt = view.findViewById(R.id.bottom_album_art)
        nextBtn = view.findViewById(R.id.skip_next_bottom)
        playPauseBtn = view.findViewById(R.id.play_pause_miniPlayer)

        nextBtn.setOnClickListener {
            if (musicService != null && isServiceBound) {
                musicService!!.nextBtnCicked()
                activity?.let { updateUI() }
            } else {
                Toast.makeText(context, "Music service not ready", Toast.LENGTH_SHORT).show()
            }
        }

        playPauseBtn.setOnClickListener {
            println("Play/Pause button clicked")
            if (musicService != null && isServiceBound) {
                try {
                    musicService!!.playPauseBtnClicked()
                    // The UI will be updated through the callback mechanism
                } catch (e: Exception) {
                    Toast.makeText(context, "Error controlling playback: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Music service not ready", Toast.LENGTH_SHORT).show()
            }
        }

        val cardBottomPlayer: View? = view.findViewById(R.id.card_bottom_player)
        cardBottomPlayer?.setOnClickListener {
            if (musicService != null && musicService!!.mediaPlayer != null && PATH_TO_FRAG != null) {
                val intent = Intent(context, PlayerActivity::class.java)
                intent.putExtra("position", musicService!!.position)
                intent.putExtra("current_position", musicService!!.getCurrentPosition())

                // Set the correct song list based on the source
                when (CURRENT_SONG_SOURCE) {
                    "playlist" -> {
                        intent.putExtra("sender", "playlistSongs")
                        PlayerActivity.listSongs = ArrayList(PlaylistSongsActivity.currentPlaylistSongs)
                    }
                    "album" -> {
                        intent.putExtra("sender", "albumDetails")
                        // Album songs should already be in PlayerActivity.listSongs
                    }
                    else -> {
                        intent.putExtra("sender", "mainList")
                        PlayerActivity.listSongs = MainActivity.musicFiles
                    }
                }

                startActivity(intent)
            }
        }

        return view
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val binder = service as MusicService.MyBinder
        musicService = binder.getService()
        musicService?.addCallback(this)
        isServiceBound = true

        updatePlayPauseButton()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService?.removeCallback(this)
        musicService = null
        isServiceBound = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        musicService?.removeCallback(this)
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        activity?.runOnUiThread {
            updatePlayPauseButton()
        }
    }

    fun updatePlayPauseButton() {
        if (musicService?.mediaPlayer != null) {
            val isPlaying = musicService!!.isPlaying()
            playPauseBtn.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )

            // Notify PlaylistSongsActivity about the state change
            (activity as? PlaylistSongsActivity)?.onMiniPlayerStateChanged()
        }
    }

    private fun updateUI() {
        if (activity == null) return

        val editor = activity?.getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE)?.edit()
        if (musicService != null && musicService!!.musicFiles != null && musicService!!.position >= 0
            && musicService!!.position < musicService!!.musicFiles.size) {

            editor?.putString(MUSIC_FILE, musicService!!.musicFiles[musicService!!.position].path)
            editor?.putString(ARTIST_NAME, musicService!!.musicFiles[musicService!!.position].artist)
            editor?.putString(SONG_NAME, musicService!!.musicFiles[musicService!!.position].title)
            editor?.apply()

            val preferences = activity?.getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE)
            val path = preferences?.getString(MUSIC_FILE, null)
            val artistName = preferences?.getString(ARTIST_NAME, null)
            val songName = preferences?.getString(SONG_NAME, null)

            if (path != null) {
                SHOW_MINI_PLAYER = true
                PATH_TO_FRAG = path
                ARTIST_TO_FRAG = artistName
                SONG_NAME_TO_FRAG = songName
            } else {
                SHOW_MINI_PLAYER = false
                PATH_TO_FRAG = null
                ARTIST_TO_FRAG = null
                SONG_NAME_TO_FRAG = null
            }

            if (SHOW_MINI_PLAYER && PATH_TO_FRAG != null) {
                updateBottomPlayerUI()
            }
        }
    }

    private fun updateBottomPlayerUI() {
        if (context == null) return

        val art = PATH_TO_FRAG?.let { getAlbumArt(it) }
        if (art != null) {
            Glide.with(requireContext()).load(art)
                .into(albumArt)
        } else {
            Glide.with(requireContext()).load(R.drawable.bewedoc)
                .into(albumArt)
        }
        songName.text = SONG_NAME_TO_FRAG
        artist.text = ARTIST_TO_FRAG

        updatePlayPauseButton()
    }

    override fun onResume() {
        super.onResume()
        if (SHOW_MINI_PLAYER) {
            if (PATH_TO_FRAG != null) {
                context?.let {
                    val intent = Intent(it, MusicService::class.java)
                    it.bindService(intent, this, Context.BIND_AUTO_CREATE)
                    updateBottomPlayerUI()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isServiceBound) {
            context?.let {
                it.unbindService(this)
                isServiceBound = false
            }
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
            try {
                retriever.release()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }


}