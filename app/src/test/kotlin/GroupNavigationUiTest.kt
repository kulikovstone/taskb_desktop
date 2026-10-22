package com.mkulikov.taskb

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.core.database.AppDatabase
import com.mkulikov.taskb.feature.group.api.database.GroupEntity
import com.mkulikov.taskb.feature.settings.api.IAppSettings
import com.mkulikov.taskb.core.navigation.IAppNavigator
import com.mkulikov.taskb.feature.group.api.GroupRoute
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
    private lateinit var legacyNavigator: IAppNavigator

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

    @Test
    fun `wide layout keeps the list visible and replaces the right pane`() {
        withAppKoin(directory.resolve("wide-groups.db")) { application ->
            runBlocking {
                application.koin.get<IAppSettings>().completeWelcome()
                val dao = application.koin.get<AppDatabase>().groupDao()
                dao.insert(GroupEntity(name = "Work", color = Palette.Blue.s400.toArgb()))
                dao.insert(GroupEntity(name = "Personal", color = Palette.Green.s400.toArgb()))
            }
            runSkikoComposeUiTest(size = Size(1200f, 900f)) {
                setApp(application, darkTheme = false)
                openGroups()
                waitForText("Work")
                onNodeWithText("Select a group or create a new one.").assertIsDisplayed()
                groupRow("Work").performClick()
                waitForField("Work")
                groupRow("Work").assertIsSelected()
                onNodeWithText("Groups").assertIsDisplayed()
                onNode(hasSetTextAction()).performTextReplacement("Unsaved work")
                groupRow("Work").performClick()
                onNode(hasSetTextAction()).assertTextEquals("Unsaved work")
                groupRow("Personal").performClick()
                waitForField("Personal")
                groupRow("Personal").assertIsSelected()
                screenshot("groups-wide-edit-light")
                onNodeWithContentDescription("Add group").performClick()
                waitForText("New group")
                onNodeWithContentDescription("Close group").performClick()
                waitForText("Select a group or create a new one.")
                onNodeWithText("Group").assertDoesNotExist()
                onNodeWithContentDescription("Add group").performClick()
                onNode(hasSetTextAction()).performTextInput("Projects")
                saveGroup()
                waitForText("Projects")
                onNodeWithText("Select a group or create a new one.").assertIsDisplayed()
                groupRow("Projects").performClick()
                waitForField("Projects")
                onNodeWithContentDescription("Back").performClick()
                waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText("Tasks") and hasClickAction()).fetchSemanticsNodes().isNotEmpty() }
                openGroups()
                onNodeWithText("Select a group or create a new one.").assertIsDisplayed()
            }
        }
    }

    @Test
    fun `resizing keeps add and edit drafts and switches at the breakpoint`() {
        withAppKoin(directory.resolve("resizable-groups.db")) { application ->
            runBlocking {
                application.koin.get<IAppSettings>().completeWelcome()
                application.koin.get<AppDatabase>().groupDao().insert(GroupEntity(name = "Work", color = Palette.Blue.s400.toArgb()))
            }
            val width = mutableStateOf(400.dp)
            runSkikoComposeUiTest(size = Size(1200f, 900f)) {
                setApp(application, darkTheme = true, contentWidth = width)
                openGroups()
                waitForText("Work")
                groupRow("Work").performClick()
                waitForField("Work")
                onNode(hasSetTextAction()).performTextReplacement("Work draft")
                onNodeWithText("Green").performClick()
                runOnIdle { width.value = 1200.dp }
                waitForText("Groups")
                onNode(hasSetTextAction()).assertTextEquals("Work draft")
                onNodeWithText("Green").assertIsSelected()
                groupRow("Work").assertIsSelected()
                screenshot("groups-wide-edit-dark")
                runOnIdle { width.value = 400.dp }
                waitForIdle()
                onNodeWithText("Groups").assertDoesNotExist()
                onNode(hasSetTextAction()).assertTextEquals("Work draft")
                onNodeWithContentDescription("Back").performClick()
                waitForText("Groups")
                onNodeWithContentDescription("Add group").performClick()
                onNode(hasSetTextAction()).performTextInput("Travel draft")
                onNodeWithText("Red").performClick()
                runOnIdle { width.value = 840.dp }
                waitForText("Groups")
                onNodeWithContentDescription("Close group").assertIsDisplayed()
                onNode(hasSetTextAction()).assertTextEquals("Travel draft")
                onNodeWithText("Red").assertIsSelected()
                runOnIdle { width.value = 839.dp }
                waitForIdle()
                onNodeWithText("Groups").assertDoesNotExist()
                onNodeWithContentDescription("Back").assertIsDisplayed()
                onNode(hasSetTextAction()).assertTextEquals("Travel draft")
                runOnIdle { width.value = 1200.dp }
                waitForText("Groups")
                onNodeWithContentDescription("Close group").performClick()
                waitForText("Select a group or create a new one.")
                onNodeWithContentDescription("Add group").performClick()
                onNodeWithText("Enter group name").assertIsDisplayed()
                onNodeWithContentDescription("Save group").assertIsNotEnabled()
                onNodeWithContentDescription("Close group").performClick()
            }
            val groups = runBlocking { application.koin.get<AppDatabase>().groupDao().observeGroups().first() }
            assertEquals("Work", groups.single().name)
            assertEquals(Palette.Blue.s400.toArgb(), groups.single().color)
        }
    }

    private fun SkikoComposeUiTest.groupRow(name: String) = onNode(hasText(name) and hasClickAction() and !hasSetTextAction())

    private suspend fun SkikoComposeUiTest.waitForField(name: String) {
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasSetTextAction() and hasText(name)).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun SkikoComposeUiTest.setApp(application: KoinApplication, darkTheme: Boolean, contentWidth: State<Dp>? = null) {
        legacyNavigator = application.koin.get()
        val settings = application.koin.get<IAppSettings>()
        setContent {
            KoinIsolatedContext(application) {
                TaskBTheme(darkTheme) {
                    Box(if (contentWidth == null) Modifier.fillMaxSize() else Modifier.width(contentWidth.value).fillMaxHeight()) {
                        TaskBApp(settings)
                    }
                }
            }
        }
    }

    private suspend fun SkikoComposeUiTest.saveGroup() {
        waitUntil(timeoutMillis = 10_000) {
            !onNodeWithContentDescription("Save group").fetchSemanticsNode().config.contains(SemanticsProperties.Disabled)
        }
        onNodeWithContentDescription("Save group").performClick()
    }

    private suspend fun SkikoComposeUiTest.openGroups() {
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText("Groups") and hasClickAction()).fetchSemanticsNodes().isNotEmpty() }
        // Keep regression coverage of feature:group after Home starts opening feature:task.
        runOnIdle { legacyNavigator.navigate(GroupRoute) }
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
