/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.SlicedGeneratedTest

internal class KoverDisableTests {
    @SlicedGeneratedTest(allLanguages = true)
    fun BuildConfigurator.test() {
        addProjectWithKover {
            sourcesFrom("simple")
            kover {
                disable()
            }
        }

        run("build", "koverXmlReport") {
            checkOutcome("koverGenerateArtifactJvm", "SKIPPED")
            checkOutcome("koverGenerateArtifact", "SKIPPED")
            checkOutcome("koverVerify", "SKIPPED")
            checkOutcome("koverXmlReport", "SKIPPED")
            checkDefaultBinReport(false)
            checkDefaultReports(false)
        }
    }

    // even if we explicitly enable instrumentation for variants,
    // it still shouldn't happen if the disable() function is called.
    @SlicedGeneratedTest(allLanguages = true)
    fun BuildConfigurator.testOverrideInstrumentation() {
        addProjectWithKover {
            sourcesFrom("simple")
            kover {
                disable()

                currentProject {
                    instrumentation {
                        // enable instrumentation for current project
                        disabledForAll.set(false)
                    }
                }
            }
        }

        run("build", "koverXmlReport") {
            checkOutcome("koverGenerateArtifactJvm", "SKIPPED")
            checkOutcome("koverGenerateArtifact", "SKIPPED")
            checkOutcome("koverVerify", "SKIPPED")
            checkOutcome("koverXmlReport", "SKIPPED")
            checkDefaultBinReport(false)
            checkDefaultReports(false)
        }
    }
}