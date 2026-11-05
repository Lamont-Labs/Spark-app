# SPK-020 — Android release-readiness gate

Date: 2026-11-05
Branch: spark3/task-020-release-readiness
Parent: b36b8f9b14675d0d1a62d8956d4abb0c993e88a2
Status: SOURCE_COMMITTED / CI_PENDING / NOT_PRODUCTION_RELEASED

## Verified predecessor
- SPK-018 Android compilation passed in run 42111619521.
- SPK-019 unit-test step passed in run 42112480357 while APK build was still in progress when checked.

## Added release evidence gate
- Run JUnit unit tests before assembling debug APK.
- Require nonempty APK, expected com.lamontlabs.spark package, valid APK signature.
- Fail CI if merged APK declares INTERNET permission.
- Verify compiled AndroidManifest.xml declares allowBackup=false.
- Publish APK plus SHA256SUMS.txt in GitHub Actions artifact with 30-day retention.

## Release classification
- DEBUG_DEVELOPMENT_BUILD_ONLY: debug signing; not a Play Store release, not release-signed.
- CI verification is necessary but does not prove installation, runtime behavior, accessibility or data durability.
- GitHub Actions artifact is a ZIP download containing app-debug.apk and SHA256SUMS.txt. The ZIP itself is not installable.

## Open blocking issues
1. Full photo bytes backup/restore is not implemented; text-only export fails for photo-only entries.
2. Backup importer needs adversarial tests and atomic rollback verification.
3. Notification delivery, reboot/timezone handling and permission denial need device verification.
4. UI/TalkBack, large font, keyboard and screen-size checks are pending.
5. Runtime smoke test on Android hardware/emulator pending.
6. No signed release artifact, Play policy/privacy disclosure audit or reproducible release procedure.
7. No end-to-end migration, upgrade or uninstall/reinstall recovery test.

## Acceptance criteria
- Latest commit has successful tests, APK assembly, APK verification and artifact publication.
- Download artifact and inspect checksum; install APK on supported Android device; exercise Today, Journal, Affirm, You, reflection creation, photos, import/export, reminders, accessibility.
- Do not mark production-ready until blocking gaps have been closed and re-tested.

## Next work
Address failing CI if present; finish backup and runtime release blockers before public release.
