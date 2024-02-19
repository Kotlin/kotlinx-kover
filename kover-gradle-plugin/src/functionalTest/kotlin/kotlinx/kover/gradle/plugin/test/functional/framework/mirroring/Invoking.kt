/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.mirroring

import org.gradle.api.Action
import org.gradle.api.provider.Provider
import java.lang.reflect.*

@Suppress("UNCHECKED_CAST")
internal fun <Dsl : Any, Scope : Any> collectInvokesWithScope(
    dslType: Class<Dsl>,
    scopeType: Class<Scope>,
    block: Dsl.(Scope) -> Unit
): List<Invoke> {
    val slicer = BlockSlicer()
    slicer.stepInto()

    val dsl = Proxy.newProxyInstance(
        dslType.getClassLoader(),
        arrayOf<Class<*>>(dslType),
        Handler(null, slicer)
    ) as Dsl

    val scope = Proxy.newProxyInstance(
        scopeType.getClassLoader(),
        arrayOf<Class<*>>(scopeType),
        Handler(null, slicer)
    ) as Scope

    dsl.block(scope)

    return slicer.stepOut()
}

@Suppress("UNCHECKED_CAST")
internal fun <Dsl : Any> collectInvokes(
    dslType: Class<Dsl>,
    block: Dsl.() -> Unit
): List<Invoke> {
    val slicer = BlockSlicer()
    slicer.stepInto()

    val dsl = Proxy.newProxyInstance(
        dslType.getClassLoader(),
        arrayOf<Class<*>>(dslType),
        Handler(null, slicer)
    ) as Dsl

    dsl.block()

    return slicer.stepOut()
}


private class BlockSlicer {
    private var frame: CodeFrame? = null
    operator fun plusAssign(invoke: Invoke) {
        val currentFrame = frame ?: throw IllegalStateException("No current code block")
        currentFrame.invokes += invoke
    }

    fun stepInto() {
        val new = CodeFrame(frame, mutableListOf())
        frame = new
    }

    fun stepOut(): List<Invoke> {
        val currentFrame = frame ?: throw IllegalStateException("No current code block")
        frame = currentFrame.parent

        return currentFrame.invokes
    }

    private class CodeFrame(val parent: CodeFrame?, val invokes: MutableList<Invoke>)
}


internal sealed class Invoke(val prev: Invoke?) {
    private var usageCount: Int = 0

    init {
        if (prev != null) {
            prev.usageCount++
        }
    }

    fun asArgument() {
        usageCount++
    }

    fun isUnused(): Boolean {
        return usageCount == 0
    }

    fun isUsedSeveralTimes(): Boolean {
        return usageCount > 1
    }
}

internal open class FunctionInvoke(
    prev: Invoke?,
    val name: String,
    val returnType: Class<*>,
    val thisType: Class<*>?,
    val args: List<Invoke> = emptyList()
) : Invoke(prev) {
    init {
        args.forEach { arg -> arg.asArgument() }
    }
}


internal class VarargInvoke(val elements: List<Invoke>): Invoke(null)

internal class ValueInvoke(val value: Any?): Invoke(null)

internal class EnumEntryInvoke(val enumType: Class<Any>, val entry: Enum<*>): Invoke(null)

internal class LambdaInvoke(val invokes: List<Invoke>): Invoke(null)

internal interface ExpressionResult {
    fun `$getMyself`(): Invoke
}


private class Handler(
    private val receiver: Invoke? = null,
    private val slicer: BlockSlicer
) : InvocationHandler {

    override fun invoke(proxy: Any?, method: Method, args: Array<Any?>?): Any? {
        val methodName = method.name
        val params = method.parameters

        if (methodName == "toString") {
            return receiver?.let { "$it." } ?: ""
        }

        if (methodName == "\$getMyself") {
            return receiver
        }

        val argInvokes = invokeArgs(params.toList(), args)

        val invoke = FunctionInvoke(receiver, methodName, method.returnType, proxy?.javaClass, argInvokes)
        slicer += invoke
        return wrapResul(invoke, method, proxy, args)
    }

    private fun invokeArgs(params: List<Parameter>, args: Array<Any?>?): List<Invoke> {
        return params.mapIndexed { index, parameter ->
            if (parameter.isVarArgs) {
                varargInvoke(parameter, slicer, args!![index])
            } else {
                valueInvoke(parameter, slicer, args!![index])
            }
        }
    }

    private fun wrapResul(resultAsReceiver: Invoke?, method: Method, proxy: Any?, args: Array<Any?>?): Any? {
        val type = method.returnType

        if (ProviderClass.isAssignableFrom(type)) {
            return Proxy.newProxyInstance(
                type.getClassLoader(),
                arrayOf<Class<*>>(type, ExpressionResult::class.java),
                Handler(resultAsReceiver, slicer)
            )
        }

        if (type.isInterface) {
            return Proxy.newProxyInstance(
                type.getClassLoader(),
                arrayOf(type, ExpressionResult::class.java),
                Handler(resultAsReceiver, slicer)
            )
        }

        if (type == Void.TYPE) {
            return null
        }

        return if (args == null) {
            method.invoke(proxy)
        } else {
            method.invoke(proxy, args)
        }
    }
}

private fun varargInvoke(parameter: Parameter, slicer: BlockSlicer, value: Any?): Invoke {
    if (value == null) return ValueInvoke(null)

    @Suppress("UNCHECKED_CAST") val elements = (value as Array<Any>).map { element ->
        valueInvoke(parameter, slicer, element)
    }

    return VarargInvoke(elements)
}

private fun valueInvoke(parameter: Parameter, slicer: BlockSlicer, value: Any?): Invoke {
    if (value == null) return ValueInvoke(null)

    if (value is ExpressionResult) {
        return value.`$getMyself`()
    }

    if (parameter.isFunctionalType()) {
        return lambdaInvoke(parameter, slicer, value)
    }

    val actualType = value.javaClass

    if (actualType.isEnum) {
        return EnumEntryInvoke(actualType, value as Enum<*>)
    }

    return ValueInvoke(value)
}

private fun Parameter.isFunctionalType(): Boolean {
    return type == Action::class.java || Function1::class.java.isAssignableFrom(type)
}

private fun lambdaInvoke(parameter: Parameter, slicer: BlockSlicer, value: Any): Invoke {
    val parameterType =
        (parameter.parameterizedType as ParameterizedType).actualTypeArguments[0]

    val actionClass = when (parameterType) {
        is Class<*> -> {
            if (!parameterType.isInterface) {
                throw IllegalStateException("Supports only interface parameter types but has '$parameterType'")
            }
            parameterType
        }

        is WildcardType -> {
            parameterType.lowerBounds.filterIsInstance<Class<*>>().firstOrNull {
                it.isInterface
            } ?: throw IllegalStateException("Now interface lower bound for type '$parameterType'")
        }

        else -> {
            throw IllegalStateException("Unknown parameter type '$parameterType'")
        }
    }

    // TODO support non-final classes (should use another lib?)
    val actionReceiver = Proxy.newProxyInstance(
        actionClass.getClassLoader(),
        arrayOf(actionClass),
        Handler(null, slicer)
    )

    slicer.stepInto()
    if (parameter.type == Action::class.java) {
        @Suppress("UNCHECKED_CAST")
        (value as Action<Any>).execute(actionReceiver)
    } else if (Function1::class.java.isAssignableFrom(parameter.type)) {
        @Suppress("UNCHECKED_CAST")
        (value as Function1<Any, Any>).invoke(actionReceiver)
    } else {
        throw IllegalStateException("Unknown action type ${parameter.type}")
    }
    val blockInvokes = slicer.stepOut()
    return LambdaInvoke(blockInvokes)
}

internal val ProviderClass = Provider::class.java
