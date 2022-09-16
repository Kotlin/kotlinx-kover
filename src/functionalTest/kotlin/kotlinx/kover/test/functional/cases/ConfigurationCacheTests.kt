package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.starter.*

internal class ConfigurationCacheTests {
    @GeneratedTest
    fun BuildConfigurator.testConfigCache() {
        addKoverProject {
            sourcesFrom("simple")
            koverMerged {
                enable()
            }
        }

        run(
            "build",
            "koverMergedReport",
            "koverMergedVerify",
            "koverReport",
            "koverVerify",
            "--configuration-cache"
        )
    }
}
