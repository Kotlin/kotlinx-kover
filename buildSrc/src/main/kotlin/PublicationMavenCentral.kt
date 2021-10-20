/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.publish.*
import org.gradle.api.publish.maven.*
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.*

fun PublishingExtension.addMavenRepository(project: Project) {
    repositories {
        maven {
            url = project.uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.acquireProperty("libs.sonatype.user")
                password = project.acquireProperty("libs.sonatype.password")
            }
        }
    }
}

fun MavenPublication.signPublicationIfKeyPresent(project: Project) {
    val keyId = project.acquireProperty("libs.sign.key.id")
    val signingKey = project.acquireProperty("libs.sign.key.private")
    val signingKeyPassphrase = project.acquireProperty("libs.sign.passphrase")
    if (!signingKey.isNullOrBlank()) {
        project.extensions.configure<SigningExtension>("signing") {
            useInMemoryPgpKeys(keyId, signingKey, signingKeyPassphrase)
            sign(this@signPublicationIfKeyPresent)
        }
    }
}

fun PublishingExtension.addMavenMetadata() {
    publications.withType(MavenPublication::class) {
        pom {
            if (!name.isPresent) {
                name.set(artifactId)
            }
            description.set("Kotlin code coverage plugin")
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
                url.set("https://github.com/Kotlin/kotlinx-kover")
            }
        }
    }
}

fun MavenPublication.addExtraMavenArtifacts(project: Project, sources: SourceDirectorySet) {
    val sourcesJar by project.tasks.creating(org.gradle.jvm.tasks.Jar::class) {
        archiveClassifier.set("sources")
        from(sources)
    }
    val javadocJar by project.tasks.creating(org.gradle.jvm.tasks.Jar::class) {
        archiveClassifier.set("javadoc")
        // contents are deliberately left empty
    }
    artifact(sourcesJar)
    artifact(javadocJar)
}

private fun Project.acquireProperty(name: String): String? {
    return project.findProperty(name) as? String ?: System.getenv(name)
}
