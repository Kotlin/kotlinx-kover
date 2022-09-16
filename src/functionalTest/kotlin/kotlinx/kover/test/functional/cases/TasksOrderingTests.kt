package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.starter.*

internal class TasksOrderingTests {
    @GeneratedTest
    fun BuildConfigurator.testProjectTasks() {
        addKoverProject {
            sourcesFrom("simple")
            kover {
                verify {
                    rule {
                        bound {
                            minValue = 100
                        }
                    }
                }
            }
        }
        runWithError("koverVerify", "koverReport") {
            // reports should be generated even if verification failed with an error
            checkDefaultReports()
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testMergedTasks() {
        addKoverProject {
            sourcesFrom("simple")
            koverMerged {
                enable()
                verify {
                    rule {
                        bound {
                            minValue = 100
                        }
                    }
                }
            }
        }
        runWithError("koverMergedVerify", "koverMergedReport") {
            // reports should be generated even if verification failed with an error
            checkDefaultMergedReports()
        }
    }

}
