package com.example.musicplayerapp.ui.theme

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

public class MusicPlayerDatabaseHelper {
    companion object {
        private const val DATABASE_URL = "jdbc:mysql://sql12.freesqldatabase.com:3306/sql12760814"
        private const val DATABASE_USER = "sql12760814"
        private const val DATABASE_PASSWORD = "sKRnKxEizD"

        // Table creation queries
         const val CREATE_PLAYLIST_TABLE = """
            CREATE TABLE IF NOT EXISTS PlaylistDatabase (
                _id BIGINT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(255) NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """

         const val CREATE_PLAYLIST_SONGS_TABLE = """
            CREATE TABLE IF NOT EXISTS PlaylistSongs (
                _id BIGINT PRIMARY KEY AUTO_INCREMENT,
                playlist_id BIGINT,
                song_id BIGINT,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (playlist_id) REFERENCES PlaylistDatabase(_id) ON DELETE CASCADE,
                UNIQUE KEY unique_playlist_song (playlist_id, song_id)
            )
        """
    }

     var connection: Connection? = null

    init {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            createTables()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            throw RuntimeException("Failed to load MySQL JDBC driver")
        }
    }

     fun getConnection(): Connection {
        if (connection == null || connection?.isClosed == true) {
            try {
                println("Attempting to connect to database at: $DATABASE_URL")
                connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD)
                println("Successfully connected to database")
            } catch (e: SQLException) {
                println("Database connection failed!")
                println("Error Message: ${e.message}")
                println("SQL State: ${e.sqlState}")
                println("Error Code: ${e.errorCode}")
                e.printStackTrace()
                throw RuntimeException("Failed to connect to database: ${e.message}")
            }
        }
        return connection!!
    }

     fun createTables() {
        try {
            getConnection().use { conn ->
                conn.createStatement().use { statement ->
                    statement.execute(CREATE_PLAYLIST_TABLE)
                    statement.execute(CREATE_PLAYLIST_SONGS_TABLE)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            throw RuntimeException("Failed to create tables")
        }
    }

    // Create a new playlist
    fun createPlaylist(name: String): Long {
        val query = "INSERT INTO PlaylistDatabase (name) VALUES (?)"
        try {
            getConnection().use { conn ->
                conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS).use { stmt ->
                    stmt.setString(1, name)
                    stmt.executeUpdate()

                    val rs = stmt.generatedKeys
                    if (rs.next()) {
                        return rs.getLong(1)
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return -1
    }

    // Add song to playlist
    fun addSongToPlaylist(playlistId: Long, songId: Long): Boolean {
        val query = "INSERT INTO PlaylistSongs (playlist_id, song_id) VALUES (?, ?)"
        try {
            getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setLong(1, playlistId)
                    stmt.setLong(2, songId)
                    return stmt.executeUpdate() > 0
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }

    // Get all playlists
    fun getAllPlaylists(): List<PlaylistData> {
        val playlists = mutableListOf<PlaylistData>()
        val query = "SELECT * FROM PlaylistDatabase ORDER BY timestamp DESC"
        
        try {
            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(query).use { rs ->
                        while (rs.next()) {
                            playlists.add(
                                PlaylistData(
                                    _id = rs.getLong("_id"),
                                    name = rs.getString("name"),
                                    timestamp = rs.getTimestamp("timestamp").toString()
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return playlists
    }

    // Get songs in a playlist
    fun getPlaylistSongs(playlistId: Long): List<Long> {
        val songIds = mutableListOf<Long>()
        val query = "SELECT song_id FROM PlaylistSongs WHERE playlist_id = ? ORDER BY timestamp DESC"
        
        try {
            getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setLong(1, playlistId)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            songIds.add(rs.getLong("song_id"))
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return songIds
    }

    // Delete a playlist
    fun deletePlaylist(playlistId: Long): Boolean {
        val query = "DELETE FROM PlaylistDatabase WHERE _id = ?"
        try {
            getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setLong(1, playlistId)
                    return stmt.executeUpdate() > 0
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }

    // Remove song from playlist
    fun removeSongFromPlaylist(playlistId: Long, songId: Long): Boolean {
        val query = "DELETE FROM PlaylistSongs WHERE playlist_id = ? AND song_id = ?"
        try {
            getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setLong(1, playlistId)
                    stmt.setLong(2, songId)
                    return stmt.executeUpdate() > 0
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }

    // Update playlist name
    fun updatePlaylistName(playlistId: Long, newName: String): Boolean {
        val query = "UPDATE PlaylistDatabase SET name = ? WHERE _id = ?"
        try {
            getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, newName)
                    stmt.setLong(2, playlistId)
                    return stmt.executeUpdate() > 0
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }

    // Check if song exists in playlist
    fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean {
        val query = "SELECT COUNT(*) FROM PlaylistSongs WHERE playlist_id = ? AND song_id = ?"
        try {
            getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setLong(1, playlistId)
                    stmt.setLong(2, songId)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            return rs.getInt(1) > 0
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }

    // Close database connection
    fun closeConnection() {
        try {
            connection?.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}

fun main(){
    val db = MusicPlayerDatabaseHelper()

    println(db.getAllPlaylists())
}

data class PlaylistData(
    val _id: Long,
    val name: String,
    val timestamp: String
)

data class PlaylistSongData(
    val _id: Long,
    val playlist_id: Long,
    val song_id: Long,
    val timestamp: String
)
