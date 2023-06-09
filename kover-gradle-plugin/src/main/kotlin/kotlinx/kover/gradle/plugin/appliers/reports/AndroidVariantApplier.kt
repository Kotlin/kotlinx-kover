package kotlinx.kover.gradle.plugin.appliers.reports

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.AndroidVariantCompilationKit
import kotlinx.kover.gradle.plugin.commons.BuildTypeAttr
import kotlinx.kover.gradle.plugin.commons.ProductFlavorAttr
import kotlinx.kover.gradle.plugin.commons.ReportsVariantType
import kotlinx.kover.gradle.plugin.dsl.internal.KoverReportsConfigImpl
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.*
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

/**
 * Apply plugin for Android build variant: instrument unit-tests, create generation tasks and set of report tasks.
 */
internal class AndroidVariantApplier(
    private val project: Project,
    buildVariantName: String,
    koverDependencies: Configuration,
    reportClasspath: Configuration,
    toolProvider: Provider<CoverageTool>
) : ReportsVariantApplier(
    project,
    buildVariantName,
    ReportsVariantType.ANDROID,
    koverDependencies,
    reportClasspath,
    toolProvider
) {
    fun applyCompilationKit(kit: AndroidVariantCompilationKit) {
        applyCommonCompilationKit(kit)

        config.configure {
            attributes {
                // set android attributes for Kover Android artifact

                attribute(BuildTypeAttr.ATTRIBUTE, project.objects.named(kit.buildType))

                kit.flavors.forEach { flavor ->
                    attribute(ProductFlavorAttr.of(flavor.dimension), project.objects.named(flavor.name))
                }
            }
        }

        dependencies.configure {
            attributes {
                // set attribute requirements
                attribute(BuildTypeAttr.ATTRIBUTE, project.objects.named(kit.buildType))

                kit.missingDimensions.forEach { (dimension, flavorName) ->
                    attribute(ProductFlavorAttr.of(dimension), project.objects.named(flavorName))
                }

                kit.flavors.forEach { flavor ->
                    attribute(ProductFlavorAttr.of(flavor.dimension), project.objects.named(flavor.name))
                }
            }
        }

        val schema = project.dependencies.attributesSchema
        setBuildTypeStrategy(schema, kit.fallbacks.buildTypes)
        setupFlavorStrategy(schema, kit.fallbacks.flavors)
    }
}

internal fun ObjectFactory.androidReports(variant: String, layout: ProjectLayout): KoverReportsConfigImpl {
    val buildDir = layout.buildDirectory
    val reports = newInstance<KoverReportsConfigImpl>(this)

    reports.xml {
        setReportFile(buildDir.file(xmlReportPath(variant)))
        onCheck = false
    }

    reports.html {
        setReportDir(buildDir.dir(htmlReportPath(variant)))
        onCheck = false
    }

    reports.verify {
        onCheck = false
    }

    return reports
}

internal fun Project.androidReports(variant: String): KoverReportsConfigImpl {
    return objects.androidReports(variant, layout)
}


/*
Sources taken from Android Gradle Plugin with minor changes.

see working with build variants https://developer.android.com/studio/build/build-variants
 */

private fun setBuildTypeStrategy(
    schema: AttributesSchema,
    alternateMap: Map<String, List<String>>
) {
    if (alternateMap.isNotEmpty()) {
        val buildTypeStrategy = schema.attribute(BuildTypeAttr.ATTRIBUTE)

        buildTypeStrategy
            .compatibilityRules
            .add(AlternateCompatibilityRule.BuildTypeRule::class.java) {
                setParams(alternateMap)
            }
        buildTypeStrategy
            .disambiguationRules
            .add(AlternateDisambiguationRule.BuildTypeRule::class.java) {
                setParams(alternateMap)
            }
    }
}

private fun setupFlavorStrategy(
    schema: AttributesSchema,
    flavorFallbacks: Map<String, Map<String, List<String>>>
) {
    // now that we know we have all the fallbacks for each dimensions, we can create the
    // rule instances.
    for ((dimension, alternateMap) in flavorFallbacks) {
        val attr = ProductFlavorAttr.of(dimension)
        val flavorStrategy = schema.attribute(attr)
        flavorStrategy
            .compatibilityRules
            .add(AlternateCompatibilityRule.ProductFlavorRule::class.java) {
                setParams(alternateMap)
            }
        flavorStrategy
            .disambiguationRules
            .add(AlternateDisambiguationRule.ProductFlavorRule::class.java) {
                setParams(alternateMap)
            }
    }
}


/** alternate-based Compat rule to handle the different values of attributes.  */
internal open class AlternateCompatibilityRule<T : Named>
protected constructor(private val alternates: Map<String, List<String>>) : AttributeCompatibilityRule<T> {
    override fun execute(details: CompatibilityCheckDetails<T>) {
        val producerValue: T = details.producerValue!!
        val consumerValue: T = details.consumerValue!!
        if (producerValue == consumerValue) {
            details.compatible()
        } else {
            alternates[consumerValue.name]?.let { alternatesForValue ->
                if (alternatesForValue.contains(producerValue.name)) {
                    details.compatible()
                }
            }
        }
    }

    class BuildTypeRule @Inject constructor(alternates: Map<String, List<String>>) :
        AlternateCompatibilityRule<BuildTypeAttr>(alternates)

    class ProductFlavorRule @Inject constructor(alternates: Map<String, List<String>>) :
        AlternateCompatibilityRule<ProductFlavorAttr>(alternates)
}

/** alternate-based Disambiguation rule to handle the different values of attributes.  */
internal open class AlternateDisambiguationRule<T : Named>
protected constructor(
    /** Sorted alternates from high to low priority, associated to a requested value.  */
    private val alternates: Map<String, List<String>>
) : AttributeDisambiguationRule<T> {
    override fun execute(details: MultipleCandidatesDetails<T>) {
        val consumerValue: T = details.consumerValue ?: return
        val alternatesForValue = alternates[consumerValue.name] ?: return
        val candidates: Set<T> = details.candidateValues
        if (candidates.contains(consumerValue)) {
            details.closestMatch(consumerValue)
        } else if (alternatesForValue.size == 1) {
            val fallback = alternatesForValue[0]
            // quick optim for single alternate
            for (candidate in candidates) {
                if (candidate.name == fallback) {
                    details.closestMatch(candidate)
                    return
                }
            }
        } else {
            // build a map to go from name->T
            val map: MutableMap<String, T> = HashMap(candidates.size)
            for (candidate in candidates) {
                map[candidate.name] = candidate
            }

            // then go through the alternates and pick the first one
            for (fallback in alternatesForValue) {
                val candidate = map[fallback]
                if (candidate != null) {
                    details.closestMatch(candidate)
                    return
                }
            }
        }
    }

    class BuildTypeRule @Inject constructor(alternates: Map<String, List<String>>) :
        AlternateDisambiguationRule<BuildTypeAttr>(alternates)

    class ProductFlavorRule @Inject constructor(alternates: Map<String, List<String>>) :
        AlternateDisambiguationRule<ProductFlavorAttr>(alternates)
}
