# Kover Maven Plugin

Maven plugin to measure test coverage and generate human-readable reports with coverage values.

## Table of contents
 * [Requirements](#requirements)
 * [Current limitations](#current-limitations)
 * [Quickstart](#quickstart)
 * [Maven Goals](#goals)
 * [Multi-module projects](#multi-module-projects)
 * [Configuration](#configuration)
 * [Coverage values](#coverage-values)
 * [Examples](#examples)

## Requirements
- Maven 3.0 or higher
- Java 1.8 or higher for Maven runtime

## Current limitations
- only instrumentation of tests in `test` goal is supported; `it-tests` tests are not supported yet
- if several Kover JVM agents are specified when running the tests, then only the first one will work
- simultaneous use of several instrumentation agents can lead to unpredictable consequences and unstable operation

## Quickstart
To use Kover coverage measurement it is necessary to add plugin `org.jetbrains.kotlinx:kover-maven-plugin:0.8.2` to build configuration in `pom.xml` and create executions for used goals.

With the following configuration HTML and XML reports will be generated, and verification rules will be checked on `verify` phase:
```xml
    <build>
        <!-- other build configs -->
        
        <plugins>
            <!-- other plugins -->
            
            <plugin>
                <groupId>org.jetbrains.kotlinx</groupId>
                <artifactId>kover-maven-plugin</artifactId>
                <version>0.8.2</version>
                <executions>
                    <!-- instrument test tasks -->
                    <execution>
                        <id>instr</id>
                        <goals>
                            <goal>instrumentation</goal>
                        </goals>
                    </execution>
                    
                    <!-- generate XML report in verify phase -->
                    <execution>
                        <id>kover-xml</id>
                        <goals>
                            <goal>report-xml</goal>
                        </goals>
                    </execution>
                    
                    <!-- generate HTML report in verify phase -->
                    <execution>
                        <id>kover-html</id>
                        <goals>
                            <goal>report-html</goal>
                        </goals>
                    </execution>

                    <!-- check coverage rules in verify phase -->
                    <execution>
                        <id>kover-verify</id>
                        <goals>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

## Goals
Kover Maven Plugin provides the following goals:
 - `instrumentation` - activate the measurement of the coverage in tests
 - `report-xml` - generate XML coverage report in JaCoCo format
 - `report-html` - generate HTML coverage report
 - `report-ic` - generate binary coverage report in intellij coverage agent format
 - `verify` - check specified coverage rules
 - `log` - print coverage values to the Maven log 

## Multi-module projects
If the project consists of several modules, then the source classes and tests can be distributed across several modules.

In this case, it becomes necessary to generate reports for several modules at once.

In order to activate the creation of reports on several projects, you need to add this configuration flag:
`<aggregate>true</aggregate>` into the Kover Plugin `configuration` block.

Then, the classes in the report will be collected for all modules specified in the `dependencies` block, except for those for which the scope is `test`.
And the coverage will be collected from tests located in modules that are also in the `dependencies` block, including dependencies with the `test` scope.

See the full [example project](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-maven-plugin/examples/merged-report).

## Configuration

All available configuration options are shown below:
```xml

<plugin>
    <groupId>org.jetbrains.kotlinx</groupId>
    <artifactId>kover-maven-plugin</artifactId>
    <version>0.8.2</version>
    <executions>
        <!-- instrument test tasks -->
        <execution>
            <id>instr</id>
            <goals>
                <goal>instrumentation</goal>
            </goals>
        </execution>
        
        <!-- generate XML report in verify phase -->
        <execution>
            <id>kover-xml</id>
            <goals>
                <goal>report-xml</goal>
            </goals>
        </execution>
        
        <!-- generate HTML report in verify phase -->
        <execution>
            <id>kover-html</id>
            <goals>
                <goal>report-html</goal>
            </goals>
        </execution>

        <!-- check coverage rules in verify phase -->
        <execution>
            <id>kover-verify</id>
            <goals>
                <goal>verify</goal>
            </goals>
        </execution>

        <!-- generate IC report -->
        <execution>
            <id>kover-ic</id>
            <goals>
                <goal>report-ic</goal>
            </goals>
        </execution>

        <!-- print coverage values to the log -->
        <execution>
            <id>kover-log</id>
            <goals>
                <goal>log</goal>
            </goals>
        </execution>
    </executions>

    
    <configuration>
        <!-- Skip execution of Kover goals -->
        <!-- Boolean, default: false -->
        <skip>false</skip>
        
        <!-- Classes excluded from instrumentation. -->
        <!-- The specified classes will not be modified when loaded into the JVM and the coverage for them will always be 0%. -->
        <!-- Wildcards `*` and `?` are acceptable. -->
        <!-- List of String values, default: empty list -->
        <uninstrumentedClasses>com.example.*,excluded.from.instrumentation.*</uninstrumentedClasses>

        <!-- Property name to pass java agent argument to the JVM in which tests are running. -->
        <!-- String, default: argLine -->
        <agentPropertyName>argLine</agentPropertyName>
        
        <!-- Filters to limit the code that gets into the report. -->
        <!-- Used in goals: report-xml, report-html, verify, report-ic, log -->
        <!-- Complex type, default: no filters -->
        <filters>
            <filters>
                <!-- Specify classes and methods that should be excluded from reports -->
                <!-- Excludes have priority over includes. -->
                <excludes>
                    <!-- Exclude specified classes from report -->
                    <!-- Using fully-qualified JVM class names, wildcards `*` and `?` are acceptable. -->
                    <!-- List of String values, default: empty list -->
                    <classes>com.example.*.ExcludedByName,com.example.*.serializables.*$Companion</classes>

                    <!-- Exclude classes marked by specified annotations -->
                    <!-- Using fully-qualified JVM class names, wildcards `*` and `?` are acceptable. -->
                    <!-- List of String values, default: empty list -->
                    <annotatedBy>*.Generated</annotatedBy>


                    
                    <!-- Exclude classes inherited from the specified classes, or implementing the specified interfaces. -->
                    <!-- Using fully-qualified JVM class names, wildcards `*` and `?` are acceptable. -->
                    <!--  The entire inheritance tree is analyzed; a class may inherit the specified class/interface indirectly and still be included in the report, unless the specified class/interface is located outside of the application (see below).-->
                    <!--  The following classes and interfaces can be specified in arguments:-->
                    <!--    *  classes and interfaces declared in the application-->
                    <!--    *  classes and interfaces declared outside the application, however they are directly inherited or implemented by any type from the application-->
                    <!--  Due to technical limitations, if a specified class or interface is not declared in the application and not extended/implemented directly by one of the application types, such a filter will have no effect.-->
                    <!-- List of String values, default: empty list -->  
                    <inheritedFrom>java.lang.AutoCloseable</inheritedFrom>

                    <!-- Exclude classes of specified projects by project names. -->
                    <!-- Wildcards `*` and `?` are acceptable. -->
                    <!-- List of String values, default: empty list -->
                    <projects>test-utils</projects>
                </excludes>
                
                <!-- Specify classes that should be included to reports, all other classes are not included in the report. -->
                <!-- Excludes have priority over includes. -->
                <includes>
                    <!-- content is the same as in excludes block -->
                </includes>
            </filters>
        </filters>

        <!-- Specify to collect coverage from project dependencies. -->
        <!-- Used in goals: report-xml, report-html, verify, report-ic, log -->
        <!-- Boolean, default: false -->
        <aggregate>true</aggregate>
        
        <!-- Binary reports that built in advance, before the start of the project build. -->
        <!-- Used in goals: report-xml, report-html, verify, report-ic, log -->
        <!-- List of File values, default: empty list -->
        <additionalBinaryReports>${project.basedir}/external/report.ic</additionalBinaryReports>
        
        <!-- Specify subdirectory name for HTML report. -->
        <!-- This subdirectory will be created in ${project.reporting.outputDirectory}/kover -->
        <!-- String, default: html -->
        <htmlDirName>html</htmlDirName>

        <!-- Specify title for human-readable reports. -->
        <!-- Used in goals: report-xml, report-html -->
        <!-- String, default: ${project.name} -->
        <title>My title</title>

        <!-- Specify charset for HTML reports. -->
        <!-- String, default: UTF-8 -->
        <charset>UTF-8</charset>

        <!-- Specify path to the IC report file. -->
        <!-- File, default: ${project.reporting.outputDirectory}/kover/report.ic -->
        <icFile>${project.build.directory}/custom.ic</icFile>

        <!-- Specify path to the XML report file. -->
        <!-- File, default: ${project.reporting.outputDirectory}/kover/report.xml -->
        <xmlFile>${project.build.directory}/custom.xml</xmlFile>

        <!-- Specifies by which entity the code for particular coverage evaluation will be grouped. -->
        <!-- For more information refer to the section 'Coverage values' -->
        <!-- Enum value of {APPLICATION, CLASS, PACKAGE}, default: APPLICATION -->
        <logGroupBy>APPLICATION</logGroupBy>

        <!-- The type of application code division (unit type) whose unit coverage will be considered independently. -->
        <!-- For more information refer to the section 'Coverage values' -->
        <!-- Enum value of {LINE, INSTRUCTION, BRANCH}, default: LINE -->
        <logCoverageUnit>BRANCH</logCoverageUnit>

        <!-- Specifies aggregation function that will be calculated over all the units of the same group.  -->
        <!-- This function used to calculate the aggregated coverage value, it uses the values of the covered and uncovered units of type logCoverageUnit as arguments. -->
        <!-- Result value will be printed. -->
        <!-- For more information refer to the section 'Coverage values' -->
        <!-- Enum value of {COVERED_COUNT, MISSED_COUNT, COVERED_PERCENTAGE, MISSED_PERCENTAGE}, default: COVERED_PERCENTAGE -->
        <logAggregationForGroup>MISSED_COUNT</logAggregationForGroup>


        <!-- Format of the strings to print coverage for the specified in [groupBy] group.-->
        <!-- The following placeholders can be used: -->
        <!--  {value} - coverage value -->
        <!--  {entity} - name of the entity by which the grouping took place. `application` if [groupBy] is [GroupingEntityType.APPLICATION]. -->
        <!-- String, default: {entity} line coverage: {value}% -->
        <logFormat>Full coverage is {value}%</logFormat>

        <!-- In case of a verification error, print a message to the log with the warn level instead of the Maven goal execution error. -->
        <!-- Boolean, default: false -->
        <warningInsteadOfFailure>true</warningInsteadOfFailure>
        
        <!-- List of coverage rules to check in verify goal. -->
        <!-- List of elements of complex type, default: no rules -->
        <rules>
            <!-- create new coverage verification rule -->
            <rule>
                <!-- Name of the rule -->
                <!-- String, default: empty string -->
                <name>package covered lines</name>
                
                <!-- Specifies by which entity the code for separate coverage evaluation will be grouped. -->
                <!-- For more information refer to the section 'Coverage values' -->
                <!-- Enum value of {APPLICATION, CLASS, PACKAGE}, default: APPLICATION -->
                <groupBy>PACKAGE</groupBy>
                
                <!-- Override filters individually for this rule. -->
                <!-- If at least one filter is specified, it will be used instead of the general filter declared above. -->
                <!-- Complex type, default: no filters -->
                <filters>
                    <!-- same content as in filters tag above -->
                </filters>

                <!-- List of restrictions on coverage values. -->
                <!-- List of elements of complex type, default: no bounds -->
                <bounds>
                    <bound>
                        <!-- Specifies aggregation function that will be calculated over all the units of the same group.-->
                        <!-- This function used to calculate the aggregated coverage value, it uses the values of the covered and uncovered units of type [coverageUnits] as arguments.-->
                        <!-- Result value will be compared with the bounds.-->
                        <!-- For more information refer to the section 'Coverage values' -->
                        <!-- Enum value of {COVERED_COUNT, MISSED_COUNT, COVERED_PERCENTAGE, MISSED_PERCENTAGE}, default: COVERED_PERCENTAGE -->
                        <aggregationForGroup>MISSED_COUNT</aggregationForGroup>

                        <!-- The type of application code division (unit type) whose unit coverage will be considered independently.-->
                        <!-- It affects which blocks the value of the covered and missed units will be calculated for.-->
                        <!-- For more information refer to the section 'Coverage values' -->
                        <!-- Enum value of {LINE, INSTRUCTION, BRANCH}, default: LINE -->
                        <coverageUnits>LINE</coverageUnits>

                        <!-- Specifies maximal value to compare with aggregated coverage value.-->
                        <!-- The comparison occurs only if the value is present.-->
                        <!-- Int, default: null -->
                        <maxValue>90</maxValue>

                        <!-- Specifies minimal value to compare with aggregated coverage value.-->
                        <!-- The comparison occurs only if the value is present.-->
                        <!-- Int, default: null -->
                        <minValue>10</minValue>
                    </bound>
                </bounds>
            </rule>
        </rules>
    </configuration>
</plugin>
```

### Coverage values
During verification, the entire code is divided into units for which Kover determines whether it was covered (executed) or skipped (not executed).
For example, an entire line from source code or a specific JVM instruction from compiled byte-code can be executed or not.

All units are grouped into one or more groups.
Based on amount of the executed and non-executed code units, one number (coverage value) will be calculated for each group using the aggregation function.

Type `CoverageUnit` determines for which types of units the coverage will be measured.
It can be:
- `LINE`. This is a default value.
- `INSTRUCTION`.
- `BRANCH`.

For comparison with the specified boundaries, the number of covered (executed) or skipped (not executed) units should be aggregated into one number.
`AggregationType` determines exactly how the current measurement value will be calculated:
- `COVERED_COUNT` - the total number of units of code that were executed.
- `MISSED_COUNT` - the total number of units of code that were not executed.
- `COVERED_PERCENTAGE` - is the number of covered units divided by the number of all units and multiplied by 100. This is a default value.
- `MISSED_PERCENTAGE` - is the number of uncovered units divided by the number of all units and multiplied by 100.

To calculate the coverage value, units are grouped by various entities.
By default, all application units of code are grouped by a single application entity, so one coverage value is calculated for the entire application using the aggregating function.

But you can group code units by other named entities.
The `GroupingEntityType` type is used for this:
- `APPLICATION` - one current coverage value for the entire application will be calculated. This is a default value.
- `CLASS` - the coverage value will be calculated individually for each class. So the bounds will be checked for each class.
- `PACKAGE` - the coverage value will be calculated individually for all classes in each package. So the bounds will be checked for each package.

## Examples
- Enable all Kover goals in Kotlin project: [directory](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-maven-plugin/examples/all-goals)
- Specifying common filters for all reports and verification rules: [directory](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-maven-plugin/examples/filters-common)
- Override filters in verification rules: [project](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-maven-plugin/examples/filters-rules)
- Use externally generated binary IC report to merge coverage: [project](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-maven-plugin/examples/additional-binary-report)
- Override standard parameter of surefire plugin to pass arguments to JVM: [project](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-maven-plugin/examples/change-agent-line)
- Change paths to the XML, HTML and IC reports: [project](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-maven-plugin/examples/change-paths)
- Create merged (aggregated) report: [project](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-maven-plugin/examples/merged-report)
- Using Kover Maven Plugin in `site` lifecycle (`reporting` block) [project](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-maven-plugin/examples/site)
- Skip Kover goals by configuration: [project](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-maven-plugin/examples/skip-config) 
- Adding coverage verification rules: [project](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-maven-plugin/examples/verify-error) 
- Print warning instead of verification error: [project](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-maven-plugin/examples/verify-warn)
- Configure coverage logging: [project](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-maven-plugin/examples/logs)
- Exclude class from instrumentation: [project](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-maven-plugin/examples/exclude-instrumentation)
