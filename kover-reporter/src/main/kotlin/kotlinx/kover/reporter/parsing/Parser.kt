@file:OptIn(ExperimentalContextReceivers::class)

package kotlinx.kover.reporter.parsing

import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import kotlinx.kover.reporter.files.findSourceFile
import kotlinx.kover.reporter.report.formatType
import kotlinx.kover.reporter.utils.JvmNames
import kotlinx.kover.reporter.utils.JvmNames.CONSTRUCTOR_NAME
import kotlinx.kover.reporter.utils.JvmNames.DEFAULT_IMPLS_CLASS_NAME
import kotlinx.kover.reporter.utils.JvmNames.getterNameInLocalFun
import kotlinx.kover.reporter.utils.JvmNames.setterNameInLocalFun
import kotlinx.kover.reporter.utils.findBySignature
import kotlinx.kover.reporter.utils.isDefaultForConstructor
import kotlinx.kover.reporter.utils.jvmSignature
import kotlinx.kover.reporter.utils.lineNumbers
import kotlinx.kover.reporter.utils.outerMethod
import kotlinx.kover.reporter.utils.kotlinMetadata
import kotlinx.kover.reporter.utils.qualifiedName
import java.io.File
import kotlin.collections.plusAssign
import kotlin.metadata.*
import kotlin.metadata.jvm.*

internal object Parser {

    fun parseClasspath(classpath: List<File>, sourceDirs: List<File>, rootDir: File): ClasspathInitial {
        val primaryState = ClasspathInitial()

        classpath.flatMap { dir ->
            dir.walk().filter { file -> file.isFile && file.name.endsWith(".class") }
        }.map { classFile ->
            ClassReader(classFile.inputStream())
        }.forEach { reader ->
            primaryState.processClassFile(reader, sourceDirs, rootDir)
        }

        return primaryState
    }
}


private fun ClasspathInitial.processClassFile(reader: ClassReader, sourceDirs: List<File>, rootDir: File) {
    val classNode = ClassNode()
    reader.accept(classNode, ClassReader.SKIP_FRAMES)

    val sourceFile = findSourceFile(sourceDirs, classNode)
    val sourcePath = sourceFile.toRelativeString(rootDir)

    val file = files.getOrPut(sourcePath) { FileInitial(sourcePath) }

    val classMetadata = classNode.kotlinMetadata
    if (classMetadata != null) {
        file.processKotlinClassFile(classNode, classMetadata)
    } else {
        file.processJavaClassFile(classNode)
    }
}

private fun FileInitial.processKotlinClassFile(classNode: ClassNode, metadata: KotlinClassMetadata) {
    when (metadata) {
        is KotlinClassMetadata.Class -> {
            val kmClass = metadata.kmClass
            if (kmClass.visibility != Visibility.LOCAL) {
                classes += parseSimpleKotlinClass(classNode, kmClass)
            } else {
                localClasses += parseLocalClass(classNode, kmClass)
            }
        }

        is KotlinClassMetadata.FileFacade -> {
            processFileFacade(classNode, metadata.kmPackage, this)
        }

        is KotlinClassMetadata.SyntheticClass -> {
            // skip non-lambda synthetic classes
            if (metadata.isLambda) {
                parseLambda(classNode, metadata.kmLambda!!)?.let { localClasses += it }
            }
            if (classNode.name.contains("$${DEFAULT_IMPLS_CLASS_NAME}")) {
                defImpls += parseDefaultImpls(classNode)
            }
        }

        is KotlinClassMetadata.MultiFileClassFacade -> {
            println("MULTI FACADE ${classNode.name}")
        }

        is KotlinClassMetadata.MultiFileClassPart -> {
            println("MULTI PART ${classNode.name}")
        }

        is KotlinClassMetadata.Unknown -> {
            throw IllegalStateException("Library is too old")
        }
    }
}

private fun FileInitial.processJavaClassFile(classNode: ClassNode) {
    // TODO JAVA CLASS
    println("JAVA CLASS ${classNode.name}")
}


internal fun processFileFacade(classNode: ClassNode, metadata: KmPackage, fileInitial: FileInitial) {
    fileInitial.facadeClassName = classNode.qualifiedName

    classNode.methods.filter { it.name == JvmNames.STATIC_CONSTRUCTOR_NAME }.forEach { method ->
        fileInitial.initFunction = method.parseAnonymousFunction()
    }

    classNode.processFunctions(metadata.functions, fileInitial.functions, fileInitial.localFunctions)
    classNode.processProperties(metadata.properties, fileInitial.properties, fileInitial.localFunctions)
}

internal fun parseLambda(classNode: ClassNode, metadata: KmLambda): LocalClassInitial? {
    if (metadata.function.kind != MemberKind.DECLARATION) return null

    val potentialMethods =
        classNode.methods.filter { method -> method.lineNumbers().isNotEmpty() }

    if (potentialMethods.size > 1) {
        throw IllegalStateException("Several lambda methods found: ${potentialMethods.map { it.name + it.desc }}")
    }
    if (potentialMethods.isEmpty()) {
        throw IllegalStateException("No lambda method found")
    }

    val lambdaMethod = potentialMethods.single()

    val function = SimpleFunctionInitial(
        lambdaMethod.name,
        null,
        emptyList(),
        emptyList(),
        lambdaMethod.jvmSignature(),
        lambdaMethod.lineNumbers()
    )
    return LocalClassInitial(
        classNode.outerClass,
        classNode.outerMethod(),
        classNode.qualifiedName,
        mutableListOf(),
        mutableListOf(),
        mutableListOf(function),
        mutableListOf(),
        true
    )
}

internal fun parseLocalClass(classNode: ClassNode, metadata: KmClass): LocalClassInitial {
    val localFunctions: MutableList<AnonymousFunctionInitial> = mutableListOf()
    val functions: MutableList<SimpleFunctionInitial> = mutableListOf()
    val constructors: MutableList<SimpleFunctionInitial> = mutableListOf()
    val properties: MutableList<PropertyInitial> = mutableListOf()

    val classParameters = metadata.typeParameters.associate { it.id to it.name }

    classNode.processConstructors(metadata.constructors, constructors, localFunctions, classParameters)
    classNode.processFunctions(metadata.functions, functions, localFunctions, classParameters)
    classNode.processProperties(metadata.properties, properties, localFunctions)

    return LocalClassInitial(
        classNode.outerClass,
        classNode.outerMethod(),
        classNode.qualifiedName,
        localFunctions,
        constructors,
        functions,
        properties,
        false
    )
}

internal fun parseSimpleKotlinClass(classNode: ClassNode, metadata: KmClass): KotlinClassInitial {
    val classState = KotlinClassInitial(
        kotlinName = metadata.qualifiedName,
        jvmName = classNode.qualifiedName,
        isObject = metadata.kind == ClassKind.OBJECT || metadata.kind == ClassKind.COMPANION_OBJECT,
        isData = metadata.isData,
        isInterfaceWithDefaultImpls = metadata.hasMethodBodiesInInterface
    )
    classState.companionName = metadata.companionObject

    val classParameters = metadata.typeParameters.associate { it.id to it.name }

    classNode.processConstructors(
        metadata.constructors,
        classState.constructors,
        classState.localFunctions,
        classParameters
    )
    classNode.processFunctions(metadata.functions, classState.functions, classState.localFunctions, classParameters)
    classNode.processProperties(metadata.properties, classState.properties, classState.localFunctions)

    return classState
}

internal fun parseDefaultImpls(classNode: ClassNode): DefaultImplsInitial {
    val functions = classNode.methods.map { method ->
        AnonymousFunctionInitial(method.jvmSignature(), method.lineNumbers())
    }

    return DefaultImplsInitial(classNode.qualifiedName, functions)
}


internal fun KmFunction.parseSimpleFunction(
    javaMethod: MethodNode,
    classParameters: Map<Int, String> = emptyMap()
): SimpleFunctionInitial {
    val functionTypeParameters = classParameters + typeParameters.associate { it.id to it.name }
    val receiver = receiverParameterType?.formatType(functionTypeParameters)
    val context = contextReceiverTypes.map { it.formatType(functionTypeParameters) }
    val valueParameters = valueParameters.map { it.formatType(functionTypeParameters) }

    return SimpleFunctionInitial(
        name,
        receiver,
        context,
        valueParameters,
        javaMethod.jvmSignature(),
        javaMethod.lineNumbers()
    )
}

internal fun KmConstructor.parseConstructor(
    javaMethod: MethodNode,
    classParameters: Map<Int, String> = emptyMap()
): SimpleFunctionInitial {
    val functionTypeParameters = classParameters
    val valueParameters = valueParameters.map { it.formatType(functionTypeParameters) }

    return SimpleFunctionInitial(
        javaMethod.name,
        null,
        emptyList(),
        valueParameters,
        javaMethod.jvmSignature(),
        javaMethod.lineNumbers()
    )
}

internal fun MethodNode.parseLocalFunction(): AnonymousFunctionInitial {
    return AnonymousFunctionInitial(jvmSignature(), lineNumbers())
}

internal fun MethodNode.parseAnonymousFunction(): AnonymousFunctionInitial {
    return AnonymousFunctionInitial(jvmSignature(), lineNumbers())
}

internal fun KmProperty.parseProperty(methods: List<MethodNode>): PropertyInitial {
    return PropertyInitial(
        name,
        isVar,
        getter.isNotDefault,
        setter?.isNotDefault == true,
        methods.findBySignature(getterSignature)?.parseAnonymousFunction(),
        methods.findBySignature(setterSignature)?.parseAnonymousFunction()
    )
}


internal fun ClassNode.processFunctions(
    kotlinFunctions: MutableList<KmFunction>,
    outputFunctions: MutableList<SimpleFunctionInitial>,
    outputLocalFunctions: MutableList<AnonymousFunctionInitial>,
    classParameters: Map<Int, String> = emptyMap()
) {
    kotlinFunctions.forEach { function ->
        // skip non-declared functions
        if (function.kind != MemberKind.DECLARATION) return@forEach

        val javaMethod = methods.findBySignature(function.signature)!! // TODO

        methods.filter { it.name.startsWith("${function.name}$") }.forEach { localFunction ->
            outputLocalFunctions += localFunction.parseLocalFunction()
        }

        outputFunctions += function.parseSimpleFunction(javaMethod, classParameters)
    }
}

internal fun ClassNode.processConstructors(
    kotlinConstructors: MutableList<KmConstructor>,
    outputFunctions: MutableList<SimpleFunctionInitial>,
    outputLocalFunctions: MutableList<AnonymousFunctionInitial>,
    classParameters: Map<Int, String> = emptyMap()
) {
    kotlinConstructors.forEach { constructor ->
        val javaMethod = methods.findBySignature(constructor.signature)!! // TODO

        // default parameters
        methods
            .filter { it.name == CONSTRUCTOR_NAME && it.jvmSignature().isDefaultForConstructor(javaMethod.jvmSignature()) }
            .forEach { localFunction ->
                outputLocalFunctions += localFunction.parseLocalFunction()
            }
        // local functions
        methods.filter { it.name.startsWith("_init_$") }.forEach { localFunction ->
            outputLocalFunctions += localFunction.parseLocalFunction()
        }

        outputFunctions += constructor.parseConstructor(javaMethod, classParameters)
    }
}

internal fun ClassNode.processProperties(
    properties: List<KmProperty>,
    outputProperties: MutableList<PropertyInitial>,
    outputLocalFunctions: MutableList<AnonymousFunctionInitial>
) {
    properties.forEach { property ->
        outputProperties += property.parseProperty(methods)
        if (property.getterSignature != null) {
            methods.filter { it.name.startsWith(getterNameInLocalFun(property.name)) }.forEach { localFunction ->
                outputLocalFunctions += localFunction.parseLocalFunction()
            }
        }
        if (property.setterSignature != null) {
            methods.filter { it.name.startsWith(setterNameInLocalFun(property.name)) }.forEach { localFunction ->
                outputLocalFunctions += localFunction.parseLocalFunction()
            }
        }
    }
}
