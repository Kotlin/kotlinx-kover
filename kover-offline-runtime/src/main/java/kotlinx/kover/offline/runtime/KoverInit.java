package kotlinx.kover.offline.runtime;

import com.intellij.rt.coverage.offline.api.CoverageRuntime;
import com.intellij.rt.coverage.util.ErrorReporter;
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
        String logFileProp = System.getProperty(LOG_FILE_PROPERTY_NAME);
        if (logFileProp == null) {
            // by default, we do not create a file, because we do not know what rights our application is running with
            //   and whether it can create files in the current directory or next to the binary report file
            CoverageRuntime.setLogPath(null);
        } else {
            CoverageRuntime.setLogPath(new File(logFileProp));
        }

        // setting the logging level in the "standard" error output stream
        CoverageRuntime.setLogLevel(ErrorReporter.WARNING);

        String reportNameSavedOnExitProp = System.getProperty(REPORT_PROPERTY_NAME);
        if (reportNameSavedOnExitProp != null) {
            // if a parameter is passed, then use the shutdown hook to save the binary report to a file
            saveOnExit(reportNameSavedOnExitProp);
        }
    }

    private static void saveOnExit(final String fileName) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    KoverRuntime.saveReport(new File(fileName));
                } catch (Throwable e) {
                    System.err.println("Kover error: failed to save report file '" + fileName +"': " + e.getMessage());
                }
            }
        }));
    }

    private KoverInit() {
        // no instances
    }
}
