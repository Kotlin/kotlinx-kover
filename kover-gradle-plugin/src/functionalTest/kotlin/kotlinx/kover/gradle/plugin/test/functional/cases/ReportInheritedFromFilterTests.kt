/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.GeneratedTest

internal class ReportInheritedFromFilterTests {
    @GeneratedTest
    fun BuildConfigurator.testExclusions() {
        addProjectWithKover {
            sourcesFrom("inherited-main")
            kover {
                reports {
                    filters {
                        excludes {
                            inheritedFrom("*.Interface", "org.jetbrains.A", "*AutoCloseable")
                        }
                    }
                }
            }
        }

        run("koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.RegularClass").assertFullyMissed()
                classCounter("org.jetbrains.CloseableClass").assertFullyMissed()
                classCounter("org.jetbrains.A").assertFullyMissed()

                classCounter("org.jetbrains.B").assertAbsent()
                classCounter("org.jetbrains.C").assertAbsent()
                classCounter("org.jetbrains.D").assertAbsent()
                classCounter("org.jetbrains.AChild").assertAbsent()
                classCounter("org.jetbrains.BChild").assertAbsent()
                classCounter("org.jetbrains.CChild").assertAbsent()
                classCounter("org.jetbrains.DChild").assertAbsent()
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testInclusions() {
        addProjectWithKover {
            sourcesFrom("inherited-main")
            kover {
                reports {
                    filters {
                        includes {
                            inheritedFrom("*.Interface", "org.jetbrains.A", "*AutoCloseable")
                        }
                    }
                }
            }
        }

        run("koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.RegularClass").assertAbsent()
                classCounter("org.jetbrains.CloseableClass").assertAbsent()
                classCounter("org.jetbrains.A").assertAbsent()

                classCounter("org.jetbrains.B").assertFullyMissed()
                classCounter("org.jetbrains.C").assertFullyMissed()
                classCounter("org.jetbrains.D").assertFullyMissed()
                classCounter("org.jetbrains.AChild").assertFullyMissed()
                classCounter("org.jetbrains.BChild").assertFullyMissed()
                classCounter("org.jetbrains.CChild").assertFullyMissed()
                classCounter("org.jetbrains.DChild").assertFullyMissed()
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testIncludeAndExclude() {
        addProjectWithKover {
            sourcesFrom("inherited-main")
            kover {
                reports {
                    filters {
                        includes {
                            inheritedFrom("*.Interface", "org.jetbrains.A", "*AutoCloseable")
                        }

                        excludes {
                            inheritedFrom("*.B", "*.AutoCloseable")
                        }
                    }
                }
            }
        }

        run("koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.RegularClass").assertAbsent()
                classCounter("org.jetbrains.CloseableClass").assertAbsent()
                classCounter("org.jetbrains.BChild").assertAbsent()
                classCounter("org.jetbrains.A").assertAbsent()
                classCounter("org.jetbrains.D").assertAbsent()
                classCounter("org.jetbrains.DChild").assertAbsent()

                classCounter("org.jetbrains.B").assertFullyMissed()
                classCounter("org.jetbrains.C").assertFullyMissed()
                classCounter("org.jetbrains.AChild").assertFullyMissed()
                classCounter("org.jetbrains.CChild").assertFullyMissed()
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testDifferentIncludeFilters() {
        addProjectWithKover {
            sourcesFrom("inherited-main")
            kover {
                reports {
                    filters {
                        includes {
                            // for includes 'AND' rule should work
                            inheritedFrom("org.jetbrains.A")
                            classes("*.*Child")
                        }
                    }
                }
            }
        }

        run("koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.A").assertAbsent()
                classCounter("org.jetbrains.B").assertAbsent()

                classCounter("org.jetbrains.BChild").assertFullyMissed()
                classCounter("org.jetbrains.AChild").assertFullyMissed()
            }
        }
    }
    @GeneratedTest
    fun BuildConfigurator.testDifferentExcludeFilters() {
        addProjectWithKover {
            sourcesFrom("inherited-main")
            kover {
                reports {
                    filters {
                        excludes {
                            // for excludes 'OR' rule should work
                            inheritedFrom("org.jetbrains.A")
                            classes("*.*Child")
                        }
                    }
                }
            }
        }

        run("koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.A").assertFullyMissed()

                // excluded as inheritor of A
                classCounter("org.jetbrains.B").assertAbsent()

                // excluded by name
                classCounter("org.jetbrains.AChild").assertAbsent()
                classCounter("org.jetbrains.BChild").assertAbsent()
                classCounter("org.jetbrains.CChild").assertAbsent()
            }
        }
    }

}