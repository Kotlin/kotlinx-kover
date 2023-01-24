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

    @Suppress("UNCHECKED_CAST")
    fun <T> property(name: String): T {
        return gradleWrapper.getProperty(name) as T
    }

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
