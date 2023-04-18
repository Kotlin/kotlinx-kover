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

package kotlinx.kover.cli.commands

import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.OptionDef
import org.kohsuke.args4j.spi.Messages
import org.kohsuke.args4j.spi.OptionHandler
import org.kohsuke.args4j.spi.Parameters
import org.kohsuke.args4j.spi.Setter
import java.io.PrintWriter
import java.util.*


internal class RootCommand : Command {

    @Argument(handler = RootHandler::class, required = true)
    private var command: Command? = null

    override val name: String? = null

    override val description = "Command line interface for Kover - Kotlin Coverage Toolset"

    override fun call(output: PrintWriter, error: PrintWriter) = command?.call(output, error) ?: 0

    // no constructor without args
    class RootHandler(parser: CmdLineParser?, option: OptionDef, setter: Setter<in Command>?) :
        OptionHandler<Command>(
            parser,
            object : OptionDef(
                joinedCommandNames(), "<command>",
                option.required(), option.help(), option.hidden(),
                RootHandler::class.java, option.isMultiValued
            ) {}, setter
        ) {


        override fun parseArguments(params: Parameters): Int {
            val invokedCommand = params.getParameter(0)
            for (command in commands) {
                if (command.name == invokedCommand) {
                    // separate arguments of subcommand
                    val args = ArrayList<String>()
                    for (i in 1 until params.size()) {
                        args.add(params.getParameter(i))
                    }
                    val parser = CommandParser(command)
                    // inject parsed values
                    parser.parseArgument(args)
                    setter.addValue(command)
                    return params.size()
                }
            }
            throw CmdLineException(owner, Messages.ILLEGAL_OPERAND, option.toString(), invokedCommand)
        }

        override fun getDefaultMetaVariable() = "<command>"
    }

    companion object {
        val commands: List<Command> =
            listOf(OfflineInstrumentCommand(), ReportCommand())

        private fun joinedCommandNames(): String {
            return commands.joinToString(" | ") { it.name ?: "" }
        }
    }

}
