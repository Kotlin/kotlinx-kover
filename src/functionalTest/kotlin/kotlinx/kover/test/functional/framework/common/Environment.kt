/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.common

import java.io.*

internal val koverVersion = System.getProperty("koverVersion")
    ?: throw Exception("System property 'koverVersion' not defined for functional tests")

internal val recentKoverVersion = System.getProperty("recentKoverVersion")
    ?: throw Exception("System property 'recentKoverVersion' not defined for functional tests")

internal val kotlinVersion = System.getProperty("kotlinVersion")
    ?: throw Exception("System property 'kotlinVersion' not defined for functional tests")


internal val additionalPluginClasspath = File(System.getProperty("plugin-classpath"))
    .also {
        if (!it.exists()) {
            throw IllegalStateException("Could not find classpath resource $it")
        }
    }
    .readLines()
    .map { File(it) }

internal fun logInfo(message: String) {
    if (infoLogsEnabled) {
        println(message)
    }
}

private val infoLogsEnabled = System.getProperty("infoLogsEnabled") == "true"
