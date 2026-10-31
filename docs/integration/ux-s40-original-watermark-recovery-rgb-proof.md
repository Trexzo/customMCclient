# UX S40 — genuine Minecraft 1.8.9 Watermark recovery evidence

## Motivation and reference

S39's minimalist Minecraft Watermark recovers persisted out-of-frame X/Y without changing saved settings or the original font. The S39 source runtime regression passed, but the original two-player screenshot fixture had not enabled the Watermark; that screenshot could not prove recovery. The existing simple Slinky/Rise-inspired text HUD remains the visual reference; no new theme, copied font or mock screenshot.

## Source-first authentic evidence

Under the existing dedicated native `--hud-visual-dir` opt-in **only**, the original host process starts with the new JVM property `custommc.acceptance.captureRealWatermarkRecovery=true`. The Mojang runtime:
- installs real native host, actual Target HUD, ArrayList and remote player Box ESP as before;
- sets the **actual registered Watermark settings X=4096 and Y=4096** (not magic paint coordinates), and enables the actual Watermark module through `ModuleController`;
- leaves normal user defaults and profiles untouched outside the explicit original-game test.
- emits a source-native `CUSTOMMC_OFFICIAL_189_REAL_WATERMARK_RECOVERY_PASS=YES saved=4096,4096 visible=8,224` only after `drawText` and `endUi` succeed in the true Watermark render pass for the genuine 854×480/GUI-scale-2 window.

The independent native X11 screenshot must then contain original white glyph pixels at the **recovered bottom-left position** (logical 8,224 → 16,448 screen pixels) in x=16..179/y=446..460. The S39 original screenshot has zero bright neutral pixels in this region above the vanilla y>=461 bottom rail. The S40 validator requires >=80 bright neutral pixels distributed across >=28 x columns and >=7 y rows. Mere Java receipts or a white vanilla bottom edge cannot pass alone.

Enabling Watermark makes it a fourth **genuine enabled module**, so the existing right-side native ArrayList classifier is extended to demand **exactly four** 22-physical-pixel-spaced glyph bands in this original-game fixture; the default, no-Watermark classifier still demands exactly three. Previous S36 24px row-pitch and wrong three/four row count remain negative cases. The original Target HUD glyph, native font width, soft shadow, real source enable state, dedicated-server remote player and ESP gates all remain mandatory.

## Safety and non-goals

No editing of combat/world state, packets, user profile persistence, production enable defaults or renderer timings. The CI-only test property is not a user-visible feature. The actual framebuffer is captured from the original Mojang 1.8.9 window using X11 (no PIL/HTML synthetic render substituted); Python unit fixtures are solely classifier regressions.

Release only if exact-head Windows+Ubuntu Foundation CI and complete original Mojang Binary Preflight pass. Keep PR draft/unmerged, stacked on S39 #889, until this occurs.
