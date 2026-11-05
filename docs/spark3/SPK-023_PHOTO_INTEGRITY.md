# SPK-023 — Photo archive integrity and recovery hardening

Date: 2026-11-05
Branch: spark3/task-023-photo-integrity
Parent: 285ddcb5fcdefc9cee3f9ec0011ef8200a3356ee
Status: SOURCE_COMMITTED / CI_PENDING / DEVICE_UNVERIFIED

## Verified predecessor
- SPK-022 latest source commit 33e50d02a2ca6df0b397ad69a36fddc4b3d7edbf passed GitHub Actions run 42125974527.

## Changes
- Export computes SHA-256 of each private photo and stores photo_sha256 object in manifest.json.
- Import requires exact correspondence between referenced photos, ZIP photo entries and checksum keys.
- Every extracted photo's SHA-256 must match before any new photo is copied into private storage or any reflection is inserted.
- Reject empty ZIP entries and invalid photo lengths.
- Retain existing ZIP path restrictions, size caps, private staging, transaction-backed bulk insertion and best-effort cleanup.

## Compatibility
- The SPK-023 reader requires photo_sha256. ZIP files created by SPK-022 before this change are not importable by the new reader. No backward compatibility or archive migration has been implemented.
- The archive format still reports version=1 despite this stricter requirement. A future versioned migration must resolve that mismatch before stable release.

## Limitations
- SHA-256 detects accidental changes relative to the manifest, not malicious tampering: an attacker can replace both bytes and hash in an unencrypted, unsigned ZIP.
- No encryption, authenticity signature or format sniffing of image bytes.
- Cross-filesystem transaction atomicity, interrupted process cleanup, concurrent import race, low-disk and forced failure tests are pending.
- Device backup/restore roundtrip and accessibility testing pending.
- The source is not production-ready; CI pending.
