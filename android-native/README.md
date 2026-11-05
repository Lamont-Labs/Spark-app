# Spark 3 — isolated Android foundation

This directory is a new Kotlin/Jetpack Compose proof-of-build alongside the unchanged Expo legacy source. It is **not** a verified upgrade path for installed Spark 2.3 users.

Build with JDK 17, Android SDK 35, and Gradle 8.13:

```sh
cd android-native
gradle --no-daemon :app:assembleDebug
```

CI workflow: `.github/workflows/spark3-android.yml`. The APK artifact is only available after the workflow succeeds.

Current screen: static Today composition with Gratitude, Daily Win and Affirmation cards. No navigation, editing, persistence, app lock, import/export or migration is implemented.

Security baseline: no INTERNET permission requested; Android automatic backup disabled in manifest. Verify the final merged manifest and actual device behavior before claiming release compliance.

**Upgrade caution:** Existing Expo builds may use the same package ID with different signing keys. Installing this debug APK over an existing installation may fail. Never uninstall an existing app containing journal data without exporting it first.
