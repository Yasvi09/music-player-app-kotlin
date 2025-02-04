package com.example.musicplayerapp.ui.theme.database

import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import java.time.LocalDateTime


class PlaylistRepository {
    private val TAG = "PlaylistRepository"


    suspend fun createPlaylist(name: String): String? = withContext(Dispatchers.IO) {
        try {
            val playlistId = UUID.randomUUID().toString()
            val timestamp = LocalDateTime.now().toString()
            val query = "INSERT INTO PlaylistDatabase (_id, name, timestamp) VALUES (?, ?, ?)"  // Changed 'id' to '_id'

            MySQLDatabase.executeQuery(query, listOf(playlistId, name, timestamp))
            Log.d(TAG, "Playlist created successfully")
            playlistId
        } catch (e: Exception) {
            Log.e(TAG, "Error creating playlist: ${e.message}")
            null
        }
    }

    suspend fun getAllPlaylists(): List<Playlist> = withContext(Dispatchers.IO) {
        try {
            val isConnected = MySQLDatabase.connect()
            if (!isConnected) {
                Log.e(TAG, "Database is not connected")
                return@withContext emptyList()
            }
            val query = "SELECT _id, name, timestamp FROM PlaylistDatabase ORDER BY timestamp DESC"
            val result = MySQLDatabase.executeQuery(query) ?: return@withContext emptyList()

            Log.e("Playlist","${result}")
            result.rows.map { row ->
                Playlist(
                    id = row.getString("_id") ?: "",
                    name = row.getString("name") ?: "",
                    timestamp = row.getString("timestamp") ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("Playlist", "Error getting playlists: ${e.message}")
            emptyList()
        }
    }

}


data class Playlist(
    val id: String,
    val name: String,
    val timestamp: String,
)