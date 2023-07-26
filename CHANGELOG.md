0.7.3 / 2023-07-26
===================
## Kover Gradle Plugin
### Features
* Added ability to specify verification rules in the root of reports config
* [`#423`](https://github.com/Kotlin/kotlinx-kover/issues/423)  Implemented task of generating binary report

### Bugfixes
* [`#405`](https://github.com/Kotlin/kotlinx-kover/issues/405) Fixed lookup for tests if unit tests are disabled in Android config
* [`#415`](https://github.com/Kotlin/kotlinx-kover/issues/415) Fixed usage Kover Gradle Plugin in buildSrc directory
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
