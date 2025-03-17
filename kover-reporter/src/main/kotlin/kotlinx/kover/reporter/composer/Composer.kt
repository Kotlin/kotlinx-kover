package kotlinx.kover.reporter.composer

import kotlinx.kover.reporter.parsing.AnonymousFunctionInitial
import kotlinx.kover.reporter.parsing.FileInitial
import kotlinx.kover.reporter.parsing.KotlinClassInitial
import kotlinx.kover.reporter.parsing.LocalClassInitial
import kotlinx.kover.reporter.parsing.PropertyInitial
import kotlinx.kover.reporter.parsing.SimpleFunctionInitial
import kotlinx.kover.reporter.report.formatDescriptor
import kotlinx.kover.reporter.utils.JvmNames
import kotlinx.kover.reporter.utils.JvmNames.CONSTRUCTOR_NAME
import kotlinx.kover.reporter.utils.JvmNames.CONSTRUCTOR_NAME_IN_LOCAL_FUN
import kotlinx.kover.reporter.utils.JvmNames.getterNameInLocalFun
import kotlinx.kover.reporter.utils.JvmNames.setterNameInLocalFun
import kotlinx.kover.reporter.utils.toJvmInternalName
import kotlin.collections.get
import kotlin.collections.plusAssign

internal object Composer {

    fun compose(fileInitial: FileInitial): SourceFile {
        val facadeClass = if (fileInitial.facadeClassName != null) {
            fileInitial.compose()
        } else {
            null
        }

        val localClasses = fileInitial.localClasses.map { initial -> initial.compose() }
        val simpleClasses = fileInitial.classes.map { initial -> initial.compose() }

        val allClasses = extractAllClasses(localClasses, simpleClasses, facadeClass)

        fillLocalClasses(allClasses, localClasses)
        fillNestedClasses(simpleClasses)
        fillDefaultImpls(simpleClasses, fileInitial)
        fillCompanions(simpleClasses)
        fillDefaultParameters(allClasses)

        return SourceFile(fileInitial.path, findTopLevelClasses(simpleClasses), facadeClass)
    }

    private fun findTopLevelClasses(simpleClasses: List<SimpleClass>): List<SimpleClass> =
        simpleClasses.filterNot { it.kotlinName.relativeName.contains('.') }

    private fun fillDefaultParameters(allClasses: MutableList<ClassLike>) {
        allClasses.forEach { classLike ->
            fun fillDefs(function: Function) {
                function.localFunctions.forEach { localFunction -> fillDefs(localFunction) }

                if (function is AnonymousFunction || function.localFunctions.isEmpty()) return

                val defaultFunction = if (function is SimpleFunction && function.isConstructor) {
                    function.findDefaultValuesForConstructor()
                } else {
                    function.findDefaultValuesForFunction()
                }
                if (defaultFunction == null) return
                val def = AnonymousFunction(defaultFunction.jvmSignature, defaultFunction.lineNumbers).also {
                    it.localFunctions += defaultFunction.localFunctions
                    it.localClasses += defaultFunction.localClasses
                }
                if (function is SimpleFunction) {
                    function.defaultValues = def
                } else if (function is LocalFunction) {
                    function.defaultValues = def
                }
                function.localFunctions.remove(defaultFunction)
            }
            classLike.functions.forEach { function ->
                fillDefs(function)
            }
            classLike.properties.forEach { property ->
                property.getter?.also { fillDefs(it) }
                property.setter?.also { fillDefs(it) }
            }
        }
    }

    private fun fillCompanions(simpleClasses: List<SimpleClass>) {
        simpleClasses.forEach { simpleClass ->
            if (simpleClass.companionName == null) return@forEach
            val companionName = simpleClass.kotlinName.relativeName + "." + simpleClass.companionName
            val companion = simpleClass.nested.firstOrNull { it.kotlinName.relativeName == companionName }
                ?: throw IllegalStateException("Companion class $companionName not found for ${simpleClass.kotlinName}")

            simpleClass.nested.remove(companion)
            simpleClass.companion = companion
        }
    }

    private fun fillNestedClasses(simpleClasses: List<SimpleClass>) {
        val classBySegments = simpleClasses.associateBy { it.kotlinName.relativeName.split('.') }
        classBySegments.asSequence()
            .sortedBy { it.key.size }
            .forEach { (nameSegments, simpleClass) ->
                if (nameSegments.size == 1) return@forEach
                val parentClass = nameSegments.dropLast(1)
                classBySegments[parentClass]?.nested?.add(simpleClass)
                    ?: throw IllegalStateException("Parent class not found for ${simpleClass.kotlinName}")
            }
    }

    private fun LocalClassInitial.compose(): ClassInFunction {
        val properties = properties.map { property -> property.compose() }

        val simpleFunctions = mutableListOf<SimpleFunction>()
        simpleFunctions += functions.map { it.compose(false) }
        simpleFunctions += constructors.map { it.compose(true) }

        val forLocal = collectFunctionsForLocal(simpleFunctions, properties)
        forLocal.addLocalFunctions(localFunctions)

        val result = when {
            isLambda -> {
                LambdaClass(
                    jvmName,
                    outerClass,
                    outerMethodSignature
                )
            }

            isAnonymous -> {
                // anonymous classes have numeric names
                AnonymousClass(
                    jvmName,
                    outerClass,
                    outerMethodSignature
                )
            }

            else -> {
                LocalClass(
                    jvmName.lastSegment(),
                    jvmName,
                    outerClass,
                    outerMethodSignature
                )
            }
        }

        result.functions += simpleFunctions
        result.properties += properties
        return result
    }

    private fun KotlinClassInitial.compose(): SimpleClass {
        val properties = properties.map { property -> property.compose() }

        val simpleFunctions = mutableListOf<SimpleFunction>()
        simpleFunctions += functions.map { it.compose(false) }
        simpleFunctions += constructors.map { it.compose(true) }

        val forLocal = collectFunctionsForLocal(simpleFunctions, properties)
        forLocal.addLocalFunctions(localFunctions)

        return SimpleClass(
            kotlinName,
            jvmName,
            isObject,
            isData,
            isInterfaceWithDefaultImpls,
            companionName
        ).also { simpleClass ->
            simpleClass.functions += simpleFunctions
            simpleClass.properties += properties
        }
    }

    private fun extractAllClasses(
        localClasses: List<ClassInFunction>,
        simpleClasses: List<SimpleClass>,
        facadeClass: FileFacade?
    ): MutableList<ClassLike> {
        val allClasses = mutableListOf<ClassLike>()
        allClasses += localClasses
        allClasses += simpleClasses
        facadeClass?.also { allClasses += it }
        simpleClasses.forEach { simpleClass ->
            simpleClass.defaultImpls?.also { defaultImpls -> allClasses += defaultImpls }
        }
        return allClasses
    }

    private fun fillLocalClasses(
        classes: List<ClassLike>,
        localClasses: List<ClassInFunction>
    ) {
        val localClassByOuterName = localClasses.groupBy { it.outerClass }.toMutableMap()
        classes.forEach { classLike ->
            localClassByOuterName.remove(classLike.jvmName.toJvmInternalName())?.also { localClasses ->
                classLike.addLocalClasses(localClasses)
            }
        }
        if (localClassByOuterName.isNotEmpty()) throw IllegalStateException("Some local classes are missing")
    }

    private fun fillDefaultImpls(
        simpleClasses: List<SimpleClass>,
        fileInitial: FileInitial
    ) {
        val simpleClassByJvmName = simpleClasses.associateBy { it.jvmName.toJvmInternalName() }
        fileInitial.defImpls.forEach { defImpl ->
            val resultClass = SimpleClass(defImpl.jvmName, defImpl.jvmName, false, false, false, null)
            val outerName = defImpl.jvmName.toJvmInternalName().removeSuffix("$${JvmNames.DEFAULT_IMPLS_CLASS_NAME}")
            val originalInterface = simpleClassByJvmName[outerName]
                ?: throw IllegalStateException("DefImpl class not found for ${defImpl.jvmName}")

            originalInterface.functions.forEach { interfaceFunction ->
                val searchedName = interfaceFunction.jvmSignature.toDefaultImpls(originalInterface.jvmName)
                defImpl.functions
                    .firstOrNull { functionInDefImpls -> functionInDefImpls.jvmSignature == searchedName }
                    ?.also { functionInDefImpls ->
                        resultClass.functions += functionInDefImpls.composeForDefImpls(interfaceFunction)
                    }
            }
            originalInterface.properties.forEach { interfaceProperty ->
                val getter = interfaceProperty.getter?.jvmSignature?.toDefaultImpls(originalInterface.jvmName)
                val setter = interfaceProperty.setter?.jvmSignature?.toDefaultImpls(originalInterface.jvmName)

                val setterDefImpls =
                    defImpl.functions.firstOrNull { it.jvmSignature == getter }?.composeAccessorForDefImpls()
                val getterDefImpls =
                    defImpl.functions.firstOrNull { it.jvmSignature == setter }?.composeAccessorForDefImpls()

                resultClass.properties += Property(
                    interfaceProperty.name,
                    interfaceProperty.isVar,
                    interfaceProperty.customGetter,
                    interfaceProperty.customSetter,
                    getterDefImpls,
                    setterDefImpls
                )
            }

            originalInterface.defaultImpls = resultClass
        }
    }

    private fun AnonymousFunctionInitial.composeForDefImpls(interfaceFunction: SimpleFunction): SimpleFunction {
        return SimpleFunction(
            interfaceFunction.kotlinName,
            interfaceFunction.isConstructor,
            null,
            jvmSignature,
            lineNumbers
        )
    }

    private fun AnonymousFunctionInitial.composeAccessorForDefImpls(): AnonymousFunction {
        return AnonymousFunction(jvmSignature, lineNumbers)
    }

    private fun FileInitial.compose(): FileFacade {
        val properties = properties.map { property -> property.compose() }
        val functions = functions.map { it.compose(false) }
        val initFunction = initFunction?.let { AnonymousFunction(it.jvmSignature, it.lineNumbers) }
        // TODO local
        val forLocal = collectFunctionsForLocal(functions, properties)
        forLocal.addLocalFunctions(localFunctions)

        return FileFacade(facadeClassName!!, initFunction).also { facade ->
            facade.functions += functions
            facade.properties += properties
        }
    }

    private fun PropertyInitial.compose(): Property {
        val getter = getter?.let { AnonymousFunction(it.jvmSignature, it.lineNumbers) }
        val setter = setter?.let { AnonymousFunction(it.jvmSignature, it.lineNumbers) }
        return Property(name, isVar, customGetter, customSetter, getter, setter)
    }

    private fun SimpleFunctionInitial.compose(isConstructor: Boolean): SimpleFunction =
        SimpleFunction(name, isConstructor, formatDescriptor(), jvmSignature, lineNumbers)

    private fun Map<String, List<Function>>.addLocalFunctions(localFunctionInitials: MutableList<AnonymousFunctionInitial>) {
        val functionByJvmName: MutableMap<String, MutableList<Function>> = mutableMapOf()
        forEach { (name, function) ->
            functionByJvmName
                .getOrPut(name) { mutableListOf() }
                .addAll(function)
        }

        localFunctionInitials.asSequence()
            .sortedBy { it.jvmSignature.name.length }
            .forEach { localFunctionInitial ->
                val outerFunction = functionByJvmName.findOuterFunctionForLocal(localFunctionInitial)

                if (localFunctionInitial.isNamedLikeLambda()) {
                    // real lambda
                    val kotlinLambda = AnonymousFunction(
                        localFunctionInitial.jvmSignature,
                        localFunctionInitial.lineNumbers
                    )
                    outerFunction.localFunctions += kotlinLambda
                    functionByJvmName.getOrPut(localFunctionInitial.jvmSignature.name) { mutableListOf() }
                        .add(kotlinLambda)
                } else {
                    // local function, it's acceptable to use '$' in function name
                    val kotlinLocalFunction = LocalFunction(
                        localFunctionInitial.jvmSignature.name.removePrefix(outerFunction.jvmSignature.name + "$"),
                        localFunctionInitial.jvmSignature,
                        localFunctionInitial.lineNumbers
                    )
                    outerFunction.localFunctions += kotlinLocalFunction
                    functionByJvmName.getOrPut(localFunctionInitial.jvmSignature.name) { mutableListOf() }
                        .add(kotlinLocalFunction)
                }
            }
    }

    private fun Map<String, List<Function>>.findOuterFunctionForLocal(localFunctionInitial: AnonymousFunctionInitial): Function {
        val candidates = findCandidatesForLocal(localFunctionInitial)

        if (candidates.isEmpty()) throw IllegalStateException(
            "Can't locate outer function for local function ${localFunctionInitial.jvmSignature}"
        )
        val outerFunction = findOuterFunction(localFunctionInitial, candidates)
        if (outerFunction == null) throw IllegalStateException() // TODO
        return outerFunction
    }

    private fun Map<String, List<Function>>.findCandidatesForLocal(localFunctionInitial: AnonymousFunctionInitial): List<Function> {
        val localName = localFunctionInitial.jvmSignature.name
        if (localName == CONSTRUCTOR_NAME) {
            // if local function is named as <init> this means that it is a default function
            this[CONSTRUCTOR_NAME_IN_LOCAL_FUN]?.also { return it }
        } else if (localFunctionInitial.isNamedLikeLambda()) {
            // if the name is like a lambda, then we are trying to find base method
            // for function a() lambda is called like a$lambda$1, for a$b is a$b$lambda$1
            this[localName.substringBeforeLast('$').substringBeforeLast('$')]?.also { return it }
        }

        // if this is not a lambda or a default value, then trying to find the longest nearest function name
        entries
            .sortedByDescending { it.key.length }
            .forEach { (name, functions) ->
                if (localName != name && localName.startsWith(name)) return functions
            }

        return emptyList()
    }

    private fun collectFunctionsForLocal(
        functions: List<SimpleFunction>,
        properties: List<Property>
    ): Map<String, List<Function>> {
        val result = mutableMapOf<String, MutableList<Function>>()
        functions.forEach { function ->
            result
                .getOrPut(function.kotlinName.constructorNameForLocalClass()) { mutableListOf() }
                .add(function)
        }
        properties.forEach { property ->
            property.getter?.also {
                result
                    .getOrPut(getterNameInLocalFun(property.name)) { mutableListOf() }
                    .add(it)
            }
            property.setter?.also {
                result
                    .getOrPut(setterNameInLocalFun(property.name)) { mutableListOf() }
                    .add(it)
            }
        }

        return result
    }

    private fun String.constructorNameForLocalClass(): String {
        return if (this == CONSTRUCTOR_NAME) {
            CONSTRUCTOR_NAME_IN_LOCAL_FUN
        } else {
            this
        }
    }


    private fun ClassLike.addLocalClasses(localClassInitials: List<ClassInFunction>) {
        fun collectFunctions(function: Function): List<Function> {
            val result = mutableListOf<Function>()
            result += function
            result += function.localFunctions.flatMap { collectFunctions(it) }
            return result
        }

        val allFunctions = functions.flatMap { collectFunctions(it) }.toMutableList()
        properties.forEach { property ->
            property.getter?.also { allFunctions += it }
            property.setter?.also { allFunctions += it }
        }
        if (this is FileFacade) {
            initFunction?.also { allFunctions += it }
        }

        val functionBySignature = allFunctions.associateBy { it.jvmSignature }

        localClassInitials.forEach { localClass ->
            val function = if (this !is FileFacade || localClass.outerMethod != null) {
                functionBySignature[localClass.outerMethod] ?: throw IllegalStateException("Local class ")
            } else {
                initFunction ?: throw IllegalStateException("No init class ")
            }

            function.localClasses += localClass
        }
    }

}
