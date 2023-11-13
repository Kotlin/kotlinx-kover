/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.mirroring

import java.util.Locale

internal fun parse(invokes: List<Invoke>): CodeBlock {
    val context = ParseContext()
    return parseBlock(invokes, context)
}

private fun parseBlock(invokes: List<Invoke>, context: ParseContext) : CodeBlock {
    val statements = invokes.mapNotNull { invoke ->
        if (invoke.isUnused()) {
            parseExpression(invoke, context)
        } else if (invoke.isUsedSeveralTimes()) {
            val name = context.addVariable(invoke)
            val expr = parseExpression(invoke, context, false)
            VariableInitialization(name, expr)
        } else {
            null
        }
    }

    return CodeBlock(statements)
}


private class ParseContext {
    private val variables: MutableMap<Invoke, String> = mutableMapOf()

    fun addVariable(invoke: Invoke): String {
        val name = "var${variables.size}"
        variables[invoke] = name
        return name
    }

    fun getVariable(invoke: Invoke): String {
        return variables[invoke] ?: throw IllegalStateException("Not found variable for invoke: $invoke")
    }
}


private fun parseExpression(invoke: Invoke, context: ParseContext, mayBeVariable: Boolean = true): Expression {
    if (invoke.isUsedSeveralTimes() && mayBeVariable) {
        return VariableUsage(context.getVariable(invoke))
    }

    return when (invoke) {
        is ValueInvoke -> parseValue(invoke.value)
        is VarargInvoke -> parseVararg(invoke, context)
        is EnumEntryInvoke -> EnumEntryLiteral(invoke.enumType, invoke.entry)
        is FunctionInvoke -> parseFunction(invoke, context)
        is LambdaInvoke -> LambdaExpression(parseBlock(invoke.invokes, context))
    }

}

private fun parseFunction(invoke: FunctionInvoke, context: ParseContext): Expression {
    val receiver = invoke.prev?.let { parseExpression(it, context) }

    // special override for provider setter
    if (invoke.name == "set"
        && invoke.args.size == 1
        && receiver != null
        && invoke.thisType != null
        && ProviderClass.isAssignableFrom(invoke.thisType)
    ) {
        val rightSide = parseExpression(invoke.args[0], context)
        return SetOperatorCall(receiver, rightSide)
    }

    if (invoke.name.startsWith("get") && invoke.name != "get" && invoke.args.isEmpty()) {
        val propertyName = extractPropertyName(invoke.name)
        return PropertyGetter(propertyName, receiver)
    }

    if (invoke.name.startsWith("set") && invoke.name != "set" && invoke.args.size == 1) {
        val propertyName = extractPropertyName(invoke.name)
        val value =  parseExpression(invoke.args[0], context)
        return PropertySetter(propertyName, value, receiver)
    }

    val args = invoke.args.map { arg ->
        parseExpression(arg, context)
    }

    return FunctionCall(invoke.name, receiver, args)
}

private fun parseVararg(invoke: VarargInvoke, context: ParseContext): VarargExpression {
    val elements = invoke.elements.map { element ->
        parseExpression(element, context)
    }
    return VarargExpression(elements)
}

private fun parseValue(value: Any?): LiteralExpression {
    if (value == null) return NullLiteral
    return ValueLiteral(value)
}

private fun extractPropertyName(methodName: String): String {
    return methodName.substring(3).replaceFirstChar { it.lowercase(Locale.getDefault()) }
}

internal class CodeBlock(val statements: List<Statement>)

internal sealed interface Statement

internal sealed class Expression(val receiver: Expression?): Statement

internal class VariableInitialization(val name: String, val rightSide: Expression): Statement

internal class LambdaExpression(val block: CodeBlock): Expression(null)

internal class VariableUsage(val name: String): Expression(null)

internal class SetOperatorCall(val leftSide: Expression, val rightSide: Expression): Expression(null)
internal class PropertyGetter(val name: String, receiver: Expression?): Expression(receiver)
internal class PropertySetter(val name: String, val value: Expression, receiver: Expression?): Expression(receiver)

internal class FunctionCall(val name: String, receiver: Expression?, val args: List<Expression>): Expression(receiver)

internal class VarargExpression(val elements: List<Expression>): Expression(null)

internal sealed class LiteralExpression: Expression(null)

internal object NullLiteral: LiteralExpression()
internal class EnumEntryLiteral(val enumType: Class<Any>, val entry: Enum<*>): LiteralExpression()
internal class ValueLiteral(val value: Any): LiteralExpression()
