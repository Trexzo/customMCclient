# UX S29 — original 1.8.9 ArrayList legibility and responsive bounds

## References / evidence quality

The user's prior Library comparison of Slinky, Rise and Vape documents a Slinky-style minimal, text-first HUD and Rise's compact, right-aligned enabled-module list and easing. Current vendor binaries/screenshots are **not established in the Library**, so this PR does **not** copy or claim native parity with a proprietary client. CustomMC stays Graphite with no Nebula branding, neon bars or huge identical cards.

Base: S28 [PR #870](https://github.com/Trexzo/customMCclient/pull/870) at `9b32fbfe7a3430c2a2a37a780bf77efa2c0f0e31`. That base has exact-head Foundation Windows+Ubuntu and complete official Mojang 1.8.9 native CI **SUCCESS**. S29 changes only the original Minecraft 1.8.9 ArrayList renderer, its pure HUD geometry/motion helpers, and tests — **no combat, settings registry, input, native mouse focus, class transformer, target data or Minecraft font hook changes**.

## Actual improvements

- Real logical-viewport-aware rows: at 854×480, the list now stops at the bottom of the screen instead of drawing more enabled-module labels off-screen.
- Optional existing overflow setting can consume the final available row when the stack does not fit, reporting the **exact count of enabled modules omitted**; a one-row viewport can correctly show only the overflow entry. If overflow was disabled in a saved profile, no counter is invented.
- Long source module names are clipped and ellipsized using the same conservative font-metric approximation already used for ordering, never drawn into unrelated HUD. Short labels at standard 1280×720 coordinates retain their original positions, colors and appearance.
- An X/Y value saved for a larger monitor recovers to a usable edge after a window resize or GUI scale change, without mutating the persisted setting. Ordinary saved positions are unchanged. The explicit left-aligned variant still works.
- The existing 12px entry offset now uses smoothstep rather than strictly linear position motion: no jitter at the end, with identical start, stop, opacity and instant disable behavior. This is **not FPS-independent timing** and must not be represented as such.
- The default remains text-first, right aligned, no backplate and the same understated two-pixel accent edge. No new theme/setting, no copied graphics.

## Strict quality checks

- `Minecraft189ArrayListLayoutTest`: original 1280×720 parity, 854×480 dense stack, correct overflow reservation, long/short/too-small names, position recovery, 0 capacity, and invalid input.
- `Minecraft189ArrayListModuleTest`: actual platform runtime `renderHud` with a bounded fake host at 854×480, five genuine enabled module identities, one-row overflow, no synthetic enabled names and persisted X/Y unchanged; ordinary view still renders all.
- `Minecraft189ArrayListMotionTest`: strictly monotonic smoothing, exact initial and final offsets, immediate removal on disable.
- This is source-level native platform simulation; **not a substitute for captured original Minecraft ArrayList enabled on screen**. Existing full original Mojang Binary Preflight must pass, and separate actual HUD-on screenshots must be gathered before describing the full aesthetic as visually verified. CI may render the GUI with ArrayList disabled by default.

No edits to any Target HUD implementation or parallel native focus/combat branches. Keep S29 draft and unmerged until regression suite results are known.
