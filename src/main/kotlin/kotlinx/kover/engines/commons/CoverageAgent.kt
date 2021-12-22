package kotlinx.kover.engines.commons

import kotlinx.kover.api.*
import org.gradle.api.*
import org.gradle.api.file.*

internal interface CoverageAgent {
    val engine: CoverageEngine
    val classpath: FileCollection
    fun buildCommandLineArgs(task: Task, extension: KoverTaskExtension): MutableList<String>
}
