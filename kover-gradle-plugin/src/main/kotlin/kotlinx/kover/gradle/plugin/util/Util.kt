/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.util

import java.io.File

internal val ONE_HUNDRED = 100.toBigDecimal()


internal fun File.subdirs(): List<File> {
    return listFiles { it ->
        it.exists() && it.isDirectory
    }?.toList() ?: emptyList()
}

