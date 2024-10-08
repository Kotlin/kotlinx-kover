/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*

internal class InstrumentationFilteringTests {

    @SlicedGeneratedTest(all = true)
    fun BuildConfigurator.testExclude() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                currentProject {
                    instrumentation {
                        excludedClasses.add("org.jetbrains.*Exa?ple*")
                    }
                }
            }
        }

        run("build", "koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertFullyMissed()
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }
    }

    @SlicedGeneratedTest(all = true)
    fun BuildConfigurator.testInclude() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                currentProject {
                    instrumentation {
                        includedClasses.add("*.ExampleClass")
                    }
                }
            }
        }
    }

    @SlicedGeneratedTest(all = true)
    fun BuildConfigurator.testMixed() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                currentProject {
                    instrumentation {
                        includedClasses.add("*Class")
                        excludedClasses.add("*.SecondClass")
                    }
                }
            }
        }

        run("build", "koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertCovered()
                classCounter("org.jetbrains.SecondClass").assertFullyMissed()
            }
        }
    }

    @SlicedGeneratedTest(allLanguages = true)
    fun BuildConfigurator.testDisableAll() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                currentProject {
                    instrumentation {
                        disabledForAll.set(true)
                    }
                }
            }
        }

        run("build", "koverXmlReport") {
            checkOutcome("test", "SUCCESS")
            checkDefaultBinReport(false)
        }
    }

    @SlicedGeneratedTest(allLanguages = true)
    fun BuildConfigurator.testDisableByName() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                currentProject {
                    instrumentation {
                        disabledForTestTasks.add("test")
                    }
                }
            }
        }

        run("build") {
            checkOutcome("test", "SUCCESS")
            checkDefaultBinReport(false)
        }
    }

}
