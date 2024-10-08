# Kover Command Line Interface

This single jar artifact allows using some of the functionality of Kover Toolset through command-line calls.

Java 1.6 or higher is required for execution.

## Commands

### Offline instrumentation

For information about offline instrumentation, [see](../offline-instrumentation#description).

`java -jar kover-cli.jar instrument [<class-file-path> ...] --dest <dir> [--exclude <class-name>] [--excludeAnnotation <annotation-name>] [--hits] [--include <class-name>]`

| Option                                | Description                                                                                                                | Required | Multiple |
|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------|:--------:|:--------:|
| `<class-file-path>`                   | list of the compiled class-files roots                                                                                     |    +     |    +     |
| --dest <dir>                          | path to write instrumented Java classes to                                                                                 |    +     |          |
| --exclude <class-name>                | filter to exclude classes from instrumentation, wildcards `*` and `?` are acceptable. Excludes have priority over includes |          |    +     |
| --excludeAnnotation <annotation-name> | filter to exclude annotated classes from instrumentation, wildcards `*` and `?` are acceptable                             |          |    +     |
| --hits                                | a flag to enable line hits counting                                                                                        |          |          |
| --include <class-name>                | instrument only specified classes, wildcards `*` and `?` are acceptable                                                    |          |    +     |

### Generating reports

Allows you to generate HTML and XML reports from the existing binary report.

`java -jar kover-cli.jar report [<binary-report-path> ...] --classfiles <class-file-path> [--exclude <class-name>] [--excludeAnnotation <annotation-name>] [--html <html-dir>] [--include <class-name>] --src <sources-path> [--title <html-title>] [--xml <xml-file-path>]`

| Option                                         | Description                                                                                                                                   | Required | Multiple |
|------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|:--------:|:--------:|
| `<binary-report-path>`                         | list of binary reports files                                                                                                                  |          |    +     |
| --classfiles <class-file-path>                 | location of the compiled class-files root (must be original and not instrumented)                                                             |    +     |    +     |
| --exclude <class-name>                         | filter to exclude classes, wildcards `*` and `?` are acceptable                                                                               |          |    +     |
| --include <class-name>                         | filter to include classes, wildcards `*` and `?` are acceptable                                                                               |          |    +     |
| --excludeAnnotation <exclude-annotation-name>  | filter to exclude classes and functions marked by this annotation, wildcards `*` and `?` are acceptable. Excludes have priority over includes |          |    +     |
| --includeAnnotation <include-annotation-name>  | filter to include classes marked by this annotation, wildcards `*` and `?` are acceptable                                                     |          |    +     |
| --excludeInheritedFrom <exclude-ancestor-name> | filter to exclude classes extending the specified class or implementing an interface, wildcards `*` and `?` are acceptable                    |          |    +     |
| --includeInheritedFrom <include-ancestor-name> | filter to include only classes extending the specified class or implementing an interface, wildcards `*` and `?` are acceptable               |          |    +     |
| --html <html-dir>                              | generate a HTML report in the specified path                                                                                                  |          |          |
| --src <sources-path>                           | location of the source files root                                                                                                             |    +     |    +     |
| --title <html-title>                           | title in the HTML report                                                                                                                      |          |          |
| --xml <xml-file-path>                          | generate a XML report in the specified path                                                                                                   |          |          |

## Merging binary reports

Allows you to merge multiple files into single binary report.

If the target file does not exist, a new one is created. Otherwise, the existing file will be overwritten.

`java -jar kover-cli.jar merge [<binary-report-path> ...] --target <merged-report-path>`

| Option                        | Description                  | Required | Multiple |
|-------------------------------|------------------------------|:--------:|:--------:|
| <binary-report-path>          | list of binary reports files |          |    +     |
| --target <merged-report-path> | merged binary report file    |    +     |          |

Example:

`java -jar kover-cli.jar merge build/reports/report1.ic build/reports/report2.ic --target build/reports/merged.ic`