/*
 * Copyright 2000-2024 JetBrains s.r.o.
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

interface KoverDocsExtension {
    val docsDirectory: Property<String>
    val description: Property<String>
    val callDokkaHtml: Property<Boolean>
}

val extension = extensions.create<KoverDocsExtension>("koverDocs")

extension.callDokkaHtml.convention(false)

tasks.register("releaseDocs") {
    dependsOn(
        tasks.matching { extension.callDokkaHtml.get() && it.name == "dokkaHtml" }
    )

    doLast {
        val dirName = extension.docsDirectory.get()
        val description = extension.description.get()

        val sourceDir = projectDir.resolve("docs")
        val resultDir = rootDir.resolve("docs/$dirName")
        val mainIndexFile = rootDir.resolve("docs/index.md")

        resultDir.mkdirs()
        sourceDir.copyRecursively(resultDir)
        mainIndexFile.appendText("- [$description]($dirName)\n")
    }
}
