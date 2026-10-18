package com.mkulikov.taskb

import androidx.room3.useReaderConnection
import com.mkulikov.taskb.storage.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppSettingsTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    private val databaseFile get() = temporaryDirectory.resolve("data/taskb.db")
    private fun settings() = AppSettings(databaseFile)

    @Test
    fun `first launch creates database and shows welcome`() = runBlocking {
        assertFalse(Files.exists(databaseFile))

        settings().use { assertFalse(it.hasCompletedWelcome()) }

        assertTrue(Files.isRegularFile(databaseFile))
    }

    @Test
    fun `completed welcome is remembered after restart`() = runBlocking {
        settings().use {
            it.completeWelcome()
            it.completeWelcome()
        }

        settings().use { assertTrue(it.hasCompletedWelcome()) }
        AppDatabase.open(databaseFile).useDatabase { database ->
            database.useReaderConnection { connection ->
                connection.usePrepared("SELECT COUNT(*) FROM application_settings") {
                    assertTrue(it.step())
                    assertEquals(1L, it.getLong(0))
                }
            }
        }
    }

    @Test
    fun `settings write failure is reported without changing existing file`() = runBlocking {
        val blockedConfig = Files.writeString(temporaryDirectory.resolve("config"), "existing")

        AppSettings(blockedConfig.resolve("taskb.db")).use { settings ->
            assertFailsWith<IOException> { settings.completeWelcome() }
        }

        assertEquals("existing", Files.readString(blockedConfig))
    }
}
