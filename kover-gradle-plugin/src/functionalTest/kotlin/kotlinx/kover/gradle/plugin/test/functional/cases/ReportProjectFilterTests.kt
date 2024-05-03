/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.SlicedGeneratedTest

internal class ReportProjectFilterTests {
    private val subprojectPath = ":common"

    @SlicedGeneratedTest(allLanguages = true)
    fun BuildConfigurator.testInclude() {
        addProjectWithKover(subprojectPath) {
            sourcesFrom("multiproject-common")
        }

        addProjectWithKover {
            sourcesFrom("multiproject-user")
            dependencyKover(subprojectPath)

            kover {
                reports {
                    filters {
                        includes {
                            // show classes only from ':common' project
                            projects.add(":c?m*")
                        }
                    }
                }
            }
        }

        run(":koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.CommonClass").assertCovered()
                classCounter("org.jetbrains.CommonInternalClass").assertCovered()
                classCounter("org.jetbrains.UserClass").assertAbsent()
            }
        }
    }

    @SlicedGeneratedTest(allLanguages = true)
    fun BuildConfigurator.testExclude() {
        addProjectWithKover(subprojectPath) {
            sourcesFrom("multiproject-common")
        }

        addProjectWithKover {
            sourcesFrom("multiproject-user")
            dependencyKover(subprojectPath)

            kover {
                reports {
                    filters {
                        excludes {
                            // exclude classes from ':common' project
                            projects.add(":c?m*")
                        }
                    }
                }
            }
        }

        run(":koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.CommonClass").assertAbsent()
                classCounter("org.jetbrains.CommonInternalClass").assertAbsent()
                classCounter("org.jetbrains.UserClass").assertCovered()
            }
        }
    }

    @SlicedGeneratedTest(allLanguages = true)
    fun BuildConfigurator.testIncludeAndExclude() {
        addProjectWithKover(subprojectPath) {
            sourcesFrom("multiproject-common")
        }

        addProjectWithKover {
            sourcesFrom("multiproject-user")
            dependencyKover(subprojectPath)

            kover {
                reports {
                    filters {
                        includes {
                            // include all projects
                            projects.add(":*")
                        }
                        excludes {
                            // exclude classes from ':' project
                            projects.add(":")
                        }
                    }
                }
            }
        }

        run(":koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.CommonClass").assertCovered()
                classCounter("org.jetbrains.CommonInternalClass").assertCovered()
                classCounter("org.jetbrains.UserClass").assertAbsent()
            }
        }
    }
}
