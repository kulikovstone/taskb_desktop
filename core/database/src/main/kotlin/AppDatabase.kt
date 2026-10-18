package com.mkulikov.taskb.core.database

import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import java.nio.file.Files
import java.nio.file.Path

@Database(entities = [ApplicationSetting::class], version = 1)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): AppSettingsDao

    companion object {
        fun open(file: Path = DatabasePaths.databaseFile()): AppDatabase {
            val absoluteFile = file.toAbsolutePath().normalize()
            Files.createDirectories(absoluteFile.parent)
            return Room.databaseBuilder<AppDatabase>(name = absoluteFile.toString())
                .setDriver(ForeignKeySQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        }
    }
}

private class ForeignKeySQLiteDriver : SQLiteDriver {
    private val delegate = BundledSQLiteDriver()

    override fun open(fileName: String): SQLiteConnection {
        val connection = delegate.open(fileName)
        try {
            // This setting belongs to each connection and must precede Room's transactions.
            connection.prepare("PRAGMA foreign_keys = ON").use { it.step() }
            return connection
        } catch (exception: Throwable) {
            connection.close()
            throw exception
        }
    }
}
