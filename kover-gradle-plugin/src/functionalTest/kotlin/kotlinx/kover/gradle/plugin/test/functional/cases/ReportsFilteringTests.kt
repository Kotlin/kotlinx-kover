/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*

internal class ReportsFilteringTests {

    @SlicedGeneratedTest(allLanguages = true, allTools = true)
    fun BuildConfigurator.testXmlCommonExclude() {
        addProjectWithKover {
            sourcesFrom("simple")

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

    @SlicedGeneratedTest(allLanguages = true, allTools = true)
    fun BuildConfigurator.testXmlDefaultsExclude() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                reports {
                    filters {
                        excludes {
                            classes("org.*")
                        }
                    }
                    verify {
                        rule {
                            // without ExampleClass covered lines count = 2, but 4 with it
                            maxBound(2, aggregationForGroup = AggregationType.COVERED_COUNT)
                        }
                    }

                    total {
                        filters {
                            excludes {
                                classes("org.jetbrains.*Exa?ple*")
                            }
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

    @SlicedGeneratedTest(allLanguages = true, allTools = true)
    fun BuildConfigurator.testXmlExclude() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                reports {
                    filters {
                        excludes {
                            classes("foo.*")
                        }
                    }

                    total {
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
        }
        run("koverXmlReport", "koverVerify") {
            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertAbsent()
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }
    }

    @SlicedGeneratedTest(allLanguages = true, allTools = true)
    fun BuildConfigurator.testExcludeInclude() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                reports {
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
        }
        run("koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertAbsent()
                classCounter("org.jetbrains.Unused").assertAbsent()
                classCounter("org.jetbrains.SecondClass").assertFullyCovered()
            }
        }
    }


    /**
     * Check that when excluding packages, the excluding occurs starting from the root package,
     * and there is no search for any middle occurrence of the specified string.
     *
     * See https://github.com/Kotlin/kotlinx-kover/issues/543
     */
    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testPackageInTheMiddle() {
        addProjectWithKover {
            sourcesFrom("different-packages")

            kover {
                reports {
                    filters {
                        excludes {
                            packages("foo")
                        }
                    }

                }
            }
        }
        run("koverXmlReport") {
            xmlReport {
                classCounter("foo.bar.FooClass").assertAbsent()
                classCounter("org.jetbrains.foo.ExampleClass").assertCovered()
            }
        }
    }

}
