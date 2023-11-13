/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.mirroring

import kotlinx.kover.gradle.plugin.test.functional.framework.common.ScriptLanguage


internal inline fun <reified Dsl : Any, reified Scope : Any> printGradleDsl(
    language: ScriptLanguage,
    gradleVersion: String,
    rootBlock: String? = null,
    noinline block: Dsl.(Scope) -> Unit
): String {
    val invokes = collectInvokesWithScope(Dsl::class.java, Scope::class.java, block)
    val parsedBlock = parse(invokes)
    return printCode(rootBlock, language, gradleVersion, parsedBlock)
}

internal inline fun <reified Dsl : Any> printGradleDsl(
    language: ScriptLanguage,
    gradleVersion: String,
    rootBlock: String? = null,
    noinline block: Dsl.() -> Unit
): String {
    val invokes = collectInvokes(Dsl::class.java, block)
    val parsedBlock = parse(invokes)
    return printCode(rootBlock, language, gradleVersion, parsedBlock)
}