/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.common

import org.gradle.testkit.runner.*
import java.io.*


internal fun File.gradleBuild(args: List<String>): BuildResult {
    return GradleRunner.create()
        .withProjectDir(this)
        .withPluginClasspath()
        .addPluginTestRuntimeClasspath()
        .withArguments(args)
        .build()
}

private fun GradleRunner.addPluginTestRuntimeClasspath() = apply {
    val classpathFile = File(System.getProperty("plugin-classpath"))
    if (!classpathFile.exists()) {
        throw IllegalStateException("Could not find classpath resource $classpathFile")
    }

    val pluginClasspath = pluginClasspath + classpathFile.readLines().map { File(it) }
    withPluginClasspath(pluginClasspath)
}
