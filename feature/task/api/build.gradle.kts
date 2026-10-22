plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

group = "com.mkulikov.taskb.feature.task"

base {
    archivesName.set("feature-task-api")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(libs.navigation3.runtime)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.room.runtime)
}
