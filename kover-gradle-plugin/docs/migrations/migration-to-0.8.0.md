# Kover migration guide from 0.7.x to 0.8.3

- [Migration steps](#migration-steps)
- [Conceptual changes](#conceptual-changes)
  - [Single kover project extension](#single-kover-project-extension)
  - [The concept of default reports has been removed](#the-concept-of-default-reports-has-been-removed)
  - [Total reports](#total-reports)
  - [Custom reports variants](#custom-reports-variants)
  - [More about the reports variant](#more-about-the-reports-variant)
  - [The format-specific reports filters has been removed](#the-format-specific-reports-filters-has-been-removed)
  - [Added the ability of lazy configuration](#added-the-ability-of-lazy-configuration)
  - [Added public interfaces for Kover tasks](#added-public-interfaces-for-kover-tasks)
- [Migration error messages](#migration-error-messages)


## Migration steps
1. Move all content of extension `koverReport` to `kover { reports { ... } }`.
If you use the extensions property to access the extension
    ```kotlin
    extensions.configure<KoverReportExtension>("koverReport") {
        // content
    }
    ```
   then it is necessary to move all its contents to
    ```kotlin
    extensions.configure<KoverProjectExtension>("kover") {
      reports {
          // content
      }
    }
    ```
2. Move the default reports settings to the total report settings.
   Instead of
    ```kotlin
    koverReport {
        defaults {
            // content
        }
    }
    ```
   write
    ```kotlin
    kover {
        reports {
            total {
                // content
            } 
        }
    }
    ```
3. For Android Projects or Kotlin Multiplatform with Android target:
   If you use merging of Android build variants, you need to create a custom variant, configure it, and then you can generate reports for this variant.

   Instead of
    ```kotlin
    koverReport {
        defaults {
            mergeWith(/*dependent project*/)
            // reports settings
        }
    }
    ```
   write
    ```kotlin
    kover {
        currentProject {
            createVariant("custom") {
                addWithDependencies(/*dependent project*/)
            }
        }
        reports {
            variant("custom") {
                // reports settings
            }
        }
    }
    ```
   See details in [corresponding section](#custom-reports-variants).
4. Delete all `filters` blocks from the settings of a specific report (XML, HTML, verify, log, etc.), e.g.
    ```kotlin
    koverReport {
        defaults {
            html {
                filters {
                    // filters
                }
            }
        }
    }
    ```
    move the filters to common block for all variants settings
    ```kotlin
    kover {
        reports {
            filters {
                // filters
            }     
        }
    }
    ```
    or specific variant settings, like
    ```kotlin
    kover {
        reports {
            total {
                filters {
                    // filters
                }
            }
        }
    }
    ```
    If you need to create specific reports with individual sets of filters, then create a [custom reports variant](#custom-reports-variants).

5. Rename some functions and properties, the actual names are written in error messages or IDE hints
6. For kts files and Gradle versions less than 8.2, it is necessary to replace the assignment operator with a call `set()` function for some properties in DSL.
   These properties can be found by configuration error messages. 

   An example of such properties: `minValue` and `maxValue` in the bound of the verification rule.


## Conceptual changes
### Single kover project extension
The `0.7.x` version used two project extensions named `kover` and `koverReports`.
This was confusing and made it difficult to search through documentation or examples.

Since `0.8.0`, there is only one `kover` extension left.
All the settings that used to be in `koverReports` are now located in
```kotlin
kover {
    reports {
        // setting from old koverReports extension
    }
}
```
The settings from the old `kover` are now either left as is, or moved to `kover { currentProject { ... } }` block.

Following configuration
```kotlin
kover {
    // exclude classes compiled by Java compiler from all reports
    excludeJavaCode()
    
    excludeTests { 
        // exclude Gradle test tasks
        tasks(testTasks)
    }
    excludeInstrumentation {
        // exclude classes from instrumentation
        classes(unistrumentedClasses)
    }
    excludeSourceSets {
        // exclude source classes of specified source sets from all reports
        names(excludedSourceSet)
    }
}
```

Since `0.8.0` looks like:
```kotlin
kover {
    currentProject {
        instrumentation {
            /* exclude Gradle test tasks */
            disabledForTestTasks.addAll(testTasks)
        }

        instrumentation {
            // exclude classes from instrumentation
            excludedClasses.addAll(unistrumentedClasses)
        }

        sources {
            // exclude classes compiled by Java compiler from all reports
            excludeJava = true

            // exclude source classes of specified source sets from all reports
            excludedSourceSets.addAll(excludedSourceSet)
        }
    }
}
```

### The concept of default reports has been removed
In the `0.7.x` version, there were tasks named `koverHtmlReport`, `koverXmlReport`, `koverVerify`, `koverLog`, `koverBinaryReport`, 
which included only classes and tests for JVM target, and it was also possible to add tests from classes of specified Android build variant using
```kotlin
koverReport {
    defaults {
        mergeWith("buildVariant")
    }
}
```

Starting with the `0.8.0` version, these features have been removed.
However, to implement similar capabilities, the concepts of _Total reports_ and _Custom reports_ were introduced.

### Total reports
Since `0.8.0` tasks `koverHtmlReport`, `koverXmlReport`, `koverVerify`, `koverLog`, `koverBinaryReport` are designed 
to generate reports on all classes and tests of the project.
For JVM projects, nothing changes compared to the `0.7.x` version, however, for Android projects, 
running one of these tasks will result running tests  for all build variants present in the project.

### Custom reports variants
In the `0.7.x` version, it was allowed to merge a report for several Android build variants only into default reports,
it was not possible to create several combinations with the arbitrary inclusion of different build variants.

Since `0.8.0` in order to merge several Android build variants, you need to create a custom reports variant.

The newly created variant is initially empty, in order for classes to appear in the report and tests to be executed 
when generating it, need to add existing variants provided from Kotlin targets to it: it can be a `jvm` variant, or any of an Android build variant.
```kotlin
kover {
    currentProject {
        createVariant("custom") {
            add("jvm")
            add("release")
        }
    }
}
```

Creating a variant with a name `custom` will add tasks named `koverHtmlReportCustom`, `koverXmlReportCustom`, `koverVerifyCustom`, `koverLogCustom`, `koverBinaryReportCustom`.

### More about the reports variant
A reports variant is a set of information used to generate a report, namely: 
project classes, a list of Gradle test tasks, classes that need to be excluded from instrumentation.

There are several types of report variants: 
- total variant (have no special name), always created
- variant for classes in JVM target (named `jvm`), created by Kover Gradle Plugin if JVM target is present
- variants for the Android build variant (the name matches the name of the build variant), created by Kover Gradle Plugin if there are Android build variants in the project
- custom variants (the name is specified when creating), declared in the build script configuration

For each variant, a set of tasks is created in the project to generate reports on the information contained in it.

The names are generated according to the following rule:
`koverHtmlReport<VariantName>`, `koverXmlReport<VariantName>`, `koverVerify<VariantName>`, `koverLog<VariantName>`, `koverBinaryReport<VariantName>`


The reports variant can be used in another project to create a merged reports.
To do this, in another project, which we will call the merging project, we will specify a dependency on the project from which we want to import classes and Gradle test tasks.
```kotlin
dependencies {
    kover(project(":lib"))
}
```
As a result, if you run the `:koverHtmlReport` task, it will run all the tests from merging project and `:lib` project 
and generate a report for all classes from merging project and `:lib` project.

However, if you call a task for a named variant, for example `:koverHtmlReportRelease`, then it will run tests for `release` variant from merging project and `:lib` project
and generate a report for classes of `release` variant from merging project and `:lib` project.

At the same time, it is recommended that variant `release` is also present in the `:lib` project, however, 
for technical reasons, such a check may not be implemented in `0.8.0` and subsequent versions.

### The format-specific reports filters has been removed
Previously, it was acceptable to override filters for each report format (XML, HTML, etc.), like
```kotlin
koverReport {
    filters {
        // top-level filters
    }
    defaults {
        filters {
            // filters for all default reports 
        }

        html {
            filters {
                // filters for HTML default report
            }
        }
    }
}
```

This is very confusing, because there are 3 levels in which you can write `filters { }` also it is difficult for users to understand exactly where to write filters - which leads to the fact of copy-paste the same filters are specified for all types of reports (inside xml, html, verify blocks).

Since `0.8.0`, specifying filters for specific type of report (HTML or XML) is deprecated. It is now possible to create custom variants of reports, if it is necessary to generate a report with a different set of filters.
In this case, it is better to create a new custom variant and override the filters in it:
```kotlin
kover {
    currentProject {
        createVariant("customJvm") {
            add("jvm")
        }
    }

    reports {
        variant("customJvm") {
            filters {
                // filters only for customJvm report set
            }
        }
    }
}

```


### Added the ability of lazy configuration
In some cases, the values are not known at the time of configuration, for example, when using convention plugins and extensions in them.

To do this, overloads have been added that allow to work with the value providers (Gradle `Provider<T>` type).

```kotlin
kover {
    reports {
        filters {
            exludes {
               classes(classProvider) 
            }
        }
        
        verify {
            rule {
                bound {
                    minValue = minValueProvider
                }
                // or
                minBound(minValueProvider)
            }
        }
    }
}

```


### Added public interfaces for Kover tasks

Now all Kover report tasks implement interface `kotlinx.kover.gradle.plugin.dsl.tasks.KoverReport`.

Also, a separate interface has been created for each report type:
 - `kotlinx.kover.gradle.plugin.dsl.tasks.KoverXmlReport`
 - `kotlinx.kover.gradle.plugin.dsl.tasks.KoverHtmlReport`
 - `kotlinx.kover.gradle.plugin.dsl.tasks.KoverLogReport`
 - `kotlinx.kover.gradle.plugin.dsl.tasks.KoverVerifyReport`
 - `kotlinx.kover.gradle.plugin.dsl.tasks.KoverBinaryReport`

Adding public interfaces will allow to filter Kover tasks, for example, to specify them in dependencies
```kotlin
tasks.check {
    dependsOn(tasks.matching { it is KoverHtmlReport })
}
```

## Migration error messages

#### Unresolved reference: koverReport
Solution: 
Move content of `koverReport` extension to 
```kotlin
kover {
    reports {
        // place content of `koverReport` here
    }
}
```

#### Unresolved reference: KoverReportExtension
There is no such extension anymore.

If used configuration like 
```kotlin
extensions.configure<KoverReportExtension>("koverReport") {
    // content
}
```
then it is necessary to move all its contents to
```kotlin
extensions.configure<KoverProjectExtension>("kover") {
  reports {
      // content
  }
}
```

#### Default reports was removed, the concepts of total and custom reports are now used.
If you have an Kotlin/JVM project or an Kotlin Multiplatform project without an Android target, then block `total` should be used instead.

Was:
```kotlin
koverReport {
    defaults {
        // content    
    }
}
```
become:
```kotlin
kover {
    reports {
        total {
            // content    
        }    
    }
}
```

#### Kover renaming: Symbol ... was removed, use ... instead
The function or property used has been renamed. Replace the usage with the character specified in the message.

#### Function excludeJavaCode was removed, to exclude all Java sources write here `variants { sources { excludeJava = true } }` or `variants { sources { excludeJava.set(true) } } instead.

Instead of 
```kotlin
kover {
    excludeJavaCode()
}
```
use for Gradle > 8.2
```kotlin
kover {
    variants { 
        sources { 
            excludeJava = true 
        } 
    }
}
```
and for Gradle <= 8.2
```kotlin
kover {
    variants { 
        sources { 
            excludeJava.set(true) 
        } 
    }
}
```

#### Function excludeTests was removed, specify excluded tasks in `currentProject { instrumentation { disabledForTestTasks.addAll(/*name of tasks*/) } }`

Instead of 
```kotlin
kover {
    excludeTests {
        tasks(/*names*/)
    }
}
```
write 
```kotlin
kover {
    currentProject {
        instrumentation {
            disabledForTestTasks.addAll(/*names*/)
        }
    }
}
```

#### Function excludeInstrumentation was removed, specify instrumentation excluded classes in `currentProject { instrumentation { excludedClasses.addAll(/*class names*/) }`

Instead of 
```kotlin
kover {
    excludeInstrumentation {
        classes(/* class names */)
    }
}
```
write 
```kotlin
kover {
    currentProject {
        instrumentation { 
            excludedClasses.addAll(/* class names */) 
        }
    }
}
```

#### "Function excludeSourceSets was removed, specify instrumentation excluded classes in `currentProject { sources { excludedSourceSets.addAll(/*source sets names*/) } }`

Instead of 
```kotlin
kover {
    excludeSourceSets {
        names(/*source sets names*/)
    }
}
```

write 
```kotlin
kover {
    currentProject { 
        sources { 
            excludedSourceSets.addAll(/*source sets names*/) 
        } 
    }
}
```

#### Block mergeWith was removed, create custom reports variant and merge with specified variant

Now it is possible to add provided variant only to custom reports variant.

You need to create a custom variant, configure it, and then you can generate reports for this variant.

Instead of
```kotlin
koverReport {
    defaults {
        mergeWith(/*dependent project*/)
    }
}
```
write
```kotlin
kover {
    currentProject {
        createVariant("custom") {
            addWithDependencies(/*dependent project*/)
        }
    }
}
```
See details in [corresponding section](#custom-reports-variants).

#### Val cannot be reassigned
or
#### Type mismatch: inferred type is ... but Property<...> was expected
or 
#### The integer literal does not conform to the expected type Property<Int>
Reason: the kts file is used in Gradle version less than 8.2

Solution: Update Gradle version or write `.set(value)` instead of ` = value`

#### It is forbidden to override filters for a specific report, use custom report variants to create reports with a different set of filters
It is necessary to delete the `filters` block from the settings of a specific report (XML, HTML, verify, log, etc.).
```kotlin
koverReport {
    defaults {
        html {
            filters {
                // filters
            }
        }
    }
}
```
move the filters to common block for all variants settings 
```kotlin
kover {
    reports {
        filters {
            // filters
        }     
    }
}
```
or specific variant settings
```kotlin
kover {
    reports {
        total {
            filters {
                // filters
            }
        }
    }
}
```
If you need to create specific reports with individual sets of filters, then create a [custom reports variant](#custom-reports-variants).

#### Property isEnabled was renamed to disabled and inverted
Now to disable the rule you need to use the `disabled` property.
```kotlin
kover {
    reports {
        verify {
            rule {
                disabled = true
                // or disabled.set(true) for kts files in Gradle < 8.2
            }
        }
    }
}
```
