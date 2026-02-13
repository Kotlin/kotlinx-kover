# Kover Aggregated Plugin

## Quickstart
This plugin is a prototype for testing ideas related to an alternative configuration method.

The main difference from the existing Kover Gradle Plugin is the reactive content of the report: only those classes and tests that were compiled and executed within the same build get into the report.

To use the plugin, just add into a `settings.gradle.kts` file 
```kotlin
plugins {
    id("org.jetbrains.kotlinx.kover.aggregation") version "0.9.7"
}
```
**There is no need to apply Kover plugin in other places, the `org.jetbrains.kotlinx.kover` plug-in should not be applied anywhere.**

To measure coverage you should pass special `-Pkover` argument to Gradle CLI command and call the command to generate the corresponding report `koverHtmlReport` or `koverXmlReport`.
Example, if you want to measure the coverage of the `test` task:
```shell
./gradlew test -Pkover koverHtmlReport
```

Only those classes that were compiled as part of the current Gradle build are included in the report.
If no compilation tasks were called in the build, the report will be empty.

The report covers only those tests that were run as part of the current build.
If no tests were called in the assembly, then the coverage for all classes will be 0.

## Configuring
At the moment, Kover Settings Plugin allows to configure reports minimally.

There are two ways to configure, using a build script in `settings.gradle.kts` and CLI

Acceptable settings in `settings.gradle.kts` and their equivalent in CLI
```kotlin
kover {
    // -Pkover
    enableCoverage()
    
    reports {
        // -Pkover.projects.includes=:a
        includedProjects.add(":a")
        
        // -Pkover.projects.excludes=:b
        excludedProjects.add(":b")

        // -Pkover.classes.includes=classes.to.exclude.*
        includedClasses.add("classes.to.include.*")

        // -Pkover.classes.excludes=classes.to.include.*
        excludedClasses.add("classes.to.exclude.*")

        // -Pkover.classes.excludesAnnotated=*.Generated*
        excludesAnnotatedBy.add("*.Generated*")
        
        // -Pkover.classes.includesAnnotated=*Included*
        includesAnnotatedBy.add("*Included*")

        // -Pkover.classes.includesHeir=*ParentIncluded
        includesInheritedFrom.add("*ParentIncluded")
        
        // -Pkover.classes.excludesHeir=*ParentExcluded
        excludesInheritedFrom.add("*ParentExcluded")
        
        verify {
            // -Pkover.verify.warn=true
            warningInsteadOfFailure = true
            
            rule {
                name = "custom name"
                disabled = false
                groupBy = GroupingEntityType.APPLICATION
                
                // specify filters for given rule, common filters will be inherited
                // call `clearFilters()` to avoid common filters inheritance
                filters {
                    includedProjects.add(":a2")
                    excludedProjects.add(":b2")
                    includedClasses.add("classes.to.include2.*")
                    excludedClasses.add("classes.to.exclude2.*")
                    excludesAnnotatedBy.add("*.Generated2*")
                    includesAnnotatedBy.add("*Included2*")
                    includesInheritedFrom.add("*ParentIncluded2")
                    excludesInheritedFrom.add("*ParentExcluded2")
                }

                bound {
                    // append minimal bound
                    // -Pkover.verify.min=1
                    minValue = 1

                    // append maximal bound
                    // -Pkover.verify.max=90
                    maxValue = 90
                }
            }
        }
    }
}
```

For example, the following setting is in `settings.gradle.kts`
```kotlin
kover {
    enableCoverage()
    
    reports {
        excludedClasses.add("org.test.MyClass*")
    }
}
```
fully equivalent to Gradle CLI arguments `-Pkover -Pkover.classes.excludes=org.test.MyClass*`

**Any of the specified settings or DSL is preliminary and can be deleted or changed without maintaining backward compatibility**
