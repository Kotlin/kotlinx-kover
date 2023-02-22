/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*

internal class ConfigurationCacheTests {
    @GeneratedTest
    fun BuildConfigurator.testConfigCache() {
        addProjectWithKover {
            sourcesFrom("simple")
        }

        run(
            "build",
            "koverXmlReport",
            "koverHtmlReport",
            "koverVerify",
            "--configuration-cache"
        )
    }
}
