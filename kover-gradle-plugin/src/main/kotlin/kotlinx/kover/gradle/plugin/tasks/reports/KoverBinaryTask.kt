/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.reports

import org.gradle.api.file.*
import org.gradle.api.tasks.*

@CacheableTask
internal abstract class KoverBinaryTask : AbstractKoverReportTask() {
    @get:OutputFile
    internal abstract val file: RegularFileProperty

    @TaskAction
    fun generate() {
        val binary = file.get().asFile
        binary.parentFile.mkdirs()
        tool.get().binaryReport(binary, context())
    }
}
