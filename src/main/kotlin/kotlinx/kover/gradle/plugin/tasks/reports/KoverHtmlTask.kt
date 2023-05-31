/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.reports

import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.file.*
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import javax.inject.*

@CacheableTask
internal abstract class KoverHtmlTask @Inject constructor(tool: CoverageTool) : AbstractKoverReportTask(tool) {
    @get:OutputDirectory
    abstract val reportDir: DirectoryProperty

    @get:Input
    abstract val title: Property<String>

    @get:Input
    @get:Optional
    abstract val charset: Property<String>

    @TaskAction
    fun generate() {
        val htmlDir = reportDir.get().asFile
        htmlDir.mkdirs()
        tool.htmlReport(htmlDir, title.get(), charset.orNull, context())
    }

    fun printPath(): Boolean {
        logger.lifecycle("Kover: HTML report for '$projectPath' file://${reportDir.get().asFile.canonicalPath}/index.html")
        return true
    }
}
