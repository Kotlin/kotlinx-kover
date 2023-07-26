# Kover migration guide from 0.6.x to 0.7.3

## Migration steps
To migrate to version `0.7.3`, you must follow all steps below if they are applicable to your project.

### Merge reports config was removed
Now all Kotlin report tasks (`koverHtmlReport`, `koverXmlReport`, `koverVerify`) are in single copy, they can be both single-project or merged cross-projects reports.  

To make Kover tasks merged now you need specify as a dependency all the projects used.

For example:
```
dependencies {
    kover(project(":core"))
    kover(project(":utils"))
}
```
in this case report will be generated for current project joined with `core` and `utils` projects.

_If you don't specify these dependencies, then the reports will be generated only for the current project._ 

#### Kotlin JVM and Kotlin Multiplatform projects
If you using merged-reports move all report configurations from `koverMerged` extension (with type `kotlinx.kover.api.KoverMergedConfig`) to [default reports configuration block](#report-settings-for-kotlin-jvm-and-kotlin-multiplatform-projects). 

#### Kotlin Android projects
If you using merged-reports move all report configurations from `koverMerged` extension (with type `kotlinx.kover.api.KoverMergedConfig`) to [Android reports configuration block](#report-settings-for-android-projects).



### New report configurations
Now all report settings are moved to a separate extension.

#### Report settings for Kotlin JVM and Kotlin Multiplatform projects
XML, HTML, verify reports configured in special Kover extension
```
koverReport {
    defaults {
        // reports configs for XML, HTML, verify reports
    }
}
```

With Gradle API syntax:

for kts build script
```
extensions.configure<kotlinx.kover.gradle.plugin.dsl.KoverReportExtension> {
    defaults {
        // reports configs for XML, HTML, verify reports
    }
}
```
for Groovy build script
```
extensions.configure(kotlinx.kover.gradle.plugin.dsl.KoverReportExtension.class) {
    defaults {
        // reports configs for XML, HTML, verify reports
    }
}
```

#### Report settings for Android projects
For Android, you may configure report for a certain build variant (Build Type + Flavor)
```
koverReport {
    androidReports("release") {
        // reports configs for XML, HTML, verify reports for 'release' build variant 
    }
}
```

With Gradle API syntax:

for kts build script
```
extensions.configure<kotlinx.kover.gradle.plugin.dsl.KoverReportExtension> {
    androidReports("release") {
        // reports configs for XML, HTML, verify reports for 'release' build variant 
    }
}
```
for Groovy build script
```
extensions.configure(kotlinx.kover.gradle.plugin.dsl.KoverReportExtension.class) {
    androidReports("release") {
        // reports configs for XML, HTML, verify reports for 'release' build variant 
    }
}
```

#### Reports configs
Full list of report configurations with descriptions.

Use suitable settings for you
```
koverReport {
    // common filters for all reports of all variants
    filters {
        // exclusions for reports
        excludes {
            // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
            classes("com.example.*")
            // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
            packages("com.another.subpackage")
            // excludes all classes and functions, annotated by specified annotations, wildcards '*' and '?' are available
            annotatedBy("*Generated*")
        }
        
        // inclusions for reports
        includes {
            // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
            classes("com.example.*")
            // includes all classes located in specified package and it subpackages
            packages("com.another.subpackage")
        }
    }

    // configure default reports - for Kotlin/JVM or Kotlin/MPP projects or merged android variants  
    defaults {
        // add reports of 'release' Android build variant to default reports - applicable only for Android projects
        mergeWith("release")
    
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
                    // excludes all classes and functions, annotated by specified annotations, wildcards '*' and '?' are available
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
                    // excludes all classes and functions, annotated by specified annotations, wildcards '*' and '?' are available
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
                        // excludes all classes and functions, annotated by specified annotations, wildcards '*' and '?' are available
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
    
    // configure reports for 'release' build variant
    androidReports("release") {
        // same as for 'defaults' with the exception of 'mergeWith'
    }
    
}
```


### Renaming Coverage Engines to Coverage Tools
Previously called engines have been renamed tools.

Kover instrumentation agent and report generator are used by default.

To use JaCoCo tools with default version:
```
kover {
    useJacoco()
}
```

To use JaCoCo tools with specified version:
```
kover {
    useJacoco("0.8.10")
}
```



### report filters now not affects instrumentation
In version `0.6.1`, report filters also excluded classes from instrumentation.

However, starting from version `0.7.0`, classes are excluded from instrumentation separately (see [this](#excluding-from-instrumentation))

### Kover extension for test tasks was removed
The `kover` task extension has been removed from the JVM test tasks.

Class filters for instrumentation have been moved to a [special extension](#excluding-from-instrumentation).

The ability to rename binary raw report files has been removed, now they are always located in the directory `build/kover/raw-reports` and are named the same as the test task + `".ic"` for Kover and `".exec"` for JaCoCo.


### Excluding from instrumentation
Now to exclude classes from instrumentation, you need to configure a special extension
```
kover {
    excludeInstrumentation {
        // excludes from instrumentations classes by fully-qualified JVM class name, wildcards '*' and '?' are available
        classes("*Foo*", "*Bar")
        
        // excludes from instrumentations all classes located in specified package and it subpackages, wildcards '*' and '?' are available
        packages("com.project")
    }
}
```

### rename dependency in buildSrc
Dependencies like `classpath "org.jetbrains.kotlinx:kover:$koverVersion"` or `implementation("org.jetbrains.kotlinx:kover:$koverVersion")` should be replaced by `classpath "org.jetbrains.kotlinx:kover-gradle-plugin:$koverVersion"`


## Migration error for Kotlin script
### Using 'isDisabled: Boolean' is an error
_Solution_

Use property 'disabledForProject' instead.

---

### disabledForProject.set(    Unresolved reference
_Solution_

Use function `disable()`

---

### Using 'CoverageEngineVariant' is an error
or 
### Using 'engine: Nothing?' is an error
_Solution_

Use appropriate functions instead: `useJacoco()` or `useJacoco("version")`

---

### Using 'filters(() -> Unit): Unit' is an error
_Solution_

Move all common report filters to the block
```
koverReport {
    filters {
        // filters for reports of all types
    }
}
```

---

### Using 'classes(() -> Unit): Unit' is an error
_Solution_

Rewrite class filters, for exclusions
```
    filters {
        excludes {
            classes("class1", "class2", "class3")
        }
    }
```

for inclusions
```
    filters {
        includes {
            classes("class1", "class2", "class3")
        }
    }
```

---

### Using 'instrumentation(KoverTestsExclusions.() -> Unit): Unit' is an error
_Solution_

Use block `excludeTests` instead.

---

### Using 'excludeTasks: MutableList<String>' is an error
_Solution_

Instead of `excludeTasks += "test"` use `tasks("test1")` or `tasks("test1", "test2")` for multiple test tasks

---

###  Using 'xmlReport(() -> Unit): Unit' is an error
_Solution_

Configure XML report filter in block
```
koverReport {
    defaults {
        xml {
            // configs ...
        }
    }
}
```

---

###  Using 'htmlReport(() -> Unit): Unit' is an error
_Solution_

Configure HTML report filter in block
```
koverReport {
    defaults {
        html {
            // configs ...
        }
    }
}
```

---

### Using 'verify(() -> Unit): Unit' is an error
_Solution_

Configure verification report filter in block
```
koverReport {
    defaults {
        verify {
            // configs ...
        }
    }
}
```

---

### Using 'reportFile: Nothing?' is an error
_Solution_

Property usage `reportFile.set(file)` should be replaced by `setReportFile(file)` 

---

### Using 'name: String?' is an error
_Solution_

`name` property is deprecated, specify rule custom name in `rule` function
```
koverReport {
    defaults {
        verify {
            rule("My rule name") {
                // ... rule definition
            }
        }
    }
}
```

---

### Using 'reportDir: Nothing?' is an error
_Solution_

Property usage `reportDir.set(dir)` should be replaced by `setReportDir(dir)`

---

### Using 'overrideFilters(() -> Unit): Unit' is an error
_Solution_

To override report filters, instead of `overrideFilters` call `filters`

---

### Using 'target: GroupingEntityType' is an error
_Solution_

Instead of `target` use `entity`.

---

### onCheck.set(  Unresolved reference.
_Solution_

Use assignment `onCheck = `

---

### Using 'VerificationTarget' is an error
_Solution_

Instead of class `kotlinx.kover.api.VerificationTarget` use `kotlinx.kover.gradle.plugin.dsl.GroupingEntityType`

---

### Using 'ALL' is an error
_Solution_

Use `APPLICATION` instead of `ALL`

---

### Using 'overrideClassFilter(() -> Unit): Unit' is an error
_Solution_

To override verification rule filters, use `filters`

---

### Using 'includes: MutableList<String>' is an error
_Solution_

Instead of property `includes += listOf("class1", "class2")` use block
```
    includes {
        classes("class1", "class2")
    }
```

---

### Using 'excludes: MutableList<String>' is an error
_Solution_

Instead of property `excludes += listOf("class1", "class2")` use block
```
    excludes {
        classes("class1", "class2")
    }
```

---

### Using 'counter: MetricType' is an error
_Solution_

Use `metric` property.

---

### Using 'CounterType' is an error. Class was removed
_Solution_

Use class `kotlinx.kover.gradle.plugin.dsl.MetricType`

---

### Using 'valueType: AggregationType' is an error
_Solution_

Use `aggregation` property.

---

### Using 'VerificationValueType' is an error
_Solution_

Use class `kotlinx.kover.gradle.plugin.dsl.AggregationType`

---
### Using 'KoverTaskExtension' is an error. Class was removed
or
### Using 'isDisabled: Boolean' is an error. Kover test task config was removed
or 
### Using 'reportFile: Nothing?' is an error. Kover test task config was removed
or
### Using 'includes: MutableList<String>' is an error. Kover test task config was removed
or 
### Using 'excludes: MutableList<String>' is an error. Kover test task config was removed
_Solution_

Remove test task extension and move instrumentation configuration to root extension
```
kover {
    excludeTests {
        // to disable instrumentation for specified test tasks
        tasks("testTaskName")
    }
    
    excludeInstrumentation {
        // to disable instrumentation of specified class by fully-qualified JVM class name
        classes("class1", "class2")
    }
}
```

---

### Could not find org.jetbrains.kotlinx:kover:0.7.3
_Solution_

rename dependencies in _buildSrc_ from `org.jetbrains.kotlinx:kover:` to `org.jetbrains.kotlinx:kover-gradle-plugin:`

---

### Unresolved reference: KoverExtension
_Solution_

Replace `KoverExtension` (or `kotlinx.kover.api.KoverExtension`) by `kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension`

---

### Using 'KoverMergedConfig' is an error. Kover merged report config was removed
or 
### Using 'enable(): Unit' is an error. Kover merged report config was removed
or 
### Using 'filters(() -> Unit): Unit' is an error. Kover merged report config was removed
or 
### Using 'xmlReport(() -> Unit): Unit' is an error. Kover merged report config was removed
or
### Using 'htmlReport(() -> Unit): Unit' is an error. Kover merged report config was removed
or 
### Using 'verify(() -> Unit): Unit' is an error. Kover merged report config was removed
_Solution_

See [migrate instruction](#merge-reports-config-was-removed).

---

### Using 'IntellijEngine' is an error
_Solution_

Remove the engine change, the Kover toolkit is used by default

---

### Using 'DefaultIntellijEngine' is an error
_Solution_

Remove the engine change, embedded Kover toolkit is used by default

---

### Using 'JacocoEngine' is an error
_Solution_

Use function `useJacoco("version")` instead.

---

### Using 'DefaultJacocoEngine' is an error
_Solution_

Use function `useJacoco()` instead.

---

### Using 'KoverVersions' is an error
_Solution_

Use class with fully-qualified name `kotlinx.kover.gradle.plugin.dsl.KoverVersions`
