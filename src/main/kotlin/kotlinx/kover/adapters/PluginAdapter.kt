package kotlinx.kover.adapters

import kotlinx.kover.adapters.api.*
import org.gradle.api.*
import java.io.*

private fun createAdapters(): List<CompilationPluginAdapter> {
    return listOf(
        OldJavaPluginAdapter(),
        KotlinMultiplatformPluginAdapter(),
        AndroidPluginAdapter(),
        KotlinAndroidPluginAdapter()
    )
}

fun Project.collectDirs(): Pair<List<File>, List<File>> {
    val srcDirs = HashMap<String, File>()
    val outDirs = HashMap<String, File>()

    createAdapters().forEach {
        val dirs = it.findDirs(this)
        srcDirs += dirs.sources.asSequence().map { f -> f.canonicalPath to f }
        outDirs += dirs.output.asSequence().map { f -> f.canonicalPath to f }
    }

    val src = srcDirs.asSequence().map { it.value }.filter { it.exists() && it.isDirectory }.toList()
    val out = outDirs.asSequence().map { it.value }.filter { it.exists() && it.isDirectory }.toList()

    return src to out
}
