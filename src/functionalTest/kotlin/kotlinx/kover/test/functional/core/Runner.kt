package kotlinx.kover.test.functional.core

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.cases.utils.*
import org.gradle.testkit.runner.*
import org.w3c.dom.*
import java.io.*
import javax.xml.parsers.*


internal class GradleRunnerImpl(private val projects: Map<ProjectSlice, File>) :
    GradleRunner {

    override fun run(vararg args: String, checker: RunResult.() -> Unit): GradleRunnerImpl {
        val argsList = listOf(*args)

        projects.forEach { (slice, project) ->
            try {
                project.runGradle(argsList, checker)
            } catch (e: Throwable) {
                throw AssertionError("Assertion error occurred in test for project $slice", e)
            }
        }

        return this
    }
}

internal class SingleGradleRunnerImpl(private val projectDir: File) : GradleRunner {
    override fun run(vararg args: String, checker: RunResult.() -> Unit): SingleGradleRunnerImpl {
        projectDir.runGradle(listOf(*args), checker)
        return this
    }
}

private fun File.runGradle(args: List<String>, checker: RunResult.() -> Unit) {
    val buildResult = org.gradle.testkit.runner.GradleRunner.create()
        .withProjectDir(this)
        .withPluginClasspath()
        .addPluginTestRuntimeClasspath()
        .withArguments(args)
        .build()

    RunResultImpl(buildResult, this).apply { checkIntellijErrors() }.apply(checker)
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

    override val engine: CoverageEngine by lazy {
        if (buildScript.contains("set(kotlinx.kover.api.CoverageEngine.JACOCO)")) {
            CoverageEngine.JACOCO
        } else {
            CoverageEngine.INTELLIJ
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
            if (buildScript.contains("""id 'org.jetbrains.kotlin.jvm'""")) {
                ProjectType.KOTLIN_JVM
            } else if (buildScript.contains("""id 'org.jetbrains.kotlin.multiplatform'""")) {
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

