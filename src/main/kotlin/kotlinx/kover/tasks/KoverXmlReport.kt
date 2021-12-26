/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.tasks

import kotlinx.kover.api.*
import kotlinx.kover.engines.intellij.*
import kotlinx.kover.engines.jacoco.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*

@CacheableTask
open class KoverXmlReportTask : KoverAggregateTask() {

    /**
     * Specifies file path of generated XML report file with coverage data.
     */
    @get:OutputFile
    val xmlReportFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun generate() {
        if (coverageEngine.get() == CoverageEngine.INTELLIJ) {
            intellijReport(
                report(),
                xmlReportFile.get().asFile,
                null,
                classpath.get()
            )
        } else {
            jacocoReport(
                report(),
                xmlReportFile.get().asFile,
                null,
                classpath.get()
            )
        }
    }
}

@CacheableTask
open class KoverXmlProjectReportTask : KoverProjectTask() {

    /**
     * Specifies file path of generated XML report file with coverage data.
     */
    @get:OutputFile
    val xmlReportFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun generate() {
        if (coverageEngine.get() == CoverageEngine.INTELLIJ) {
            intellijReport(
                report(),
                xmlReportFile.get().asFile,
                null,
                classpath.get()
            )
        } else {
            jacocoReport(
                report(),
                xmlReportFile.get().asFile,
                null,
                classpath.get()
            )
        }
    }
}
