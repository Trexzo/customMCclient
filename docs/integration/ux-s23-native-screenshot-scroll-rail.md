# UX S23 — actual original Minecraft screenshots → visible scroll depth

## Authentic evidence

The S22 exact-head Original Minecraft 1.8.9 Binary Preflight run [#40690229598](https://github.com/Trexzo/customMCclient/actions/runs/40690229598) completed its dedicated **real original-world X11 screenshot stage successfully**. The uploaded GitHub Actions artifact is `custommc-189-real-graphite-visuals` (artifact ID `12816724472`, from the same run; captured 28 October 2026).

The archived `modules.png`, `settings.png` and `world.png` are authentic 854×480 frames from the pinned Mojang 1.8.9 runtime. They show a compact dark Graphite interface with clear sidebar navigation, a settings value well and simple, restrained module toggle; no purple Nebula styling. The original rendered screenshot visibly cuts the bottom of a module row when the module list continues beyond the panel, and Settings ends at a similarly tight boundary. A user cannot infer scroll depth from the static view.

The screenshot capture proves that the original game rendered both pages, but **is not a certification of GUI scale 2/3, native font quality, scrolling action, module detail view, or visual parity with Vape/Slinky/Rise**.

## Native production changes

Introduce pure `UiContentScrollRail.compose` and render it through the actual content composer for `ModuleListPageContent`, `SettingListPageContent`, and `ModuleDetailPageContent`:
- Slim 2px Graphite border track and 3px muted thumb, deliberately confined to the existing right content margin, outside settings/module row pointer hitboxes.
- Thumb size represents viewport/content ratio, with a 27px lower bound for very long module lists. Thumb position reflects the existing `ClickGuiContentScrollState` offset (no independent timer, inferred input or fake scroll ownership).
- Hidden when no overflow or too narrow/short to fit; clamp overlarge offsets without changing retained scroll state.
- No pointer handler, click/drag redirection, changes to module toggles, settings editor behavior, hitboxes, native bindings, original Minecraft input, or combat controller state.

This is an incremental, real Java renderer improvement supported by real screenshots — not a mockup. It preserves the compact Graphite design, avoiding visual noise while signaling scrollability.

## S23 genuine screenshot verification (28 October 2026)

The updated S23 source also completed its **original Mojang Java 8/X11 GUI capture** in [run #40693008780](https://github.com/Trexzo/customMCclient/actions/runs/40693008780), stage `S22 capture original Mojang ClickGUI visual evidence` = SUCCESS, followed by successful PNG artifact upload. Exact S23 binary input: `e30cedaed63d8d10e33f738e8836071c73201478`, output artifact `custommc-189-real-graphite-visuals` ID `12818670405` (`world.png`, `modules.png`, `settings.png`).

I inspected the downloaded **actual** 854×480 PNGs (not source code or an approximation). The new 3px muted right-hand scrollbar thumb is visibly present at the top of the Modules and Settings viewport, aligned to the content margin and not overlapping row text, toggles or editable values. The state still shows 85 modules and the authentic native Settings values. This satisfies the initial native visual scroll-discoverability acceptance.

The scrollbar is deliberately restrained, and the final row can still be partly clipped at the bottom: S23 makes the presence of more content explicit rather than solving all pagination, mouse-wheel, varied GUI scale or font-size issues. **Genuine scrolled-page captures and module-detail screenshots remain future acceptance gaps.** The overall long original-binary test may still be in progress after the successful capture stage.

## Verification

`UiContentScrollRailTest` covers no-overflow suppression, long-content proportional thumb, beginning/middle/end scroll travel, viewport containment, theme roles and nonfinite geometry rejection. Existing module/settings/detail interaction suites stay active. Real screenshots of scrolled pages are still needed before calling the whole scrolling UX visually approved. Keep this draft until exact-head Windows/Ubuntu Foundation and pinned native original-game acceptance pass.

Stacked on UX S22 [#854](https://github.com/Trexzo/customMCclient/pull/854), exact base `1b4ab8821f9f3d62dddd0db99f458bc18016ae78`; independent combat code untouched.
