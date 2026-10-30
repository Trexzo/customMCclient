# UX S31 — genuine two-player Target HUD and ArrayList visual evidence

## Problem / reference provenance
S30 [#873](https://github.com/Trexzo/customMCclient/pull/873) passed Windows/Ubuntu Foundation and complete original Minecraft 1.8.9 Binary Preflight, but its singleplayer screenshots could not visually verify the Target HUD with a real remote player. We retain Slinky restraint, Rise information hierarchy, and Vape compact utility as *design direction from earlier research*, without copying vendor code or claiming their binaries were available.

## Scope
This change stacks directly on S30's exact verified SHA `b242563e4a681bbb80e2369e8497a24506476766`, not on combat branches.

- A **disabled-by-default** acceptance JVM flag `custommc.acceptance.captureRealHudVisuals` enables only the ordinary existing Target HUD and ArrayList modules through `ModuleController` for the test client; it does not change persisted settings or production defaults.
- Both modules emit one bounded receipt only from successful actual render passes. The Target HUD still requires the unchanged coherent original nearest-player distance/index and rotation source. ArrayList receipt requires at least two actually processed rows.
- Reuse the existing offline dedicated server and **two genuine Mojang graphical JVMs**, the exact-UUID remote Box ESP draw, and its original OpenGL framebuffer-pixel evidence. Only after both HUD receipts and the remote framebuffer evidence does the runner capture the **previously isolated original CIHost X11 window** as `target-hud-arraylist.png`. This is not a synthetic renderer.
- New independent Github Actions screenshot stage uploads an artifact even on failure; the CLI fails if `--hud-visual-dir` is requested without `--esp-visual`.

## Acceptance / remaining limitations
1. Foundation CI Windows and Ubuntu **must** pass on exact commit.
2. Complete original 1.8.9 native workflow **must** pass, including the new two-player stage.
3. Inspect the archived PNG for actual text/target visibility, correct cropping, spacing and contrast. The source receipts and PNG hash establish actual runtime evidence, **not** an independent aesthetic assessment.
4. Existing Scaffold/native combat failures remain unrelated and may not be waived.
5. The explicit Box ESP outline is present in this fixture to establish remote native UUID/framebuffer evidence; it is not a claim that Box ESP must be enabled for ordinary HUD use.

No changes to combat behavior, targeting authority, native mouse-focus branch, proprietary assets, UI theme configuration, or main. Keep the PR a **draft** until exact-head tests and image review.

## First real screenshot finding and focus recovery (2026-10-30)

The first genuine original-game two-player screenshot stage **passed its render/framebuffer markers**, and its archived PNG (`target-hud-arraylist.png`, SHA256 `11f1493064b0bab8ba0ba3f8e6b228c2c5815c5fcfc507571486365c1be98b73`) indeed contains the real Target HUD and ArrayList. However, the host's vanilla **Game menu was open over the HUD**. The first screenshot therefore does **not** visually certify spacing, hierarchy, clipping or contrast.

The corrected test now explicitly opts into the already verified **original obfuscated Minecraft currentScreen trace** on the host, activates only the previously isolated original host X11 window, and reads the latest source-native screen state. If and only if the original screen is **axp (vanilla pause GUI)** it sends one physical Escape to that exact host window and requires a **fresh post-input `(in-game/no GUI)` native callback**. A screen already clear receives no Escape. Missing, other or stale source states reject the screenshot. It checks GUI-clear again immediately before capture. Separate host-source regression tests cover existing clear, true pause dismissal, unknown GUI and stale-reply rejection.

This follow-up does **not** change HUD drawing, target selection, player data, combat or normal user profile state. The first run's `PASS` remains correctly classified as genuine render/screenshot evidence **but visually obscured**; the corrected exact-head run must be inspected before claiming a clean in-game design screenshot.
