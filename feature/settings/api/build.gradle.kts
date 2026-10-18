plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

group = "com.mkulikov.taskb.feature.settings"

base {
    archivesName.set("feature-settings-api")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.room.runtime)
}
