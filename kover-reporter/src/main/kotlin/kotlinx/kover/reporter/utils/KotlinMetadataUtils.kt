package kotlinx.kover.reporter.utils

import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.jvm.Metadata

internal val ClassNode.kotlinMetadata: KotlinClassMetadata?
    get() {
        val metadata = findAnnotation("kotlin/Metadata", false) ?: return null

        @Suppress("UNCHECKED_CAST")
        val header = with(metadata) {
            Metadata(
                kind = get("k") as Int?,
                metadataVersion = (get("mv") as List<Int>?)?.toIntArray(),
                data1 = (get("d1") as List<String>?)?.toTypedArray(),
                data2 = (get("d2") as List<String>?)?.toTypedArray(),
                extraString = get("xs") as String?,
                packageName = get("pn") as String?,
                extraInt = get("xi") as Int?
            )
        }
        return KotlinClassMetadata.readLenient(header)
    }

private operator fun AnnotationNode.get(key: String): Any? = values.annotationValue(key)

private fun List<Any>.annotationValue(key: String): Any? {
    for (index in (0 until size / 2)) {
        if (this[index * 2] == key)
            return this[index * 2 + 1]
    }
    return null
}
