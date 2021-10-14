package kotlinx.kover.engines.intellij

import kotlinx.kover.adapters.*
import kotlinx.kover.api.*
import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.tasks.testing.*
import java.io.*
import java.util.*

internal fun Project.createIntellijConfig(koverExtension: KoverExtension): Configuration {
    val config = project.configurations.create("IntellijKoverConfig")
    config.isVisible = false
    config.isTransitive = true
    config.description = "Kotlin Kover Plugin configuration for IntelliJ agent and reporter"

    config.defaultDependencies { dependencies ->
        val usedIntellijAgent = tasks.withType(Test::class.java)
            .any { (it.extensions.findByName("kover") as KoverTaskExtension).coverageEngine == CoverageEngine.INTELLIJ }

        if (usedIntellijAgent) {
            val agentVersion = koverExtension.intellijEngineVersion.get()
            dependencies.add(
                this.dependencies.create("org.jetbrains.intellij.deps:intellij-coverage-agent:$agentVersion")
            )

            dependencies.add(
                this.dependencies.create("org.jetbrains.intellij.deps:intellij-coverage-reporter:$agentVersion")
            )
        }
    }
    return config
}

internal fun Task.intellijReport(
    extension: KoverTaskExtension,
    configuration: Configuration
) {
    val binary = extension.binaryReportFile.get()

    val dirs = project.collectDirs()
    val output = dirs.second.joinToString(",") { file -> file.canonicalPath }

    val args = mutableListOf(
        "reports=\"${binary.canonicalPath}\":\"${binary.canonicalPath}.smap\"",
        "output=$output"
    )

    if (extension.generateXml) {
        val xmlFile = extension.xmlReportFile.get()
        xmlFile.parentFile.mkdirs()
        args += "xml=${xmlFile.canonicalPath}"
    }
    if (extension.generateHtml) {
        val htmlDir = extension.htmlReportDir.get().asFile
        htmlDir.mkdirs()
        args += "html=${htmlDir.canonicalPath}"
        args += dirs.first.joinToString(",", "sources=") { file -> file.canonicalPath }
    }

    project.javaexec { e ->
        e.mainClass.set("com.intellij.rt.coverage.report.Main")
        e.classpath = configuration
        e.args = args
    }
}

internal fun Task.intellijVerification(extension: KoverTaskExtension) {
    if (extension.rules.isEmpty()) {
        return
    }

    val counters = readCounterValuesFromXml(extension.xmlReportFile.get())
    val violations = extension.rules.mapNotNull { checkRule(counters, it) }

    if (violations.isNotEmpty()) {
        throw GradleException(violations.joinToString("\n"))
    }
}

private fun readCounterValuesFromXml(file: File): Map<VerificationValueType, Int> {
    val scanner = Scanner(file)
    var lineCounterLine: String? = null

    while (scanner.hasNextLine()) {
        val line = scanner.nextLine()
        if (line.startsWith("<counter type=\"LINE\"")) {
            lineCounterLine = line
        }
    }
    scanner.close()

    lineCounterLine ?: throw GradleException("No LINE counter in XML report")

    val coveredCount = lineCounterLine.substringAfter("covered=\"").substringBefore("\"").toInt()
    val missedCount = lineCounterLine.substringAfter("missed=\"").substringBefore("\"").toInt()
    val percentage = 100 * coveredCount / (coveredCount + missedCount)

    return mapOf(
        VerificationValueType.COVERED_LINES_COUNT to coveredCount,
        VerificationValueType.MISSED_LINES_COUNT to missedCount,
        VerificationValueType.COVERED_LINES_PERCENTAGE to percentage
    )
}


private fun Task.checkRule(counters: Map<VerificationValueType, Int>, rule: VerificationRule): String? {
    val minValue = rule.minValue
    val maxValue = rule.maxValue

    val value = counters[rule.valueType] ?: throw GradleException("Not found value for counter `${rule.valueType}`")

    val ruleName = if (rule.name != null) "`${rule.name}` " else ""
    val valueTypeName = when (rule.valueType) {
        VerificationValueType.COVERED_LINES_COUNT -> "covered lines count"
        VerificationValueType.MISSED_LINES_COUNT -> "missed lines count"
        VerificationValueType.COVERED_LINES_PERCENTAGE -> "covered lines percentage"
    }

    return if (minValue != null && minValue > value) {
        "Rule ${ruleName}violated for `${project.name}`: $valueTypeName is $value, but expected minimum is $minValue"
    } else if (maxValue != null && maxValue < value) {
        "Rule ${ruleName}violated for `${project.name}`: $valueTypeName is $value, but expected maximum is $maxValue"
    } else {
        null
    }
}
