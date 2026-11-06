# SPK-037 — Navigation motion

Date: 2026-11-05
Branch: spark3/task-037-navigation-motion
Status: SOURCE_COMMITTED / CI_PENDING

Added a restrained 220 ms fade-in and 110 ms fade-out when switching between Today, Journal, Affirm and You using Compose AnimatedContent. Existing destination selection, editor, privacy controls, repository and local persistence remain unchanged. No new permissions or dependencies.

This is the first motion pass. Device review for reduced-motion preferences, focus retention, screen-reader navigation and visual quality remains necessary. Build and emulator verification are pending.
