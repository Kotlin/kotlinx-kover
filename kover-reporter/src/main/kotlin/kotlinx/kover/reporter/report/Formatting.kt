package kotlinx.kover.reporter.report

import kotlinx.kover.reporter.commons.QualifiedName
import kotlinx.kover.reporter.parsing.SimpleFunctionInitial
import kotlin.metadata.*

internal fun KmValueParameter.formatType(typeNames: Map<Int, String> = emptyMap()): String {
    return if (varargElementType == null) {
        type.formatType(typeNames)
    } else {
        "vararg " + varargElementType!!.formatType(typeNames)
    }
}

internal fun KmType.formatType(typeParameters: Map<Int, String> = emptyMap()): String {
    val classifier = classifier
    var result = when (classifier) {
        is KmClassifier.Class -> classifier.name.toKLibName()
        is KmClassifier.TypeAlias -> classifier.name.toKLibName()
        is KmClassifier.TypeParameter -> typeParameters[classifier.id] ?: ("#" + classifier.id)
    }

    if (isNullable) {
        result += "?"
    }
    if (isDefinitelyNonNull) {
        result += " & Any"
    }
    return result
}

internal fun SimpleFunctionInitial.formatDescriptor(): String {
    return "desc"
}

private fun QualifiedName.toKLibName(): String {
    return if (packageName.isNotEmpty()) "$packageName/$relativeName" else relativeName
}

/**
 * Get name in format mypackage.name/MyClass.Nested mostly used in KLib naming.
 *
 */
private fun ClassName.toKLibName(): String {
    val packageName = substringBeforeLast("/", "").replace('/', '.')
    val relativePart = substringAfterLast('/')
    return if (packageName.isNotEmpty()) "$packageName/$relativePart" else relativePart
}