package kotlinx.kover.reporter.utils

import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodNode
import kotlinx.kover.reporter.commons.MethodSignature
import kotlin.metadata.jvm.JvmMethodSignature

internal fun ClassNode.findAnnotation(annotationName: String, includeInvisible: Boolean = false) =
    visibleAnnotations?.firstOrNull { it.refersToName(annotationName) }
        ?: if (includeInvisible) invisibleAnnotations?.firstOrNull { it.refersToName(annotationName) } else null

internal fun AnnotationNode.refersToName(className: String) = desc.isJvmReferenceTo(className)

internal fun List<MethodNode>.findBySignature(signature: JvmMethodSignature?): MethodNode? {
    return singleOrNull { it.name == signature?.name && it.desc == signature?.descriptor }
}

internal fun MethodNode.jvmSignature(): MethodSignature {
    return MethodSignature(name, desc)
}

internal fun ClassNode.outerMethod(): MethodSignature? {
    return if (outerMethod != null) {
        MethodSignature(outerMethod, outerMethodDesc)
    } else {
        null
    }
}

internal fun MethodNode.lineNumbers(): List<Int> {
    return instructions.filterIsInstance<LineNumberNode>().map { it.line }.toSortedSet().toList()
}

