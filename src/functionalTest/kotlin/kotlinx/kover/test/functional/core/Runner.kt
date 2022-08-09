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
import kotlin.test.*


internal class DiverseGradleRunner(private val projects: Map<ProjectSlice, File>, private val extraArgs: List<String>) :
    GradleRunner {

    override fun run(vararg args: String, checker: RunResult.() -> Unit): DiverseGradleRunner {
        val argsList = listOf(*args) + extraArgs
        projects.forEach { (slice, project) ->
            try {
                val gradleResult = project.runGradle(argsList)
                RunResultImpl(slice, project, gradleResult).apply(checker)
            } catch (e: Throwable) {
                throw AssertionError("Error occurred in test for project $slice", e)
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
                RunResultImpl(slice, project, e.buildResult).apply(errorChecker)
            }
        }
        return this
    }
}

internal class SingleGradleRunnerImpl(private val projectDir: File) : GradleRunner {
    override fun run(vararg args: String, checker: RunResult.() -> Unit): SingleGradleRunnerImpl {
        val buildResult = projectDir.runGradle(listOf(*args))
        RunResultImpl(null, projectDir, buildResult).apply(checker)
        return this
    }

    override fun runWithError(vararg args: String, errorChecker: RunResult.() -> Unit): SingleGradleRunnerImpl {
        try {
            projectDir.runGradleWithError(listOf(*args))
            throw AssertionError("Assertion error expected in test")
        } catch (e: UnexpectedBuildFailure) {
            RunResultImpl(null, projectDir, e.buildResult).apply(errorChecker)
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
    private val slice: ProjectSlice?,
    private val dir: File,
    private val result: BuildResult,
    private val path: String = ":"
) : RunResult {
    val buildDir: File = File(dir, "build")

    override val defaultBinaryReport: String
        get() {
            // IntelliJ is a default Engine
            val extension = if (slice?.engine == CoverageEngineVendor.JACOCO) "exec" else "ic"
            return binaryReportsDirectory() + "/" + defaultTestTask(slice?.type ?: ProjectType.KOTLIN_JVM) + "." + extension
        }

    private val buildScriptFile: File = buildFile()
    private val buildScript: String by lazy { buildScriptFile.readText() }

    init {
        checkIntellijErrors()
    }

    override fun subproject(name: String, checker: RunResult.() -> Unit) {
        RunResultImpl(slice, File(dir, name), result, "$path$name:").also(checker)
    }

    private fun RunResult.checkIntellijErrors() {
        file(errorsDirectory()) {
            if (this.exists()) {
                val errorLogs = this.listFiles()?.map { it.name } ?: return@file
                throw AssertionError("Detected Coverage Agent errors: $errorLogs")
            }
        }
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
        XmlReportImpl(this, xmlFile).checker()
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
private data class CounterValues(val missed: Int, val covered: Int)
private class CounterImpl(val context: RunResultImpl, val symbol: String, val type: String, val values: CounterValues?): Counter {
    override fun assertAbsent() {
        assertNull(values, "Counter '$symbol' with type '$type' isn't absent")
    }

    override fun assertFullyMissed() {
        assertNotNull(values, "Counter '$symbol' with type '$type' isn't fully missed because it absent")
        assertTrue(values.missed > 0, "Counter '$symbol' with type '$type' isn't fully missed")
        assertEquals(0, values.covered, "Counter '$symbol' with type '$type' isn't fully missed")
    }

    override fun assertCovered() {
        assertNotNull(values, "Counter '$symbol' with type '$type' isn't covered because it absent")
        assertTrue(values.covered > 0, "Counter '$symbol' with type '$type' isn't covered")
    }

    override fun assertTotal(expectedTotal: Int) {
        assertNotNull(values, "Counter '$symbol' with type '$type' is absent so total value can't be checked")
        val actual = values.covered + values.missed
        assertEquals(expectedTotal, actual, "Expected total value $expectedTotal but actual $actual for counter '$symbol' with type '$type'")
    }

    override fun assertCovered(covered: Int, missed: Int) {
        assertNotNull(values, "Counter '$symbol' with type '$type' is absent so covered can't be checked")
        assertEquals(covered, values.covered, "Expected covered value $covered but actual ${values.covered} for counter '$symbol' with type '$type'")
        assertEquals(missed, values.missed, "Expected covered value $missed but actual ${values.missed} for counter '$symbol' with type '$type'")
    }

    override fun assertFullyCovered() {
        assertNotNull(values, "Counter '$symbol' with type '$type' is absent so fully covered can't be checked")
        assertTrue(values.covered > 0, "Counter '$symbol' with type '$type' isn't fully covered")
        assertEquals(0, values.missed, "Counter '$symbol' with type '$type' isn't fully covered")
    }
}


private class XmlReportImpl(val context: RunResultImpl, file: File) : XmlReport {
    private val document = DocumentBuilderFactory.newInstance()
        // This option disables checking the dtd file for JaCoCo XML file
        .also { it.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false) }
        .newDocumentBuilder().parse(file)

    override fun classCounter(className: String, type: String): Counter {
        val correctedClassName = className.replace('.', '/')
        val packageName = correctedClassName.substringBeforeLast('/')

        val reportElement = ((document.getElementsByTagName("report").item(0)) as Element)

        val values = reportElement
            .filter("package", "name", packageName)
            ?.filter("class", "name", correctedClassName)
            ?.filter("counter", "type", type)
            ?.let {
                CounterValues(
                    it.getAttribute("missed").toInt(),
                    it.getAttribute("covered").toInt()
                )
            }

        return CounterImpl(context, className, type, values)
    }

    override fun methodCounter(className: String, methodName: String, type: String): Counter {
        val correctedClassName = className.replace('.', '/')
        val packageName = correctedClassName.substringBeforeLast('/')

        val reportElement = ((document.getElementsByTagName("report").item(0)) as Element)

        val values = reportElement
            .filter("package", "name", packageName)
            ?.filter("class", "name", correctedClassName)
            ?.filter("method", "name", methodName)
            ?.filter("counter", "type", type)
            ?.let {
                CounterValues(
                    it.getAttribute("missed").toInt(),
                    it.getAttribute("covered").toInt()
                )
            }

        return CounterImpl(context, "$className#$methodName", type, values)
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

