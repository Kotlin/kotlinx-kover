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


internal fun Project.createProviders(agents: Map<CoverageEngine, CoverageAgent>): ProjectProviders {
    val modules: MutableMap<String, ModuleProviders> = mutableMapOf()

    allprojects {
        modules[it.name] = ModuleProviders(
            it.provider { it.files(it.binaryReports()) },
            it.provider { it.files(it.smapFiles()) },
            it.provider { it.testTasks() },
            it.provider { it.collectDirs().first },
            it.provider { it.collectDirs().second }
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
    val allModulesProviders =
        ModuleProviders(allReportsProvider, allSmapProvider, allTestsProvider, emptyProvider, emptyProvider)

    return ProjectProviders(modules, allModulesProviders, engineProvider, classpathProvider, extensionProvider)
}


internal fun Project.allTestTasks(): List<Test> {
    return allprojects.flatMap { it.testTasks() }
}

internal fun Project.allBinaryReports(): List<File> {
    return allprojects.flatMap { it.binaryReports() }
}

internal fun Project.allSmapFiles(): List<File> {
    return allprojects.flatMap { it.smapFiles() }
}


internal fun Project.testTasks(): List<Test> {
    return tasks.withType(Test::class.java)
        .filter { t -> t.extensions.getByType(KoverTaskExtension::class.java).isEnabled }
}

internal fun Project.binaryReports(): List<File> {
    return tasks.withType(Test::class.java).asSequence()
        .map { t -> t.extensions.getByType(KoverTaskExtension::class.java) }
        // process binary report only from tasks with enabled cover
        .filter { e -> e.isEnabled }
        .map { e -> e.binaryReportFile.get() }
        // process binary report only from tasks with sources
        .filter { f -> f.exists() }
        .toList()
}

internal fun Project.smapFiles(): List<File> {
    return tasks.withType(Test::class.java).asSequence()
        .map { t -> t.extensions.getByType(KoverTaskExtension::class.java) }
        .filter { e -> e.isEnabled }
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


internal class ProjectProviders(
    val modules: Map<String, ModuleProviders>,
    val allModules: ModuleProviders,

    val engine: Provider<CoverageEngine>,
    val classpath: Provider<FileCollection>,
    val koverExtension: Provider<KoverExtension>
)

internal class ModuleProviders(
    val reports: Provider<FileCollection>,
    val smap: Provider<FileCollection>,
    val tests: Provider<List<Test>>,
    val sources: Provider<FileCollection>,
    val output: Provider<FileCollection>
)

