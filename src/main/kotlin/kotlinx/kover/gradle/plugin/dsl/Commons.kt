/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

/**
 * TODO
 */
public const val DEPENDENCY_CONFIGURATION_NAME = "kover"

/**
 * TODO
 */
public const val PROJECT_EXTENSION_NAME = "kover"

/**
 * TODO
 */
public const val REGULAR_REPORT_EXTENSION_NAME = "koverReport"

/**
 * TODO
 */
public const val ANDROID_EXTENSION_NAME = "koverAndroid"

/**
 * Name of the XML report generation task for Kotlin JVM and Kotlin multi-platform projects.
 */
public const val REGULAR_XML_REPORT_NAME = "koverXmlReport"

/**
 * Name of the HTML report generation task for Kotlin JVM and Kotlin multi-platform projects.
 */
public const val REGULAR_HTML_REPORT_NAME = "koverHtmlReport"

/**
 * Name of the verification task for Kotlin JVM and Kotlin multi-platform projects.
 */
public const val REGULAR_VERIFY_REPORT_NAME = "koverVerify"

public interface KoverClassDefinitions {
    public fun className(vararg className: String)
    public fun className(classNames: Iterable<String>)

    public fun packageName(vararg className: String)
    public fun packageName(classNames: Iterable<String>)
}

public interface KoverTaskDefinitions {
    public fun taskName(vararg name: String)
    public fun taskName(names: Iterable<String>)
}
