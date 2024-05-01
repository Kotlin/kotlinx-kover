/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.features.jvm

import com.intellij.rt.coverage.aggregate.api.AggregatorApi
import com.intellij.rt.coverage.aggregate.api.Request
import com.intellij.rt.coverage.instrument.api.OfflineInstrumentationApi
import com.intellij.rt.coverage.report.api.ReportApi
import com.intellij.rt.coverage.util.ErrorReporter
import kotlinx.kover.features.jvm.impl.ConDySettings
import kotlinx.kover.features.jvm.impl.LegacyVerification
import kotlinx.kover.features.jvm.impl.convert
import java.io.File
import java.io.IOException
import java.math.BigDecimal

/**
 * Kover Features for support Kover capabilities in Kover CLI via outdated API.
 */
public object KoverLegacyFeatures {
    private const val FREE_MARKER_LOGGER_PROPERTY_NAME = "org.freemarker.loggerLibrary"

    /**
     * Generate modified class-files to measure the coverage.
     *
     * @param resultDir    Directory where the instrumented class-files will be placed
     * @param originalDirs Root directories where the original files are located, the coverage of which needs to be measured
     * @param filters      Filters to limit the classes that will be displayed in the report
     * @param countHits    Flag indicating whether to count the number of executions to each block of code. `false` if it is enough to register only the fact of at least one execution
     */
    public fun instrument(
        resultDir: File, originalDirs: List<File?>, filters: ClassFilters, countHits: Boolean
    ) {
        val outputs = ArrayList<File>(originalDirs.size)
        for (i in originalDirs.indices) {
            outputs.add(resultDir)
        }

        val previousConDySetting = ConDySettings.disableConDy()
        try {
            OfflineInstrumentationApi.instrument(originalDirs, outputs, filters.convert(), countHits)
        } finally {
            ConDySettings.restoreConDy(previousConDySetting)
        }
    }

    /**
     * Generate Kover XML report, compatible with JaCoCo XML.
     *
     * @param xmlFile       Path to the generated XML report
     * @param binaryReports List of coverage binary reports in IC format
     * @param classfileDirs List of root directories for compiled class-files
     * @param sourceDirs    List of root directories for Java and Kotlin source files
     * @param title         Title for header
     * @param filters       Filters to limit the classes that will be displayed in the report
     * @throws IOException In case of a report generation error
     */
    @Throws(IOException::class)
    public fun generateXmlReport(
        xmlFile: File,
        binaryReports: List<File>,
        classfileDirs: List<File>,
        sourceDirs: List<File>,
        title: String,
        filters: ClassFilters
    ) {
        ReportApi.xmlReport(xmlFile, title, binaryReports, classfileDirs, sourceDirs, filters.convert())
    }

    /**
     * Generate Kover HTML report.
     *
     * @param htmlDir       Output directory with result HTML report
     * @param charsetName   Name of charset used in HTML report
     * @param binaryReports List of coverage binary reports in IC format
     * @param classfileDirs List of root directories for compiled class-files
     * @param sourceDirs    List of root directories for Java and Kotlin source files
     * @param title         Title for header
     * @param filters       Filters to limit the classes that will be displayed in the report.
     * @throws IOException In case of a report generation error
     */
    @Throws(IOException::class)
    public fun generateHtmlReport(
        htmlDir: File,
        charsetName: String?,
        binaryReports: List<File>,
        classfileDirs: List<File>,
        sourceDirs: List<File>,
        title: String,
        filters: ClassFilters
    ) {
        // print to stdout only critical errors
        ErrorReporter.setLogLevel(ErrorReporter.ERROR)

        // disable freemarker logging to stdout for the time of report generation
        val oldFreemarkerLogger = System.setProperty(FREE_MARKER_LOGGER_PROPERTY_NAME, "none")
        try {
            ReportApi.htmlReport(
                htmlDir, title, charsetName, binaryReports, classfileDirs, sourceDirs, filters.convert()
            )
        } finally {
            if (oldFreemarkerLogger == null) {
                System.clearProperty(FREE_MARKER_LOGGER_PROPERTY_NAME)
            } else {
                System.setProperty(FREE_MARKER_LOGGER_PROPERTY_NAME, oldFreemarkerLogger)
            }
        }
    }


    /**
     * Verify coverage by specified verification rules.
     *
     * @param rules         List of the verification rules to check
     * @param tempDir       Directory to create temporary files
     * @param filters       Filters to limit the classes that will be verified
     * @param binaryReports List of coverage binary reports in IC format
     * @param classfileDirs List of root directories for compiled class-files
     * @return List of rule violation errors, empty list if there is no verification errors.
     */
    public fun verify(
        rules: List<Rule>, tempDir: File, filters: ClassFilters, binaryReports: List<File>, classfileDirs: List<File>
    ): List<RuleViolations> {
        try {
            return LegacyVerification.verify(rules, tempDir, filters, binaryReports, classfileDirs)
        } catch (e: IOException) {
            throw RuntimeException("Kover features exception occurred while verification", e)
        }
    }

    /**
     * Merge several IC binaryReports into one file.
     *
     * @param icFile        Target IC report file
     * @param filters       Filters to limit the classes that will be placed into result file
     * @param tempDir       Directory to create temporary files
     * @param binaryReports List of coverage binary reports in IC format
     * @param classfileDirs List of root directories for compiled class-files
     */
    public fun aggregateIc(
        icFile: File, filters: ClassFilters, tempDir: File, binaryReports: List<File>, classfileDirs: List<File>
    ) {
        val smapFile = tempDir.resolve("report.smap")

        val request = Request(filters.convert(), icFile, smapFile)
        AggregatorApi.aggregate(listOf(request), binaryReports, classfileDirs)
    }

    /**
     * Get coverage values from binary reports.
     *
     * @param groupBy             Code unit for which coverage will be aggregated
     * @param coverageUnit        Specify which units to measure coverage for (line, branch, etc.)
     * @param aggregationForGroup Aggregation function that will be calculated over all the elements of the same group
     * @param tempDir             Directory to create temporary files
     * @param filters             Filters to limit the classes that will be placed into result coverage
     * @param binaryReports       List of coverage binary reports in IC format
     * @param classfileDirs       List of root directories for compiled class-files
     * @return List of coverage values.
     */
    public fun evalCoverage(
        groupBy: GroupingBy,
        coverageUnit: CoverageUnit,
        aggregationForGroup: AggregationType,
        tempDir: File,
        filters: ClassFilters,
        binaryReports: List<File>,
        classfileDirs: List<File>
    ): List<CoverageValue> {
        val bound = Bound(LegacyVerification.ONE_HUNDRED, BigDecimal.ZERO, coverageUnit, aggregationForGroup)
        val rule = Rule("", groupBy, listOf(bound))

        val violations = verify(listOf(rule), tempDir, filters, binaryReports, classfileDirs)
        val result = ArrayList<CoverageValue>()

        for (violation in violations) {
            for (boundViolation in violation.violations) {
                result.add(CoverageValue(boundViolation.entityName, boundViolation.value))
            }
        }

        return result
    }
}

/**
 * Class filters.
 */
public data class ClassFilters(
    /**
     * If specified, only the classes specified in this field are filtered.
     */
    public val includeClasses: Set<String>,
    /**
     * The classes specified in this field are not filtered.
     */
    public val excludeClasses: Set<String>,
    /**
     * Classes that have at least one of the annotations specified in this field are presents in the report.
     * All other classes that are not marked with at least one of the specified annotations are not included in the report.
     *
     *
     * If inclusion and exclusion rules are specified at the same time, then excludes have priority over includes.
     * This means that even if a class is annotated both annotations from exclude and include, it will be excluded from the report.
     *
     */
    public val includeAnnotation: Set<String>,
    /**
     * Classes that have at least one of the annotations specified in this field are not filtered.
     *
     *
     * If inclusion and exclusion rules are specified at the same time, then excludes have priority over includes.
     * This means that even if a class is annotated both annotations from exclude and include, it will be excluded from the report.
     *
     */
    public val excludeAnnotation: Set<String>,
    /**
     *
     * Include only classes extending at least one of the specified classes or implementing at least one of the interfaces.
     *
     *
     * The entire inheritance tree is analyzed, that is, a class may not extending the specified class directly.
     * Similarly, for the specified interfaces, it is checked that they are implemented directly in the class, or in one of its heirs.
     *
     *
     * The following classes and interfaces can be specified in arguments:
     *
     *  *  classes and interfaces declared in the application
     *  *  classes and interfaces declared outside the application, if they are directly inherited or implemented by any type from the application
     *
     *
     * If specified class or interface that is not declared in the application and that is not extending/implemented directly by one of the application types, then such a filter will have no effect.
     */
    public val includeInheritedFrom: Set<String>,
    /**
     *
     * Exclude classes extending at least one of the specified classes or implementing at least one of the interfaces.
     *
     *
     * The entire inheritance tree is analyzed, that is, a class may not extend the specified class directly.
     * Similarly, for the specified interfaces, it is checked that they are implemented directly in the class, or in one of its heirs.
     *
     *
     * The following classes and interfaces can be specified in arguments:
     *
     *  *  classes and interfaces declared in the application
     *  *  classes and interfaces declared outside the application, however they are directly inherited or implemented by any type from the application
     *
     *
     * If specified class or interface that is not declared in the application and that is not extended/implemented directly by one of the application types, then such a filter will have no effect.
     */
    public val excludeInheritedFrom: Set<String>
)
