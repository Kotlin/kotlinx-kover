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
    `java-base`
    `maven-publish`
    signing
}

// automatically generate sources and javadoc artifacts along with the main library jar
java {
    withJavadocJar()
    withSourcesJar()
}

interface KoverPublicationExtension {
    val description: Property<String>
    val addPublication: Property<Boolean>
    val localRepository: DirectoryProperty
}

val extension = extensions.create<KoverPublicationExtension>("koverPublication")

extension.description.convention("")
extension.addPublication.convention(true)
extension.localRepository.convention(layout.buildDirectory.dir(".m2"))

internal interface LocalArtifactAttr : Named {
    companion object {
        val ATTRIBUTE = Attribute.of(
            "kotlinx.kover.gradle-plugin",
            LocalArtifactAttr::class.java
        )
    }
}

val publicationTask: TaskCollection<*> = tasks.matching { task -> task.name == "publishAllPublicationsToLocalRepository" }
configurations.register("localPublication") {
    isVisible = false
    isCanBeResolved = false
    // this configuration produces modules that can be consumed by other projects
    isCanBeConsumed = true
    attributes {
        attribute(LocalArtifactAttr.ATTRIBUTE, objects.named("local-repository"))
    }

    outgoing.artifact(extension.localRepository) {
        builtBy(publicationTask)
    }
}

val snapshotRelease = configurations.create("snapshotRelease") {
    isVisible = true
    isCanBeResolved = false
    isCanBeConsumed = false
}

val externalSnapshots = configurations.create("snapshots") {
    isVisible = false
    isCanBeResolved = true
    // this config consumes modules from OTHER projects, and cannot be consumed by other projects
    isCanBeConsumed = false

    attributes {
        attribute(LocalArtifactAttr.ATTRIBUTE, objects.named("local-repository"))
    }

    extendsFrom(snapshotRelease)
}

tasks.register<CollectTask>("collectRepository") {
    dependsOn(publicationTask)
    dependsOn(externalSnapshots)

    local.convention(extension.localRepository)
    externals.from(externalSnapshots)
}

@CacheableTask
abstract class CollectTask: DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val local: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val externals: ConfigurableFileCollection

    @get:OutputDirectories
    val repositories: MutableList<File> = mutableListOf()

    @TaskAction
    fun doCollect() {
        val localFile = local.get().asFile
        repositories += localFile
        repositories += externals.toList()
    }
}


publishing {
    repositories {
        addSonatypeRepository()

        /**
         * Maven repository in build directory to store artifacts for using in functional tests.
         */
        maven {
            setUrl(extension.localRepository)
            name = "local"
        }
    }

    publications.withType<MavenPublication>().configureEach {
        addMetadata()
        signPublicationIfKeyPresent()
    }
    tasks.withType<PublishToMavenRepository>().configureEach {
        dependsOn(tasks.withType<Sign>())
    }
}

afterEvaluate {
    if (extension.addPublication.get()) {
        publishing.publications.register<MavenPublication>("Kover") {
            // add jar with module
            from(components["java"])
        }
    }
}

fun RepositoryHandler.addSonatypeRepository() {
    maven {
        url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        credentials {
            username = acquireProperty("libs.sonatype.user")
            password = acquireProperty("libs.sonatype.password")
        }
    }
}

fun MavenPublication.signPublicationIfKeyPresent() {
    val keyId = acquireProperty("libs.sign.key.id")
    val signingKey = acquireProperty("libs.sign.key.private")
    val signingKeyPassphrase = acquireProperty("libs.sign.passphrase")
    if (!signingKey.isNullOrBlank()) {
        extensions.configure<SigningExtension>("signing") {
            useInMemoryPgpKeys(keyId, signingKey, signingKeyPassphrase)
            sign(this@signPublicationIfKeyPresent)
        }
    }
}


fun MavenPublication.addMetadata() {
    pom {
        if (!name.isPresent) {
            name.set(artifactId)
        }
        if (hasProperty("releaseVersion")) {
            version = property("releaseVersion") as String
        }
        groupId = "org.jetbrains.kotlinx"
        description.set(extension.description)

        url.set("https://github.com/Kotlin/kotlinx-kover")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("JetBrains")
                name.set("JetBrains Team")
                organization.set("JetBrains")
                organizationUrl.set("https://www.jetbrains.com")
            }
        }
        scm {
            connection.set("scm:git:git@github.com:Kotlin/kotlinx-kover.git")
            developerConnection.set("scm:git:git@github.com:Kotlin/kotlinx-kover.git")
            url.set("https://github.com/Kotlin/kotlinx-kover")
        }
    }
}

fun Project.acquireProperty(name: String): String? {
    return project.findProperty(name) as? String ?: System.getenv(name)
}

val Project.sourceSets: SourceSetContainer
    get() =
        (this as ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer

val SourceSetContainer.main: NamedDomainObjectProvider<SourceSet>
    get() = named<SourceSet>("main")

signing {
    // disable signing if private key isn't passed
    isRequired = findProperty("libs.sign.key.private") != null
}
