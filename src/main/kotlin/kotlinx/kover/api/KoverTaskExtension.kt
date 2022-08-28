/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.api

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import java.io.File

/**
 * Extension for Kover plugin that additionally configures test tasks and
 * runs them with coverage agent to generate coverage execution data.
 */
public interface KoverTaskExtension {
//    (
//    private val objects: ObjectFactory
//) {
    /**
     * Specifies whether instrumentation is disabled for an extended test task.
     */
    @get:Input
//    @get:JvmName("getIsDisabled")
    public val disabled: Property<Boolean>

    /**
     * Specifies file path of generated binary file with coverage data.
     */
//    @get:OutputFile
    public val reportFile: RegularFileProperty

    /**
     * Specifies class instrumentation inclusion rules.
     * Only the specified classes may be instrumented, for the remaining classes there will be zero coverage.
     * Exclusion rules have priority over inclusion ones.
     *
     * Inclusion rules are represented as a set of fully-qualified names of the classes being instrumented.
     * It's possible to use `*` and `?` wildcards.
     */
    @get:Input
    public val includes: ListProperty<String>

    /**
     * Specifies class instrumentation exclusion rules.
     * The specified classes will not be instrumented and there will be zero coverage for them.
     * Exclusion rules have priority over inclusion ones.
     *
     * Exclusion rules are represented as a set of fully-qualified names of the classes being instrumented.
     * It's possible to use `*` and `?` wildcards.
     */
    @get:Input
    public val excludes: ListProperty<String>


    // DEPRECATIONS
    // TODO delete in 0.7 version
    @get:Internal
    @Deprecated(
        message = "Property was renamed in Kover API version 2",
        replaceWith = ReplaceWith("reportFile"),
        level = DeprecationLevel.ERROR
    )
    public val binaryReportFile: Property<File>
}
