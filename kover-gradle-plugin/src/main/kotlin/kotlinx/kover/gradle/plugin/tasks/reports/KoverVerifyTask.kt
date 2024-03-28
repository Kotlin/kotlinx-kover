/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.reports

import kotlinx.kover.gradle.plugin.commons.KoverVerificationException
import kotlinx.kover.gradle.plugin.dsl.tasks.KoverVerifyReport
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault
internal abstract class KoverVerifyTask : DefaultTask(), KoverVerifyReport {
    @get:Input
    abstract val warningInsteadOfFailure: Property<Boolean>

    @get:InputFile
    abstract val errorFile: RegularFileProperty

    @TaskAction
    fun verify() {
        val errorMessage = errorFile.get().asFile.readText()
        if (errorMessage.isEmpty()) {
            // no errors
            return
        }

        if (warningInsteadOfFailure.get()) {
            logger.warn("Kover Verification Error\n$errorMessage")
        } else {
            throw KoverVerificationException(errorMessage)
        }
    }

}
