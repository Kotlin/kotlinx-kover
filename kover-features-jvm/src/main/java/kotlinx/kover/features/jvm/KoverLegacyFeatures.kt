/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.features.jvm;

import com.intellij.rt.coverage.aggregate.api.AggregatorApi;
import com.intellij.rt.coverage.aggregate.api.Request;
import com.intellij.rt.coverage.instrument.api.OfflineInstrumentationApi;
import com.intellij.rt.coverage.report.api.ReportApi;
import com.intellij.rt.coverage.util.ErrorReporter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Kover Features for support Kover capabilities in Kover CLI via outdated API.
 */
public class KoverLegacyFeatures {

    private static final String FREE_MARKER_LOGGER_PROPERTY_NAME = "org.freemarker.loggerLibrary";

    /**
     * Generate modified class-files to measure the coverage.
     *
     * @param resultDir    Directory where the instrumented class-files will be placed
     * @param originalDirs Root directories where the original files are located, the coverage of which needs to be measured
     * @param filters      Filters to limit the classes that will be displayed in the report
     * @param countHits    Flag indicating whether to count the number of executions to each block of code. {@code false} if it is enough to register only the fact of at least one execution
     */
    public static void instrument(File resultDir,
                                  List<File> originalDirs,
                                  ClassFilters filters,
                                  boolean countHits
    ) {
        ArrayList<File> outputs = new ArrayList<>(originalDirs.size());
        for (int i = 0; i < originalDirs.size(); i++) {
            outputs.add(resultDir);
        }

        String previousConDySetting = ConDySettings.disableConDy();
        try {
            OfflineInstrumentationApi.instrument(originalDirs, outputs, Wildcards.convertFilters(filters), countHits);
        } finally {
            ConDySettings.restoreConDy(previousConDySetting);
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
    public static void generateXmlReport(
            File xmlFile,
            List<File> binaryReports,
            List<File> classfileDirs,
            List<File> sourceDirs,
            String title,
            ClassFilters filters
    ) throws IOException {
        ReportApi.xmlReport(xmlFile, title, binaryReports, classfileDirs, sourceDirs, Wildcards.convertFilters(filters));
    }

    /**
     * Generate Kover HTML report.
     *
     * @param htmlDir       Output directory with result HTML report
     * @param binaryReports List of coverage binary reports in IC format
     * @param classfileDirs List of root directories for compiled class-files
     * @param sourceDirs    List of root directories for Java and Kotlin source files
     * @param title         Title for header
     * @param filters       Filters to limit the classes that will be displayed in the report.
     * @throws IOException In case of a report generation error
     */
    public static void generateHtmlReport(
            File htmlDir,
            String charsetName,
            List<File> binaryReports,
            List<File> classfileDirs,
            List<File> sourceDirs,
            String title,
            ClassFilters filters
    ) throws IOException {
        // repeat reading freemarker temple from resources if error occurred, see https://github.com/Kotlin/kotlinx-kover/issues/510
        // the values are selected empirically so that the maximum report generation time is not much more than a second
        ReportApi.setFreemarkerRetry(7, 150);

        // print to stdout only critical errors
        ErrorReporter.setLogLevel(ErrorReporter.ERROR);

        // disable freemarker logging to stdout for the time of report generation
        String oldFreemarkerLogger = System.setProperty(FREE_MARKER_LOGGER_PROPERTY_NAME, "none");
        try {
            ReportApi.htmlReport(htmlDir, title, charsetName, binaryReports, classfileDirs, sourceDirs, Wildcards.convertFilters(filters));
        } finally {
            if (oldFreemarkerLogger == null) {
                System.clearProperty(FREE_MARKER_LOGGER_PROPERTY_NAME);
            } else {
                System.setProperty(FREE_MARKER_LOGGER_PROPERTY_NAME, oldFreemarkerLogger);
            }
        }
    }


    /**
     * Verify coverage by specified verification rules.
     *
     * @param tempDir       Directory to create temporary files
     * @param filters       Filters to limit the classes that will be verified
     * @param binaryReports List of coverage binary binaryReports in IC format
     * @param classfileDirs List of root directories for compiled class-files
     * @return List of rule violation errors, empty list if there is no verification errors.
     */
    public static List<RuleViolations> verify(List<KoverLegacyFeatures.Rule> rules, File tempDir, KoverLegacyFeatures.ClassFilters filters, List<File> binaryReports, List<File> classfileDirs) {
        try {
            return LegacyVerification.verify(rules, tempDir, filters, binaryReports, classfileDirs);
        } catch (IOException e) {
            throw new RuntimeException("Kover features exception occurred while verification", e);
        }
    }

    /**
     * Merge several IC binaryReports into one file.
     *
     * @param icFile        Target IC report file
     * @param filters       Filters to limit the classes that will be placed into result file
     * @param tempDir       Directory to create temporary files
     * @param binaryReports List of coverage binary binaryReports in IC format
     * @param classfileDirs List of root directories for compiled class-files
     */
    public static void aggregateIc(File icFile, KoverLegacyFeatures.ClassFilters filters, File tempDir, List<File> binaryReports, List<File> classfileDirs) {
        final File smapFile = new File(tempDir, "report.smap");

        Request request = new Request(Wildcards.convertFilters(filters), icFile, smapFile);
        AggregatorApi.aggregate(Collections.singletonList(request), binaryReports, classfileDirs);
    }

    /**
     * Get coverage values from binary reports.
     *
     * @param tempDir       Directory to create temporary files
     * @param filters       Filters to limit the classes that will be placed into result coverage
     * @param binaryReports List of coverage binary binaryReports in IC format
     * @param classfileDirs List of root directories for compiled class-files
     * @return List of coverage values.
     */
    public static List<CoverageValue> evalCoverage(GroupingBy groupBy, CoverageUnit coverageUnit, AggregationType aggregationForGroup, File tempDir, KoverLegacyFeatures.ClassFilters filters, List<File> binaryReports, List<File> classfileDirs) {
        Bound bound = new Bound(LegacyVerification.ONE_HUNDRED, BigDecimal.ZERO, coverageUnit, aggregationForGroup);
        Rule rule = new Rule("", groupBy, Collections.singletonList(bound));

        List<KoverLegacyFeatures.RuleViolations> violations = verify(Collections.singletonList(rule), tempDir, filters, binaryReports, classfileDirs);
        ArrayList<KoverLegacyFeatures.CoverageValue> result = new ArrayList<>();

        for (KoverLegacyFeatures.RuleViolations violation : violations) {
            for (KoverLegacyFeatures.BoundViolation boundViolation : violation.violations) {
                result.add(new KoverLegacyFeatures.CoverageValue(boundViolation.entityName, boundViolation.value));
            }
        }

        return result;
    }

    /**
     * Class filters.
     */
    public static class ClassFilters {
        /**
         * If specified, only the classes specified in this field are filtered.
         */
        public final Set<String> includeClasses;

        /**
         * The classes specified in this field are not filtered.
         */
        public final Set<String> excludeClasses;

        /**
         * Classes that have at least one of the annotations specified in this field are not filtered.
         */
        public final Set<String> excludeAnnotation;

        public ClassFilters(Set<String> includeClasses,
                            Set<String> excludeClasses,
                            Set<String> excludeAnnotation) {
            this.includeClasses = includeClasses;
            this.excludeClasses = excludeClasses;
            this.excludeAnnotation = excludeAnnotation;
        }
    }

    /**
     * Entity type for grouping code to coverage evaluation.
     */
    public enum GroupingBy {
        /**
         * Counts the coverage values for all code.
         */
        APPLICATION,
        /**
         * Counts the coverage values for each class separately.
         */
        CLASS,
        /**
         * Counts the coverage values for each package that has classes separately.
         */
        PACKAGE
    }

    /**
     * Type of the metric to evaluate code coverage.
     */
    public enum CoverageUnit {
        /**
         * Number of lines.
         */
        LINE,
        /**
         * Number of JVM bytecode instructions.
         */
        INSTRUCTION,
        /**
         * Number of branches covered.
         */
        BRANCH
    }

    /**
     * Type of counter value to compare with minimal and maximal values if them defined.
     */
    public enum AggregationType {
        COVERED_COUNT,
        MISSED_COUNT,
        COVERED_PERCENTAGE,
        MISSED_PERCENTAGE
    }

    public static class CoverageValue {
        public final String entityName;
        public final BigDecimal value;

        public CoverageValue(String entityName, BigDecimal value) {
            this.entityName = entityName;
            this.value = value;
        }
    }

    /**
     * Describes a single bound for the verification rule to enforce
     */
    public static class Bound {
        public final BigDecimal minValue;
        public final BigDecimal maxValue;

        public final CoverageUnit coverageUnits;
        public final AggregationType aggregationForGroup;

        public Bound(BigDecimal minValue, BigDecimal maxValue, CoverageUnit coverageUnits, AggregationType aggregationForGroup) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.coverageUnits = coverageUnits;
            this.aggregationForGroup = aggregationForGroup;
        }
    }

    /**
     * Verification rule - a named set of bounds of coverage value to check.
     */
    public static class Rule {
        public final String name;

        public final GroupingBy groupBy;

        public final List<Bound> bounds;

        public Rule(String name, GroupingBy groupBy, List<Bound> bounds) {
            this.name = name;
            this.groupBy = groupBy;
            this.bounds = bounds;
        }
    }

    /**
     * Violation of verification rule.
     */
    public static class RuleViolations {
        public final Rule rule;

        public final List<BoundViolation> violations;

        public RuleViolations(Rule rule, List<BoundViolation> violations) {
            this.rule = rule;
            this.violations = violations;
        }
    }

    /**
     * Violation of verification bound.
     */
    public static class BoundViolation {
        public final Bound bound;
        public final boolean isMax;
        public final BigDecimal value;
        public final String entityName;

        public BoundViolation(Bound bound, boolean isMax, BigDecimal value, String entityName) {
            this.bound = bound;
            this.isMax = isMax;
            this.value = value;
            this.entityName = entityName;
        }
    }

}
