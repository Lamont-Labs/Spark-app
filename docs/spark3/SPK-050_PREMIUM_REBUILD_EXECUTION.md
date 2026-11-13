# Spark 3 — Premium Experience Rebuild (approved)

Status: ACTIVE — not a completed visual rebuild
Baseline: spark3/task-047-premium-experience
Identity: midnight / electric blue / cyan / ice
Rule: electric when something meaningful happens; calm when reflecting

## Diagnosis
The current app uses conventional Compose Material 3 scaffolds, large vertical card stacks, generic typography, plain navigation, limited motion, and a mostly static five-page onboarding flow. Palette swaps alone do not meet the approved premium target.

## Native open-source reference shortlist (verified 2026-11-13)
1. Gurupreet/ComposeCookBook — MIT — https://github.com/Gurupreet/ComposeCookBook — Compose layout, navigation and polished component patterns; adapt techniques, do not paste the demo app.
2. SmartToolFactory/Jetpack-Compose-Tutorials — Apache-2.0 — https://github.com/SmartToolFactory/Jetpack-Compose-Tutorials — gestures, graphics, transitions and canvas techniques; selectively adapt.
3. canopas/compose-animations-examples — https://github.com/canopas/compose-animations-examples — motion references only until license verified.
4. Ramotion/paper-onboarding-android — MIT — https://github.com/Ramotion/paper-onboarding-android — conceptual onboarding choreography only; legacy Android library is not a drop-in Compose dependency.

No third-party source has been copied or dependency added. Preserve notices and license terms for any future adaptation.

## Target screen architecture
- Today: one prominent action above the fold, a concise daily snapshot, quick capture paths, restrained reflection discovery; reduce mandatory scrolling.
- Journal: date-grouped timeline with visual density controls, media-friendly cards, clear empty state and entry search/filter only when backed by data.
- Affirm: daily focal statement, readable typography, simple browse experience, no fake engagement metrics.
- You: coherent preferences, progress, privacy and export/backup access with real state and clear navigation.
- Onboarding: cinematic welcome, focus selection, first real reflection, optional reminders, privacy assurance; progress and back/skip always reliable.

## Implementation order and acceptance gates
P0. Capture device screenshots for onboarding, Today, Journal, Affirm, You at small and large widths; compare with reference.
P1. Build native design primitives: layered glass surface, edge glow, premium header, consistent iconography, safe-area handling and reduced-motion semantics.
P2. Rebuild Today information architecture (not simply repaint existing cards).
P3. Rebuild navigation and reflection editor with keyboard, accessibility and one-save semantics verified.
P4. Rebuild Journal, Affirm, You and onboarding using shared primitives.
P5. Integrate approved branding as appropriately cropped and licensed assets, never as a static substitute for the app.
P6. Unit tests, emulator interaction tests, screenshots, package/signature verification, and physical device review.
P7. Publish a new APK only after all required checks pass; retain application ID and signing continuity.

## Non-negotiable constraints
- Native Kotlin/Compose app stays authoritative; do not substitute a static web page or screenshot APK.
- Keep Room data and offline privacy behavior unchanged unless a migration is explicitly tested.
- No internet permission, analytics SDK or intrusive notifications.
- All actions must be real and accessible; no fake buttons or decorative affordances.
- A green compile does not establish premium visual quality; screenshot review and user acceptance are separate gates.
