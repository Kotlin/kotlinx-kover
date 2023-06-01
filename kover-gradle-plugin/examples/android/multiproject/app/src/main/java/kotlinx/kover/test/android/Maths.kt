package kotlinx.kover.test.android

object Maths {
    fun sum(a: Int, b: Int): Int {
        DebugUtil.log("invoked sum")
        return a + b
    }

    fun sub(a: Int, b: Int): Int {
        DebugUtil.log("invoked sub")
        return a - b
    }
}