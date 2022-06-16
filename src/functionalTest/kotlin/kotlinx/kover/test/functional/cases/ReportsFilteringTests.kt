package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import kotlin.test.*

internal class ReportsFilteringTests : BaseGradleScriptTest() {

    @Test
    fun testExclude() {
        val build = diverseBuild(languages = ALL_LANGUAGES)
        build.addKoverRootProject {
            sourcesFrom("simple")

            kover {
                filters {
                    classes {
                        excludes += "org.jetbrains.*Exa?ple*"
                    }
                }
            }
        }
        val runner = build.prepare()
        runner.run("koverXmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.ExampleClass").assertAbsent()
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }
    }

    @Test
    fun testExcludeInclude() {
        val build = diverseBuild(languages = ALL_LANGUAGES)
        build.addKoverRootProject {
            sourcesFrom("simple")

            kover {
                filters {
                    classes {
                        excludes += "org.jetbrains.*Exa?ple*"
                        includes += "org.jetbrains.*Cla?s"
                    }
                }
            }
        }
        val runner = build.prepare()
        runner.run("koverXmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.ExampleClass").assertAbsent()
                classCounter("org.jetbrains.Unused").assertAbsent()
                classCounter("org.jetbrains.SecondClass").assertFullyCovered()
            }
        }
    }

}
