/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*

internal class TasksOrderingTests {
    @GeneratedTest
    fun BuildConfigurator.testProjectTasks() {
        addProjectWithKover {
            sourcesFrom("simple")
            kover {
                reports {
                    verify {
                        rule {
                            minBound(100)
                        }
                    }
                }
            }
        }
        run("koverVerify", "koverXmlReport", "koverHtmlReport", errorExpected = true) {
            // reports should be generated even if verification failed with an error
            checkDefaultReports()
        }
    }
}
