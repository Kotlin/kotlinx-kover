/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.cases

import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.dsl.MetricType.LINE
import kotlinx.kover.test.functional.framework.checker.defaultXmlReport
import kotlinx.kover.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.test.functional.framework.starter.*


internal class ReportAnnotationFilterTests {

    @GeneratedTest
    fun BuildConfigurator.testExclusions() {
        addProjectWithKover {
            sourcesFrom("annotations-main")
            koverReport {
                filters {
                    excludes {
                        annotated {
                            annotationName("org.jetbrains.Exclude", "*ByMask")
                        }
                    }
                }

                verify {
                    rule {
                        bound {
                            metric = LINE
                            aggregation = AggregationType.COVERED_COUNT
                            minValue = 9
                            maxValue = 9
                        }
                    }
                }
            }
        }

        run("koverXmlReport", "check") {
            xml(defaultXmlReport()) {
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
            koverReport {
                filters {
                    excludes {
                        annotated {
                            annotationName("org.jetbrains.Exclude", "*ByMask")
                        }
                    }
                }

                xml {
                    filters {
                        excludes {
                            annotated {
                                annotationName("org.jetbrains.OverriddenExclude")
                            }
                        }
                    }
                }

                verify {
                    rule {
                        filters {
                            // clear all filters
                        }

                        bound {
                            metric = LINE
                            aggregation = AggregationType.COVERED_COUNT
                            minValue = 16
                            maxValue = 16
                        }
                    }
                }
            }
        }

        run("koverXmlReport", "check") {
            xml(defaultXmlReport()) {
                methodCounter("org.jetbrains.PartiallyExcludedClass", "function1").assertAbsent()
                methodCounter("org.jetbrains.PartiallyExcludedClass", "function2").assertFullyCovered()
            }
        }
    }
}
