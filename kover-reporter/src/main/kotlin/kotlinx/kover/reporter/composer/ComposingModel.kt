package kotlinx.kover.reporter.composer

import kotlinx.kover.reporter.commons.MethodSignature
import kotlinx.kover.reporter.commons.QualifiedName


internal class SourceFile(
    val path: String,
    val classes: List<SimpleClass>,
    val facade: FileFacade?
) {
    override fun toString(): String = path
}


internal class SimpleClass(
    val kotlinName: QualifiedName,
    override val jvmName: QualifiedName,
    val isObject: Boolean,
    val isData: Boolean,
    val isInterfaceWithDefaultImpls: Boolean,
    val companionName: String?
) : ClassLike {
    override val functions: MutableList<SimpleFunction> = mutableListOf()
    override val properties: MutableList<Property> = mutableListOf()
    var companion: SimpleClass? = null
    val nested: MutableList<SimpleClass> = mutableListOf()

    var defaultImpls: SimpleClass? = null

    override fun toString(): String = "$kotlinName ($jvmName)"
}

internal class LocalClass(
    val kotlinName: String,
    override val jvmName: QualifiedName,
    override val outerClass: String,
    override val outerMethod: MethodSignature?
) : ClassInFunction {
    override val functions: MutableList<SimpleFunction> = mutableListOf()
    override val properties: MutableList<Property> = mutableListOf()

    override fun toString(): String = "$kotlinName ($jvmName)"
}

internal class AnonymousClass(
    override val jvmName: QualifiedName,
    override val outerClass: String,
    override val outerMethod: MethodSignature?
) : ClassInFunction {
    override val functions: MutableList<SimpleFunction> = mutableListOf()
    override val properties: MutableList<Property> = mutableListOf()

    override fun toString(): String = jvmName.toString()
}

internal class LambdaClass(
    override val jvmName: QualifiedName,
    override val outerClass: String,
    override val outerMethod: MethodSignature?
) : ClassInFunction {
    override val functions: MutableList<SimpleFunction> = mutableListOf()
    override val properties: MutableList<Property> = mutableListOf()

    override fun toString(): String = jvmName.toString()
}

internal sealed interface ClassInFunction : ClassLike {
    val outerClass: String
    val outerMethod: MethodSignature?
}


internal class FileFacade(override val jvmName: QualifiedName, val initFunction: AnonymousFunction?) : ClassLike {
    override val properties: MutableList<Property> = mutableListOf()
    override val functions: MutableList<SimpleFunction> = mutableListOf()

    override fun toString(): String = jvmName.toString()
}

internal interface ClassLike {
    val jvmName: QualifiedName
    val properties: MutableList<Property>
    val functions: MutableList<SimpleFunction>
}


internal class SimpleFunction(
    val kotlinName: String,
    val isConstructor: Boolean,
    val descriptor: String?,
    override val jvmSignature: MethodSignature,
    override val lineNumbers: List<Int>
) : Function {
    override val localClasses: MutableList<ClassInFunction> = mutableListOf()
    override val localFunctions: MutableList<FunctionInFunction> = mutableListOf()
    var defaultValues: AnonymousFunction? = null

    override fun toString(): String = "$kotlinName  ; $jvmSignature"
}

internal sealed interface FunctionInFunction : Function

internal class LocalFunction(
    val kotlinName: String,
    override val jvmSignature: MethodSignature,
    override val lineNumbers: List<Int>
) : FunctionInFunction {
    override val localClasses: MutableList<ClassInFunction> = mutableListOf()
    override val localFunctions: MutableList<FunctionInFunction> = mutableListOf()
    var defaultValues: AnonymousFunction? = null

    override fun toString(): String = "$kotlinName  ; $jvmSignature"
}

internal class AnonymousFunction(
    override val jvmSignature: MethodSignature,
    override val lineNumbers: List<Int>
) : FunctionInFunction {
    override val localClasses: MutableList<ClassInFunction> = mutableListOf()
    override val localFunctions: MutableList<FunctionInFunction> = mutableListOf()

    override fun toString(): String = jvmSignature.toString()
}


internal interface Function {
    val jvmSignature: MethodSignature
    val localClasses: MutableList<ClassInFunction>
    val localFunctions: MutableList<FunctionInFunction>
    val lineNumbers: List<Int>
}

internal class Property(
    val name: String,
    val isVar: Boolean,
    val customGetter: Boolean,
    val customSetter: Boolean,
    val getter: AnonymousFunction?,
    val setter: AnonymousFunction?
    ) {
    override fun toString(): String = "${if (isVar) {"var "} else "val "} $name"
}
