package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.GeneratedTest
import kotlin.test.assertTrue

internal class BinaryReportTests {

    @GeneratedTest
    fun BuildConfigurator.testGenerateReport() {
        addProjectWithKover {
            sourcesFrom("simple")
        }

        run(":koverBinaryReport") {
            checkOutcome("koverBinaryReport", "SUCCESS")
            file("reports/kover/report.bin") {
                assertTrue(exists(), "Binary report should exists")
                assertTrue(this.length() > 0, "Binary report should contains data")
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testOnCheckDefault() {
        addProjectWithKover {}

        run(":check") {
            taskNotCalled("koverBinaryReport")
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testOnCheck() {
        addProjectWithKover {
            kover {

                reports {
                    total {
                        binary {
                            onCheck.set(true)
                        }
                    }
                }

            }
        }

        run(":check") {
            checkOutcome("koverBinaryReport", "SUCCESS")
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testReportPath() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                reports {
                    total {
                        binary {
                            file.set(it.layout.buildDirectory.file("custom/fileName"))
                        }
                    }
                }
            }
        }

        run(":koverBinaryReport") {
            checkOutcome(":koverBinaryReport", "SUCCESS")
            file("custom/fileName") {
                assertTrue(exists(), "Binary report should exists")
                assertTrue(this.length() > 0, "Binary report should contains data")
            }
        }
    }
}