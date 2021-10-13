/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("RedundantVisibilityModifier")

package kotlinx.kover

import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.io.File

/**
 * Extension for Kover plugin that additionally configures test tasks and
 * runs them with coverage agent to generate coverage execution data.
 */
open class KoverTaskExtension(objects: ObjectFactory) {
    /**
     * Specifies whether the plugin is applied to the test task and configures it to collect and generate coverage data.
     */
    public var isEnabled: Boolean = true

    /**
     * Specifies the coverage engine to be used to collect execution data.
     */
    public var coverageEngine: CoverageEngine = CoverageEngine.INTELLIJ

    /**
     * Specifies whether XML report should be generated.
     */
    public var generateXml: Boolean = true

    /**
     * Specifies whether HTML report should be generated.
     */
    public var generateHtml: Boolean = false

    /**
     * Specifies file path of generated XML report file with coverage data.
     */
    public var xmlReportFile: Property<File> = objects.property(File::class.java)

    /**
     * Specifies the directory of generated HTML report.
     */
    public var htmlReportDir: DirectoryProperty = objects.directoryProperty()

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

    val rules: MutableList<VerificationRule> = mutableListOf()

    fun verificationRule(configuration: Action<VerificationRule>) {
        rules += VerificationRule().also { configuration.execute(it) }
    }
}

public enum class CoverageEngine {
    INTELLIJ,
    JACOCO
}

enum class VerificationValueType {
    COVERED_LINES_COUNT, MISSED_LINES_COUNT, COVERED_LINES_PERCENTAGE
}

class VerificationRule internal constructor() {
    var name: String? = null
    var minValue: Int? = null
    var maxValue: Int? = null
    var valueType: VerificationValueType = VerificationValueType.COVERED_LINES_PERCENTAGE
}
