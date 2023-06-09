/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.util

import org.gradle.internal.metaobject.*

internal fun Any.bean(): DynamicBean = DynamicBean(this)

internal class DynamicBean(val origin: Any) {
    private val gradleWrapper = BeanDynamicObject(origin)

    operator fun get(name: String): DynamicBean {
        return gradleWrapper.getProperty(name).bean()
    }

    fun hasFunction(functionName: String, vararg args: Any?): Boolean {
        return gradleWrapper.hasMethod(functionName, *args)
    }

    fun call(functionName: String, vararg args: Any?): Any? {
        return gradleWrapper.invokeMethod(functionName, *args)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> property(name: String): T {
        return gradleWrapper.getProperty(name) as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> propertyOrNull(name: String): T? {
        if (!gradleWrapper.hasProperty(name)) return null

        return gradleWrapper.getProperty(name) as T
    }

    operator fun contains(name: String): Boolean = gradleWrapper.hasProperty(name)

    @Suppress("UNCHECKED_CAST")
    fun propertyBeans(name: String): Collection<DynamicBean> {
        return (gradleWrapper.getProperty(name) as Collection<Any>).map { it.bean() }
    }
    @Suppress("UNCHECKED_CAST")
    fun <T> propertyCollection(name: String): Collection<T> {
        return gradleWrapper.getProperty(name) as Collection<T>
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
