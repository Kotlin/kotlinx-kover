/*
 * Copyright 2017-2026 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.runner.BuildOptions
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.buildFromTemplate
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import org.junit.jupiter.api.Test

internal class AndroidConfigurationCacheTests {

    @Test
    fun testConfigCacheForLatestAgp() {
        val buildSource = buildFromTemplate("android-latest")

        val theBuild = buildSource.generate()
        val buildOptions = BuildOptions(
            gradleVersion = "9.4.1",
//            androidSdkDir = "/path/to/android/sdk",
            )

        val result1 = theBuild.runWithParams(":app:koverXmlReport", "--configuration-cache", options = buildOptions)
        println("FIRST RUN")
        println(result1.output)

        theBuild.runWithParams("clean", "--configuration-cache", options = buildOptions)


        val result2 = theBuild.runWithParams(":app:koverXmlReport", "--configuration-cache", options = buildOptions)
        println("\n\nSECOND RUN")
        println(result2.output)
    }

}