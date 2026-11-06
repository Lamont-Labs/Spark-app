# SPK-031 — Interrupted restore and missing-data regression tests

Date: 2026-11-05
Branch: spark3/task-031-restore-recovery
Parent: 16dd112dfe332a9deabbb34dbbc0f2d0eee1249a
Status: SOURCE_COMMITTED / NEW CI_PENDING

## Verified baseline
SPK-029 run 42145667853 completed SUCCESS for both build and emulator-tests jobs.

## Added Android integration tests
- Truncated ZIP input should not change existing journal entries.
- Invalid calendar date in a manifest should reject import before database writes.
- Referenced but absent photo should reject import without database writes or private photo creation.

The integration suite now contains 12 authored emulator tests; 19 JVM unit tests remain.

## Known limitations
- Truncated ZIP behavior is not yet verified; ZIP streams may permit missing central directory. A passing test on a preserved existing row does not prove that an incomplete ZIP cannot import a previously absent row.
- Simulated disk-full and process death, photo-file/Room cross-resource atomicity, concurrent imports, physical device and production signing remain unverified.
