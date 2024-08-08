/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import kotlinx.kover.features.jvm.KoverFeatures.koverWildcardToRegex
import kotlinx.kover.gradle.plugin.commons.ArtifactContent
import kotlinx.kover.gradle.plugin.commons.ReportContext
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.tools.ExecFileLoader
import org.jacoco.report.DirectorySourceFileLocator
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiSourceFileLocator
import java.io.File

internal interface CommonJacocoParameters: WorkParameters {
    val filters: Property<ReportFilters>

    val files: Property<ArtifactContent>
    val tempDir: DirectoryProperty
    val projectPath: Property<String>
}


internal fun IReportVisitor.loadContent(name: String?, content: ArtifactContent, filters: ReportFilters) {
    val loader = ExecFileLoader()
    content.reports.forEach { file ->
        file.inputStream().use { loader.load(it) }
    }
    val sessionInfoStore = loader.sessionInfoStore
    val executionDataStore = loader.executionDataStore

    val builder = CoverageBuilder()
    val analyzer = Analyzer(executionDataStore, builder)

    val classfiles = collectClassFiles(content.outputs, filters)
    classfiles.forEach { classfile ->
        analyzer.analyzeAll(classfile)
    }
    val bundle = builder.getBundle(name)

    visitInfo(sessionInfoStore.infos, executionDataStore.contents)

    val sourceLocator = MultiSourceFileLocator(DEFAULT_TAB_WIDTH)
    content.sources.forEach { sourceDir ->
        sourceLocator.add(DirectorySourceFileLocator(sourceDir, null, DEFAULT_TAB_WIDTH))
    }
    visitBundle(bundle, sourceLocator)
}

private const val DEFAULT_TAB_WIDTH = 4

private fun collectClassFiles(outputs: Iterable<File>, filters: ReportFilters): Collection<File> {
    val filesByClassName = mutableMapOf<String, File>()
    outputs.forEach { output ->
        output.walk().forEach { file ->
            if (file.isFile && file.name.endsWith(CLASS_FILE_EXTENSION)) {
                val className = file.toRelativeString(output).filenameToClassname()
                filesByClassName[className] = file
            }
        }
    }

    return if (filters.excludesClasses.isNotEmpty() || filters.includesClasses.isNotEmpty()) {
        val excludeRegexes = filters.excludesClasses.map { koverWildcardToRegex(it).toRegex() }
        val includeRegexes = filters.includesClasses.map { koverWildcardToRegex(it).toRegex() }

        filesByClassName.filterKeys { className ->
            ((includeRegexes.isEmpty() || includeRegexes.any { regex -> className.matches(regex) })
                    // if the exclusion rules are declared, then the file should not fit any of them
                    && excludeRegexes.none { regex -> className.matches(regex) })
        }.values
    } else {
        filesByClassName.values
    }
}

internal fun <T : CommonJacocoParameters> T.fillCommonParameters(context: ReportContext) {
    filters.convention(context.filters)
    files.convention(context.files)
    tempDir.set(context.tempDir)
    projectPath.convention(context.projectPath)
}

/**
 * Replaces characters `|` or `\` to `.` and remove postfix `.class`.
 */
internal fun String.filenameToClassname(): String {
    return this.replace(File.separatorChar, '.').removeSuffix(CLASS_FILE_EXTENSION)
}

/**
 * Extension of class-files.
 */
internal const val CLASS_FILE_EXTENSION = ".class"
