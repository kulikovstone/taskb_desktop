package com.mkulikov.taskb

import com.mkulikov.taskb.storage.AppDatabase
import com.mkulikov.taskb.storage.ApplicationSettingsTable
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
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
    fun `first launch creates database and shows welcome`() {
        assertFalse(Files.exists(databaseFile))

        assertFalse(settings().hasCompletedWelcome())

        assertTrue(Files.isRegularFile(databaseFile))
    }

    @Test
    fun `completed welcome is remembered after restart`() {
        settings().completeWelcome()
        settings().completeWelcome()

        assertTrue(settings().hasCompletedWelcome())
        transaction(AppDatabase(databaseFile).database) {
            assertEquals(1L, ApplicationSettingsTable.selectAll().count())
        }
    }

    @Test
    fun `settings write failure is reported without changing existing file`() {
        val blockedConfig = Files.writeString(temporaryDirectory.resolve("config"), "existing")

        assertFailsWith<IOException> {
            AppSettings(blockedConfig.resolve("taskb.db")).completeWelcome()
        }

        assertEquals("existing", Files.readString(blockedConfig))
    }
}
