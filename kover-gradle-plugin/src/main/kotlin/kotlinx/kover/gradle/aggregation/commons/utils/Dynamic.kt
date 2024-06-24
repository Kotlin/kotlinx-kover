/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.commons.utils

import org.gradle.internal.metaobject.*

internal fun Any?.bean(): DynamicBean = DynamicBean(this)

internal class DynamicBean(private val origin: Any?) {
    private val wrappedOrigin = origin?.let { BeanDynamicObject(origin) }

    operator fun get(name: String): DynamicBean = getNotNull("get property '$name'").getProperty(name).bean()

    operator fun contains(name: String): Boolean = getNotNull("check for a property '$name'").hasProperty(name)

    inline fun <reified T> value(): T {
        val notNull = origin ?: throw IllegalStateException("Value is null, failed to get value")
        return notNull as? T ?: throw IllegalStateException("Invalid property value type, expected ${T::class.qualifiedName}, found ${notNull::class.qualifiedName}")
    }

    inline fun <reified T> orNull(): T? {
        if (origin == null) return null
        return value<T>()
    }

    fun sequence(): Sequence<DynamicBean> {
        return value<Iterable<*>>().asSequence().map { it.bean() }
    }

    private fun getNotNull(extra: String): BeanDynamicObject {
        return wrappedOrigin ?: throw IllegalArgumentException("Wrapped value is null, failed to $extra")
    }
}

internal fun Any.hasSuper(className: String): Boolean {
    val kClass: Class<*> = this::class.java
    return extendedOf(className, kClass)
}

private fun extendedOf(className: String, currentClass: Class<*>): Boolean {
    if (currentClass.simpleName == className) return true

    if (currentClass.superclass != null) {
        if (extendedOf(className, currentClass.superclass)) return true
    }

    for (iface in currentClass.interfaces) {
        if (extendedOf(className, iface)) return true
    }

    return false
}
