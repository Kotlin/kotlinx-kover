package kotlinx.kover.tasks

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*

open class KoverCollectingTask : DefaultTask() {
    /**
     * Specifies directory path for collecting of all XML and HTML reports from all projects.
     */
    @get:OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()

    @get:Internal
    internal var xmlFiles: MutableMap<String, RegularFileProperty> = mutableMapOf()

    @get:Internal
    internal var htmlDirs: MutableMap<String, DirectoryProperty> = mutableMapOf()


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
            // delete directory for HTML reports so that the old reports do not overlap with the new ones
            project.delete(outputDir.dir("html/$p"))

            project.copy {
                it.from(d)
                it.into(outputDir.dir("html/$p"))
            }
        }
    }
}
