package com.mkulikov.taskb.feature.task.model

internal data class Group(
    val id: Int,
    val name: String,
    val color: GroupColor,
    val tasks: GroupTaskSummary = GroupTaskSummary(),
)
