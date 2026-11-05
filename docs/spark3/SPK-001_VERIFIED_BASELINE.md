# Spark 3.0 — Verified baseline and development continuity

Status: BASELINE_RECORDED; no application implementation or build verified.
Recorded: 2026-11-05 UTC
Task: SPK-001
Repository: Lamont-Labs/Spark-app (private)
Baseline branch: main
Baseline commit: 5bd8c730fb730710cb418af37d29c7690023789c
Baseline Git tree: 5bd8c730fb730710cb418af37d29c7690023789c
Companion historical repository: Lamont-Labs/Spark (public)
Companion main HEAD (previous inspection; reconfirm before use): 379eb61eaaedd0c8edfdc908612a0804cc394cdd

## Source observations from GitHub

- package.json identifies Spark 2.3.0, Expo ^52, React Native 0.76, React 18.3.1.
- src/app.tsx imports ./screens/Today, but that file is absent from the recursive main tree.
- app/src/main/AndroidManifest.xml uses tools:node without declaring xmlns:tools, and specifies android:allowBackup="true".
- app.json references assets/icon.png and assets/splash.png; neither is in the recursive main tree.
- package.json invokes verify.sh; that file is absent from the recursive main tree.
- No tsconfig.json, package lockfile, or GitHub Actions workflow exists in the inspected main tree.
- privacy-policy.md claims an encrypted SQLite database and kid/teen/parent modes; these claims have not been substantiated by inspected implementation.
- The existing Android build is not proven. No APK, AAB, instrumented tests, or device installation has been verified in this task.

## Spark 3.0 governing decisions from project planning

- Audience: teens and adults; age-appropriate prompts, same visual system.
- Today: three independent cards, Gratitude, Daily Win, Affirmation; no mandatory wizard.
- Optional mood only as context on gratitude/win reflections; no standalone mood log.
- Limited enhancements: photo memories, prompts, memory resurfacing, gentle milestones.
- Four destinations: Today, Journal, Affirm, You.
- Visual identity: Warm Editorial × Atmospheric Luminous.
- Local-first; no required account, cloud sync, third-party analytics, or AI therapy.
- Security baseline A: app-private storage, Android device encryption, optional biometric/device-credential app lock, automatic cloud journal backup disabled, user-initiated export/import.
- Kotlin/Jetpack Compose + Room/DataStore is the recommended *unproven* migration direction, not a completed decision or implementation.
- Exact theme default, typography, progress grace semantics, and data migration implementation are not yet locked or tested.

## Safety and migration boundaries

1. Do not overwrite main or delete legacy files as part of baseline capture.
2. Preserve the existing application ID com.lamontlabs.spark until a migration decision is approved.
3. Treat the existing Expo and native Android fragments as legacy evidence, not a buildable Android application.
4. Do not represent a future Kotlin build as an in-place update of an installed Expo APK until signing key, package identity, and Android data migration compatibility are verified.
5. Do not repeat unsupported privacy/encryption claims in release materials.
6. Record each subsequent task's branch, commit, changed files, checks, and blockers in this directory.
7. GitHub commits prove source persistence; they do not prove runtime correctness.

## SPK-001 acceptance evidence

- Baseline HEAD and recursive tree inspected via connected GitHub.
- A development branch created from the exact verified baseline commit.
- This continuity record committed to that branch.
- No application files modified; no build or tests run.

## Next task — SPK-002

Evaluate native Kotlin/Compose build feasibility in a separate branch based on this checkpoint. Establish a minimal clean Android project with a reproducible CI build and downloadable debug APK before replacing any existing source. Check Android SDK/Gradle compatibility and application signing implications. Keep legacy source available for comparison.
