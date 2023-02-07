# Kover migration guide from 0.6.x to 0.7.0

## Main differences
- Coverage Engines were renamed to Coverage Tools
- IntelliJ Coverage Engine was renamed to Kover Coverage Tool


## 
#### rename dependency in buildSrc
Dependencies like `classpath "org.jetbrains.kotlinx:kover:$koverVersion"` or `implementation("org.jetbrains.kotlinx:kover:$koverVersion")` should be replaced by `classpath "org.jetbrains.kotlinx:kover-gradle-plugin:$koverVersion"` etc


## Migration Issues
### Could not find org.jetbrains.kotlinx:kover:0.7.0
_Solution_

rename dependencies in _buildSrc_ from `org.jetbrains.kotlinx:kover:` to `org.jetbrains.kotlinx:kover-gradle-plugin:`

### Unresolved reference: KoverExtension
_Solution_

Replace `KoverExtension` (or `kotlinx.kover.api.KoverExtension`) by `kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension`



remove imports `import kotlinx.kover.tasks.*`


// TODO kotlinx.kover.tasks.*











### Engines renaming
#### Property `engine` was renamed to `tool`
_Error message:_

```
Using 'engine: CoverageEngineVariant' is an error
```

_Solution_

Rename property `engine` to `tool`

#### Class `CoverageEngineVariant` was renamed to `CoverageToolVariant`
_Error message:_

```
Using 'CoverageEngineVariant' is an error
```

_Solution_

Use class `CoverageToolVariant` instead of `CoverageEngineVariant`

#### Class `IntellijEngine` was renamed to `KoverTool`
_Error message:_

```
Using 'IntellijEngine' is an error
```

_Solution_

Use class `KoverTool` instead of `IntellijEngine`

#### Object `DefaultIntellijEngine` was renamed to `KoverToolDefault`
_Error message:_

```
Using 'DefaultIntellijEngine' is an error
```

_Solution_

Use class `KoverToolDefault` instead of `DefaultIntellijEngine`


#### Class `JacocoEngine` was renamed to `JacocoTool`
_Error message:_

```
Using 'JacocoEngine' is an error
```

_Solution_

Use class `JacocoTool` instead of `JacocoEngine`

#### Object `DefaultJacocoEngine` was renamed to `JacocoToolDefault`
_Error message:_

```
Using 'DefaultJacocoEngine' is an error
```

_Solution_

Use class `JacocoToolDefault` instead of `DefaultJacocoEngine`

#### Constant `KoverVersions.MINIMAL_INTELLIJ_VERSION` was renamed to `KoverVersions.KOVER_TOOL_MINIMAL_VERSION`
_Error message:_

```
Using 'MINIMAL_INTELLIJ_VERSION: String' is an error.
```

_Solution_

Use constant `KOVER_TOOL_MINIMAL_VERSION` instead of `MINIMAL_INTELLIJ_VERSION`

#### Constant `KoverVersions.DEFAULT_INTELLIJ_VERSION` was renamed to `KoverVersions.KOVER_TOOL_DEFAULT_VERSION`
_Error message:_

```
Using 'DEFAULT_INTELLIJ_VERSION: String' is an error.
```

_Solution_

Use constant `KOVER_TOOL_DEFAULT_VERSION` instead of `DEFAULT_INTELLIJ_VERSION`

#### Constant `KoverVersions.DEFAULT_JACOCO_VERSION` was renamed to `KoverVersions.JACOCO_TOOL_DEFAULT_VERSION`
_Error message:_

```
Using 'DEFAULT_JACOCO_VERSION: String' is an error.
```

_Solution_

Use constant `JACOCO_TOOL_DEFAULT_VERSION` instead of `DEFAULT_JACOCO_VERSION`


