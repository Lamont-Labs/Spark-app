# SPK-015 — Privacy controls

Date: 2026-11-05
Branch: spark3/task-015-privacy-controls
Parent: d91b087b146778668781e141d436ca8389497ecc
Status: SOURCE_COMMITTED / CI_PENDING

## Implemented
- Local DataStore boolean for resurfacing opt-in, default false.
- Today screen only composes resurfacing card when enabled.
- You destination exposes Privacy & preferences with explicit toggle.
- Plain-language explanation of private app storage and uninstall data-loss risk.
- No account, telemetry, remote sync, or new permissions.

## Evidence
- Source commits d25e4c354a382e7114c6ac38091798c03c48f317, 602519298f6a2317d220a44c9728eb0b1fa95a4b, 4218a7ce183fca1b4d1c5196fa8bd46db88a955d, 07529268a43063d3622de81dc3f9443a7a8315de.
- SPK-014 CI remained in progress at task start.
- SPK-015 compilation, persistence and device testing pending.

## Limitations
- No biometric gate, export, import, selective deletion, or data retention controls.
- Preference loading uses false initial state; the switch may briefly render off while reading enabled preference.
- DataStore corruption handling not yet tested.
- App-private storage and allowBackup=false are not equivalent to user-managed end-to-end encryption.
- No release/privacy policy audit yet.

## Next
Verify CI, fix compilation issues. SPK-016 manual export/import with evidence and rollback.
