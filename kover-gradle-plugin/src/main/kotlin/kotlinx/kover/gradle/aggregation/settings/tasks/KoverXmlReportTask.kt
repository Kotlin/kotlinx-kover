/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.tasks

import kotlinx.kover.features.jvm.ClassFilters
import kotlinx.kover.features.jvm.KoverLegacyFeatures
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@CacheableTask
internal abstract class KoverXmlReportTask : AbstractKoverTask() {
    @get:OutputFile
    internal abstract val reportFile: RegularFileProperty

    @get:Input
    abstract val title: Property<String>

    @TaskAction
    fun generate() {
        val xmlFile = reportFile.get().asFile
        xmlFile.parentFile.mkdirs()

        KoverLegacyFeatures.generateXmlReport(
            xmlFile,
            reports,
            outputs,
            sources,
            title.get(),
            ClassFilters(includedClasses.get(), excludedClasses.get(), emptySet(), emptySet(), emptySet(), emptySet())
        )
    }

}