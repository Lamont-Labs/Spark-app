# SPK-024 — Photo archive format versioning and backward compatibility

Date: 2026-11-05
Branch: spark3/task-024-archive-versioning
Parent: 4dc9cee01bff35383a0f4a4c96daa832ad692abf
Status: SOURCE_COMMITTED / CI_PENDING

## Version contract
- v1 (SPK-022): manifest.json format=spark-photo-archive, version=1, reflections; no photo_sha256. Reader accepts for backward compatibility, without cryptographic corruption detection.
- v2 (SPK-024 onward): same manifest with version=2 and photo_sha256 object mapping each referenced photo UUID to lowercase SHA-256 hex digest.
- Export always emits v2.
- Import accepts v1 or v2; v2 enforces exact photo-reference/checksum correspondence and hash verification before writes.
- Mixed v1 with photo_sha256 is rejected. Unsupported versions are rejected.

## Verification
- SPK-023 latest source commit 0ddc5af60b572bf0ff8af8be13c381f84c7b2c1a passed GitHub Actions run 42127340763.
- SPK-024 CI and actual device roundtrip pending.
- Automated archive roundtrip tests and failure-injection tests remain pending.

## Known risks
- Legacy v1 has no embedded checksums; imported v1 photo bytes are not integrity-sealed.
- SHA-256 inside an unsigned ZIP is not an authenticity signature.
- No encryption, device tests, import interruption recovery or low-disk verification.
- Release readiness remains blocked.
