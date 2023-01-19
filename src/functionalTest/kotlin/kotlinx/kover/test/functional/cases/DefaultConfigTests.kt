package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.starter.*

internal class DefaultConfigTests {

    @SlicedGeneratedTest(allLanguages = true, allTypes = true)
    fun BuildConfigurator.testImplicitConfigs() {
        addProjectWithKover {
            sourcesFrom("simple")
        }

        run("koverXmlReport", "koverHtmlReport") {
            checkDefaultRawReport()
            checkDefaultReports()
        }
    }
}
