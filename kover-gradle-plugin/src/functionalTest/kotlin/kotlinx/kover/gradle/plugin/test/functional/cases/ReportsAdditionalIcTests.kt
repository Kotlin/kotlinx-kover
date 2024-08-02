/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.GeneratedTest
import java.io.File

internal class ReportsAdditionalIcTests {

    @GeneratedTest
    fun BuildConfigurator.test() {
        copy(File("src/functionalTest/templates/report.bin"), "additional.ic")
        addProjectWithKover {
            sourcesFrom("simple")
            kover {
                reports {
                    verify {
                        rule {
                            minBound(100)
                        }
                    }

                    total {
                        additionalBinaryReports.add(File("additional.ic"))
                    }
                }
            }
        }
        run("koverBinaryReport", "koverVerify", "koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.ExampleClass").assertFullyCovered()
                classCounter("org.jetbrains.SecondClass").assertFullyCovered()
                classCounter("org.jetbrains.Unused").assertFullyCovered()
            }
        }
    }


}
