/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.offline.runtime.api;

import com.intellij.rt.coverage.offline.api.CoverageRuntime;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Utility Kover to collect coverage inside JVM process in which the tests were run.
 */ 
public class KoverRuntime {

    /**
     * Default name of file with Kover offline logs.
     *
     * Can be overridden using the {@link KoverRuntime#LOG_FILE_PROPERTY_NAME} property.
     */
    public static final String DEFAULT_LOG_FILE_NAME = "kover-offline.log";

    /**
     * JVM property name used to define the path where the offline report will be stored.
     * <p>
     * If this property is specified, then at the end of the JVM process,
     * the binary coverage report will be saved to a file at the path passed in the parameter value.
     * <p>
     *  If the file does not exist, it will be created. If a file with that name already exists, it will be overwritten.
     */
    public static final String REPORT_PROPERTY_NAME = "kover.offline.report.path";

    /**
     * JVM property name used to define the path to the file with Kover offline logs.
     *
     *<p>
     * If this property is not specified, the logs are saved to the {@link KoverRuntime#DEFAULT_LOG_FILE_NAME} file located in the current directory.
     */
    public static final String LOG_FILE_PROPERTY_NAME = "kover.offline.log.file.path";

    /**
     * Get classes coverage. For the correct collection of coverage, an analysis of the class-files is required.
     * <p/>
     * Calling this method is allowed only after all tests are completed. If the method is called in parallel with the execution of the measured code, the coverage value is unpredictable.
     *
     * @param classFileRoots root directories containing non-instrumented class-files the coverage of which
     *                       needs to be measured.
     *                       The search for class-files is recursive.
     * @return Coverage of classes that were present in the directories passed in the <code>classFileRoots</code> parameter.
     */
    public static List<ClassCoverage> collectByDirs(List<File> classFileRoots) {
        return convertClasses(CoverageRuntime.collectInRoots(classFileRoots));
    }

    /**
     * Get classes coverage. For the correct collecting of coverage, an analysis of the class-files is required.
     * <p/>
     * Calling this method is allowed only after all tests are completed. If the method is called in parallel with the execution of the measured code, the coverage value is unpredictable.
     *
     * @param classFiles a bytecode of non-instrumented application classes the coverage of which needs to be measured.
     * @return Coverage of classes that were present in the <code>classFiles</code> parameter.
     */
    public static List<ClassCoverage> collect(List<byte[]> classFiles) {
        return convertClasses(CoverageRuntime.collectClassfileData(classFiles));
    }

    /**
     * Save coverage binary report in file with ic format.
     * If the file does not exist, it will be created. If a file already exists, it will be overwritten.
     * <p/>
     * Calling this method is allowed only after all tests are completed. If the method is called in parallel with the execution of the measured code, the coverage value is unpredictable.
     *
     *
     * @param file the file to save binary report
     * @throws IOException in case of any error working with files
     */
    public static void saveReport(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file)); DataOutputStream outputStream = new DataOutputStream(out)) {
            CoverageRuntime.dumpIcReport(outputStream);
        }
    }

    /**
     * Get content of the coverage binary report with ic format.
     * The resulting byte array can be directly saved to an ic file for working with the CLI
     * <p/>
     * Calling this method is allowed only after all tests are completed. If the method is called in parallel with the execution of the measured code, the coverage value is unpredictable.
     *
     *
     * @return byte array with binary report in ic format
     * @throws IOException in case of any error working with files
     */
    public static byte[] getReport() throws IOException {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        try (DataOutputStream outputStream = new DataOutputStream(byteArrayStream)) {
            CoverageRuntime.dumpIcReport(outputStream);
        }
        return byteArrayStream.toByteArray();
    }

    private static List<ClassCoverage> convertClasses(List<com.intellij.rt.coverage.offline.api.ClassCoverage> origins) {
        ArrayList<ClassCoverage> result = new ArrayList<>(origins.size());
        for (com.intellij.rt.coverage.offline.api.ClassCoverage classCoverage : origins) {
            result.add(convertClass(classCoverage));
        }

        return result;
    }

    private static ClassCoverage convertClass(com.intellij.rt.coverage.offline.api.ClassCoverage origin) {
        List<com.intellij.rt.coverage.offline.api.MethodCoverage> originMethods = origin.methods;
        ArrayList<MethodCoverage> methods = new ArrayList<>(originMethods.size());
        for (com.intellij.rt.coverage.offline.api.MethodCoverage methodCoverage: originMethods) {
            methods.add(convertMethod(methodCoverage));
        }

        ClassCoverage result = new ClassCoverage();
        result.className = origin.className;
        result.fileName = origin.fileName;
        result.methods = methods;
        return result;
    }

    private static MethodCoverage convertMethod(com.intellij.rt.coverage.offline.api.MethodCoverage origin) {
        List<com.intellij.rt.coverage.offline.api.LineCoverage> originLines = origin.lines;
        ArrayList<LineCoverage> lines = new ArrayList<>();
        for (com.intellij.rt.coverage.offline.api.LineCoverage lineCoverage : originLines) {
            lines.add(convertLine(lineCoverage));
        }

        MethodCoverage result = new MethodCoverage();
        result.signature = origin.signature;
        result.hit = origin.hits;
        result.lines = lines;
        return result;
    }

    private static LineCoverage convertLine(com.intellij.rt.coverage.offline.api.LineCoverage origin) {
        List<Integer> branchHits = origin.branchHits;
        ArrayList<BranchCoverage> branches = new ArrayList<>(branchHits.size());
        for (int i = 0; i < branchHits.size(); i++) {
            BranchCoverage branch = new BranchCoverage();
            branch.branchNumber = i;
            branch.hit = branchHits.get(i);

            branches.add(branch);
        }

        LineCoverage result = new LineCoverage();
        result.lineNumber = origin.lineNumber;
        result.hit = origin.hits;
        result.branches = branches;
        return result;
    }
}