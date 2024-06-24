/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.commons.artifacts

import org.gradle.api.attributes.*

internal interface KoverContentAttr {
    companion object {
        val ATTRIBUTE = Attribute.of(
            "kotlinx.kover.content.type",
            String::class.java
        )

        const val AGENT_JAR = "AgentJar"
        const val LOCAL_ARTIFACT = "localArtifact"
    }
}