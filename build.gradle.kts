import com.mkulikov.buildlogic.GitVersionSource

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}

val applicationVersion = providers.of(GitVersionSource::class) {
    parameters.repositoryDirectory.set(layout.projectDirectory)
}.get()

allprojects {
    group = "com.mkulikov.taskb"
    version = applicationVersion
}

tasks.register("printVersion") {
    group = "versioning"
    description = "Prints the application version calculated from version-major.txt and Git history."
    val versionName = applicationVersion
    doLast { logger.lifecycle(versionName) }
}
