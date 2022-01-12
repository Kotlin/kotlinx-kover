package kotlinx.kover.engines.commons

import java.io.*

internal class Report(val files: List<File>, val projects: List<ProjectInfo>)
internal class ProjectInfo(val sources: Iterable<File>, val outputs: Iterable<File>)
