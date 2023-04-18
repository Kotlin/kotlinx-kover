# Kover Command Line Interface

This is a single jar artifact that allows to use some of the functionality of Kover Toolset through command-line calls.

Java 1.6 or greater is required for execution.

## Commands

### Off-line instrumentation

`java -jar kover-cli.jar instrument [<class-file-path> ...] --dest <dir> [--exclude <class-name>] [--excludeAnnotation <annotation-name>] [--hits] [--include <class-name>]`

| Option                                | Description                                                                                    | Required | Multiple |
|---------------------------------------|------------------------------------------------------------------------------------------------|:--------:|:--------:|
| `<class-file-path>`                   | list of the compiled class-files roots                                                         |          |    +     |
| --dest <dir>                          | path to write instrumented Java classes to                                                     |    +     |          |
| --exclude <class-name>                | filter to exclude classes from instrumentation, wildcards `*` and `?` are acceptable           |          |    +     |
| --excludeAnnotation <annotation-name> | filter to exclude annotated classes from instrumentation, wildcards `*` and `?` are acceptable |          |    +     |
| --hits                                | a flag to enable line hits counting                                                            |          |          |
| --include <class-name>                | instrument only specified classes, wildcards `*` and `?` are acceptable                        |          |    +     |

### Generating reports
Allows you to generate HTML and XML reports.

`java -jar kover-cli.jar report [<binary-report-path> ...] --classfiles <class-file-path> [--exclude <class-name>] [--excludeAnnotation <annotation-name>] [--html <html-dir>] [--include <class-name>] --src <sources-path> [--title <html-title>] [--xml <xml-file-path>]`

| Option                                | Description                                                                                             | Required | Multiple |
|---------------------------------------|---------------------------------------------------------------------------------------------------------|:--------:|:--------:|
| `<binary-report-path>`                | list of binary reports files                                                                            |          |    +     |
| --classfiles <class-file-path>        | location of the compiled class-files root (must be original and not instrumented)                       |    +     |    +     |
| --exclude <class-name>                | filter to exclude classes, wildcards `*` and `?` are acceptable                                         |          |    +     |
| --excludeAnnotation <annotation-name> | filter to include classes and functions marked by this annotation, wildcards `*` and `?` are acceptable |          |    +     |
| --html <html-dir>                     | generate a HTML report in the specified path                                                            |          |          |
| --include <class-name>                | filter to include classes, wildcards `*` and `?` are acceptable                                         |          |    +     |
| --src <sources-path>                  | location of the source files root                                                                       |    +     |    +     |
| --title <html-title>                  | title in the HTML report                                                                                |          |          |
| --xml <xml-file-path>                 | generate a XML report in the specified path                                                             |          |          |

## Off-line instrumentation

Off-line instrumentation is suitable when using runtime environments that do not support Java agents.
It instruments the files located in the file system and saves the result to the specified directory.

To run classes instrumented offline, you need to add `kover-offline` artifact (with group `org.jetbrains.kotlinx`) to the classpath of the application; in build systems, you need to add a dependency.

You also need to pass the system property `kover.offline.report.path` to the application with the path to the file with the collected coverage.
