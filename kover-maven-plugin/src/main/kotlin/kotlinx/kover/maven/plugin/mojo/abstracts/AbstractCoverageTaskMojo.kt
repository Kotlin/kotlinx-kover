/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.mojo.abstracts

import kotlinx.kover.features.jvm.ClassFilters
import kotlinx.kover.features.jvm.KoverFeatures
import kotlinx.kover.maven.plugin.Constants
import kotlinx.kover.maven.plugin.MavenReportFilters
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.apache.maven.artifact.versioning.VersionRange
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.util.xml.Xpp3Dom
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path

/**
 * Common class using for processing project coverage.
 */
abstract class AbstractCoverageTaskMojo : AbstractKoverMojo() {
    /**
     * Filters to limit the code that gets into the report.
     */
    @Parameter
    private var filters: MavenReportFilters? = null

    /**
     * Flag to use code coverage from dependencies.
     *
     * `false` by default.
     */
    @Parameter(property = "kover.aggregate", defaultValue = "false")
    protected var aggregate: Boolean = false

    /**
     * Binary reports that built in advance, before the start of the project build.
     *
     * It's convenient to use when tests are performed on CI on different nodes.
     */
    @Parameter
    private val additionalBinaryReports: MutableList<File> = mutableListOf()

    /**
     * Project modules involved in the build.
     */
    @Parameter(property = "reactorProjects", readonly = true)
    private lateinit var reactorProjects: List<MavenProject>

    protected abstract fun processCoverage(
        binaryReports: List<File>,
        outputDirs: List<File>,
        sourceDirs: List<File>,
        filters: ClassFilters
    )


    final override fun doExecute() {
        val merged = collectVariants()
        val allBinaryReports = merged.binaryReports + additionalBinaryReports
        processCoverage(allBinaryReports, merged.outputs, merged.sources, koverFilters())
    }

    /**
     * Create temporary dir on each call.
     */
    protected fun tempDirectory(): File {
        val commonTmpDir = File(project.build.directory).resolve(Constants.TMP_DIR_NAME)
        commonTmpDir.mkdirs()
        return Files.createTempDirectory(Path(commonTmpDir.path), "kover")
            .toFile()
    }

    /**
     * Get filters as Kover filters classes.
     */
    private fun koverFilters(): ClassFilters {
        return filters?.let { nnFilters ->
            ClassFilters(
                nnFilters.includes.classes.toSet(),
                nnFilters.excludes.classes.toSet(),
                nnFilters.includes.annotatedBy.toSet(),
                nnFilters.excludes.annotatedBy.toSet(),
                nnFilters.includes.inheritedFrom.toSet(),
                nnFilters.excludes.inheritedFrom.toSet()
            )
        } ?: ClassFilters(emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet())
    }

    /**
     * Collect directory and file information from current project and all its dependencies.
     */
    private fun collectVariants(): KoverArtifact {
        val currentArtifact = project.extractArtifact(false)

        val artifacts = if (aggregate) {
            val dependencyArtifacts =
                project.getDependencyProjects().map { pair -> pair.first.extractArtifact(pair.second) }
            dependencyArtifacts + currentArtifact
        } else {
            listOf(currentArtifact)
        }

        val binaryReports = mutableSetOf<File>()
        val sources = mutableSetOf<File>()
        val outputs = mutableSetOf<File>()

        artifacts
            .map { it.filterProjectSources() }
            .forEach { artifact ->
                binaryReports += artifact.binaryReports.filter { it.exists() }
                sources += artifact.sources.filter { it.exists() && it.isDirectory }
                outputs += artifact.outputs.filter { it.exists() && it.isDirectory }
            }

        return KoverArtifact("<aggregated>", binaryReports.toList(), sources.toList(), outputs.toList())
    }

    /**
     * Filters projects sources according project filters.
     */
    private fun KoverArtifact.filterProjectSources(): KoverArtifact {
        val nnFilters = filters ?: return this

        if (nnFilters.includes.projects.isNotEmpty()) {
            val notIncluded = nnFilters.includes.projects.none { filter ->
                KoverFeatures.koverWildcardToRegex(filter).toRegex().matches(projectName)
            }
            if (notIncluded) {
                return KoverArtifact(projectName, binaryReports, emptyList(), emptyList())
            }
        }
        if (nnFilters.excludes.projects.isNotEmpty()) {
            val excluded = nnFilters.excludes.projects.any { filter ->
                KoverFeatures.koverWildcardToRegex(filter).toRegex().matches(projectName)
            }
            if (excluded) {
                return KoverArtifact(projectName, binaryReports, emptyList(), emptyList())
            }
        }
        return this
    }


    /**
     * Find all dependencies of current project, which are modules.
     *
     * If there are several modules in the project that pass the dependency version condition, then only the very first one will be taken.
     *
     * @return list of pairs: dependency project and sign that it was a test dependency.
     */
    private fun MavenProject.getDependencyProjects(): List<Pair<MavenProject, Boolean>> {
        return dependencies
            .filter { it.scope in Constants.DEPENDENCY_SCOPES }
            .mapNotNull { dependency ->
                val versionRange = VersionRange.createFromVersionSpec(dependency.version)

                // take first project, satisfying the condition in dependency
                val projectDependency = reactorProjects.firstOrNull { fromReactor ->
                    fromReactor.groupId == dependency.groupId
                            && fromReactor.artifactId == dependency.artifactId
                            && versionRange.containsVersion(DefaultArtifactVersion(fromReactor.version))
                }

                if (projectDependency != null) {
                    projectDependency to (dependency.scope == Constants.TEST_SCOPE)
                } else {
                    null
                }
            }
    }

    /**
     * Find all binary reports, source directory roots and output directory roots in given project.
     */
    private fun MavenProject.extractArtifact(testProject: Boolean): KoverArtifact {
        // binary report name is fixed
        val binaryReports = listOf(File(build.directory).resolve(Constants.BIN_REPORT_PATH))

        val sources: List<File>
        val outputs: List<File>

        if (testProject) {
            // skip classes of test dependency
            sources = emptyList()
            outputs = emptyList()
        } else {
            sources = findSourceDirs()
            outputs = listOf(File(build.outputDirectory))
        }

        return KoverArtifact(name, binaryReports, sources, outputs)
    }

    /**
     * Collect all Kotlin source roots.
     */
    private fun MavenProject.findSourceDirs(): List<File> {
        // common source roots
        val standardDirs = compileSourceRoots.map { sourcePath -> toAbsoluteFile(sourcePath) }

        val kotlinPlugin =
            project.buildPlugins.firstOrNull { it.groupId == "org.jetbrains.kotlin" && it.artifactId == "kotlin-maven-plugin" }
                ?: return standardDirs

        /*
        Processing Kotlin plugin configuration:
                     <execution>
                        <id>compile-kotlin</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                        <configuration>
                            <sourceDirs>
                                <sourceDir>some/dir</sourceDir>
                            </sourceDirs>
                        </configuration>
                    </execution>
         */
        var kotlinDirs: List<File>? = null
        try {
            kotlinDirs = kotlinPlugin.executions
                .filter { "compile" in it.goals }
                .filter { execution -> execution.configuration != null && execution.configuration is Xpp3Dom }
                .flatMap { execution ->
                    val config = execution.configuration as Xpp3Dom
                    val sourceDirs = config.getChild("sourceDirs") ?: return@flatMap emptyList()
                    sourceDirs.children.map { toAbsoluteFile(it.value) }
                }
        } catch (e: Exception) {
            // in future versions configuration may be changed
            log.warn("Error when trying to read the Kotlin configuration", e)
        }

        return if (kotlinDirs == null) standardDirs else (standardDirs + kotlinDirs)
    }

    private fun MavenProject.toAbsoluteFile(path: String): File {
        val file = File(path)
        return if (file.isAbsolute) {
            file
        } else {
            basedir.resolve(path)
        }
    }

    /**
     * Information about project's files and directories, used to process coverage.
     */
    class KoverArtifact(
        val projectName: String,
        val binaryReports: List<File>,
        val sources: List<File>,
        val outputs: List<File>
    )
}