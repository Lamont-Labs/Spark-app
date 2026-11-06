# SPK-026 — Android Room + photo archive integration tests

Date: 2026-11-05
Branch: spark3/task-026-archive-integration
Parent: ba0201b362ef668c98caf0f0d835a4d50c6e82f8
Status: SOURCE_COMMITTED / CI_PENDING / INSTRUMENTATION_EXECUTION_PENDING

## Implemented
- Added AndroidJUnitRunner and AndroidX instrumentation test dependencies.
- Added Android instrumentation tests using an in-memory Room SparkDatabase, real app-private files and ZIP export/import.
- Cases: text reflection roundtrip, photo-only reflection byte-for-byte roundtrip, preservation of an existing reflection, invalid version rejected before writes, changed photo checksum rejected before writes or photo installation.
- CI now compiles Android instrumentation tests via :app:assembleDebugAndroidTest before assembling and publishing debug APK.
- Previous SPK-025 JUnit archive integrity and progress tests remain in CI.

## Verification limits
- Compiling androidTest is not running androidTest. No emulator or physical Android device execution has been performed in this task.
- SPK-025 source tests passed in GitHub Actions run 42129475756.
- SPK-026 compilation run pending; do not claim integration tests passed.
- Process death, disk-full, concurrent imports, ZIP CRC corruption and real image decode remain untested.
- Archive remains unencrypted and unsigned.
- No production release signing.
