package kotlinx.kover.test.functional.core

import org.junit.*
import org.junit.rules.*


internal open class BaseGradleScriptTest {
    @Rule
    @JvmField
    internal val rootFolder: TemporaryFolder = TemporaryFolder()

    fun builder(): ProjectBuilder {
        return createBuilder(rootFolder.root)
    }
}
