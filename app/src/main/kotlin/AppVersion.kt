package com.mkulikov.taskb

internal object AppVersion {
    val value: String = checkNotNull(javaClass.getResourceAsStream("/app-version.txt")) {
        "Application version resource is missing"
    }.bufferedReader(Charsets.UTF_8).use { it.readText().trim() }
}
