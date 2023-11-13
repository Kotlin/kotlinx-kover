/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.dsl.MetricType.LINE
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*


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
                                min.set(9)
                                max.set(9)
                            }
                        }
                    }
                }
            }
        }

        run("koverXmlReport", "check") {
            xmlReport {
                methodCounter("org.jetbrains.NotExcludedClass", "function").assertFullyCovered()
                classCounter("org.jetbrains.ExcludedClass").assertAbsent()
                methodCounter("org.jetbrains.PartiallyExcludedClass", "function1").assertFullyCovered()
                methodCounter("org.jetbrains.PartiallyExcludedClass", "function2").assertAbsent()
                methodCounter("org.jetbrains.PartiallyExcludedClass", "inlined").assertAbsent()

                methodCounter("org.jetbrains.SourcesKt", "inlinedExcluded").assertAbsent()
                methodCounter("org.jetbrains.SourcesKt", "inlinedNotExcluded").assertFullyCovered()
                methodCounter("org.jetbrains.SourcesKt", "notExcluded").assertFullyCovered()
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
                        filters {
                            // clear all filters
                        }
                        rule {
                            bound {
                                coverageUnits.set(LINE)
                                aggregationForGroup.set(AggregationType.COVERED_COUNT)
                                min.set(15)
                                max.set(15)
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
