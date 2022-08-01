package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import kotlin.test.*

internal class InstrumentationFilteringTests : BaseGradleScriptTest() {

    @Test
    fun testExclude() {
        val build = diverseBuild(ALL_LANGUAGES, ALL_ENGINES, ALL_TYPES)
        build.addKoverRootProject {
            sourcesFrom("simple")
            testTasks {
                excludes("org.jetbrains.*Exa?ple*")
            }
        }
        val runner = build.prepare()
        runner.run("build", "koverXmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.ExampleClass").assertFullyMissed()
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }

    }

    @Test
    fun testExcludeInclude() {
        val build = diverseBuild(ALL_LANGUAGES, ALL_ENGINES, ALL_TYPES)
        build.addKoverRootProject {
            sourcesFrom("simple")
            testTasks {
                includes("org.jetbrains.*Cla?s")
                excludes("org.jetbrains.*Exa?ple*")
            }
        }
        val runner = build.prepare()
        runner.run("build", "koverXmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.ExampleClass").assertFullyMissed()
                classCounter("org.jetbrains.Unused").assertFullyMissed()
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }
    }

    @Test
    fun testDisableInstrumentationOfTask() {
        val build = diverseBuild(ALL_LANGUAGES, ALL_ENGINES, listOf(ProjectType.KOTLIN_JVM))
        build.addKoverRootProject {
            sourcesFrom("simple")
            kover {
                instrumentation {
                    excludeTasks += "test"
                }
            }
        }
        val runner = build.prepare()
        runner.run("build", "koverXmlReport") {
            // if task `test` is excluded from instrumentation then the binary report is not created for it
            checkDefaultBinaryReport(false)
        }
    }

}
