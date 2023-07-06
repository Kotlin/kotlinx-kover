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


internal fun ReportContext.koverIcReport(ic: File) {
    val workQueue: WorkQueue = services.workerExecutor.classLoaderIsolation {
        this.classpath.from(this@koverIcReport.classpath)
    }

    workQueue.submit(IcReportAction::class.java) {
        icFile.set(ic)
        filters.convention(this@koverIcReport.filters)

        files.convention(this@koverIcReport.files)
        tempDir.set(this@koverIcReport.tempDir)
        projectPath.convention(this@koverIcReport.projectPath)
    }
}

internal interface IcReportParameters : ReportParameters {
    val icFile: Property<File>
}

internal abstract class IcReportAction : WorkAction<IcReportParameters> {
    override fun execute() {
        val icFile = parameters.icFile.get()
        val smapFile = icFile.parentFile.resolve(icFile.nameWithoutExtension + ".smap")


        val files = parameters.files.get()
        val filtersIntern = parameters.filters.get()
        val filters = Filters(
            filtersIntern.includesClasses.toList().asPatterns(),
            filtersIntern.excludesClasses.toList().asPatterns(),
            filtersIntern.excludesAnnotations.toList().asPatterns()
        )
        val request = Request(filters, icFile, smapFile)

        AggregatorApi.aggregate(listOf(request), files.reports.toList(), files.outputs.toList())
    }
}

