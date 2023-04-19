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

package kotlinx.kover.cli.md

import kotlinx.kover.cli.commands.RootCommand
import java.io.PrintWriter

/**
 * Used for internal purposes to generate documentation.
 * Not an actual test
 */
fun main() {
    val output = PrintWriter(System.out, true)
    RootCommand.commands.forEach { command ->
        MarkdownPrinter.printUsage(command, output)
    }
}