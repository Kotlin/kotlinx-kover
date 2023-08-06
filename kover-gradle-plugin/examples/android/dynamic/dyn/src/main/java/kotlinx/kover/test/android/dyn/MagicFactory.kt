package kotlinx.kover.test.android.dyn

object MagicFactory {
    fun generate(): Int {
        DebugUtil.log("generate Int")
        return 42
    }
}