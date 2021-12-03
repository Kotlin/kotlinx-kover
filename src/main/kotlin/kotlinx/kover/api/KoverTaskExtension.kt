/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("RedundantVisibilityModifier")

package kotlinx.kover.api

import org.gradle.api.model.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import java.io.*

/**
 * Extension for Kover plugin that additionally configures test tasks and
 * runs them with coverage agent to generate coverage execution data.
 */
open class KoverTaskExtension(objects: ObjectFactory) {
    /**
     * Specifies whether the plugin is applied to the test task and configures it to collect and generate coverage data.
     */
    @get:Input
    public var isEnabled: Boolean = true

    /**
     * Specifies file path of generated binary file with coverage data.
     */
    @get:OutputFile
    public val binaryReportFile: Property<File> = objects.property(File::class.java)

    /**
     * Specifies file path of generated source map (SMAP) file generated by IntelliJ coverage agent.
     */
    @get:OutputFile
    @get:Optional
    public val smapFile: Property<File> = objects.property(File::class.java)


    /**
     * Specifies class inclusion rules coverage engine.
     *
     * Inclusion rules are represented as set of fully-qualified names of the classes being instrumented
     * that also supports `*` and `?` wildcards.
     */
    @get:Input
    public var includes: List<String> = emptyList()

    /**
     * Specifies class exclusion rules for coverage engine.
     *
     * Inclusion rules are represented as set of fully-qualified names of the classes being instrumented
     * that also supports `*` and `?` wildcards.
     */
    @get:Input
    public var excludes: List<String> = emptyList()
}
