package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.starter.*

internal class DefaultConfigTests {

    @SlicedGeneratedTest(allLanguages = true, allTypes = true)
    fun BuildConfigurator.testImplicitConfigs() {
        addKoverProject {
            sourcesFrom("simple")
        }

        run("koverReport") {
            checkDefaultBinaryReport()
            checkDefaultReports()
        }
    }
}
