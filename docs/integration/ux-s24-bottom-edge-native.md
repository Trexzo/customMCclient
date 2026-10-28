# UX S24 — original Minecraft ClickGUI bottom-edge closure

## Grounded visual issue

The genuine original Mojang 1.8.9 X11 captures from [S23 run #40693008780](https://github.com/Trexzo/customMCclient/actions/runs/40693008780), artifact `custommc-189-real-graphite-visuals` ID `12818670405`, showed the new slim scrollbar working, but the last partially visible Modules row abruptly cut off against the bottom of the panel. Settings showed similarly cramped bottom clipping.

The objective here is **not** to invent a redesign or replace Minecraft native fonts. It is to make the real panel's bottom edge deliberate and safe, while preserving compact dark Graphite and existing scroll behavior.

## Actual Java source changes

- New pure `UiContentBottomCue` draws an 18px Graphite surface footer, subtle 1px dividing line, and muted `SCROLL FOR MORE` label only when more content is below the viewport. It is omitted when the end of the content has been reached, so the last row remains fully accessible.
- In Modules, Settings and Module Details this footer is drawn **after** row and scrollbar commands. It masks the partial last row that had previously been visible at the hard viewport cutoff, without modifying row positions, scroll step or component dimensions.
- `UiContentBottomCue.blocksPointer` uses **exactly the same active viewport geometry** as the rendered mask. A left/right PRESS through the masked area must never toggle a partially covered module, change a setting, or begin keybind capture. Non-press release/motion is retained for detail editors.
- The S23 scroll rail finishes 21px above the panel bottom to avoid painting beneath the 18px footer.
- No new mouse wheel callbacks, packet/controller/gameplay logic, CSS, synthetic Java UI, color themes, font renderer paths or edits to parallel native-font PR #848.

## Verification and limitations

New `UiContentBottomCueTest` verifies no-overflow and end-of-list suppression, exact 18px geometry and ARGB theme, click-through veto, and invalid measurements. Extended `ClickGuiModuleRowActionTest` and `SettingListPageContentTest` demonstrate a **real hidden row cannot respond to a click until the existing wheel scroll makes it visible**.

Original-game screenshot acceptance remains mandatory: exact-head Foundation CI on Windows/Ubuntu plus the pinned original Minecraft capture stage, then inspect `modules.png` and `settings.png` for clean lower edges, visible text, unaffected toggle/value controls. Current prior screenshots demonstrate the issue, **not its fix**. A genuine scrolled-end screenshot is still needed before closing all scrolling UX acceptance.

The Minecraft 1.8.9 built-in font remains intentionally unchanged; scaling it without reliable native glyph metrics/GUI-scale evidence would risk worse clipping. Parallel font measurement work remains independent. No claim of parity with Vape/Slinky/Rise.

S24 stacks only on S23 [#858](https://github.com/Trexzo/customMCclient/pull/858) at `5d4edb3eca08b7351c55d26b57a0241cce4eb3d7`. Keep draft, no `main` merge, and no combat changes.
