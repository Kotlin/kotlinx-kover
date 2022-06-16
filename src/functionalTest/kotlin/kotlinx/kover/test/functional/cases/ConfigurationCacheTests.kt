package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.core.*
import kotlin.test.*

internal class ConfigurationCacheTests : BaseGradleScriptTest() {
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
