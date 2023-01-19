package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.framework.checker.*
import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.starter.*

internal class InstrumentationFilteringTests {

    @SlicedGeneratedTest(all = true)
    fun BuildConfigurator.testExclude() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                excludeInstrumentation {
                    className("org.jetbrains.*Exa?ple*")
                }
            }
        }

        run("build", "koverXmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.ExampleClass").assertFullyMissed()
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }
    }

    @SlicedGeneratedTest(all = true)
    fun SlicedBuildConfigurator.testDisableInstrumentationOfTask() {
        addProjectWithKover {
            sourcesFrom("simple")
            kover {
                excludeTests {
                    taskName(defaultTestTaskName(slice.type))
                }
            }
        }
        run("koverXmlReport") {
            // if task `test` is excluded from instrumentation then the raw report is not created for it
            checkDefaultRawReport(false)
        }
    }

}
