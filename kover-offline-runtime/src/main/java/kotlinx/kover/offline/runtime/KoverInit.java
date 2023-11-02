package kotlinx.kover.offline.runtime;

import com.intellij.rt.coverage.offline.api.CoverageRuntime;
import kotlinx.kover.offline.runtime.api.KoverRuntime;

import java.io.File;

import static kotlinx.kover.offline.runtime.api.KoverRuntime.LOG_FILE_PROPERTY_NAME;
import static kotlinx.kover.offline.runtime.api.KoverRuntime.REPORT_PROPERTY_NAME;

/**
 * Class for initializing the Kover offline instrumentation runtime.
 * <p>
 * This class is initialized by the instrumented code by class name when any of the instrumented method is executed for the first time.
 * Therefore, all initialization code must be placed in the {@code <clinit>} method.
 */
class KoverInit {

    static {
        String reportNameSavedOnExitProp = System.getProperty(REPORT_PROPERTY_NAME);
        String logFileProp = System.getProperty(LOG_FILE_PROPERTY_NAME);

        if (logFileProp != null) {
            CoverageRuntime.setLogPath(new File(LOG_FILE_PROPERTY_NAME));
        } else {
            CoverageRuntime.setLogPath(new File(KoverRuntime.DEFAULT_LOG_FILE_NAME));
        }

        if (reportNameSavedOnExitProp != null) {
            saveOnExit(reportNameSavedOnExitProp);
        }
    }

    private static void saveOnExit(final String fileName) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    KoverRuntime.saveReport(fileName);
                } catch (Throwable e) {
                    System.out.println("Kover error: failed to save report file '" + fileName +"': " + e.getMessage());
                }
            }
        }));
    }

    private KoverInit() {
        // no instances
    }
}
