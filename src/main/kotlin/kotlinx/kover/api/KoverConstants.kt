package kotlinx.kover.api

public object KoverNames {
    public const val CHECK_TASK_NAME = "check"
    public const val VERIFICATION_GROUP = "verification"

    public const val ROOT_EXTENSION_NAME = "kover"
    public const val TASK_EXTENSION_NAME = "kover"

    public const val XML_REPORT_TASK_NAME = "koverXmlReport"
    public const val HTML_REPORT_TASK_NAME = "koverHtmlReport"
    public const val REPORT_TASK_NAME = "koverReport"
    public const val VERIFY_TASK_NAME = "koverVerify"

    public const val XML_PROJECT_REPORT_TASK_NAME = "koverXmlProjectReport"
    public const val HTML_PROJECT_REPORT_TASK_NAME = "koverHtmlProjectReport"
    public const val PROJECT_REPORT_TASK_NAME = "koverProjectReport"
    public const val COLLECT_PROJECT_REPORTS_TASK_NAME = "koverCollectProjectsReports"
    public const val PROJECT_VERIFY_TASK_NAME = "koverProjectVerify"
}

public object KoverPaths {
    public const val HTML_AGG_REPORT_DEFAULT_PATH = "reports/kover/html"
    public const val XML_AGG_REPORT_DEFAULT_PATH = "reports/kover/report.xml"

    public const val HTML_PROJECT_REPORT_DEFAULT_PATH = "reports/kover/project-html"
    public const val XML_PROJECT_REPORT_DEFAULT_PATH = "reports/kover/project-xml/report.xml"

    public const val ALL_PROJECTS_REPORTS_DEFAULT_PATH = "reports/kover/projects"

}
