# SPK-022 — Complete photo backup and restore

Date: 2026-11-05
Branch: spark3/task-022-photo-backup
Parent: 748d4f64dd74da487b6522df9d81715f5accf934
Status: SOURCE_COMMITTED / CI_PENDING / DEVICE_UNVERIFIED

## Implemented
- ZIP export with manifest.json containing reflection metadata and photos/<UUID>.jpg containing private photo bytes.
- ZIP restore validates manifest format/version, reflection IDs/dates, entry paths, duplicates, count, and photo-manifest consistency before database writes.
- Limits: 5 MB manifest, 15 MB per photo, 200 MB total uncompressed archive, 10,000 reflections and 10,001 entries.
- ZIP extraction goes to private cache staging, then copies only required photos to app-private storage.
- Existing reflection IDs are skipped; existing photo files cannot be overwritten.
- Room bulk insert is transactional (SPK-021); cleanup removes newly copied photos on failure.
- Privacy settings include complete ZIP export/restore and retain legacy JSON text-only backup.

## Important limitations and risks
- ZIP is UNENCRYPTED. It contains highly personal journal text and photos.
- Output stream failure can leave a partial ZIP at the chosen document location.
- File writes and Room transaction are not one atomic cross-filesystem transaction. Concurrent imports, process death and disk-full recovery are not verified.
- Photo MIME/format verification is not implemented; original image bytes are stored with a .jpg suffix.
- Import reads potentially large files up to 200 MB uncompressed into private cache; free-space checks are not implemented.
- No cryptographic integrity signature or encryption.
- Export snapshot and photo file reads are not transactionally frozen; concurrent deletion could fail export.
- Android hardware/emulator import/export roundtrip, photo rendering, and failure-injection tests are pending.
- Existing photo-only entries can now be included in complete ZIP backups, but legacy JSON export still refuses them.

## Verification
- SPK-021 latest source commit a4e74122cbce990df1f4e32607293bbe73847a59 passed GitHub Actions run 42113851350.
- SPK-022 CI and device testing pending. Do not call this production-ready.
