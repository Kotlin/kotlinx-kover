/*
 * Copyright 2017-2025 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import kotlinx.kover.gradle.plugin.commons.ArtifactContent
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.tools.ExecFileLoader
import org.jacoco.report.IReportVisitor
import java.io.File

/**
 * Load binary reports and source files to [this] visitor.
 */
internal fun IReportVisitor.loadContent(name: String?, content: ArtifactContent, filters: ReportFilters) {
    val loader = ExecFileLoader()
    content.reports.forEach { file ->
        loader.load(file)
    }

    val sessionInfoStore = loader.sessionInfoStore
    val executionDataStore = loader.executionDataStore

    val originalBuilder = CoverageBuilder()
    val analyzer = Analyzer(executionDataStore, originalBuilder)
    val classfiles = collectClassFiles(content.outputs)
    classfiles.forEach { classfile ->
        analyzer.analyzeAll(classfile)
    }
    val filteredBuilder = originalBuilder.filter(filters, classfiles)

    val bundle = filteredBuilder.getBundle(name)

    visitInfo(sessionInfoStore.infos, executionDataStore.contents)

    val sourceLocator = KotlinAwareSourceFileLocator(content.sources, DEFAULT_TAB_WIDTH)
    visitBundle(bundle, sourceLocator)
}

private const val DEFAULT_TAB_WIDTH = 4

/**
 * Collect all class files from the source root.
 * It also eliminates class duplication, leaving only the first available class with the same full name.
 */
private fun collectClassFiles(outputs: Iterable<File>): Collection<File> {
    val filesByClassName = mutableMapOf<String, File>()
    outputs.forEach { output ->
        output.walk().forEach { file ->
            if (file.isFile && file.name.endsWith(CLASS_FILE_EXTENSION)) {
                val relativeName = file.toRelativeString(output)
                filesByClassName[relativeName] = file
            }
        }
    }

    return filesByClassName.values
}


/**
 * Extension of class-files.
 */
internal const val CLASS_FILE_EXTENSION = ".class"
