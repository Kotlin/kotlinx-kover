/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */



package kotlinx.kover.tasks

import kotlinx.kover.api.*
import kotlinx.kover.api.KoverNames.XML_REPORT_TASK_NAME
import kotlinx.kover.engines.intellij.*
import kotlinx.kover.engines.jacoco.*
import org.gradle.api.*
import org.gradle.api.model.*
import org.gradle.api.tasks.*
import java.io.*
import javax.inject.*

open class KoverVerificationTask : KoverCommonTask() {
    private val rulesInternal: MutableList<VerificationRule> = mutableListOf()

    /**
     * Added verification rules for test task.
     */
    @get:Nested
    public val rules: List<VerificationRule>
        get() = rulesInternal

    /**
     * Add new coverage verification rule to check after test task execution.
     */
    public fun rule(configureRule: Action<VerificationRule>) {
        rulesInternal += project.objects.newInstance(VerificationRuleImpl::class.java, project.objects)
            .also { configureRule.execute(it) }
    }

    @TaskAction
    fun verify() {
        if (coverageEngine.get() == CoverageEngine.INTELLIJ) {
            intellij()
        } else {
            jacoco()
        }
    }

    private fun intellij() {
        val xmlReport =
            this.project.tasks.withType(KoverXmlReportTask::class.java).findByName(XML_REPORT_TASK_NAME)
                ?: throw GradleException("Kover: task '${XML_REPORT_TASK_NAME}' not exists but it is required for verification")

        var xmlFile = xmlReport.xmlReportFile.get().asFile
        if (!xmlFile.exists()) {
            xmlFile = File(temporaryDir, "counters.xml")
            intellijReport(
                xmlReport.binaryReportFiles.get(),
                xmlReport.smapFiles.get(),
                xmlReport.srcDirs.get(),
                xmlReport.outputDirs.get(),
                xmlFile,
                null,
                xmlReport.classpath.get()
            )
        }
        this.intellijVerification(xmlFile, rulesInternal)
    }

    private fun jacoco() {
        this.jacocoVerification(
            binaryReportFiles.get(),
            srcDirs.get(),
            outputDirs.get(),
            classpath.get(),
            rulesInternal
        )
    }

}

private open class VerificationRuleImpl @Inject constructor(private val objects: ObjectFactory) : VerificationRule {
    override var name: String? = null
    override val bounds: MutableList<VerificationBound> = mutableListOf()
    override fun bound(configureBound: Action<VerificationBound>) {
        bounds += objects.newInstance(VerificationBoundImpl::class.java).also { configureBound.execute(it) }
    }
}

private open class VerificationBoundImpl : VerificationBound {
    override var minValue: Int? = null
    override var maxValue: Int? = null
    override var valueType: VerificationValueType = VerificationValueType.COVERED_LINES_PERCENTAGE
}
