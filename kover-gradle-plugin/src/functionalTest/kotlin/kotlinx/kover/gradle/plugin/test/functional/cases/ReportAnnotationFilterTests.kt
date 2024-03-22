/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit.LINE
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*
import java.io.Closeable


internal class ReportAnnotationFilterTests {

    @GeneratedTest
    fun BuildConfigurator.testExclusions() {
        addProjectWithKover {
            sourcesFrom("annotations-main")
            kover {
                reports {
                    filters {
                        excludes {
                            annotatedBy("org.jetbrains.Exclude", "*ByMask")
                        }
                    }

                    verify {
                        rule {
                            bound {
                                coverageUnits.set(LINE)
                                aggregationForGroup.set(AggregationType.COVERED_COUNT)
                                minValue.set(9)
                                maxValue.set(9)
                            }
                        }
                    }
                }
            }
        }
    }

    class Foo : AutoCloseable {
        fun function() {
            println("function")
        }

        override fun close() {
            println("foo")
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testInclusions() {
        addProjectWithKover {
            sourcesFrom("annotations-mix")
            kover {
                reports {
                    filters {
                        excludes {
                            classes("*ByName")
                            annotatedBy("org.jetbrains.Exclude")
                        }
                        includes {
                            annotatedBy("*.Include")
                        }
                    }
                }
            }
        }

        run("koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.NotAnnotatedClass").assertAbsent()
                classCounter("org.jetbrains.ExcludedClass").assertAbsent()
                classCounter("org.jetbrains.ExcludedByName").assertAbsent()
                classCounter("org.jetbrains.TogetherClass").assertAbsent()

                classCounter("org.jetbrains.IncludedClass").assertFullyMissed()
                methodCounter("org.jetbrains.IncludedClass", "function").assertFullyMissed()

                classCounter("org.jetbrains.MixedClass").assertFullyMissed()
                methodCounter("org.jetbrains.MixedClass", "function1").assertFullyMissed()
                methodCounter("org.jetbrains.MixedClass", "function2").assertAbsent()
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testOverride() {
        addProjectWithKover {
            sourcesFrom("annotations-main")
            kover {
                reports {
                    filters {
                        excludes {
                            annotatedBy("org.jetbrains.Exclude", "*ByMask")
                        }
                    }
                    verify {
                        rule {
                            bound {
                                coverageUnits.set(LINE)
                                aggregationForGroup.set(AggregationType.COVERED_COUNT)
                                minValue.set(15)
                                maxValue.set(15)
                            }
                        }
                    }

                    total {
                        filters {
                            excludes {
                                annotatedBy("org.jetbrains.OverriddenExclude")
                            }
                        }
                    }
                }
            }
        }

        run("koverXmlReport", "koverHtmlReport", "check") {
            xmlReport {
                methodCounter("org.jetbrains.PartiallyExcludedClass", "function1").assertAbsent()
                methodCounter("org.jetbrains.PartiallyExcludedClass", "function2").assertFullyCovered()
            }
        }
    }
}
