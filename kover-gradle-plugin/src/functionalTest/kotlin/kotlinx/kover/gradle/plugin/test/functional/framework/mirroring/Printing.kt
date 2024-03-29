/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.mirroring

import kotlinx.kover.gradle.plugin.test.functional.framework.common.ScriptLanguage
import kotlinx.kover.gradle.plugin.util.SemVer


internal fun printCode(name: String?, language: ScriptLanguage, gradleVersion: String, block: CodeBlock): String {
    val builder = StringBuilder()
    val context = PrintingContext(builder::append, language, SemVer.ofVariableOrNull(gradleVersion)!!)

    if (name != null) {
        context.print(name)
        context.print(" {")
        context.newLine()
        context.stepIn()
    }

    doPrint(context, block)

    if (name != null) {
        context.stepOut()
        context.print("}")
    }

    return builder.toString()
}

private fun doPrint(context: PrintingContext, block: CodeBlock) {
    block.statements.forEach { statement ->
        when (statement) {
            is VariableInitialization -> printVariableInit(context, statement)
            is Expression -> printStatement(context, statement)
        }
    }
}

private fun printVariableInit(context: PrintingContext, init: VariableInitialization) {
    if (context.language == ScriptLanguage.GROOVY) {
        context.print("def ")
    } else {
        context.print("val ")
    }
    context.print(init.name)
    context.print(" = ")

    printExpression(context, init.rightSide)
    context.newLine()
}

private fun printStatement(context: PrintingContext, expression: Expression) {
    printExpression(context, expression)
    context.newLine()
}

private fun printExpression(context: PrintingContext, expression: Expression) {
    if (expression.receiver != null) {
        printExpression(context, expression.receiver)
        context.print(".")
    }

    when (expression) {
        is PropertyGetter -> context.print(expression.name)
        is FunctionCall -> handleFunctionCall(context, expression)
        is LambdaExpression -> handleLambdaExpression(context, expression)
        is EnumEntryLiteral -> handleEnumEntryLiteral(context, expression)
        NullLiteral -> context.print("null")
        is ValueLiteral -> handleValueLiteral(context, expression)
        is PropertySetter -> handlePropertySetter(context, expression)
        is SetOperatorCall -> handleSetOperatorCall(context, expression)
        is VarargExpression -> handleVarargExpression(context, expression)
        is VariableUsage -> context.print(expression.name)
        is CodeLine -> {
            context.print(expression.line)
        }
    }
}

private fun handleFunctionCall(context: PrintingContext, expression: FunctionCall) {
    context.print(expression.name)

    val lastLambda =
        if (expression.args.lastOrNull() is LambdaExpression) expression.args.last() as LambdaExpression else null

    val argsWithoutLastLambda = if (lastLambda == null) {
        expression.args
    } else {
        expression.args.dropLast(1)
    }

    val needBraces = (lastLambda == null || argsWithoutLastLambda.isNotEmpty())

    if (needBraces) {
        context.print("(")
    }
    argsWithoutLastLambda.forEachIndexed { index, arg ->
        printExpression(context, arg)
        if (index < argsWithoutLastLambda.lastIndex) {
            context.print(", ")
        }
    }
    if (needBraces) {
        context.print(")")
    }

    if (lastLambda != null) {
        context.print(" ")
        handleLambdaExpression(context, lastLambda)
    }
}

private fun handleLambdaExpression(context: PrintingContext, expression: LambdaExpression) {
    context.print("{")
    context.newLine()
    context.stepIn()
    doPrint(context, expression.block)
    context.stepOut()
    context.print("}")
}

private fun handleEnumEntryLiteral(context: PrintingContext, expression: EnumEntryLiteral) {
    context.print(expression.enumType.canonicalName)
    context.print(".")
    context.print(expression.entry.name)
}

private fun handleValueLiteral(context: PrintingContext, expression: ValueLiteral) {
    when (expression.value) {
        is String -> context.print("\"${expression.value}\"")
        is Int -> context.print(expression.value.toString())
        is Boolean -> context.print(expression.value.toString())
        else -> throw IllegalStateException("Value '${expression.value}' with type ${expression.value.javaClass} unsupported as argument")
    }
}

private fun handlePropertySetter(context: PrintingContext, expression: PropertySetter) {
    context.print(expression.name)
    context.print(" = ")
    printExpression(context, expression.value)
}

private fun handleSetOperatorCall(context: PrintingContext, expression: SetOperatorCall) {
    printExpression(context, expression.leftSide)
    if (context.language == ScriptLanguage.GROOVY
        || (context.language == ScriptLanguage.KTS && context.gradleVersion >= SemVer.ofThreePartOrNull("8.0.0")!!)
    ) {
        context.print(" = ")
        printExpression(context, expression.rightSide)
    } else {
        context.print(".set(")
        printExpression(context, expression.rightSide)
        context.print(")")
    }
}

private fun handleVarargExpression(context: PrintingContext, expression: VarargExpression) {
    expression.elements.forEachIndexed { index, element ->
        printExpression(context, element)
        if (index < expression.elements.lastIndex) {
            context.print(", ")
        }
    }
}

private class PrintingContext(private val appender: (String) -> Unit, val language: ScriptLanguage, val gradleVersion: SemVer) {
    var indents: Int = 0
    var emptyLine: Boolean = true

    fun newLine() {
        appender.invoke("\n")
        emptyLine = true
    }

    fun stepIn() {
        indents++
    }
    fun stepOut() {
        indents--
    }

    fun print(message: String) {
        if (emptyLine) {
            for (i in 0 until indents) {
                appender.invoke("    ")
            }
        }
        appender.invoke(message)
        emptyLine = false
    }

}
