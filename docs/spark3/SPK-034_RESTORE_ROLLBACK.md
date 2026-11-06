# SPK-034 — Restore photo-install verification

Date: 2026-11-05
Branch: spark3/task-034-restore-rollback
Parent: e48857f556418218a498802adfc9f357f82d0fe7
Status: SOURCE_COMMITTED / CI_PENDING

## Verified baseline
SPK-032 GitHub Actions run 42148994259 passed both build and emulator jobs.

## Change
After each private photo copy, verify installed file length and, for v2 archives, installed SHA-256 checksum before inserting reflections into Room. Any verification failure triggers the existing catch cleanup for newly installed photo IDs. This guards against a corrupted/incomplete copied photo being referenced by newly imported database records.

## Remaining limitation
File installation and Room transaction are not an atomic cross-resource transaction. Process death or power loss between photo installation and Room commit can still leave orphan files; no persistent recovery journal has been implemented. Concurrent restores also remain unverified. This task does not claim crash-safe rollback.
