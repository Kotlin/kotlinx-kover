package kotlinx.kover.reporter.report

import com.charleskorn.kaml.SingleLineStringStyle
import com.charleskorn.kaml.YamlSingleLineStringStyle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * JSON Schema for Kotlin coverage report
 */
@Serializable
data class JvmCoverageReport(
    /** All source files */
    val files: List<FileCoverage> = emptyList()
)

@Serializable
data class FileCoverage(
    /** The file path is relative to some root of the entire project */
    val path: String,
    /** Top-level classes in the file */
    val classes: List<ClassCoverage> = emptyList(),
    /** Top-level functions in the file */
    val functions: List<FunctionCoverage> = emptyList(),
    /** Top-level properties in the file */
    val properties: List<PropertyCoverage> = emptyList(),
    /** Top-level named objects in the file */
    val objects: List<NamedObjectCoverage> = emptyList(),
    /** Code written in initializers of top-level properties */
    val initFunction: AnonymousFunctionCoverage? = null
)

@Serializable
data class ClassCoverage(
    /** Qualified class name */
    val name: String,
    /** JVM internal binary name of the class */
    val jvmName: String,
    /** Class functions */
    val functions: List<FunctionCoverage> = emptyList(),
    /** Class constructors */
    val constructors: List<FunctionCoverage> = emptyList(),
    /** Class properties */
    val properties: List<PropertyCoverage> = emptyList(),
    /** Nested classes in the class */
    val classes: List<ClassCoverage> = emptyList(),
    /** Nested objects in the class */
    val objects: List<NamedObjectCoverage> = emptyList(),
    /* Is data class or object */
    val isData: Boolean = false,
    /** Companion object of the class */
    val companion: NamedObjectCoverage? = null,
    /** Separate JVM class with implementations of default methods of an interface */
    val jvmDefaultImpl: AnonymousClassCoverage? = null
)

@Serializable
data class FunctionCoverage(
    /** Kotlin or Java name of the function */
    val name: String,
    /** Function descriptor */
    val descriptor: String? = null,
    /** JVM signature of the function */
    val jvmSignature: String,
    /** Line-by-line class coverage */
    @Serializable(with = LineCoverageSerializer::class)
    @YamlSingleLineStringStyle(SingleLineStringStyle.Plain)
    val lines: Map<Int, LineCoverage>,
    /** Function to fill default values */
    val jvmDefaultValues: AnonymousFunctionCoverage? = null,
    /** Local functions */
    val functions: List<FunctionCoverage> = emptyList(),
    /** Local classes */
    val classes: List<LocalClassCoverage> = emptyList(),
    val lambdaClasses: List<AnonymousClassCoverage> = emptyList(),
    val anonymousFunctions: List<AnonymousFunctionCoverage> = emptyList(),
    val anonymousClasses: List<AnonymousClassCoverage> = emptyList()
)

@Serializable
data class AnonymousFunctionCoverage(
    /** JVM signature of the function */
    val jvmSignature: String,
    @Serializable(with = LineCoverageSerializer::class)
    @YamlSingleLineStringStyle(SingleLineStringStyle.Plain)
    val lines: Map<Int, LineCoverage>,
    val functions: List<FunctionCoverage> = emptyList(),
    val classes: List<LocalClassCoverage> = emptyList(),
    val lambdaClasses: List<AnonymousClassCoverage> = emptyList(),
    val anonymousFunctions: List<AnonymousFunctionCoverage> = emptyList(),
    val anonymousClasses: List<AnonymousClassCoverage> = emptyList()
)

@Serializable
data class AnonymousClassCoverage(
    /** JVM internal binary name of the class */
    val jvmName: String,
    val functions: List<FunctionCoverage> = emptyList(),
    /** Class constructors */
    val constructors: List<FunctionCoverage> = emptyList(),
    val properties: List<PropertyCoverage> = emptyList()
)

@Serializable
data class LocalClassCoverage(
    /** Kotlin or Java name of the class */
    val name: String,
    /** JVM internal binary name of the class */
    val jvmName: String,
    val functions: List<FunctionCoverage> = emptyList(),
    /** Class constructors */
    val constructors: List<FunctionCoverage> = emptyList(),
    val properties: List<PropertyCoverage> = emptyList()
)

@Serializable
data class PropertyCoverage(
    /** Property name */
    val name: String,
    val customGetter: Boolean = false,
    val customSetter: Boolean = false,
    val getter: AnonymousFunctionCoverage?,
    val setter: AnonymousFunctionCoverage? = null
)

@Serializable
data class NamedObjectCoverage(
    /** Qualified object name */
    val name: String,
    /** JVM internal binary name of the class */
    val jvmName: String,
    val functions: List<FunctionCoverage> = emptyList(),
    /** Class constructors */
    val constructors: List<FunctionCoverage> = emptyList(),
    val properties: List<PropertyCoverage> = emptyList(),
    val classes: List<ClassCoverage> = emptyList(),
    val objects: List<NamedObjectCoverage> = emptyList(),
    /* Is data class or object */
    val isData: Boolean = false,
)

@Serializable
sealed class LineCoverage

@Serializable
data class SimpleLineCoverage(
    /** Hit count of the line if there are no branches */
    val hits: Int
) : LineCoverage()

@Serializable
data class BranchedLineCoverage(
    /** Hit count of a numbered branch of the line */
    val branches: Map<Int, Int>
) : LineCoverage()


@OptIn(ExperimentalSerializationApi::class)
internal class LineCoverageSerializer<K, V>(val key: KSerializer<*>, val value: KSerializer<*>) : KSerializer<Map<Int, LineCoverage>> {
    override val descriptor = buildClassSerialDescriptor("LinesCoverage")

    public override fun serialize(encoder: Encoder, value: Map<Int, LineCoverage>) {
        val builder = StringBuilder()
        var isFirst = true
        builder.append("{")
        value.forEach { (index, coverage) ->
            if (!isFirst) {
                builder.append(",")
            }
            isFirst = false
            builder.append(index)
            builder.append(":")
            builder.encodeLine(coverage)
        }
        builder.append("}")
        encoder.encodeString(builder.toString())
    }

    private fun StringBuilder.encodeLine(line: LineCoverage) {
        if (line is SimpleLineCoverage) {
            append(line.hits)
        }
        if (line is BranchedLineCoverage) {
            line.branches.toString()
        }
    }
    private fun StringBuilder.encodeLine(line: BranchedLineCoverage) {

    }


    public override fun deserialize(decoder: Decoder): Map<Int, LineCoverage> {
        return emptyMap()
    }
}