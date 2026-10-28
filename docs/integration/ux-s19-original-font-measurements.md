# CustomMC S19 — original Minecraft 1.8.9 font widths

**Stack**: S18 [UI PR #845](https://github.com/Trexzo/customMCclient/pull/845). Native UI/font instrumentation only, not combat or input.

## Production path

- Original Mojang 1.8.9 `FontRenderer` (notch `avn`) exposes `a(Ljava/lang/String;)I`, corresponding to SRG `func_78256_a` / MCP `getStringWidth`. Its exact class/method descriptor is verified before transformation, and the transformed original object receives a read-only `getStringWidth(String)` bridge on `Minecraft189FontRendererAccess`.
- `Minecraft189DefaultFontRenderer` delegates width requests to the same native object that executes `drawString`; `Lwjgl2LegacyUiHostCallbacks.textWidth()` forwards this capability to HUD consumers.
- `Minecraft189ArrayListModule` measures labels once per HUD render, caches widths for the sort comparator, sorts by actual native widths, and uses the same widths for right-alignment and optional compact row backgrounds. This respects Minecraft's resource-pack font metrics, Unicode flag, bold and formatting semantics rather than assuming 6px per glyph.
- The exact measurement path is active only if the real native font exposes it. Older synthetic/test callback implementations preserve the previous deterministic `Minecraft189HudTextMetrics` approximation via a return value of -1.
- No new renderer input, entity state, combat arbitration, client packet, networking or controller behavior. The width lookup is read-only and invoked on the normal HUD render thread.

## Verification / limits

- Mapping regression covers the precise original owner `avn`, descriptor `(Ljava/lang/String;)I`, SRG name and source method.
- ASM regression proves injected `getStringWidth(String)` invokes the original `avn.a(String)I` and that unknown/missing native shapes fail closed.
- Host font regression confirms measurement uses the same renderer without issuing a draw call.
- ArrayList host integration regression overrides native measurement for selected labels and verifies that actual widths, rather than fallback estimates, determine ordering and position.
- This is not a custom font renderer, a new font resource, or a visual screenshot. Native Minecraft 1.8.9 original binary and exact-head Foundation CI must pass. Actual on-screen alignment still needs Minecraft capture/review.

**Source provenance**: the authoritative original notch mappings and exact class shape remain pinned in `Minecraft189Mappings`; source-compatible 1.8.9 `FontRenderer.getStringWidth` is also documented at https://mc-packet-reference.netlify.app/source/net/minecraft/client/gui/fontrenderer/. Do not silently guess mapping names on later game versions.
