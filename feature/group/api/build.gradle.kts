plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

group = "com.mkulikov.taskb.feature.group"

base {
    archivesName.set("feature-group-api")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(libs.navigation3.runtime)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.room.runtime)
}
