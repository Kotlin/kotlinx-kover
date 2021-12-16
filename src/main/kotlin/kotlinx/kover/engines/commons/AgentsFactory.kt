package kotlinx.kover.engines.commons

import kotlinx.kover.api.*
import kotlinx.kover.engines.intellij.*
import kotlinx.kover.engines.jacoco.*
import org.gradle.api.*

internal object AgentsFactory {
    fun createAgents(project: Project, koverExtension: KoverExtension): Map<CoverageEngine, CoverageAgent> {
        return mapOf(
            CoverageEngine.INTELLIJ to project.createIntellijAgent(koverExtension),
            CoverageEngine.JACOCO to project.createJacocoAgent(koverExtension),
        )
    }
}

internal fun Map<CoverageEngine, CoverageAgent>.getFor(engine: CoverageEngine): CoverageAgent {
    return this[engine] ?: throw GradleException("Coverage agent for Coverage Engine '$engine' not found")
}
