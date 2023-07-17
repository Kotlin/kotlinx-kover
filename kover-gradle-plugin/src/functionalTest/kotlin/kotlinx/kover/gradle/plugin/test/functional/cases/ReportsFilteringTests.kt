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

            koverReport {
                filters {
                    excludes {
                        classes("org.jetbrains.*Exa?ple*")
                    }
                }
                verify {
                    rule {
                        // without ExampleClass covered lines count = 2, but 4 with it
                        maxBound(2, aggregation = AggregationType.COVERED_COUNT)
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

            koverReport {
                filters {
                    excludes {
                        classes("org.*")
                    }
                }
                verify {
                    rule {
                        // without ExampleClass covered lines count = 2, but 4 with it
                        maxBound(2, aggregation = AggregationType.COVERED_COUNT)
                    }
                }

                defaults {
                    filters {
                        excludes {
                            classes("org.jetbrains.*Exa?ple*")
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

            koverReport {
                filters {
                    excludes {
                        classes("foo.*")
                    }
                }

                defaults {
                    filters {
                        excludes {
                            classes("org.*")
                        }
                    }

                    xml {
                        filters {
                            excludes {
                                classes("org.jetbrains.*Exa?ple*")
                            }
                        }
                    }

                    verify {
                        filters {
                            excludes {
                                classes("org.jetbrains.*Exa?ple*")
                            }
                        }
                        rule {
                            // without ExampleClass covered lines count = 2, but 4 with it
                            maxBound(2, aggregation = AggregationType.COVERED_COUNT)
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
            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertAbsent()
                classCounter("org.jetbrains.Unused").assertAbsent()
                classCounter("org.jetbrains.SecondClass").assertFullyCovered()
            }
        }
    }

}
