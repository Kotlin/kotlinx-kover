/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.tasks

import kotlinx.kover.tools.commons.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*

@CacheableTask
internal open class KoverHtmlTask : KoverReportTask() {
    @get:OutputDirectory
    internal val reportDir: DirectoryProperty = project.objects.directoryProperty()

    @TaskAction
    fun generate() {
        val reportDirFile = reportDir.get().asFile

        val projectFiles = files.get()
        ToolManager.report(
            tool.get(),
            this,
            exec,
            projectFiles,
            getReportFilters(),
            null,
            reportDirFile
        )

        if (projectFiles.keys.size > 1) {
            logger.lifecycle("Kover: HTML merged report for '$projectPath' file://${reportDirFile.canonicalPath}/index.html \n merged projects ${projectFiles.keys}")
        } else {
            logger.lifecycle("Kover: HTML report for '$projectPath' file://${reportDirFile.canonicalPath}/index.html")
        }
    }
}
