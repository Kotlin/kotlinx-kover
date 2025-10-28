/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import kotlinx.kover.gradle.plugin.commons.ArtifactContent
import kotlinx.kover.gradle.plugin.commons.ReportContext
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

internal interface CommonJacocoParameters: WorkParameters {
    val filters: Property<ReportFilters>

    val files: Property<ArtifactContent>
    val tempDir: DirectoryProperty
    val projectPath: Property<String>
}

internal fun <T : CommonJacocoParameters> T.fillCommonParameters(context: ReportContext) {
    filters.convention(context.filters)
    files.convention(context.files)
    tempDir.set(context.tempDir)
    projectPath.convention(context.projectPath)
}

