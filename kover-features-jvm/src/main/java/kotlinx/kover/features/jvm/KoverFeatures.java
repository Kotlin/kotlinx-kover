package kotlinx.kover.features.jvm;

import java.io.InputStream;
import java.util.Scanner;

/**
 * A class for using features via Java calls.
 */
public class KoverFeatures {
    private static final String koverVersion = readVersion();

    /**
     * Getting the Kover version.
     *
     * @return The version of Kover used in these utilities.
     */
    public static String getVersion() {
        return koverVersion;
    }

    /**
     * Converts a Kover template string to a regular expression string.
     * <p>
     * Replaces characters *` or `.` to `.*`, `#` to `[^.]*` and `?` to `.` regexp characters.
     * All special characters of regular expressions are also escaped.
     * </p>
     *
     * @param template Template string in Kover format
     * @return Regular expression corresponding given Kover template
     */
    public static String koverWildcardToRegex(String template) {
        return Wildcards.wildcardsToRegex(template);
    }

    /**
     * Create instance to instrument already compiled class-files.
     *
     * @return instrumenter for offline instrumentation.
     */
    public static OfflineInstrumenter createOfflineInstrumenter() {
        return new OfflineInstrumenterImpl(false);
    }

    private static String readVersion() {
        String version = "unrecognized";
        // read version from file in resources
        try (InputStream stream = KoverFeatures.class.getClassLoader().getResourceAsStream("kover.version")) {
            if (stream != null) {
                version = new Scanner(stream).nextLine();
            }
        } catch (Throwable e) {
            // can't read
        }
        return version;
    }
}
