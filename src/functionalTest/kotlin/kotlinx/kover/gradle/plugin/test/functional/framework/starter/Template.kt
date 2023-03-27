/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.starter

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.*
import org.opentest4j.*
import java.io.*
import java.lang.reflect.*
import java.nio.file.*

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Test
@Extensions(ExtendWith(TemplateGradleTest::class))
internal annotation class TemplateTest(val templateName: String, val commands: Array<String>)

private const val TEMPLATES_DIR = "src/functionalTest/templates/builds"

private class TemplateGradleTest : DirectoryBasedGradleTest() {

    override fun readAnnotationArgs(element: AnnotatedElement?): Foo {
        val annotation = element?.getAnnotation(TemplateTest::class.java)
            ?: throw IllegalStateException("Test not marked by '${TemplateTest::class.qualifiedName}' annotation")

        val templateName = annotation.templateName
        val templateDir = File(TEMPLATES_DIR).resolve(templateName)
        val commands = annotation.commands.toList()

        return Foo(templateName, templateDir, commands)
    }

    override val testType: String = "Template"
}
