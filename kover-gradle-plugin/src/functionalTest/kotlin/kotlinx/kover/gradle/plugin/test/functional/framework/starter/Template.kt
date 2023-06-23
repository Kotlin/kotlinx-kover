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

private class TemplateGradleTest : DirectoryBasedGradleTest() {

    override fun readAnnotationArgs(element: AnnotatedElement?): RunCommand {
        val annotation = element?.getAnnotation(TemplateTest::class.java)
            ?: throw IllegalStateException("Test not marked by '${TemplateTest::class.qualifiedName}' annotation")

        val templateName = annotation.templateName

        val sources = buildFromTemplate(templateName)
        sources.buildType = "template test"
        sources.buildName = templateName
        val commands = annotation.commands.toList()
        return RunCommand(sources, commands)
    }
}
