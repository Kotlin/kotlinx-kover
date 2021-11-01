/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("RedundantVisibilityModifier")

package kotlinx.kover.api

import org.gradle.api.model.*
import org.gradle.api.provider.*
import java.io.*

/**
 * Extension for Kover plugin that additionally configures test tasks and
 * runs them with coverage agent to generate coverage execution data.
 */
open class KoverTaskExtension(objects: ObjectFactory) {
    /**
     * Specifies whether the plugin is applied to the test task and configures it to collect and generate coverage data.
     */
    public var isEnabled: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Specifies file path of generated binary file with coverage data.
     */
    public var binaryReportFile: Property<File> = objects.property(File::class.java)

    /**
     * Specifies inclusion rules coverage engine.
     *
     * ### Inclusion rules for IntelliJ
     *
     * Inclusion rules are represented as set of regular expressions
     * that are matched against fully-qualified names of the classes being instrumented.
     *
     * ### Inclusion rules for JaCoCo
     *
     * Inclusion rules are represented as set of [JaCoCo-specific](https://www.eclemma.org/jacoco/trunk/doc/report-mojo.html#includes)
     * fully qualified name that also supports `*` and `?`.
     */
    public var includes: List<String> = emptyList()

    /**
     * Specifies exclusion rules for coverage engine.
     *
     * ### Exclusion rules for IntelliJ
     *
     * Exclusion rules are represented as set of regular expressions
     * that are matched against fully-qualified names of the classes being instrumented.
     *
     * ### Inclusion rules for JaCoCo
     *
     * Exclusion rules are represented as set of [JaCoCo-specific](https://www.eclemma.org/jacoco/trunk/doc/report-mojo.html#excludes)
     * fully qualified name that also supports `*` and `?`.
     */
    public var excludes: List<String> = emptyList()
}
