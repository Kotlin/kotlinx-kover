package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.framework.checker.*
import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.starter.*

internal class InstrumentationFilteringTests {

    @SlicedGeneratedTest(all = true)
    fun BuildConfigurator.testExclude() {
        addKoverProject {
            sourcesFrom("simple")
            testTasks {
                excludes("org.jetbrains.*Exa?ple*")
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
    fun BuildConfigurator.testExcludeInclude() {
        addKoverProject {
            sourcesFrom("simple")
            testTasks {
                includes("org.jetbrains.*Cla?s")
                excludes("org.jetbrains.*Exa?ple*")
            }
        }
        run("build", "koverXmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.ExampleClass").assertFullyMissed()
                classCounter("org.jetbrains.Unused").assertFullyMissed()
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }
    }

    @SlicedGeneratedTest(all = true)
    fun BuildConfigurator.testExcludeByKoverExtension() {
        addKoverProject {
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
        run("build", "koverXmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.ExampleClass").assertFullyMissed()
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }
    }

    @SlicedGeneratedTest(all = true)
    fun BuildConfigurator.testExcludeIncludeByKoverExtension() {
        addKoverProject {
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
        run("build", "koverXmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.ExampleClass").assertFullyMissed()
                classCounter("org.jetbrains.Unused").assertFullyMissed()
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }
    }

    @SlicedGeneratedTest(all = true)
    fun SlicedBuildConfigurator.testDisableInstrumentationOfTask() {
        addKoverProject {
            sourcesFrom("simple")
            kover {
                instrumentation {
                    excludeTasks += defaultTestTask(slice.type)
                }
            }
        }
        run("koverXmlReport") {
            // if task `test` is excluded from instrumentation then the binary report is not created for it
            checkDefaultBinaryReport(false)
        }
    }

}
