0.6.0-BETA / 2022-08-02
===================
In this version, the plugin API has been fully redesigned. The new API allows you to configure Kover in a more flexible manner, there is no need to configure Kover or test tasks separately.

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
