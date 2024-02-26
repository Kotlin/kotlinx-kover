package kotlinx.kover.jvmagent;

public class ParseUtils {
    private ParseUtils() {
        // no-op
    }

    public static boolean isBoolean(String value) {
        return "true".equals(value) || "false".equals(value);
    }
}
