# SPK-042 — Optional reminder setup in First Light

Date: 2026-11-05
Branch: spark3/task-042-onboarding-reminders
Status: SOURCE_COMMITTED / CI_PENDING

Onboarding stage four now includes an off-by-default daily reminder toggle. If opted in, users can choose morning (09:00), afternoon (14:00) or evening (19:00). The UI reuses the existing offline PrivacyPreferences DataStore and ReminderScheduler WorkManager path. Android 13+ notification permission is requested only on explicit enable; denial keeps the reminder disabled and shows a message. Turning off cancels scheduled work. Time selection reschedules enabled work. No journal text appears in notifications.

Known limitations: WorkManager delivery is approximate; timezone/DST adjustments, denied permission recovery, manual visual accessibility review and on-device testing remain future tasks. CI pending.
