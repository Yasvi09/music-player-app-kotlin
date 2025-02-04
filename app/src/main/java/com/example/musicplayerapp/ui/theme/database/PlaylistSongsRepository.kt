package com.example.musicplayerapp.ui.theme.database

import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import java.time.LocalDateTime

class PlaylistSongsRepository {
    private val TAG = "PlaylistSongsRepository"

    suspend fun addSongToPlaylist(playlistId: String, songId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val songEntryId = UUID.randomUUID().toString()
            val timestamp = LocalDateTime.now().toString()
            val query = "INSERT INTO PlaylistSongs (_id, playlist_id, song_id, timestamp) VALUES (?, ?, ?, ?)"

            MySQLDatabase.executeQuery(query, listOf(songEntryId, playlistId, songId, timestamp))
            Log.d(TAG, "Song added to playlist successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding song to playlist: ${e.message}")
            false
        }
    }

    suspend fun getSongsOfPlaylist(playlistId: String): List<PlaylistSong> = withContext(Dispatchers.IO) {
        try {
            val query = "SELECT _id, playlist_id, song_id, timestamp FROM PlaylistSongs WHERE playlist_id = ? ORDER BY timestamp DESC"
            val result = MySQLDatabase.executeQuery(query, listOf(playlistId)) ?: return@withContext emptyList()

            result.rows.map { row ->
                PlaylistSong(
                    id = row.getString("_id") ?: "",
                    playlistId = row.getString("playlist_id") ?: "",
                    songId = row.getString("song_id") ?: "",
                    timestamp = row.getString("timestamp") ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching songs for playlist: ${e.message}")
            emptyList()
        }
    }

    suspend fun removeSongFromPlaylist(songId: String, playlistId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val query = "DELETE FROM PlaylistSongs WHERE song_id = ? AND playlist_id = ?"
            MySQLDatabase.executeQuery(query, listOf(songId, playlistId))
            Log.d(TAG, "Song removed from playlist successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing song from playlist: ${e.message}")
            false
        }
    }
}

data class PlaylistSong(
    val id: String,
    val playlistId: String,
    val songId: String,
    val timestamp: String,
)
