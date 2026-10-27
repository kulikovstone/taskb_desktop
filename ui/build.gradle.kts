plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

base {
    archivesName.set("taskb-ui")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.compose.ui.toolingPreview)
    implementation(libs.materialSymbols.outlined)
    implementation(libs.materialSymbols.outlinedFilled)
    api(libs.compose.runtime)
    api(libs.compose.ui)
    api(libs.compose.foundation)
    api(libs.compose.material)
}
