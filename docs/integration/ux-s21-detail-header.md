# UX S21 — bounded source-native module header

## Provenance, scope, ownership

This GitHub-only UI branch stacks exclusively on S20 [#850](https://github.com/Trexzo/customMCclient/pull/850) at commit `66d16508975c2840f14759a4d0e4b35f464a52d0`, and the underlying S19 [#849](https://github.com/Trexzo/customMCclient/pull/849). Neither combat code nor `main` is changed. The independent S19 native font width [#848](https://github.com/Trexzo/customMCclient/pull/848) remains intact. All styling uses the S17 neutral Graphite theme roles; Nebula is not reintroduced.

## What changed in the real ClickGUI

`ModuleDetailPageContent` previously emitted four unbounded text draws for selected-module name, lifecycle state, description and bind. Long description/labels/key chord strings could paint over the whole page or each other, particularly at nondefault GUI scales or with wider source-native fonts.

`UiModuleDetailHeader.compose` now emits actual 1.8.9 draw-command groups with independent clips:
- Title: available full header width; cannot extend into the surrounding ClickGUI shell.
- State: reserved left 86px; warning/error/positive/muted roles reflect actual `ModuleController.stateOf()`.
- Description: begins after a 90px status partition, so it cannot overlap lifecycle state.
- Keybind: bounded to the existing 280px native clickable keybind rectangle. The capture label still uses the accent state color, without mutating keybind state.
- Quiet 1px divider beneath header clarifies where settings begin without raised capsules, glow or gradients.
- Narrow views omit the description when there is not enough horizontal room. Zero/negative drawing areas are never emitted. Scroll offset moves all header elements consistently.

All user interaction coordinates, bind capture, module selection, edit validation, search, scrolling and `World`/packet/input hooks remain on existing controllers. No fake player info is rendered.

## Verification contract

New `UiModuleDetailHeaderTest` checks clipped long names/descriptions, distinct status/description regions, state-color fidelity, bind capture visual alignment with the unchanged native hitbox, narrow viewport legality, and scroll movement. Existing module detail behavioral regression tests remain active.

This is a real Java UI modification, **not** a mockup. Exact-head Windows+Ubuntu Foundation and official Mojang 1.8.9 must pass. A genuine in-game screenshot at GUI scales 1/2/3, with resource-pack font and long labels, is still necessary for final visual approval. Do not call this done solely because code-level draw commands pass.

## Design approach

Source inspiration: Vape's compact detail access, Slinky's disciplined separation and typography, Rise's accessible module settings. This implementation copies no assets or source from those clients.
