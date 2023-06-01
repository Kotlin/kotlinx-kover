/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*

internal class DefaultConfigTests {

    @SlicedGeneratedTest(allLanguages = true, allTypes = true)
    fun BuildConfigurator.testImplicitConfigs() {
        addProjectWithKover {
            sourcesFrom("simple")
        }

        run("koverXmlReport", "koverHtmlReport") {
            checkDefaultRawReport()
            checkDefaultReports()
        }
    }
}
