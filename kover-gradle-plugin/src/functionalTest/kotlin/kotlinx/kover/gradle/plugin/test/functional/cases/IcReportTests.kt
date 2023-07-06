package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.fileInBuildDir
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.GeneratedTest
import kotlin.test.assertTrue

internal class IcReportTests {

    @GeneratedTest
    fun BuildConfigurator.testGenerateReport() {
        addProjectWithKover {
            sourcesFrom("simple")
        }

        run(":koverIcReport") {
            checkOutcome("koverIcReport", "SUCCESS")
            file("reports/kover/report.ic") {
                assertTrue(exists(), "IC report should exists")
                assertTrue(this.length() > 0, "IC report should contains data")
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testOnCheckDefault() {
        addProjectWithKover {}

        run(":check") {
            taskNotCalled("koverIcReport")
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testOnCheck() {
        addProjectWithKover {
            koverReport {
                defaults {
                    ic {
                        onCheck.set(true)
                    }
                }
            }
        }

        run(":check") {
            checkOutcome("koverIcReport", "SUCCESS")
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testReportPath() {
        addProjectWithKover {
            sourcesFrom("simple")

            koverReport {
                defaults {
                    ic {
                        icFile.set(fileInBuildDir("custom/fileName"))
                    }
                }
            }
        }

        run(":koverIcReport") {
            checkOutcome(":koverIcReport", "SUCCESS")
            file("custom/fileName") {
                assertTrue(exists(), "IC report should exists")
                assertTrue(this.length() > 0, "IC report should contains data")
            }
        }
    }
}