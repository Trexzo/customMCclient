# UX S47 — opt-in native Watermark theme accent

## Visual reference

The original Mojang Minecraft 1.8.9 S40–S46 screenshots show a minimal white source-font Watermark and right-aligned ArrayList with an understated Graphite accent rail, consistent with Slinky/Rise text-first HUDs. S46 introduced a fully optional native watermark shadow. The existing ArrayList and Target HUD already resolve shared client `UiThemeSettings` colors, but the Watermark cannot follow that accent without changing its source string or editing code.

## S47 visual change

- Add a persisted, discoverable `render.watermark.useThemeAccent` Boolean under Watermark in the real ClickGUI, order 40, **off by default**. On existing profiles and the default two-player original screenshot, text remains exactly white `0xFFFFFFFF`, without any change to original glyph metrics, placement, S39/S40 X/Y recovery or S46 shadow default.
- If opted in, resolve the **existing registered theme** through `UiThemeSettings.resolve(settings, UiThemes.graphite())` and draw Watermark using its `UiColorRole.ACCENT`: Graphite `0xFFEE8755`, Blue `0xFF5994EA` in tested themes. This is the same source-native theme API used by ArrayList; no new palette, texture, shaders or bundled fonts.
- S46 optional shadow stays independent: the native original Minecraft 1.8.9 `FontRenderer.drawString(..., true)` receives the same resolved color. Source text, saved positioning, UI viewport clamp, clipping and render priority remain unchanged.
- Preserve original constructor compatibility for third-party/tests without theme registry by keeping the 2-arg overload, using Graphite as fallback **only when opt-in is enabled**. The actual host catalog injects the real SettingRegistry.
- Register and close all three setting layers (value, Boolean presentation, per-module binding), including installation-rollback and normal-close paths.

## Test gates

- Runtime test with genuine module registry: default unshadowed white label at 8,8; shadow-only still white; opt-in plus shadow displays Graphite accent, live theme change yields Blue accent, neither changes glyph, text position or clip balancing; recovered saved `x/y=4096` at 854×480 displays correctly without rewriting preferences; turning both options off returns to white/unshadowed and disabling removes the module.
- Host lifecycle test checks **five** Watermark settings (Text, X, Y, Text Shadow, Theme Accent) and new default `false`, replacing previous hardcoded count 4.
- Exact-head Foundation Windows/Ubuntu and complete original Mojang 1.8.9 Binary Preflight are required. Real two-player screenshot proves **default-white** nonregression, while the specific opt-in accent state is checked by runtime source callback tests; do not claim it appears in the default genuine-game screenshot.
- Remain **draft/unmerged** on S46 #896. No combat, network packets, player authority, timing, gameplay hooks or new rendering dependencies.
