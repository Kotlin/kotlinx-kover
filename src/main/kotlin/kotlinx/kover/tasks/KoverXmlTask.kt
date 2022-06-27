package kotlinx.kover.tasks

import kotlinx.kover.engines.commons.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*

// TODO make internal in 0.7 version
@CacheableTask
public open class KoverXmlTask : KoverReportTask() {
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
            classFilter.get(),
            reportFile.get().asFile,
            null
        )
    }


    // DEPRECATIONS
    // TODO delete in 0.7 version
    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2. Please read migration to 0.6.0 guide to solve the issue",
        level = DeprecationLevel.ERROR
    )
    public val xmlReportFile: RegularFileProperty = project.objects.fileProperty()

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
