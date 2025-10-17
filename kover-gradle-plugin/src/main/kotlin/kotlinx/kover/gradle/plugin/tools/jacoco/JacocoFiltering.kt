/*
 * Copyright 2017-2025 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import kotlinx.kover.features.jvm.KoverFeatures.koverWildcardToRegex
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.analysis.IClassCoverage
import org.jacoco.core.internal.analysis.ClassCoverageImpl
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.File

/**
 * Filter declarations in [this] [CoverageBuilder] by the given [filters].
 *
 * The filtering behavior is exactly the same as for the Kover reporter.
 */
internal fun CoverageBuilder.filter(filters: ReportFilters, classfiles: Collection<File>): CoverageBuilder {
    // No filters - return original builder
    if (filters.excludesClasses.isEmpty()
        && filters.excludesAnnotations.isEmpty()
        && filters.excludeInheritedFrom.isEmpty()
        && filters.includesClasses.isEmpty()
        && filters.includesAnnotations.isEmpty()
        && filters.includeInheritedFrom.isEmpty()
    ) {
        return this
    }

    val regexFilter = filters.toRegex()


    val annotations = mutableMapOf<String, AnnotatedClass>()
    val ancestors = mutableMapOf<String, Set<String>>()
    // if any annotation filter is specified - read annotations
    if (regexFilter.hasAnnotationFilters) {
        classfiles.forEach { classfile ->
            val annotatedClass = readAnnotations(classfile)
            annotations[annotatedClass.name] = annotatedClass
        }
    }
    if (regexFilter.hasInheritedFilters) {
        ancestors += buildAncestors(classes)
    }

    val filteredBuilder = CoverageBuilder()

    classes.forEach { classCoverage ->
        val binaryName = classCoverage.name.toBinaryName()
        val annotatedClass = annotations[binaryName]
        val ancestorsOfClass = ancestors[binaryName] ?: emptySet()

        val classFiltered = classCoverage.filterClass(regexFilter, annotatedClass, ancestorsOfClass)
        if (classFiltered == ClassFilteringResult.EXCLUDED) {
            // skip the excluded class
            return@forEach
        }

        val newClassCoverage = ClassCoverageImpl(classCoverage.name, classCoverage.id, classCoverage.isNoMatch)

        classCoverage.methods.forEach { methodCoverage ->
            val methodAnnotations = annotatedClass?.methods?.get(methodCoverage.name) ?: emptySet()

            if (methodIsIncluded(regexFilter, classFiltered, methodAnnotations)) {
                newClassCoverage.addMethod(methodCoverage)
            }
        }
        newClassCoverage.signature = classCoverage.signature
        newClassCoverage.setInterfaces(classCoverage.interfaceNames)
        newClassCoverage.superName = classCoverage.superName
        newClassCoverage.sourceFileName = classCoverage.sourceFileName
        if (classCoverage is ClassCoverageImpl) {
            newClassCoverage.fragments = classCoverage.fragments
        }

        if (classFiltered == ClassFilteringResult.DEPENDS_ON_MEMBERS && newClassCoverage.methods.isEmpty()) {
            // if any include filter is specified, but neither class nor method included - exclude it from the report
            return@forEach
        }

        filteredBuilder.visitCoverage(newClassCoverage);
    }

    return filteredBuilder
}

private enum class ClassFilteringResult {
    /**
     * Exclude class from a report.
     */
    EXCLUDED,

    /**
     * Include class to a report.
     */
    INCLUDED,

    /**
     * This state means: some inclusion filters are specified, but this class doesn't match them.
     * Class can be included in the report only in case if at least one of its methods matches inclusion filters.
     */
    DEPENDS_ON_MEMBERS
}

/**
 * Determine whether to include this class in the report.
 */
private fun IClassCoverage.filterClass(
    filters: RegexFilter,
    annotated: AnnotatedClass?,
    ancestors: Set<String>
): ClassFilteringResult {
    val binaryName = name.toBinaryName()
    if (filters.excludesClasses.any { regex -> regex.matches(binaryName) }) {
        return ClassFilteringResult.EXCLUDED
    }

    if (filters.excludesAnnotations.isNotEmpty() && annotated != null) {
        annotated.annotations.forEach { annotationName ->
            if (filters.excludesAnnotations.any { regex -> regex.matches(annotationName) }) {
                return ClassFilteringResult.EXCLUDED
            }
        }
    }

    if (filters.excludeInheritedFrom.isNotEmpty()) {
        ancestors.forEach { ancestorName ->
            if (filters.excludeInheritedFrom.any { regex -> regex.matches(ancestorName) }) {
                return ClassFilteringResult.EXCLUDED
            }
        }
    }

    if (!filters.hasAnyInclusionFilter) {
        return ClassFilteringResult.INCLUDED
    }

    var included = true
    if (filters.includesClasses.isNotEmpty()) {
       included = included && filters.includesClasses.any { regex -> regex.matches(binaryName) }
    }
    if (filters.includesAnnotations.isNotEmpty() && annotated != null) {
        included = included && annotated.annotations.any { annotationName ->
            filters.includesAnnotations.any { regex -> regex.matches(annotationName) }
        }
    }
    if (filters.includeInheritedFrom.isNotEmpty()) {
        included = included && ancestors.any { ancestorName ->
            filters.includeInheritedFrom.any { regex -> regex.matches(ancestorName) }
        }
    }

    return if (included) {
        // to be included class should pass all specified types of inclusion filters
        ClassFilteringResult.INCLUDED
    } else {
        ClassFilteringResult.DEPENDS_ON_MEMBERS
    }
}

/**
 * Determine whether to include this method in the report.
 */
private fun methodIsIncluded(
    filters: RegexFilter,
    classFilterResult: ClassFilteringResult,
    annotations: Set<String>
): Boolean {
    if (filters.excludesAnnotations.isNotEmpty()) {
        annotations.forEach { annotationName ->
            if (filters.excludesAnnotations.any { regex -> regex.matches(annotationName) }) {
                return false
            }
        }
    }

    if (filters.includesAnnotations.isNotEmpty()) {
        annotations.forEach { annotationName ->
            if (filters.includesAnnotations.any { regex -> regex.matches(annotationName) }) {
                return true
            }
        }
    }

    // method without annotation can be included to report only if class is included or if it has inclusion annotation (checked above)
    return classFilterResult == ClassFilteringResult.INCLUDED
}

/**
 * Compile filters to regular expressions.
 */
private fun ReportFilters.toRegex(): RegexFilter {
    val excludesClasses = excludesClasses.map { koverWildcardToRegex(it).toRegex() }
    val excludesAnnotations = excludesAnnotations.map { koverWildcardToRegex(it).toRegex() }
    val excludeInheritedFrom = excludeInheritedFrom.map { koverWildcardToRegex(it).toRegex() }
    val includesClasses = includesClasses.map { koverWildcardToRegex(it).toRegex() }
    val includesAnnotations = includesAnnotations.map { koverWildcardToRegex(it).toRegex() }
    val includeInheritedFrom = includeInheritedFrom.map { koverWildcardToRegex(it).toRegex() }
    return RegexFilter(
        includesClasses,
        includesAnnotations,
        includeInheritedFrom,
        excludesClasses,
        excludesAnnotations,
        excludeInheritedFrom
    )
}

/**
 * Filters with precompiled regular expressions.
 */
private class RegexFilter(
    val includesClasses: List<Regex>,
    val includesAnnotations: List<Regex>,
    val includeInheritedFrom: List<Regex>,
    val excludesClasses: List<Regex>,
    val excludesAnnotations: List<Regex>,
    val excludeInheritedFrom: List<Regex>,
) {
    val hasAnyInclusionFilter: Boolean
        get() {
            return includesClasses.isNotEmpty() || includesAnnotations.isNotEmpty() || includeInheritedFrom.isNotEmpty()
        }

    val hasAnnotationFilters: Boolean
        get() {
            return excludesAnnotations.isNotEmpty() || includesAnnotations.isNotEmpty()
        }

    val hasInheritedFilters: Boolean
        get() {
            return excludeInheritedFrom.isNotEmpty() || includeInheritedFrom.isNotEmpty()
        }
}

/**
 * Set of annotations for [name] class.
 */
private class AnnotatedClass(val name: String, val annotations: Set<String>, val methods: Map<String, Set<String>>)

/**
 * Collect annotations for class from [classfile].
 */
private fun readAnnotations(classfile: File): AnnotatedClass {
    val classNode = ClassNode()
    ClassReader(classfile.readBytes()).accept(classNode, ClassReader.SKIP_CODE)

    val annotations = mutableSetOf<String>()
    annotations += classNode.visibleAnnotations?.map { it.desc.toBinaryName() } ?: emptyList()
    annotations += classNode.invisibleAnnotations?.map { it.desc.toBinaryName() } ?: emptyList()

    val methods = classNode.methods.associate { methodNode ->
        val methodAnnotations = mutableSetOf<String>()
        methodAnnotations += methodNode.visibleAnnotations?.map { it.desc.toBinaryName() } ?: emptyList()
        methodAnnotations += methodNode.invisibleAnnotations?.map { it.desc.toBinaryName() } ?: emptyList()

        methodNode.name.toBinaryName() to methodAnnotations
    }

    return AnnotatedClass(classNode.name.toBinaryName(), annotations, methods)
}

/**
 * Collect all ancestors for classes in [classes].
 * Ancestor is a direct superclass or superinterface, or their superclasses and superinterfaces.
 */
private fun buildAncestors(classes: Collection<IClassCoverage>): Map<String, Set<String>> {
    val classesByName = classes.associateBy { it.name.toBinaryName() }
    val allAncestors = mutableMapOf<String, Set<String>>()

    fun fillAncestorsFor(classCoverage: IClassCoverage) {
        val binaryName = classCoverage.name.toBinaryName()
        allAncestors[binaryName]?.let { return }

        val result = mutableSetOf<String>()

        val directAncestors =
            listOf(classCoverage.superName.toBinaryName()) + classCoverage.interfaceNames.map { it.toBinaryName() }
        directAncestors.forEach { ancestorName ->
            result += ancestorName
            classesByName[ancestorName]?.let { classInProject ->
                fillAncestorsFor(classInProject)
                result += allAncestors.getValue(ancestorName)
            }
        }

        allAncestors[binaryName] = result
    }

    classes.forEach { classCoverage ->
        fillAncestorsFor(classCoverage)
    }

    return allAncestors
}


