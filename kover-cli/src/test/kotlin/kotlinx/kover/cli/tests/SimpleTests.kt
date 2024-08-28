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

package kotlinx.kover.cli.tests

import kotlinx.kover.cli.invokeCli
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

private const val RESOURCES_PATH = "src/test/resources"

class SimpleTests {
    @Test
    fun instrument() {
        val classes = File("$RESOURCES_PATH/classes")
        val dir = createTempDirectory("kover-offline-test")
        val targetPath = dir.toFile().canonicalPath

        val args = buildList {
            add("instrument")
            add(classes.canonicalPath)
            add("--dest")
            add(targetPath)
        }

        println("Offline instrumentation with args: " + args.joinToString(" "))
        println("Output dir file://$targetPath")
        assertEquals(0, invokeCli(args.toTypedArray()))
    }

    @Test
    fun report() {
        val classes = File("$RESOURCES_PATH/classes")
        val sources = File("$RESOURCES_PATH/sources")
        val ic = File("$RESOURCES_PATH/test.ic")

        val dir = createTempDirectory("kover-html-test")
        val targetPath = dir.toFile().canonicalPath

        val args = buildList {
            add("report")
            add(ic.canonicalPath)
            add("--src")
            add(sources.canonicalPath)
            add("--classfiles")
            add(classes.canonicalPath)
            add("--html")
            add(targetPath)
            add("--title")
            add("kover test")
        }

        println("Generate report with args: " + args.joinToString(" "))
        println("Output HTML path file://$targetPath")
        assertEquals(0, invokeCli(args.toTypedArray()))
    }

    @Test
    fun merge() {
        // class `com.example.A` is present only in test1.ic and `com.example.B` only in test2.ic
        val ic1 = File("$RESOURCES_PATH/merge/test1.ic")
        val ic2 = File("$RESOURCES_PATH/merge/test2.ic")

        val target = kotlin.io.path.createTempFile("kover-merge-test", ".ic").toFile()

        val args = buildList {
            add("merge")
            add(ic1.canonicalPath)
            add(ic2.canonicalPath)
            add("--target")
            add(target.canonicalPath)
        }

        println("Merge reports, args: " + args.joinToString(" "))
        assertEquals(0, invokeCli(args.toTypedArray()))

        val contentAsUtf8 = target.readText()
        // check both classes are present in merged report
        assertContains(contentAsUtf8, "com.example.A")
        assertContains(contentAsUtf8, "com.example.B")
    }
}