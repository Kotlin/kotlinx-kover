/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.commons.names

import java.io.File

internal object KoverPaths {
    internal fun binReportPath(taskName: String): String {
        return "${binReportsRootPath()}$separator${binReportName(taskName)}"
    }

    internal fun binReportName(taskName: String) = "$taskName.bin"

    internal fun binReportsRootPath() = "kover${separator}bin-reports"

    private val separator = File.separatorChar
}