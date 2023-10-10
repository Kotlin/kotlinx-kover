/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import com.intellij.rt.coverage.util.ErrorReporter
import kotlinx.kover.gradle.plugin.commons.ArtifactContent
import kotlinx.kover.gradle.plugin.commons.ReportContext
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkQueue

private const val FREE_MARKER_LOGGER_PROPERTY_NAME = "org.freemarker.loggerLibrary"

internal interface ReportParameters: WorkParameters {
    val filters: Property<ReportFilters>

    val files: Property<ArtifactContent>
    val tempDir: DirectoryProperty
    val projectPath: Property<String>
    val charset: Property<String>
}

internal abstract class AbstractReportAction<T : ReportParameters> : WorkAction<T> {
    protected abstract fun generate()

    final override fun execute() {
        // print to stdout only critical errors
        ErrorReporter.setLogLevel(ErrorReporter.ERROR)

        // disable freemarker logging to stdout for the time of report generation
        val oldFreemarkerLogger = System.setProperty(FREE_MARKER_LOGGER_PROPERTY_NAME, "none")
        try {
            generate()
        } finally {
            if (oldFreemarkerLogger == null) {
                System.clearProperty(FREE_MARKER_LOGGER_PROPERTY_NAME)
            } else {
                System.setProperty(FREE_MARKER_LOGGER_PROPERTY_NAME, oldFreemarkerLogger)
            }
        }
    }
}

internal inline fun <reified A : AbstractReportAction<P>, P : ReportParameters> ReportContext.submitAction(noinline parametersConfig: P.() -> Unit) {
    val workQueue: WorkQueue = services.workerExecutor.classLoaderIsolation {
        classpath.from(this@submitAction.classpath)
    }
    workQueue.submit(A::class.java) { parametersConfig(this) }
}
