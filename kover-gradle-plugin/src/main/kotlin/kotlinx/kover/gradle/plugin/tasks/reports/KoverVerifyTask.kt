/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.reports

import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.dsl.tasks.KoverVerifyReport
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@CacheableTask
internal abstract class KoverVerifyTask : AbstractKoverReportTask(), KoverVerifyReport {
    @get:Nested
    abstract val rules: ListProperty<VerificationRule>

    @get:OutputFile
    abstract val resultFile: RegularFileProperty

    @TaskAction
    fun verify() {
        val enabledRules = rules.get().filter { it.isEnabled }
        tool.get().verify(enabledRules, resultFile.get().asFile, context())
    }

}
