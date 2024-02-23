/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.configurator

import kotlinx.kover.gradle.plugin.commons.KoverCriticalException
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.*
import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import java.io.File
import java.util.function.BiFunction


internal interface BuildConfigurator {
    fun addProjectWithKover(path: String = ":", name: String = path.substringAfterLast(":"), kotlinVersion: String? = null, generator: ProjectConfigurator.() -> Unit)

    fun addProject(path: String, name: String, generator: ProjectConfigurator.() -> Unit)

    fun run(vararg args: String, errorExpected: Boolean? = false, checker: CheckerContext.() -> Unit = {})

    fun edit(filePath: String, editor: (String) -> String)

    fun add(filePath: String, editor: () -> String)

    fun delete(filePath: String)

    fun useLocalCache(use: Boolean = true)

    fun prepare(): TestBuildConfig
}

internal interface ProjectConfigurator {
    fun plugins(block: PluginsConfigurator.() -> Unit)

    fun repositories(block: RepositoriesConfigurator.() -> Unit)

    fun kover(config: KoverExtension.(ProjectScope) -> Unit)

    fun sourcesFrom(template: String)

    fun dependencyKover(path: String)
}

internal interface PluginsConfigurator {
    fun kotlin(version: String? = null)

    fun kover(version: String? = null)
}

internal interface RepositoriesConfigurator {
    fun repository(name: String)
}

internal interface ProjectScope: Project {
    fun line(value: String)
}


internal abstract class BuilderConfiguratorWrapper(private val origin: BuildConfigurator) : BuildConfigurator {

    override fun addProjectWithKover(path: String, name: String, kotlinVersion: String?, generator: ProjectConfigurator.() -> Unit) {
        origin.addProjectWithKover(path, name, kotlinVersion, generator)
    }

    override fun addProject(path: String, name: String, generator: ProjectConfigurator.() -> Unit) {
        origin.addProject(path, name, generator)
    }

    override fun run(vararg args: String, errorExpected: Boolean?, checker: CheckerContext.() -> Unit) {
        origin.run(*args, errorExpected = errorExpected) { checker() }
    }

    override fun edit(filePath: String, editor: (String) -> String) {
        origin.edit(filePath, editor)
    }

    override fun add(filePath: String, editor: () -> String) {
        origin.add(filePath, editor)
    }

    override fun delete(filePath: String) {
        origin.delete(filePath)
    }

    override fun useLocalCache(use: Boolean) {
        origin.useLocalCache(use)
    }

    override fun prepare(): TestBuildConfig {
        return origin.prepare()
    }
}

internal fun ProjectConfigurator.fileInBuildDir(path: String): RegularFileProperty {
    return FilePropertyFromBuildDir(path)
}


private class FilePropertyFromBuildDir(private val relativePath: String): RegularFileProperty {
    override fun toString(): String {
        return "layout.buildDirectory.file(\"$relativePath\")"
    }

    override fun get(): RegularFile {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun getOrNull(): RegularFile? {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun getOrElse(defaultValue: RegularFile): RegularFile {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun <S : Any?> map(transformer: Transformer<out S?, in RegularFile>): Provider<S> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun <S : Any?> flatMap(transformer: Transformer<out Provider<out S>?, in RegularFile>): Provider<S> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun isPresent(): Boolean {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun orElse(value: RegularFile): Provider<RegularFile> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun orElse(provider: Provider<out RegularFile>): Provider<RegularFile> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun forUseAtConfigurationTime(): Provider<RegularFile> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun <U : Any?, R : Any?> zip(
        right: Provider<U>,
        combiner: BiFunction<in RegularFile, in U, out R?>
    ): Provider<R> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun finalizeValue() {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun finalizeValueOnRead() {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun disallowChanges() {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun disallowUnsafeRead() {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun set(file: File?) {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun set(value: RegularFile?) {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun set(provider: Provider<out RegularFile>) {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun value(value: RegularFile?): RegularFileProperty {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun value(provider: Provider<out RegularFile>): RegularFileProperty {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun convention(value: RegularFile?): RegularFileProperty {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun convention(provider: Provider<out RegularFile>): RegularFileProperty {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun getAsFile(): Provider<File> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun fileValue(file: File?): RegularFileProperty {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun fileProvider(provider: Provider<File>): RegularFileProperty {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun getLocationOnly(): Provider<RegularFile> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

}
