# SPK-047 — Premium Experience Implementation Baseline

Status: SOURCE BASELINE VERIFIED; IMPLEMENTATION PENDING

## Canonical source
Repository: Lamont-Labs/Spark-app
Starting branch: spark3/task-046-parallel-install
Starting commit: e3b207b16f39e01692165747c07190b08c90428f
Working branch: spark3/task-047-premium-experience
Application: android-native/ (Kotlin, Jetpack Compose, Room)
Do not implement new Android work in the incomplete Expo source on main.

## Preserved capabilities
First Light onboarding; offline reflection editor; affirmations; journal; guided prompts; optional mood; private photos; progress; resurfacing; privacy preferences; import/export with integrity checks; reminders; accessibility.

## Non-negotiable constraints
- Keep offline-first operation and user data intact.
- Do not change Android application ID or signing identity without explicit verification and approval.
- Preserve existing installation and upgrade continuity; do not claim an APK is installable without building, signing and device checks.
- Do not replace working screens with static mockups.
- No Replit.
- Preserve existing light/dark palette direction until a visual design is approved.
- Treat animation as progressive enhancement; respect reduced-motion accessibility.
- All work must be committed in independently verifiable tasks.

## Premium upgrade sequence
1. Inventory actual Compose screens, existing navigation, components, and build/release workflow.
2. Establish cohesive typography, spacing, elevations, shapes, iconography, motion tokens, and contrast checks.
3. Refine Today: clear daily hierarchy, fast reflection action, subtle progress feedback, consistent premium cards.
4. Refine First Light onboarding with clear progress, back/skip, personalized focus, first reflection and reminder consent.
5. Refine Journal, Affirmations, Progress, Privacy with consistent empty/loading/error/success states.
6. Verify accessible touch targets, screen readers, dynamic type, dark/light contrast, keyboard behavior, and reduced motion.
7. Run unit tests, Gradle build, instrumentation where available, APK signature/package/version validation, and installation smoke tests.
8. Publish only verified builds; report exact artifact link and upgrade/parallel-install behavior.

## Observed source facts
- The main branch is an older Expo/React Native skeleton and references absent screens/assets.
- Spark 3 native source exists on numbered branches through task 046.
- Native theme currently defines light Ivory/Paper and dark Midnight/NightCard surfaces with Lavender/Apricot accents.
- MainActivity integrates First Light onboarding, reflection editor, and SparkNavigation.

## Verification
No build, emulator run, UI screenshot comparison, signing validation, or device installation was performed for this checkpoint.
