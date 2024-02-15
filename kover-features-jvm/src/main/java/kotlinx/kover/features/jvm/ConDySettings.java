package kotlinx.kover.features.jvm;

/**
 * Internal class to control JVM ConDy settings.
 */
final class ConDySettings {

    private ConDySettings() {
        // no-op
    }

    private static final String CONDY_SYSTEM_PARAM_NAME = "coverage.condy.enable";

    /**
     * Disable JVM ConDy during instrumentation.
     *
     * @return previous value of ConDy setting
     */
    static String disableConDy() {
        // disable ConDy for offline instrumentations
        return System.setProperty(CONDY_SYSTEM_PARAM_NAME, "false");
    }

    /**
     * Restore previous value of JVM ConDy setting.
     *
     * @param prevValue new setting value
     */
    static void restoreConDy(String prevValue) {
        if (prevValue == null) {
            System.clearProperty(CONDY_SYSTEM_PARAM_NAME);
        } else {
            System.setProperty(CONDY_SYSTEM_PARAM_NAME, prevValue);
        }
    }
}
