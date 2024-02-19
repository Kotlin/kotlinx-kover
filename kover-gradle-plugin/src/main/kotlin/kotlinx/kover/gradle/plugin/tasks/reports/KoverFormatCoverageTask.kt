/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.reports

import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import kotlinx.kover.gradle.plugin.dsl.MetricType
import kotlinx.kover.gradle.plugin.dsl.tasks.KoverLogReport
import kotlinx.kover.gradle.plugin.tools.CoverageRequest
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import javax.annotation.Nullable

@CacheableTask
internal abstract class KoverFormatCoverageTask : AbstractKoverReportTask(), KoverLogReport {
    @get:Input
    @get:Optional
    @get:Nullable
    abstract val header: Property<String>

    @get:Input
    abstract val lineFormat: Property<String>

    @get:Input
    abstract val groupBy: Property<GroupingEntityType>

    @get:Input
    abstract val coverageUnits: Property<MetricType>

    @get:Input
    abstract val aggregationForGroup: Property<AggregationType>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun formatToFile() {
        val request =
            CoverageRequest(
                groupBy.get(),
                coverageUnits.get(),
                aggregationForGroup.get(),
                header.orNull,
                lineFormat.get()
            )

        tool.get().collectCoverage(request, outputFile.get().asFile, context())
    }
}
