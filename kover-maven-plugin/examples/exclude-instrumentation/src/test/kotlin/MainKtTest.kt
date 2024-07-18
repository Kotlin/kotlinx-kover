package kotlinx.kover.maven.plugin.testing

import kotlin.test.Test

class MainKtTest {
    @Test
    fun myTest() {
        Main().used()
        SecondClass().used()
    }
}