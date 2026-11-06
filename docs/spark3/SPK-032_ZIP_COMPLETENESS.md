# SPK-032 — ZIP completeness verification

Date: 2026-11-05
Branch: spark3/task-032-zip-completeness
Status: SOURCE_COMMITTED / CI_PENDING

## Baseline
SPK-031 GitHub Actions run 42147244999 passed both Android build and emulator jobs.

## Change
- Incoming archive is copied into a bounded private staging file.
- java.util.zip.ZipFile must successfully parse the central directory before reflection or photo installation.
- All entries are read with per-entry bounds before the existing stream importer validates manifest, checksums and records.
- The staged ZIP is deleted in the existing finally cleanup.
- Added Android regression test for ZIP missing its end-of-central-directory record, checking that a new reflection is not imported.

## Limitations
- This does not make photo installation and Room commit a single atomic transaction.
- Archive remains plaintext and unsigned.
- New emulator verification pending.
