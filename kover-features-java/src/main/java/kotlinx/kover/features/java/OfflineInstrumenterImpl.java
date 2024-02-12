package kotlinx.kover.features.java;

import com.intellij.rt.coverage.instrument.api.OfflineInstrumentationApi;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of {@link OfflineInstrumenter}.
 * The class should not be explicitly used from the outside.
 */
class OfflineInstrumenterImpl implements OfflineInstrumenter {
    private final boolean countHits;

    OfflineInstrumenterImpl(boolean countHits) {
        this.countHits = countHits;
    }

    @Override
    public byte[] instrument(InputStream originalClass, String debugName) throws IOException {
        String condySetting = ConDySettings.disableConDy();

        try {
            return OfflineInstrumentationApi.instrument(originalClass, countHits);
        } catch (Throwable e) {
            throw new IOException(
                    String.format("Error while instrumenting '%s' with Kover instrumenter version '%s'",
                            debugName, KoverFeatures.getVersion()), e);
        } finally {
            ConDySettings.restoreConDy(condySetting);
        }
    }

}
