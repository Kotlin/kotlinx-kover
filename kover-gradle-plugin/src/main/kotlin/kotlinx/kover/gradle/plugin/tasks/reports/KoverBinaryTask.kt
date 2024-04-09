/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.reports

import kotlinx.kover.gradle.plugin.dsl.tasks.KoverBinaryReport
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import javax.inject.Inject

@CacheableTask
internal abstract class KoverBinaryTask @Inject constructor(@get:Internal override val variantName: String) : AbstractKoverReportTask(), KoverBinaryReport {
    @get:OutputFile
    internal abstract val file: RegularFileProperty

    @TaskAction
    fun generate() {
        val binary = file.get().asFile
        binary.parentFile.mkdirs()
        tool.get().binaryReport(binary, context())
    }
}
