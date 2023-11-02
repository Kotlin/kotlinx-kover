package org.jetbrains.kotlinx.kover

import kotlinx.kover.offline.runtime.api.KoverRuntime
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class Tests {
    @Test
    fun test() {
        MainClass().readState()

        val reportFile = Files.createTempFile("kover-report-", ".ic").toFile()

        // save binary report to file
        KoverRuntime.saveReport(reportFile.canonicalPath)

        // get binary report as byte array
        val bytes = KoverRuntime.getReport()

        // check reports are same
        val bytesFromFile = reportFile.readBytes()
        assertContentEquals(bytesFromFile, bytes)


        val outputDir = File(System.getProperty("output.dir"))
        val coverage = KoverRuntime.collectByDirs(listOf(outputDir))

        // check coverage of `readState` method
        assertEquals(3, coverage.size)
        val coverageByClass = coverage.associateBy { cov -> cov.className }

        val mainClassCoverage = coverageByClass.getValue("org.jetbrains.kotlinx.kover.MainClass")
        assertEquals("Main.kt", mainClassCoverage.fileName)
        assertEquals(4, mainClassCoverage.methods.size)

        val coverageBySignature = mainClassCoverage.methods.associateBy { meth -> meth.signature }
        val readStateCoverage = coverageBySignature.getValue("readState()Lorg/jetbrains/kotlinx/kover/DataClass;")

        assertEquals(1, readStateCoverage.hit)
        assertEquals(1, readStateCoverage.lines.size)
        assertEquals(6, readStateCoverage.lines[0].lineNumber)
        assertEquals(1, readStateCoverage.lines[0].hit)
        assertEquals(0, readStateCoverage.lines[0].branches.size)
    }
}