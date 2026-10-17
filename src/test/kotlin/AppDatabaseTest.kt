package com.mkulikov.taskb

import com.mkulikov.taskb.storage.AppDatabase
import com.mkulikov.taskb.storage.ApplicationSettingsTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppDatabaseTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `schema is initialized once and data survives reopening`() {
        val file = temporaryDirectory.resolve("taskb.db")
        transaction(AppDatabase(file).database) {
            ApplicationSettingsTable.insert {
                it[key] = "example"
                it[value] = "saved"
            }
        }

        transaction(AppDatabase(file).database) {
            assertEquals("saved", ApplicationSettingsTable.selectAll().single()[ApplicationSettingsTable.value])
            assertEquals(1, exec("PRAGMA user_version") { it.next(); it.getInt(1) })
        }
    }

    @Test
    fun `foreign keys protect relationships on every new connection`() {
        val storage = AppDatabase(temporaryDirectory.resolve("taskb.db"))
        transaction(storage.database) {
            SchemaUtils.create(Parents, Children)
        }

        repeat(2) {
            transaction(storage.database) {
                assertEquals(1, exec("PRAGMA foreign_keys") { it.next(); it.getInt(1) })
            }
            assertFailsWith<SQLException> {
                transaction(storage.database) {
                    maxAttempts = 1
                    Children.insert {
                        it[id] = 1
                        it[parentId] = 999
                    }
                }
            }
        }
        transaction(storage.database) {
            assertEquals(0L, Children.selectAll().count())
        }
    }

    @Test
    fun `failed transaction leaves no partial changes`() {
        val storage = AppDatabase(temporaryDirectory.resolve("taskb.db"))

        assertFailsWith<IllegalStateException> {
            transaction(storage.database) {
                ApplicationSettingsTable.insert {
                    it[key] = "incomplete"
                    it[value] = "must be rolled back"
                }
                error("Simulated failure")
            }
        }

        transaction(storage.database) {
            assertEquals(0L, ApplicationSettingsTable.selectAll().count())
        }
    }

    @Test
    fun `newer database schema is rejected without changing existing data`() {
        val file = temporaryDirectory.resolve("newer.db")
        DriverManager.getConnection("jdbc:sqlite:$file").use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("CREATE TABLE future_data (value TEXT NOT NULL)")
                statement.execute("INSERT INTO future_data VALUES ('preserved')")
                statement.execute("PRAGMA user_version = 2")
            }
        }

        assertFailsWith<IllegalStateException> {
            AppDatabase(file).database
        }

        DriverManager.getConnection("jdbc:sqlite:$file").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT value FROM future_data").use { result ->
                    result.next()
                    assertEquals("preserved", result.getString(1))
                }
                statement.executeQuery("PRAGMA user_version").use { result ->
                    result.next()
                    assertEquals(2, result.getInt(1))
                }
            }
        }
    }

    private object Parents : Table("test_parents") {
        val id = integer("id")
        override val primaryKey = PrimaryKey(id)
    }

    private object Children : Table("test_children") {
        val id = integer("id")
        val parentId = reference("parent_id", Parents.id, onDelete = ReferenceOption.RESTRICT)
        override val primaryKey = PrimaryKey(id)
    }
}
