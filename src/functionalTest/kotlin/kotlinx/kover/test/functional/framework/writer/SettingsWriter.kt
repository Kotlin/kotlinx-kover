/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.writer

import kotlinx.kover.test.functional.framework.common.BuildSlice
import kotlinx.kover.test.functional.framework.common.localRepositoryPath
import kotlinx.kover.test.functional.framework.configurator.TestBuildConfig
import java.io.*


internal fun File.writeSettings(build: TestBuildConfig, slice: BuildSlice) {
    writeScript() {
        call("pluginManagement") {
            call("repositories") {
                line("maven { url=${localRepositoryPath.uriForScript(slice.language)} }")
                line("gradlePluginPortal()")
                line("mavenCentral()")
            }
        }
        line("")
        line("""rootProject.name = "kover-functional-test"""")
        build.projects.keys.forEach { path ->
            if (path != ":") {
                line("""include("$path")""")
            }
        }
        if (build.useLocalCache) {
            call("buildCache") {
                call("local") {
                    line("""directory = "${"$"}settingsDir/build-cache"""")
                }
            }
        }
    }
}
