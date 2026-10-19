plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    alias(libs.plugins.compose.compiler)
}

base {
    archivesName.set("taskb-ui")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(libs.compose.runtime)
    api(libs.compose.ui)
    api(libs.compose.foundation)
    api(libs.compose.material)
}
