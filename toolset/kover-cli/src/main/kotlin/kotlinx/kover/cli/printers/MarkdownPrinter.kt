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

package kotlinx.kover.cli.printers

import kotlinx.kover.cli.commands.Command
import kotlinx.kover.cli.commands.CommandParser
import org.kohsuke.args4j.spi.OptionHandler
import java.io.PrintWriter
import kotlin.math.max

private const val OPTION_HEADER = " Option "
private const val DESCRIPTION_HEADER = " Description "

internal object MarkdownPrinter {
    internal fun printUsage(command: Command, writer: PrintWriter) {
        val parser = CommandParser(command)
        writer.println(command.description)
        writer.println()
        writer.printSingleLineUsage(parser)
        writer.println()
        writer.println()
        // determine the length of the option + metavar first

        var maxNameLen = OPTION_HEADER.length
        var maxDescLen = DESCRIPTION_HEADER.length

        for (h in parser.arguments) {
            maxNameLen = max(maxNameLen, h.getNameAndMeta(null, parser.properties).length + 2)
            maxDescLen = max(maxDescLen, h.option.usage().length + 2)
        }
        for (h in parser.options) {
            maxNameLen = max(maxNameLen, h.getNameAndMeta(null, parser.properties).length + 2)
            maxDescLen = max(maxDescLen, h.option.usage().length + 2)
        }

        // print header
        writer.print("|")
        writer.print(OPTION_HEADER)
        writer.print(" ".repeat(maxNameLen -  OPTION_HEADER.length))
        writer.print("|")
        writer.print(DESCRIPTION_HEADER)
        writer.print(" ".repeat(maxDescLen -  DESCRIPTION_HEADER.length))
        writer.print("| Required | Multiple |")
        writer.println()
        writer.print("|")
        writer.print("-".repeat(maxNameLen))
        writer.print("|")
        writer.print("-".repeat(maxDescLen))
        writer.print("|:--------:|:--------:|")
        writer.println()
        // then print

        for (h in parser.arguments) {
            writer.printOptionMarkdown(h, h.getNameAndMeta(null, parser.properties), maxNameLen, maxDescLen)
            writer.println()
        }
        for (h in parser.options) {
            writer.printOptionMarkdown(h, h.getNameAndMeta(null, parser.properties), maxNameLen, maxDescLen)
            writer.println()
        }

        writer.println()
    }

    private fun PrintWriter.printOptionMarkdown(handler: OptionHandler<Any>, name: String, maxNameLen: Int, maxDescLen: Int) {
        val option = handler.option
        write("| ")
        print(name)
        print(" ".repeat(maxNameLen - name.length - 2))
        write(" | ")
        val desc = option.usage()
        print(desc)
        print(" ".repeat(maxDescLen - desc.length - 2))
        write(" |")

        if (option.required()) {
            print("    +     ")
        } else {
            print("          ")
        }
        write("|")
        if (option.isMultiValued || handler.setter.isMultiValued) {
            print("    +     ")
        } else {
            print("          ")
        }
        write("|")
    }
}