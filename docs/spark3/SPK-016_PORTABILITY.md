# SPK-016 — Manual text reflection portability

Date: 2026-11-05
Branch: spark3/task-016-portability
Parent: 856ef12ba2afa7df9956f3736dc67b5cdc92c011
Status: SOURCE_COMMITTED / CI_PENDING

## Implementation
- User-initiated JSON export via Android CreateDocument; import via OpenDocument.
- Versioned format spark-reflections v1, preserving UUIDs, kind, text, date, timestamps, mood, favorite and prompt ID.
- Maximum 5 MB import / 10,000 reflections, duplicate-ID and schema validation before any database insertion.
- Conflict-safe Room bulk insertion with IGNORE: existing IDs never overwritten.
- Unencrypted backups; UI explicitly warns about sensitive journal contents.
- Photos are not embedded. Export fails when photo-only entries exist rather than silently losing their entries; text-plus-photo entries export text only and restore without image references.
- No network, account or cloud sync.

## Evidence
- SPK-015 CI run 42108029872 passed for latest source commit.
- Source commits 8b5462d51a16f5046581307ef125b74a12e5588e, ea872229e65278973853ccc2c971e2586e094432, f27112a7bda71fc2de6b7ff7268ec96d3dbedcd8, 4ade08ba2eede494eae8edce0fa21866f9d05b8f.
- SPK-016 CI/device verification pending.

## Risks and release blockers
- This is text-only portability, not a complete archive. Photo bytes are not backed up.
- JSON export uses a system document destination and is not encrypted.
- Import is append-only and cannot undo imported entries in one action.
- No import preview, signature, checksum, adversarial parser tests, or transactional rollback verified on device.
- Large export has no output cap; large backups may fail memory limits.
- Device and process-death testing required.

## Next
Verify CI and address compiler errors. SPK-017 reminders follows, but complete photo backup must be resolved before release claims.
