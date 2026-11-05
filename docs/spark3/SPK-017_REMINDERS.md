# SPK-017 — Gentle local reminders

Date: 2026-11-05
Branch: spark3/task-017-reminders
Parent: e93faf955332559c91916d1ac115ae14f519840a
Status: SOURCE_COMMITTED / CI_PENDING

## Implemented
- Android WorkManager periodic daily reminder, best effort (not exact alarm).
- Default disabled; local DataStore preference for opt-in and preferred hour.
- User can select morning 09:00, afternoon 14:00, evening 19:00; time follows local device clock at scheduling.
- Android 13+ POST_NOTIFICATIONS permission requested on opt-in; denial leaves reminders off.
- Notification includes generic supportive text, never private journal content.
- Unique work canceled on disable; no network service or remote push notifications.

## Evidence
- SPK-016 latest source commit 4ade08ba2eede494eae8edce0fa21866f9d05b8f passed GitHub Actions run 42109101221.
- SPK-017 source commits e3293877931fd3a6044a334e72027fa0551ae671, b7131c0fa85b2821f5a471506f7058537f687f46, b36036c130413975ebc1dfb8a03d77f3a78aecfb, 1003840766f301b5615a74e3da7479a20e4160c1, 87e11dfb6c434a218bbf0d0014b0c83eee3cb877, cbd5cd328c4a034827f9258a5a21b00e7e2fc05d.
- SPK-017 CI/device verification pending.

## Limitations
- WorkManager delivery is approximate, not guaranteed at selected clock hour.
- Time zone and daylight-saving changes do not automatically reschedule until user updates settings; periodic work may drift.
- No boot/device restart scheduling test or denial/revocation UX verification.
- Permission and worker behavior must be tested on Android 13+ and older devices.
- No tap-to-open PendingIntent; notification is informational.
- Reminder preferences and work state could become inconsistent after restore/reinstall; reconciliation needed.
- No guarantee of one notification per local calendar day.

## Next
Verify CI, fix compilation, then SPK-018 accessibility and interaction audit.
