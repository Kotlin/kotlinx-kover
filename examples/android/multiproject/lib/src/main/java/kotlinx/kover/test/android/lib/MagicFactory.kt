package kotlinx.kover.test.android.lib

object MagicFactory {
    fun generate(): Int {
        DebugUtil.log("generate Int")
        return 42
    }
}