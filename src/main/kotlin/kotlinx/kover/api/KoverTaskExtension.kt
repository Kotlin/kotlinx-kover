/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("RedundantVisibilityModifier")

package kotlinx.kover.api

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.*
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * Extension for Kover plugin that additionally configures test tasks and
 * runs them with coverage agent to generate coverage execution data.
 */
public open class KoverTaskExtension(objects: ObjectFactory) {
    /**
     * Specifies whether instrumentation is disabled for an extended test task.
     */
    @get:Input
    @get:JvmName("getIsDisabled")
    public val isDisabled: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Specifies file path of generated binary file with coverage data.
     */
    @get:OutputFile
    public val reportFile: RegularFileProperty = objects.fileProperty()

    /**
     * Specifies class instrumentation inclusion rules.
     * Only the specified classes may be instrumented, for the remaining classes there will be zero coverage.
     * Exclusion rules have priority over inclusion ones.
     *
     * Inclusion rules are represented as a set of fully-qualified names of the classes being instrumented.
     * It's possible to use `*` and `?` wildcards.
     */
    @get:Input
    public val includes: ListProperty<String> = objects.listProperty(String::class.java)

    /**
     * Specifies class instrumentation exclusion rules.
     * The specified classes will not be instrumented and there will be zero coverage for them.
     * Exclusion rules have priority over inclusion ones.
     *
     * Exclusion rules are represented as a set of fully-qualified names of the classes being instrumented.
     * It's possible to use `*` and `?` wildcards.
     */
    @get:Input
    public val excludes: ListProperty<String> = objects.listProperty(String::class.java)


    // DEPRECATIONS
    // TODO delete in 0.9 version
    @get:Internal
    @Deprecated(
        message = "Property was renamed in Kover API version 2",
        replaceWith = ReplaceWith("reportFile"),
        level = DeprecationLevel.ERROR
    )
    public val binaryReportFile: RegularFileProperty? = null
}
