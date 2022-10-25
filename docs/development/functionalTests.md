# Functional Tests
Functional tests run Gradle in a separate system process, and then check the result of the work.

By default, to run functional tests, Gradle is used, which builds a plugin.

## Test types
 * examples - used to demonstrate the use of the Kover plugin on ready-made projects. Located in [examples](/examples) directory. 
For these projects, the `build` command must pass successfully. For such projects, it is mandatory that the latest release version of the plugin is used in the build script (the value of the `releaseVersion` property). 
<br/>To create test on all examples, use `@kotlinx.kover.test.functional.framework.starter.ExamplesTest` annotation on function. This function must have receiver or single parameter with type `kotlinx.kover.test.functional.framework.checker.CheckerContext` to check result of Gradle run.
 * templates - test on some specific rare case. Located in special [directory](/src/functionalTest/templates). You can execute any Gradle command for template project.
<br/>To create test on all examples, use `@kotlinx.kover.test.functional.framework.starter.TemplateTest` annotation on function. This function must have receiver or single parameter with type `kotlinx.kover.test.functional.framework.checker.CheckerContext` to check result of Gradle run. 
 * single generated test - launching a project without a ready source code. The project is builded using the configurator in the code of the test itself. This makes the test code more versatile - it is suitable for creating builds in different script languages and using different Kotlin plugins or different Coverage Tools.
<br/>To create test on all examples, use `@kotlinx.kover.test.functional.framework.starter.TemplateTest` annotation on function. This function must have receiver or single parameter with type `kotlinx.kover.test.functional.framework.configurator.BuildConfigurator` to generate projects and perform Gradle run with checks. 
 * sliced generated test - same as a single generated test, but it can be executed several times for different combinations of script language, Kotlin Plugin type and Coverage Tool (taken together, these three values are called slice).
<br/>To create test on all examples, use `@kotlinx.kover.test.functional.framework.starter.SlicedGeneratedTest` annotation on function. This function must have receiver or single parameter with type `kotlinx.kover.test.functional.framework.configurator.BuildConfigurator` to generate projects and perform Gradle run with checks.
 * simple test - the test additionally does not perform any actions with Gradle, all the logic of the work should be written in the test code itself using the available functions
<br/>To create test on all examples, use `@kotlinx.kover.test.functional.framework.starter.SimpleTest` annotation on function. This function must have receiver or single parameter with type `java.io.File` with temporary directory for this test.

## Configuring the testing
### Project parameters
* `-PgradleVersion=` override Gradle version to run functional tests
* `-PandroidSdk=` specify directory with Android SDK. Takes precedence over the environment variable `ANDROID_HOME`.
* `-PdisableAndroidTests` disable the execution of functional tests with android projects. Useful if the Android SDK is not installed locally
* `-Pdebug` runs each functional test in debug mode - to start, it will wait for the debug client to connect to port `5005`
* `-PtestLogs` output extended information on the execution of functional tests to the build logs
