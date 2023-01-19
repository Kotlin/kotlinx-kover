package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.starter.*

internal class ConfigurationCacheTests {
    @GeneratedTest
    fun BuildConfigurator.testConfigCache() {
        addProjectWithKover {
            sourcesFrom("simple")
        }

        run(
            "build",
            "koverXmlReport",
            "koverHtmlReport",
            "koverVerify",
            "--configuration-cache"
        )
    }
}
