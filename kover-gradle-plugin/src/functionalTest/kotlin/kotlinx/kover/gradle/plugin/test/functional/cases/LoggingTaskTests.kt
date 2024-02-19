/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.commons.CoverageToolVendor
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import kotlinx.kover.gradle.plugin.dsl.MetricType
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.GeneratedTest
import kotlin.test.assertEquals

internal class LoggingTaskTests {
    @GeneratedTest
    fun BuildConfigurator.testCaching() {
        addProjectWithKover {
            sourcesFrom("simple")
            useLocalCache()
        }

        run(":koverLog", "--build-cache") {
            checkOutcome("koverLog", "SUCCESS")
            checkOutcome("koverPrintCoverage", "SUCCESS")
        }
        run(":koverLog", "--build-cache") {
            checkOutcome("koverLog", "UP-TO-DATE")
            checkOutcome("koverPrintCoverage", "SUCCESS")
        }
        run("clean", "--build-cache")
        run(":koverLog", "--build-cache") {
            checkOutcome("koverLog", "FROM-CACHE")
            checkOutcome("koverPrintCoverage", "SUCCESS")
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testDefaultFormat() {
        addProjectWithKover {
            sourcesFrom("simple")
        }

        run(":koverLog") {
            checkOutcome("koverPrintCoverage", "SUCCESS")
            taskOutput("koverPrintCoverage") {
                assertEquals("application line coverage: 57.1429%\n\n", this)
            }
        }
    }

    @GeneratedTest(tool = CoverageToolVendor.JACOCO)
    fun BuildConfigurator.testDefaultFormatJacoco() {
        addProjectWithKover {
            sourcesFrom("simple")
        }

        run(":koverLog") {
            checkOutcome("koverPrintCoverage", "SUCCESS")
            taskOutput("koverPrintCoverage") {
                assertEquals("application line coverage: 50%\n\n", this)
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testHeaderAndFormat() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                reports {
                    total {
                        log {
                            header.set("Custom header")
                            format.set("My format for <entity> is <value>")
                        }
                    }
                }
            }
        }

        run(":koverLog") {
            checkOutcome("koverPrintCoverage", "SUCCESS")
            taskOutput("koverPrintCoverage") {
                assertEquals("Custom header\nMy format for application is 57.1429\n\n", this)
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testCustomFormatKover() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                reports {
                    total {
                        log {
                            header.set("Coverage for classes:")
                            format.set("Class <entity> covered instructions=<value>")
                            groupBy.set(GroupingEntityType.CLASS)
                            aggregationForGroup.set(AggregationType.COVERED_COUNT)
                            coverageUnits.set(MetricType.INSTRUCTION)
                        }
                    }
                }
            }
        }

        run(":koverLog") {
            checkOutcome("koverPrintCoverage", "SUCCESS")
            taskOutput("koverPrintCoverage") {
                assertEquals(
                    "Coverage for classes:\n" +
                            "Class org.jetbrains.ExampleClass covered instructions=5\n" +
                            "Class org.jetbrains.SecondClass covered instructions=5\n" +
                            "Class org.jetbrains.Unused covered instructions=0\n\n",
                    this
                )
            }
        }
    }

    @GeneratedTest(tool = CoverageToolVendor.JACOCO)
    fun BuildConfigurator.testCustomFormatJacoco() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                reports {
                    total {
                        log {
                            header.set("Coverage for classes:")
                            format.set("Class <entity> covered instructions=<value>")
                            groupBy.set(GroupingEntityType.CLASS)
                            aggregationForGroup.set(AggregationType.COVERED_COUNT)
                            coverageUnits.set(MetricType.INSTRUCTION)
                        }
                    }
                }
            }
        }

        run(":koverLog") {
            checkOutcome("koverPrintCoverage", "SUCCESS")
            taskOutput("koverPrintCoverage") {
                assertEquals(
                    "Coverage for classes:\n" +
                            "Class org.jetbrains.ExampleClass covered instructions=7\n" +
                            "Class org.jetbrains.SecondClass covered instructions=7\n" +
                            "Class org.jetbrains.Unused covered instructions=0\n\n",
                    this
                )
            }
        }
    }

}