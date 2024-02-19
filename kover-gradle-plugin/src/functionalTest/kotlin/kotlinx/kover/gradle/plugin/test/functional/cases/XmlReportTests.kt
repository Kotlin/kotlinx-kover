/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.defaultReportsDir
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*
import kotlin.test.assertContains

internal class XmlReportTests {

    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testDefaultXmlTitle() {
        addProjectWithKover {

        }

        addProjectWithKover(":nested") {
            sourcesFrom("simple")
        }

        run("koverXmlReport") {
            subproject(":nested") {
                file("$defaultReportsDir/report.xml") {
                    assertContains(readText(), "Kover Gradle Plugin XML report for :nested")
                }
            }
        }
    }

    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testCustomXmlTitle() {
        val title = "My Custom XML title"

        addProjectWithKover {

        }

        addProjectWithKover(":nested") {
            sourcesFrom("simple")

            kover {
                reports {
                    total {
                        xml {
                            this.title.set("My Custom XML title")
                        }
                    }
                }
            }
        }

        run("koverXmlReport") {
            subproject(":nested") {
                file("$defaultReportsDir/report.xml") {
                    assertContains(readText(), title)
                }
            }
        }
    }

}
