package kotlinx.kover.jvmagent;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class KoverJvmAgentPremain {

    private static final String FILE_PREFIX_IN_ARGS = "file:";
    private static final String FILE_PATH_ARGUMENT = "report.file=";
    private static final String APPEND_ARGUMENT = "report.append=";
    private static final String EXCLUDE_WITH_WILDCARDS_ARGUMENT = "exclude=";
    private static final String EXCLUDE_WITH_REGEX_ARGUMENT = "exclude.regex=";
    private static final String INCLUDE_WITH_WILDCARDS_ARGUMENT = "include=";
    private static final String INCLUDE_WITH_REGEX_ARGUMENT = "include.regex=";

    private static final String regexMetacharacters = "<([{\\^-=$!|]})+.>";

    private static final HashSet<Character> regexMetacharactersSet = new HashSet<Character>();

    private static final List<String> arguments = Arrays.asList(
            FILE_PATH_ARGUMENT,
            APPEND_ARGUMENT,
            EXCLUDE_WITH_WILDCARDS_ARGUMENT,
            EXCLUDE_WITH_REGEX_ARGUMENT,
            INCLUDE_WITH_WILDCARDS_ARGUMENT,
            INCLUDE_WITH_REGEX_ARGUMENT
    );

    static {
        for (int i = 0; i < regexMetacharacters.length(); i++) {
            char c = regexMetacharacters.charAt(i);
            regexMetacharactersSet.add(c);
        }
    }

    public static void premain(String argsString, Instrumentation instrumentation) throws Exception {
        KoverAgentSettings settings = readSettingsFromFile(extractArgsFile(argsString));
        IntellijIntegration.callPremain(settings, instrumentation);
    }

    private static File extractArgsFile(String args) {
        if (!args.startsWith(FILE_PREFIX_IN_ARGS)) {
            throw new IllegalArgumentException("Incorrect arguments format for Kover JVM agent, arguments are expected to start with " + FILE_PREFIX_IN_ARGS);
        }
        return new File(args.substring(FILE_PREFIX_IN_ARGS.length()));
    }

    public static boolean isBoolean(String value) {
        return "true".equals(value) || "false".equals(value);
    }

    private static KoverAgentSettings readSettingsFromFile(File file) throws IOException {
        KoverAgentSettings settings = new KoverAgentSettings();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

            String line = reader.readLine();
            while (line != null ) {
                if (line.startsWith(FILE_PATH_ARGUMENT)) {
                    settings.reportFilePath = line.substring(FILE_PATH_ARGUMENT.length());
                } else if (line.startsWith(EXCLUDE_WITH_WILDCARDS_ARGUMENT)) {
                    String wildcards = line.substring(EXCLUDE_WITH_WILDCARDS_ARGUMENT.length());
                    settings.exclusions.add(wildcardsToRegex(wildcards));
                } else if (line.startsWith(EXCLUDE_WITH_REGEX_ARGUMENT)) {
                    settings.exclusions.add(line.substring(EXCLUDE_WITH_REGEX_ARGUMENT.length()));
                } else if (line.startsWith(INCLUDE_WITH_WILDCARDS_ARGUMENT)) {
                    String wildcards = line.substring(INCLUDE_WITH_WILDCARDS_ARGUMENT.length());
                    settings.exclusions.add(wildcardsToRegex(wildcards));
                } else if (line.startsWith(INCLUDE_WITH_REGEX_ARGUMENT)) {
                    settings.exclusions.add(line.substring(INCLUDE_WITH_REGEX_ARGUMENT.length()));
                } else if (line.startsWith(APPEND_ARGUMENT)) {
                    String value = line.substring(APPEND_ARGUMENT.length());
                    if (!isBoolean(value)) {
                        throw new IllegalArgumentException("Incorrect value for argument " + APPEND_ARGUMENT + " in Kover JVM agent arguments file, expected true or false");
                    }
                    settings.appendToReportFile = Boolean.parseBoolean(value);
                } else if (line.length() == 0) {
                    // skip empty line
                } else {
                    throw new IllegalArgumentException("Unrecognized line in Kover arguments file: " + line
                            + ". Line must start with one of prefixes: " + arguments);
                }
                line = reader.readLine();
            }

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    // no-op
                }
            }
        }

        if (settings.reportFilePath == null) {
            throw new IllegalArgumentException("Path to the report file is required, add " + FILE_PATH_ARGUMENT + " argument to the args file");
        }

        return settings;
    }

    /**
     * Replaces characters `*` or `.` to `.*`, `#` to `[^.]*` and `?` to `.` regexp characters.
     */
    private static String wildcardsToRegex(String value) {
        // in most cases, the characters `*` or `.` will be present therefore, we increase the capacity in advance
        final StringBuilder builder = new StringBuilder(value.length() * 2);

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (regexMetacharactersSet.contains(c)) {
                builder.append('\\').append(c);
            } else if (c == '*') {
                builder.append(".*");
            } else if (c == '?') {
                builder.append('.');
            } else if (c == '#') {
                builder.append("[^.]*");
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

}
