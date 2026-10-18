package com.mkulikov.taskb

import com.mkulikov.taskb.di.appModule
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.dsl.koinApplication
import java.nio.file.Path
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AppModuleTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `container shares settings and closes their database`() = runBlocking {
        val file = temporaryDirectory.resolve("taskb.db")
        val application = koinApplication { modules(appModule(file)) }
        val settings = application.koin.get<AppSettings>()
        try {
            assertSame(settings, application.koin.get<AppSettings>())
            settings.completeWelcome()
        } finally {
            application.close()
        }

        assertFailsWith<IllegalStateException> { settings.hasCompletedWelcome() }
        AppSettings(file).use { assertTrue(it.hasCompletedWelcome()) }
    }
}
