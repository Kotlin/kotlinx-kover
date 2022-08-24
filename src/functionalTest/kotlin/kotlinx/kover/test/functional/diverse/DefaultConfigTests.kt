package kotlinx.kover.test.functional.diverse

import kotlinx.kover.test.functional.diverse.core.*
import kotlinx.kover.test.functional.diverse.core.ALL_LANGUAGES
import kotlinx.kover.test.functional.diverse.core.ALL_TYPES
import kotlinx.kover.test.functional.diverse.core.AbstractDiverseGradleTest
import kotlin.test.*

internal class DefaultConfigTests : AbstractDiverseGradleTest() {
    @Test
    fun testImplicitConfigs() {
        val build = diverseBuild(
            languages = ALL_LANGUAGES,
            types = ALL_TYPES
        )
        build.addKoverRootProject {
            sourcesFrom("simple")
        }
        val runner = build.prepare()

        runner.run("koverReport") {
            checkDefaultBinaryReport()
            checkDefaultReports()
        }

    }

}
