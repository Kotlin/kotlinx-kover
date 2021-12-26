package kotlinx.kover.engines.commons

import java.io.*

internal class Report(val files: List<ReportFiles>, val projects: List<ProjectInfo>)
internal class ReportFiles(val binary: File, val smap: File? = null)
internal class ProjectInfo(val sources: Iterable<File>, val outputs: Iterable<File>)
