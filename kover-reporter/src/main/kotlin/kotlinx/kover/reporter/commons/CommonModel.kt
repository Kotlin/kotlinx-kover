package kotlinx.kover.reporter.commons

internal class QualifiedName(val packageName: String, val relativeName: String) {
    override fun toString(): String = "$packageName/$relativeName"

    fun lastSegment() = relativeName.substringAfterLast('.')

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QualifiedName) return false

        if (packageName != other.packageName) return false
        if (relativeName != other.relativeName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + relativeName.hashCode()
        return result
    }
}

internal class MethodSignature(val name: String, val desc: String) {
    override fun toString(): String = "$name$desc"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MethodSignature) return false

        if (name != other.name) return false
        if (desc != other.desc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + desc.hashCode()
        return result
    }
}

