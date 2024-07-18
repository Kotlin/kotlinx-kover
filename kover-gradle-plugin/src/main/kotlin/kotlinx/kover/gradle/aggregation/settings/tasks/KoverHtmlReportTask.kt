/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.tasks

import kotlinx.kover.features.jvm.ClassFilters
import kotlinx.kover.features.jvm.KoverLegacyFeatures
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File
import java.net.URI

@CacheableTask
internal abstract class KoverHtmlReportTask : AbstractKoverReportTask() {
    @get:OutputDirectory
    abstract val htmlDir: DirectoryProperty

    @get:Input
    abstract val title: Property<String>

    @get:Input
    @get:Optional
    abstract val charset: Property<String>

    @TaskAction
    fun generate() {
        KoverLegacyFeatures.generateHtmlReport(
            htmlDir.asFile.get(),
            charset.orNull,
            reports,
            outputs,
            sources,
            title.get(),
            filters.get().toExternalFilters()
        )
    }

    fun printPath() {
        val clickablePath = URI(
            "file",
            "",
            File(htmlDir.get().asFile.canonicalPath, "index.html").toURI().path,
            null,
            null,
        ).toASCIIString()
        logger.lifecycle("Kover: HTML report for '$projectPath' $clickablePath")
    }

}