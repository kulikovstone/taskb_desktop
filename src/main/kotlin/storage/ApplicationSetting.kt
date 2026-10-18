package com.mkulikov.taskb.storage

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "application_settings")
internal data class ApplicationSetting(
    @PrimaryKey val key: String,
    val value: String
)
