package com.mkulikov.taskb

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppSettingsTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `first launch shows welcome without creating settings`() {
        val file = temporaryDirectory.resolve("config/settings.properties")

        assertFalse(AppSettings(file).hasCompletedWelcome())
        assertFalse(Files.exists(file))
    }

    @Test
    fun `completed welcome is remembered after restart`() {
        val file = temporaryDirectory.resolve("config/settings.properties")

        AppSettings(file).completeWelcome()

        assertTrue(AppSettings(file).hasCompletedWelcome())
    }

    @Test
    fun `old folder setting is ignored and replaced after welcome`() {
        val file = temporaryDirectory.resolve("settings.properties")
        val oldDirectory = temporaryDirectory.resolve("missing-data")
        Files.newBufferedWriter(file).use { writer ->
            Properties().apply { setProperty("dataDirectory", oldDirectory.toString()) }.store(writer, null)
        }
        val settings = AppSettings(file)

        assertFalse(settings.hasCompletedWelcome())
        settings.completeWelcome()

        assertTrue(AppSettings(file).hasCompletedWelcome())
        assertFalse(Files.exists(oldDirectory))
        val saved = Properties().apply { Files.newBufferedReader(file).use { load(it) } }
        assertNull(saved.getProperty("dataDirectory"))
    }

    @Test
    fun `settings write failure is reported without changing existing file`() {
        val blockedConfig = Files.writeString(temporaryDirectory.resolve("config"), "existing")

        assertFailsWith<IOException> {
            AppSettings(blockedConfig.resolve("settings.properties")).completeWelcome()
        }

        assertEquals("existing", Files.readString(blockedConfig))
    }

    @Test
    fun `continuing replaces malformed settings`() {
        val file = temporaryDirectory.resolve("settings.properties")
        Files.writeString(file, "welcomeCompleted=\\uINVALID\n")
        val settings = AppSettings(file)

        assertFailsWith<IllegalArgumentException> { settings.hasCompletedWelcome() }
        settings.completeWelcome()

        assertTrue(AppSettings(file).hasCompletedWelcome())
    }
}
