plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
}

base {
    archivesName.set("feature-home")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":feature:home:api"))
    implementation(libs.navigation3.runtime)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
}
