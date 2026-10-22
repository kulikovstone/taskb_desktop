package com.mkulikov.taskb

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.core.database.AppDatabase
import com.mkulikov.taskb.feature.group.api.database.GroupEntity
import com.mkulikov.taskb.feature.settings.api.IAppSettings
import com.mkulikov.taskb.ui.theme.Palette
import com.mkulikov.taskb.ui.theme.TaskBTheme
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.compose.KoinIsolatedContext
import org.koin.core.KoinApplication

@OptIn(ExperimentalTestApi::class)
class TaskGroupsNavigationUiTest {
    @TempDir lateinit var directory: Path

    @Test
    fun `home opens new compact screens with persisted CRUD and confirmation dialogs`() {
        val file = directory.resolve("task-groups.db")
        withAppKoin(file) { application ->
            runBlocking { application.koin.get<IAppSettings>().completeWelcome() }
            runSkikoComposeUiTest(size = Size(400f, 980f)) {
                setApp(application, false)
                openGroups()
                waitText("No groups yet.")
                onNodeWithContentDescription("Search groups").assertIsDisplayed()
                onNodeWithContentDescription("Add group").performClick()
                onNodeWithContentDescription("Save group").assertIsNotEnabled()
                onNodeWithText("Planned").assertDoesNotExist()
                onNodeWithContentDescription("Group name").assertIsFocused()
                onNodeWithContentDescription("Clear group name").assertDoesNotExist()
                val initialNameBounds = onNodeWithContentDescription("Group name").fetchSemanticsNode().boundsInRoot
                onNodeWithContentDescription("Group name").performTextInput("Draft")
                val editingNameBounds = onNodeWithContentDescription("Group name").fetchSemanticsNode().boundsInRoot
                val clearNameBounds = onNodeWithContentDescription("Clear group name").fetchSemanticsNode().boundsInRoot
                assertTrue(editingNameBounds.right < clearNameBounds.left)
                assertEquals(initialNameBounds.left, editingNameBounds.left, 0.5f)
                assertEquals(initialNameBounds.right, clearNameBounds.right, 0.5f)
                screenshot("task-groups-compact-field-focused-light")
                onNodeWithContentDescription("Clear group name").performClick()
                waitText("Enter group name")
                onNodeWithContentDescription("Group name").assertIsFocused()
                onNodeWithContentDescription("Clear group name").assertDoesNotExist()
                assertEquals(initialNameBounds.width, onNodeWithContentDescription("Group name").fetchSemanticsNode().boundsInRoot.width, 0.5f)
                onNodeWithContentDescription("Save group").assertIsNotEnabled()
                onNodeWithContentDescription("Group name").performTextInput(" Travel ")
                onNodeWithContentDescription("Orange").performClick()
                onNodeWithContentDescription("Orange").assertIsSelected()
                onNodeWithContentDescription("Clear group name").assertDoesNotExist()
                assertEquals(initialNameBounds.width, onNodeWithContentDescription("Group name").fetchSemanticsNode().boundsInRoot.width, 0.5f)
                screenshot("task-groups-compact-new-light")
                saveGroup()
                waitText("Travel")
                onNodeWithText("Travel").performClick()
                waitText("Planned")
                onNodeWithContentDescription("Save group").assertIsNotEnabled()
                onNodeWithContentDescription("Group name").performTextReplacement("Work")
                onNodeWithContentDescription("Blue").performClick()
                screenshot("task-groups-compact-edit-light")
                saveGroup()
                waitText("Work")
                onNodeWithContentDescription("Add group").performClick()
                onNodeWithContentDescription("Group name").performTextInput("work")
                saveGroup()
                waitText("Name conflict")
                onNodeWithText("OK").performClick()
                onNodeWithContentDescription("Group name").assertTextEquals("work")
                onNodeWithContentDescription("Back").performClick()
                waitText("Work")
                onNodeWithText("Work").performClick()
                waitText("Planned")
                onNodeWithText("Delete with tasks").assertDoesNotExist()
                onNodeWithText("Delete").performClick()
                waitText("Delete group")
                onNodeWithText("Cancel").performClick()
                onNodeWithText("Delete").performClick()
                waitText("Delete group")
                onNode(hasText("Delete") and hasAnyAncestor(isDialog())).performClick()
                waitText("No groups yet.")
            }
            assertEquals(emptyList(), runBlocking { application.koin.get<AppDatabase>().groupDao().observeGroups().first() })
        }
    }

    @Test
    fun `desktop search filters and resizing preserve selection and drafts across the breakpoint`() {
        withAppKoin(directory.resolve("resize.db")) { application ->
            seedGroups(application)
            val width = mutableStateOf(400.dp)
            runSkikoComposeUiTest(size = Size(1280f, 980f)) {
                setApp(application, false, width)
                openGroups()
                waitText("8 groups")
                screenshot("task-groups-compact-list-light")
                onNodeWithText("Work").performClick()
                waitText("Planned")
                onNodeWithContentDescription("Group name").performTextReplacement("Work draft")
                onNodeWithContentDescription("Green").performClick()
                runOnIdle { width.value = 300.dp }
                waitForIdle()
                val deleteBounds = onNodeWithText("Delete").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
                val saveBounds = onNodeWithContentDescription("Save group").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
                assertTrue(saveBounds.top > deleteBounds.bottom)
                assertEquals(onNodeWithContentDescription("Group name").fetchSemanticsNode().boundsInRoot.right, saveBounds.right, 0.5f)
                assertEquals(deleteBounds.right, saveBounds.right, 0.5f)
                screenshot("task-groups-compact-actions-wrapped-light")
                runOnIdle { width.value = 1280.dp }
                waitText("Save changes")
                onNodeWithContentDescription("Group name").assertTextEquals("Work draft")
                onNodeWithContentDescription("Green").assertIsSelected()
                onNode(hasText("Work") and hasClickAction()).assertIsSelected()
                screenshot("task-groups-wide-edit-light")
                onNodeWithContentDescription("Search groups").performTextInput("hea")
                waitText("1 of 8 groups")
                onNodeWithContentDescription("Group name").assertTextEquals("Work draft")
                onNodeWithContentDescription("Clear search groups").performClick()
                waitText("8 groups")
                onNodeWithContentDescription("Search groups").assertIsFocused()
                onNodeWithContentDescription("Clear search groups").assertDoesNotExist()
                onNodeWithText("Empty 8").performClick()
                waitText("8 of 8 groups")
                runOnIdle { width.value = 839.dp }
                waitForIdle()
                onNodeWithText("Groups").assertDoesNotExist()
                onNodeWithContentDescription("Group name").assertTextEquals("Work draft")
                val wideColorStep = onNodeWithContentDescription("Red").fetchSemanticsNode().boundsInRoot.left -
                    onNodeWithContentDescription("Transparent").fetchSemanticsNode().boundsInRoot.left
                assertEquals(onNodeWithContentDescription("Transparent").fetchSemanticsNode().boundsInRoot.top,
                    onNodeWithContentDescription("Blue grey").fetchSemanticsNode().boundsInRoot.top, 0.5f)
                runOnIdle { width.value = 840.dp }
                waitText("Groups")
                onNodeWithContentDescription("Group name").assertTextEquals("Work draft")
                assertEquals(wideColorStep, onNodeWithContentDescription("Red").fetchSemanticsNode().boundsInRoot.left -
                    onNodeWithContentDescription("Transparent").fetchSemanticsNode().boundsInRoot.left, 0.5f)
                assertTrue(onNodeWithContentDescription("Blue grey").fetchSemanticsNode().boundsInRoot.top >
                    onNodeWithContentDescription("Transparent").fetchSemanticsNode().boundsInRoot.top)
                onNodeWithContentDescription("Close group").performClick()
                onNodeWithText("New group").performClick()
                waitText("Create group")
                onNodeWithContentDescription("Group name").assertIsFocused()
                onNodeWithContentDescription("Group name").performTextInput("Travel draft")
                onNodeWithContentDescription("Orange").performClick()
                runOnIdle { width.value = 320.dp }
                waitForIdle()
                onNodeWithContentDescription("Group name").assertTextEquals("Travel draft")
                onNodeWithContentDescription("Orange").assertIsSelected()
                assertTrue(onNodeWithContentDescription("Light blue").fetchSemanticsNode().boundsInRoot.top >
                    onNodeWithContentDescription("Transparent").fetchSemanticsNode().boundsInRoot.top)
                runOnIdle { width.value = 1280.dp }
                waitText("Create group")
                screenshot("task-groups-wide-new-light")
                onNodeWithContentDescription("Close group").performClick()
                onNodeWithText("New group").performClick()
                onNodeWithText("Enter group name").assertIsDisplayed()
                onNodeWithContentDescription("Group name").assertIsFocused()
            }
            val groups = runBlocking { application.koin.get<AppDatabase>().groupDao().observeGroups().first() }
            assertEquals(8, groups.size)
            assertEquals("Work", groups.last().name)
            assertEquals(Palette.Blue.s400.toArgb(), groups.last().color)
        }
    }

    @Test
    fun `dark desktop and compact editor render the approved palette and saved data survives reopening`() {
        val file = directory.resolve("dark.db")
        withAppKoin(file) { application ->
            seedGroups(application)
            val width = mutableStateOf(1280.dp)
            runSkikoComposeUiTest(size = Size(1280f, 980f)) {
                setApp(application, true, width)
                openGroups()
                waitText("8 groups")
                onNodeWithText("Work").performClick()
                waitText("Planned")
                screenshot("task-groups-wide-edit-dark")
                onNodeWithContentDescription("Group name").performTextReplacement("Work renamed")
                saveGroup()
                waitText("Work renamed")
                onNodeWithContentDescription("Search groups").performTextInput("missing")
                waitText("No groups match your search or filter.")
                onNodeWithContentDescription("Clear search groups").performClick()
                waitText("8 groups")
                onNodeWithText("New group").performClick()
                onNodeWithContentDescription("Group name").performTextInput("Travel")
                screenshot("task-groups-wide-new-dark")
                runOnIdle { width.value = 400.dp }
                waitForIdle()
                screenshot("task-groups-compact-new-dark")
                onNodeWithContentDescription("Back").performClick()
                waitText("8 groups")
                screenshot("task-groups-compact-list-dark")
                onNodeWithText("Work renamed").performClick()
                waitText("Planned")
                screenshot("task-groups-compact-edit-dark")
            }
        }
        withAppKoin(file) { application ->
            assertEquals("Work renamed", runBlocking { application.koin.get<AppDatabase>().groupDao().observeGroups().first() }.last().name)
        }
    }

    @Test
    fun `list and editor scroll areas reach the bottom of compact and wide windows`() {
        withAppKoin(directory.resolve("bottom-edge.db")) { application ->
            seedGroups(application)
            val width = mutableStateOf(400.dp)
            runSkikoComposeUiTest(size = Size(1280f, 420f)) {
                setApp(application, false, width)
                openGroups()
                waitText("8 groups")
                for (availableWidth in listOf(400.dp, 1280.dp)) {
                    runOnIdle { width.value = availableWidth }
                    waitForIdle()
                    val bottom = onRoot().fetchSemanticsNode().boundsInRoot.bottom
                    val list = onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollToIndex))
                    assertEquals(bottom, list.fetchSemanticsNode().boundsInRoot.bottom, 0.5f)
                    list.performScrollToIndex(7)
                    onNodeWithText("Work").assertIsDisplayed().performClick()
                    waitText("Planned")
                    val form = onNode(hasScrollAction() and hasAnyDescendant(hasContentDescription("Group name")))
                    assertEquals(bottom, form.fetchSemanticsNode().boundsInRoot.bottom, 0.5f)
                    onNodeWithText("Delete with tasks").assertDoesNotExist()
                    onNodeWithText("Actions").assertDoesNotExist()
                    val delete = onNodeWithText("Delete")
                    delete.performScrollTo().assertIsDisplayed()
                    val deleteBounds = delete.fetchSemanticsNode().boundsInRoot
                    val saveBounds = onNodeWithContentDescription("Save group").fetchSemanticsNode().boundsInRoot
                    assertEquals(saveBounds.center.y, deleteBounds.center.y, 0.5f)
                    screenshot(if (availableWidth == 400.dp) "task-groups-compact-bottom-edge" else "task-groups-wide-bottom-edge")
                    onNodeWithContentDescription(if (availableWidth == 400.dp) "Back" else "Close group").performClick()
                    waitText("8 groups")
                }
            }
        }
    }

    private fun seedGroups(application: KoinApplication) = runBlocking {
        application.koin.get<IAppSettings>().completeWelcome()
        val dao = application.koin.get<AppDatabase>().groupDao()
        listOf("Health" to Palette.Teal.s400, "Ideas" to Palette.Amber.s400, "Learning" to Palette.Purple.s400,
            "Personal" to Palette.Green.s400, "Reading" to Palette.Indigo.s400, "Shopping" to Palette.Orange.s400,
            "Someday" to Palette.Grey.s400, "Work" to Palette.Blue.s400).forEach { (name, color) ->
            dao.insert(GroupEntity(name = name, color = color.toArgb()))
        }
    }

    private fun SkikoComposeUiTest.setApp(application: KoinApplication, dark: Boolean, contentWidth: State<Dp>? = null) {
        setContent {
            KoinIsolatedContext(application) {
                TaskBTheme(dark) {
                    Box(Modifier.width(contentWidth?.value ?: 400.dp).fillMaxHeight()) { TaskBApp(application.koin.get()) }
                }
            }
        }
    }

    private suspend fun SkikoComposeUiTest.openGroups() {
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText("Groups") and hasClickAction()).fetchSemanticsNodes().isNotEmpty() }
        onNode(hasText("Groups") and hasClickAction()).performClick()
        waitText("Groups")
    }

    private suspend fun SkikoComposeUiTest.saveGroup() {
        waitForIdle()
        onNodeWithContentDescription("Save group").performScrollTo().performClick()
    }

    private suspend fun SkikoComposeUiTest.waitText(text: String) {
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun SkikoComposeUiTest.screenshot(name: String) {
        val output = Path.of("build/reports/screenshots")
        Files.createDirectories(output)
        ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", output.resolve("$name.png").toFile())
    }
}
