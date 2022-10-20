# Kover migration guide from 0.6.x to 0.7.0

## Main differences
- coverage engines were renamed to coverage tools
- IntelliJ Coverage Engine was renamed to Kover Coverage Tool



## Migration Issues
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

#### Object `DefaultIntellijEngine` was renamed to `DefaultKoverTool`
_Error message:_

```
Using 'DefaultIntellijEngine' is an error
```

_Solution_

Use class `DefaultKoverTool` instead of `DefaultIntellijEngine`


#### Class `JacocoEngine` was renamed to `JacocoTool`
_Error message:_

```
Using 'JacocoEngine' is an error
```

_Solution_

Use class `JacocoTool` instead of `JacocoEngine`

#### Object `DefaultJacocoEngine` was renamed to `DefaultJacocoTool`
_Error message:_

```
Using 'DefaultJacocoEngine' is an error
```

_Solution_

Use class `DefaultJacocoTool` instead of `DefaultJacocoEngine`
