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
