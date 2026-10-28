# UX S26 — genuine original Minecraft 1.8.9 Module Details/back screenshot

## Purpose

S25's genuine Java8/X11 Minecraft UI artifact `12819354015` from [native run #40697489881](https://github.com/Trexzo/customMCclient/actions/runs/40697489881) visibly proved the new 3-item Graphite sidebar and Settings navigation, but did **not** open or capture the contextual Module Details page. S25 Foundation CI passed Windows and Ubuntu. The full S25 native suite eventually **failed at a separate V26 original Scaffold source cursor/eye/reach candidate checkpoint**; preserve that negative result, do not downgrade the strict stage or misrepresent original full-binary certification.

S26 adds a strictly opt-in, source-backed **physical UI interaction and screenshot proof**, not another Java mockup.

## Original runtime acceptance

The pinned real Mojang 1.8.9 `RunMojang189SingleplayerSmoke.py` accepts `--visual-capture-details` only with `--visual-capture-dir`. Its visual stage now:
1. Loads real singleplayer, captures `world.png`; opens CustomMC with physical X11 Right Shift and demands `CUSTOMMC_189_NATIVE_VISUAL_PAGE=modules`; captures `modules.png` and confirms real Graphite pixels.
2. At pinned 854×480/GUI scale 1 physically right-clicks inside the actual first Modules row (`300,114`). Takes the **pre-click log cursor** and requires the genuine post-render source `module-detail` marker strictly **after** that click; captures and validates `module-detail.png`.
3. Physically left-clicks S25's source-native top-right `< Modules` return link (`733,47`), takes another pre-click cursor and requires a **new** `modules` render marker (the earlier initial Modules marker can never count). Captures `modules-return.png` and validates real Graphite pixels.
4. Physically presses Down and requires genuine Settings renderer marker; captures `settings.png`; closes overlay using Right Shift.

The original screenshot step is mandatory in `.github/workflows/minecraft-189-real-client.yml`. Original artifact upload uses `*.png` and `if: always()`; even a test failure retains any genuine frames for inspection. No `continue-on-error`, no screenshot substitution, no simulated `ClickGuiModel.select` in the screenshot runner, no gameplay/controller or binary changes.

The new offline `test_mojang189_s26_detail_capture.py` verifies *freshness semantics*, rejects a pre-existing Modules marker as return evidence, checks source inclusion of actual X11 right and left clicks, output paths and workflow flag. **Offline tests are not substitute evidence**.

## Actual initial native failure and scoped correction

The first S26 native screenshot run [#40700783690](https://github.com/Trexzo/customMCclient/actions/runs/40700783690), exact initial head `2c507f12a62054c135b4cfc5813275ce588aa196`, **failed correctly**. Authentic artifact `12820458916` contained `world.png` and `modules.png` only. The screenshot runner then timed out: `no fresh source-rendered module-detail page after physical original X11 navigation`. This was not a PNG parser issue or a synthetic pass.

The source diagnosis is independently testable: `Minecraft189OverlayFocusGuardian.permitsProductionMouseInput` deliberately rejects real `GAMEPLAY_CAPTURED` / `Mouse.isGrabbed()`. The former S25 Right Shift toggle only changed the core model, leaving vanilla gameplay focus and grabbed mouse intact. Rendering the overlay alone could not enable physical mouse interactions. **Never remove or relax that production mouse guard.**

Verified against the exact original 1.8.9 joined.srg (`BigBroadBean/mappings-extracted`): `ave/o()V` is `func_71364_i` = `setIngameNotInFocus`, and `ave/n()V` is `func_71381_h` = `setIngameFocus`.

The UI-only `Minecraft189ClickGuiNativeFocusLease` now calls **vanilla methods** (not synthetic focus fields, guessed mouse coordinates or custom LWJGL grabs) precisely when the real Right Shift overlay opens/closes. Opening requires original native screen = null, known focus, real grabbed Mouse; native post-readback must prove GUI_READY and ungrabbed, otherwise the overlay is closed fail-closed. On closure, only a truly owned focus release can be restored, and never while a vanilla GuiScreen owns focus. Existing game/native tests without an X11 Display remain unchanged. The bridge only intercepts UI key-state transition handling, leaving combat and attack/click-rate paths intact.

Extra native acceptance now demands `CUSTOMMC_189_NATIVE_GUI_CURSOR_RELEASE=YES` before the physical Details right-click, and `CUSTOMMC_189_NATIVE_GUI_CURSOR_RESTORE=YES` after Right Shift closes the custom overlay. Java tests cover mapping exactness, safe ownership transitions, strict refusal of unknown/vanilla focus, and the unchanged captured-mouse rejection. This was a necessary repair discovered through **genuine original-game failure**, not a relaxation of admission.

## Acceptance status

At PR creation, genuine S26 `module-detail.png` and `modules-return.png` do not yet exist. Do **not** claim actual native back-link functionality until both pictures and source markers are produced by a successful CI run, and do **not** claim full native CI certification unless the entire original Minecraft job has passed. If a real click fails the bounded deadline, leave the stage red and inspect the authentic source.

S26 is stacked on S25 [#862](https://github.com/Trexzo/customMCclient/pull/862) at exactly `7600acf453c7e0a398ab34d3c1366baa56e5c1a6`, on a separate visual branch. None of the combat or native-font branches are modified.
