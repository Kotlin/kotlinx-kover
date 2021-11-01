/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.api

import org.gradle.api.model.*
import org.gradle.api.provider.*

open class KoverExtension(objects: ObjectFactory) {

    /**
     * Specifies whether the plugin is applied to the test task and configures it to collect and generate coverage data.
     */
    public val isEnabled: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Specifies the coverage engine to be used to collect execution data.
     */
    public var coverageEngine: Property<CoverageEngine> = objects.property(CoverageEngine::class.java)

    /**
     * Specifies the version of Intellij-coverage dependency.
     */
    val intellijEngineVersion: Property<String> = objects.property(String::class.java)

    /**
     * Specifies the version of JaCoCo dependency.
     */
    val jacocoEngineVersion: Property<String> = objects.property(String::class.java)

    /**
     * TODO fdsg
     */
    val generateReportOnCheck: Property<Boolean> = objects.property(Boolean::class.java)
}

public enum class CoverageEngine {
    INTELLIJ,
    JACOCO
}
