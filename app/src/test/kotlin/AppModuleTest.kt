package com.mkulikov.taskb

import com.mkulikov.taskb.core.database.AppSettings
import com.mkulikov.taskb.core.navigation.IAppNavigator
import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.di.appModule
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.dsl.koinApplication
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AppModuleTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `container registers features shares settings and closes database`() = runBlocking {
        val file = temporaryDirectory.resolve("taskb.db")
        val application = koinApplication { modules(appModule(file)) }
        val settings = application.koin.get<AppSettings>()
        try {
            assertSame(settings, application.koin.get<AppSettings>())
            assertSame(application.koin.get<IAppNavigator>(), application.koin.get<IAppNavigator>())
            assertEquals(3, application.koin.getAll<INavEntry>().size)
            settings.completeWelcome()
        } finally {
            application.close()
        }

        assertFailsWith<IllegalStateException> { settings.hasCompletedWelcome() }
        withAppKoin(file) { reopened ->
            assertTrue(reopened.koin.get<AppSettings>().hasCompletedWelcome())
        }
    }
}
