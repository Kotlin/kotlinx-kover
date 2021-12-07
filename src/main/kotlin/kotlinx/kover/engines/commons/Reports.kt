package kotlinx.kover.engines.commons

import java.io.*

internal class Report(val files: List<ReportFiles>, val modules: List<ModuleInfo>)
internal class ReportFiles(val binary: File, val smap: File? = null)
internal class ModuleInfo(val sources: Iterable<File>, val outputs: Iterable<File>)
