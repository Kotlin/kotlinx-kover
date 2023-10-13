/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.SlicedGeneratedTest

internal class ReportsUpToDateTests {

    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testDeleteTest() {
        addProjectWithKover {
            sourcesFrom("simple")
        }

        add("src/test/kotlin/ExtraTestClass.kt") {
            """ 
                package org.jetbrains.serialuser
                
                import org.jetbrains.Unused
                import kotlin.test.Test
                
                class AdditionalTest { 
                    @Test  
                    fun extra() {
                        Unused().functionInUsedClass()
                    }
                }
            """.trimMargin()
        }
        run("koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.Unused").assertCovered()
            }
        }

        // report should be regenerated if test are deleted
        delete("src/test/kotlin/ExtraTestClass.kt")
        run("koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.Unused").assertFullyMissed()
            }
        }
    }

}

