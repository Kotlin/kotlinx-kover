/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlinx.kover.cli

import kotlinx.kover.cli.commands.Command
import kotlinx.kover.cli.commands.CommandParser
import kotlinx.kover.cli.commands.RootCommand
import kotlinx.kover.cli.printers.TerminalPrinter
import org.kohsuke.args4j.CmdLineException
import java.io.PrintWriter
import kotlin.system.exitProcess

internal fun invokeCli(args: Array<String>): Int {
    val output = PrintWriter(System.out, true)
    val error = PrintWriter(System.err, true)

    val rootCommand: Command = RootCommand()
    val parser = CommandParser(rootCommand)
    try {
        parser.parseArgument(*args)
    } catch (e: CmdLineException) {
        val errorParser = e.parser
        if (errorParser is CommandParser) {
            TerminalPrinter.printUsage(errorParser.command, error)
            error.println()
        }
        error.println(e.message)

        return -1
    }

    return rootCommand.call(output, error)
}

fun main(args: Array<String>) {
    val code = invokeCli(args)
    exitProcess(code)
}
