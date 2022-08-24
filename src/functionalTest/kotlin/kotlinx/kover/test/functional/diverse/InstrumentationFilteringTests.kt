package kotlinx.kover.test.functional.diverse

import kotlinx.kover.test.functional.diverse.core.*
import kotlinx.kover.test.functional.diverse.core.AbstractDiverseGradleTest
import kotlin.test.*

internal class InstrumentationFilteringTests : AbstractDiverseGradleTest() {

    @Test
    fun testExclude() {
        val build = diverseBuild(
            kotlinx.kover.test.functional.diverse.core.ALL_LANGUAGES,
            kotlinx.kover.test.functional.diverse.core.ALL_ENGINES,
            kotlinx.kover.test.functional.diverse.core.ALL_TYPES
        )
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
        val build = diverseBuild(
            kotlinx.kover.test.functional.diverse.core.ALL_LANGUAGES,
            kotlinx.kover.test.functional.diverse.core.ALL_ENGINES,
            kotlinx.kover.test.functional.diverse.core.ALL_TYPES
        )
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
    fun testExcludeByKoverExtension() {
        val build = diverseBuild(
            kotlinx.kover.test.functional.diverse.core.ALL_LANGUAGES,
            kotlinx.kover.test.functional.diverse.core.ALL_ENGINES,
            kotlinx.kover.test.functional.diverse.core.ALL_TYPES
        )
        build.addKoverRootProject {
            sourcesFrom("simple")
            kover {
                filters {
                    classes {
                        excludes += "org.jetbrains.*Exa?ple*"
                    }
                }
                xmlReport {
                    overrideFilters {
                        classes {
                            // override class filter (discard all rules) to in order for all classes to be included in the report
                        }
                    }
                }
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
    fun testExcludeIncludeByKoverExtension() {
        val build = diverseBuild(
            kotlinx.kover.test.functional.diverse.core.ALL_LANGUAGES,
            kotlinx.kover.test.functional.diverse.core.ALL_ENGINES,
            kotlinx.kover.test.functional.diverse.core.ALL_TYPES
        )
        build.addKoverRootProject {
            sourcesFrom("simple")
            kover {
                filters {
                    classes {
                        includes += "org.jetbrains.*Cla?s"
                        excludes += "org.jetbrains.*Exa?ple*"
                    }
                }
                xmlReport {
                    overrideFilters {
                        classes {
                            // override class filter (discard all rules) to in order for all classes to be included in the report
                        }
                    }
                }
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
        val build = diverseBuild(
            kotlinx.kover.test.functional.diverse.core.ALL_LANGUAGES,
            kotlinx.kover.test.functional.diverse.core.ALL_ENGINES, listOf(ProjectType.KOTLIN_JVM))
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
