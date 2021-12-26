package kotlinx.kover

import kotlinx.kover.adapters.*
import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.*
import kotlinx.kover.engines.commons.CoverageAgent
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.testing.*
import java.io.*


internal fun Project.createProviders(agents: Map<CoverageEngine, CoverageAgent>): AllProviders {
    val projects: MutableMap<String, ProjectProviders> = mutableMapOf()

    allprojects {
        projects[it.name] = ProjectProviders(
            it.provider { it.files(it.binaryReports(this)) },
            it.provider { it.files(it.smapFiles(this)) },
            it.provider { it.testTasks(this) },
            it.provider { it.collectDirs(this).first },
            it.provider { it.collectDirs(this).second },
            it.provider { it.isDisabled(this) }
        )
    }

    val engineProvider = provider { extensions.getByType(KoverExtension::class.java).coverageEngine.get() }

    val classpathProvider: Provider<FileCollection> = provider {
        val koverExtension = extensions.getByType(KoverExtension::class.java)
        agents.getFor(koverExtension.coverageEngine.get()).classpath
    }

    val extensionProvider = provider { extensions.getByType(KoverExtension::class.java) }


    val allReportsProvider: Provider<FileCollection> = provider { files(allBinaryReports()) }
    val allSmapProvider: Provider<FileCollection> = provider { files(allSmapFiles()) }
    val allTestsProvider = provider { allTestTasks() }

    // all sources and all outputs providers are unused, so NOW it can return empty file collection
    val emptyProvider: Provider<FileCollection> = provider { files() }
    val aggregatedProviders =
        ProjectProviders(
            allReportsProvider,
            allSmapProvider,
            allTestsProvider,
            emptyProvider,
            emptyProvider,
            provider { false })

    return AllProviders(projects, aggregatedProviders, engineProvider, classpathProvider, extensionProvider)
}


internal fun Project.allTestTasks(): List<Test> {
    return allprojects.flatMap { it.testTasks(this) }
}

internal fun Project.allBinaryReports(): List<File> {
    return allprojects.flatMap { it.binaryReports(this) }
}

internal fun Project.allSmapFiles(): List<File> {
    return allprojects.flatMap { it.smapFiles(this) }
}


internal fun Project.testTasks(rootProject: Project): List<Test> {
    if (isDisabled(rootProject)) {
        return emptyList()
    }

    return tasks.withType(Test::class.java)
        .filterNot { t -> t.extensions.getByType(KoverTaskExtension::class.java).isDisabled }
}

internal fun Project.binaryReports(root: Project): List<File> {
    if (isDisabled(root)) {
        return emptyList()
    }

    return tasks.withType(Test::class.java).asSequence()
        .map { t -> t.extensions.getByType(KoverTaskExtension::class.java) }
        // process binary report only from tasks with enabled cover
        .filterNot { e -> e.isDisabled }
        .map { e -> e.binaryReportFile.get() }
        // process binary report only from tasks with sources
        .filter { f -> f.exists() }
        .toList()
}

internal fun Project.smapFiles(root: Project): List<File> {
    if (isDisabled(root)) {
        return emptyList()
    }

    return tasks.withType(Test::class.java).asSequence()
        .map { t -> t.extensions.getByType(KoverTaskExtension::class.java) }
        .filterNot { e -> e.isDisabled }
        .mapNotNull { e -> e.smapFile.orNull }
        /*
         Binary reports and SMAP files have same ordering for IntelliJ engine:
            * SMAP file is null if coverage engine is a JaCoCo by default - in this case property is unused
            * SMAP file not creates by JaCoCo - property is unused
            * test task have no sources - in this case binary report and SMAP file not exists
         */
        .filter { f -> f.exists() }
        .toList()
}

private fun Project.collectDirs(root: Project): Pair<FileCollection, FileCollection> {
    if (isDisabled(root)) {
        return files() to files()
    }

    val srcDirs = HashMap<String, File>()
    val outDirs = HashMap<String, File>()

    createAdapters().forEach {
        val dirs = it.findDirs(this)
        srcDirs += dirs.sources.asSequence().map { f -> f.canonicalPath to f }
        outDirs += dirs.output.asSequence().map { f -> f.canonicalPath to f }
    }

    val src = srcDirs.asSequence().map { it.value }.filter { it.exists() && it.isDirectory }.toList()
    val out = outDirs.asSequence().map { it.value }.filter { it.exists() && it.isDirectory }.toList()

    return files(src) to files(out)
}

private fun Project.isDisabled(root: Project): Boolean {
    return root.extensions.getByType(KoverExtension::class.java).disabledProjects.contains(name)
}


internal class AllProviders(
    val projects: Map<String, ProjectProviders>,
    val aggregated: ProjectProviders,

    val engine: Provider<CoverageEngine>,
    val classpath: Provider<FileCollection>,
    val koverExtension: Provider<KoverExtension>
)

internal class ProjectProviders(
    val reports: Provider<FileCollection>,
    val smap: Provider<FileCollection>,
    val tests: Provider<List<Test>>,
    val sources: Provider<FileCollection>,
    val output: Provider<FileCollection>,
    val disabled: Provider<Boolean>
)

