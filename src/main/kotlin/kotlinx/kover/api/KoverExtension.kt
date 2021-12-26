/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.api

import org.gradle.api.model.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*

open class KoverExtension(objects: ObjectFactory) {

    /**
     * Specifies whether instrumentation is disabled for all test tasks of all projects.
     */
    @get:Input
    public var isDisabled: Boolean = false

    /**
     * Specifies the coverage engine to be used to collect execution data.
     */
    @get:Input
    public val coverageEngine: Property<CoverageEngine> = objects.property(CoverageEngine::class.java)

    /**
     * Specifies the version of Intellij-coverage dependency.
     */
    @get:Input
    val intellijEngineVersion: Property<String> = objects.property(String::class.java)

    /**
     * Specifies the version of JaCoCo dependency.
     */
    @get:Input
    val jacocoEngineVersion: Property<String> = objects.property(String::class.java)

    /**
     * Specifies whether the reports will be generated within 'check' task execution.
     */
    @get:Input
    val generateReportOnCheck: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Specifies the projects to be disabled from instrumentation and reportings.
     */
    @get:Input
    var disabledProjects: Set<String> = emptySet()

    /**
     * Specifies whether the classes from 'android' and 'com.android' packages should be included if Android plugin is applied.
     */
    @get:Input
    public var instrumentAndroidPackage: Boolean = false
}

public enum class CoverageEngine {
    INTELLIJ,
    JACOCO
}
