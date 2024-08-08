/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.tests.functional.framework

import java.io.File
import java.nio.file.Files

private const val EXAMPLES_DIR = "examples"

fun runAndCheckExample(name: String, vararg args: String, checker: CheckerContext.() -> Unit) {
    val exampleDir = File(EXAMPLES_DIR).resolve(name)
    if (!exampleDir.exists()) {
        throw MavenAssertionException("Example '$exampleDir' not found in directory '$EXAMPLES_DIR'")
    }

    exampleDir.runAndCheck(args.toList(), checker)
}

private fun File.runAndCheck(commands: List<String>, checker: CheckerContext.() -> Unit) {
    val workingDirectory = Files.createTempDirectory("kover-maven-test").toFile()

    this.copyRecursively(workingDirectory)

    val log = runMaven(
        workingDirectory,
        commands,
        SystemProperties.repository,
        SystemProperties.kotlinVersion,
        SystemProperties.koverVersion
    ).replace("\r\n", "\n") // for Windows logs

    try {
        createContext(log, workingDirectory).checker()

        // delete build files
        workingDirectory.deleteRecursively()
    } catch (t: Throwable) {
        throw AssertionError("Assertion error: ${t.message}\nProject build path file://${workingDirectory.canonicalPath}\nBuild log:\n\n$log", t)
    }
}