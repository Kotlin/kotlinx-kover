/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks

import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.file.*
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import javax.inject.*

@CacheableTask
internal open class KoverHtmlTask @Inject constructor(tool: CoverageTool) : AbstractKoverReportTask(tool) {
    @get:OutputDirectory
    val reportDir: DirectoryProperty = project.objects.directoryProperty()

    @get:Input
    val title: Property<String> = project.objects.property()

    @get:Input
    @get:Optional
    val charset: Property<String> = project.objects.property()

    @TaskAction
    fun generate() {
        val htmlDir = reportDir.get().asFile
        htmlDir.mkdirs()
        tool.htmlReport(htmlDir, title.get(), charset.orNull, filters.get(), context())
    }

    fun printPath(): Boolean {
        logger.lifecycle("Kover: HTML report for '$projectPath' file://${reportDir.get().asFile.canonicalPath}/index.html")
        return true
    }
}
