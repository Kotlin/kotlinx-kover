/*
 * Copyright 2017-2025 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.SlicedGeneratedTest

internal class InlineTests {
    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testFilteringInline() {
        addProjectWithKover {
            sourcesFrom("inlines")
            kover {
                reports.filters.excludes.classes("*.TestingClass")
            }
        }

        run("koverXmlReport") {
            xmlReport {
                methodCounter("org.jetbrains.ClassWithInline", "main", type = "LINE").assertFullyCovered()
                classCounter("org.jetbrains.TestingClass").assertAbsent()
            }
        }
    }
}