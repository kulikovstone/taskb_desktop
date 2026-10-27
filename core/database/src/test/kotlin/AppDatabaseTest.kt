package com.mkulikov.taskb.core.database

import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection
import androidx.room3.withWriteTransaction
import androidx.sqlite.SQLiteException
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.mkulikov.taskb.feature.settings.api.database.ApplicationSetting
import com.mkulikov.taskb.feature.task.api.database.GroupEntity
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppDatabaseTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `schema is initialized once and data survives reopening`() = runBlocking {
        val file = temporaryDirectory.resolve("taskb.db")
        AppDatabase.open(file).useDatabase { database ->
            database.settingsDao().upsert(ApplicationSetting("example", "saved"))
        }

        AppDatabase.open(file).useDatabase { database ->
            assertEquals("saved", database.settingsDao().getValue("example"))
            assertEquals(3L, database.queryLong("PRAGMA user_version"))
        }
    }

    @Test
    fun `existing version two database retains groups and settings`() = runBlocking {
        val file = temporaryDirectory.resolve("existing.db")
        BundledSQLiteDriver().open(file.toString()).use { connection ->
            connection.execSQL("CREATE TABLE application_settings (`key` TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL)")
            connection.execSQL(
                "CREATE TABLE `groups` (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT NOT NULL COLLATE NOCASE, color INTEGER NOT NULL)"
            )
            connection.execSQL("CREATE UNIQUE INDEX index_groups_name ON `groups` (name)")
            connection.execSQL("INSERT INTO application_settings VALUES ('welcomeCompleted', 'true')")
            connection.execSQL("INSERT INTO `groups` VALUES (7, 'Work', 123)")
            connection.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
            connection.execSQL("INSERT INTO room_master_table VALUES (42, '1f6fcf3a57abd711d5a91fe555af4949')")
            connection.execSQL("PRAGMA user_version = 2")
        }

        AppDatabase.open(file).useDatabase { database ->
            assertEquals(GroupEntity(7, "Work", 123), database.groupDao().getById(7))
            assertEquals("true", database.settingsDao().getValue("welcomeCompleted"))
            assertEquals(3L, database.queryLong("PRAGMA user_version"))
        }
    }

    @Test
    fun `foreign keys protect relationships on every new connection`() = runBlocking {
        val file = temporaryDirectory.resolve("taskb.db")
        AppDatabase.open(file).useDatabase { database ->
            database.useWriterConnection { connection ->
                connection.usePrepared("CREATE TABLE test_parents (id INTEGER NOT NULL PRIMARY KEY)") { it.step() }
                connection.usePrepared(
                    "CREATE TABLE test_children (id INTEGER NOT NULL PRIMARY KEY, " +
                        "parent_id INTEGER NOT NULL REFERENCES test_parents(id) ON DELETE RESTRICT)"
                ) { it.step() }
            }
        }

        repeat(2) {
            AppDatabase.open(file).useDatabase { database ->
                assertEquals(1L, database.queryLong("PRAGMA foreign_keys"))
                assertFailsWith<SQLiteException> {
                    database.useWriterConnection { connection ->
                        connection.usePrepared("INSERT INTO test_children VALUES (1, 999)") { it.step() }
                    }
                }
                assertEquals(0L, database.queryLong("SELECT COUNT(*) FROM test_children"))
            }
        }
    }

    @Test
    fun `failed transaction leaves no partial changes`() = runBlocking {
        AppDatabase.open(temporaryDirectory.resolve("taskb.db")).useDatabase { database ->
            assertFailsWith<IllegalStateException> {
                database.withWriteTransaction {
                    database.settingsDao().upsert(ApplicationSetting("incomplete", "must be rolled back"))
                    error("Simulated failure")
                }
            }

            assertEquals(0L, database.queryLong("SELECT COUNT(*) FROM application_settings"))
        }
    }

    @Test
    fun `newer database schema is rejected without changing existing data`() = runBlocking {
        val file = temporaryDirectory.resolve("newer.db")
        val driver = BundledSQLiteDriver()
        driver.open(file.toString()).use { connection ->
            connection.execSQL("CREATE TABLE future_data (value TEXT NOT NULL)")
            connection.execSQL("INSERT INTO future_data VALUES ('preserved')")
            connection.execSQL("PRAGMA user_version = 4")
        }

        AppDatabase.open(file).useDatabase { database ->
            assertFailsWith<IllegalStateException> {
                database.settingsDao().getValue("example")
            }
        }

        driver.open(file.toString()).use { connection ->
            connection.prepare("SELECT value FROM future_data").use {
                assertTrue(it.step())
                assertEquals("preserved", it.getText(0))
            }
            connection.prepare("PRAGMA user_version").use {
                assertTrue(it.step())
                assertEquals(4L, it.getLong(0))
            }
        }
    }

    @Test
    fun `version one database is recreated without a data migration`() = runBlocking {
        val file = temporaryDirectory.resolve("old.db")
        BundledSQLiteDriver().open(file.toString()).use { connection ->
            connection.execSQL("CREATE TABLE application_settings (`key` TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL)")
            connection.execSQL("INSERT INTO application_settings VALUES ('welcomeCompleted', 'true')")
            connection.execSQL("PRAGMA user_version = 1")
        }
        AppDatabase.open(file).useDatabase { database ->
            assertNull(database.settingsDao().getValue("welcomeCompleted"))
            assertEquals(3L, database.queryLong("PRAGMA user_version"))
            assertEquals(0L, database.queryLong("SELECT COUNT(*) FROM `groups`"))
        }
    }

    private suspend fun AppDatabase.queryLong(sql: String): Long = useReaderConnection { connection ->
        connection.usePrepared(sql) {
            check(it.step()) { "Query returned no rows: $sql" }
            it.getLong(0)
        }
    }
}
