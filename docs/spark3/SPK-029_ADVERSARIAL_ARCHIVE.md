# SPK-029 — Adversarial Android archive regression coverage

Date: 2026-11-05
Branch: spark3/task-029-archive-adversarial
Parent: 99898d34082a05b410dece17e4d0d09bcd537b80
Status: SOURCE_COMMITTED / NEW CI_PENDING

## Verified baseline
SPK-027 GitHub Actions run 42144297577 completed SUCCESS for both build and emulator-tests jobs.

## Added Android emulator test cases
1. Version 1 legacy ZIP with no checksums restores a text reflection.
2. Version 1 ZIP with unexpected checksum metadata is rejected without writes.
3. Duplicate reflection IDs in a manifest reject the entire import without writes.
4. Unexpected ZIP traversal path ../escape.txt is rejected without writes.

These supplement the five existing Room-backed Android integration tests and nineteen local JVM tests. The test source now contains nine Android integration tests.

## Verification limits
The new SPK-029 tests must pass on the emulator before this task can be marked verified. The prior SPK-027 success does not establish SPK-029 success. Device installation, interrupted writes and disk exhaustion remain unverified.
