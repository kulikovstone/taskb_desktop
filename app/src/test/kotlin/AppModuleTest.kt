package com.mkulikov.taskb

import com.mkulikov.taskb.core.navigation.IAppNavigator
import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.di.appModule
import com.mkulikov.taskb.feature.settings.api.IAppSettings
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.dsl.koinApplication

class AppModuleTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `container registers features shares settings and closes database`() = runBlocking {
        val file = temporaryDirectory.resolve("taskb.db")
        val application = koinApplication { modules(appModule(file)) }
        val settings = application.koin.get<IAppSettings>()
        try {
            assertSame(settings, application.koin.get<IAppSettings>())
            assertSame(application.koin.get<IAppNavigator>(), application.koin.get<IAppNavigator>())
            assertEquals(4, application.koin.getAll<INavEntry>().size)
            settings.completeWelcome()
        } finally {
            application.close()
        }

        assertFailsWith<IllegalStateException> { settings.hasCompletedWelcome() }
        withAppKoin(file) { reopened ->
            assertTrue(reopened.koin.get<IAppSettings>().hasCompletedWelcome())
        }
    }
}
