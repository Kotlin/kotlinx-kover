/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.tasks

import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.*
import kotlinx.kover.engines.intellij.*
import kotlinx.kover.engines.jacoco.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.model.*
import org.gradle.api.tasks.*
import org.gradle.process.*
import javax.inject.*

open class KoverMergedVerificationTask : KoverMergedTask() {
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
        rulesInternal += project.objects.newInstance(
            VerificationRuleImpl::class.java,
            rulesInternal.size,
            project.objects
        )
            .also { configureRule.execute(it) }
    }

    @TaskAction
    fun verify() {
        verify(exec, report(), coverageEngine.get(), rulesInternal, classpath.get())
    }

}

open class KoverVerificationTask : KoverProjectTask() {
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
        rulesInternal += project.objects.newInstance(
            VerificationRuleImpl::class.java,
            rulesInternal.size,
            project.objects
        )
            .also { configureRule.execute(it) }
    }

    @TaskAction
    fun verify() {
        verify(exec, report(), coverageEngine.get(), rulesInternal, classpath.get())
    }

}

private fun Task.verify(
    exec: ExecOperations,
    report: Report,
    engine: CoverageEngine,
    rules: List<VerificationRule>,
    classpath: FileCollection
) {
    if (engine == CoverageEngine.INTELLIJ) {
        intellijVerification(exec, report, rules, classpath)
    } else {
        jacocoVerification(report, rules, classpath)
    }
}

private open class VerificationRuleImpl @Inject constructor(override val id: Int, private val objects: ObjectFactory) :
    VerificationRule {
    override var name: String? = null
    override val bounds: MutableList<VerificationBound> = mutableListOf()
    override fun bound(configureBound: Action<VerificationBound>) {
        bounds += objects.newInstance(VerificationBoundImpl::class.java, bounds.size)
            .also { configureBound.execute(it) }
    }
}

private open class VerificationBoundImpl @Inject constructor(override val id: Int) : VerificationBound {
    override var minValue: Int? = null
    override var maxValue: Int? = null
    override var valueType: VerificationValueType = VerificationValueType.COVERED_LINES_PERCENTAGE
}



