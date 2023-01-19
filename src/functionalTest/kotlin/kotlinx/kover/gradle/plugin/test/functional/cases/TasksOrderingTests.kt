package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*

internal class TasksOrderingTests {
    @GeneratedTest
    fun BuildConfigurator.testProjectTasks() {
        addProjectWithKover {
            sourcesFrom("simple")
            koverReport {
                verify {
                    rule {
                        minBound(100)
                    }
                }
            }
        }
        runWithError("koverVerify", "koverXmlReport", "koverHtmlReport") {
            // reports should be generated even if verification failed with an error
            checkDefaultReports()
        }
    }
}
