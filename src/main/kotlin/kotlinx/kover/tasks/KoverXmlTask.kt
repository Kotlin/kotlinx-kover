package kotlinx.kover.tasks

import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*

// TODO make internal in 0.7 version - for now it public to save access to deprecated fields to print deprecation message
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
            getReportFilters(),
            reportFile.get().asFile,
            null
        )
    }


    // DEPRECATIONS
    // TODO delete in 0.7 version
    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
        level = DeprecationLevel.ERROR
    )
    public val xmlReportFile: RegularFileProperty = project.objects.fileProperty()

    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
        level = DeprecationLevel.ERROR
    )
    public var includes: List<String> = emptyList()

    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
        level = DeprecationLevel.ERROR
    )
    public var excludes: List<String> = emptyList()
}
