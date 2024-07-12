/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.tests.functional.framework

import org.apache.maven.cli.MavenCli
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream


fun runMaven(
    projectDir: File,
    commands: List<String>,
    snapshotRepositoryPath: String,
    kotlinVersion: String,
    koverVersion: String
): String {
    val cli = MavenCli()

    val stdStream = ByteArrayOutputStream()

    System.setProperty("maven.multiModuleProjectDirectory", "")
    val args = mutableListOf<String>()
    args += commands
    args += "--s"
    args += File("src/functionalTest/templates/settings.xml").canonicalPath
    args += "-DsnapshotRepository=$snapshotRepositoryPath"
    args += "-Dkotlin.version=$kotlinVersion"
    args += "-Dkover.version=$koverVersion"

    val printer = PrintStream(stdStream)
    cli.doMain(args.toTypedArray(), projectDir.canonicalPath, printer, printer)
    return stdStream.toString()
}
