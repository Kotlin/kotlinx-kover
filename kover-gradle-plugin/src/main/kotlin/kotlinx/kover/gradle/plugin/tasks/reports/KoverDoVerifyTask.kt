/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.reports

import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.tools.generateErrorMessage
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import javax.inject.Inject

@CacheableTask
internal abstract class KoverDoVerifyTask @Inject constructor(@get:Internal override val variantName: String) : AbstractKoverReportTask() {
    @get:Nested
    abstract val rules: ListProperty<VerificationRule>

    @get:OutputFile
    abstract val resultFile: RegularFileProperty

    @TaskAction
    fun verify() {
        val enabledRules = rules.get().filter { it.isEnabled }
        val violations = tool.get().verify(enabledRules, context())

        val errorMessage = generateErrorMessage(violations)
        resultFile.get().asFile.writeText(errorMessage)
    }

}
