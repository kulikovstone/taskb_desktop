package com.mkulikov.taskb

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AppNavigationUiTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `continue opens home and persists completed welcome`() = runComposeUiTest {
        val file = temporaryDirectory.resolve("taskb.db")
        AppSettings(file).use { settings ->
            setContent { MaterialTheme { TaskBApp(settings) } }
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
        AppSettings(file).use { settings ->
            assertTrue(runBlocking { settings.hasCompletedWelcome() })
        }
    }
}
