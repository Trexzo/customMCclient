# CustomMC UX S55 — contextual Settings search placeholder

## Source evidence

`ClickGuiComposer` always displayed `Search modules` in the common search field when empty. That is accurate on Modules, but global Settings actually filters registered settings from `SettingListPageContent.entries(snapshot)`. The misleading copy remained even after S54 stabilized the search/drag lifecycle.

## Change

When the selected page is `ClickGuiRuntime.SETTINGS_PAGE_ID` and the search string is empty, the composer draws `Search settings`. The established `Search modules` remains unchanged for Modules and other page contexts. When a user has typed a query, the exact query continues to be displayed without replacement. No runtime search filtering, focus behavior, pointer hitbox, scroll geometry, theme, module control, saved setting, game, network or combat logic changed.

## Verification

A renderer-command regression test proves the expected clipped placeholder on Modules vs Settings and typed-query precedence without altering the clear affordance. Require exact-head Foundation Windows/Ubuntu and full official Mojang 1.8.9 binary acceptance. An intentional placeholder glyph change on Settings may require an appropriate reference screenshot review; **do not weaken pixel or gameplay gates**. Draft/unmerged stacked on S54 #906.
