package com.mkulikov.taskb.feature.task.screen.task_editor

import java.time.LocalDate

internal sealed interface TaskEditorIntent {
    data class NameChanged(val name: String) : TaskEditorIntent
    data class DateChanged(val date: LocalDate?) : TaskEditorIntent
    data class GroupChanged(val id: Int?) : TaskEditorIntent
    data class ShiftDate(val days: Long = 0, val months: Long = 0) : TaskEditorIntent
    data object ChooseDate : TaskEditorIntent
    data object Save : TaskEditorIntent
    data object PrepareDelete : TaskEditorIntent
    data object ConfirmDelete : TaskEditorIntent
    data object DismissDialog : TaskEditorIntent
    data object RetryLoad : TaskEditorIntent
}
