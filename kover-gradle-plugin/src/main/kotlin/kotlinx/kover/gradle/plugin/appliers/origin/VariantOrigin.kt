/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers.origin

import kotlinx.kover.gradle.plugin.commons.AndroidBuildVariant
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.testing.Test
import java.io.File

/**
 * Common interface for all variant origins.
 */
internal interface VariantOrigin {
    val tests: TaskCollection<Test>

    /**
     * The compilation units are associated with their names.
     * E.g. each source set in Kotlin JVM has its own compilation.
     */
    val compilations: Provider<Map<String, CompilationDetails>>
}

/**
 * Represents details about some independent compilation unit.
 */
internal class CompilationDetails(
    /**
     * Directories with all source files.
     *
     * They are not separated by languages because both `.java` and `.kt` files can be located in the same directory.
     */
    val sources: Set<File>,

    /**
     * Compilation details specific to Kotlin code.
     */
    val kotlin: LanguageCompilation,

    /**
     * Compilation details specific to Java code.
     */
    val java: LanguageCompilation?
)

/**
 * Information about compilation unit for specific language (Kotlin or Java)
 */
internal class LanguageCompilation(
    /**
     * Directories with compiled classes, outputs of [compileTasks].
     */
    val outputs: Provider<out FileCollection>,

    /**
     * In case when no one compile tasks will be triggered,
     * output dirs will be empty and reporter can't determine project classes.
     *
     * So compile tasks must be triggered anyway.
     */
    val compileTask: Provider<Task>?
)

internal class JvmVariantOrigin(
    override val tests: TaskCollection<Test>,
    override val compilations: Provider<Map<String, CompilationDetails>>,
    val targetName: String
) : VariantOrigin

internal class AndroidVariantOrigin(
    override val tests: TaskCollection<Test>,
    override val compilations: Provider<Map<String, CompilationDetails>>,
    val buildVariant: AndroidBuildVariant
) : VariantOrigin

internal class AllVariantOrigins(val jvm: List<JvmVariantOrigin>, val android: List<AndroidVariantOrigin>)
