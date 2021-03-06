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
     * Specifies whether instrumentation is disabled for an extended test task.
     */
    @get:Input
    public var isDisabled: Boolean = false

    /**
     * Specifies file path of generated binary file with coverage data.
     */
    @get:OutputFile
    public val binaryReportFile: Property<File> = objects.property(File::class.java)

    /**
     * Specifies class instrumentation inclusion rules.
     * Only the specified classes may be instrumented, for the remaining classes there will be zero coverage.
     * Exclusion rules have priority over inclusion ones.
     *
     * Inclusion rules are represented as a set of fully-qualified names of the classes being instrumented.
     * It's possible to use `*` and `?` wildcards.
     */
    @get:Input
    public var includes: List<String> = emptyList()

    /**
     * Specifies class instrumentation exclusion rules.
     * The specified classes will not be instrumented and there will be zero coverage for them.
     * Exclusion rules have priority over inclusion ones.
     *
     * Exclusion rules are represented as a set of fully-qualified names of the classes being instrumented.
     * It's possible to use `*` and `?` wildcards.
     */
    @get:Input
    public var excludes: List<String> = emptyList()
}
