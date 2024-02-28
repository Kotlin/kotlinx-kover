/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.buildFromTemplate
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

internal class VerificationTests {
    @SlicedGeneratedTest(allLanguages = true, allTools = true)
    fun BuildConfigurator.testVerified() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover{
                reports {
                    verify {
                        rule("test rule") {
                            bound {
                                minValue.set(50)
                                maxValue.set(60)
                            }
                            bound {
                                aggregationForGroup.set(AggregationType.COVERED_COUNT)
                                minValue.set(2)
                                maxValue.set(10)
                            }
                        }
                    }
                }
            }
        }

        run("koverVerify", "--stacktrace")
    }

    @SlicedGeneratedTest(allLanguages = true, allTools = true)
    fun BuildConfigurator.testVerificationError() {
        addProjectWithKover {
            sourcesFrom("verification")

            kover {
                reports {
                    verify {
                        rule("counts rule") {
                            bound {
                                minValue.set(58)
                                maxValue.set(60)
                            }
                            bound {
                                aggregationForGroup.set(AggregationType.COVERED_COUNT)
                                minValue.set(2)
                                maxValue.set(3)
                            }
                        }
                        rule("fully uncovered instructions by classes") {
                            groupBy.set(GroupingEntityType.CLASS)
                            bound {
                                coverageUnits.set(CoverageUnit.INSTRUCTION)
                                aggregationForGroup.set(AggregationType.MISSED_PERCENTAGE)
                                minValue.set(100)
                            }
                        }
                        rule("fully covered instructions by packages") {
                            groupBy.set(GroupingEntityType.PACKAGE)
                            bound {
                                coverageUnits.set(CoverageUnit.INSTRUCTION)
                                aggregationForGroup.set(AggregationType.COVERED_PERCENTAGE)
                                minValue.set(100)
                            }
                        }
                        rule("branches by classes") {
                            groupBy.set(GroupingEntityType.CLASS)
                            bound {
                                coverageUnits.set(CoverageUnit.BRANCH)
                                aggregationForGroup.set(AggregationType.COVERED_COUNT)
                                minValue.set(1000)
                            }
                        }
                        rule("missed packages") {
                            groupBy.set(GroupingEntityType.PACKAGE)
                            bound {
                                aggregationForGroup.set(AggregationType.MISSED_COUNT)
                                maxValue.set(1)
                            }
                        }
                    }
                }
            }
        }

        run("koverHtmlReport", "koverVerify", errorExpected = true) {
            verification {
                assertKoverResult("""Rule 'counts rule' violated:
  lines covered percentage is 46.590900, but expected minimum is 58
  lines covered count is 41, but expected maximum is 3
Rule 'fully uncovered instructions by classes' violated:
  instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.FullyCovered' is 0.000000, but expected minimum is 100
  instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.PartiallyCoveredFirst' is *, but expected minimum is 100
  instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.PartiallyCoveredSecond' is *, but expected minimum is 100
  instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubFullyCovered' is 0.000000, but expected minimum is 100
  instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubPartiallyCoveredFirst' is *, but expected minimum is 100
  instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubPartiallyCoveredSecond' is *, but expected minimum is 100
Rule 'fully covered instructions by packages' violated:
  instructions covered percentage for package 'org.jetbrains.kover.test.functional.verification' is *, but expected minimum is 100
  instructions covered percentage for package 'org.jetbrains.kover.test.functional.verification.subpackage' is *, but expected minimum is 100
Rule 'branches by classes' violated:
  branches covered count for class 'org.jetbrains.kover.test.functional.verification.FullyCovered' is 0, but expected minimum is 1000
  branches covered count for class 'org.jetbrains.kover.test.functional.verification.PartiallyCoveredFirst' is 2, but expected minimum is 1000
  branches covered count for class 'org.jetbrains.kover.test.functional.verification.PartiallyCoveredSecond' is 1, but expected minimum is 1000
  branches covered count for class 'org.jetbrains.kover.test.functional.verification.Uncovered' is 0, but expected minimum is 1000
  branches covered count for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubFullyCovered' is 0, but expected minimum is 1000
  branches covered count for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubPartiallyCoveredFirst' is 0, but expected minimum is 1000
  branches covered count for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubPartiallyCoveredSecond' is 1, but expected minimum is 1000
  branches covered count for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubUncovered' is 0, but expected minimum is 1000
Rule 'missed packages' violated:
  lines missed count for package 'org.jetbrains.kover.test.functional.verification' is 23, but expected maximum is 1
  lines missed count for package 'org.jetbrains.kover.test.functional.verification.subpackage' is 24, but expected maximum is 1
""")

                assertJaCoCoResult("""Rule violated: lines covered count is 41, but expected maximum is 3
Rule violated: lines covered percentage is 46.5900, but expected minimum is 58.0000
Rule violated: branches covered count for class 'org.jetbrains.kover.test.functional.verification.FullyCovered' is 0, but expected minimum is 1000
Rule violated: instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.FullyCovered' is 0.0000, but expected minimum is 100.0000
Rule violated: branches covered count for class 'org.jetbrains.kover.test.functional.verification.PartiallyCoveredFirst' is 2, but expected minimum is 1000
Rule violated: instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.PartiallyCoveredFirst' is *, but expected minimum is 100.0000
Rule violated: branches covered count for class 'org.jetbrains.kover.test.functional.verification.PartiallyCoveredSecond' is 1, but expected minimum is 1000
Rule violated: instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.PartiallyCoveredSecond' is *, but expected minimum is 100.0000
Rule violated: branches covered count for class 'org.jetbrains.kover.test.functional.verification.Uncovered' is 0, but expected minimum is 1000
Rule violated: branches covered count for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubFullyCovered' is 0, but expected minimum is 1000
Rule violated: instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubFullyCovered' is 0.0000, but expected minimum is 100.0000
Rule violated: branches covered count for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubPartiallyCoveredFirst' is 0, but expected minimum is 1000
Rule violated: instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubPartiallyCoveredFirst' is *, but expected minimum is 100.0000
Rule violated: branches covered count for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubPartiallyCoveredSecond' is 1, but expected minimum is 1000
Rule violated: instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubPartiallyCoveredSecond' is *, but expected minimum is 100.0000
Rule violated: branches covered count for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubUncovered' is 0, but expected minimum is 1000
Rule violated: instructions covered percentage for package 'org.jetbrains.kover.test.functional.verification.subpackage' is *, but expected minimum is 100.0000
Rule violated: lines missed count for package 'org.jetbrains.kover.test.functional.verification.subpackage' is 24, but expected maximum is 1
Rule violated: instructions covered percentage for package 'org.jetbrains.kover.test.functional.verification' is *, but expected minimum is 100.0000
Rule violated: lines missed count for package 'org.jetbrains.kover.test.functional.verification' is 23, but expected maximum is 1
""")
            }
        }
    }

    @SlicedGeneratedTest(allLanguages = true, allTools = true)
    fun BuildConfigurator.testRootRules() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                reports {
                    verify {
                        rule("root rule") {
                            bound {
                                minValue.set(99)
                            }
                        }
                    }
                }
            }
        }

        run("koverVerify", errorExpected = true) {
            verification {
                assertKoverResult("Rule 'root rule' violated: lines covered percentage is *, but expected minimum is 99\n")
                assertJaCoCoResult("Rule violated: lines covered percentage is *, but expected minimum is 99.0000\n")
            }
        }
    }

    @SlicedGeneratedTest(allLanguages = true, allTools = true)
    fun BuildConfigurator.testRootRulesOverride() {
        addProjectWithKover {
            sourcesFrom("simple")

            kover {
                reports {
                    verify {
                        rule("root rule") {
                            bound {
                                minValue.set(99)
                            }
                        }

                        total {
                            verify {
                                rule("root rule") {
                                    bound {
                                        minValue.set(10)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        run("koverVerify")
    }

    /**
     * In the common verify config, rules are declared that always lead to an error.
     * Verification of the Android build variant should use these rules and failed.
     */
    @Test
    fun testAndroidInverseOrder() {
        val buildSource = buildFromTemplate("android-common-verify")
        val build = buildSource.generate()
        val buildResult = build.runWithParams(":app:koverVerifyRelease")
        assertFalse(buildResult.isSuccessful)
    }

}
