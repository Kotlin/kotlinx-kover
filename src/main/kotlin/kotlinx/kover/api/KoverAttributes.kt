package kotlinx.kover.api

import org.gradle.api.Named
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.named


/**
 * The attributes used to identify a [Configuration] of directories that
 * contain **source** files used to create a Kover report.
 *
 * @see kotlinx.kover.tasks.ProjectFiles.sources
 * @param[objects] required to create [Named] objects
 */
internal fun AttributeContainer.koverAggregate(objects: ObjectFactory) {
    attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlinx.kover.aggregate"))
}


/**
 * The attributes used to identify a [Configuration] of directories that
 * contain **source** files used to create a Kover report.
 *
 * @see kotlinx.kover.tasks.ProjectFiles.sources
 * @param[objects] required to create [Named] objects
 */
internal fun AttributeContainer.koverSourceFiles(objects: ObjectFactory) {
    koverAggregate(objects)
    attribute(Category.CATEGORY_ATTRIBUTE, objects.named("report.source-files"))
}

/**
 * The attributes used to identify a [Configuration] of directories that
 * contain **binary report** files used to create a Kover report.
 *
 * @see kotlinx.kover.tasks.ProjectFiles.binaryReportFiles
 * @param[objects] required to create [Named] objects
 */
internal fun AttributeContainer.koverBinaryReport(objects: ObjectFactory) {
    koverAggregate(objects)
    attribute(Category.CATEGORY_ATTRIBUTE, objects.named("report.binary-report-files"))
}


/**
 * Mark this [Configuration] as a 'producer' that exposes artifacts and their dependencies for consumption by other
 * projects
 *
 * See: https://docs.gradle.org/7.5.1/userguide/declaring_dependencies.html#sec:resolvable-consumable-configs
 */
internal fun Configuration.isProducer() {
    isCanBeResolved = true
    isCanBeConsumed = false
}

/**
 * Mark this [Configuration] as consumable, which means it’s an "exchange" meant for consumers.
 *
 * See: https://docs.gradle.org/7.5.1/userguide/declaring_dependencies.html#sec:resolvable-consumable-configs
 */
internal fun Configuration.isConsumer() {
    isCanBeResolved = false
    isCanBeConsumed = true
}

