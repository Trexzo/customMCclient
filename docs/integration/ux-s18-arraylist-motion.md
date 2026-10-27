# CustomMC UX S18 — native HUD ArrayList entry motion

**Stack:** [S17 UI PR #842](https://github.com/Trexzo/customMCclient/pull/842) (Graphite default, no Nebula). UI-lane only; no combat module or host/native controller hooks touched.

## Actual production behavior

- The real `Minecraft189ArrayListModule` HUD renderer owns a bounded, per-module entrance phase rather than an illustrative preview.
- First visible snapshot renders immediately, avoiding hidden modules at startup.
- New enabled modules move 12 logical pixels inward while opacity eases across five HUD draw callbacks. No asynchronous timers, clock dependency or stochastic motion.
- Live registry status is authoritative **every frame**. When a module is disabled, it disappears immediately from the render list and the motion state. Re-enabling starts a new entrance; no last-known/disabled row persists.
- The overflow count is always sourced from actual enabled modules and remains static (no falsified module transition count).
- Existing left/right text alignment and compact optional background styles remain available.
- New persisted ArrayList module setting **Animate Entries**, enabled by default. Setting it off makes subsequent newly enabled modules render fully without transitions, useful for minimal/reduced-motion profiles.
- Turning ArrayList itself off clears all per-module motion state; next enable starts with an immediate full snapshot.

## Verification & constraints

- `Minecraft189ArrayListMotionTest`: initial snapshot, bounded phased entrance, re-entry, immediate removal, immutability, opacity and reduced-motion operation.
- `Minecraft189ArrayListModuleTest`: actual renderer receives entry slide and partial alpha, settles into correct right-aligned X, excludes disabled module immediately and honours persisted animation-off setting.
- State size tracks only currently enabled module IDs, no identity/UUID, combat or network use.
- **Not a native timing guarantee**: animation advances once per HUD render callback, not milliseconds; actual perceived speed depends on FPS and 1.8.9 render cadence. It is intentionally short to avoid distracting long transitions.
- Existing glyph widths are conservative estimates until native Minecraft font measurement is independently wired and tested. No shader blur or custom fonts claimed.
- Exact commit Windows/Ubuntu Foundation and original official Minecraft 1.8.9 binary CI are required. Keep Draft and run a genuine in-game screenshot/visual acceptance pass before merge/release.
