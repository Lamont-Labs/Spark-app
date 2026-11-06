# SPK-027 — Execute Android instrumentation tests in GitHub Actions

Date: 2026-11-05
Branch: spark3/task-027-emulator-tests
Parent: d02c0418546922d473a83f65636c90011ebe612b
Status: WORKFLOW_COMMITTED / EMULATOR_EXECUTION_PENDING

## Implemented
- New independent emulator-tests job on ubuntu-latest, Java 17, Gradle 8.13.
- Enable /dev/kvm using GitHub-hosted runner udev rule.
- reactivecircus/android-emulator-runner@v2 starts Google APIs Android API 29 x86_64 emulator.
- Execute :app:connectedDebugAndroidTest (not merely assembleDebugAndroidTest).
- Upload connected test reports and results even if emulator tests fail.
- Preserve existing build, unit tests, APK checks and debug APK artifact job.

## Verification boundary
- Android emulator test run is not yet confirmed successful.
- Prior SPK-026 workflow runs were still in progress at the start of SPK-027.
- Device-specific test outcomes, actual APK installation, and full release readiness remain unverified.
- Emulator testing covers currently authored instrumentation tests only; process death, concurrent imports, disk-full and image decoding are not yet covered.
