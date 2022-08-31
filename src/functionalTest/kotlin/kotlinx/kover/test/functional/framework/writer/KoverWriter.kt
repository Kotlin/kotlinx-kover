/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.writer

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.configurator.TestVerifyConfig

internal fun ScriptAppender.writeKover(kover: TestKoverConfig?) {
    if (kover == null) {
        if (engineForced != null) {
            block("kover") {
                writeEngine(null)
            }
        }
        return
    }

    block("kover") {
        writeDisabled(kover.isDisabled)
        writeEngine(kover.engine)
        writeInstrumentation(kover.instrumentation)
        writeFilters(kover.filters)
        writeXmlReport(kover.xml)
        writeVerify(kover.verify)
    }
}


private fun ScriptAppender.writeEngine(engineFromConfig: CoverageEngineVariant?) {
    if (engineFromConfig == null && engineForced == null) return

    val value = if (engineForced != null) {
        val clazz =
            if (engineForced == CoverageEngineVendor.INTELLIJ) DefaultIntellijEngine::class else DefaultJacocoEngine::class
        clazz.obj(language)
    } else {
        val clazz =
            if (engineFromConfig!!.vendor == CoverageEngineVendor.INTELLIJ) IntellijEngine::class else JacocoEngine::class
        val new = if (language == ScriptLanguage.KOTLIN) {
            clazz.qualifiedName
        } else {
            clazz.qualifiedName
        }
        "$new(\"${engineFromConfig.version}\")"
    }

    line("engine".setProperty(value, language))
}

private fun ScriptAppender.writeDisabled(isDisabled: Boolean?) {
    if (isDisabled == null) return

    if (language == ScriptLanguage.KOTLIN) {
        line("isDisabled.set($isDisabled)")
    } else {
        line("disabled = $isDisabled")
    }
}

private fun ScriptAppender.writeFilters(state: TestKoverFiltersConfig) {
    val classes = state.classes
    val sourceSets = state.sourceSets

    block("filters", (sourceSets != null || classes != null)) {
        block("classes", classes != null && (classes.excludes.isNotEmpty() || classes.includes.isNotEmpty())) {
            writeClassFilterContent(classes!!)
        }
        block("sourceSets", sourceSets != null) {
            if (sourceSets!!.excludes.isNotEmpty()) {
                line("excludes".addAllList(sourceSets.excludes, language))
            }
            line("excludeTests = " + sourceSets.excludeTests)
        }
    }
}

private fun ScriptAppender.writeXmlReport(state: TestXmlConfig) {
    val overrideFilters = state.overrideFilters

    block("xmlReport", state.onCheck != null || state.reportFile != null || overrideFilters != null) {
        block("overrideFilters", overrideFilters != null) {
            val classFilter = overrideFilters?.classes
            block("classes", classFilter != null) {
                writeClassFilterContent(classFilter!!)
            }
        }
    }
}

private fun ScriptAppender.writeInstrumentation(state: KoverProjectInstrumentation) {
    block("instrumentation", state.excludeTasks.isNotEmpty()) {
        line("excludeTasks".addAllList(state.excludeTasks, language))
    }
}


internal fun ScriptAppender.writeClassFilterContent(classFilter: KoverClassFilter) {
    if (classFilter.excludes.isNotEmpty()) {
        line("excludes".addAllList(classFilter.excludes, language))
    }
    if (classFilter.includes.isNotEmpty()) {
        line("includes".addAllList(classFilter.includes, language))
    }
}

internal fun ScriptAppender.writeVerify(conf: TestVerifyConfig) {
    val onCheck = conf.onCheck
    val rules = conf.rules

    block("verify", onCheck != null || rules.isNotEmpty()) {
        lineIf(onCheck != null, "onCheck".setProperty(onCheck.toString(), language))

        blockForEach(rules, "rule") { rule ->
            lineIf(rule.isEnabled != null, "isEnabled = ${rule.isEnabled}")
            lineIf(rule.name != null, "name = ${rule.name?.asTextLiteral()}")
            lineIf(rule.target != null, "target = ${rule.target?.enum(language)}")
            block("overrideClassFilter {", rule.overrideClassFilter != null) {
                writeClassFilterContent(rule.overrideClassFilter!!)
            }
            blockForEach(rule.bounds, "bound") { bound ->
                lineIf(bound.minValue != null, "minValue = ${bound.minValue}")
                lineIf(bound.maxValue != null, "maxValue = ${bound.maxValue}")
                lineIf(bound.counter != null, "counter = ${bound.counter?.enum(language)}")
                lineIf(bound.valueType != null, "valueType = ${bound.valueType?.enum(language)}")
            }
        }
    }


}


