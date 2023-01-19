/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl


const val DEPENDENCY_CONFIGURATION_NAME = "kover"

const val PROJECT_SETUP_EXTENSION = "kover"


public interface KoverClassDefinitions {
    public fun className(vararg name: String)
    public fun className(names: Iterable<String>)

    public fun packageName(vararg name: String)
    public fun packageName(names: Iterable<String>)
}

public interface KoverTaskDefinitions {
    public fun taskName(vararg name: String)
    public fun taskName(names: Iterable<String>)
}

public interface KoverKmpCompilationDefinitions {
    public fun kmpTargetName(vararg name: String)
    public fun kmpCompilation(targetName: String, compilationName: String)
    public fun kmpCompilation(compilationName: String)
}
