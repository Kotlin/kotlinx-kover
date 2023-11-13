/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

import org.gradle.api.GradleException

internal class KoverCriticalException(message: String, cause: Throwable? = null): GradleException("Kover error: $message\nPlease create an issue in Kover Gradle Plugin repository: https://github.com/Kotlin/kotlinx-kover/issues", cause)

internal open class KoverIllegalConfigException(message: String): GradleException(message)

internal class KoverDeprecationException(message: String): KoverIllegalConfigException(message)

internal class KoverVerificationException(violations: String): GradleException(violations)
