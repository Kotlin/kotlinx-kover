package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.core.*
import kotlin.test.*

internal class ConfigurationCacheTests: BaseGradleScriptTest() {
    @Test
    fun testConfigCache() {
        builder("Testing configuration cache support")
            .sources("simple")
            .build()
            .run("build", "koverMergedReport", "koverMergedVerify", "koverReport", "koverVerify", "--configuration-cache")
    }
}
