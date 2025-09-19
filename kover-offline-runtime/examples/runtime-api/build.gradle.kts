plugins {
    kotlin("jvm") version ("2.2.20")
}

group = "org.jetbrains"

repositories {
    mavenCentral()
}

configurations.register("koverCli") {
    isVisible = false
    isCanBeConsumed = false
    isTransitive = true
    isCanBeResolved = true
}

dependencies {
    add("koverCli", "org.jetbrains.kotlinx:kover-cli:0.9.2")

    implementation("org.jetbrains.kotlinx:kover-offline-runtime:0.9.2")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()

    systemProperty("output.dir", tasks.compileKotlin.get().destinationDirectory.get().asFile.absolutePath)
}

kotlin {
    jvmToolchain(8)
}

fun cliJar(): File {
    val cliConfig = configurations.getByName("koverCli")
    return cliConfig.filter {it.name.startsWith("kover-cli")}.singleFile
}

tasks.compileKotlin {
    doLast {
        val outputDir = destinationDirectory.get().asFile

        exec {
            commandLine(
                "java",
                "-jar",
                cliJar().canonicalPath,
                "instrument",
                outputDir,
                "--dest",
                outputDir,
                "--hits",
            )
        }
    }
}
