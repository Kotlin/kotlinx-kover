# Contributing Guidelines

There are two main ways to contribute to the project &mdash; submitting issues and submitting 
fixes/changes/improvements via pull requests.

## Submitting issues

Both bug reports and feature requests are welcome.
Submit issues [here](https://github.com/Kotlin/kotlinx-kover/issues).

* Search for existing issues to avoid reporting duplicates.
* When submitting a bug report:
  * Test it against the most recently released version. It might have been already fixed.
  * By default, we assume that your problem reproduces in Kotlin/JVM project with the Kover Coverage Tool. Please, mention if the problem is
    specific to Kotlin Multiplatform/Android or JaCoCo Tool. 
  * Include the code that reproduces the problem. Provide the complete reproducer code, yet minimize it as much as possible.
  * However, don't put off reporting any weird or rarely appearing issues just because you cannot consistently 
    reproduce them.
  * If the bug is in behavior, then explain what behavior you've expected and what you've got.  
* When submitting a feature request:
  * Explain why you need the feature &mdash; what's your use-case, what's your domain.
  * Explaining the problem you face is more important than suggesting a solution. 
    Report your problem even if you don't have any proposed solution.
  * If there is an alternative way to do what you need, then show the code of the alternative.

## Submitting PRs

We love PRs. Submit PRs [here](https://github.com/Kotlin/kotlinx-kover/pulls).
However, please keep in mind that maintainers will have to support the resulting code of the project,
so do familiarize yourself with the following guidelines. 

* All development (both new features and bug fixes) is performed in the `main` branch.
  * The `release` branch always contains sources of the most recently released version.
  * Base PRs against the `main` branch.
  * The `main` branch is pushed to the `release` branch during release.
* If you make any code changes:
  * Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html). 
    Use 4 spaces for indentation.
  * [Build the project](#building) to make sure it all works and passes the tests.
* If you fix a bug:
  * Write the test that reproduces the bug.
  * Fixes without tests are accepted only in exceptional circumstances if it can be shown that writing the 
    corresponding test is too hard or otherwise impractical.
  * Place a test for the functionality of one or more Kover plugin classes in [unit tests directory](src/test/kotlin)
  * Place in [functional test directory](src/functionalTest/kotlin) the test that check the functionality of the Kover plugin by the Gradle runs
  * Follow the style of writing tests that is used in this project: 
    name test functions as `test...`, don't use backticks in test names. Name test classes as `...Tests`.
  * Fixes that, in addition to directly solving the bug, add a large piece of new functionality or change the existing one, will be considered as features
* If you introduce any new features or change the existing behavior:
  * Comment on the existing issue if you want to work on it or create one beforehand. 
    Ensure that the issue not only describes a problem, but also describes a solution that had received a positive feedback. Propose a solution if there isn't any.
    PRs with new features, but without a corresponding issue with a positive feedback about the proposed implementation are unlikely to
    be approved or reviewed.
  * All new or modified features must come with tests.
  * If you plan significant changes in the internal structure of the plugin without changing external behavior then please start by submitting an issue with the
    proposed design to receive community feedback.
  * If you plan API changes, then please start by submitting an issue with the proposed API design  
    to gather community feedback.
  * [Contact the maintainers](#contacting-maintainers) to coordinate any big piece of work in advance.
* Drafts are used to demonstrate a prototype solution and discuss it with the community for further implementation

## Building

This plugin is built with Gradle. 

* Run `./gradlew build` to build. It also runs all unit and functional tests.
* Run `./gradlew functionalTest --tests "test.name"` to run specific functional test to speed 
  things up during development.

### Environment requirements

* JDK >= 1.8 referred to by the `JAVA_HOME` environment variable.

## Contacting maintainers

* If something cannot be done, not convenient, or does not work &mdash; submit an [issue](#submitting-issues).
* "How to do something" questions &mdash; [StackOverflow](https://stackoverflow.com).
