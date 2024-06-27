/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.common.kotlinVersionCurrent
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*
import kotlinx.kover.gradle.plugin.util.SemVer

internal class ConfigurationCacheTests {
    private val subprojectPath = ":common"

    @GeneratedTest
    fun BuildConfigurator.testConfigCache() {
        addProjectWithKover(subprojectPath) {
            sourcesFrom("multiproject-common")
        }

        addProjectWithKover {
            sourcesFrom("multiproject-user")
            dependencyKover(subprojectPath)
        }

        run(
            "build",
            "koverXmlReport",
            "koverHtmlReport",
            "koverVerify",
            "--configuration-cache",
        )

        // test reusing configuration cache
        run(
            "build",
            "koverXmlReport",
            "koverHtmlReport",
            "koverVerify",
            "--configuration-cache",
        ) {
            output.match {
                assertContains("Reusing configuration cache.")
            }
        }
    }

    @GeneratedTest
    fun BuildConfigurator.testProjectIsolation() {
        addProjectWithKover(subprojectPath) {
            sourcesFrom("multiproject-common")
        }

        // Only since 1.9.20 Kotlin is fully compatible with project isolation
        val kotlinVersion = if (SemVer.ofThreePartOrNull(kotlinVersionCurrent)!! < SemVer.ofThreePartOrNull("1.9.20")!!) {
            "1.9.20"
        } else {
            kotlinVersionCurrent
        }
        addProjectWithKover(kotlinVersion = kotlinVersion) {
            sourcesFrom("multiproject-user")
            dependencyKover(subprojectPath)
        }

        run(
            ":koverXmlReport",
            ":koverHtmlReport",
            ":koverVerify",
            "-Dorg.gradle.unsafe.isolated-projects=true"
        )
    }
}
