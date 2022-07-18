/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.core

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.cases.utils.*
import org.gradle.testkit.runner.*
import org.w3c.dom.*
import java.io.*
import javax.xml.parsers.*


internal class DiverseGradleRunner(private val projects: Map<ProjectSlice, File>, private val extraArgs: List<String>) :
    GradleRunner {

    override fun run(vararg args: String, checker: RunResult.() -> Unit): DiverseGradleRunner {
        val argsList = listOf(*args) + extraArgs
        projects.forEach { (slice, project) ->
            try {
                val buildResult = project.runGradle(argsList)
                RunResultImpl(buildResult, project).apply { checkIntellijErrors() }.apply(checker)
            } catch (e: UnexpectedBuildFailure) {
                throw AssertionError("Assertion error occurred in test for project $slice", e)
            }
        }
        return this
    }
    override fun runWithError(vararg args: String, errorChecker: RunResult.() -> Unit): DiverseGradleRunner {
        val argsList = listOf(*args) + extraArgs
        projects.forEach { (slice, project) ->
            try {
                project.runGradleWithError(argsList)
                throw AssertionError("Assertion error expected in test for project $slice")
            } catch (e: UnexpectedBuildFailure) {
                RunResultImpl(e.buildResult, project).apply { checkIntellijErrors() }.apply(errorChecker)
            }
        }
        return this
    }
}

internal class SingleGradleRunnerImpl(private val projectDir: File) : GradleRunner {
    override fun run(vararg args: String, checker: RunResult.() -> Unit): SingleGradleRunnerImpl {
        val buildResult = projectDir.runGradle(listOf(*args))
        RunResultImpl(buildResult, projectDir).apply { checkIntellijErrors() }.apply(checker)
        return this
    }

    override fun runWithError(vararg args: String, errorChecker: RunResult.() -> Unit): SingleGradleRunnerImpl {
        try {
            projectDir.runGradleWithError(listOf(*args))
            throw AssertionError("Assertion error expected in test")
        } catch (e: UnexpectedBuildFailure) {
            RunResultImpl(e.buildResult, projectDir).apply { checkIntellijErrors() }.apply(errorChecker)
        }
        return this
    }
}

private fun File.runGradle(args: List<String>): BuildResult {
    return org.gradle.testkit.runner.GradleRunner.create()
        .withProjectDir(this)
        .withPluginClasspath()
        .addPluginTestRuntimeClasspath()
        .withArguments(args)
        .build()
}

private fun File.runGradleWithError(args: List<String>) {
    org.gradle.testkit.runner.GradleRunner.create()
        .withProjectDir(this)
        .withPluginClasspath()
        .addPluginTestRuntimeClasspath()
        .withArguments(args)
        .build()
}

private fun org.gradle.testkit.runner.GradleRunner.addPluginTestRuntimeClasspath() = apply {
    val classpathFile = File(System.getProperty("plugin-classpath"))
    if (!classpathFile.exists()) {
        throw IllegalStateException("Could not find classpath resource $classpathFile")
    }

    val pluginClasspath = pluginClasspath + classpathFile.readLines().map { File(it) }
    withPluginClasspath(pluginClasspath)
}


private class RunResultImpl(
    private val result: BuildResult,
    private val dir: File,
    private val path: String = ":"
) : RunResult {
    val buildDir: File = File(dir, "build")

    private val buildScriptFile: File = buildFile()
    private val buildScript: String by lazy { buildScriptFile.readText() }

    override val engine: CoverageEngineVendor by lazy {
        if (buildScript.contains("set(kotlinx.kover.api.CoverageEngine.JACOCO)")) {
            CoverageEngineVendor.JACOCO
        } else {
            CoverageEngineVendor.INTELLIJ
        }
    }

    override val projectType: ProjectType by lazy {
        if (buildScriptFile.name.substringAfterLast(".") == "kts") {
            if (buildScript.contains("""kotlin("jvm")""")) {
                ProjectType.KOTLIN_JVM
            } else if (buildScript.contains("""kotlin("multiplatform")""")) {
                ProjectType.KOTLIN_MULTIPLATFORM
            } else {
                throw IllegalArgumentException("Impossible to determine the type of project")
            }
        } else {
            if (buildScript.contains("""id "org.jetbrains.kotlin.jvm"""")) {
                ProjectType.KOTLIN_JVM
            } else if (buildScript.contains("""id "org.jetbrains.kotlin.multiplatform"""")) {
                ProjectType.KOTLIN_MULTIPLATFORM
            } else {
                throw IllegalArgumentException("Impossible to determine the type of project")
            }
        }
    }

    override fun subproject(name: String, checker: RunResult.() -> Unit) {
        RunResultImpl(result, File(dir, name), "$path$name:").also(checker)
    }

    override fun output(checker: String.() -> Unit) {
        result.output.checker()
    }

    override fun file(name: String, checker: File.() -> Unit) {
        File(buildDir, name).checker()
    }

    override fun xml(filename: String, checker: XmlReport.() -> Unit) {
        val xmlFile = File(buildDir, filename)
        if (!xmlFile.exists()) throw IllegalStateException("XML file '$filename' not found")
        XmlReportImpl(xmlFile).checker()
    }

    override fun outcome(taskName: String, checker: TaskOutcome.() -> Unit) {
        result.task(path + taskName)?.outcome?.checker()
            ?: throw IllegalArgumentException("Task '$taskName' with path '$path$taskName' not found in build result")
    }

    private fun buildFile(): File {
        val file = File(dir, "build.gradle")
        if (file.exists() && file.isFile) return file

        return File(dir, "build.gradle.kts")
    }
}


private class XmlReportImpl(file: File) : XmlReport {
    private val document = DocumentBuilderFactory.newInstance()
        // This option disables checking the dtd file for JaCoCo XML file
        .also { it.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false) }
        .newDocumentBuilder().parse(file)

    override fun classCounter(className: String, type: String): Counter? {
        val correctedClassName = className.replace('.', '/')
        val packageName = correctedClassName.substringBeforeLast('/')

        val reportElement = ((document.getElementsByTagName("report").item(0)) as Element)

        val counterElement: Element? = reportElement
            .filter("package", "name", packageName)
            ?.filter("class", "name", correctedClassName)
            ?.filter("counter", "type", type)

        return counterElement?.let {
            Counter(
                type,
                it.getAttribute("missed").toInt(),
                it.getAttribute("covered").toInt()
            )
        }
    }

    override fun methodCounter(className: String, methodName: String, type: String): Counter? {
        val correctedClassName = className.replace('.', '/')
        val packageName = correctedClassName.substringBeforeLast('/')

        val reportElement = ((document.getElementsByTagName("report").item(0)) as Element)

        val counterElement: Element? = reportElement
            .filter("package", "name", packageName)
            ?.filter("class", "name", correctedClassName)
            ?.filter("method", "name", methodName)
            ?.filter("counter", "type", type)

        return counterElement?.let {
            Counter(
                type,
                it.getAttribute("missed").toInt(),
                it.getAttribute("covered").toInt()
            )
        }
    }
}

private fun Element.filter(tag: String, attributeName: String, attributeValue: String): Element? {
    val elements = getElementsByTagName(tag)
    for (i in 0 until elements.length) {
        val element = elements.item(i) as Element
        if (element.parentNode == this) {
            if (element.getAttribute(attributeName) == attributeValue) {
                return element
            }
        }
    }
    return null
}

