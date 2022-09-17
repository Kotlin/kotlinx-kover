# Kover migration guide from 0.5.x to 0.6.0

The new API allows you to configure Kover in a more flexible manner, while being more concise than the previous API.
From now on, there is no need to configure Kover or test tasks separately.

## Main differences
- applying the plugin to the root project no longer causes it to be recursively applied to all subprojects - you must explicitly apply it to all projects that will be covered
- merged tasks are not created by default. You must explicitly enable it if necessary (for details [see](#merged-report-changes))
- the extension `kover {}` is used to configure tasks `koverXmlReport`, `koverHtmlReport`, `koverVerify`, test tasks, instead of configuring these tasks directly
- the extension `koverMerged {}` is used to configure tasks `koverMergedXmlReport`, `koverMergedHtmlReport`, `koverMergedVerify`, instead of configuring these tasks directly
- task `koverCollectReports` was removed

## Merged report changes

In the new API, merged tasks are not created by default. To create them, you need to use the plugin in the `koverMerged` extension, it is necessary to call the function `enable()`.
e.g.
```
koverMerged.enable()
```
or
```
koverMerged {
    enable()
}
```
or 
```
extensions.configure<KoverMergedConfig> {
    enable()
}
```

Now any merged tasks settings occur only in the `koverMerged` extension.
By default, tasks use the results of measuring the coverage of the project in which they were created and all subprojects.
At the same time, it is important that the Kover plugin is used in all these projects, as well as that they use the same variant (vendor and version) of the coverage engine.

For example, it can be done this way
```
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlinx:kover:0.6.0-Beta")
    }
}

apply(plugin = "kover")

extensions.configure<KoverMergedConfig> {
    enable()
    // configure merged tasks
}

allprojects {
    apply(plugin = "kover")

    extensions.configure<KoverProjectConfig> {
        // `true` - to disable the collection of coverage metrics for tests from this project
        isDisabled.set(false)
    
        // configure engine variant 
        engine.set(kotlinx.kover.api.IntellijEngine("1.0.657"))
        
        // configure project's tasks if needed
    }
}
```

In order not to use measurements from some projects now instead of `disabledProjects` property for `kover` extension you need to use `koverMerged` extension:
```
extensions.configure<KoverMergedConfig> {
    enable()
    filters {
        projects {
            excludes.addAll("project-to-exclude", ":path:to:exclude")
        }
    }
}
```

Instead of configuring the merged XML report task 
```
koverMergedXmlReport {
    // config task
}
```
or
```
tasks.withType<KoverMergedXmlReportTask> {
    // config task
}
```
you need to configure `koverMerged` extension:
```
extensions.configure<KoverMergedConfig> {
    enable()
    xmlReport {
        // config task
    }
}
```

Instead of configuring the merged HTML report task
```
koverMergedHtmlReport {
    // config task
}
```
or
```
tasks.withType<KoverMergedHtmlReportTask> {
    // config task
}
```
you need to configure `koverMerged` extension:
```
extensions.configure<KoverMergedConfig> {
    enable()
    htmlReport {
        // config task
    }
}
```


Instead of configuring the merged verification report task
```
koverMergedVerify {
    // config task
}
```
or
```
tasks.withType<KoverMergedVerificationTask> {
    // config task
}
```
you need to configure `koverMerged` extension:
```
extensions.configure<KoverMergedConfig> {
    enable()
    verify {
        // config task
    }
}
```

## Migration Issues

### Root kover extension

#### type of `isDisabled` property changed from `Boolean` to `Property<Boolean>`.

_Error message:_

```
Val cannot be reassigned
```

_Solution for Kotlin script:_ change `isDisabled = true` to `isDisabled.set(true)`

#### Properties `coverageEngine`, `intellijEngineVersion` and `jacocoEngineVersion` were removed.

_Error messages:_

```
Using 'coverageEngine: Property<CoverageEngine>' is an error
```
```
Using 'intellijEngineVersion: Property<String>' is an error
```
```
Using 'jacocoEngineVersion: Property<String>' is an error
```

_Solution:_

Use property `engine` - it combines version and coverage engine vendor.

To use IntelliJ Coverage Engine with default version write  `engine.set(kotlinx.kover.api.DefaultIntellijEngine)`
(Kotlin) or `engine = kotlinx.kover.api.DefaultIntellijEngine.INSTANCE` (Groovy).

To use IntelliJ Coverage Engine with custom version write  `engine.set(kotlinx.kover.api.IntellijEngine("version"))`
(Kotlin) or `engine = kotlinx.kover.api.IntellijEngine("version")` (Groovy).

To use JaCoCo Coverage Engine with default version write  `engine.set(kotlinx.kover.api.DefaultJacocoEngine)`
(Kotlin) or `engine = kotlinx.kover.api.DefaultJacocoEngine.INSTANCE` (Groovy).

To use JaCoCo Coverage Engine with custom version write  `engine.set(kotlinx.kover.api.JacocoEngine("version"))`
(Kotlin) or `engine = kotlinx.kover.api.JacocoEngine("version")` (Groovy).

#### Property "generateReportOnCheck" was removed

Use the properties individually for each report

```
kover {
    xmlReport {
        onCheck.set(true)
    }
   
    htmlReport {
        onCheck.set(true)
    }
    
    verify {
        onCheck.set(true)
    }
}
```

#### property `disabledProjects` was removed

_Error message:_

```Using 'disabledProjects: Set<String>' is an error.```

_Solution_

- read about [merged reports changes](#merged-report-changes)
- use exclusion list in project filters of merged configuration extension

```
koverMerged {
    enable()
    filters {
        projects {
            excludes.add(":path or unique project name")
        }
    }
}
```

If `includes` are empty, all subprojects and current project are used in merged reports.

#### Property `instrumentAndroidPackage` was removed

There is no replacement. At the moment, all classes from the packages "android." and "com.android.*" excluded from
instrumentation.

#### property `runAllTestsForProjectTask` was removed

In the new API for single-project reports, it is impossible to call test tasks of another project. To account for coverage from tests of another module, use merged reports.

### Kover extension for test task

#### type of `isDisabled` property changed from `Boolean` to `Property<Boolean>`.
_Error message_

```
Val cannot be reassigned
```

_Solution for Kotlin script:_ change `isDisabled = true` to `isDisabled.set(true)`

#### `binaryReportFile` was renamed to `reportFile`

Solution: change `binaryReportFile` to `reportFile`

#### Type of `includes` property changed from `List<String>` to `ListProperty<String>`

Solution:

```includes.addAll("com.example.*", "foo.bar.*")```

#### type of `excludes` property changed from `List<String>` to `ListProperty<String>`

Solution for Kotlin: change `excludes = listOf("com.example.*", "foo.bar.*")`
to `excludes.addAll("com.example.*", "foo.bar.*")`

Solution for Groovy: change `excludes = ["com.example.*", "foo.bar.*"]`
to `excludes.addAll("com.example.*", "foo.bar.*")`

### `koverXmlReport` and `koverMergedXmlReport` tasks configuration

#### Property `xmlReportFile` was removed
Solution: use property in Kover extension at the root of the project

```
kover {
    xmlReport {
        reportFile.set(yourFile)
    }
}
```
&ast; for `xmlReportFile` task use `koverMerged { ... }` extension of the project.

#### Property `includes` was removed

Solution for Kotlin: use filter in Kover extension at the root of the project

```
kover {
    filters {
        classes {
            includes += listOf("foo.bar.*", "foo.biz.*")
        }
    }
}
```

Solution for Groovy: use filter in Kover extension at the root of the project

```
kover {
    filters {
        classes {
            includes.addAll("foo.bar.*", "foo.biz.*")
        }
    }
}
```
&ast; for `xmlReportFile` task use `koverMerged { ... }` extension of the project.

#### Property `excludes` was removed

Solution for Kotlin: use filter in Kover extension at the root of the project

```
kover {
    filters {
        classes {
            excludes += listOf("foo.bar.*", "foo.biz.*")
        }
    }
}
```

Solution for Groovy: use filter in Kover extension at the root of the project

```
kover {
    filters {
        classes {
            excludes.addAll("foo.bar.*", "foo.biz.*")
        }
    }
}
```
&ast; for `xmlReportFile` task use `koverMerged { ... }` extension of the project.

### `KoverTaskExtension` configuration

#### Type of `excludes` and `includes` property changed from `List<String>` to `ListProperty<String>`
_Error message:_
```
Val cannot be reassigned
```

_Solution:_

```
includes.addAll("com.example.*", "foo.bar.*")
```
and
```
excludes.addAll("com.example.*", "foo.bar.*")
```


### `koverHtmlReport` and `koverMergedHtmlReport` tasks configuration

#### Class `KoverHtmlReportTask` was removed

_Error message:_
```
Using 'KoverHtmlReportTask' is an error
```

_Solution:_

Configure report by Kover project extension

```
kover {
    htmlReport {
        // HTML report settings
    }
}
```

#### Property `htmlReportDir` was removed

_Error message:_
```
Using 'htmlReportDir: DirectoryProperty' is an error
```

Solution: use property `reportDir` in Kover extension at the root of the project

```
kover {
    htmlReport {
        reportDir.set(yourDir)
    }
}
```
&ast; for `koverMergedHtmlReport` task use `koverMerged { ... }` extension of the project.
 
#### Property `includes` was removed

_Error message:_
```
Using 'includes: List<String>' is an error
```

_Solution for Kotlin:_ use filter in Kover extension at the root of the project

```
kover {
    filters {
        classes {
            includes += listOf("foo.bar.*", "foo.biz.*")
        }
    }
}
```

_Solution for Groovy:_ use filter in Kover extension at the root of the project

```
kover {
    filters {
        classes {
            includes.addAll("foo.bar.*", "foo.biz.*")
        }
    }
}
```
&ast; for `koverMergedHtmlReport` task use `koverMerged { ... }` extension of the project.

#### Property `excludes` was removed

Error message:
```
Using 'excludes: List<String>' is an error
```

_Solution for Kotlin:_ use filter in Kover extension at the root of the project

```
kover {
    filters {
        classes {
            excludes += listOf("foo.bar.*", "foo.biz.*")
        }
    }
}
```

Solution for Groovy: use filter in Kover extension at the root of the project

```
kover {
    filters {
        classes {
            excludes.addAll("foo.bar.*", "foo.biz.*")
        }
    }
}
```
&ast; for `koverMergedHtmlReport` task use `koverMerged { ... }` extension of the project.

### `koverVerify` and `koverMergedVerify` tasks configuration

#### Function `rule` was removed for single-project verification

Error message:
```
Using 'rule(Action<VerificationRule>): Unit' is an error
```

_Solution:_

use function `rule` in Kover project extension

```
kover {
    verify {
        rule {
            // your verification rule
        }
    }
}
```

* For `koverMergedVerify` task use `koverMerged { ... }` extension of the project.

#### Property `includes` was removed

_Error message:_
```
Using 'includes: List<String>' is an error
```

Solution for Kotlin: use filter in Kover extension at the root of the project

```
kover {
    filters {
        classes {
            includes += listOf("foo.bar.*", "foo.biz.*")
        }
    }
}
```

Solution for Groovy: use filter in Kover extension at the root of the project

```
kover {
    filters {
        classes {
            includes.addAll("foo.bar.*", "foo.biz.*")
        }
    }
}
```
&ast; for `koverMergedVerify` task use `koverMerged { ... }` extension of the project.

#### Property `excludes` was removed

_Error message:_
```
Using 'excludes: List<String>' is an error
```

_Solution for Kotlin:_ use filter in Kover extension at the root of the project

```
kover {
    filters {
        classes {
            excludes += listOf("foo.bar.*", "foo.biz.*")
        }
    }
}
```

_Solution for Groovy:_ use filter in Kover extension at the root of the project

```
kover {
    filters {
        classes {
            excludes.addAll("foo.bar.*", "foo.biz.*")
        }
    }
}
```
* For `koverMergedVerify` task use `koverMerged { ... }` extension of the project.

