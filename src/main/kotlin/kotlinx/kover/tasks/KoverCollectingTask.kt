package kotlinx.kover.tasks

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*

open class KoverCollectingTask : DefaultTask() {
    /**
     * Specifies directory path for collecting of all XML and HTML reports from all modules.
     */
    val outputDir: DirectoryProperty = project.objects.directoryProperty()
        @OutputDirectory get

    internal var xmlFiles: MutableMap<String, RegularFileProperty> = mutableMapOf()
        @Internal get

    internal var htmlDirs: MutableMap<String, DirectoryProperty> = mutableMapOf()
        @Internal get

    @TaskAction
    fun collect() {
        project.copy {
            it.into(outputDir)
            xmlFiles.forEach { (p, f) ->
                it.from(f) { c ->
                    c.rename { "$p.xml" }
                }
            }
        }

        htmlDirs.forEach { (p, d) ->
            project.copy {
                it.from(d)
                it.into(outputDir.dir("html/$p"))
            }
        }
    }
}
