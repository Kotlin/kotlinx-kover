package kotlinx.kover.tasks

import kotlinx.kover.engines.commons.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*

@CacheableTask
internal open class KoverXmlTask : KoverReportTask() {
    @get:OutputFile
    internal val reportFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun generate() {
        val projectFiles = files.get()

        EngineManager.report(
            engine.get(),
            this,
            exec,
            projectFiles,
            classFilters.get(),
            reportFile.get().asFile,
            null
        )
    }

}
