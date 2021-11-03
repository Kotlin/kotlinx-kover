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
open class KoverXmlReportTask : KoverCommonTask() {

    /**
     * Specifies file path of generated XML report file with coverage data.
     */
    val xmlReportFile: RegularFileProperty = project.objects.fileProperty()
        @OutputFile get

    @TaskAction
    fun generate() {
        if (coverageEngine.get() == CoverageEngine.INTELLIJ) {
            intellijReport(
                binaryReportFiles.get(),
                srcDirs.get(),
                outputDirs.get(),
                xmlReportFile.get().asFile,
                null,
                classpath.get()
            )
        } else {
            jacocoReport(
                binaryReportFiles.get(),
                srcDirs.get(),
                outputDirs.get(),
                classpath.get(),
                xmlReportFile.get().asFile,
                null
            )
        }
    }
}
