package kotlinx.kover.tasks

import kotlinx.kover.tools.commons.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*

@CacheableTask
internal open class KoverXmlTask : KoverReportTask() {
    @get:OutputFile
    internal val reportFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun generate() {
        val projectFiles = files.get()

        ToolManager.report(
            tool.get(),
            this,
            exec,
            projectFiles,
            getReportFilters(),
            reportFile.get().asFile,
            null
        )
    }
}
