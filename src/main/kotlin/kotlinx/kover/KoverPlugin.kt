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
import kotlinx.kover.appliers.KoverMergedApplier
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

class KoverPlugin @Inject constructor(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
    private val archiveOperations: ArchiveOperations,
    private val layout: ProjectLayout,
) : Plugin<Project> {
    override fun apply(target: Project) {

        val koverAggregateConf = target.configurations.createKoverAggregateBaseConsumer()

        val sourceFilesConsumer = target.configurations.createKoverSourceFilesConsumer(koverAggregateConf)
        val sourceFilesProducer = target.configurations.registerKoverSourceFilesProducer()

        val binaryReportFilesConsumer = target.configurations.createKoverBinaryReportFilesConsumer(koverAggregateConf)
        val binaryReportFilesProducer = target.configurations.registerKoverBinaryReportFilesProducer()

        KoverProjectApplier(
            objects,
            providers,
            archiveOperations,
            layout,

            sourceFilesProducer,
            binaryReportFilesProducer,
        ).applyToProject(target)

        KoverMergedApplier(
            target,
            archiveOperations,
            sourceFilesConsumer,
            binaryReportFilesConsumer,
        ).execute()
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
     * Both [createKoverSourceFilesConsumer] and [createKoverBinaryReportFilesConsumer] extend from
     * this configuration, meaning that this configuration (when resolved) can also be used to
     * retrieve source and binary report files.
     */
    private fun ConfigurationContainer.createKoverAggregateBaseConsumer(): Configuration =
        create(CONFIGURATION_AGGREGATE_NAME) {
            description = "A resolvable configuration to collect Kover report information from projects"
            asConsumer()
            isVisible = false
            attributes { koverAggregate(objects) }
        }

    /** Register the [Configuration] that will provide source files from this project to consuming projects. */
    private fun ConfigurationContainer.registerKoverSourceFilesProducer(): NamedDomainObjectProvider<Configuration> =
        register(CONFIGURATION_SOURCE_FILES_PRODUCER_NAME) {
            description = "A resolvable configuration to collect Kover sources files"
            asProducer()
            isVisible = false
            attributes { koverSourceFiles(objects) }
        }

    /** Create a consumer that will consume source files from  */
    private fun ConfigurationContainer.createKoverSourceFilesConsumer(
        koverAggregateConf: Configuration
    ): Configuration  =
        create(CONFIGURATION_SOURCE_FILES_CONSUMER_NAME) {
            description = "A resolvable configuration to collect Kover sources files"
            asConsumer()
            isVisible = false
            attributes { koverSourceFiles(objects) }
            extendsFrom(koverAggregateConf)
        }

    /** Register the [Configuration] that will provide binary report files from this project to consuming projects. */
    private fun ConfigurationContainer.registerKoverBinaryReportFilesProducer(): NamedDomainObjectProvider<Configuration> =
        register(CONFIGURATION_BINARY_REPORT_PRODUCER_FILES_NAME) {
            description = "A resolvable configuration to collect Kover binary report files"
            asProducer()
            isVisible = false
            attributes { koverBinaryReport(objects) }
        }

    private fun ConfigurationContainer.createKoverBinaryReportFilesConsumer(
        koverAggregateConf: Configuration
    ): Configuration =
        create(CONFIGURATION_BINARY_REPORT_CONSUMER_FILES_NAME) {
            description = "A resolvable configuration to collect Kover binary report files"
            asConsumer()
            isVisible = false
            attributes { koverBinaryReport(objects) }
            extendsFrom(koverAggregateConf)
        }
}
