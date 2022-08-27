/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover

import kotlinx.kover.api.*
import kotlinx.kover.api.KoverNames.CONFIGURATION_AGGREGATE_NAME
import kotlinx.kover.api.KoverNames.CONFIGURATION_BINARY_REPORT_CONSUMER_FILES_NAME
import kotlinx.kover.api.KoverNames.CONFIGURATION_BINARY_REPORT_PRODUCER_FILES_NAME
import kotlinx.kover.api.KoverNames.CONFIGURATION_SOURCE_FILES_CONSUMER_NAME
import kotlinx.kover.api.KoverNames.CONFIGURATION_SOURCE_FILES_PRODUCER_NAME
import kotlinx.kover.appliers.KoverProjectApplier
import kotlinx.kover.appliers.applyMerged
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

class KoverPlugin @Inject constructor(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
) : Plugin<Project> {
    override fun apply(target: Project) {

        val koverAggregateConf = target.configurations.registerKoverAggregateBaseProducer()
        val sourceFilesProducer = target.configurations.createKoverSourceFilesProducer(koverAggregateConf)
        val sourceFilesConsumer = target.configurations.registerKoverSourceFilesConsumer()
        val binaryReportFilesProducer = target.configurations.createKoverBinaryReportFilesProducer(koverAggregateConf)
        val binaryReportFilesConsumer = target.configurations.registerKoverBinaryReportFilesConsumer()

        val koverProjectApplier = KoverProjectApplier(
            objects,
            providers,
            sourceFilesConsumer,
            binaryReportFilesConsumer,
        )
        koverProjectApplier.applyToProject(target)


        target.applyMerged(
            sourceFilesProducer,
            binaryReportFilesProducer,
        )
    }

    /**
     * Create the [Configuration] that can be used in build scripts to aggregate from other projects.
     *
     * ```
     * dependencies {
     *   koverAggregate(":services")
     *   koverAggregate(":api")
     *   koverAggregate(":model")
     * }
     * ```
     *
     * Both [createKoverSourceFilesProducer] and [createKoverBinaryReportFilesProducer] extend from
     * this configuration, meaning that this configuration (when resolved) can also be used to
     * retrieve source and binary report files.
     */
    private fun ConfigurationContainer.registerKoverAggregateBaseProducer(): Configuration =
        create(CONFIGURATION_AGGREGATE_NAME) {
            description = "A resolvable configuration to collect Kover report information from projects"
            isProducer()
            isVisible = false
            attributes { koverAggregate(objects) }
        }

    /** Register the [Configuration] that will provide source files from this project to consuming projects. */
    private fun ConfigurationContainer.createKoverSourceFilesProducer(
        koverAggregateConf: Configuration
    ): Configuration =
        create(CONFIGURATION_SOURCE_FILES_PRODUCER_NAME) {
            description = "A resolvable configuration to collect Kover sources files"
            isProducer()
            isVisible = false
            extendsFrom(koverAggregateConf)
            attributes { koverSourceFiles(objects) }
        }

    /** Create a consumer that will consume source files from  */
    private fun ConfigurationContainer.registerKoverSourceFilesConsumer(): NamedDomainObjectProvider<Configuration> =
        register(CONFIGURATION_SOURCE_FILES_CONSUMER_NAME) {
            description = "A resolvable configuration to collect Kover sources files"
            isConsumer()
            isVisible = false
            attributes { koverSourceFiles(objects) }
        }

    /** Register the [Configuration] that will provide binary report files from this project to consuming projects. */
    private fun ConfigurationContainer.createKoverBinaryReportFilesProducer(
        koverAggregateConf: Configuration
    ): Configuration =
        create(CONFIGURATION_BINARY_REPORT_PRODUCER_FILES_NAME) {
            description = "A resolvable configuration to collect Kover binary report files"
            isProducer()
            isVisible = false
            extendsFrom(koverAggregateConf)
            attributes { koverBinaryReport(objects) }
        }

    private fun ConfigurationContainer.registerKoverBinaryReportFilesConsumer(): NamedDomainObjectProvider<Configuration> =
        register(CONFIGURATION_BINARY_REPORT_CONSUMER_FILES_NAME) {
            description = "A resolvable configuration to collect Kover binary report files"
            isConsumer()
            isVisible = false
            attributes { koverBinaryReport(objects) }
        }
}
