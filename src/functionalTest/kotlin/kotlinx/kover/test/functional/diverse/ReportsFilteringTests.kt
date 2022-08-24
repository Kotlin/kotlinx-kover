package kotlinx.kover.test.functional.diverse

import kotlinx.kover.test.functional.diverse.core.*
import kotlinx.kover.test.functional.diverse.core.AbstractDiverseGradleTest
import kotlin.test.*

internal class ReportsFilteringTests : AbstractDiverseGradleTest() {

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
