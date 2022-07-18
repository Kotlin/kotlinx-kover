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

}
