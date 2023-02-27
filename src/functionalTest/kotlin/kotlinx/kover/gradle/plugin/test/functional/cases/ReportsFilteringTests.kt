/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.defaultXmlReport
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*

internal class ReportsFilteringTests {

    @SlicedGeneratedTest(allLanguages = true, allTools = true)
    fun BuildConfigurator.testExclude() {
        addProjectWithKover {
            sourcesFrom("simple")

            koverReport {
                filters {
                    excludes {
                        classes("org.jetbrains.*Exa?ple*")
                    }
                }
            }
        }
        run("koverXmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.ExampleClass").assertAbsent()
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }
    }

    @SlicedGeneratedTest(allLanguages = true, allTools = true)
    fun BuildConfigurator.testExcludeInclude() {
        addProjectWithKover {
            sourcesFrom("simple")

            koverReport {
                filters {
                    excludes {
                        classes("org.jetbrains.*Exa?ple*")
                    }

                    includes {
                        classes("org.jetbrains.*Cla?s")
                    }
                }

            }
        }
        run("koverXmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.ExampleClass").assertAbsent()
                classCounter("org.jetbrains.Unused").assertAbsent()
                classCounter("org.jetbrains.SecondClass").assertFullyCovered()
            }
        }
    }

}
