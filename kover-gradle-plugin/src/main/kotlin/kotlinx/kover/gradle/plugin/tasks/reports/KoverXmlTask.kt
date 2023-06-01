/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.reports

import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import javax.inject.*

@CacheableTask
internal abstract class KoverXmlTask @Inject constructor(tool: CoverageTool) : AbstractKoverReportTask(tool) {
    @get:OutputFile
    internal abstract val reportFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val xmlFile = reportFile.get().asFile
        xmlFile.parentFile.mkdirs()
        tool.xmlReport(xmlFile, context())
    }
}
