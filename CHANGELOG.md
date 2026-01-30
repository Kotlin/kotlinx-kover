0.9.5 / 2026-01-30
===================
## Kover Gradle Plugin
### Bugfixes
* [`#784`](https://github.com/Kotlin/kotlinx-kover/issues/784) Fixed support of build variants in Android Gradle Plugin 9.0.0
* [`#785`](https://github.com/Kotlin/kotlinx-kover/issues/785) Fixed support of test task dependencies in Android Gradle Plugin 9.0.0

0.9.4 / 2025-12-09
===================
## Kover Gradle Plugin
### Bugfixes
* [`#776`](https://github.com/Kotlin/kotlinx-kover/issues/776) Added support for Android Gradle Plugin 9.0.0

0.9.3 / 2025-10-16
===================
## Kover Gradle Plugin
### Bugfixes
* [`#759`](https://github.com/Kotlin/kotlinx-kover/issues/759) Fixed locating of host tests for the multiplatform Android library
* [`#766`](https://github.com/Kotlin/kotlinx-kover/issues/766) Fixed support of KSP plugin
### Build features
* Upgraded Gradle version to `9.1.0`

0.9.2 / 2025-09-16
===================
## Kover Gradle Plugin
### Features
* [`#748`](https://github.com/Kotlin/kotlinx-kover/issues/748) Disable caching for KoverAgentJarTask
### Bugfixes
* [`#747`](https://github.com/Kotlin/kotlinx-kover/issues/747) Added support for KMP Android library plugin

0.9.1 / 2025-01-08
===================
## Kover Gradle Plugin
### Features
* [`#714`](https://github.com/Kotlin/kotlinx-kover/issues/714) Added ability to specify inclusion filter for source sets
### Bugfixes
* [`#716`](https://github.com/Kotlin/kotlinx-kover/issues/716) Fixed Gradle 9.0 migration issue
* [`#721`](https://github.com/Kotlin/kotlinx-kover/issues/721) Fixed creation of Kover Agent arguments file

0.9.0 / 2024-12-12
===================
## Kover Gradle Plugin
### Features
* [`#645`](https://github.com/Kotlin/kotlinx-kover/issues/645) Added ability to supplement coverage values from external binary reports
* [`#673`](https://github.com/Kotlin/kotlinx-kover/issues/673) Implemented ability to specify instrumentation include filter
### Bugfixes
* [`#678`](https://github.com/Kotlin/kotlinx-kover/issues/678) Added USAGE attribute to all Kover configurations
* [`#666`](https://github.com/Kotlin/kotlinx-kover/issues/666) Replaced JaCoCo ant-calls with programmatic calls of JaCoCo's classes

## Kover Aggregation Plugin
### Features
* Made class KoverSettingsGradlePlugin from aggregated plugin public
* Implemented verification in Kover Aggregated Plugin
* Added ability to skip projects
* Added ability to limit instrumented class globally and locally in a project
* Added ability to exclude test task from instrumentation in a project config
* Implement feature to check verification rule on every project
### Bugfixes
* Fixed bug with non-existing binary report files

## Kover CLI
### Features
* [`#677`](https://github.com/Kotlin/kotlinx-kover/issues/677) Implemented merging of binary reports in Kover CLI and Kover Features
### Bugfixes
* [`#709`](https://github.com/Kotlin/kotlinx-kover/issues/709) Fixed offline instrumentation of jar files
* Fixed skipping some classes during offline instrumentation

## Kover JVM Agent
### Bugfixes
* Fixed JVM agent arguments parsing to support include filter
### Documentation
* [`#660`](https://github.com/Kotlin/kotlinx-kover/issues/660) Fixed mistake in JVM agent docs

## Kover Maven Plugin
### Documentation
* [`#658`](https://github.com/Kotlin/kotlinx-kover/issues/658) Fixed documentation about Maven plugin
* [`#701`](https://github.com/Kotlin/kotlinx-kover/issues/701) Fixed warning for empty Kotlin plugin configuration


## Changelog relative to version `0.9.0-RC`
### Kover Aggregation Plugin
* Fixed bug with non-existing binary report files
* Added ability to skip projects
* Added ability to limit instrumented class globally and locally in a project
* Added ability to exclude test task from instrumentation in a project config
* Implement feature to check verification rule on every project

### Kover Maven Plugin
* [`#701`](https://github.com/Kotlin/kotlinx-kover/issues/701) Fixed warning for empty Kotlin plugin configuration

### Kover CLI
* [`#709`](https://github.com/Kotlin/kotlinx-kover/issues/709) Fixed offline instrumentation of jar files
* Fixed skipping some classes during offline instrumentation

0.9.0-RC / 2024-09-03
===================
## Kover Gradle Plugin
### Features
* [`#645`](https://github.com/Kotlin/kotlinx-kover/issues/645) Added ability to supplement coverage values from external binary reports
* [`#673`](https://github.com/Kotlin/kotlinx-kover/issues/673) Implemented ability to specify instrumentation include filter
### Bugfixes
* [`#678`](https://github.com/Kotlin/kotlinx-kover/issues/678) Added USAGE attribute to all Kover configurations
* [`#666`](https://github.com/Kotlin/kotlinx-kover/issues/666) Replaced JaCoCo ant-calls with programmatic calls of JaCoCo's classes

## Kover Aggregation Plugin
### Features
* Made class KoverSettingsGradlePlugin from aggregated plugin public
* Implemented verification in Kover Aggregated Plugin

## Kover CLI
### Features
* [`#677`](https://github.com/Kotlin/kotlinx-kover/issues/677) Implemented merging of binary reports in Kover CLI and Kover Features

## Kover JVM Agent
### Bugfixes
* Fixed JVM agent arguments parsing to support include filter

### Documentation
* [`#660`](https://github.com/Kotlin/kotlinx-kover/issues/660) Fixed mistake in JVM agent docs

## Kover Maven Plugin
### Documentation
* [`#658`](https://github.com/Kotlin/kotlinx-kover/issues/658) Fixed documentation about Maven plugin

0.8.3 / 2024-07-18
===================
## Kover Aggregation Plugin
* Added support for Android projects in Kover Aggregation Plugin

## Kover Maven Plugin
* [`#51`](https://github.com/Kotlin/kotlinx-kover/issues/51) Implemented Kover Maven Plugin

0.8.2 / 2024-06-27
===================
## Kover Aggregation Plugin
Implemented prototype of Kover Aggregation Plugin - an alternative to the existing Kover Gradle Plugin, it makes it easier to set up a configuration and collect coverage reactively, depending on the compilation and test tasks running.

**This is not a production-ready plugin, it is in an incubation state.**

Please refer to the [GitHub issue](https://github.com/Kotlin/kotlinx-kover/issues/608) and [documentation](https://kotlin.github.io/kotlinx-kover/gradle-plugin/aggregated.html) for details.

## Kover Gradle Plugin
### Bugfixes
* [`#621`](https://github.com/Kotlin/kotlinx-kover/issues/621) Fixed coverage evaluation for enum in K2
* [`#633`](https://github.com/Kotlin/kotlinx-kover/issues/633) Fix issue with identical cache keys between projects
* [`#613`](https://github.com/Kotlin/kotlinx-kover/issues/613) Fixed JaCoCo error: Can't add different class with same name
* [`#601`](https://github.com/Kotlin/kotlinx-kover/issues/601) Fixed support of Compose functions
* [`#646`](https://github.com/Kotlin/kotlinx-kover/issues/646) Fixed reusing of configuration cache
* [`#628`](https://github.com/Kotlin/kotlinx-kover/issues/628) Fixed coverage evaluation of try-finally and try-with-resources for Java code

### Features
* Disable caching on kover artifact tasks

0.8.1 / 2024-06-07
===================
## Kover Gradle Plugin

### Features
* [`#600`](https://github.com/Kotlin/kotlinx-kover/issues/600) Apply recommendations for improving DSL
* Added DSL to copy one report variant

### Bugfixes
* [`#610`](https://github.com/Kotlin/kotlinx-kover/issues/610) Fixed `KoverCriticalException` with a certain order of applying of plugins

0.8.0 / 2024-05-15
===================
This release introduces DSL rework to simplify the work with Android build variants, adds the possibility of lazy configuration, allows for the creation of custom report variants, and expands the ability of reports filtering.

It is incompatible with the previous version, and we provide best-effort migration assistance as well as the [migration guide](https://github.com/Kotlin/kotlinx-kover/blob/v0.8.0/docs/gradle-plugin/migrations/migration-to-0.8.0.md).

## Kover Gradle Plugin
### Features
* [`#461`](https://github.com/Kotlin/kotlinx-kover/issues/461) Implemented DSL revision 4
* [`#410`](https://github.com/Kotlin/kotlinx-kover/issues/410) Add possibility of lazy configuration of Kover extensions
* [`#462`](https://github.com/Kotlin/kotlinx-kover/issues/462) Redesign the concept of default reports
* [`#463`](https://github.com/Kotlin/kotlinx-kover/issues/463) Add the ability to create custom report variants
* [`#338`](https://github.com/Kotlin/kotlinx-kover/issues/338) Create an interface for Kover tasks
* [`#66`](https://github.com/Kotlin/kotlinx-kover/issues/66) Added support for the publishing plugin to Gradle Plugin Portal
* [`#466`](https://github.com/Kotlin/kotlinx-kover/issues/466) Implemented multi-project shortcuts
* [`#339`](https://github.com/Kotlin/kotlinx-kover/issues/339) Implemented warn on verification error
* [`#572`](https://github.com/Kotlin/kotlinx-kover/issues/572) Added DslMarker to Kover public interfaces
* [`#570`](https://github.com/Kotlin/kotlinx-kover/issues/570) Added overload for functions of KoverVariantCreateConfig
* [`#590`](https://github.com/Kotlin/kotlinx-kover/issues/590) Expose reportDir property in KoverHtmlReport interface
* [`#587`](https://github.com/Kotlin/kotlinx-kover/issues/587) Added property variantName to KoverReport interface
* [`#584`](https://github.com/Kotlin/kotlinx-kover/issues/584) Added project filter for reports
* [`#274`](https://github.com/Kotlin/kotlinx-kover/issues/274) Allow for classes to be Included by annotation
* [`#454`](https://github.com/Kotlin/kotlinx-kover/issues/454) Added a report filter by parent class or interface
### Bugfixes
* [`#557`](https://github.com/Kotlin/kotlinx-kover/issues/557) Changed log level for print coverage task
* [`#520`](https://github.com/Kotlin/kotlinx-kover/issues/520) Fixed error: Kover requires extension with name 'androidComponents'

### Internal features
* [`#567`](https://github.com/Kotlin/kotlinx-kover/issues/567) Used compile dependency to Kover Features in Kover Gradle Plugin
### Documentation
* [`#531`](https://github.com/Kotlin/kotlinx-kover/issues/531) Added explanations about applying of the plugin
* [`#486`](https://github.com/Kotlin/kotlinx-kover/issues/486) Increased the readability of the Kover documentation

## Kover JVM Agent
### Features
* [`#464`](https://github.com/Kotlin/kotlinx-kover/issues/464) Repacked the intellij-agent artifact to kover-jvm-agent

### Bugfixes
* [`#583`](https://github.com/Kotlin/kotlinx-kover/issues/583) Excluded from report companion objects with only constants
* [`#548`](https://github.com/Kotlin/kotlinx-kover/issues/548) Fixed coverage drop in case of using different classloaders for same class

## Changelog relative to version `0.8.0-Beta2`
### Kover Gradle Plugin
#### Features
* [`#590`](https://github.com/Kotlin/kotlinx-kover/issues/590) Expose reportDir property in KoverHtmlReport interface
* [`#587`](https://github.com/Kotlin/kotlinx-kover/issues/587) Added property variantName to KoverReport interface
* [`#584`](https://github.com/Kotlin/kotlinx-kover/issues/584) Added project filter for reports
* [`#274`](https://github.com/Kotlin/kotlinx-kover/issues/274) Allow for classes to be Included by annotation
* [`#454`](https://github.com/Kotlin/kotlinx-kover/issues/454) Added a report filter by parent class or interface

#### Documentation
* [`#486`](https://github.com/Kotlin/kotlinx-kover/issues/486) Increased the readability of the Kover documentation for 0.8.0

### Kover JVM Agent
#### Bugfixes
* [`#583`](https://github.com/Kotlin/kotlinx-kover/issues/583) Excluded from report companion objects with only constants
* [`#548`](https://github.com/Kotlin/kotlinx-kover/issues/548) Fixed coverage drop in case of using different classloaders for same class


0.8.0-Beta2 / 2024-03-28
===================
## Kover Gradle Plugin
### Features
* [`#339`](https://github.com/Kotlin/kotlinx-kover/issues/339) Implemented warn on verification error
* [`#572`](https://github.com/Kotlin/kotlinx-kover/issues/572) Added DslMarker to Kover public interfaces
* [`#570`](https://github.com/Kotlin/kotlinx-kover/issues/570) Added overload for functions of KoverVariantCreateConfig
### Bugfixes
* [`#557`](https://github.com/Kotlin/kotlinx-kover/issues/557) Changed log level for print coverage task
* [`#338`](https://github.com/Kotlin/kotlinx-kover/issues/338) Extended org.gradle.api.Task in KoverReport interface
### Internal features
* [`#567`](https://github.com/Kotlin/kotlinx-kover/issues/567) Used compile dependency to Kover Features in Kover Gradle Plugin

0.8.0-Beta / 2024-02-29
===================
## Kover Gradle Plugin
### Features
* [`#461`](https://github.com/Kotlin/kotlinx-kover/issues/461) Implemented DSL revision 4
* [`#410`](https://github.com/Kotlin/kotlinx-kover/issues/410) Add possibility of lazy configuration of Kover extensions
* [`#462`](https://github.com/Kotlin/kotlinx-kover/issues/462) Redesign the concept of default reports
* [`#463`](https://github.com/Kotlin/kotlinx-kover/issues/463) Add the ability to create custom report variants
* [`#338`](https://github.com/Kotlin/kotlinx-kover/issues/338) Create an interface for Kover tasks
* [`#66`](https://github.com/Kotlin/kotlinx-kover/issues/66) Added support for the publishing plugin to Gradle Plugin Portal
* [`#466`](https://github.com/Kotlin/kotlinx-kover/issues/466) Implemented multiproject shortcuts

### Bugfixes
* [`#520`](https://github.com/Kotlin/kotlinx-kover/issues/520) Fixed error: Kover requires extension with name 'androidComponents'

### Documentation
* [`#531`](https://github.com/Kotlin/kotlinx-kover/issues/531) Added explanations about applying of the plugin

## Kover JVM Agent
* [`#464`](https://github.com/Kotlin/kotlinx-kover/issues/464) Repacked the intellij-agent artifact to kover-jvm-agent


0.7.6 / 2024-02-16
===================
## Kover Gradle Plugin
### Features
* [`#527`](https://github.com/Kotlin/kotlinx-kover/issues/527) Added the ability to specify a header for an XML report

### Bugfixes
* [`#510`](https://github.com/Kotlin/kotlinx-kover/issues/510) Fixed `Stream closed` error when generating Kover HTML report
* [`#513`](https://github.com/Kotlin/kotlinx-kover/issues/513) Fixed breaking configuration cache
* [`#517`](https://github.com/Kotlin/kotlinx-kover/issues/517) Fixed incorrect marking of first function line when parameters with default value are used
* [`#530`](https://github.com/Kotlin/kotlinx-kover/issues/530) Fixed the presence of classes instrumented with Robolectric in Jacoco reports
* [`#543`](https://github.com/Kotlin/kotlinx-kover/issues/543) Fixed package exclusion in reports for JaCoCo

## Kover Offline
### Features
* [`#534`](https://github.com/Kotlin/kotlinx-kover/issues/534) Created Kover features artifact to invoke the capabilities of Kover programmatically

0.7.5 / 2023-11-28
===================
## Kover Gradle Plugin
### Features
* [`#503`](https://github.com/Kotlin/kotlinx-kover/issues/503) Introduced Offline Runtime API for saving binary report

### Bugfixes
* [`#478`](https://github.com/Kotlin/kotlinx-kover/issues/478) Added Kover dependency check
* [`#451`](https://github.com/Kotlin/kotlinx-kover/issues/451) Skip Kotlin object if it is containing only constants
* [`#459`](https://github.com/Kotlin/kotlinx-kover/issues/459) Fixed applying of the root verification rules
* [`#489`](https://github.com/Kotlin/kotlinx-kover/issues/489) Fixed adding data to an already existing binary report file


0.7.4 / 2023-10-10
===================
## Kover Gradle Plugin
### Features
* [`#441`](https://github.com/Kotlin/kotlinx-kover/issues/441) Added support for Android dynamic feature plugins

### Bugfixes
* [`#440`](https://github.com/Kotlin/kotlinx-kover/issues/440) Replaced absolute paths in the Kover artifact with relative ones
* [`#446`](https://github.com/Kotlin/kotlinx-kover/issues/446) Disabled writing of FreeMarker logs to stdout when generating an HTML report
* [`#470`](https://github.com/Kotlin/kotlinx-kover/issues/470) Update HTML report path to be clickable
* [`#385`](https://github.com/Kotlin/kotlinx-kover/issues/385) Exclude nested functions in function excluded by annotation
* [`#303`](https://github.com/Kotlin/kotlinx-kover/issues/303) Optional function parameters should not be considered a "branch"
* [`#436`](https://github.com/Kotlin/kotlinx-kover/issues/436) Fixed random koverHtmlReport fails with "Stream closed" or "zip file closed"

### Build features
* Upgraded Gradle version to `8.2.1`

### Test features
* [`#437`](https://github.com/Kotlin/kotlinx-kover/issues/437) Added functional test on Gradle nested classes validation error


0.7.3 / 2023-07-26
===================
## Kover Gradle Plugin
### Features
* Added ability to specify verification rules in the root of reports config
* [`#423`](https://github.com/Kotlin/kotlinx-kover/issues/423)  Implemented task of generating binary report

### Bugfixes
* [`#405`](https://github.com/Kotlin/kotlinx-kover/issues/405) Fixed lookup for tests if unit tests are disabled in Android config
* [`#415`](https://github.com/Kotlin/kotlinx-kover/issues/415) Fixed usage of Kover Gradle Plugin in buildSrc directory
* [`#431`](https://github.com/Kotlin/kotlinx-kover/issues/431) Fixed excluding of companion object by annotation from report 

## Kover Offline
### Features
* Added API for getting coverage inside a running application, instrumented offline

0.7.2 / 2023-06-26
===================
### Features
* [`#362`](https://github.com/Kotlin/kotlinx-kover/issues/362) Removed the dependency on the order of applying of the plugin
* [`#229`](https://github.com/Kotlin/kotlinx-kover/issues/229) Added task to print coverage to logs
* [`#394`](https://github.com/Kotlin/kotlinx-kover/issues/394) Added DSL accessors for Kover Default report tasks
* [`#400`](https://github.com/Kotlin/kotlinx-kover/issues/400) Added descriptions for Kover report tasks
* [`#409`](https://github.com/Kotlin/kotlinx-kover/issues/409) Added ability to generate reports even if there are no tests in the project
* Upgraded default JaCoCo version to `0.8.10`

### Bugfixes
* [`#413`](https://github.com/Kotlin/kotlinx-kover/issues/413) Fixed issues with cache miss of build cache at the time of project relocation

### Documentation
* Fixed docs typo: `dependency {}` -> `dependencies {}`

### Internal features
* Moved Kover Gradle Plugin to the separate subproject
* Migrated from buildSrc to composite build
* Added support of the version catalog
* IntelliJ coverage dependency versions upgraded to `1.0.724`


0.7.1 / 2023-06-01
===================
### Features
* Added a filter for source sets (#376)

### Bugfixes
* Fixed up-to-date checks for Kover tasks (#371)

### Documentation
* Updated documentation on filtering by annotations (#370)
* Improved Kover documentation (#282)
* Fix broken migration link in CHANGELOG.md
* Fix documentation for KoverReportExtension
* Fixed Kover Plugin ID for legacy plugin application

### Internal features
* Implemented Worker API for use with Kover toolset
* Updated Gradle and Kotlin versions
* IntelliJ coverage dependency versions upgraded to `1.0.721`

0.7.0 / 2023-05-16
===================
This release introduce API rework in order to support configuration cache, project isolation model, Android Gradle plugin.
It is incompatible with the previous version, and we provide best-effort migration assistance as well as [migration guide](https://github.com/Kotlin/kotlinx-kover/blob/v0.7.0/docs/gradle-plugin/migrations/migration-to-0.7.0.md)

### Features

* Implemented improved Kover DSL (#284)
* Added Gradle project isolation support (#144)
* Introduced API for Coverage Tools (#195)
* Added support of Android build variants (#18)
* Implemented support filtering of source sets for Kotlin JVM and Kotlin compilations for Kotlin multiplatform (#245)
* Added customizable header in HTML report for Kover and JaCoCo report generator (#194)
* Added advanced support of Android projects, flavors and flavor dimensions (#316, #319)
* Introduced Kover Tool artifacts for CLI and runtime for offline instrumentation (#322)
* Added support charset for HTML report

### Internal features
* Added dokka docs
* Added binary compatibility validator (#305)
* IntelliJ coverage dependency versions upgraded to 1.0.716

### Bugfixes
* Fixed `Cannot run Project.afterEvaluate` (#221)
* Fixed missing report path in logs for cached HTML task (#283)
* Disabled artifact generation when calling the `assemble` task (#353)
* Fixed variant level filters for reports (#366)

### Changelog relative to version `0.7.0-Beta`
#### Features
* Added support charset for html report

#### Bugfixes
* Disabled artifact generation when calling the `assemble` task (#353)
* Fixed variant level filters for reports (#366)

#### Internal features
* IntelliJ coverage dependency versions upgraded to 1.0.716

0.7.0-Beta / 2023-04-21
===================
### Features
* Added advanced support of Android projects, flavors and flavor dimensions (#316, #319)
* Introduced Kover Tool artifacts for CLI and runtime for offline instrumentation (#322)

### Internal features
* Added dokka docs
* Added binary compatibility validator (#305)

0.7.0-Alpha / 2023-02-27
===================
This is a preview version of new major release with the API rework in order to support configuration cache, project isolation model, Android Gradle plugin and source-set level granularity of configuration.
This release is incompatible with the previous version, and we provide best-effort migration assistance as well as [migration guide](https://github.com/Kotlin/kotlinx-kover/blob/v0.7.0-Alpha/docs/migration-to-0.7.0.md)
### Features
* Implemented improved Kover DSL (#284)
* Added Gradle project isolation support (#144)
* Introduced API for Coverage Tools (#195)
* Added support of Android build variants (#18)
* Implemented support filtering of source sets for Kotlin JVM and Kotlin compilations for Kotlin multiplatform (#245)
* Added customizable header in HTML report for Kover and JaCoCo report generator (#194)
* Minimal and default Kover Tool versions upgraded to 1.0.709

### Bugfixes
* Fixed `Cannot run Project.afterEvaluate` (#221)
* Fixed missing report path in logs for cached HTML task (#283)


0.6.1 / 2022-10-03
===================

### Features
* Implemented filtering of reports by annotation (#121)
* Minimal and default agent versions upgraded to `1.0.683`

### Bugfixes
* Added filtering out projects without a build file (#222)
* Added JaCoCo reports filtering (#220)
* Fixed coverage for function reference (#148)
* Fixed incorrect multiplatform lookup adapter (#193)
* Fixed `ArrayIndexOutOfBoundsException` during class instrumentation (#166)

### Internal features
* Upgraded Gradle version to `7.5.1`
* Rewritten functional tests infrastructure
* Added example projects
* XML and HTML report generation moved to Kover Aggregator

### Documentation
* Added contribution guide
* Added section `Building and contributing` into Table of contents
* Fix migration to `0.6.0` documentation

0.6.0 / 2022-08-23
===================
Note that this is a full changelog relative to `0.6.0` version. Changelog relative to `0.6.0-Beta` can be found at the end of the changelog.

In this version, the plugin API has been completely redesigned. The new API allows you to configure Kover in a more flexible manner, there is no need to configure Kover or test tasks separately.

Please refer to [migration guide](https://github.com/Kotlin/kotlinx-kover/blob/v0.6.0/docs/migration-to-0.6.0.md) in order to migrate from previous versions.

### Features
* Implemented a new plugin API (#19)
* Added support of instruction and branch counters for verification tasks (#128)
* Ordered report tasks before verification tasks (#209)
* Minimal and default agent versions upgraded to 1.0.680

### Bugfixes
* Verification task is no longer executed if there are no rules (#168)
* Added instrumentation filtering by common filters (#201)
* Fixed instrumentation counter in IntelliJ verifier (#210, #211, #212)

### Internal features
* Kotlin version upgraded to 1.7.10
* instrumentation config added to the test framework
* added test on instrumentation config

#### Documentation
* Updated docs for onCheck properties (#213)

### Changelog relative to version `0.6.0-Beta`
#### Features
* Ordered report tasks before verification (#209)
* Minimal and default agent versions upgraded to 1.0.680

#### Bugfixes
* Added instrumentation filtering by common filters (#201)
* Fixed instrumentation counter in IntelliJ verifier (#210, #211, #212)

#### Documentation
* Updated docs for onCheck properties (#213)

0.6.0-Beta / 2022-08-02
===================
In this version, the plugin API has been fully redesigned. The new API allows you to configure Kover in a more flexible manner, there is no need to configure Kover or test tasks separately.

Refer to [migration guide](https://github.com/Kotlin/kotlinx-kover/blob/v0.6.0-Beta/docs/migration-to-0.6.0.md) in order to migrate.

**This is a beta release, the stability of all features is not guaranteed. The beta version is released to collect feedback on the new API and its usability.**

### Features
* Implemented a new plugin API (#19)
* Minimal and default agent versions upgraded to 1.0.675
* Added support of instruction and branch counters for verification tasks (#128)

### Bugfixes
* Verification task is no longer executed if there are no rules (#168)

### Internal features
* Kotlin version upgraded to 1.7.10
* instrumentation config added to the test framework
* added test on instrumentation config

0.5.1 / 2022-05-11
===================

### Features
* Added `Gradle` configuration cache support (#142)
* Implemented the use of the full path to the project for the exclusion (#151)
* Supported `IntelliJ Verifier`
* Upgraded default and minimal `IntelliJ Coverage Engine` version to `1.0.668`

### IntelliJ Agent
* Fixed coverage counter for `$DefaultImpls` (#149)

### Internal features
* Added functional tests on verification
* Implemented simple JSON parser and serializer

### Requirements
* Upgraded minimal supported `Gradle` version to `6.6`

0.5.0 / 2022-02-01
===================
Note that this is a full changelog relative to `0.4.4` version. Changelog relative to `0.5.0-RC2` can be found at the end of the changelog.

### Features
* Added reports filtering (#17)
* Disabled running of all test tasks for single-project Kover tasks (#114)
* Implemented aggregated multi-project report (#20, #43)
* Unified coverage agents filters. Now only the characters '*' or '?' are used as wildcards for both IntelliJ and JaCoCo agents. **Regular expressions are no longer supported by the IntelliJ agent as filters of instrumented classes.** (#21)
* Tasks for verification and reporting for single Gradle project were renamed according to the template like `koverXmlReport` -> `koverXmlProjectReport`
* The `isEnabled` property has been renamed to `isDisabled` in extensions `KoverExtension` and `KoverTaskExtension` to make their purpose more obvious
* The term `module` has been replaced with `project` for compatibility with Gradle terminology
* Added the ability to disable the Kover for the specified Gradle project
* Made tasks cache relocatable (#85)
* Improved checks of disabled plugin before running Kover tasks
* Upgraded IntelliJ Engine minimal version to `1.0.647`
* Upgraded IntelliJ Engine default version to `1.0.656`

### Bugfixes
* Added support of parallel tests execution (#113)
* Removed checking of parent projects for re-apply of the plugin (#116)
* Added property to exclude Android classes from the instrumentation (#89)
* Kotlin Multiplatform plugin adapter rewritten to use reflection (#100)

### IntelliJ Agent Features (version 1.0.656)
* Added the ability to count JVM instructions
* Fixed getting into the report of objects and sealed classes
* Added an excluding from the report of functions marked by `Deprecated` annotation with `HIDDEN` and `ERROR` levels

### Internal features
* Added functional test on branch counter
* Added functional tests on instruction counter

### Changelog relative to version `0.5.0-RC2`

#### Features
* Improved checks of disabled plugin before running Kover tasks
* Upgraded IntelliJ Engine default version to `1.0.656`

#### Bugfixes
* Added support of parallel tests execution (#113)
* Removed checking of parent projects for re-apply of the plugin (#116)

#### IntelliJ Agent Features (version 1.0.656)
* Added the ability to count JVM instructions
* Fixed getting into the report of objects and sealed classes
* Added an excluding from the report of functions marked by `Deprecated` annotation with `HIDDEN` and `ERROR` levels 

#### Internal features
* Added functional test on branch counter
* Added functional tests on instruction counter

0.5.0-RC2 / 2022-01-14
===================

In this version, the plugin API has been redesigned for more convenient and understandable work with multi-project 
builds and merged reports. Also added filters for report and verification tasks.

### Features
* Added reports filtering (#17)
* Disabled running of all test tasks for single-project Kover tasks (#114)
* Upgraded IntelliJ Engine default version to `1.0.647`

0.5.0-RC / 2021-12-24
===================

This is a release candidate for the next version. In this version, an aggregated report on all projects has been added, 
and the plugin API has been significantly redesigned. So we ask you to evaluate it and share your feedback on whether 
the API has become more convenient.

### Features
* Implemented aggregated multi-project report (#20, #43)
* Unified coverage agents filters. Now only the characters '*' or '?' are used as wildcards for both IntelliJ and JaCoCo agents. **Regular expressions are no longer supported by the IntelliJ agent as filters of instrumented classes.** (#21)
* Tasks for verification and reporting for single Gradle project were renamed according to the template like `koverXmlReport` -> `koverXmlProjectReport`
* The `isEnabled` property has been renamed to `isDisabled` in extensions `KoverExtension` and `KoverTaskExtension` to make their purpose more obvious
* The term `module` has been replaced with `project` for compatibility with Gradle terminology
* Added the ability to disable the Kover for the specified Gradle project
* Made tasks cache relocatable (#85)
* Upgraded IntelliJ Engine default version to `1.0.640`

### Bugfixes
* Added property to exclude Android classes from the instrumentation (#89)
* Kotlin Multiplatform plugin adapter rewritten to use reflection (#100)

v0.4.4 / 2021-11-29
===================

### Bugfixes
* Fixed escape characters in intellijreport.json (#82)

v0.4.3 / 2021-11-29
===================

### Bugfixes
* Added support for the IntelliJ agent loaded from maven central (#34)
* Implemented the ability to generate a report even if there are no tests in the module (#44)
* Fixed caching of Kover report tasks (#68)
* Upgraded minimal IntelliJ agent version to `1.0.639` (#76)

### Internal features
* Implemented integration tests (#25)

v0.4.2 / 2021-11-14
===================

### Bugfixes

  * Fixed generation of HTML reports for classes located in a directory that does not match the package name (#31)
  * Removed implicit dependencies from `koverCollectReports` task (#53)
  * Fixed a crash in report generation if there are several test tasks in the module and one of them does not contain tests (#46)
  * Ignore empty private constructors of utility classes (#6)
