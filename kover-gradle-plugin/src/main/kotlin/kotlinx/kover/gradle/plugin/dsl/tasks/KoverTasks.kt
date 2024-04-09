/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.tasks

import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory

/**
 *  Common interface for all Kover report tasks.
 */
interface KoverReport: Task {
    /**
     * The name of the report variant for Kover Gradle task.
     *
     * Empty string for Kover total tasks.
     */
    @get:Internal
    val variantName: String
}

/**
 * Interface for Kover XML report generation tasks.
 */
interface KoverXmlReport: KoverReport

/**
 * Interface for Kover HTML report generation tasks.
 */
interface KoverHtmlReport: KoverReport {
    /**
     * The directory where the HTML report will be saved.
     */
    @get:OutputDirectory
    val reportDir: Provider<Directory>
}

/**
 * Interface for Kover tasks that print coverage to the build log.
 */
interface KoverLogReport: KoverReport

/**
 * Interface for Kover coverage verification tasks.
 */
interface KoverVerifyReport: KoverReport

/**
 * Interface for Kover report generation tasks in IC format.
 */
interface KoverBinaryReport: KoverReport
