/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("RedundantVisibilityModifier")

package kotlinx.kover

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.io.File

/**
 * Extension for Kover tasks that run with coverage agent to generate coverage execution data.
 */
open class KoverTaskExtension(objects: ObjectFactory) {
    /**
     * Specifies whether the plugin's tasks are enabled.
     */
    public var isEnabled: Boolean = true

    /**
     * Specifies the coverage engine to be used to collect execution data.
     */
    public var coverageAgent: CoverageAgent = CoverageAgent.INTELLIJ

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
     * Specifies inclusion rules for IntelliJ coverage agent.
     *
     * Inclusion rules are represented as set of regular expressions
     * that are matched against fully-qualified names of the classes being instrumented.
     */
    public var includes: List<String> = emptyList()

    /**
     * Specifies exclusion rules for IntelliJ coverage agent.
     *
     * Exclusion rules are represented as set of regular expressions
     * hat are matched against fully-qualified names of the classes being instrumented.
     */
    public var excludes: List<String> = emptyList()
}

public enum class CoverageAgent {
    INTELLIJ,
    JACOCO
}
