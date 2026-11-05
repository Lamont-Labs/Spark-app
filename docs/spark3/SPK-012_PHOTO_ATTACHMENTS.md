# SPK-012 — Private photo attachments

Date: 2026-11-05
Branch: spark3/task-012-photo-attachments
Parent: ebfc80f47a2730f709cbb508eefbb4bc4faedf17
Status: SOURCE_COMMITTED / BUILD_PENDING

## Implemented
- Android system GetContent photo picker; no broad photo permissions.
- PrivatePhotoStore copies selected URI bytes into app-private filesDir/reflection-photos with UUID file IDs, 15 MB maximum, sync and partial-file cleanup.
- Reflection editor supports optional photo-only or photo-plus-text entries; can replace or remove selected photo.
- ReflectionRepository stores the imported photo ID in the existing nullable Room column.
- Journal attempts to render attached image and shows missing-photo text if unavailable.
- No internet permission, external photo URI retention, or auto cloud backup.

## Evidence
- Previous SPK-011 source commit eebdae4031ea47e6651693cabc1538a85eace406 passed GitHub Actions run 42103938279.
- SPK-012 source commits d9301c7601eb263c0a66b9afbe7590c748d374e9, 501917aa13743a5cfcf37cefbdff5245b0537e85, 9a906bf41dfe96bf129c345f7e14c092445a9fb0.
- SPK-012 CI, device picker and image persistence unverified.

## Outstanding risks / hardening gates
- Imported image format and MIME magic are not verified; .jpg is a private storage suffix, not proof of JPEG encoding.
- Photo file may be orphaned when user backs out without saving or when DB insert fails; periodic reconciliation and transactional lifecycle are required.
- Image decoding is synchronous on Journal composition; move to bounded background decoding and size-aware thumbnails.
- Editor picker may be tapped during an in-flight import; race protection is not complete.
- Selected photo UUID state can survive configuration changes but photo lifetime is not fully draft-managed.
- No photo export/import, accessibility test, adversarial media tests, or release readiness claim.

## Next
Confirm compilation and artifact; harden media lifecycle before release. SPK-013 progress and gentle streaks follows only after source verification.
