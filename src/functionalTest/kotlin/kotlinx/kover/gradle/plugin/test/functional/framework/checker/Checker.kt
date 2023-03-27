/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.checker

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.*
import kotlinx.kover.gradle.plugin.tools.*
import kotlinx.kover.gradle.plugin.util.*
import org.opentest4j.*
import org.w3c.dom.*
import java.io.*
import javax.xml.parsers.*
import kotlin.test.*


internal fun File.checkResult(
    result: BuildResult,
    description: String,
    buildErrorExpected: Boolean,
    checker: CheckerContext.() -> Unit
) {
    try {
        val checkerContext = createCheckerContext(result)
        checkerContext.prepare(buildErrorExpected)
        checkerContext.checker()
    } catch (e: TestAbortedException) {
        throw e
    } catch (e: Throwable) {
        throw AssertionError("${e.message}\n For $description\n\n${result.output}", e)
    }
}

internal inline fun CheckerContext.check(
    description: String,
    buildErrorExpected: Boolean = false,
    checker: CheckerContext.() -> Unit
){
    try {
        prepare(buildErrorExpected)
        this.checker()
    } catch (e: TestAbortedException) {
        throw e
    } catch (e: Throwable) {
        throw AssertionError("${e.message}\n For $description\n\n$output", e)
    }
}

internal fun File.createCheckerContext(result: BuildResult): CheckerContext {
    return CheckerContextImpl(this.analyzeProject(), result)
}

internal fun File.analyzeProject(): ProjectAnalyze {
    return ProjectAnalyzeImpl(this,  ":")
}

private class CheckerContextImpl(
    override val project: ProjectAnalyze,
    val result: BuildResult
) : CheckerContext {
    override val output: String = result.output

    override val defaultRawReport: String
        get() {
            return rawReportsDirectory + "/" + defaultTestTaskName(project.kotlinPlugin.type!!) + "." + project.toolVariant.vendor.rawReportExtension
        }

    override fun prepare(buildErrorExpected: Boolean) {
        if (result.isSuccessful) {
            if (buildErrorExpected) {
                throw AssertionError("Build error expected")
            }
        } else {
            if (output.contains("Define a valid SDK location with an ANDROID_HOME environment variable") ||
                output.contains("Android Gradle plugin requires Java 11 to run.")
            ) {
                if (isAndroidTestDisabled) {
                    throw TestAbortedException("Android tests are disabled")
                } else {
                    throw Exception("Android SDK directory not specified, specify environment variable $ANDROID_HOME_ENV or parameter -Pkover.test.android.sdk. To ignore Android tests pass parameter -Pkover.test.android.disable")
                }
            }
            if (!buildErrorExpected) {
                throw AssertionError("Build error")
            }
        }

        checkKoverToolErrors()
    }

    override fun subproject(path: String, checker: CheckerContext.() -> Unit) {
        val newAnalyze = ProjectAnalyzeImpl(project.rootDir, path)
        CheckerContextImpl(newAnalyze, result).also(checker)
    }

    private fun checkKoverToolErrors() {
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
        project.buildDir.resolve(name).checker()
    }

    override fun xml(filename: String, checker: XmlReportChecker.() -> Unit) {
        val xmlFile = project.buildDir.resolve(filename)
        if (!xmlFile.exists()) throw IllegalStateException("XML file '$filename' not found")
        XmlReportCheckerImpl(this, xmlFile).checker()
    }

    override fun verification(checker: VerifyReportChecker.() -> Unit) {
        val verificationResultFile = project.buildDir.resolve(verificationErrorFile)
        if (!verificationResultFile.exists()) throw IllegalStateException("Verification result file '$verificationResultFile' not found")
        VerifyReportCheckerImpl(this, verificationResultFile.readText()).checker()
    }

    override fun checkDefaultRawReport(mustExist: Boolean) {
        if (mustExist) {
            file(defaultRawReport) {
                assertTrue(exists(), "Default raw report is not exists: $defaultRawReport")
                assertTrue(length() > 0, "Default raw report is empty: $defaultRawReport")
            }
        } else {
            file(defaultRawReport) {
                assertFalse(exists(), "Default raw report must not exist: $defaultRawReport" )
            }
        }
    }

    override fun checkDefaultReports(mustExist: Boolean) {
        checkReports(
            defaultXmlReport(),
            defaultHtmlReport(), mustExist
        )
    }

    override fun checkOutcome(taskNameOrPath: String, expectedOutcome: String) {
        val taskPath = taskNameOrPath.asPath()
        val outcome = result.taskOutcome(taskPath) ?: noTaskFound(taskNameOrPath, taskPath)

        assertEquals(expectedOutcome, outcome, "Unexpected outcome for task '$taskPath'")
    }

    override fun taskOutput(taskNameOrPath: String, checker: String.() -> Unit) {
        val taskPath = taskNameOrPath.asPath()
        val taskLog = result.taskLog(taskPath) ?: noTaskFound(taskNameOrPath, taskPath)

        checker(taskLog)
    }

    override fun checkReports(xmlPath: String, htmlPath: String, mustExist: Boolean) {
        if (mustExist) {
            file(xmlPath) {
                assertTrue(exists(), "XML file must exist '$xmlPath'")
                assertTrue(length() > 0, "XML file mustn't be empty '$xmlPath'")
            }
            file(htmlPath) {
                assertTrue(exists(), "HTML report must exists '$htmlPath'")
                assertTrue(isDirectory, "HTML report must be directory '$htmlPath'" )
            }
        } else {
            file(xmlPath) {
                assertFalse(exists(), "XML file mustn't exist '$xmlPath'")
            }
            file(htmlPath) {
                assertFalse(exists(), "HTML report mustn't exist '$htmlPath'")
            }
        }
    }

    private fun String.asPath(): String {
        return if (startsWith(":")) {
            this
        } else {
            if (project.path == ":") ":$this" else "${project.path}:$this"
        }
    }

    private fun noTaskFound(origin: String, path: String): Nothing {
        throw IllegalArgumentException("Task '$origin' with path '$path' not found in build result")
    }
}

private class ProjectAnalyzeImpl(override val rootDir: File, override val path: String): ProjectAnalyze {
    private val projectDir: File = rootDir.resolve(path.removePrefix(":").replace(':', '/'));
    private val buildFile: File = projectDir.buildFile()

    override val buildDir: File = projectDir.resolve("build")
    override val buildScript: String by lazy { buildFile.readText() }
    override val language = if (buildFile.name.endsWith(".kts")) ScriptLanguage.KOTLIN else ScriptLanguage.GROOVY
    override val kotlinPlugin by lazy { buildScript.kotlinPluginType(language) }
    override val definedKoverVersion: String? by lazy { buildScript.definedKoverVersion() }
    override val toolVariant: CoverageToolVariant by lazy { buildScript.definedTool() ?: KoverToolDefaultVariant }

    override fun allProjects(): List<ProjectAnalyze> {
        return mutableListOf(this) + subprojects()
    }

    private fun subprojects(): List<ProjectAnalyze> {
        val projects = rootDir.settings().detectSubprojects(path)
        return projects.map { ProjectAnalyzeImpl(rootDir, it.key) }
    }
}

/**
 * Regex for finding the version of the applied Kover plugin.
 *
 * Examples:
 *   - `id("org.jetbrains.kotlinx.kover") version "x.x.x"`
 *   - `id 'org.jetbrains.kotlinx.kover' version 'x.x.x'`
 */
private val pluginRegex =
    """id\(?\s*["']org.jetbrains.kotlinx.kover["']\s*\)?\s+version\s+["']([^"^']+)["']""".toRegex()

/**
 * Regex for finding the version of the Kover plugin applied in the legacy style.
 *
 * Examples:
 *   - `classpath("org.jetbrains.kotlinx:kover:x.x.x")`
 *   - `classpath 'org.jetbrains.kotlinx:kover:x.x.x'`
 */
private val dependencyRegex = """classpath\(?\s*["']org.jetbrains.kotlinx:kover:([^"^']+)["']""".toRegex()

/**
 * Regex for finding the custom version of the Kover Coverage Tool.
 *
 * Examples:
 *   - `kotlinx.kover.api.KoverTool("x.x.x")`
 *   - `kotlinx.kover.api.KoverTool('x.x.x')`
 */
private val koverToolRegex = """KoverTool\s*\(\s*["']([^"^']+)["']\s*\)""".toRegex()

/**
 * Regex for finding the custom version of the JaCoCo Coverage Tool.
 *
 * Examples:
 *   - `kotlinx.kover.api.JacocoTool("x.x.x")`
 *   - `kotlinx.kover.api.JacocoTool('x.x.x')`
 */
private val jacocoToolRegex = """JacocoTool\s*\(\s*["']([^"^']+)["']\s*\)""".toRegex()

/**
 * Regex for finding paths of the subprojects. *Subprojects with redefined paths are not supported!*
 *
 * Examples:
 *   - `include(":subproject")`
 *   - `include(':subproject')`
 */
//private val includeRegex = """include\s*\(\s*["']([^"']+)["']\s*\)""".toRegex()
private val includeRegex = """\s*include\s*\(.+""".toRegex()

private val pathStringRegex = """["'](:[^"']*)["']""".toRegex()

private fun File.buildFile(): File {
    var file = this.resolve("build.gradle")
    if (file.exists() && file.isFile) return file

    file = this.resolve("build.gradle.kts")
    if (file.exists() && file.isFile) return file

    throw IllegalStateException("Build file not found")
}

private fun File.settings(): String {
    var file = this.resolve("settings.gradle")
    if (file.exists() && file.isFile) return file.readText()

    file = this.resolve("settings.gradle.kts")
    if (file.exists() && file.isFile) return file.readText()

    throw Exception("Gradle settings file not found")
}

/**
 * @return map project path -> parent project path
 */
private fun String.detectSubprojects(rootPath: String): Map<String, String> {
    val result: MutableMap<String, String> = mutableMapOf()

    lines().forEach { line ->
        if (!line.matches(includeRegex)) {
            return@forEach
        }

         pathStringRegex.findAll(this).mapNotNull { it.groupValues.getOrNull(1) }.forEach { path ->
            if (path != ":") {
                val parent = path.substringBeforeLast(':') + ":"

                if (parent.startsWith(rootPath)) {
                    result[path] = parent
                }

            }
        }
    }

    return result
}

private fun String.kotlinPluginType(language: ScriptLanguage): AppliedKotlinPlugin {
    return if (language == ScriptLanguage.KOTLIN) {
        when {
            contains("""kotlin("jvm")""") -> AppliedKotlinPlugin(KotlinPluginType.JVM)
            contains("""kotlin("multiplatform")""") -> AppliedKotlinPlugin(KotlinPluginType.MULTIPLATFORM)
            else -> AppliedKotlinPlugin(null)
        }
    } else {
        when {
            contains("""org.jetbrains.kotlin.jvm""") -> AppliedKotlinPlugin(KotlinPluginType.JVM)
            contains("""org.jetbrains.kotlin.multiplatform""") -> AppliedKotlinPlugin(KotlinPluginType.MULTIPLATFORM)
            else -> AppliedKotlinPlugin(null)
        }
    }
}

internal fun String.definedTool(): CoverageToolVariant? {
    when {
        contains("useKoverTool()") -> return KoverToolDefaultVariant
        contains("useJacocoTool()") -> return JacocoToolDefaultVariant
    }

    val koverToolVersion = koverToolRegex.findAll(this).singleOrNull()?.groupValues?.getOrNull(1)
    val jacocoToolVersion = jacocoToolRegex.findAll(this).singleOrNull()?.groupValues?.getOrNull(1)
    if (koverToolVersion != null && jacocoToolVersion != null) {
        throw Exception("Both coverage tools used in build script")
    }
    if (koverToolVersion != null) return KoverToolVariant(koverToolVersion)
    if (jacocoToolVersion != null) return JacocoToolVariant(jacocoToolVersion)

    return null
}

internal fun String.definedKoverVersion(): String? {
    val pluginVersion = pluginRegex.findAll(this).singleOrNull()?.groupValues?.getOrNull(1)

    pluginVersion?.let { return it }

    return dependencyRegex.findAll(this).singleOrNull()?.groupValues?.getOrNull(1)
}

private class XmlReportCheckerImpl(val context: CheckerContextImpl, file: File) : XmlReportChecker {
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

        return CounterImpl(className, type, values)
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

        return CounterImpl("$className#$methodName", type, values)
    }
}

private class VerifyReportCheckerImpl(val context: CheckerContextImpl, val content: String) : VerifyReportChecker {
    override fun assertKoverResult(expected: String) {
        if (context.project.toolVariant.vendor != CoverageToolVendor.KOVER) return
        val regex = expected.wildcardsToRegex().toRegex()
        if (!content.matches(regex)) {
            throw AssertionError("Unexpected verification result for Kover Tool.\n\tActual\n[\n$content\n]\nExpected regex\n[\n$expected\n]")
        }
    }

    override fun assertJaCoCoResult(expected: String) {
        if (context.project.toolVariant.vendor != CoverageToolVendor.JACOCO) return
        assertEquals(expected, content, "Unexpected verification result for JaCoCo Tool")
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

private data class CounterValues(val missed: Int, val covered: Int)

private class CounterImpl(
    val symbol: String,
    val type: String,
    val values: CounterValues?
) : Counter {
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

    override fun assertCoveredPartially() {
        assertNotNull(values, "Counter '$symbol' with type '$type' isn't covered because it absent")
        assertTrue(values.covered > 0, "Counter '$symbol' with type '$type' isn't covered")
        assertTrue(values.missed > 0, "Counter '$symbol' with type '$type' isn't partially covered, but fully covered")
    }

    override fun assertTotal(expectedTotal: Int) {
        assertNotNull(values, "Counter '$symbol' with type '$type' is absent so total value can't be checked")
        val actual = values.covered + values.missed
        assertEquals(
            expectedTotal,
            actual,
            "Expected total value $expectedTotal but actual $actual for counter '$symbol' with type '$type'"
        )
    }

    override fun assertCovered(covered: Int, missed: Int) {
        assertNotNull(values, "Counter '$symbol' with type '$type' is absent so covered can't be checked")
        assertEquals(
            covered,
            values.covered,
            "Expected covered value $covered but actual ${values.covered} for counter '$symbol' with type '$type'"
        )
        assertEquals(
            missed,
            values.missed,
            "Expected covered value $missed but actual ${values.missed} for counter '$symbol' with type '$type'"
        )
    }

    override fun assertFullyCovered() {
        assertNotNull(values, "Counter '$symbol' with type '$type' is absent so fully covered can't be checked")
        assertTrue(values.covered > 0, "Counter '$symbol' with type '$type' isn't fully covered")
        assertEquals(0, values.missed, "Counter '$symbol' with type '$type' isn't fully covered")
    }
}


