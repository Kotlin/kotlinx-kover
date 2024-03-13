/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.tasks

import org.gradle.api.Task

/**
 *  Common interface for all Kover report tasks.
 */
interface KoverReport: Task

/**
 * Interface for Kover XML report generation tasks.
 */
interface KoverXmlReport: KoverReport

/**
 * Interface for Kover HTML report generation tasks.
 */
interface KoverHtmlReport: KoverReport

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
