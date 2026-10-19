package com.mkulikov.taskb

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.mkulikov.taskb.feature.settings.api.IAppSettings
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.compose.KoinIsolatedContext

@OptIn(ExperimentalTestApi::class)
class AppNavigationUiTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `continue opens home and persists completed welcome`() {
        val file = temporaryDirectory.resolve("taskb.db")
        withAppKoin(file) { application ->
            val settings = application.koin.get<IAppSettings>()
            runComposeUiTest {
                setContent {
                    KoinIsolatedContext(application) { MaterialTheme { TaskBApp(settings) } }
                }
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(hasText("Продолжить")).fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithText("Добро пожаловать в TaskB").assertIsDisplayed()
                onNodeWithText("Продолжить").performClick()
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(hasText("Главный экран")).fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithText("Главный экран").assertIsDisplayed()
            }
        }
        withAppKoin(file) { application ->
            assertTrue(runBlocking { application.koin.get<IAppSettings>().hasCompletedWelcome() })
        }
    }

    @Test
    fun `returning user starts with home`() {
        withAppKoin(temporaryDirectory.resolve("taskb.db")) { application ->
            val settings = application.koin.get<IAppSettings>()
            runBlocking { settings.completeWelcome() }
            runComposeUiTest {
                setContent {
                    KoinIsolatedContext(application) { MaterialTheme { TaskBApp(settings) } }
                }
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(hasText("Главный экран")).fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithText("Главный экран").assertIsDisplayed()
                onNodeWithText("Добро пожаловать в TaskB").assertDoesNotExist()
                val version = onNodeWithText("Версия ${AppVersion.value}").assertIsDisplayed()
                val screenBounds = onRoot().fetchSemanticsNode().boundsInRoot
                val versionBounds = version.fetchSemanticsNode().boundsInRoot
                assertTrue(versionBounds.center.y > screenBounds.top + screenBounds.height * 0.8f)
            }
        }
    }

    @Test
    fun `database error stays on hello and preserves existing file`() {
        val blocked = Files.writeString(temporaryDirectory.resolve("config"), "existing")
        withAppKoin(blocked.resolve("taskb.db")) { application ->
            val settings = application.koin.get<IAppSettings>()
            runComposeUiTest {
                setContent {
                    KoinIsolatedContext(application) { MaterialTheme { TaskBApp(settings) } }
                }
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(hasText("Не удалось прочитать настройки. Нажмите «Продолжить», чтобы начать работу."))
                        .fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithText("Продолжить").performClick()
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(hasText("Не удалось сохранить настройки. Попробуйте ещё раз."))
                        .fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithText("Добро пожаловать в TaskB").assertIsDisplayed()
                onNodeWithText("Главный экран").assertDoesNotExist()
            }
        }
        assertEquals("existing", Files.readString(blocked))
    }
}
