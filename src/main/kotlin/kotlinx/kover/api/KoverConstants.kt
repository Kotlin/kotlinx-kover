package kotlinx.kover.api

public object KoverNames {
    public const val CHECK_TASK_NAME = "check"
    public const val VERIFICATION_GROUP = "verification"

    public const val ROOT_EXTENSION_NAME = "kover"
    public const val TASK_EXTENSION_NAME = "kover"

    public const val MERGED_XML_REPORT_TASK_NAME = "koverMergedXmlReport"
    public const val MERGED_HTML_REPORT_TASK_NAME = "koverMergedHtmlReport"
    public const val MERGED_REPORT_TASK_NAME = "koverMergedReport"
    public const val MERGED_VERIFY_TASK_NAME = "koverMergedVerify"

    public const val XML_REPORT_TASK_NAME = "koverXmlReport"
    public const val HTML_REPORT_TASK_NAME = "koverHtmlReport"
    public const val REPORT_TASK_NAME = "koverReport"
    public const val COLLECT_REPORTS_TASK_NAME = "koverCollectReports"
    public const val VERIFY_TASK_NAME = "koverVerify"
}

public object KoverPaths {
    public const val MERGED_HTML_REPORT_DEFAULT_PATH = "reports/kover/html"
    public const val MERGED_XML_REPORT_DEFAULT_PATH = "reports/kover/report.xml"

    public const val PROJECT_HTML_REPORT_DEFAULT_PATH = "reports/kover/project-html"
    public const val PROJECT_XML_REPORT_DEFAULT_PATH = "reports/kover/project-xml/report.xml"

    public const val ALL_PROJECTS_REPORTS_DEFAULT_PATH = "reports/kover/projects"

}
