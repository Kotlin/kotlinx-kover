package kotlinx.kover.test.functional.core

import java.io.*

private const val INTERNAL_SAMPLES_PATH = "src/functionalTest/templates/samples"


internal fun createInternalSample(name: String, rootDir: File): GradleRunner {
    val targetDir = File.createTempFile(name, null, rootDir)

    val srcDir = File(INTERNAL_SAMPLES_PATH, name)
    if (!srcDir.exists()) {
        throw IllegalArgumentException("Internal test sample '$name' not found")
    }

    srcDir.copyRecursively(targetDir, true)

    return SingleGradleRunnerImpl(targetDir)
}
