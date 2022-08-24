package kotlinx.kover.test.functional.diverse

import kotlinx.kover.test.functional.diverse.core.*
import kotlinx.kover.test.functional.diverse.core.AbstractDiverseGradleTest
import kotlin.test.*

internal class ConfigurationCacheTests : AbstractDiverseGradleTest() {
    @Test
    fun testConfigCache() {
        val build = diverseBuild()
        build.addKoverRootProject {
            sourcesFrom("simple")
            koverMerged {
                enable()
            }
        }

        val runner = build.prepare()
        runner.run(
            "build",
            "koverMergedReport",
            "koverMergedVerify",
            "koverReport",
            "koverVerify",
            "--configuration-cache"
        )
    }
}
