import org.gradle.kotlin.dsl.base
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.java

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

plugins {
    java
}

val fatJarDependency = "fatJar"
val fatJarConfiguration = configurations.create(fatJarDependency)


tasks.jar {
    from(
        fatJarConfiguration.map { if (it.isDirectory) it else zipTree(it) }
    ) {
        exclude("OSGI-OPT/**")
        exclude("META-INF/**")
        exclude("LICENSE")
        exclude("classpath.index")
    }
}