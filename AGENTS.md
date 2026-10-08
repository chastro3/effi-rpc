<!-- CODEGRAPH_START -->

## CodeGraph

This repository is indexed by CodeGraph (`.codegraph/` exists at the repo root).
Use CodeGraph before grep/find or reading files to understand or locate code:

- MCP tools, when available: `codegraph_explore` or `codegraph_node`.
- Shell fallback:
    - `codegraph explore "<symbol names or question>"`
    - `codegraph node <symbol-or-file>`

Keep the index current with `codegraph sync .` after substantial source changes.

<!-- CODEGRAPH_END -->

# effi-rpc Build Guide

## Environment

- Repository root: `C:\Users\zhouwenbo\Desktop\rpc\code\effi-rpc`
- JDK: `C:\dev\Java\jdk-25.0.3`
- `JAVA_HOME`: `C:\dev\Java\jdk-25.0.3`
- `GRADLE_USER_HOME`: `D:\tools\gradle`
- Gradle wrapper: `9.8.0`
- Wrapper distribution:
  `gradle-9.8.0-bin`

Always use the project wrapper (`gradlew.bat`). Do not replace it with a system
Gradle installation.

## Windows TEMP Workaround

This machine can fail to connect to the Gradle daemon when `TEMP` and `TMP`
point to `C:\Users\ZHOUWE~1\AppData\Local\Temp`. A typical failure is:

```text
java.io.IOException: Unable to establish loopback connection
```

Set both variables to a normal repository-local directory before running any
Gradle command:

```powershell
$tempDir = 'C:\Users\zhouwenbo\Desktop\rpc\code\effi-rpc\.gradle\tmp'
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

$env:TEMP = $tempDir
$env:TMP = $tempDir
```

Use the same `TEMP` and `TMP` values for IDEA's Gradle process. The wrapper
launcher and the forked Gradle JVM are separate processes and both need the
corrected location.

## Verified Build Commands

Run all commands from the repository root:

```powershell
$env:TEMP = 'C:\Users\zhouwenbo\Desktop\rpc\code\effi-rpc\.gradle\tmp'
$env:TMP = $env:TEMP
$env:GRADLE_USER_HOME = 'D:\tools\gradle'
$env:JAVA_HOME = 'C:\dev\Java\jdk-25.0.3'
```

Check the wrapper:

```powershell
.\gradlew.bat --version
```

Build and test all modules:

```powershell
.\gradlew.bat build --no-daemon
```

Compile one module:

```powershell
.\gradlew.bat :effi-rpc-protocols:effi-rpc-http:compileJava --no-daemon
```

`gradle.properties` enables `org.gradle.caching=true`. Configuration cache is
kept opt-in from the CLI because IntelliJ compile/reload currently hits Gradle
issue #29087 with the included `build-logic` build:
`.\gradlew.bat build --configuration-cache --no-daemon`.

## Test Constraints

The `effi-rpc-test` test task is enabled. Two manual-only cases remain disabled:

- `ApiTest.serverExport()` requires an external Consul registry.
- `ApiTest.annotationStyle()` is a manual annotation-style stress test.

Last verified: `2026-10-05`; `.\gradlew.bat build --no-daemon` completed
successfully with 63 actionable tasks, including the enabled test task. A
second run reused the configuration cache.
