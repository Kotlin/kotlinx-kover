/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import java.io.*

internal fun buildJvmAgentArgs(jarFile: File, rawReportFile: File, excludedClasses: Set<String>): List<String> {
    val agentArgs = listOfNotNull(
        "destfile=${rawReportFile.canonicalPath},append=true,inclnolocationclasses=false,dumponexit=true,output=file,jmx=false",
        excludedClasses.joinToFilterString("excludes")
    ).joinToString(",")

    return listOf("-javaagent:${jarFile.canonicalPath}=$agentArgs")
}

private fun Collection<String>.joinToFilterString(filterName: String): String? {
    if (isEmpty()) return null
    return filterName + "=" + joinToString(":")
}
