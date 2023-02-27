/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks

import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import javax.inject.*

@CacheableTask
internal open class KoverXmlTask @Inject constructor(tool: CoverageTool) : AbstractKoverReportTask(tool) {
    @get:OutputFile
    internal val reportFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun generate() {
        val xmlFile = reportFile.get().asFile
        xmlFile.parentFile.mkdirs()
        tool.xmlReport(xmlFile, filters.get(), context())
    }
}
