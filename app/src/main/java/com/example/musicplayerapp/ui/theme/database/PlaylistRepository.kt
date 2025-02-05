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

            // Modified query to include song count
            val query = """
                SELECT p._id, p.name, p.timestamp, 
                COUNT(ps.song_id) as song_count 
                FROM PlaylistDatabase p 
                LEFT JOIN PlaylistSongs ps ON p._id = ps.playlist_id 
                GROUP BY p._id, p.name, p.timestamp 
                ORDER BY p.timestamp DESC
            """.trimIndent()

            val result = MySQLDatabase.executeQuery(query) ?: return@withContext emptyList()

            result.rows.map { row ->
                Playlist(
                    id = row.getString("_id") ?: "",
                    name = row.getString("name") ?: "",
                    timestamp = row.getString("timestamp") ?: "",
                    songCount = row.getInt("song_count") ?: 0
                )
            }
        } catch (e: Exception) {
            Log.e("Playlist", "Error getting playlists: ${e.message}")
            emptyList()
        }
    }

    suspend fun deletePlaylist(playlistId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // First delete all songs in the playlist
            val deletePlaylistSongsQuery = "DELETE FROM PlaylistSongs WHERE playlist_id = ?"
            MySQLDatabase.executeQuery(deletePlaylistSongsQuery, listOf(playlistId))

            // Then delete the playlist itself
            val deletePlaylistQuery = "DELETE FROM PlaylistDatabase WHERE _id = ?"
            MySQLDatabase.executeQuery(deletePlaylistQuery, listOf(playlistId))

            Log.d(TAG, "Playlist deleted successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting playlist: ${e.message}")
            false
        }
    }

}


data class Playlist(
    val id: String,
    val name: String,
    val timestamp: String,
    val songCount: Int = 0  // Added songCount property
)