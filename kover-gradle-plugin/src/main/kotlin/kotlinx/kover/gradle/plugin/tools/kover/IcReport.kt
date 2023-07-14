package kotlinx.kover.gradle.plugin.tools.kover

import com.intellij.rt.coverage.aggregate.api.AggregatorApi
import com.intellij.rt.coverage.aggregate.api.Request
import com.intellij.rt.coverage.report.api.Filters
import kotlinx.kover.gradle.plugin.commons.ReportContext
import kotlinx.kover.gradle.plugin.util.asPatterns
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkQueue
import java.io.File


internal fun ReportContext.koverBinaryReport(binary: File) {
    val workQueue: WorkQueue = services.workerExecutor.classLoaderIsolation {
        this.classpath.from(this@koverBinaryReport.classpath)
    }

    workQueue.submit(BinaryReportAction::class.java) {
        binaryFile.set(binary)
        filters.convention(this@koverBinaryReport.filters)

        files.convention(this@koverBinaryReport.files)
        tempDir.set(this@koverBinaryReport.tempDir)
        projectPath.convention(this@koverBinaryReport.projectPath)
    }
}

internal interface BinaryReportParameters : ReportParameters {
    val binaryFile: Property<File>
}

internal abstract class BinaryReportAction : WorkAction<BinaryReportParameters> {
    override fun execute() {
        val binary = parameters.binaryFile.get()
        val smapFile = parameters.tempDir.file("report.smap").get().asFile

        val files = parameters.files.get()
        val filtersIntern = parameters.filters.get()
        val filters = Filters(
            filtersIntern.includesClasses.toList().asPatterns(),
            filtersIntern.excludesClasses.toList().asPatterns(),
            filtersIntern.excludesAnnotations.toList().asPatterns()
        )
        val request = Request(filters, binary, smapFile)

        AggregatorApi.aggregate(listOf(request), files.reports.toList(), files.outputs.toList())
    }
}

