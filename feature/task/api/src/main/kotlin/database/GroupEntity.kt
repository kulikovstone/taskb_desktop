package com.mkulikov.taskb.feature.task.api.database

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "groups", indices = [Index(value = ["name"], unique = true)])
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val name: String,
    val color: Int,
)
