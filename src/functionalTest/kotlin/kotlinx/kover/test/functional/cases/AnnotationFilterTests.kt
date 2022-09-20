/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.cases

import kotlinx.kover.api.CounterType.LINE
import kotlinx.kover.api.VerificationValueType.COVERED_COUNT
import kotlinx.kover.test.functional.framework.checker.defaultMergedXmlReport
import kotlinx.kover.test.functional.framework.checker.defaultXmlReport
import kotlinx.kover.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.test.functional.framework.starter.*


internal class AnnotationFilterTests {

    @GeneratedTest
    fun BuildConfigurator.testExclusions() {
        addKoverProject {
            sourcesFrom("annotations-main")
            kover {
                filters {
                    annotations {
                        excludes += "org.jetbrains.Exclude"
                        excludes += "*ByMask"
                    }
                }
                verify {
                    rule {
                        bound {
                            counter = LINE
                            valueType = COVERED_COUNT
                            minValue = 6
                            maxValue = 6
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
        addKoverProject {
            sourcesFrom("annotations-main")
            kover {
                filters {
                    annotations {
                        excludes += "org.jetbrains.Exclude"
                        excludes += "*ByMask"
                    }
                }

                xmlReport {
                    filters {
                        annotations {
                            excludes += "org.jetbrains.OverriddenExclude"
                        }
                    }
                }

                verify {
                    rule {
                        bound {
                            counter = LINE
                            valueType = COVERED_COUNT
                            minValue = 11
                            maxValue = 11
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

    @GeneratedTest
    fun BuildConfigurator.testMergedExclusions() {
        addKoverProject {
            sourcesFrom("annotations-main")
            koverMerged {
                enable()
                filters {
                    annotations {
                        excludes += "org.jetbrains.Exclude"
                        excludes += "*ByMask"
                    }
                }

                verify {
                    rule {
                        bound {
                            counter = LINE
                            valueType = COVERED_COUNT
                            minValue = 6
                            maxValue = 6
                        }
                    }
                }
            }
        }

        run("koverMergedXmlReport", "check") {
            xml(defaultMergedXmlReport()) {
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
    fun BuildConfigurator.testMergedOverride() {
        addKoverProject {
            sourcesFrom("annotations-main")
            koverMerged {
                enable()
                filters {
                    annotations {
                        excludes += "org.jetbrains.Exclude"
                        excludes += "*ByMask"
                    }
                }

                xmlReport {
                    overrideAnnotationFilter {
                        excludes += "org.jetbrains.OverriddenExclude"
                    }
                }

                verify {
                    rule {
                        overrideAnnotationFilter {
                            excludes += "org.jetbrains.OverriddenExclude"
                        }
                        bound {
                            counter = LINE
                            valueType = COVERED_COUNT
                            minValue = 11
                            maxValue = 11
                        }
                    }
                }
            }
        }

        run("koverMergedXmlReport", "check") {
            xml(defaultMergedXmlReport()) {
                methodCounter("org.jetbrains.PartiallyExcludedClass", "function1").assertAbsent()
                methodCounter("org.jetbrains.PartiallyExcludedClass", "function2").assertFullyCovered()
            }
        }
    }

}
