/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.features.jvm.impl

import com.intellij.rt.coverage.instrument.api.OfflineInstrumentationApi
import kotlinx.kover.features.jvm.KoverFeatures.version
import kotlinx.kover.features.jvm.OfflineInstrumenter
import java.io.IOException
import java.io.InputStream


/**
 * Implementation of [OfflineInstrumenter].
 * The class should not be explicitly used from the outside.
 */
internal class OfflineInstrumenterImpl(private val countHits: Boolean): OfflineInstrumenter {

    override fun instrument(originalClass: InputStream, debugName: String): ByteArray {
        val previousConDySetting = ConDySettings.disableConDy()
        try {
            return OfflineInstrumentationApi.instrument(originalClass, countHits)
        } catch (e: Throwable) {
            throw IOException(
                String.format(
                    "Error while instrumenting '%s' with Kover instrumenter version '%s'",
                    debugName,
                    version
                ),
                e
            )
        } finally {
            ConDySettings.restoreConDy(previousConDySetting)
        }
    }
}
