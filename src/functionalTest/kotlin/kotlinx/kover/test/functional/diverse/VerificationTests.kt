/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.diverse

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.diverse.core.*
import kotlinx.kover.test.functional.diverse.core.AbstractDiverseGradleTest
import kotlin.test.*

internal class VerificationTests : AbstractDiverseGradleTest() {
    @Test
    fun testVerified() {
        val build = diverseBuild(languages = ALL_LANGUAGES, engines = ALL_ENGINES)
        build.addKoverRootProject {
            sourcesFrom("simple")

            kover {
                verify {
                    rule {
                        name = "test rule"
                        bound {
                            minValue = 50
                            maxValue = 60
                        }
                        bound {
                            valueType = VerificationValueType.COVERED_COUNT
                            minValue = 2
                            maxValue = 10
                        }
                    }
                }
            }
        }

        build.prepare().run("koverVerify", "--stacktrace")
    }

    @Test
    fun testVerificationError() {
        val build = diverseBuild(languages = ALL_LANGUAGES, engines = ALL_ENGINES)
        build.addKoverRootProject {
            sourcesFrom("verification")

            kover {
                verify {
                    rule {
                        name = "counts rule"
                        bound {
                            minValue = 58
                            maxValue = 60
                        }
                        bound {
                            valueType = VerificationValueType.COVERED_COUNT
                            minValue = 2
                            maxValue = 3
                        }
                    }
                    rule {
                        name = "fully uncovered instructions by classes"
                        target = VerificationTarget.CLASS
                        bound {
                            counter = CounterType.INSTRUCTION
                            valueType = VerificationValueType.MISSED_PERCENTAGE
                            minValue = 100
                        }
                    }
                    rule {
                        name = "fully covered instructions by packages"
                        target = VerificationTarget.PACKAGE
                        bound {
                            counter = CounterType.INSTRUCTION
                            valueType = VerificationValueType.COVERED_PERCENTAGE
                            minValue = 100
                        }
                    }
                    rule {
                        name = "branches by classes"
                        target = VerificationTarget.CLASS
                        bound {
                            counter = CounterType.BRANCH
                            valueType = VerificationValueType.COVERED_COUNT
                            minValue = 1000
                        }
                    }
                    rule {
                        name = "missed packages"
                        target = VerificationTarget.PACKAGE
                        bound {
                            valueType = VerificationValueType.MISSED_COUNT
                            maxValue = 1
                        }
                    }
                }
            }
        }

        build.prepare().runWithError("koverHtmlReport", "koverVerify") {
            verification {
                assertIntelliJResult("""Rule 'counts rule' violated:
  lines covered percentage is 46.590900, but expected minimum is 58
  lines covered count is 41, but expected maximum is 3
Rule 'fully uncovered instructions by classes' violated:
  instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.FullyCovered' is 0.000000, but expected minimum is 100
  instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.PartiallyCoveredFirst' is 44.642900, but expected minimum is 100
  instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.PartiallyCoveredSecond' is 51.666700, but expected minimum is 100
  instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubFullyCovered' is 0.000000, but expected minimum is 100
  instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubPartiallyCoveredFirst' is 52.631600, but expected minimum is 100
  instructions missed percentage for class 'org.jetbrains.kover.test.functional.verification.subpackage.SubPartiallyCoveredSecond' is 66.216200, but expected minimum is 100
Rule 'fully covered instructions by packages' violated:
  instructions covered percentage for package 'org.jetbrains.kover.test.functional.verification' is 48.275900, but expected minimum is 100
  instructions covered percentage for package 'org.jetbrains.kover.test.functional.verification.subpackage' is 43.085100, but expected minimum is 100
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

                assertJaCoCoResult("""Rule violated for bundle :: lines covered count is 41, but expected maximum is 3
Rule violated for bundle :: lines covered ratio is 0.46, but expected minimum is 0.58
Rule violated for class org.jetbrains.kover.test.functional.verification.FullyCovered: branches covered count is 0, but expected minimum is 1000
Rule violated for class org.jetbrains.kover.test.functional.verification.FullyCovered: instructions missed ratio is 0, but expected minimum is 1
Rule violated for class org.jetbrains.kover.test.functional.verification.PartiallyCoveredFirst: branches covered count is 2, but expected minimum is 1000
Rule violated for class org.jetbrains.kover.test.functional.verification.PartiallyCoveredFirst: instructions missed ratio is 0, but expected minimum is 1
Rule violated for class org.jetbrains.kover.test.functional.verification.PartiallyCoveredSecond: branches covered count is 1, but expected minimum is 1000
Rule violated for class org.jetbrains.kover.test.functional.verification.PartiallyCoveredSecond: instructions missed ratio is 0, but expected minimum is 1
Rule violated for class org.jetbrains.kover.test.functional.verification.Uncovered: branches covered count is 0, but expected minimum is 1000
Rule violated for class org.jetbrains.kover.test.functional.verification.subpackage.SubFullyCovered: branches covered count is 0, but expected minimum is 1000
Rule violated for class org.jetbrains.kover.test.functional.verification.subpackage.SubFullyCovered: instructions missed ratio is 0, but expected minimum is 1
Rule violated for class org.jetbrains.kover.test.functional.verification.subpackage.SubPartiallyCoveredFirst: branches covered count is 0, but expected minimum is 1000
Rule violated for class org.jetbrains.kover.test.functional.verification.subpackage.SubPartiallyCoveredFirst: instructions missed ratio is 0, but expected minimum is 1
Rule violated for class org.jetbrains.kover.test.functional.verification.subpackage.SubPartiallyCoveredSecond: branches covered count is 1, but expected minimum is 1000
Rule violated for class org.jetbrains.kover.test.functional.verification.subpackage.SubPartiallyCoveredSecond: instructions missed ratio is 0, but expected minimum is 1
Rule violated for class org.jetbrains.kover.test.functional.verification.subpackage.SubUncovered: branches covered count is 0, but expected minimum is 1000
Rule violated for package org.jetbrains.kover.test.functional.verification.subpackage: instructions covered ratio is 0, but expected minimum is 1
Rule violated for package org.jetbrains.kover.test.functional.verification.subpackage: lines missed count is 24, but expected maximum is 1
Rule violated for package org.jetbrains.kover.test.functional.verification: instructions covered ratio is 0, but expected minimum is 1
Rule violated for package org.jetbrains.kover.test.functional.verification: lines missed count is 23, but expected maximum is 1""")
            }
        }
    }


}
