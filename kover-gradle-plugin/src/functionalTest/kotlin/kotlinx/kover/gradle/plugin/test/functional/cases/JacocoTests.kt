/*
 * Copyright 2017-2025 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.commons.CoverageToolVendor
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.GeneratedTest

internal class JacocoTests {
    @GeneratedTest(tool = CoverageToolVendor.JACOCO)
    fun BuildConfigurator.testMinimalVersion() {
        addProjectWithKover {
            sourcesFrom("simple")
            kover {
                useJacoco("0.8.7")
            }

            kover {
                reports {
                    filters {
                        excludes {
                            classes("org.jetbrains.*Exa?ple*")
                        }
                    }
                    verify {
                        rule {
                            // without ExampleClass covered lines count = 2, but 4 with it
                            maxBound(2, aggregationForGroup = AggregationType.COVERED_COUNT)
                        }
                    }
                }
            }
        }
        run("koverXmlReport", "koverVerify") {
            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertAbsent()
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }
    }
}