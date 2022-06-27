/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.tasks

import kotlinx.kover.engines.commons.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*

// TODO make internal in 0.7 version
@CacheableTask
public open class KoverHtmlTask : KoverReportTask() {
    @get:OutputDirectory
    internal val reportDir: DirectoryProperty = project.objects.directoryProperty()

    @TaskAction
    fun generate() {
        val reportDirFile = reportDir.get().asFile

        val projectFiles = files.get()
        EngineManager.report(
            engine.get(),
            this,
            exec,
            projectFiles,
            classFilter.get(),
            null,
            reportDirFile
        )

        if (projectFiles.keys.size > 1) {
            logger.lifecycle("Kover: HTML merged report for '$projectPath' file://${reportDirFile.canonicalPath}/index.html \n merged projects ${projectFiles.keys}")
        } else {
            logger.lifecycle("Kover: HTML report for '$projectPath' file://${reportDirFile.canonicalPath}/index.html")
        }
    }


    // DEPRECATIONS
    // TODO delete in 0.7 version
    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2. Please read migration to 0.6.0 guide to solve the issue",
        level = DeprecationLevel.ERROR
    )
    val htmlReportDir: DirectoryProperty = project.objects.directoryProperty()

    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2. Please read migration to 0.6.0 guide to solve the issue",
        level = DeprecationLevel.ERROR
    )
    public var includes: List<String> = emptyList()

    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2. Please read migration to 0.6.0 guide to solve the issue",
        level = DeprecationLevel.ERROR
    )
    public var excludes: List<String> = emptyList()
}
