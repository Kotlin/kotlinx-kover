# Main differences

The new API allows you to configure Kover in a more flexible manner, while being more concise than the previous API.
From now on, there is no need to configure each Kover task separately.

In the new API, in order to respect upcoming Gradle conventions, the plugin should be explicitly applied to
each project that needs coverage.
To create merged tasks (that collect test coverage from different projects), enable it by `koverMerged.enable()` or 
```
koverMerged {
    enable()
}
```
in one project, which will be a merged report container.

To configure reports that collect coverage only for tests from one project, the `kover { }` project extension is used.
To configure merged reports, the `koverMerged { }` project extension is used.

# Migration Issues

## Root kover extension

### type of `isDisabled` property changed from `Boolean` to `Property<Boolean>`.

_Error message_

```
Val cannot be reassigned
```

_Solution for Kotlin script:_ change `isDisabled = true` to `isDisabled.set(true)`

### Properties `coverageEngine`, `intellijEngineVersion` and `jacocoEngineVersion` were removed.

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

### Property "generateReportOnCheck" was removed

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

### property `disabledProjects` was removed

_Error message:_

```Using 'disabledProjects: Set<String>' is an error.```

_Solution_

- read about [merged reports changes](#foo)
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

### Property `instrumentAndroidPackage` was removed

There is no replacement. At the moment, all classes from the packages "android." and "com.android.*" excluded from
instrumentation.

### property `runAllTestsForProjectTask` was removed

TBD

## Kover extension for test task

### type of `isDisabled` property changed from `Boolean` to `Property<Boolean>`.
_Error message_

```
Val cannot be reassigned
```

_Solution for Kotlin script:_ change `isDisabled = true` to `isDisabled.set(true)`

### `binaryReportFile` was renamed to `reportFile`

Solution: change `binaryReportFile` to `reportFile`

### Type of `includes` property changed from `List<String>` to `ListProperty<String>`

Solution:

```includes.addAll("com.example.*", "foo.bar.*")```

### type of `excludes` property changed from `List<String>` to `ListProperty<String>`

Solution for Kotlin: change `excludes = listOf("com.example.*", "foo.bar.*")`
to `includes.addAll("com.example.*", "foo.bar.*")`

Solution for Groovy: change `excludes = ["com.example.*", "foo.bar.*"]`
to `includes.addAll("com.example.*", "foo.bar.*")`

## `koverXmlReport` and `koverMergedXmlReport` tasks configuration

### Property `xmlReportFile` was removed
Solution: use property in Kover extension at the root of the project

```
kover {
    xmlReport {
        reportFile.set(yourFile)
    }
}
```
&ast; for `xmlReportFile` task use `koverMerged { ... }` extension of the project.

### Property `includes` was removed

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

### Property `excludes` was removed

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

## `KoverTaskExtension` configuration

### Type of `excludes` and `includes` property changed from `List<String>` to `ListProperty<String>`
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


## `koverHtmlReport` and `koverMergedHtmlReport` tasks configuration

### Class `KoverHtmlReportTask` was removed

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

### Property `htmlReportDir` was removed

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
 
### Property `includes` was removed

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

### Property `excludes` was removed

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

## `koverVerify` and `koverMergedVerify` tasks configuration

### Function `rule` was removed for single-project verification

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

### Property `includes` was removed

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

### Property `excludes` was removed

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

