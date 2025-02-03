package com.example.musicplayerapp.ui.theme

import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

class DatabaseConnection {
    private var connection: Connection? = null

    companion object {
        private const val HOST = "sql12.freesqldatabase.com"
        private const val PORT = "3306"
        private const val DATABASE = "sql12760814"
        private const val USER = "sql12760814"
        private const val PASSWORD = "sKRnKxEizD"
    }

    fun connect(): Connection? {
        if (connection?.isClosed == false) {
            println("Existing connection is still valid")
            return connection
        }

        try {
            // Step 1: Load the driver
            println("Loading MySQL driver...")
            Class.forName("com.mysql.cj.jdbc.Driver")
            println("✓ Driver loaded successfully")

            // Step 2: Build connection URL with all necessary parameters
            val url = buildConnectionUrl()
            println("Connecting to URL: $url")

            // Step 3: Set up connection properties
            val props = Properties().apply {
                setProperty("user", USER)
                setProperty("password", PASSWORD)
                setProperty("useSSL", "false")
                setProperty("allowPublicKeyRetrieval", "true")
                setProperty("connectTimeout", "5000")
                setProperty("socketTimeout", "30000")
            }

            // Step 4: Attempt connection
            println("Attempting to establish connection...")
            connection = DriverManager.getConnection(url, props)

            // Step 5: Verify connection
            if (connection?.isValid(5) == true) {
                println("✓ Connection established successfully!")
                testConnection()
                return connection
            } else {
                println("✗ Connection created but validation failed")
                return null
            }

        } catch (e: Exception) {
            handleException(e)
            return null
        }
    }

    private fun buildConnectionUrl(): String {
        return "jdbc:mysql://$HOST:$PORT/$DATABASE?" +
                "useSSL=false&" +
                "allowPublicKeyRetrieval=true&" +
                "serverTimezone=UTC&" +
                "autoReconnect=true"
    }

    private fun testConnection() {
        connection?.createStatement()?.use { stmt ->
            // Test query
            stmt.executeQuery("SELECT 1").use { rs ->
                if (rs.next()) {
                    println("✓ Test query executed successfully")
                }
            }

            // Get server version
            stmt.executeQuery("SELECT VERSION()").use { rs ->
                if (rs.next()) {
                    println("✓ Connected to MySQL version: ${rs.getString(1)}")
                }
            }
        }
    }

    private fun handleException(e: Exception) {
        println("\n=== Connection Error Details ===")
        println("Error type: ${e.javaClass.name}")
        println("Error message: ${e.message}")

        when (e) {
            is ClassNotFoundException -> println("✗ MySQL driver not found. Please check your dependencies.")
            is java.net.SocketTimeoutException -> println("✗ Connection timed out. Server may be busy or network is slow.")
            is java.sql.SQLNonTransientConnectionException -> println("✗ Server refused connection. Check credentials and server status.")
            is java.sql.SQLException -> {
                println("SQL State: ${e.sqlState}")
                println("Error Code: ${e.errorCode}")
                when (e.sqlState) {
                    "28000" -> println("✗ Invalid username or password")
                    "08S01" -> println("✗ Network error. Check your internet connection")
                    "08001" -> println("✗ Cannot connect to server. Server may be down or blocked")
                }
            }
        }
        e.printStackTrace()
    }

    fun disconnect() {
        try {
            connection?.close()
            println("Connection closed successfully")
        } catch (e: Exception) {
            println("Error closing connection: ${e.message}")
        }
    }
}

fun main() {
    val db = DatabaseConnection()
    db.connect()
    // If connection successful, close it
    db.disconnect()
}