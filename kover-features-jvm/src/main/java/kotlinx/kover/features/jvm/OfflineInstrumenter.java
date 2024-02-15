package kotlinx.kover.features.jvm;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class for instrumentation of JVM byte code of already compiled class-files.
 */
public interface OfflineInstrumenter {
    /**
     * Modify byte code of single class-file to measure the coverage of this class.
     *
     * @param originalClass input stream with byte code of original class-file
     * @param debugName name of the class or class-file, which is used in the error message
     * @return instrumented byte code
     * @throws IOException in case of any instrumentation error
     */
    byte[] instrument(InputStream originalClass, String debugName) throws IOException;
}