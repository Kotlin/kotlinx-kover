/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.jacoco

import kotlinx.kover.engines.commons.*
import org.gradle.api.Task
import java.io.*

internal fun Task.buildJacocoAgentJvmArgs(jarFile: File, reportFile: File, filters: AgentFilters): MutableList<String> {
    val agentArgs = listOfNotNull(
        "destfile=${reportFile.canonicalPath},append=true,inclnolocationclasses=false,dumponexit=true,output=file,jmx=false",
        filters.includesClasses.joinToFilterString("includes"),
        filters.excludesClasses.joinToFilterString("excludes")
    ).joinToString(",")

    return mutableListOf("-javaagent:${jarFile.canonicalPath}=$agentArgs")
}

private fun List<String>.joinToFilterString(name: String): String? {
    if (isEmpty()) return null
    return name + "=" + joinToString(":")
}
