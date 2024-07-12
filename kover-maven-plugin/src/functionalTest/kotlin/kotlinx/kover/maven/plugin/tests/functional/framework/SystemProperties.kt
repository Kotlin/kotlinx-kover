/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.tests.functional.framework

object SystemProperties {
    val repository: String by lazy {
        System.getProperty("snapshotRepository")
    }

    val koverVersion: String by lazy {
        System.getProperty("koverVersion")
    }

    val kotlinVersion: String by lazy {
        System.getProperty("kotlinVersion")
    }
}