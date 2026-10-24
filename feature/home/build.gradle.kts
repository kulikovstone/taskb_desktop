plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

base {
    archivesName.set("feature-home")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":ui"))
    implementation(project(":core:navigation"))
    implementation(project(":feature:home:api"))
    implementation(project(":feature:task:api"))
    implementation(libs.koin.core)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.toolingPreview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
}
