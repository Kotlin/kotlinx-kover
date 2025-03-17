package kotlinx.kover.reporter.report

import kotlinx.kover.reporter.composer.AnonymousClass
import kotlinx.kover.reporter.composer.AnonymousFunction
import kotlinx.kover.reporter.composer.Composer
import kotlinx.kover.reporter.composer.LambdaClass
import kotlinx.kover.reporter.composer.LocalClass
import kotlinx.kover.reporter.composer.LocalFunction
import kotlinx.kover.reporter.composer.Property
import kotlinx.kover.reporter.composer.SimpleClass
import kotlinx.kover.reporter.composer.SimpleFunction
import kotlinx.kover.reporter.composer.SourceFile
import kotlinx.kover.reporter.parsing.Parser
import kotlinx.kover.reporter.utils.toJvmInternalName
import java.io.File

class Reporter {
    fun report(classpath: List<File>, sourceDirs: List<File>, rootDir: File): JvmCoverageReport {
        val files = Parser.parseClasspath(classpath, sourceDirs, rootDir)
            .files
            .map { Composer.compose(it.value) }
            .map { it.report() }

        return JvmCoverageReport(files)
    }

    internal fun SourceFile.report(): FileCoverage {
        val reportClasses = classes
            .filter { !it.isObject }
            .map { it.report() }

        val reportObjects = classes
            .filter { it.isObject }
            .map { it.reportObject() }

        val reportProperties = facade?.properties?.map { it.report() } ?: emptyList()
        val reportFunctions = facade?.functions?.map { it.report() } ?: emptyList()
        val reportAnonymousFunction = facade?.initFunction?.report()

        return FileCoverage(
            path,
            reportClasses,
            reportFunctions,
            reportProperties,
            reportObjects,
            reportAnonymousFunction
        )
    }

    internal fun SimpleClass.report(): ClassCoverage {
        return ClassCoverage(
            kotlinName.toString(),
            jvmName.toJvmInternalName(),
            functions.filter { !it.isConstructor }.map { it.report() },
            functions.filter { it.isConstructor }.map { it.report() },
            properties.map { it.report() },
            nested.filter { !it.isObject }.map { it.report() },
            nested.filter { it.isObject }.map { it.reportObject() },
            isData,
            companion?.reportObject(),
            defaultImpls?.reportAnonymous()
        )
    }

    internal fun SimpleClass.reportObject(): NamedObjectCoverage {
        return NamedObjectCoverage(
            kotlinName.lastSegment(),
            jvmName.toJvmInternalName(),
            functions.filter { !it.isConstructor }.map { it.report() },
            functions.filter { it.isConstructor }.map { it.report() },
            properties.map { it.report() },
            nested.filter { !it.isObject }.map { it.report() },
            nested.filter { it.isObject }.map { it.reportObject() },
            isData
        )
    }

    internal fun SimpleClass.reportAnonymous(): AnonymousClassCoverage {
        return AnonymousClassCoverage(
            jvmName.toJvmInternalName(),
            functions.filter { !it.isConstructor }.map { it.report() },
            functions.filter { it.isConstructor }.map { it.report() },
            properties.map { it.report() }
        )
    }

    internal fun LocalClass.report(): LocalClassCoverage {
        return LocalClassCoverage(
            kotlinName,
            jvmName.toJvmInternalName(),
            functions.filter { !it.isConstructor }.map { it.report() },
            functions.filter { it.isConstructor }.map { it.report() },
            properties.map { it.report() }
        )
    }

    internal fun LambdaClass.report(): AnonymousClassCoverage {
        return AnonymousClassCoverage(
            jvmName.toJvmInternalName(),
            functions.map { it.report() },
            emptyList(),
            properties.map { it.report() }
        )
    }

    internal fun AnonymousClass.report(): AnonymousClassCoverage {
        return AnonymousClassCoverage(
            jvmName.toJvmInternalName(),
            functions.filter { !it.isConstructor }.map { it.report() },
            functions.filter { it.isConstructor }.map { it.report() },
            properties.map { it.report() }
        )
    }

    internal fun Property.report(): PropertyCoverage {
        return PropertyCoverage(name, customGetter, customSetter, getter?.report(), setter?.report())
    }

    internal fun SimpleFunction.report(): FunctionCoverage {
        val functionsReport = localFunctions
            .filterIsInstance<LocalFunction>()
            .map { it.report() }

        val anonymousFunReport = localFunctions
            .filterIsInstance<AnonymousFunction>()
            .map { it.report() }

        val classesReport = localClasses
            .filterIsInstance<LocalClass>()
            .map { it.report() }

        val lambdasReport = localClasses
            .filterIsInstance<LambdaClass>()
            .map { it.report() }

        val anonymousReport = localClasses
            .filterIsInstance<AnonymousClass>()
            .map { it.report() }

        return FunctionCoverage(
            kotlinName,
            descriptor,
            jvmSignature.toString(),
            lineNumbers.linesReport(),
            defaultValues?.report(),
            functionsReport,
            classesReport,
            lambdasReport,
            anonymousFunReport,
            anonymousReport
        )
    }

    internal fun LocalFunction.report(): FunctionCoverage {
        val functionsReport = localFunctions
            .filterIsInstance<LocalFunction>()
            .map { it.report() }

        val anonymousFunReport = localFunctions
            .filterIsInstance<AnonymousFunction>()
            .map { it.report() }

        val classesReport = localClasses
            .filterIsInstance<LocalClass>()
            .map { it.report() }

        val lambdasReport = localClasses
            .filterIsInstance<LambdaClass>()
            .map { it.report() }

        val anonymousReport = localClasses
            .filterIsInstance<AnonymousClass>()
            .map { it.report() }

        return FunctionCoverage(
            kotlinName,
            null,
            jvmSignature.toString(),
            lineNumbers.linesReport(),
            defaultValues?.report(),
            functionsReport,
            classesReport,
            lambdasReport,
            anonymousFunReport,
            anonymousReport
        )
    }


    internal fun AnonymousFunction.report(): AnonymousFunctionCoverage {
        val functionsReport = localFunctions
            .filterIsInstance<LocalFunction>()
            .map { it.report() }

        val anonymousFunReport = localFunctions
            .filterIsInstance<AnonymousFunction>()
            .map { it.report() }

        val classesReport = localClasses
            .filterIsInstance<LocalClass>()
            .map { it.report() }

        val lambdasReport = localClasses
            .filterIsInstance<LambdaClass>()
            .map { it.report() }

        val anonymousReport = localClasses
            .filterIsInstance<AnonymousClass>()
            .map { it.report() }

        return AnonymousFunctionCoverage(
            jvmSignature.toString(),
            lineNumbers.linesReport(),
            functionsReport,
            classesReport,
            lambdasReport,
            anonymousFunReport,
            anonymousReport
        )
    }

    internal fun List<Int>.linesReport(): Map<Int, LineCoverage> {
        val result = mutableMapOf<Int, LineCoverage>()
        forEach {
            result[it] = SimpleLineCoverage(0)
        }
        return result
    }

}