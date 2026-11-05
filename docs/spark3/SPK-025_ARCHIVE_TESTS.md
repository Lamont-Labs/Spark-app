# SPK-025 — Automated archive integrity regression tests

Date: 2026-11-05
Branch: spark3/task-025-archive-tests
Parent: 5603b43a05a7dff7d48fc57c5fa795b8f33d677f
Status: SOURCE_COMMITTED / CI_PENDING

## Implemented
- Extracted pure JVM ArchiveIntegrity validator used by PhotoArchive import and export.
- Added 12 JUnit tests for v1/v2 format acceptance and rejection, missing/extra photo references, missing/malformed checksum mappings, known SHA-256 vector and byte mutation detection.
- Existing seven progress tests remain in CI.
- GitHub Actions runs :app:testDebugUnitTest before APK assembly.

## Evidence
- SPK-024 CI was still running when SPK-025 began.
- SPK-025 unit test run is pending; no passing claim yet.

## Not yet proven
- Full Android ZIP export/import roundtrip with Room and private files.
- Process-death recovery, storage exhaustion, interrupted import, simultaneous import.
- Archive corruption through ZIP CRC failure and actual photo rendering.
- Legacy v1 restore on physical device.
- Encryption or authenticity protection.
- Release/device readiness.
