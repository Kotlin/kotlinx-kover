package kotlinx.kover.jvmagent;

import com.intellij.rt.coverage.main.CoveragePremain;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

public class IntellijIntegration {
    /**
     * A flag to enable tracking per test coverage.
     */
    private static final boolean TRACKING_PER_TEST = false;

    /**
     * A flag to calculate coverage for unloaded classes.
     */
    private static final boolean CALCULATE_FOR_UNLOADED_CLASSES = false;

    /**
     * Create hit block only for line, false adds the ability to count branches
     */
    private static final boolean LINES_ONLY_MODE = false;

    private IntellijIntegration() {
        // no-op
    }

    public static void callPremain(KoverAgentSettings settings, Instrumentation instrumentation) throws Exception {
        setIntellijSystemProperties();
        String intelliJArgsString = joinIntellijArgs(createIntellijArgs(settings));

        CoveragePremain.premain(intelliJArgsString, instrumentation);
    }

    private static void setIntellijSystemProperties() {
        System.setProperty("idea.coverage.use.system.classloader", "true");

        // Disable agent logging to stdout for messages of levels `debug`, `info`, `warn`.
        System.setProperty("idea.coverage.log.level", "error");

        // Enables saving the array in the ConDy field,
        // without it there will be an appeal to the hash table foreach method, which very slow.
        System.setProperty("idea.new.tracing.coverage", "true");

        // Enables ignoring constructors in classes where all methods are static.
        System.setProperty("coverage.ignore.private.constructor.util.class", "true");

        // Do not count amount hits of the line, only 0 or 1 will be place into int[] - reduce byte code size
        System.setProperty("idea.coverage.calculate.hits", "false");
    }

    /**
     *
     * IntelliJ agent arguments format:
     * [0] data file to save coverage result
     * [1] a flag to enable tracking per test coverage
     * [2] a flag to calculate coverage for unloaded classes
     * [3] a flag to use data file as initial coverage, also use it if several parallel processes are to write into one file
     * [4] a flag to run line coverage or branch coverage otherwise
     * // optional smap block
     * [5] optional: true
     * [6] smap file path
     * // end of optional smap
     * // optional includes block, until first line started with '-' or eof
     * [N+1] inclusion pattern
     * [N+i] ...
     * // optional excludes block, until first line started with '-' or eof
     * [N]-exclude
     * [N+1] exclusion pattern
     * [N+i] ...
     * // optional excludes annotation block, until first line started with '-' or eof
     * [N]-excludeAnnotations
     * [N+1] annotation exclusion pattern
     * [N+i] ...
     *
     */
    private static List<String> createIntellijArgs(KoverAgentSettings settings) {
        ArrayList<String> args = new ArrayList<String>();
        args.add(settings.reportFilePath);
        args.add(Boolean.toString(TRACKING_PER_TEST));
        args.add(Boolean.toString(CALCULATE_FOR_UNLOADED_CLASSES));
        args.add(Boolean.toString(settings.appendToReportFile));
        args.add(Boolean.toString(LINES_ONLY_MODE));

        args.addAll(settings.inclusions);

        if (!settings.exclusions.isEmpty()) {
            args.add("-exclude");
            args.addAll(settings.exclusions);
        }

        return args;
    }

    private static String joinIntellijArgs(List<String> args) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            if (KoverJvmAgentPremain.isBoolean(arg)) {
                builder.append(arg);
            } else {
                builder.append('"');
                builder.append(arg);
                builder.append('"');
            }
            builder.append(',');
        }
        return builder.toString();
    }
}
