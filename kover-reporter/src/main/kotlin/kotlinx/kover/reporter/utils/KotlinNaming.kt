package kotlinx.kover.reporter.utils

import kotlinx.kover.reporter.commons.QualifiedName
import kotlin.metadata.KmClass

internal val KmClass.qualifiedName: QualifiedName
    get() {
        val packageName = name.substringBeforeLast("/", "").replace('/', '.')
        val relativePart = name.substringAfterLast('/')
        return QualifiedName(packageName, relativePart)
    }
