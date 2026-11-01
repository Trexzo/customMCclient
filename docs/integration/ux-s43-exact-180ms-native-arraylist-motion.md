# UX S43 — actual 180 ms ArrayList entrance

## Original visual reference and issue

The S40–S42 real Mojang Minecraft 1.8.9 screenshots confirm the existing spare Slinky/Rise-style text-first ArrayList: compact 11 logical-pixel row pitch, original Mojang font metrics, understated native shadow, narrow accent rail and no default oversized plates. The animation should look light and consistent rather than snapping in at high FPS or lingering at low FPS.

S35 originally advertised `ENTER_DURATION_NANOS=180,000,000`, with newly enabled rows initialized at phase **0.22**. Subsequent frames added `elapsed / 180ms` directly to that phase. The last **0.78** therefore finished after **140.4ms**, not 180ms. This is a presentation-only error in the timing curve; it has no effect on module activation, network packets, player authority or saved settings.

## Bounded fix

- Keep exactly the existing first visible phase **0.22**, independent of FPS, easing function, 12px maximum slide, original alpha composition, 11px pitch and default opt-in/out `render.array-list.animate`.
- Evolve subsequent phase by `(1 - 0.22) * elapsed / 180ms`, so opacity and smoothstep slide reach settled state at **180ms** after the first visible frame.
- Preserve the tested safety behavior of the bounded elapsed clock, backward-time veto, immediate disabled-module removal, re-enable restart, fully visible first snapshot and instantly settled reduced-motion option.
- No new background, UI setting, thread, timer, input path, experimental animation backend or external assets.

## Regression evidence

- Explicit boundary checks at **0ms = .22**, **90ms = .61**, **140ms below 1.0** (the old bug would have already finished), **179ms below 1.0** and **180ms = 1.0**, sampled at **30, 60, 144 and 240 FPS**.
- Verify the smoothstep slide is still decreasing between 140ms and 179ms and rests exactly at zero at 180ms; compare 30 against 240 FPS at the same elapsed time.
- The original two-player S40–S42 Mojang 1.8.9 screenshot remains the unmodified settled-layout witness, while the above deterministic clock tests establish the dynamic timing behavior; never claim a still screenshot proves the actual animation duration.
- Exact-head Windows/Ubuntu Foundation and full original Mojang binary preflight (genuine native HUD/Watermark/ArrayList, full standalone combat gates) remain mandatory. This PR is separately stacked **draft/unmerged** on S42 #892.
