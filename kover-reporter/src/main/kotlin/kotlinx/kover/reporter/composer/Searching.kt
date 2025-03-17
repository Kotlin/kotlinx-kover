package kotlinx.kover.reporter.composer

import kotlinx.kover.reporter.commons.MethodSignature
import kotlinx.kover.reporter.commons.QualifiedName
import kotlinx.kover.reporter.parsing.AnonymousFunctionInitial
import kotlinx.kover.reporter.parsing.LocalClassInitial
import kotlinx.kover.reporter.utils.toJvmInternalName
import kotlinx.kover.reporter.utils.toJvmReference

internal fun Function.findDefaultValuesForFunction(): FunctionInFunction? {
    val searchedName = "${jvmSignature.name}\$default"
    val searchedDesc = jvmSignature.desc.removePrefix("(").replace(")", "ILjava/lang/Object;)")

    return localFunctions.singleOrNull {
        it.jvmSignature.name == searchedName && it.jvmSignature.desc.endsWith(searchedDesc)
    }
}

internal fun Function.findDefaultValuesForConstructor(): FunctionInFunction? {
    val searchedName = jvmSignature.name
    val searchedDesc = jvmSignature.desc.replace(")", "ILkotlin/jvm/internal/DefaultConstructorMarker;)")

    return localFunctions.singleOrNull {
        it.jvmSignature.name == searchedName && it.jvmSignature.desc == searchedDesc
    }
}

internal fun <T : Function> findOuterFunction(localFunction: AnonymousFunctionInitial, candidates: List<T>): T? {
    if (candidates.size == 1) return candidates.first()

    val filtered = candidates.filter {
        it.lineNumbers.last() >= localFunction.lineNumbers.last()
    }

    //
    filtered.firstOrNull { it.lineNumbers.first() <= localFunction.lineNumbers.first() }?.let { return it }

    return filtered.minByOrNull { it.lineNumbers.first() }
}


internal fun MethodSignature.toDefaultImpls(ownerName: QualifiedName): MethodSignature {
    val ownerReference = ownerName.toJvmInternalName().toJvmReference()
    return MethodSignature(name, desc.replace("(", "($ownerReference"))
}

internal val LocalClassInitial.isAnonymous: Boolean
    get() {
        val nameParts = jvmName.relativeName.split('.')

        if (nameParts.last()
                .toIntOrNull() != null && nameParts.size > 1 && nameParts[nameParts.lastIndex - 1] == outerMethodSignature?.name
        ) return true

        return false
    }

// lambda is named like
internal fun AnonymousFunctionInitial.isNamedLikeLambda(): Boolean {
    val name = jvmSignature.name
    if (!name.contains("$")) return false

    val lastSegment = name.substringAfterLast('$')
    lastSegment.toIntOrNull() ?: return false
    return name.count { it == '$' } >= 2 && jvmSignature.name.endsWith("\$lambda$$lastSegment")
}
