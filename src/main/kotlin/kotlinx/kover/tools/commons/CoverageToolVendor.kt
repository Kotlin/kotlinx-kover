package kotlinx.kover.tools.commons

/**
 * @param[reportFileExtension] The coverage report file extension, without the first `.`
 */
internal enum class CoverageToolVendor(
    val reportFileExtension: String
) {
    KOVER("ic"),
    JACOCO("exec"),
}
