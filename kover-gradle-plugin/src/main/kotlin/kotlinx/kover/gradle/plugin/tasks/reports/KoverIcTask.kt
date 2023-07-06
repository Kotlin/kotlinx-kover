/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.reports

import org.gradle.api.file.*
import org.gradle.api.tasks.*

@CacheableTask
internal abstract class KoverIcTask : AbstractKoverReportTask() {
    @get:OutputFile
    internal abstract val icFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val ic = icFile.get().asFile
        ic.parentFile.mkdirs()
        tool.get().icReport(ic, context())
    }
}
