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

    public const val XML_MODULE_REPORT_TASK_NAME = "koverXmlModuleReport"
    public const val HTML_MODULE_REPORT_TASK_NAME = "koverHtmlModuleReport"
    public const val MODULE_REPORT_TASK_NAME = "koverModuleReport"
    public const val COLLECT_MODULE_REPORTS_TASK_NAME = "koverCollectModuleReports"
    public const val MODULE_VERIFY_TASK_NAME = "koverModuleVerify"
}

public object KoverPaths {
    public const val HTML_AGG_REPORT_DEFAULT_PATH = "reports/kover/html"
    public const val XML_AGG_REPORT_DEFAULT_PATH = "reports/kover/report.xml"

    public const val HTML_MODULE_REPORT_DEFAULT_PATH = "reports/kover/module-html"
    public const val XML_MODULE_REPORT_DEFAULT_PATH = "reports/kover/module-xml/report.xml"

    public const val ALL_MODULES_REPORTS_DEFAULT_PATH = "reports/kover/modules"

}
