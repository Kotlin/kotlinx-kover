/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.writer

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.configurator.TestVerifyConfig
import kotlinx.kover.tools.commons.*

internal fun FormattedScriptAppender.writeKover(kover: TestKoverConfig?) {
    if (kover == null) {
        // if where is no such block in test script but test generated for specific overridden tool - add `kover` block to the script file
        block("kover", overriddenTool != null) {
            writeTool(null)
        }
        return
    }

    block("kover") {
        writeDisabled(kover.isDisabled)
        writeTool(kover.tool)
        writeInstrumentation(kover.instrumentation)
        writeFilters(kover.filters)
        writeXmlReport(kover.xml)
        writeVerify(kover.verify)
    }
}


private fun FormattedScriptAppender.writeTool(toolFromConfig: CoverageToolVariant?) {
    if (toolFromConfig == null && overriddenTool == null) return

    val value = if (overriddenTool != null) {
        val clazz =
            if (overriddenTool == CoverageToolVendor.KOVER) KoverToolDefault::class else JacocoToolDefault::class
        clazz.obj(language)
    } else {
        val clazz =
            if (toolFromConfig!!.vendor == CoverageToolVendor.KOVER) KoverTool::class else JacocoTool::class
        val new = if (language == ScriptLanguage.KOTLIN) {
            clazz.qualifiedName
        } else {
            clazz.qualifiedName
        }
        "$new(\"${toolFromConfig.version}\")"
    }

    line("tool".setProperty(value, language))
}

private fun FormattedScriptAppender.writeDisabled(isDisabled: Boolean?) {
    if (isDisabled == null) return

    if (language == ScriptLanguage.KOTLIN) {
        line("isDisabled.set($isDisabled)")
    } else {
        line("disabled = $isDisabled")
    }
}

private fun FormattedScriptAppender.writeFilters(state: TestKoverFiltersConfig) {
    val classes = state.classes
    val annotations = state.annotations
    val sourceSets = state.sourceSets

    block("filters", (sourceSets != null || classes != null || annotations != null)) {
        block("classes", classes != null && (classes.excludes.isNotEmpty() || classes.includes.isNotEmpty())) {
            writeClassFilterContent(classes!!)
        }
        block("annotations", annotations != null && annotations.excludes.isNotEmpty()) {
            line("excludes".addAllList(annotations!!.excludes, language))
        }
        block("sourceSets", sourceSets != null) {
            if (sourceSets!!.excludes.isNotEmpty()) {
                line("excludes".addAllList(sourceSets.excludes, language))
            }
            line("excludeTests = " + sourceSets.excludeTests)
        }
    }
}

private fun FormattedScriptAppender.writeXmlReport(state: TestXmlConfig) {
    val overrideFilters = state.overrideFilters

    block("xmlReport", state.onCheck != null || state.reportFile != null || overrideFilters != null) {
        block("overrideFilters", overrideFilters != null) {
            val classFilter = overrideFilters?.classes
            val annotations = overrideFilters?.annotations
            block("classes", classFilter != null) {
                writeClassFilterContent(classFilter!!)
            }
            block("annotations", annotations != null && annotations.excludes.isNotEmpty()) {
                line("excludes".addAllList(annotations!!.excludes, language))
            }
        }
    }
}

private fun FormattedScriptAppender.writeInstrumentation(state: KoverProjectInstrumentation) {
    block("instrumentation", state.excludeTasks.isNotEmpty()) {
        line("excludeTasks".addAllList(state.excludeTasks, language))
    }
}


internal fun FormattedScriptAppender.writeClassFilterContent(classFilter: KoverClassFilter) {
    if (classFilter.excludes.isNotEmpty()) {
        line("excludes".addAllList(classFilter.excludes, language))
    }
    if (classFilter.includes.isNotEmpty()) {
        line("includes".addAllList(classFilter.includes, language))
    }
}

internal fun FormattedScriptAppender.writeVerify(conf: TestVerifyConfig) {
    val onCheck = conf.onCheck
    val rules = conf.rules

    block("verify", onCheck != null || rules.isNotEmpty()) {
        lineIf(onCheck != null, "onCheck".setProperty(onCheck.toString(), language))

        blockForEach(rules, "rule") { rule ->
            lineIf(rule.isEnabled != null, "isEnabled = ${rule.isEnabled}")
            lineIf(rule.name != null, "name = ${rule.name?.asTextLiteral()}")
            lineIf(rule.target != null, "target = ${rule.target?.enum(language)}")
            block("overrideClassFilter", rule.overrideClassFilter != null) {
                writeClassFilterContent(rule.overrideClassFilter!!)
            }
            block("overrideAnnotationFilter", rule.overrideAnnotationFilter != null) {
                line("excludes".addAllList(rule.overrideAnnotationFilter!!.excludes, language))
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


