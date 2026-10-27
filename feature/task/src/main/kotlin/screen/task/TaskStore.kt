package com.mkulikov.taskb.feature.task.screen.task

import com.mkulikov.taskb.core.mvi.CoroutineStore
import com.mkulikov.taskb.feature.task.repository.IGroupRepository
import com.mkulikov.taskb.feature.task.repository.ITaskRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.time.Clock
import java.time.LocalDate

internal class TaskStore(
    private val repository: ITaskRepository,
    private val groups: IGroupRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val clock: Clock = Clock.systemDefaultZone(),
) : CoroutineStore<TaskIntent, TaskState, TaskMessage>(TaskState(today = LocalDate.now(clock)), ::reduceTask, dispatcher) {
    private var observation: Job? = null
    init {
        accept(TaskIntent.RetryLoad)
        collectEffect(flow {
            while (true) {
                delay(60_000)
                emit(TaskMessage.Today(LocalDate.now(clock)))
            }
        })
    }
    override fun handleIntent(intent: TaskIntent) {
        when (intent) {
            is TaskIntent.Search -> reduce(TaskMessage.Query(intent.query))
            is TaskIntent.SearchGroups -> reduce(TaskMessage.GroupQuery(intent.query))
            is TaskIntent.Filter -> reduce(TaskMessage.Filter(intent.filter))
            is TaskIntent.FilterGroup -> reduce(TaskMessage.GroupFilter(intent.filter))
            is TaskIntent.Sort -> reduce(TaskMessage.Sort(intent.sort))
            is TaskIntent.Choose -> reduce(TaskMessage.Selection(intent.selection))
            TaskIntent.CloseSelection -> reduce(TaskMessage.Selection(null))
            TaskIntent.NewTask -> reduce(TaskMessage.Editor(TaskEditorTarget.New))
            TaskIntent.CloseEditor -> reduce(TaskMessage.Editor(null))
            TaskIntent.DismissError -> reduce(TaskMessage.DismissError)
            is TaskIntent.OpenTask -> if (state.value.tasks.any { it.id == intent.id }) reduce(TaskMessage.Editor(TaskEditorTarget.Edit(intent.id)))
            is TaskIntent.ToggleCompleted -> {
                val task = state.value.tasks.find { it.id == intent.id } ?: return
                if (task.id in state.value.pendingCompletion) return
                reduce(TaskMessage.Completing(task.id))
                launchEffect {
                    try {
                        repository.setCompleted(task.id, !task.completed)
                        TaskMessage.Completed(task.id, false)
                    } catch (exception: CancellationException) { throw exception }
                    catch (_: Exception) { TaskMessage.Completed(task.id, true) }
                }
            }
            TaskIntent.RetryLoad -> if (observation?.isActive != true) {
                reduce(TaskMessage.Loading)
                observation = collectEffect(combine(repository.tasks, groups.groups) { tasks, groups ->
                    TaskMessage.Loaded(tasks, groups) as TaskMessage
                }.catch {
                    if (it is CancellationException) throw it
                    emit(TaskMessage.LoadFailed)
                })
            }
        }
    }
}
