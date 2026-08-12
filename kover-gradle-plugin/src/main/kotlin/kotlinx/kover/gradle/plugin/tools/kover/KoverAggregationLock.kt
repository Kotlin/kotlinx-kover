/*
 * Copyright 2017-2026 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

/**
 * Monitor serializing every entry into the intellij-coverage aggregator.
 *
 * Unlike the JaCoCo tool, which submits its work through `WorkerExecutor.classLoaderIsolation`, the
 * Kover tool calls [kotlinx.kover.features.jvm.KoverLegacyFeatures] directly on the task's thread.
 * The aggregator keeps process-wide mutable state - `ProjectData.ourProjectData` and the mutable
 * `OptionsUtil` flags - and all projects' report tasks share one classloader in the daemon, so two
 * of them aggregating at the same time read and write the same statics.
 *
 * Gradle runs tasks of different projects concurrently whenever the configuration cache is enabled,
 * regardless of `org.gradle.parallel`, so this is reachable in any multi-project build. The
 * observable result is a report or verification verdict computed with another project's filters or
 * coverage data.
 */
internal object KoverAggregationLock
