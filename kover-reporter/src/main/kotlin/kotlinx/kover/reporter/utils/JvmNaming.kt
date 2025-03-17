package kotlinx.kover.reporter.utils

import org.objectweb.asm.tree.ClassNode
import kotlinx.kover.reporter.commons.MethodSignature
import kotlinx.kover.reporter.commons.QualifiedName

internal object JvmNames {
    internal const val CONSTRUCTOR_NAME = "<init>"
    internal const val CONSTRUCTOR_NAME_IN_LOCAL_FUN = "_init_"
    internal const val STATIC_CONSTRUCTOR_NAME = "<clinit>"

    internal const val DEFAULT_IMPLS_CLASS_NAME = "DefaultImpls"


    internal fun setterNameInLocalFun(propertyName: String) = "_set_${propertyName}_"
    internal fun getterNameInLocalFun(propertyName: String) = "_get_${propertyName}_"
}


internal fun QualifiedName.toJvmInternalName(): String {
    val relativeName = relativeName.replace('.', '$')
    val packageName = packageName.replace('.', '/')
    return if (packageName.isEmpty()) relativeName else "$packageName/$relativeName"
}

internal val ClassNode.qualifiedName: QualifiedName
    get() = QualifiedName(packageName, className)



/**
 * Get package in canonical form `foo.bar.biz`.
 * Empty string in case if class is in root package.
 */
internal val ClassNode.packageName: String
    get() = name.jvmNameToPackageName()

private fun String.jvmNameToPackageName(): String {
    return substringBeforeLast('/', "").replace('/', '.')
}

/**
 * Get class in canonical form `Foo.Bar.Biz`.
 */
internal val ClassNode.className: String
    get() {
        return name.jvmRelativeNameToCanonicalForm()
    }

/**
 * Replace dollar sign with dots.
 */
private fun String.jvmRelativeNameToCanonicalForm(): String {
    val className = substringAfterLast('/')

    if (!className.contains('$')) return className

    return className.splitWithDollar().joinToString(separator = ".")
}

/**
 * Check given string is reference to given [className].
 *
 * It's important [className] to be internal class name;
 *
 * E.g. for class `java/lang/String` reference is `Ljava/lang/String;`
 */
internal fun String.isJvmReferenceTo(className: String): Boolean {
    return startsWith('L') && endsWith(';') && regionMatches(1, className, 0, className.length)
}

/**
 *
 */
internal fun String.toJvmReference(): String {
    return "L$this;"
}


internal fun String.splitWithDollar(): List<String> {
    val segments = mutableListOf<String>()

    val builder = StringBuilder()
    for (idx in indices) {
        val c = this[idx]
        // Don't treat a character as a separator if:
        // - it's not a '$'
        // - it's at the beginning of the segment
        // - it's the last character of the string
        if (c != '$' || builder.isEmpty() || idx == length - 1) {
            builder.append(c)
            continue
        }
        check(c == '$')
        // class$$$subclass -> class.$$subclass, were at second $ here.
        if (builder.last() == '$') {
            builder.append(c)
            continue
        }

        segments.add(builder.toString())
        builder.clear()
    }
    if (builder.isNotEmpty()) {
        segments.add(builder.toString())
    }

    return segments
}

internal fun MethodSignature.isDefaultFor(ownerMethod: MethodSignature): Boolean {
    val searchedName = ownerMethod.name
    val searchedDesc = ownerMethod.desc.removePrefix("(").replace(")", "ILjava/lang/Object;)")

    return name == searchedName && desc.endsWith(searchedDesc)
}

internal fun MethodSignature.isDefaultForConstructor(ownerMethod: MethodSignature): Boolean {
    val searchedName = ownerMethod.name
    val searchedDesc = ownerMethod.desc.replace(")", "ILkotlin/jvm/internal/DefaultConstructorMarker;)")

    return name == searchedName && desc == searchedDesc
}
