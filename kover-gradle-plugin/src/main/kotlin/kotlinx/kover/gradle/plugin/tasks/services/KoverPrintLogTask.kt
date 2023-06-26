/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.services

import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import javax.inject.*

internal abstract class KoverPrintLogTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val fileWithMessage: RegularFileProperty

    @TaskAction
    fun printToLog() {
        logger.lifecycle(fileWithMessage.asFile.get().readText())
    }
}
