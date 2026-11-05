# SPK-019 — Adversarial reliability gates

Date: 2026-11-05
Branch: spark3/task-019-adversarial
Parent: 0ade6f987967447438dc55077f2267675ac9d138
Status: SOURCE_COMMITTED / TEST_CI_PENDING

## Implemented
- Seven JUnit regression tests for progress calculator: empty journal, duplicate same-day entries, one-day grace, repeated gaps, stale run, future dates, malformed calendar date.
- GitHub Actions now runs :app:testDebugUnitTest before :app:assembleDebug, so test failures block the APK artifact.
- No database schema change, network access, or destructive migrations.

## Evidence
- SPK-018 latest source commit 651b98744cd5a815d6ffa096e864a21296b69d12 passed build run 42111619521.
- SPK-019 source commits 59cdad6fd0c76f77bbf61868cba9ec5d03a74945, dd0b233a3c9f23216f6e858b0994c5ea7e9d2f11, cf368124675f370d3ba5abf780f2241214d23a5d.
- Tests have NOT yet been observed passing.

## Unresolved release blockers
- JSON import hostile test matrix: malformed root, duplicate UUID, 5 MB limit, unknown schema, invalid dates, photo-only entries, partially valid batches, and conflict handling.
- Atomic import and rollback under simulated storage failure not verified.
- Photo attachment format validation, orphan cleanup, full photo backup/restore.
- UI instrumented tests, TalkBack and device tests.
- Reminder permission/reboot/timezone tests.
- No production release signing or Play Store compliance verification.

## Next
Inspect CI and repair failing tests or compilation; SPK-020 release readiness only after gates pass.
