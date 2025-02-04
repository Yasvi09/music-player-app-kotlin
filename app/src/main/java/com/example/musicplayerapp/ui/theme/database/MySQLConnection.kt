package com.example.musicplayerapp.ui.theme.database

import android.util.Log
import com.github.jasync.sql.db.QueryResult
import com.github.jasync.sql.db.mysql.MySQLConnection
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder
import com.github.jasync.sql.db.pool.ConnectionPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MySQLDatabase {
    private var connection: ConnectionPool<MySQLConnection>? = null
    private const val TAG = "MySQLDatabase"

    suspend fun connect() = withContext(Dispatchers.IO) {
        try {
            if (connection == null) {
                connection = MySQLConnectionBuilder.createConnectionPool(
                    "jdbc:mysql://${DatabaseConfig.HOST}:${DatabaseConfig.PORT}/${DatabaseConfig.DATABASE}?" +
                            "user=${DatabaseConfig.USERNAME}&password=${DatabaseConfig.PASSWORD}"
                )

                // Create tables if they don't exist
            }
            Log.d(TAG, "Database connected successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to database: ${e.message}")
            false
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            connection?.disconnect()
            connection = null
            Log.d(TAG, "Database disconnected successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting from database: ${e.message}")
        }
    }

    suspend fun executeQuery(query: String, params: List<Any> = emptyList()): QueryResult? =
        withContext(Dispatchers.IO) {
            try {
                connection?.sendPreparedStatement(query, params)?.get()
            } catch (e: Exception) {
                Log.e(TAG, "Error executing query: ${e.message}")
                null
            }
        }
}