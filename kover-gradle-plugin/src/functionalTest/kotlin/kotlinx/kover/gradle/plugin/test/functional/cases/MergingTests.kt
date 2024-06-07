/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.GeneratedTest

internal class MergingTests {

    @GeneratedTest
    fun BuildConfigurator.testRootNoProjects() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                merge {
                    // no projects
                }
            }
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
        }
        addProjectWithKover(":two") {
            sourcesFrom("two")
        }

        run(":koverXmlReport", errorExpected = true)
    }

    @GeneratedTest
    fun BuildConfigurator.testRootSubprojects() {
        addProjectWithKover {
            sourcesFrom("simple")
            kover {
                merge {
                    // merge with all subprojects
                    subprojects()
                }
            }
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
        }
        addProjectWithKover(":two") {
            sourcesFrom("two")
        }

        run(":koverXmlReport") {
            checkOutcome(":koverGenerateArtifact", "SUCCESS")
            checkOutcome(":one:koverGenerateArtifact", "SUCCESS")
            checkOutcome(":two:koverGenerateArtifact", "SUCCESS")

            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertCovered()
                classCounter("org.jetbrains.one.OneClass").assertCovered()
                classCounter("org.jetbrains.two.TwoClass").assertCovered()
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testRootSubprojectsWithProperty() {
        addProjectWithKover {
            sourcesFrom("simple")
            kover {
                // merge with all subprojects
                merge.subprojects()
            }
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
        }
        addProjectWithKover(":two") {
            sourcesFrom("two")
        }

        run(":koverXmlReport") {
            checkOutcome(":koverGenerateArtifact", "SUCCESS")
            checkOutcome(":one:koverGenerateArtifact", "SUCCESS")
            checkOutcome(":two:koverGenerateArtifact", "SUCCESS")

            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertCovered()
                classCounter("org.jetbrains.one.OneClass").assertCovered()
                classCounter("org.jetbrains.two.TwoClass").assertCovered()
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testRootSubprojectsByPath() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover { scope ->
                merge {
                    // merge only with subproject ':one'
                    subprojects {
                        scope.line("it.path == \":one\"")
                        true
                    }
                }
            }
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
        }
        addProjectWithKover(":two") {
            sourcesFrom("two")
        }

        run(":koverXmlReport") {
            checkOutcome(":koverGenerateArtifact", "SUCCESS")
            checkOutcome(":one:koverGenerateArtifact", "SUCCESS")
            taskNotCalled(":two:koverGenerateArtifact")

            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertCovered()
                classCounter("org.jetbrains.one.OneClass").assertCovered()
                classCounter("org.jetbrains.two.TwoClass").assertAbsent()
            }
        }
    }



    @GeneratedTest
    fun BuildConfigurator.testRootAllProjects() {
        addProjectWithKover {
            sourcesFrom("simple")
            kover {
                merge {
                    // merge with all projects
                    allProjects()
                }
            }
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
        }
        addProjectWithKover(":two") {
            sourcesFrom("two")
        }

        run(":koverXmlReport") {
            checkOutcome(":koverGenerateArtifact", "SUCCESS")
            checkOutcome(":one:koverGenerateArtifact", "SUCCESS")
            checkOutcome(":two:koverGenerateArtifact", "SUCCESS")

            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertCovered()
                classCounter("org.jetbrains.one.OneClass").assertCovered()
                classCounter("org.jetbrains.two.TwoClass").assertCovered()
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testRootAllProjectsByPath() {
        addProjectWithKover {
            sourcesFrom("simple")
            kover { scope ->
                merge {
                    // merge with all subprojects
                    allProjects {
                        scope.line("it.path == \":two\"")
                        false
                    }
                }
            }
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
        }
        addProjectWithKover(":two") {
            sourcesFrom("two")
        }

        run(":koverXmlReport") {
            checkOutcome(":koverGenerateArtifact", "SUCCESS")
            taskNotCalled(":one:koverGenerateArtifact")
            checkOutcome(":two:koverGenerateArtifact", "SUCCESS")

            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertCovered()
                classCounter("org.jetbrains.one.OneClass").assertAbsent()
                classCounter("org.jetbrains.two.TwoClass").assertCovered()
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testRootSpecified() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                merge {
                    projects(":two")
                }
            }
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
        }
        addProjectWithKover(":two") {
            sourcesFrom("two")
        }

        run(":koverXmlReport") {
            checkOutcome(":koverGenerateArtifact", "SUCCESS")
            taskNotCalled(":one:koverGenerateArtifact")
            checkOutcome(":two:koverGenerateArtifact", "SUCCESS")

            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertCovered()
                classCounter("org.jetbrains.one.OneClass").assertAbsent()
                classCounter("org.jetbrains.two.TwoClass").assertCovered()
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testIntermediateSubprojects() {
        addProjectWithKover {
            sourcesFrom("simple")
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
            kover {
                merge {
                    // merge with all subprojects
                    subprojects()
                }
            }
        }
        addProjectWithKover(":one:two") {
            sourcesFrom("two")
        }

        run(":one:koverXmlReport") {
            taskNotCalled(":koverGenerateArtifact")
            checkOutcome(":one:koverGenerateArtifact", "SUCCESS")
            checkOutcome(":one:two:koverGenerateArtifact", "SUCCESS")

            subproject(":one") {
                xmlReport {
                    classCounter("org.jetbrains.ExampleClass").assertAbsent()
                    classCounter("org.jetbrains.one.OneClass").assertCovered()
                    classCounter("org.jetbrains.two.TwoClass").assertCovered()
                }
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testIntermediateSubprojectsByPath() {
        addProjectWithKover {
            sourcesFrom("simple")
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
            kover { scope ->
                merge {
                    // merge with all subprojects
                    subprojects {
                        scope.line("it.path == \":unexisted\"")
                        false
                    }
                }
            }
        }
        addProjectWithKover(":one:two") {
            sourcesFrom("two")
        }

        run(":one:koverXmlReport") {
            taskNotCalled(":koverGenerateArtifact")
            checkOutcome(":one:koverGenerateArtifact", "SUCCESS")
            taskNotCalled(":one:two:koverGenerateArtifact")

            subproject(":one") {
                xmlReport {
                    classCounter("org.jetbrains.ExampleClass").assertAbsent()
                    classCounter("org.jetbrains.one.OneClass").assertCovered()
                    classCounter("org.jetbrains.two.TwoClass").assertAbsent()
                }
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testIntermediateAllProjects() {
        addProjectWithKover {
            sourcesFrom("simple")
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
            kover {
                merge {
                    // merge with all projects, including top-level one
                    allProjects()
                }
            }
        }
        addProjectWithKover(":one:two") {
            sourcesFrom("two")
        }

        run(":one:koverXmlReport") {
            checkOutcome(":koverGenerateArtifact", "SUCCESS")
            checkOutcome(":one:koverGenerateArtifact", "SUCCESS")
            checkOutcome(":one:two:koverGenerateArtifact", "SUCCESS")

            subproject(":one") {
                xmlReport {
                    classCounter("org.jetbrains.ExampleClass").assertCovered()
                    classCounter("org.jetbrains.one.OneClass").assertCovered()
                    classCounter("org.jetbrains.two.TwoClass").assertCovered()
                }
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testIntermediateAllProjectsByPath() {
        addProjectWithKover {
            sourcesFrom("simple")
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
            kover { scope ->
                merge {
                    // merge with all subprojects
                    allProjects {
                        scope.line("""it.path == ":" """)
                        false
                    }
                }
            }
        }
        addProjectWithKover(":one:two") {
            sourcesFrom("two")
        }

        run(":one:koverXmlReport") {
            checkOutcome(":koverGenerateArtifact", "SUCCESS")
            checkOutcome(":one:koverGenerateArtifact", "SUCCESS")
            taskNotCalled(":one:two:koverGenerateArtifact")

            subproject(":one") {
                xmlReport {
                    classCounter("org.jetbrains.ExampleClass").assertCovered()
                    classCounter("org.jetbrains.one.OneClass").assertCovered()
                    classCounter("org.jetbrains.two.TwoClass").assertAbsent()
                }
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testIntermediateSpecified() {
        addProjectWithKover {
            sourcesFrom("simple")
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
            kover {
                merge {
                    // merge with ':one:two' subproject by name
                    projects("two")
                }
            }
        }
        addProjectWithKover(":one:two") {
            sourcesFrom("two")
        }

        run(":one:koverXmlReport") {
            taskNotCalled(":koverGenerateArtifact")
            checkOutcome(":one:koverGenerateArtifact", "SUCCESS")
            checkOutcome(":one:two:koverGenerateArtifact", "SUCCESS")

            subproject(":one") {
                xmlReport {
                    classCounter("org.jetbrains.ExampleClass").assertAbsent()
                    classCounter("org.jetbrains.one.OneClass").assertCovered()
                    classCounter("org.jetbrains.two.TwoClass").assertCovered()
                }
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testConfigInstrumentation() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                merge {
                    subprojects()

                    instrumentation {
                        // change instrumentation in all selected projects
                        excludedClasses.add("org.jetbrains.*")
                    }
                }
            }
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
        }
        addProjectWithKover(":two") {
            sourcesFrom("two")
        }

        run(":koverXmlReport") {
            checkOutcome(":koverGenerateArtifact", "SUCCESS")
            checkOutcome(":one:koverGenerateArtifact", "SUCCESS")
            checkOutcome(":two:koverGenerateArtifact", "SUCCESS")

            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertFullyMissed()
                classCounter("org.jetbrains.one.OneClass").assertFullyMissed()
                classCounter("org.jetbrains.two.TwoClass").assertFullyMissed()
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testConfigSources() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                merge {
                    subprojects()

                    // change sources in all selected projects
                    sources {
                        excludedSourceSets.add("main")
                    }
                }
            }
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
        }
        addProjectWithKover(":two") {
            sourcesFrom("two")
        }

        run(":koverXmlReport") {
            checkOutcome(":koverGenerateArtifact", "SUCCESS")
            checkOutcome(":one:koverGenerateArtifact", "SUCCESS")
            checkOutcome(":two:koverGenerateArtifact", "SUCCESS")

            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertAbsent()
                classCounter("org.jetbrains.one.OneClass").assertAbsent()
                classCounter("org.jetbrains.two.TwoClass").assertAbsent()
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testConfigVariants() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover { scope ->
                merge {
                    subprojects()

                    // change sources in all selected projects
                    createVariant("custom") {
                        // bad Kotlin vararg support if vararg is not the last parameter
                        scope.line("""add("jvm")  """)
                    }
                }
            }
        }
        addProjectWithKover(":one") {
            sourcesFrom("one")
        }
        addProjectWithKover(":two") {
            sourcesFrom("two")
        }

        run(":koverXmlReportCustom") {
            checkOutcome(":koverGenerateArtifactCustom", "SUCCESS")
            checkOutcome(":one:koverGenerateArtifactCustom", "SUCCESS")
            checkOutcome(":two:koverGenerateArtifactCustom", "SUCCESS")

            xmlReport("custom") {
                classCounter("org.jetbrains.ExampleClass").assertCovered()
                classCounter("org.jetbrains.one.OneClass").assertCovered()
                classCounter("org.jetbrains.two.TwoClass").assertCovered()
            }
        }
    }

}