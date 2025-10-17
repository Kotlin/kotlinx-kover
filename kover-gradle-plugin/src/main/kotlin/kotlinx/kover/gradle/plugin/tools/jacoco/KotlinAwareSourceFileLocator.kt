/*
 * Copyright 2017-2025 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import org.jacoco.report.ISourceFileLocator
import java.io.File
import java.io.Reader

/**
 * Class to find a corresponding Kotlin source file.
 *
 * The main feature of working with Kotlin files is that the name of the directory where the source file is located may differ from the package name.
 *
 * Therefore, in case of uncertainty, a class with the desired name is taken, in which the string `package packageName` occurs.
 * We suggest that in most cases the package is written without comments between the word `package` and the package name, as well as such a line is not written in the comment.
 *
 * If a suitable file could not be found, the `null` is returned.
 */
internal class KotlinAwareSourceFileLocator(private val sourcesRoots: Collection<File>, private val tabWith: Int) : ISourceFileLocator {
    override fun getSourceFile(packageName: String, fileName: String): Reader? {
        val path = if (packageName.isNotEmpty()) {
            "$packageName/$fileName"
        } else {
            fileName
        }

        val simpleCase = sourcesRoots.asSequence()
            .map { File(it, path) }
            .firstOrNull { it.exists() }
            ?.bufferedReader()

        if (simpleCase != null) return simpleCase

        if (!fileName.endsWith(".kt")) return null

        val correctPackageName = packageName.replace("/", "\\.")
        val packageRegex = Regex(".*package\\s+$correctPackageName(?:\\s|//|/\\*)?")

        return sourcesRoots.asSequence()
            .flatMap { it.walk() }
            .filter { it.isFile && it.name == fileName }
            .firstOrNull { file ->
                file.bufferedReader().use { reader ->
                    reader.lineSequence().any { line ->
                        line.matches(packageRegex)
                    }
                }
            }
            ?.bufferedReader()
    }

    override fun getTabWidth(): Int = tabWith
}