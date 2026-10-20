package com.mkulikov.taskb

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import com.mkulikov.taskb.core.database.AppDatabase
import com.mkulikov.taskb.feature.group.api.database.GroupEntity
import com.mkulikov.taskb.feature.settings.api.IAppSettings
import com.mkulikov.taskb.ui.theme.Palette
import com.mkulikov.taskb.ui.theme.TaskBTheme
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.compose.KoinIsolatedContext
import org.koin.core.KoinApplication

@OptIn(ExperimentalTestApi::class)
class GroupNavigationUiTest {
    @TempDir lateinit var directory: Path

    @Test
    fun `create edit reopen cancel deletion and delete a group`() {
        val file = directory.resolve("groups.db")
        withAppKoin(file) { application ->
            runBlocking { application.koin.get<IAppSettings>().completeWelcome() }
            runSkikoComposeUiTest(size = Size(400f, 900f)) {
                setApp(application, darkTheme = false)
                openGroups()
                onNodeWithText("No groups yet.").assertIsDisplayed()
                onNodeWithContentDescription("Add group").performClick()
                onNodeWithText("New group").assertIsDisplayed()
                onNodeWithText("Statistics").assertDoesNotExist()
                onNodeWithText("Delete").assertDoesNotExist()
                onNodeWithContentDescription("Save group").assertIsNotEnabled()
                onNodeWithText("Light green").performClick()
                onNodeWithText("Light green").assertIsSelected()
                screenshot("new-group-light")
                onNode(hasSetTextAction()).performTextInput(" Home ")
                saveGroup()
                waitForText("Home")
                onNodeWithText("Home").performClick()
                waitForText("Group")
                waitUntil(timeoutMillis = 10_000) { onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
                onNodeWithText("Statistics").assertIsDisplayed()
                onNodeWithContentDescription("Save group").assertIsNotEnabled()
                onNode(hasSetTextAction()).performTextReplacement(" Personal ")
                onNodeWithText("Red").performClick()
                saveGroup()
                waitForText("Personal")
                onNodeWithText("Home").assertDoesNotExist()
            }
        }

        withAppKoin(file) { application ->
            val groups = runBlocking { application.koin.get<AppDatabase>().groupDao().observeGroups().first() }
            assertEquals("Personal", groups.single().name)
            assertEquals(Palette.Red.s400.toArgb(), groups.single().color)
            runSkikoComposeUiTest(size = Size(432f, 600f)) {
                setApp(application, darkTheme = false)
                openGroups()
                waitForText("Personal")
                onNodeWithText("Personal").performClick()
                waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText("Delete")).fetchSemanticsNodes().isNotEmpty() }
                onNodeWithText("Delete").performScrollTo().performClick()
                onNodeWithText("Delete group").assertIsDisplayed()
                onNodeWithText("Cancel").performClick()
                onNodeWithText("Delete group").assertDoesNotExist()
                onNodeWithText("Delete").performScrollTo().performClick()
                onNode(hasText("Delete") and hasAnyAncestor(isDialog())).performClick()
                waitForText("No groups yet.")
                onNodeWithText("Personal").assertDoesNotExist()
            }
            assertEquals(emptyList(), runBlocking { application.koin.get<AppDatabase>().groupDao().observeGroups().first() })
        }
    }

    @Test
    fun `dark form keeps draft after a name conflict and discards it after back`() {
        withAppKoin(directory.resolve("groups.db")) { application ->
            runBlocking {
                application.koin.get<IAppSettings>().completeWelcome()
                application.koin.get<AppDatabase>().groupDao().insert(GroupEntity(name = "Work", color = Palette.Blue.s400.toArgb()))
            }
            runSkikoComposeUiTest(size = Size(400f, 900f)) {
                setApp(application, darkTheme = true)
                openGroups()
                waitForText("Work")
                onNodeWithContentDescription("Add group").performClick()
                onNodeWithText("Light green").performClick()
                screenshot("new-group-dark")
                onNode(hasSetTextAction()).performTextInput(" work ")
                saveGroup()
                waitForText("Name Conflict")
                onNodeWithText("Group with this name already exists.").assertIsDisplayed()
                onNodeWithText("OK").performClick()
                onNode(hasSetTextAction()).assertIsDisplayed()
                onNodeWithText("Light green").assertIsSelected()
                onNodeWithContentDescription("Back").performClick()
                waitForText("Work")
                onNodeWithContentDescription("Add group").performClick()
                onNodeWithContentDescription("Save group").assertIsNotEnabled()
                onNodeWithText("Enter group name").assertIsDisplayed()
                onNodeWithText("Transparent").performClick()
                onNodeWithText("Transparent").assertIsSelected()
            }
            assertEquals(1, runBlocking { application.koin.get<AppDatabase>().groupDao().observeGroups().first().size })
        }
    }

    private fun SkikoComposeUiTest.setApp(application: KoinApplication, darkTheme: Boolean) {
        val settings = application.koin.get<IAppSettings>()
        setContent { KoinIsolatedContext(application) { TaskBTheme(darkTheme) { TaskBApp(settings) } } }
    }

    private suspend fun SkikoComposeUiTest.saveGroup() {
        waitUntil(timeoutMillis = 10_000) {
            !onNodeWithContentDescription("Save group").fetchSemanticsNode().config.contains(SemanticsProperties.Disabled)
        }
        onNodeWithContentDescription("Save group").performClick()
    }

    private suspend fun SkikoComposeUiTest.openGroups() {
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText("Groups") and hasClickAction()).fetchSemanticsNodes().isNotEmpty() }
        onNode(hasText("Groups") and hasClickAction()).performClick()
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText("Groups")).fetchSemanticsNodes().isNotEmpty() }
    }

    private suspend fun SkikoComposeUiTest.waitForText(text: String) {
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun SkikoComposeUiTest.screenshot(name: String) {
        val output = Path.of("build/reports/screenshots")
        Files.createDirectories(output)
        ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", output.resolve("$name.png").toFile())
    }
}
