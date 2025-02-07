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
            val query = "INSERT INTO PlaylistDatabase (_id, name, timestamp) VALUES (?, ?, ?)"

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

            val query = """
                SELECT p._id, p.name, p.timestamp,
                (SELECT COUNT(*) FROM PlaylistSongs ps WHERE ps.playlist_id = p._id) as song_count
                FROM PlaylistDatabase p
                ORDER BY p.timestamp DESC
            """.trimIndent()

            val result = MySQLDatabase.executeQuery(query)
            Log.d(TAG, "Query result: ${result?.rows?.size}")

            return@withContext result?.rows?.map { row ->
                val songCount = row.getLong("song_count")?.toInt() ?: 0  // Convert Long to Int safely
                Playlist(
                    id = row.getString("_id") ?: "",
                    name = row.getString("name") ?: "",
                    timestamp = row.getString("timestamp") ?: "",
                    songCount = songCount
                )
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting playlists: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    suspend fun deletePlaylist(playlistId: String): Boolean = withContext(Dispatchers.IO) {
        try {

            val deletePlaylistSongsQuery = "DELETE FROM PlaylistSongs WHERE playlist_id = ?"
            MySQLDatabase.executeQuery(deletePlaylistSongsQuery, listOf(playlistId))

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