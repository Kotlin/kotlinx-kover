import org.jetbrains.kover.test.functional.verification.*
import org.jetbrains.kover.test.functional.verification.subpackage.*
import kotlin.test.Test

class TestClass {
    @Test
    fun test() {
        FullyCovered().function0(0)
        FullyCovered().function1(1)
        FullyCovered().name()

        PartiallyCoveredFirst().function0(0)
        PartiallyCoveredFirst().name()

        PartiallyCoveredSecond().function1(1)

        SubFullyCovered().function0(0)
        SubFullyCovered().function1(1)
        SubFullyCovered().name()

        SubPartiallyCoveredFirst().function0(0)

        SubPartiallyCoveredSecond().function1(1)
    }
}
