package com.example.musicplayerapp.ui.theme

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlaylistPreferencesManager(context: Context) {
    private val PREFS_NAME = "playlist_preferences"
    private val SONG_ORDER_PREFIX = "song_order_"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // Save song order for a playlist
    fun saveSongOrder(playlistId: String, songPaths: List<String>) {
        val json = gson.toJson(songPaths)
        prefs.edit().putString(SONG_ORDER_PREFIX + playlistId, json).apply()
    }

    // Get song order for a playlist
    fun getSongOrder(playlistId: String): List<String> {
        val json = prefs.getString(SONG_ORDER_PREFIX + playlistId, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    // Remove song order when playlist is deleted
    fun removePlaylistOrder(playlistId: String) {
        prefs.edit().remove(SONG_ORDER_PREFIX + playlistId).apply()
    }

    // Update song order when a song is removed
    fun removeSongFromOrder(playlistId: String, songPath: String) {
        val currentOrder = getSongOrder(playlistId).toMutableList()
        currentOrder.remove(songPath)
        saveSongOrder(playlistId, currentOrder)
    }

    // Clear all preferences
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}