/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */



package kotlinx.kover.tasks

import kotlinx.kover.api.*
import kotlinx.kover.api.KoverNames.XML_PROJECT_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.XML_REPORT_TASK_NAME
import kotlinx.kover.engines.commons.*
import kotlinx.kover.engines.intellij.*
import kotlinx.kover.engines.jacoco.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.model.*
import org.gradle.api.tasks.*
import java.io.*
import javax.inject.*

open class KoverVerificationTask : KoverAggregateTask() {
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
        verify(report(), coverageEngine.get(), rulesInternal, classpath.get()) {
            val xmlReport =
                this.project.tasks.withType(KoverXmlReportTask::class.java)
                    .findByName(XML_REPORT_TASK_NAME)
                    ?: throw GradleException("Kover: task '$XML_REPORT_TASK_NAME' not exists but it is required for verification")

            var xmlFile = xmlReport.xmlReportFile.get().asFile
            if (!xmlFile.exists()) {
                xmlFile = File(temporaryDir, "counters.xml")
                intellijReport(
                    it,
                    xmlFile,
                    null,
                    xmlReport.classpath.get()
                )
            }
            xmlFile
        }
    }

}

open class KoverProjectVerificationTask : KoverProjectTask() {
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
        verify(report(), coverageEngine.get(), rulesInternal, classpath.get()) {
            val xmlReport =
                this.project.tasks.withType(KoverXmlProjectReportTask::class.java)
                    .findByName(XML_PROJECT_REPORT_TASK_NAME)
                    ?: throw GradleException("Kover: task '$XML_PROJECT_REPORT_TASK_NAME' does not exist but it is required for verification")

            var xmlFile = xmlReport.xmlReportFile.get().asFile
            if (!xmlFile.exists()) {
                xmlFile = File(temporaryDir, "counters.xml")
                intellijReport(
                    it,
                    xmlFile,
                    null,
                    xmlReport.classpath.get()
                )
            }
            xmlFile
        }
    }

}

private fun Task.verify(
    report: Report,
    engine: CoverageEngine,
    rules: List<VerificationRule>,
    classpath: FileCollection,
    // Remove it after verification is implemented in the IntelliJ reporter
    xmlFileProducer: (Report) -> File,
) {
    if (engine == CoverageEngine.INTELLIJ) {
        val xmlFile = xmlFileProducer(report)
        this.intellijVerification(xmlFile, rules)
    } else {
        jacocoVerification(report, rules, classpath)
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



