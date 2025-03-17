package kotlinx.kover.reporter.parsing

import kotlinx.kover.reporter.commons.MethodSignature
import kotlinx.kover.reporter.commons.QualifiedName

internal class ClasspathInitial {
    val files: MutableMap<String, FileInitial> = mutableMapOf()
}

internal class FileInitial(val path: String) {
    var facadeClassName: QualifiedName? = null
    var initFunction: AnonymousFunctionInitial? = null

    val classes: MutableList<KotlinClassInitial> = mutableListOf()

    val functions: MutableList<SimpleFunctionInitial> = mutableListOf()
    val localFunctions: MutableList<AnonymousFunctionInitial> = mutableListOf()
    val properties: MutableList<PropertyInitial> = mutableListOf()
    val localClasses: MutableList<LocalClassInitial> = mutableListOf()

    val defImpls: MutableList<DefaultImplsInitial> = mutableListOf()
}

internal class KotlinClassInitial(
    val kotlinName: QualifiedName,
    val jvmName: QualifiedName,
    val isObject: Boolean,
    val isData: Boolean,
    val isInterfaceWithDefaultImpls: Boolean
) {
    var companionName: String? = null
    val functions: MutableList<SimpleFunctionInitial> = mutableListOf()
    val constructors: MutableList<SimpleFunctionInitial> = mutableListOf()
    val localFunctions: MutableList<AnonymousFunctionInitial> = mutableListOf()
    val properties: MutableList<PropertyInitial> = mutableListOf()
}

internal class SimpleFunctionInitial(
    val name: String,
    val receiver: String?,
    val context: List<String>,
    val valueParameters: List<String>,
    val jvmSignature: MethodSignature,
    val lineNumbers: List<Int>
)

internal class PropertyInitial(
    val name: String,
    val isVar: Boolean,
    val customGetter: Boolean,
    val customSetter: Boolean,
    val getter: AnonymousFunctionInitial?,
    val setter: AnonymousFunctionInitial?,
)


internal class AnonymousFunctionInitial(
    val jvmSignature: MethodSignature,
    val lineNumbers: List<Int>
)

internal class LocalClassInitial(
    val outerClass: String,
    val outerMethodSignature: MethodSignature?,
    val jvmName: QualifiedName,
    val localFunctions: MutableList<AnonymousFunctionInitial>,
    val constructors: MutableList<SimpleFunctionInitial>,
    val functions: MutableList<SimpleFunctionInitial>,
    val properties: MutableList<PropertyInitial>,
    val isLambda: Boolean
)

internal class DefaultImplsInitial(
    val jvmName: QualifiedName,
    val functions: List<AnonymousFunctionInitial>,
)
