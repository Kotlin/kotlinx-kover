package kotlinx.kover.test.functional.core

import java.io.*

private const val INTERNAL_PROJECTS_PATH = "src/functionalTest/templates/projects"


internal fun loadInternalProject(name: String, rootDir: File): ProjectRunner {
    val targetDir = File.createTempFile(name, null, rootDir)

    val srcDir = File(INTERNAL_PROJECTS_PATH, name)
    if (!srcDir.exists()) {
        throw IllegalArgumentException("Internal test project '$name' not found")
    }

    srcDir.copyRecursively(targetDir, true)

    return SingleProjectRunnerImpl(targetDir)
}
