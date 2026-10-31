# UX S36 — original Minecraft 1.8.9 FontRenderer widths for ArrayList

## Grounded problem and reference design

The S29/S35 ArrayList uses an understated Slinky-inspired text-only stack with Rise-like right alignment, descending width ordering and compact overflow. In S35 this stack is native-rendered in Mojang 1.8.9, but its label widths still came from a hand-estimated Minecraft glyph table. In a genuinely variable-width Minecraft font, the estimate can misalign right edges, under/over-ellipsize names and misorder similarly sized labels.

The 1.8.9 MCP mapping pins original `avn/a(Ljava/lang/String;)I` to `net.minecraft.client.gui.FontRenderer.getStringWidth(String)` / `func_78256_a`. It is independent of the existing drawString method, and uses Minecraft's own actual active font / Unicode settings. The repository already pins the same 1.8.9 mapping source and exact class verifier.

## Scope

- Extend the typed `Minecraft189FontRendererAccess` original-class adapter with `getStringWidth(String)`; add a fail-closed mapped-method shape check and ASM delegate to the unchanged original obfuscated method.
- Thread actual original width measurements through the current host callback/text renderer. Old test/alternate hosts still report **-1 unavailable** and keep the existing deterministic conservative estimator; no guessed width is treated as an original-client witness.
- Use measured widths for ArrayList right alignment, width sort, text clipping/ellipsis and optional backplate. Preserve the previously saved x/y, right-aligned setting, category mode, enabled-module identity, colors, font choice, animation timing and combat data.
- In the original Mojang two-player screenshot, demand `CUSTOMMC_OFFICIAL_189_REAL_ARRAYLIST_NATIVE_FONT_METRICS_PASS=YES` from **actually measured native text in the original HUD render pass**, not from a mock or simply loading a class. The full native run and the screenshot pixel witness remain unchanged and obligatory.
- Add fixture coverage for original `getStringWidth` ASM bridge, mapped method verification, adapter forwarding, legacy fallback and width-provided ellipsis. No copied font or proprietary UI assets.

## Release gate

Require exact-head Foundation CI Windows + Ubuntu and official Mojang 1.8.9 native Binary Preflight with real two-player screenshot and native font marker. The still image establishes glyph legibility/right-alignment, not FPS-independent motion. This is a **separate stacked visual draft on S35**, not a merge or a production release. Any failure of existing native PvP/security gates stays red.
