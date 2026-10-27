package com.mkulikov.taskb.feature.task.screen.task_editor

import com.mkulikov.taskb.core.mvi.CoroutineStore
import com.mkulikov.taskb.feature.task.model.Task
import com.mkulikov.taskb.feature.task.repository.ITaskRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class TaskEditorStore(
    private val repository: ITaskRepository,
    private val taskId: Int? = null,
    initialDate: java.time.LocalDate? = null,
    initialGroup: Int? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineStore<TaskEditorIntent, TaskEditorState, TaskEditorMessage>(
    TaskEditorState(isNew = taskId == null, isLoading = taskId != null, dueDate = initialDate, groupId = initialGroup), ::reduceTaskEditor, dispatcher,
) {
    private var loadRequested = false
    init { if (taskId != null) accept(TaskEditorIntent.RetryLoad) }
    override fun handleIntent(intent: TaskEditorIntent) {
        val current = state.value
        if (current.isFinished || current.isSaving) return
        when (intent) {
            TaskEditorIntent.RetryLoad -> {
                if (taskId == null || current.isLoading && loadRequested || current.original != null) return
                loadRequested = true
                reduce(TaskEditorMessage.Loading)
                launchEffect {
                    try { TaskEditorMessage.Loaded(repository.getById(taskId)) }
                    catch (exception: CancellationException) { throw exception }
                    catch (_: Exception) { TaskEditorMessage.LoadFailed }
                }
            }
            TaskEditorIntent.DismissDialog -> reduce(TaskEditorMessage.Dialog(null))
            TaskEditorIntent.ConfirmDelete -> if (current.dialog == TaskEditorDialog.ConfirmDelete && taskId != null) {
                reduce(TaskEditorMessage.Saving)
                launchEffect { recover("Couldn't delete this task. Please try again.") { repository.delete(taskId); TaskEditorMessage.Finished } }
            }
            is TaskEditorIntent.DateChanged -> if (!current.isLoading && current.loadError == null && (current.dialog == null || current.dialog == TaskEditorDialog.DatePicker)) reduce(TaskEditorMessage.Date(intent.date))
            else -> {
                if (current.isBusy || current.loadError != null || current.dialog != null) return
                when (intent) {
                    is TaskEditorIntent.NameChanged -> reduce(TaskEditorMessage.Name(intent.name))
                    is TaskEditorIntent.GroupChanged -> reduce(TaskEditorMessage.Group(intent.id))
                    is TaskEditorIntent.ShiftDate -> current.dueDate?.let { reduce(TaskEditorMessage.Date(it.plusDays(intent.days).plusMonths(intent.months))) }
                    TaskEditorIntent.ChooseDate -> reduce(TaskEditorMessage.Dialog(TaskEditorDialog.DatePicker))
                    TaskEditorIntent.PrepareDelete -> if (current.original != null) reduce(TaskEditorMessage.Dialog(TaskEditorDialog.ConfirmDelete))
                    TaskEditorIntent.Save -> if (current.canSave) {
                        reduce(TaskEditorMessage.Saving)
                        val task = (current.original ?: Task(name = "")).copy(name = current.normalizedName, dueDate = current.dueDate, groupId = current.groupId)
                        launchEffect { recover("Couldn't save this task. Please try again.") {
                            if (repository.save(task)) TaskEditorMessage.Finished else TaskEditorMessage.Failed("This task no longer exists.")
                        } }
                    }
                }
            }
        }
    }
    private suspend fun recover(message: String, block: suspend () -> TaskEditorMessage): TaskEditorMessage = try { block() }
        catch (exception: CancellationException) { throw exception }
        catch (_: Exception) { TaskEditorMessage.Failed(message) }
}
