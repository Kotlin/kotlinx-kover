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

for Kotlin script: change `isDisabled = true` to `isDisabled.set(true)`

### Properties `coverageEngine`, `intellijEngineVersion` and `jacocoEngineVersion` were removed.

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

Use inclusion list in project filters

```
koverMerged {
    enable()
    filters {
        projects {
            includes.add(":path or unique project name")
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

### type of `isDisabled` property changed from "Boolean" to `Property<Boolean>`".

Solution for Kotlin script: change `isDisabled = true` to `isDisabled.set(true)`

### `binaryReportFile` was renamed to `reportFile`

Solution: change `binaryReportFile` to `reportFile`

### Type of `includes` property changed from `List<String>` to `ListProperty<String>`

Solution for Kotlin: change `includes = listOf("com.example.*", "foo.bar.*")`
to `includes.addAll("com.example.*", "foo.bar.*")`

Solution for Groovy: change `includes = ["com.example.*", "foo.bar.*"]`
to `includes.addAll("com.example.*", "foo.bar.*")`

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

## `koverHtmlReport` and `koverMergedHtmlReport` tasks configuration

### Property `htmlReportDir` was removed

Solution: use property in Kover extension at the root of the project

```
kover {
    htmlReport {
        reportDir.set(yourDir)
    }
}
```
&ast; for `koverMergedHtmlReport` task use `koverMerged { ... }` extension of the project.
 
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
&ast; for `koverMergedHtmlReport` task use `koverMerged { ... }` extension of the project.

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
&ast; for `koverMergedHtmlReport` task use `koverMerged { ... }` extension of the project.

## `koverVerify` and `koverMergedVerify` tasks configuration

### Function `rule` was removed

Solution: use function in Kover extension at the root of the project

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
* For `koverMergedVerify` task use `koverMerged { ... }` extension of the project.

