package kotlinx.kover.test.functional.core

import org.junit.*
import org.junit.rules.*


internal open class BaseGradleScriptTest {
    @Rule
    @JvmField
    internal val rootFolder: TemporaryFolder = TemporaryFolder()

    fun builder(description: String): TestCaseBuilder {
        return createBuilder(rootFolder.root, description)
    }

    fun internalSample(name: String): GradleRunner {
        return createInternalSample(name, rootFolder.root)
    }
}
