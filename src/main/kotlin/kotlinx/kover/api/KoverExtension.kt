/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.api

import org.gradle.api.model.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*

open class KoverExtension(objects: ObjectFactory) {

    /**
     * Specifies whether the plugin is applied to the test task and configures it to collect and generate coverage data.
     */
    public var isEnabled: Boolean = true
        @Input get

    /**
     * Specifies the coverage engine to be used to collect execution data.
     */
    public val coverageEngine: Property<CoverageEngine> = objects.property(CoverageEngine::class.java)
        @Input get

    /**
     * Specifies the version of Intellij-coverage dependency.
     */
    val intellijEngineVersion: Property<String> = objects.property(String::class.java)
        @Input get

    /**
     * Specifies the version of JaCoCo dependency.
     */
    val jacocoEngineVersion: Property<String> = objects.property(String::class.java)
        @Input get

    /**
     * Specifies whether the reports will be generated within 'check' task execution.
     */
    val generateReportOnCheck: Property<Boolean> = objects.property(Boolean::class.java)
        @Input get
}

public enum class CoverageEngine {
    INTELLIJ,
    JACOCO
}
