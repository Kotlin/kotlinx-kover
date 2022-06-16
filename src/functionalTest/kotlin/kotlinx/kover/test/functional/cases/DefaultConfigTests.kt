package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.*
import kotlin.test.*

internal class DefaultConfigTests : BaseGradleScriptTest() {
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
