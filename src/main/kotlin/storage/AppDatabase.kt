package com.mkulikov.taskb.storage

import com.mkulikov.taskb.AppPaths
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.statements.StatementType
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection

internal class AppDatabase(file: Path = AppPaths.databaseFile()) {
    private val file = file.toAbsolutePath().normalize()

    // Opening the database is deferred until the caller's IO operation.
    val database: Database by lazy {
        Files.createDirectories(file.parent)
        val sqliteConfig = SQLiteConfig().apply {
            enforceForeignKeys(true)
            setBusyTimeout(5_000)
        }
        val dataSource = SQLiteDataSource(sqliteConfig).apply {
            url = "jdbc:sqlite:$file"
        }
        Database.connect(
            datasource = dataSource,
            databaseConfig = DatabaseConfig {
                defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
            }
        ).also(::migrate)
    }

    private fun migrate(database: Database) {
        transaction(database) {
            val version = exec("PRAGMA user_version") { result ->
                check(result.next()) { "SQLite did not return its schema version" }
                result.getInt(1)
            } ?: error("SQLite did not return its schema version")

            check(version <= SCHEMA_VERSION) {
                "Database schema $version is newer than supported schema $SCHEMA_VERSION"
            }
            // Apply explicit migrations and their version change in the same transaction.
            if (version == 0) {
                SchemaUtils.create(ApplicationSettingsTable)
                exec("PRAGMA user_version = 1", explicitStatementType = StatementType.UPDATE)
            }
        }
    }

    private companion object {
        const val SCHEMA_VERSION = 1
    }
}
