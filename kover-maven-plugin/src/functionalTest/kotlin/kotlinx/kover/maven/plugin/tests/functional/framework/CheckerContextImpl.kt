/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.tests.functional.framework

import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.BUILD_DIRECTORY
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

fun createContext(log: String, projectDirectory: File): CheckerContext {
    // logs: goal name -> (module id -> log)
    val goalsLog = mutableMapOf<String, MutableMap<String, String>>()


    var prevMessage = ""
    var content = ""
    var goalName = ""
    var isGoalOutput = false
    var moduleId: String? = null

    fun addLog(log: String) {
        val id = moduleId ?: throw IllegalStateException("Module id missed")
        goalsLog.computeIfAbsent(goalName) { mutableMapOf() }[id] = log
    }

    log.lineSequence().forEach { line ->
        val message = line.removePrefix("[INFO] ").removePrefix("[WARNING] ").removePrefix("[ERROR] ")


        when {
            /*
            ---------------------< org.example:merged-report >----------------------
             */
            message.matches("-*< .+:.+ >-*".toRegex()) -> {
                moduleId = message.substringAfterLast(":").substringBefore(" >")
                if (isGoalOutput) {
                    addLog(content)
                }
                isGoalOutput = false
            }

            message.startsWith("------------------------------------------------------------------------") -> {
                if (isGoalOutput) {
                    addLog(content)
                }
                isGoalOutput = false
            }

            /*

            --- kover:0.8.4-SNAPSHOT:report-xml (kover-xml) @ maven-test ---
             */
            message.matches("-* .+:.+:.+ \\(.*\\) @ .+ -*".toRegex()) && prevMessage.isEmpty() -> {
                if (isGoalOutput) {
                    // new goal name always starts with empty line
                    content.removeSuffix("\n")
                    addLog(content)
                }

                isGoalOutput = true
                goalName = message.substringAfter("- ").substringBeforeLast(" (")
                content = ""
            }

            else -> {
                content += (if (content.isNotEmpty()) "\n" else "") + message
            }
        }

        prevMessage = message
    }

    return CheckerContextImpl(log, goalsLog, projectDirectory)
}

fun parseXmlReport(file: File): XmlReportContent {
    return XmlReportContentImpl(file)
}

private class CheckerContextImpl(
    override val log: String,
    val goals: Map<String, Map<String, String>>,
    val projectDirectory: File
) : CheckerContext {

    override val isSuccessful: Boolean = log.contains("BUILD SUCCESS\n")

    override fun koverGoalLog(goalName: String, moduleId: String?): String {
        val filtered = goals.filterKeys { string -> string.startsWith("kover:") && string.endsWith(":$goalName") }.map { it.value }
        if (filtered.isEmpty()) throw MavenAssertionException("The '$goalName' goal was not found among the completed ones")
        if (filtered.size > 1) throw IllegalStateException("Several goals were found that were completed with the name '$goalName'")

        val logsByModules = filtered.first()

        return if (moduleId == null) {
            if (logsByModules.size == 1) {
                logsByModules.values.first()
            } else {
                throw IllegalStateException("Goal $goalName was executed in several projects: ${logsByModules.keys}")
            }
        } else {
            val logs = logsByModules.filterKeys { id -> moduleId == id }
            if (logs.size != 1) throw IllegalStateException("Goal $goalName was not executed in module $moduleId")
            logs.values.first()
        }

    }

    override fun findFile(relativePath: String, module: String?): File {
        val moduleDir = if (module == null) projectDirectory else projectDirectory.resolve(module)
        return moduleDir.resolve(BUILD_DIRECTORY).resolve(relativePath)
    }
}

private class XmlReportContentImpl(file: File) : XmlReportContent {
    private val document = DocumentBuilderFactory.newInstance()
        // This option disables checking the dtd file for JaCoCo XML file
        .also { it.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false) }
        .newDocumentBuilder().parse(file)

    override fun classCounter(className: String, type: CounterType): Counter {
        val correctedClassName = className.replace('.', '/')
        val packageName = correctedClassName.substringBeforeLast('/')

        val reportElement = ((document.getElementsByTagName("report").item(0)) as Element)

        val values = reportElement
            .filter("package", "name", packageName)
            ?.filter("class", "name", correctedClassName)
            ?.filter("counter", "type", type.name)
            ?.let {
                CounterValues(
                    it.getAttribute("missed").toInt(),
                    it.getAttribute("covered").toInt()
                )
            }

        return Counter(className, type, values)
    }

    override fun methodCounter(className: String, methodName: String, type: CounterType): Counter {
        val correctedClassName = className.replace('.', '/')
        val packageName = correctedClassName.substringBeforeLast('/')

        val reportElement = ((document.getElementsByTagName("report").item(0)) as Element)

        val values = reportElement
            .filter("package", "name", packageName)
            ?.filter("class", "name", correctedClassName)
            ?.filter("method", "name", methodName)
            ?.filter("counter", "type", type.name)
            ?.let {
                CounterValues(
                    it.getAttribute("missed").toInt(),
                    it.getAttribute("covered").toInt()
                )
            }

        return Counter("$className#$methodName", type, values)
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