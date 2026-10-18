plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

group = "com.mkulikov.taskb.feature.home"

base {
    archivesName.set("feature-home-api")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(libs.navigation3.runtime)
}
