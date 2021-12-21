package kotlinx.kover.test.functional.core

import org.junit.*
import org.junit.rules.*


internal open class BaseGradleScriptTest {
    @Rule
    @JvmField
    internal val rootFolder: TemporaryFolder = TemporaryFolder()

    fun builder(description: String): ProjectBuilder {
        return createBuilder(rootFolder.root, description)
    }

    fun internalProject(name: String): ProjectRunner {
        return loadInternalProject(name, rootFolder.root)
    }
}
