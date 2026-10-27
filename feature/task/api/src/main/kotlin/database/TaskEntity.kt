package com.mkulikov.taskb.feature.task.api.database

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = GroupEntity::class, parentColumns = ["id"], childColumns = ["groupId"],
        onDelete = ForeignKey.SET_NULL,
    )],
    indices = [Index("groupId")],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dueDate: Long? = null,
    val groupId: Int? = null,
    val completed: Boolean = false,
)
