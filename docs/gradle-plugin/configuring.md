# Configuring the Kover plugin
- [Configuring default reports](#configuring-default-reports)
- [Configuring Android reports](#configuring-android-reports)
- [Reports filtering](#reports-filtering)
- [Class name with wildcards](#class-name-with-wildcards)
- [Configuring measurements](#configuring-measurements)
- [Verification](#verification)
- [Merging reports](#merging-reports)


## Configuring default reports

The full configuration for default reports is given below.
These reports are available by default when Kover is applied by tasks such as  `:koverHtmlReport` and `:koverXmlReport`.

See also [verification explanations](#verification)

```kotlin
koverReport {
    // configure default reports - for Kotlin/JVM or Kotlin/MPP projects or merged android variants  
    defaults {
        // filters for all default reports 
        filters {
            excludes {
              // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
              classes("com.example.*")
            }
        }
        
        // configure XML report
        xml {
            //  generate an XML report when running the `check` task
            onCheck = false

            // XML report file
            setReportFile(layout.buildDirectory.file("my-project-report/result.xml"))

            // overriding filters only for the XML report 
            filters {
                // exclusions for XML reports
                excludes {
                    // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                    packages("com.another.subpackage")
                    // excludes all classes and functions, annotated by specified annotations (with BINARY or RUNTIME AnnotationRetention), wildcards '*' and '?' are available
                    annotatedBy("*Generated*")
                }

                // inclusions for XML reports
                includes {
                    // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // includes all classes located in specified package and it subpackages
                    packages("com.another.subpackage")
                }
            }
        }

        // configure HTML report
        html {
            // custom header in HTML reports, project path by default
            title = "My report title"

            // custom charset in HTML report files, used return value of `Charset.defaultCharset()` for Kover or UTF-8 for JaCoCo by default.
            charset = "UTF-8"
          
            //  generate a HTML report when running the `check` task
            onCheck = false

            // directory for HTML report
            setReportDir(layout.buildDirectory.dir("my-project-report/html-result"))

            // overriding filters only for the HTML report
            filters {
                // exclusions for HTML reports
                excludes {
                    // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                    packages("com.another.subpackage")
                    // excludes all classes and functions, annotated by specified annotations (with BINARY or RUNTIME AnnotationRetention), wildcards '*' and '?' are available
                    annotatedBy("*Generated*")
                }

                // inclusions for HTML reports
                includes {
                    // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // includes all classes located in specified package and it subpackages
                    packages("com.another.subpackage")
                }
            }
        }

        // configure verification
        verify {
            //  verify coverage when running the `check` task
            onCheck = true

            // add verification rule
            rule {
                // check this rule during verification 
                isEnabled = true

                // specify the code unit for which coverage will be aggregated 
                entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION

                // overriding filters only for current rule
                filters {
                    excludes {
                        // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                        classes("com.example.*")
                        // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                        packages("com.another.subpackage")
                        // excludes all classes and functions, annotated by specified annotations (with BINARY or RUNTIME AnnotationRetention), wildcards '*' and '?' are available
                        annotatedBy("*Generated*")
                    }
                    includes {
                        // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                        classes("com.example.*")
                        // includes all classes located in specified package and it subpackages
                        packages("com.another.subpackage")
                    }
                }

                // specify verification bound for this rule
                bound {
                    // lower bound
                    minValue = 1

                    // upper bound
                    maxValue = 99

                    // specify which units to measure coverage for
                    metric = kotlinx.kover.gradle.plugin.dsl.MetricType.LINE

                    // specify an aggregating function to obtain a single value that will be checked against the lower and upper boundaries
                    aggregation = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                }

                // add lower bound for percentage of covered lines
                minBound(2)

                // add upper bound for percentage of covered lines
                maxBound(98)
            }
        }

        // configure coverage logging
        log {
            //  print coverage when running the `check` task
            onCheck = true
  
            // overriding filters only for the logging report
            filters {
                // exclusions for logging reports
                excludes {
                  // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                  classes("com.example.*")
                  // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                  packages("com.another.subpackage")
                  // excludes all classes and functions, annotated by specified annotations (with BINARY or RUNTIME AnnotationRetention), wildcards '*' and '?' are available
                  annotatedBy("*Generated*")
                }
    
                // inclusions for logging reports
                includes {
                  // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                  classes("com.example.*")
                  // includes all classes located in specified package and it subpackages
                  packages("com.another.subpackage")
                }
            }
            // Add a header line to the output before the lines with coverage
            header = null
            // Format of the strings to print coverage for the specified in `groupBy` group
            format = "<entity> line coverage: <value>%"
            // Specifies by which entity the code for separate coverage evaluation will be grouped
            groupBy = GroupingEntityType.APPLICATION
            // Specifies which metric is used for coverage evaluation
            coverageUnits = MetricType.LINE
            // Specifies aggregation function that will be calculated over all the elements of the same group
            aggregationForGroup = AggregationType.COVERED_PERCENTAGE
        }
    }
}
```

## Configuring Android reports

The full configuration for Android reports for `release` build variant is given below.

See also [verification explanations](#verification)

```kotlin
koverReport {
    // configure report for `release` build variant (Build Type + Flavor) - generated by tasks `koverXmlReportRelease`, `koverHtmlReportRelease` etc
    androidReports("release") {
        // filters for all reports of `release` build variant 
        filters {
            excludes {
                // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                classes("com.example.*")
            }
        }
        
        // configure XML report for `release` build variant (task `koverXmlReportRelease`)
        xml {
            //  generate an XML report when running the `check` task
            onCheck = false

            // XML report file
            setReportFile(layout.buildDirectory.file("my-project-report/result.xml"))

            // overriding filters only for the XML report 
            filters {
                // exclusions for XML reports
                excludes {
                    // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                    packages("com.another.subpackage")
                    // excludes all classes and functions, annotated by specified annotations (with BINARY or RUNTIME AnnotationRetention), wildcards '*' and '?' are available
                    annotatedBy("*Generated*")
                }

                // inclusions for XML reports
                includes {
                    // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // includes all classes located in specified package and it subpackages
                    packages("com.another.subpackage")
                }
            }
        }

        // configure HTML report for `release` build variant (task `koverHtmlReportRelease`)
        html {
            // custom header in HTML reports, project path by default
            title = "My report title"

            // custom charset in HTML report files, used return value of `Charset.defaultCharset()` for Kover or UTF-8 for JaCoCo by default.
            charset = "UTF-8"
          
            //  generate a HTML report when running the `check` task
            onCheck = false

            // directory for HTML report
            setReportDir(layout.buildDirectory.dir("my-project-report/html-result"))

            // overriding filters only for the HTML report
            filters {
                // exclusions for HTML reports
                excludes {
                    // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                    packages("com.another.subpackage")
                    // excludes all classes and functions, annotated by specified annotations (with BINARY or RUNTIME AnnotationRetention), wildcards '*' and '?' are available
                    annotatedBy("*Generated*")
                }

                // inclusions for HTML reports
                includes {
                    // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // includes all classes located in specified package and it subpackages
                    packages("com.another.subpackage")
                }
            }
        }

        // configure verification for `release` build variant (task `koverVerifyRelease`)
        verify {
            //  verify coverage when running the `check` task
            onCheck = true

            // add verification rule
            rule {
                // check this rule during verification 
                isEnabled = true

                // specify the code unit for which coverage will be aggregated 
                entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION

                // overriding filters only for current rule
                filters {
                    excludes {
                        // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                        classes("com.example.*")
                        // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                        packages("com.another.subpackage")
                        // excludes all classes and functions, annotated by specified annotations (with BINARY or RUNTIME AnnotationRetention), wildcards '*' and '?' are available
                        annotatedBy("*Generated*")
                    }
                    includes {
                        // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                        classes("com.example.*")
                        // includes all classes located in specified package and it subpackages
                        packages("com.another.subpackage")
                    }
                }

                // specify verification bound for this rule
                bound {
                    // lower bound
                    minValue = 1

                    // upper bound
                    maxValue = 99

                    // specify which units to measure coverage for
                    metric = kotlinx.kover.gradle.plugin.dsl.MetricType.LINE

                    // specify an aggregating function to obtain a single value that will be checked against the lower and upper boundaries
                    aggregation = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                }

                // add lower bound for percentage of covered lines
                minBound(2)

                // add upper bound for percentage of covered lines
                maxBound(98)
            }
        }

        // configure coverage logging for `release` build variant (task `koverLogRelease`)
        log {
            //  print coverage when running the `check` task
            onCheck = true
    
            // overriding filters only for the logging report
            filters {
              // exclusions for logging reports
              excludes {
                // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                classes("com.example.*")
                // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                packages("com.another.subpackage")
                // excludes all classes and functions, annotated by specified annotations (with BINARY or RUNTIME AnnotationRetention), wildcards '*' and '?' are available
                annotatedBy("*Generated*")
              }
    
              // inclusions for logging reports
              includes {
                // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                classes("com.example.*")
                // includes all classes located in specified package and it subpackages
                packages("com.another.subpackage")
              }
            }
            // Add a header line to the output before the lines with coverage
            header = null
            // Format of the strings to print coverage for the specified in `groupBy` group
            format = "<entity> line coverage: <value>%"
            // Specifies by which entity the code for separate coverage evaluation will be grouped
            groupBy = GroupingEntityType.APPLICATION
            // Specifies which metric is used for coverage evaluation
            coverageUnits = MetricType.LINE
            // Specifies aggregation function that will be calculated over all the elements of the same group
            aggregationForGroup = AggregationType.COVERED_PERCENTAGE
        }
    }
}
```

## Reports filtering
Report filtering is used to exclude one or more classes from the report, and so that they are not taken into account during verification.

Filters consist of inclusion and exclusion rules.
Exclusion rules are names of the classes that must be excluded from the report. Inclusion rules are the classes that should be included in the report, all other classes are excluded from the report.

If inclusion and exclusion rules are specified at the same time, then excludes have priority over includes.
This means that even if a class is specified in both the inclusion and exclusion rules, it will be excluded from the report (e.g. class `com.example.Class1` above).

It is acceptable to filter a class from the report by its fully-qualified name - using `classes` or `packages`. Also acceptable to filter a class, function or getter marked with the specified annotation - `annotatedBy`.

**Kover supports filtering by annotations having `AnnotationRetention` `BINARY` or `RUNTIME`.**

[Wildcards](#class-name-with-wildcards) `*` and `?` are allowed in class names.

There are several levels where you can define filters. Each of the levels has its own priority.
```kotlin

koverReport {
    // common filters for all reports
    filters {
        excludes {
            // exclusions for all reports
        }
        includes {
            // inclusions for all reports
        }
    }
  
    defaults {
        // overriding filters for default reports 
        filters {
            excludes {
                // exclusions for default reports
            }
            includes {
                // inclusions for default reports
            }
        }
      
        xml {
            // overriding filters for default xml report 
            filters {
              excludes {  }
              includes {  }
            }
        }
    }  
    androidReports("release") {
        // overriding filters for reports for `release` build variant 
        filters {
            excludes {
                // exclusions for reports for `release` build variant 
            }
            includes {
                // inclusions for default reports for `release` build variant 
            }
        }
      
        xml {
            // overriding filters for xml report for `release` build variant  
            filters {
              excludes {  }
              includes {  }
            }
        }
    }
}
```

Filter definition levels in ascending order of priority:
- common level - applies to all Kover reports in the current project
- variant level - used for Kover reports for default tasks or some Android build variant
- report level - specifies the xml or html filters of the report, or verification, for the default variant or Android build variant
- verification rule level - applies only to one specific verification rule

If a higher priority filter is specified, it completely replaces the rules written by the level above.
By specifying an empty filter `filters { }`, you can completely disable report filtering.

## Class name with wildcards

Inclusion/exclusion value rules:

* Should be a fully-qualified class name.
* Can contain wildcards:
    * `*` for zero or more of any char.
    * `**` is the same as `*`.
    * `?` for one of any char.
* File and directory names are not allowed.

Examples:

* (good) `my.package.ClassName`
* (good) `my.*.*Name`
* (bad) `my/package/ClassName.kt`
* (bad) `src/my.**.ClassName`

## Configuring measurements
The configuration of measurements can affect not only the content of the report in a particular project, but also the instrumentation of the code and reports in all projects that depend on the configurable.

### Exclusion of JVM source sets

It is possible to exclude from all reports the code declared in certain source sets.

As a side effect, the generation of Kover reports ceases to depend on the compilation tasks of these source sets.

```kotlin
kover {
    excludeSourceSets {
        names("test1", "extra")
    }
}
```

## Verification
When checking a certain verification rule, the entire code is divided into units of code for which it determines whether it was covered (executed) or skipped (not executed).
For example, an entire line from source code or a specific JVM instruction from compiled byte-code can be executed or not.

All units are grouped into one or more groups. 
Based on amount of the executed and non-executed code units, one number (current value) will be calculated for each group using the aggregation function.

It is this current value of the group that will be compared with the minimum and maximum boundaries specified in the configuration.

Type `MetricType` determines for which types of units the coverage will be measured.
It is:
 - `LINE`
 - `INSTRUCTION`
 - `BRANCH`

For comparison with the specified boundaries, the number of covered (executed) or skipped (not executed) units should be aggregated into one number.
`AggregationType` determines exactly how the current measurement value will be calculated:
- `COVERED_COUNT` - the total number of units of code that were executed
- `MISSED_COUNT` - the total number of units of code that were not executed
- `COVERED_PERCENTAGE` - is the number of covered units divided by the number of all units and multiplied by 100
- `MISSED_PERCENTAGE` - is the number of uncovered units divided by the number of all units and multiplied by 100

To calculate the current value, units are grouped by various entities.
By default, all application units of code are grouped by a single application entity, so one current value is calculated for the entire application using the aggregating function.

But you can group code units by other named entities.
The `GroupingEntityType` type is used for this:
- `APPLICATION` - one current coverage value for the entire application will be calculated
- `CLASS` - the current value will be calculated individually for each class. So the bounds will be checked for each class
- `PACKAGE` - the current value will be calculated individually for all classes in each package. So the bounds will be checked for each package

## Merging reports
If it is necessary to generate a report for a specific build variant using the Kover default report tasks, it is possible to combine the contents of the Android report and the default report.

This is done by configuring default reports
```
koverReport {
    defaults {
        mergeWith("release")
    }
}
```
This will add the reports contents of `release` Android build variant to default reports.

This can be useful if only one command is run in a multimodule project for uniformity, instead of many different ones for each module.
Or if in a multiplatform project it is necessary to generate a single report for JVM and Android targets.

**If the report for the build variant was added to the default report, the measurements for this variant are exported and will be used in dependencies.**
