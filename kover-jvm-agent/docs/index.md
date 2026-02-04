# Kover JVM instrumentation agent

## On the fly instrumentation
On the fly instrumentation - modification of the bytecode of classes in order to measure coverage, that occurs when the class is loaded into the JVM.

To instrument the loaded classes, it is necessary to connect a JVM agent to the Java application being launched.

## Getting a readable report
To get a readable coverage report, you need to:
1. [Connect JVM agent on JVM start](#connecting-the-jvm-agent).
2. Run your tests and wait until it exits. When the application is finished, the binary report file will be saved.
3. Using the [Kover CLI](/cli#generating-reports), generate a human-readable report from a binary report.

## Connecting the JVM agent
1. Download the latest version of the Kover JVM agent jar from [maven repository](https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kover-jvm-agent).
2. Put JVM agent jar file in a local directory (Important! renaming a jar file is not allowed).
3. Create a file with agent arguments, [learn more about the arguments](#kover-jvm-arguments-file).
4. Add an argument to the java application startup command `-javaagent:<path_to_agent_jar>=file:<path_to_settings_file>`.
   Example of an application launch command `java -javaagent:/opt/kover-jvm-agent-0.9.6.jar=file:/tmp/agent.args -jar application.jar`.

## Kover JVM arguments file
The arguments file is a set of settings, each of which is written on a new line.
Line format: `argument_name=argument_value`

`report.file` argument is required, while the rest are optional.

List of all available arguments:
- `report.file` - path to the file, which will contain a binary coverage report in ic format. The file is created if it did not exist before
- `report.append` - it is acceptable to specify true or false. if true, then if the file will be appended if the coverage is already stored in it
- `exclude` - specify which classes do not need to be modified when loading. For such classes, the coverage will always be 0.
  
  It is acceptable to use `*` and `?` wildcards, `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
- `exclude.regex` - specify which classes do not need to be modified when loading. For such classes, the coverage will always be 0.

  It is acceptable to specify regex.
- `include` - specify which classes will be modified when loading, all other classes will not.

  It is acceptable to use `*` and `?` wildcards, `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
- `include.regex` - specify which classes will be modified when loading, all other classes will not.

  It is acceptable to specify regex.

It is possible to use `exclude` and `exclude.regex` at the same time, also `include` and `include.regex`. 

Example of arguments file:
```properties
report.file=/tmp/kover-report.ic
exclude=com.example.*
```
