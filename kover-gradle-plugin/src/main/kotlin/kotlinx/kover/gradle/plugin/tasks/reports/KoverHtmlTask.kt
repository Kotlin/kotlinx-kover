/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.reports

import kotlinx.kover.gradle.plugin.dsl.tasks.KoverHtmlReport
import org.gradle.api.file.*
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File
import java.net.URI
import javax.inject.Inject

@CacheableTask
internal abstract class KoverHtmlTask @Inject constructor(@get:Internal override val variantName: String) : AbstractKoverReportTask(), KoverHtmlReport {
    @get:OutputDirectory
    abstract override val reportDir: DirectoryProperty

    @get:Input
    abstract val title: Property<String>

    @get:Input
    @get:Optional
    abstract val charset: Property<String>

    @TaskAction
    fun generate() {
        val htmlDir = reportDir.get().asFile
        htmlDir.mkdirs()
        tool.get().htmlReport(htmlDir, title.get(), charset.orNull, context())
    }

    fun printPath() {
        val clickablePath = URI(
            "file",
            "",
            File(reportDir.get().asFile.canonicalPath, "index.html").toURI().path,
            null,
            null,
        ).toASCIIString()
        logger.lifecycle("Kover: HTML report for '$projectPath' $clickablePath")
    }
}
