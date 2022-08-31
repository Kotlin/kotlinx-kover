/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.checker

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.framework.common.*
import org.gradle.testkit.runner.*
import org.w3c.dom.*
import java.io.*
import javax.xml.parsers.*
import kotlin.test.*


internal fun File.checkResult(
    result: BuildResult,
    description: String,
    errorExpected: Boolean,
    checker: CheckerContext.() -> Unit
) {
    try {
        createCheckerContext(result).also(checker)
    } catch (e: Exception) {
        if (!errorExpected) throw e
    }

    if (errorExpected) {
        throw AssertionError("Error expected for $description")
    }
}

internal fun File.createCheckerContext(result: BuildResult): CheckerContext {
    return CheckerContextImpl(this, result, ":")
}

internal abstract class CheckerContextWrapper(private val origin: CheckerContext) : CheckerContext {
    override val koverVersion: String?
        get() = origin.koverVersion
    override val engine: CoverageEngineVariant
        get() = origin.engine
    override val language: ScriptLanguage
        get() = origin.language
    override val output: String
        get() = origin.output
    override val buildScript: String
        get() = origin.buildScript
    override val pluginType: KotlinPluginType?
        get() = origin.pluginType
    override val defaultBinaryReport: String
        get() = origin.defaultBinaryReport
    override fun allProjects(checker: CheckerContext.() -> Unit) {
        origin.allProjects(checker)
    }

    override fun subproject(path: String, checker: CheckerContext.() -> Unit) {
        origin.subproject(path, checker)
    }

    override fun output(checker: String.() -> Unit) {
        origin.output(checker)
    }

    override fun file(name: String, checker: File.() -> Unit) {
        origin.file(name, checker)
    }

    override fun xml(filename: String, checker: XmlReportChecker.() -> Unit) {
        origin.xml(filename, checker)
    }

    override fun verification(checker: VerifyReportChecker.() -> Unit) {
        origin.verification(checker)
    }

    override fun outcome(taskName: String, checker: TaskOutcome.() -> Unit) {
        origin.outcome(taskName, checker)
    }

    override fun checkReports(xmlPath: String, htmlPath: String, mustExist: Boolean) {
        origin.checkReports(xmlPath, htmlPath, mustExist)
    }

    override fun checkOutcome(taskName: String, outcome: TaskOutcome) {
        origin.checkOutcome(taskName, outcome)
    }

    override fun checkDefaultReports(mustExist: Boolean) {
        origin.checkDefaultReports(mustExist)
    }

    override fun checkDefaultMergedReports(mustExist: Boolean) {
        origin.checkDefaultMergedReports(mustExist)
    }

    override fun checkDefaultBinaryReport(mustExist: Boolean) {
        origin.checkDefaultBinaryReport(mustExist)
    }

}

private class CheckerContextImpl(
    val rootDir: File,
    val result: BuildResult,
    val path: String
) : CheckerContext {
    val projectDir: File = rootDir.sub(path.removeSuffix(":").replace(':', '/'));
    val buildDir: File = projectDir.sub("build")
    val buildFile: File = projectDir.buildFile()

    override val buildScript: String = buildFile.readText()
    override val language = if (buildFile.name.endsWith(".kts")) ScriptLanguage.KOTLIN else ScriptLanguage.GROOVY
    override val pluginType = buildScript.kotlinPluginType(language)
    override val output: String = result.output
    override val koverVersion: String? = buildScript.koverVersion()
    override val engine: CoverageEngineVariant = buildScript.engine()

    override val defaultBinaryReport: String
        get() {
            val extension = if (engine.vendor == CoverageEngineVendor.JACOCO) "exec" else "ic"
            return binaryReportsDirectory + "/" + defaultTestTask(pluginType!!) + "." + extension
        }


    init {
        checkIntellijErrors()
    }

    override fun subproject(path: String, checker: CheckerContext.() -> Unit) {
        CheckerContextImpl(rootDir, result, path).also(checker)
    }

    override fun allProjects(checker: CheckerContext.() -> Unit) {
        this.also(checker)
        val projects = rootDir.settings().detectSubprojects(path)
        projects.forEach { subproject(it.key, checker) }
    }

    private fun checkIntellijErrors() {
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
        buildDir.sub(name).checker()
    }

    override fun xml(filename: String, checker: XmlReportChecker.() -> Unit) {
        val xmlFile = buildDir.sub(filename)
        if (!xmlFile.exists()) throw IllegalStateException("XML file '$filename' not found")
        XmlReportCheckerImpl(this, xmlFile).checker()
    }

    override fun verification(checker: VerifyReportChecker.() -> Unit) {
        val verificationResultFile = buildDir.sub(verificationErrorFile)
        if (!verificationResultFile.exists()) throw IllegalStateException("Verification result file '$verificationResultFile' not found")
        VerifyReportCheckerImpl(this, verificationResultFile.readText()).checker()
    }

    override fun outcome(taskName: String, checker: TaskOutcome.() -> Unit) {
        val taskPath = (if (path == ":") "" else path) + ":" + taskName
        result.task(taskPath)?.outcome?.checker()
            ?: throw IllegalArgumentException("Task '$taskName' with path '$taskPath' not found in build result")
    }

    override fun checkDefaultBinaryReport(mustExist: Boolean) {
        if (mustExist) {
            file(defaultBinaryReport) {
                assertTrue { exists() }
                assertTrue { length() > 0 }
            }
        } else {
            file(defaultBinaryReport) {
                assertFalse { exists() }
            }
        }
    }

    override fun checkDefaultMergedReports(mustExist: Boolean) {
        checkReports(
            defaultMergedXmlReport(),
            defaultMergedHtmlReport(), mustExist
        )
    }

    override fun checkDefaultReports(mustExist: Boolean) {
        checkReports(
            defaultXmlReport(),
            defaultHtmlReport(), mustExist
        )
    }

    override fun checkOutcome(taskName: String, outcome: TaskOutcome) {
        outcome(taskName) {
            assertEquals(outcome, this)
        }
    }

    override fun checkReports(xmlPath: String, htmlPath: String, mustExist: Boolean) {
        if (mustExist) {
            file(xmlPath) {
                assertTrue("XML file must exist '$xmlPath'") { exists() }
                assertTrue { length() > 0 }
            }
            file(htmlPath) {
                assertTrue { exists() }
                assertTrue { isDirectory }
            }
        } else {
            file(xmlPath) {
                assertFalse { exists() }
            }
            file(htmlPath) {
                assertFalse { exists() }
            }
        }
    }
}

private val pluginRegex =
    """id\(?\s*["']org.jetbrains.kotlinx.kover["']\s*\)?\s+version\s+["']([^"^']+)["']""".toRegex()
private val dependencyRegex = """classpath\(?\s*["']org.jetbrains.kotlinx:kover:([^"^']+)["']""".toRegex()
private val intellijEngineRegex = """IntellijEngine\s*\(\s*["']([^"^']+)["']\s*\)""".toRegex()
private val jacocoEngineRegex = """JacocoEngine\s*\(\s*["']([^"^']+)["']\s*\)""".toRegex()

private val includeRegex = """include\s*\(\s*["']([^"^']+)["']\s*\)""".toRegex()

private fun File.buildFile(): File {
    var file = this.sub("build.gradle")
    if (file.exists() && file.isFile) return file

    file = this.sub("build.gradle.kts")
    if (file.exists() && file.isFile) return file

    throw IllegalStateException("Build file not found")
}

private fun File.settings(): String {
    var file = this.sub("build.gradle")
    if (file.exists() && file.isFile) return file.readText()

    file = this.sub("build.gradle.kts")
    if (file.exists() && file.isFile) return file.readText()

    throw Exception("Gradle settings file not found")
}

/**
 * @return map project path -> parent project path
 */
private fun String.detectSubprojects(rootPath: String): Map<String, String> {
    val result: MutableMap<String, String> = mutableMapOf()
    includeRegex.findAll(this).mapNotNull { it.groupValues.getOrNull(1) }.forEach { path ->
        if (path == ":") return@forEach

        val parent = path.substringBeforeLast(':') + ":"

        if (parent.startsWith(rootPath)) {
            result[path] = parent
        }
    }
    return result
}

private fun String.kotlinPluginType(language: ScriptLanguage): KotlinPluginType? {
    return if (language == ScriptLanguage.KOTLIN) {
        when {
            contains("""kotlin("jvm")""") -> KotlinPluginType.JVM
            contains("""kotlin("multiplatform")""") -> KotlinPluginType.MULTIPLATFORM
            else -> null
        }
    } else {
        when {
            contains("""org.jetbrains.kotlin.jvm""") -> KotlinPluginType.JVM
            contains("""org.jetbrains.kotlin.multiplatform""") -> KotlinPluginType.MULTIPLATFORM
            else -> null
        }
    }
}

internal fun String.engine(): CoverageEngineVariant {
    when {
        contains("DefaultIntellijEngine") -> return DefaultIntellijEngine
        contains("DefaultJacocoEngine") -> return DefaultJacocoEngine
    }

    val intellijEngineVersion = intellijEngineRegex.findAll(this).singleOrNull()?.groupValues?.getOrNull(1)
    val jacocoEngineVersion = jacocoEngineRegex.findAll(this).singleOrNull()?.groupValues?.getOrNull(1)
    if (intellijEngineVersion != null && jacocoEngineVersion != null) {
        throw Exception("Both coverage engines used in build script")
    }
    if (intellijEngineVersion != null) return IntellijEngine(intellijEngineVersion)
    if (jacocoEngineVersion != null) return JacocoEngine(jacocoEngineVersion)

    return DefaultIntellijEngine
}

// TODO support value if plugin was applied in subproject
internal fun String.koverVersion(): String? {
    val pluginVersion = pluginRegex.findAll(this).singleOrNull()?.groupValues?.getOrNull(1)
    val dependencyVersion = dependencyRegex.findAll(this).singleOrNull()?.groupValues?.getOrNull(1)

    if (pluginVersion != null && dependencyVersion != null) {
        throw Exception("Using the old and new ways of applying plugins")
    }
    return pluginVersion ?: dependencyVersion
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

private class VerifyReportCheckerImpl(val context: CheckerContextImpl, val content: String) : VerifyReportChecker {
    override fun assertIntelliJResult(expected: String) {
        if (context.engine.vendor != CoverageEngineVendor.INTELLIJ) return
        assertEquals(expected, content, "Unexpected verification result for IntelliJ Engine")
    }

    override fun assertJaCoCoResult(expected: String) {
        if (context.engine.vendor != CoverageEngineVendor.JACOCO) return
        assertEquals(expected, content, "Unexpected verification result for JaCoCo Engine")
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
    val context: CheckerContextImpl,
    val symbol: String,
    val type: String,
    val values: CounterValues?
) :
    Counter {
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


