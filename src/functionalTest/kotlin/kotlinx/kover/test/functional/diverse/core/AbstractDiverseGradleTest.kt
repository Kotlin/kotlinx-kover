/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.diverse.core

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.common.*
import org.junit.*
import org.junit.rules.*


/**
 * Kotlin by default.
 */
internal val DEFAULT_LANGUAGE = listOf(GradleScriptLanguage.KOTLIN)

/**
 * Used engine from configuration if specified. Otherwise, used [DefaultIntellijEngine] engine.
 */
internal val DEFAULT_ENGINE = listOf<CoverageEngineVendor>()

/**
 * Kotlin JVM project by default.
 */
internal val DEFAULT_TYPE = listOf(ProjectType.KOTLIN_JVM)

internal val ALL_LANGUAGES = listOf(GradleScriptLanguage.KOTLIN, GradleScriptLanguage.GROOVY)
internal val ALL_ENGINES = listOf(CoverageEngineVendor.INTELLIJ, CoverageEngineVendor.JACOCO)
internal val ALL_TYPES = listOf(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)

private val kotlinVersion = System.getProperty("kotlinVersion")
    ?: throw Exception("System property 'kotlin-version' not defined for functional tests")

internal abstract class AbstractDiverseGradleTest {
    @Rule
    @JvmField
    internal val rootFolder: TemporaryFolder = TemporaryFolder()


    internal fun diverseBuild(
        languages: List<GradleScriptLanguage> = DEFAULT_LANGUAGE,
        engines: List<CoverageEngineVendor> = DEFAULT_ENGINE,
        types: List<ProjectType> = DEFAULT_TYPE,
        withCache: Boolean = false
    ): DiverseBuild {
        return DiverseBuildState(rootFolder.root, languages, engines, types, withCache)
    }

    internal fun sampleBuild(templateName: String): GradleRunner {
        return createInternalSample(templateName, rootFolder.root)
    }
}


internal fun DiverseBuild.addKoverRootProject(builder: ProjectBuilder.() -> Unit) {
    addProject("root", ":") {
        plugins {
            kotlin(kotlinVersion)
            kover("DEV")
        }

        repositories {
            repository("mavenCentral()")
        }

        builder()
    }

}

internal fun DiverseBuild.addKoverSubproject(name: String, builder: ProjectBuilder.() -> Unit): String {
    addProject(name, ":$name") {
        plugins {
            kotlin()
            kover()
        }

        repositories {
            repository("mavenCentral()")
        }

        builder()
    }

    return ":$name"
}
