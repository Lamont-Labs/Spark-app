# SPK-021 — Import atomicity and backup boundary hardening

Date: 2026-11-05
Branch: spark3/task-021-import-atomicity
Parent: cd4160e505eb602b1dc81b6328be80cafe7a9af1
Status: SOURCE_COMMITTED / CI_PENDING

## Implemented
- Room bulk insertIfAbsent now uses @Transaction so storage failures roll back the batch rather than leaving partially inserted entries.
- Existing-ID conflicts remain ignored, preserving local records.
- Export enforces the same 5 MB / 10,000-reflection bounds already enforced by import.
- Import requires explicit photo_bytes_included=false and validates photo metadata shape.
- Full parse/validation completes before the DAO insert call.

## Evidence
- SPK-020 unit tests passed while APK compilation and release checks were still pending at last inspection.
- Source commits 2c4a6fdcbc1887657ed0c4af31987e162b2975a8 and a4e74122cbce990df1f4e32607293bbe73847a59.
- SPK-021 CI not yet verified.

## Unresolved
- Instrumented rollback test with injected DB failure and concurrent imports is still required.
- Export remains plaintext, text-only, with no photo-byte portability.
- JSON parser malformed input fuzz tests and data integrity roundtrip tests remain pending.
- Device import/export, storage permissions and process-death testing pending.
- This checkpoint is not a production release.
