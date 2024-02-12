package kotlinx.kover.features.java;

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
