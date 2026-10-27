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
import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.core.database.AppDatabase
import com.mkulikov.taskb.feature.settings.api.IAppSettings
import com.mkulikov.taskb.feature.task.api.database.GroupEntity
import com.mkulikov.taskb.feature.task.api.database.TaskEntity
import com.mkulikov.taskb.ui.theme.Palette
import com.mkulikov.taskb.ui.theme.TaskBTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.compose.KoinIsolatedContext
import org.koin.core.KoinApplication
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.imageio.ImageIO
import kotlin.test.*

@OptIn(ExperimentalTestApi::class)
class TaskListNavigationUiTest {
    @TempDir lateinit var directory: Path

    @Test
    fun `compact CRUD date and group choices completion filters and deletion persist`() {
        val file = directory.resolve("crud.db")
        withAppKoin(file) { app ->
            seed(app, tasks = false)
            runSkikoComposeUiTest(size = Size(400f, 980f)) {
                setApp(app)
                openTasks()
                waitText("No tasks yet.")
                onNodeWithContentDescription("Add task").performClick()
                onNodeWithContentDescription("Save task").assertIsNotEnabled()
                screenshot("task-compact-new-empty")
                onNodeWithContentDescription("Task name").assertIsFocused().performTextInput(" New task ")
                onNodeWithText("Tomorrow").performClick()
                onNodeWithText("Tomorrow").assertIsSelected()
                onNodeWithContentDescription("Add one day").performClick()
                onNodeWithContentDescription("Choose task group").performClick()
                waitText("Choose group")
                onNodeWithContentDescription("Search groups").performTextInput("wor")
                onNodeWithText("Work").performClick()
                waitText("New task")
                screenshot("task-compact-new")
                onNodeWithContentDescription("Save task").performClick()
                waitText("1 task")
                onNodeWithText("New task").performClick()
                waitText("Task")
                onNodeWithContentDescription("Save task").assertIsNotEnabled()
                onNodeWithContentDescription("Task name").performTextReplacement("Edited task")
                onNodeWithContentDescription("Choose date").performClick()
                waitText("Choose date")
                onNodeWithContentDescription("Next month").performClick()
                screenshot("task-date-picker", dialog = true)
                val chosen = YearMonth.from(LocalDate.now().plusDays(2)).plusMonths(1).atDay(15)
                onNodeWithContentDescription("Choose ${chosen.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH))}").performClick()
                onNodeWithContentDescription("Clear date").performClick()
                onNodeWithContentDescription("Clear task group").performClick()
                screenshot("task-compact-edit")
                onNodeWithContentDescription("Save task").performClick()
                waitText("1 task")
                onNodeWithContentDescription("Complete Edited task").performClick()
                waitUntil(timeoutMillis = 5_000) { onAllNodes(hasContentDescription("Mark Edited task incomplete")).fetchSemanticsNodes().isNotEmpty() }
                onNodeWithContentDescription("Choose task filter").performClick()
                screenshot("task-compact-filters")
                onNodeWithText("Completed").performClick()
                waitText("1 of 1 tasks")
                onNodeWithContentDescription("Choose group").performClick()
                onNodeWithText("No group").performClick()
                waitText("1 of 1 tasks")
                onNodeWithContentDescription("Clear task filter").performClick()
                onNodeWithText("Edited task").performClick()
                waitText("Task")
                onNodeWithContentDescription("Delete task").performClick()
                waitText("Delete task")
                onNodeWithText("Cancel").performClick()
                onNodeWithContentDescription("Delete task").performClick()
                onNode(hasText("Delete") and hasAnyAncestor(isDialog())).performClick()
                waitText("No tasks yet.")
            }
            assertTrue(runBlocking { app.koin.get<AppDatabase>().taskDao().observeTasks().first() }.isEmpty())
        }
        withAppKoin(file) { app -> assertTrue(runBlocking { app.koin.get<AppDatabase>().taskDao().observeTasks().first() }.isEmpty()) }
    }

    @Test
    fun `wide layouts match task designs and resizing preserves draft and active selectors`() {
        withAppKoin(directory.resolve("resize.db")) { app ->
            seed(app)
            val width = mutableStateOf(1280.dp)
            runSkikoComposeUiTest(size = Size(1280f, 980f)) {
                setApp(app, width = width)
                openTasks()
                waitText("8 tasks")
                screenshot("task-wide-list")
                onNodeWithText("New task").performClick()
                waitText("Create task")
                onNodeWithContentDescription("Save task").assertIsNotEnabled()
                screenshot("task-wide-new-empty")
                onNodeWithContentDescription("Task name").performTextInput("Plan the next release")
                screenshot("task-wide-new")
                runOnIdle { width.value = 300.dp }
                waitForIdle()
                onNodeWithContentDescription("Task name").assertTextEquals("Plan the next release")
                runOnIdle { width.value = 1280.dp }
                waitText("Create task")
                onNodeWithContentDescription("Close task").performClick()
                onNodeWithText("Prepare task screen mockups").performClick()
                waitText("Save changes")
                onNodeWithContentDescription("Task name").performTextReplacement("Task draft")
                screenshot("task-wide-edit")
                onNodeWithContentDescription("Choose task group").performClick()
                waitText("Choose group")
                screenshot("task-wide-groups", dialog = true)
                runOnIdle { width.value = 400.dp }
                waitForIdle()
                onNodeWithText("Choose group").assertIsDisplayed()
                screenshot("task-compact-groups")
                onNodeWithText("Health").performClick()
                waitText("Task")
                onNodeWithContentDescription("Task name").assertTextEquals("Task draft")
                onNodeWithContentDescription("Choose task group").assert(hasText("Health"))
                runOnIdle { width.value = 300.dp }
                waitForIdle()
                screenshot("task-narrow-edit")
                runOnIdle { width.value = 840.dp }
                waitText("Save changes")
                onNodeWithContentDescription("Task name").assertTextEquals("Task draft")
                runOnIdle { width.value = 1280.dp }
                waitForIdle()
                onNodeWithContentDescription("Close task").performClick()
                onNodeWithContentDescription("Choose task filter").performClick()
                waitText("Filters")
                screenshot("task-wide-filters", dialog = true)
                onNode(hasText("Today") and hasAnyAncestor(isDialog())).performClick()
                waitText("2 of 8 tasks")
                onNodeWithContentDescription("Choose group").performClick()
                onNode(hasText("Health") and hasAnyAncestor(isDialog())).performClick()
                waitText("1 of 8 tasks")
                screenshot("task-wide-filtered")
                runOnIdle { width.value = 400.dp }
                waitForIdle()
                screenshot("task-compact-filtered")
                onNodeWithContentDescription("Clear group filter").performClick()
                onNodeWithContentDescription("Clear task filter").performClick()
                waitText("8 tasks")
                screenshot("task-compact-list")
                onNodeWithContentDescription("Search tasks").performTextInput("nonexistent")
                waitText("No tasks match your search or filters.")
                onNodeWithContentDescription("Clear search tasks").performClick()
                onNodeWithContentDescription("Search tasks").assertIsFocused()
            }
        }
    }

    @Test
    fun `dark short windows keep editor actions and long selections reachable`() {
        withAppKoin(directory.resolve("dark.db")) { app ->
            seed(app)
            val width = mutableStateOf(1280.dp)
            runSkikoComposeUiTest(size = Size(1280f, 420f)) {
                setApp(app, dark = true, width = width)
                openTasks()
                waitText("8 tasks")
                onNodeWithText("Prepare task screen mockups").performClick()
                waitText("Save changes")
                onNodeWithContentDescription("Save task").performScrollTo().assertIsDisplayed()
                screenshot("task-wide-short-dark")
                onNodeWithContentDescription("Choose task group").performScrollTo().performClick()
                waitText("Choose group")
                onNode(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsActions.ScrollToIndex) and hasAnyAncestor(isDialog())).performScrollToIndex(8)
                onNode(hasText("Work") and hasAnyAncestor(isDialog())).assertIsDisplayed()
                onNodeWithContentDescription("Close selection").performClick()
                runOnIdle { width.value = 300.dp }
                waitForIdle()
                onNodeWithContentDescription("Save task").assertIsDisplayed()
                onNodeWithContentDescription("Choose task group").performScrollTo().assertIsDisplayed()
                screenshot("task-narrow-short-dark")
            }
        }
    }

    private fun seed(app: KoinApplication, tasks: Boolean = true) = runBlocking {
        app.koin.get<IAppSettings>().completeWelcome()
        val db = app.koin.get<AppDatabase>()
        val groupIds = listOf("Health" to Palette.Teal.s400, "Ideas" to Palette.Amber.s400,
            "Learning" to Palette.Purple.s400, "Personal" to Palette.Green.s400, "Reading" to Palette.Indigo.s400,
            "Shopping" to Palette.Orange.s400, "Someday" to Palette.Grey.s400, "Work" to Palette.Blue.s400).associate { (name, color) -> name to db.groupDao().insert(GroupEntity(name = name, color = color.toArgb())).toInt() }
        if (tasks) {
            val today = LocalDate.now()
            listOf(TaskEntity(name = "Prepare task screen mockups", dueDate = today.minusDays(1).toEpochDay(), groupId = groupIds["Work"]),
                TaskEntity(name = "Book a dentist appointment", dueDate = today.toEpochDay(), groupId = groupIds["Health"]),
                TaskEntity(name = "Buy groceries for the weekend", dueDate = today.toEpochDay(), groupId = groupIds["Shopping"]),
                TaskEntity(name = "Read a chapter", dueDate = today.plusDays(1).toEpochDay(), groupId = groupIds["Reading"]),
                TaskEntity(name = "Practice Kotlin coroutines", dueDate = today.plusDays(2).toEpochDay(), groupId = groupIds["Learning"]),
                TaskEntity(name = "Plan the next week", groupId = groupIds["Personal"]),
                TaskEntity(name = "Send the project update", groupId = groupIds["Work"], completed = true),
                TaskEntity(name = "Go for an evening walk", groupId = groupIds["Health"], completed = true)).forEach { db.taskDao().insert(it) }
        }
    }
    private fun SkikoComposeUiTest.setApp(app: KoinApplication, dark: Boolean = false, width: State<Dp>? = null) {
        setContent { KoinIsolatedContext(app) { TaskBTheme(dark) { Box(Modifier.width(width?.value ?: 400.dp).fillMaxHeight()) { TaskBApp(app.koin.get()) } } } }
    }
    private suspend fun SkikoComposeUiTest.openTasks() {
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText("Tasks") and hasClickAction()).fetchSemanticsNodes().isNotEmpty() }
        onNode(hasText("Tasks") and hasClickAction()).performClick()
    }
    private suspend fun SkikoComposeUiTest.waitText(text: String) {
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun SkikoComposeUiTest.screenshot(name: String, dialog: Boolean = false) {
        val output = Path.of("build/reports/screenshots")
        Files.createDirectories(output)
        ImageIO.write((if (dialog) onNode(isDialog()) else onRoot()).captureToImage().toAwtImage(), "png", output.resolve("$name.png").toFile())
    }
}
