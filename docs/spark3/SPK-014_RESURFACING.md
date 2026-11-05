# SPK-014 — Reflection resurfacing

Date: 2026-11-05
Branch: spark3/task-014-resurfacing
Parent: 3042337d38b8c97b3d6dd64531cf3cc650e64cb9
Status: SOURCE_COMMITTED / CI_PENDING

## Implemented
- Deterministic offline selection from existing Room reflections.
- Only reflections at least seven calendar days old are eligible.
- Prefers favorite reflections in candidate ordering; daily index selects among eligible items.
- Today displays one quiet card with original text or a photo-only reference and saved date.
- No remote service, notifications, separate engagement score, or schema migration.
- No eligible reflections: no resurfacing card.

## Evidence
- SPK-013 latest source commit f9d2840034887ca469d9c7f7e2321af6d267aa93 passed CI run 42106270619.
- SPK-014 source commits d32680d594dd76e97332407b22f47d797f1a3179, 22ea8fdbb56c268159bfffe1582aa1144d1a5611, 2118192e953526284018600b95cf14418bea299e.
- SPK-014 compilation and device behavior pending.

## Outstanding
- No user toggle to disable resurfacing yet; privacy preferences task SPK-015 must add it before release.
- Favorites weighting is indirect: sorting before modulo does not guarantee favorite priority.
- Local date can change without Compose recomposition.
- Photo-only cards show a textual reminder, not image preview.
- No automated selector boundary tests or accessibility checks.

## Next
Verify CI and implement SPK-015 privacy controls with opt-out.
