package com.mkulikov.taskb

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.mkulikov.taskb.feature.settings.api.IAppSettings
import com.mkulikov.taskb.ui.theme.TaskBTheme
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
                    KoinIsolatedContext(application) { TaskBTheme(darkTheme = false) { TaskBApp(settings) } }
                }
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(hasText("Продолжить")).fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithText("Добро пожаловать в TaskB").assertIsDisplayed()
                onNodeWithText("Продолжить").performClick()
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(hasText("Задачи") and hasClickAction()).fetchSemanticsNodes().isNotEmpty()
                }
                onNode(hasText("Задачи") and hasClickAction()).assertIsDisplayed()
                onNode(hasText("Группы") and hasClickAction()).assertIsDisplayed()
                onNodeWithText("Главный экран").assertDoesNotExist()
            }
        }
        withAppKoin(file) { application ->
            assertTrue(runBlocking { application.koin.get<IAppSettings>().hasCompletedWelcome() })
        }
    }

    @Test
    fun `returning user starts with home and can open tasks and groups and return`() {
        withAppKoin(temporaryDirectory.resolve("taskb.db")) { application ->
            val settings = application.koin.get<IAppSettings>()
            runBlocking { settings.completeWelcome() }
            runComposeUiTest {
                setContent {
                    KoinIsolatedContext(application) { TaskBTheme(darkTheme = true) { TaskBApp(settings) } }
                }
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(hasText("Задачи") and hasClickAction()).fetchSemanticsNodes().isNotEmpty()
                }
                onNode(hasText("Задачи") and hasClickAction()).assertIsDisplayed()
                onNode(hasText("Группы") and hasClickAction()).assertIsDisplayed()
                onNodeWithText("Главный экран").assertDoesNotExist()
                onNodeWithText("Добро пожаловать в TaskB").assertDoesNotExist()
                val version = onNodeWithText("Версия ${AppVersion.value}").assertIsDisplayed()
                val screenBounds = onRoot().fetchSemanticsNode().boundsInRoot
                val versionBounds = version.fetchSemanticsNode().boundsInRoot
                assertTrue(versionBounds.center.y > screenBounds.top + screenBounds.height * 0.8f)

                onNode(hasText("Задачи") and hasClickAction()).performClick()
                onNodeWithText("Назад").assertIsDisplayed()
                onNodeWithText("Задачи").assertIsDisplayed()
                onNode(hasText("Группы") and hasClickAction()).assertDoesNotExist()
                onNodeWithText("Назад").performClick()
                onNode(hasText("Задачи") and hasClickAction()).assertIsDisplayed()
                onNode(hasText("Группы") and hasClickAction()).assertIsDisplayed()
                onNodeWithText("Версия ${AppVersion.value}").assertIsDisplayed()

                onNode(hasText("Группы") and hasClickAction()).performClick()
                onNodeWithText("Назад").assertIsDisplayed()
                onNodeWithText("Группы").assertIsDisplayed()
                onNode(hasText("Задачи") and hasClickAction()).assertDoesNotExist()
                onNodeWithText("Назад").performClick()
                onNode(hasText("Задачи") and hasClickAction()).assertIsDisplayed()
                onNode(hasText("Группы") and hasClickAction()).assertIsDisplayed()
                onNodeWithText("Версия ${AppVersion.value}").assertIsDisplayed()
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
                    KoinIsolatedContext(application) { TaskBTheme(darkTheme = false) { TaskBApp(settings) } }
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
                onNode(hasText("Задачи") and hasClickAction()).assertDoesNotExist()
                onNode(hasText("Группы") and hasClickAction()).assertDoesNotExist()
            }
        }
        assertEquals("existing", Files.readString(blocked))
    }
}
