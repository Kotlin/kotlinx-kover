package kotlinx.kover.gradle.plugin.tools.kover

import com.intellij.rt.coverage.aggregate.api.AggregatorApi
import com.intellij.rt.coverage.aggregate.api.Request
import kotlinx.kover.gradle.plugin.commons.ReportContext
import org.gradle.api.provider.Property
import java.io.File


internal fun ReportContext.koverBinaryReport(binary: File) {
    submitAction<BinaryReportAction, BinaryReportParameters> {
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

internal abstract class BinaryReportAction : AbstractReportAction<BinaryReportParameters>() {
    override fun generate() {
        val binary = parameters.binaryFile.get()
        val smapFile = parameters.tempDir.file("report.smap").get().asFile

        val files = parameters.files.get()
        val filters = parameters.filters.get()
        val request = Request(filters.toIntellij(), binary, smapFile)

        AggregatorApi.aggregate(listOf(request), files.reports.toList(), files.outputs.toList())
    }
}

