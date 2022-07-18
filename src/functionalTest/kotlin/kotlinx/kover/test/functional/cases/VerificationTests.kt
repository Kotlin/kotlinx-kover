/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.cases

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import kotlin.test.*

internal class VerificationTests : BaseGradleScriptTest() {
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
    fun testNotVerifiedIntelliJ() {
        val build = diverseBuild(languages = ALL_LANGUAGES, engines = listOf(CoverageEngineVendor.INTELLIJ))
        build.addKoverRootProject {
            sourcesFrom("simple")

            kover {
                verify {
                    rule {
                        name = "test rule"
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
                }
            }
        }

        build.prepare().runWithError("koverVerify") {
            output {
                assertTrue {
                    this.contains(
                        "> Rule 'test rule' violated:\n" +
                                "    covered lines percentage is 57.142900, but expected minimum is 58\n" +
                                "    covered lines count is 4, but expected maximum is 3"
                    )
                }
            }
        }
    }

    @Test
    fun testNotVerifiedJaCoCo() {
        val build = diverseBuild(languages = ALL_LANGUAGES, engines = listOf(CoverageEngineVendor.JACOCO))
        build.addKoverRootProject {
            sourcesFrom("simple")

            kover {
                verify {
                    rule {
                        name = "test rule"
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
                }
            }
        }

        build.prepare().runWithError("koverVerify") {
            output {
                assertTrue {
                    this.contains(
                        "[ant:jacocoReport] Rule violated for bundle :: lines covered ratio is 0.50, but expected minimum is 0.58\n" +
                                "[ant:jacocoReport] Rule violated for bundle :: lines covered count is 4, but expected maximum is 3"
                    )
                }
            }
        }
    }


}
