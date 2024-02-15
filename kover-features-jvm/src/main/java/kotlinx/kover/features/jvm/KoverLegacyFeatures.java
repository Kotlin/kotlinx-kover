package kotlinx.kover.features.jvm;

import com.intellij.rt.coverage.instrument.api.OfflineInstrumentationApi;
import com.intellij.rt.coverage.report.api.Filters;
import com.intellij.rt.coverage.report.api.ReportApi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Kover Features for support Kover capabilities in Kover CLI via outdated API.
 */
public class KoverLegacyFeatures {

    /**
     * Generate modified class-files to measure the coverage.
     *
     * @param resultDir Directory where the instrumented class-files will be placed
     * @param originalDirs Root directories where the original files are located, the coverage of which needs to be measured
     * @param filters Filters to limit the classes that will be displayed in the report
     * @param countHits Flag indicating whether to count the number of executions to each block of code. {@code false} if it is enough to register only the fact of at least one execution
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
            OfflineInstrumentationApi.instrument(originalDirs, outputs, convertFilters(filters), countHits);
        } finally {
            ConDySettings.restoreConDy(previousConDySetting);
        }
    }

    /**
     * Generate Kover XML report, compatible with JaCoCo XML.
     *
     * @param xmlFile path to the generated XML report
     * @param binaryReports list of coverage binary reports in IC format
     * @param classfileDirs list of root directories for compiled class-files
     * @param sourceDirs list of root directories for Java and Kotlin source files
     * @param title Title for header
     * @param filters Filters to limit the classes that will be displayed in the report
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
        ReportApi.xmlReport(xmlFile, title, binaryReports, classfileDirs, sourceDirs, convertFilters(filters));
    }

    /**
     * Generate Kover HTML report.
     *
     * @param htmlDir output directory with result HTML report
     * @param binaryReports list of coverage binary reports in IC format
     * @param classfileDirs list of root directories for compiled class-files
     * @param sourceDirs list of root directories for Java and Kotlin source files
     * @param title Title for header
     * @param filters Filters to limit the classes that will be displayed in the report.
     * @throws IOException In case of a report generation error
     */
    public static void generateHtmlReport(
            File htmlDir,
            List<File> binaryReports,
            List<File> classfileDirs,
            List<File> sourceDirs,
            String title,
            ClassFilters filters
    ) throws IOException {
        ReportApi.htmlReport(htmlDir, title, null, binaryReports, classfileDirs, sourceDirs, convertFilters(filters));
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

    private static Filters convertFilters(ClassFilters filters) {
        return new Filters(
                convert(filters.includeClasses),
                convert(filters.excludeClasses),
                convert(filters.excludeAnnotation)
        );
    }

    private static List<Pattern> convert(Set<String> regexes) {
        ArrayList<Pattern> patterns = new ArrayList<>(regexes.size());
        for (String regex : regexes) {
            patterns.add(Pattern.compile(regex));
        }
        return patterns;
    }


}
