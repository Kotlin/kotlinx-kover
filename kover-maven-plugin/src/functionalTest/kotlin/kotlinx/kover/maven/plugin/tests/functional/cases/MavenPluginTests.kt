/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.tests.functional.cases

import kotlinx.kover.maven.plugin.tests.functional.framework.*
import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.ARTIFACT_TASK_NAME
import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.DEFAULT_REPORT_DIR
import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.INSTRUMENTATION_TASK_NAME
import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.LOG_TASK_NAME
import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.VERIFY_TASK_NAME
import kotlinx.kover.maven.plugin.tests.functional.framework.CounterAssert.*
import kotlinx.kover.maven.plugin.tests.functional.framework.CounterType.LINE
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MavenPluginTests {
    @Test
    fun testInstrumentation() {
        runAndCheckExample("instrumentation-only", "verify") {
            assertBuildIsSuccessful()
            assertBinaryReportExists()
            assertDefaultXmlReportExists(false)
            assertDefaultHtmlReportExists(false)
            assertDefaultIcReportExists(false)
        }
    }

    @Test
    fun testSkipByProperty() {
        runAndCheckExample("skip-property", "verify") {
            assertBuildIsSuccessful()
            assertAllSkipped()
        }
    }

    @Test
    fun testSkipByConfiguration() {
        runAndCheckExample("skip-config", "verify") {
            assertBuildIsSuccessful()
            assertAllSkipped()
        }
    }

    @Test
    fun testPassVerification() {
        runAndCheckExample("verify-pass", "verify") {
            assertBuildIsSuccessful()
            assertVerificationPassed()
        }
    }

    @Test
    fun testAllGoalsDefaults() {
        runAndCheckExample("all-goals", "verify") {
            assertBuildIsSuccessful()

            // no verification rules by default
            assertNoVerificationRules()
            assertDefaultHtmlReportExists()
            assertDefaultIcReportExists()
            assertKoverLogIs(LOG_TASK_NAME, "application line coverage: 66.6667%")

            checkDefaultXmlReport {
                classCounter("kotlinx/kover/maven/plugin/testing/Main", LINE) assert Coverage(2, 1)
            }
        }
    }

    @Test
    fun testVerificationError() {
        runAndCheckExample("verify-error", "verify") {
            assertBuildIsSuccessful(false)
            assertLogContains(
                "Kover Verification Error",
                "Rule violated: lines covered percentage is 50.000000, but expected minimum is 100",
                "Rule 'package covered lines' violated: lines missed count for package 'kotlinx.kover.maven.plugin.testing' is 2, but expected maximum is 1"
            )
        }
    }

    @Test
    fun testVerificationWarning() {
        runAndCheckExample("verify-warn", "verify") {
            assertBuildIsSuccessful()
            assertKoverLogIs(
                VERIFY_TASK_NAME,
                """Kover Verification Error
Rule violated: lines covered percentage is 50.000000, but expected minimum is 100
Rule 'package covered lines' violated: lines missed count for package 'kotlinx.kover.maven.plugin.testing' is 2, but expected maximum is 1"""
            )
        }
    }

    @Test
    fun testChangeAgentLine() {
        runAndCheckExample("change-agent-line", "test") {
            assertBuildIsSuccessful()
            assertBinaryReportExists()

            val taskLog = koverGoalLog(INSTRUMENTATION_TASK_NAME)
            assertTrue(
                taskLog.contains("Test property 'jvmArgs' set to \"-javaagent:"),
                "Invalid instrumentation task log, actual '$taskLog'"
            )
        }
    }

    @Test
    fun testPrependAgentLine() {
        runAndCheckExample("prepend-agent-line", "test") {
            assertBuildIsSuccessful()
            assertBinaryReportExists()

            val taskLog = koverGoalLog(INSTRUMENTATION_TASK_NAME)
            assertTrue(
                taskLog.contains("Test property 'argLine' set to -ea \"-javaagent:"),
                "Invalid instrumentation task log, actual '$taskLog'"
            )
        }
    }

    @Test
    fun testMultiDirectory() {
        val s = File.separator
        runAndCheckExample("multidir", "verify") {
            assertBuildIsSuccessful()
            assertBinaryReportExists()
            assertKoverLogIs(
                ARTIFACT_TASK_NAME, """Kover artifact

Binary reports
target${s}kover${s}test.ic

Source root directories
src${s}main${s}kotlin
target${s}generated-sources${s}annotations
src${s}extra

Target root directories
target${s}classes
"""
            )
        }
    }

    @Test
    fun testCommonFilters() {
        runAndCheckExample("filters-common", "verify") {
            assertBuildIsSuccessful()

            checkDefaultXmlReport {
                classCounter("kotlinx/kover/maven/plugin/testing/Main", LINE) assert IsCovered
                classCounter("kotlinx/kover/maven/plugin/testing/ExcludedByParent", LINE) assert IsAbsent
                classCounter("kotlinx/kover/maven/plugin/testing/ExcludedByName", LINE) assert IsAbsent
                classCounter("kotlinx/kover/maven/plugin/testing/ExcludedByAnnotation", LINE) assert IsAbsent
            }
            assertKoverLogIs(LOG_TASK_NAME, "kotlinx.kover.maven.plugin.testing.Main line coverage: 66.6667%")
        }
    }

    @Test
    fun testFiltersInRules() {
        runAndCheckExample("filters-rules", "verify") {
            assertBuildIsSuccessful()

            assertKoverLogIs(
                VERIFY_TASK_NAME, """Kover Verification Error
Rule 'Inherited filter' violated:
  lines covered percentage for class 'kotlinx.kover.maven.plugin.testing.Main' is 66.666700, but expected minimum is 100
  lines covered percentage for class 'kotlinx.kover.maven.plugin.testing.SecondClass' is 0.000000, but expected minimum is 100
Rule 'Exclude Main' violated:
  lines covered percentage for class 'kotlinx.kover.maven.plugin.testing.SecondClass' is 0.000000, but expected minimum is 100
  lines covered percentage for class 'kotlinx.kover.maven.plugin.testing.ThirdClass' is 0.000000, but expected minimum is 100
Rule 'Exclude SecondClass' violated:
  lines covered percentage for class 'kotlinx.kover.maven.plugin.testing.Main' is 66.666700, but expected minimum is 100
  lines covered percentage for class 'kotlinx.kover.maven.plugin.testing.ThirdClass' is 0.000000, but expected minimum is 100"""
            )
        }
    }

    @Test
    fun testMergedReports() = runAndCheckExample("merged-report", "verify") {
        assertBuildIsSuccessful()

        assertKoverLogIs(
            LOG_TASK_NAME,
            """kotlinx.kover.maven.plugin.testing.Child1Class line coverage: 100%
kotlinx.kover.maven.plugin.testing.Child2ExtraClass line coverage: 0%"""
        )

        assertKoverLogIs(VERIFY_TASK_NAME, """Kover Verification Error
Rule violated:
  lines covered count for class 'kotlinx.kover.maven.plugin.testing.Child1Class' is 2, but expected minimum is 100
  lines covered count for class 'kotlinx.kover.maven.plugin.testing.Child2ExtraClass' is 0, but expected minimum is 100""")

        checkDefaultXmlReport("report") {
            classCounter("kotlinx/kover/maven/plugin/testing/Child1Class", LINE) assert IsCovered
            classCounter("kotlinx/kover/maven/plugin/testing/Child2Class", LINE) assert IsAbsent
            classCounter("kotlinx/kover/maven/plugin/testing/Child2ExtraClass", LINE) assert IsFullyMissed
        }
    }

    @Test
    fun testPathChanged() = runAndCheckExample("change-paths", "verify") {
        assertBuildIsSuccessful()
        assertHtmlReportExists("${DEFAULT_REPORT_DIR}/custom-html")
        assertIcReportExists("custom.ic")

        checkXmlReport("custom.xml") {
            classCounter("kotlinx/kover/maven/plugin/testing/Main", LINE) assert IsCovered
        }
    }

    @Test
    fun testAdditionalBinaryReport() = runAndCheckExample("additional-binary-report", "verify") {
        assertBuildIsSuccessful()

        assertKoverLogIs(LOG_TASK_NAME, "application line coverage: 100%")
        checkDefaultXmlReport {
            classCounter("kotlinx/kover/maven/plugin/testing/Main", LINE) assert IsFullyCovered
        }
    }

    @Test
    fun testSiteReporting() = runAndCheckExample("site", "test", "site") {
        assertBuildIsSuccessful()
        assertDefaultHtmlReportExists(true)
        assertDefaultXmlReportExists(false)
        assertDefaultIcReportExists(false)
    }

    @Test
    fun testTitleOverride() = runAndCheckExample("titles", "verify") {
        assertBuildIsSuccessful()
        assertDefaultXmlTitle("Custom XML")
        assertDefaultHtmlTitle("Custom HTML")
    }


    @Test
    fun testLogTaskConfig() = runAndCheckExample("logs", "verify") {
        assertBuildIsSuccessful()
        assertKoverLogIs(
            LOG_TASK_NAME,
            """1 branches covered in kotlinx.kover.maven.plugin.testing.Main"""
        )
    }

    @Test
    fun testExcludeFromInstrumentation() = runAndCheckExample("exclude-instrumentation", "verify") {
        assertBuildIsSuccessful()
        checkDefaultXmlReport {
            classCounter("kotlinx.kover.maven.plugin.testing.Main", LINE) assert IsFullyMissed
            classCounter("kotlinx.kover.maven.plugin.testing.SecondClass", LINE) assert IsCovered
        }
    }

    @Test
    fun testHtmlCharset() = runAndCheckExample("charset", "verify") {
        assertBuildIsSuccessful()
        assertDefaultHtmlTitle("charset", "UTF-16BE")
    }

    @Test
    fun testEmptyKotlinConfig() = runAndCheckTest("kotlin-empty-config", "verify") {
        assertBuildIsSuccessful()
        assertFalse("java.lang.NullPointerException" in log, "NPE should not be thrown")
    }

}
