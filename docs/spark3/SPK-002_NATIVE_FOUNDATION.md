# SPK-002 — Native Android foundation checkpoint

Date: 2026-11-05
Branch: spark3/task-002-native-foundation
Parent checkpoint: 90352e9006daf1fc998b37eedb5a545fab160adf
Status: SOURCE_COMMITTED / BUILD_UNVERIFIED

## Implemented
- Isolated `android-native/` Gradle Android application, Kotlin + Compose.
- Android app identity `com.lamontlabs.spark`, minSdk 26, compileSdk/targetSdk 35.
- Static Today screen showing three independent cards: Gratitude, Daily Win, Affirmation.
- Android manifest with `allowBackup=false` and no requested INTERNET permission.
- GitHub Actions workflow for Gradle debug APK assembly and artifact upload.
- README documents commands, limits, and installed-app upgrade risk.

## Source evidence
Created:
- android-native/settings.gradle.kts
- android-native/build.gradle.kts
- android-native/gradle.properties
- android-native/app/build.gradle.kts
- android-native/app/src/main/AndroidManifest.xml
- android-native/app/src/main/java/com/lamontlabs/spark/MainActivity.kt
- android-native/README.md
- .github/workflows/spark3-android.yml

Legacy Expo source is preserved unchanged.

## Verification performed
- GitHub Contents API accepted commits for the eight files.
- GitHub Actions run list for this branch was empty when queried.
- No local Gradle installation or usable Android SDK was confirmed in the available execution environment.
- Therefore build compilation, APK generation, device installation, and app behavior remain **UNVERIFIED**.
- No real reflection saving, navigation, journal database, migration, or release signing has been implemented.

## Required next checks
1. Confirm the GitHub workflow is recognized and starts for the branch (new workflows may require default-branch registration).
2. Inspect CI Gradle errors, correct version/dependency issues, and repeat until assembleDebug passes.
3. Read the APK artifact SHA-256 and verify artifact presence.
4. Install on a test device; do not overwrite an existing Spark installation holding journal data.
5. Record exact build run URL and artifact before marking SPK-002 VERIFIED.

## Next development task
SPK-003: verify reproducible GitHub Actions build and APK delivery, then implement foundational navigation and persistence in subsequent tasks.


## SPK-003 verified build result (2026-11-05)

GitHub Actions run: https://github.com/Lamont-Labs/Spark-app/actions/runs/42098934261
Source commit built: bee2109ed76a8ace8cb6f258f9f11926d03bd09d
Result: SUCCESS
- Java/Gradle setup: passed
- Android SDK installation: passed
- :app:assembleDebug: passed
- SHA-256 computation step: passed (APK byte digest not transcribed from logs)
- actions/upload-artifact: passed
Artifact ID: 13448942000
Artifact name: spark3-debug-apk
Artifact archive size: 8,855,262 bytes
Artifact ZIP SHA-256 as reported by GitHub: 7a0fd226a7a9193633a2a9f90feaa0f767e0ca5cae85e9e375f59be55a14e427
Artifact URL: https://github.com/Lamont-Labs/Spark-app/actions/runs/42098934261/artifacts/13448942000

Important: the artifact digest is for the uploaded archive, not necessarily the APK inside. A device install, screenshot, accessibility audit, and upgrade compatibility remain unverified. This document update is later than the successful source commit; the successful build applies to the stated source commit only.

Next: SPK-004, implement a design-system foundation with explicit tokens and light/dark behavior in a new task branch. Retain all legacy data and do not claim feature completeness.
