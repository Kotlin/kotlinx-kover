/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.util

import kotlinx.kover.gradle.plugin.commons.KoverCriticalException
import org.gradle.internal.metaobject.*

internal fun Any.bean(): DynamicBean = DynamicBean(this)

internal class DynamicBean(origin: Any) {
    private val wrappedOrigin = BeanDynamicObject(origin)

    operator fun get(name: String): DynamicBean = bean(name)

    operator fun invoke(functionName: String, vararg args: Any?): Any? {
        return wrappedOrigin.invokeMethod(functionName, *args)
    }

    operator fun contains(name: String): Boolean = wrappedOrigin.hasProperty(name)

    fun bean(name: String): DynamicBean {
        return value<Any>(name).bean()
    }

    fun beanOrNull(name: String): DynamicBean? {
        return valueOrNull<Any>(name)?.bean()
    }

    fun hasFunction(functionName: String, vararg args: Any?): Boolean {
        return wrappedOrigin.hasMethod(functionName, *args)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> value(name: String): T {
        return wrappedOrigin.getProperty(name) as? T
            ?: throw KoverCriticalException("Non-nullable '$name' property has `null` value in dynamic bean over '${wrappedOrigin.displayName}'")
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> valueOrNull(name: String): T? {
        return wrappedOrigin.getProperty(name) as T?
    }

    fun beanCollection(name: String): Collection<DynamicBean> {
        return value<Collection<Any>>(name).map { it.bean() }
    }

    fun <T> valueCollection(name: String): Collection<T> {
        return value(name)
    }
}

internal fun Any.hasSuperclass(className: String): Boolean {
    var kClass: Class<*>? = this::class.java
    while (kClass != null) {
        if (kClass.simpleName == className) {
            return true
        }
        kClass = kClass.superclass
    }

    return false
}
