/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.diverse.core.writer

import kotlinx.kover.test.functional.common.GradleScriptLanguage.GROOVY
import kotlinx.kover.test.functional.common.GradleScriptLanguage.KOTLIN
import kotlinx.kover.test.functional.diverse.core.PluginsState
import kotlinx.kover.test.functional.diverse.core.ProjectBuilderState
import kotlinx.kover.test.functional.diverse.core.ProjectSlice
import kotlinx.kover.test.functional.diverse.core.ProjectType
import kotlinx.kover.test.functional.diverse.core.RepositoriesState
import kotlinx.kover.test.functional.diverse.core.TestTaskConfigState
import java.io.*

private const val SAMPLES_PATH = "src/functionalTest/templates"
private const val SAMPLES_SOURCES_PATH = "$SAMPLES_PATH/sources"

internal fun initSlice(
    rootDir: File,
    slice: ProjectSlice,
    projects: Map<String, ProjectBuilderState>,
    localCache: Boolean
): File {
    val sliceDir = File(rootDir, slice.encodedString()).also { it.mkdirs() }
    generateSettingsFile(sliceDir, slice, projects, localCache)
    projects.forEach { (path, state) -> state.generateProject(sliceDir, path, slice) }
    return sliceDir
}

private fun ProjectBuilderState.generateProject(sliceDir: File, path: String, slice: ProjectSlice) {
    val subpath = path.replace(':', '/')
    val projectDir = File(sliceDir, subpath).also { it.mkdirs() }
    copySources(projectDir, slice)
    File(projectDir, "build.${slice.scriptExtension}").printWriter().use {
        it.printPlugins(plugins, slice)
        it.printRepositories(repositories)
        it.printDependencies(subprojects, slice)
        it.printKover(kover, slice, 0)
        it.printKoverMerged(merged, slice, 0)
        it.printTestTasks(testTasks, slice)
    }
}


private fun PrintWriter.printPlugins(plugins: PluginsState, slice: ProjectSlice) {
    if (!plugins.useKotlin && !plugins.useKover) return

    println("plugins {")
    if (plugins.useKotlin) {
        when {
            slice.language == GROOVY && slice.type == ProjectType.KOTLIN_JVM -> print("""    id "org.jetbrains.kotlin.jvm"""")
            slice.language == GROOVY && slice.type == ProjectType.KOTLIN_MULTIPLATFORM -> print("""    id "org.jetbrains.kotlin.multiplatform"""")
            slice.language == KOTLIN && slice.type == ProjectType.KOTLIN_JVM -> print("""    kotlin("jvm")""")
            slice.language == KOTLIN && slice.type == ProjectType.KOTLIN_MULTIPLATFORM -> print("""    kotlin("multiplatform")""")
            else -> throw Exception("Unsupported test combination: language ${slice.language} and project type ${slice.type}")
        }
        plugins.kotlinVersion?.let { print(""" version "$it"""") }
        println()
    }

    if (plugins.useKover) {
        if (slice.language == KOTLIN) {
            print("""    id("org.jetbrains.kotlinx.kover")""")
        } else {
            print("""    id "org.jetbrains.kotlinx.kover"""")
        }
        plugins.koverVersion?.let { print(""" version "$it"""") }
        println()
    }
    println("}")
}

private fun PrintWriter.printRepositories(repositories: RepositoriesState) {
    if (repositories.repositories.isEmpty()) return

    println("repositories {")
    repositories.repositories.forEach {
        print("    ")
        println(it)
    }
    println("}")
}

private fun PrintWriter.printDependencies(subprojects: List<String>, slice: ProjectSlice) {
    val subprojectsPart = subprojects.joinToString(separator = "\n") {
        if (slice.language == KOTLIN) "implementation(project(\"$it\"))" else "implementation project('$it')"
    }

    val template = when {
        slice.language == GROOVY && slice.type == ProjectType.KOTLIN_JVM -> GROOVY_JVM_DEPS
        slice.language == GROOVY && slice.type == ProjectType.KOTLIN_MULTIPLATFORM -> GROOVY_KMP_DEPS
        slice.language == KOTLIN && slice.type == ProjectType.KOTLIN_JVM -> KOTLIN_JVM_DEPS
        slice.language == KOTLIN && slice.type == ProjectType.KOTLIN_MULTIPLATFORM -> KOTLIN_KMP_DEPS
        else -> throw Exception("Unsupported test combination: language ${slice.language} and project type ${slice.type}")
    }
    println(template.replace(DEPS_PLACEHOLDER, subprojectsPart))
}

private fun PrintWriter.printTestTasks(state: TestTaskConfigState, slice: ProjectSlice) {
    if (state.excludes == null && state.includes == null) return

    val testTaskName = when {
        slice.type == ProjectType.KOTLIN_JVM -> "test"
        slice.type == ProjectType.KOTLIN_MULTIPLATFORM && slice.language == KOTLIN -> "named(\"jvmTest\").configure"
        slice.type == ProjectType.KOTLIN_MULTIPLATFORM -> "jvmTest"
        else -> throw Exception("Project with type ${slice.type} and language ${slice.language} not supported for test task configuring")
    }
    val extension =
        if (slice.language == KOTLIN) "extensions.configure(kotlinx.kover.api.KoverTaskExtension::class)" else "kover"

    println("tasks.$testTaskName {")
    println("    $extension {")
    if (state.excludes != null) {
        val excludesString = state.excludes!!.joinToString(separator = ",") { "\"$it\"" }
        println("        excludes.addAll($excludesString)")
    }
    if (state.includes != null) {
        val includesString = state.includes!!.joinToString(separator = ",") { "\"$it\"" }
        println("        includes.addAll($includesString)")
    }
    println("    }")
    println("}")
}

private const val DEPS_PLACEHOLDER = "/*DEPS*/"

private const val KOTLIN_JVM_DEPS = """
dependencies {
    $DEPS_PLACEHOLDER
    testImplementation(kotlin("test"))
}
"""

private const val KOTLIN_KMP_DEPS = """
kotlin {
    jvm() {
        withJava()
    }
    dependencies {
        commonTestImplementation(kotlin("test"))
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                $DEPS_PLACEHOLDER
            }
        }
    }
}
"""

private const val GROOVY_JVM_DEPS = """
dependencies {
    $DEPS_PLACEHOLDER
    testImplementation 'org.jetbrains.kotlin:kotlin-test'
}
"""

private const val GROOVY_KMP_DEPS = """
kotlin {
    jvm() {
        withJava()
    }
    dependencies {
        commonTestImplementation 'org.jetbrains.kotlin:kotlin-test'
    }
    sourceSets {
        jvmMain {
            dependencies {
                $DEPS_PLACEHOLDER
            }
        }
    }
}
"""

private fun ProjectBuilderState.copySources(projectDir: File, slice: ProjectSlice) {
    fun File.copyInto(targetFile: File) {
        listFiles()?.forEach { src ->
            val subTarget = File(targetFile, src.name)
            if (src.isDirectory) {
                subTarget.mkdirs()
                src.copyInto(subTarget)
            } else if (src.exists() && src.length() > 0) {
                src.copyTo(subTarget)
            }
        }
    }

    sourceTemplates.forEach { template ->
        File(SAMPLES_SOURCES_PATH, "$template/main").copyInto(File(projectDir, slice.mainPath))
        File(SAMPLES_SOURCES_PATH, "$template/test").copyInto(File(projectDir, slice.testPath))
    }
}

private val ProjectSlice.mainPath: String
    get() {
        return when (type) {
            ProjectType.KOTLIN_JVM -> "src/main"
            ProjectType.KOTLIN_MULTIPLATFORM -> "src/jvmMain"
            ProjectType.ANDROID -> "src/jvmMain"
        }
    }

private val ProjectSlice.testPath: String
    get() {
        return when (type) {
            ProjectType.KOTLIN_JVM -> "src/test"
            ProjectType.KOTLIN_MULTIPLATFORM -> "src/jvmTest"
            ProjectType.ANDROID -> "src/jvmTest"
        }
    }


//
//private fun CommonBuilderState.buildRootExtension(slice: ProjectSlice): String {
//    if (slice.engine == null && koverConfig.isDefault) {
//        return ""
//    }
//
//    val builder = StringBuilder()
//    builder.appendLine()
//    builder.appendLine("kover {")
//
//    if (koverConfig.disabled != null) {
//        val property = if (slice.language == KOTLIN) "isDisabled" else "disabled"
//        builder.appendLine("$property = ${koverConfig.disabled}")
//    }
//
//    if (slice.engine == CoverageEngineVendor.INTELLIJ) {
//        builder.appendLine("    coverageEngine.set(kotlinx.kover.api.CoverageEngine.INTELLIJ)")
//        if (koverConfig.intellijVersion != null) {
//            builder.appendLine("""    intellijEngineVersion.set("${koverConfig.intellijVersion}")""")
//        }
//    }
//    if (slice.engine == CoverageEngineVendor.JACOCO) {
//        builder.appendLine("    coverageEngine.set(kotlinx.kover.api.CoverageEngine.JACOCO)")
//        if (koverConfig.jacocoVersion != null) {
//            builder.appendLine("""    jacocoEngineVersion.set("${koverConfig.jacocoVersion}")""")
//        }
//    }
//
//    if (koverConfig.disabledProjects.isNotEmpty()) {
//        val prefix = if (slice.language == KOTLIN) "setOf(" else "["
//        val postfix = if (slice.language == KOTLIN) ")" else "]"
//        val value = koverConfig.disabledProjects.joinToString(prefix = prefix, postfix = postfix) { "\"$it\"" }
//        builder.appendLine("    disabledProjects = $value")
//    }
//
//    if (koverConfig.runAllTestsForProjectTask != null) {
//        builder.appendLine("    runAllTestsForProjectTask = ${koverConfig.runAllTestsForProjectTask}")
//    }
//
//    builder.appendLine("}")
//
//    return builder.toString()
//}

//
//@Suppress("UNUSED_PARAMETER")
//private fun ProjectBuilderState.buildVerifications(slice: ProjectSlice): String {
//    if (rules.isEmpty()) {
//        return ""
//    }
//
//    val builder = StringBuilder()
//    builder.appendLine()
//    builder.appendLine("tasks.koverVerify {")
//
//    for (rule in rules) {
//        builder.appendLine("    rule {")
//        rule.name?.also { builder.appendLine("""        name = "$it"""") }
//        for (bound in rule.bounds) {
//            builder.appendLine("        bound {")
//            bound.minValue?.let { builder.appendLine("            minValue = $it") }
//            bound.maxValue?.let { builder.appendLine("            maxValue = $it") }
//            if (bound.valueType != VerificationValueType.COVERED_PERCENTAGE) {
//                builder.appendLine("            valueType = kotlinx.kover.api.VerificationValueType.${bound.valueType}")
//            }
//            builder.appendLine("        }")
//        }
//        builder.appendLine("    }")
//    }
//    builder.appendLine("}")
//
//    return builder.toString()
//}

