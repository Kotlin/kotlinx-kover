package kotlinx.kover.test.functional.core

import kotlinx.kover.api.*
import org.gradle.testkit.runner.*
import org.w3c.dom.*
import java.io.*
import javax.xml.parsers.*


internal class ProjectRunnerImpl(private val projects: Map<ProjectSlice, File>) : ProjectRunner {

    override fun run(vararg args: String, checker: RunResult.() -> Unit): ProjectRunnerImpl {
        val argsList = listOf(*args)

        projects.forEach { (slice, project) -> project.runGradle(argsList, slice, checker) }

        return this
    }

    private fun File.runGradle(args: List<String>, slice: ProjectSlice, checker: RunResult.() -> Unit) {
        val buildResult = GradleRunner.create()
            .withProjectDir(this)
            .withPluginClasspath()
            .addPluginTestRuntimeClasspath()
            .withArguments(args)
            .build()

        try {
            RunResultImpl(buildResult, slice, this).apply(checker)
        } catch (e: Throwable) {
            throw AssertionError("Assertion error occurred in test for project $slice", e)
        }
    }
}

private fun GradleRunner.addPluginTestRuntimeClasspath() = apply {
    val classpathFile = File(System.getProperty("plugin-classpath"))
    if (!classpathFile.exists()) {
        throw IllegalStateException("Could not find classpath resource $classpathFile")
    }

    val pluginClasspath = pluginClasspath + classpathFile.readLines().map { File(it) }
    withPluginClasspath(pluginClasspath)
}


private class RunResultImpl(private val result: BuildResult, private val slice: ProjectSlice, dir: File) : RunResult {
    val buildDir: File = File(dir, "build")

    override val engine: CoverageEngine = slice.engine ?: CoverageEngine.INTELLIJ

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

    override fun outcome(taskPath: String, checker: TaskOutcome.() -> Unit) {
        result.task(taskPath)?.outcome?.checker()
            ?: throw IllegalArgumentException("Task '$taskPath' not found in build result")
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

        var classElement: Element? = null

        reportElement.forEach("package") loop@{
            if (getAttribute("name") == packageName) {
                forEach("class") {
                    if (getAttribute("name") == correctedClassName) {
                        classElement = this
                        return@loop
                    }
                }
            }
        }

        classElement?.forEach("counter") {
            if (getAttribute("type") == type) {
                return Counter(type, this.getAttribute("missed").toInt(), this.getAttribute("covered").toInt())
            }
        }
        return null
    }
}

private inline fun Element.forEach(tag: String, block: Element.() -> Unit) {
    val elements = getElementsByTagName(tag)

    for (i in 0 until elements.length) {
        val element = elements.item(i) as Element
        if (element.parentNode == this) {
            element.block()
        }
    }
}
