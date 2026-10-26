# UX S2 — viewport-safe HUD drag foundation

Branch family: UI/UX only, following Draft PR #809. Minecraft target remains Java 1.8.9.

## Scope and compatibility

- Retain the original unbounded `HudDragController.begin(..., snapshot)` semantics unchanged for callers and previous unit tests.
- Add explicit `begin(..., snapshot, viewport)` for **opt-in viewport-bounded** HUD editing.
- Constrain widget top-left to visible logical bounds while preserving its original anchor and offset model. Oversize widgets align at the origin rather than becoming unreachable.
- Add explicit `moveSnapped(x, y, logicalPixelGridStep)` for grid alignment only during a bounded editing session.
- Preserve transactional drag semantics: `cancel()` restores the previous override (or absence), `commit()` retains the edited override. Offscreen moves cannot store invalid floats.
- Add five deterministic core regressions for edges, UI scale 2, BOTTOM_RIGHT anchor, invalid grids, and oversize widgets.

## Evidence boundary

This provides a hardened reusable **core HUD editing primitive**, not a claimed in-game drag-and-drop editor. Current TargetHUD and other 1.8.9 overlays still use their own native settings/render paths. Live editor wiring, preview, focus routing and persisted user profile layouts must follow in a separate scoped PR, with original Minecraft input and rendering acceptance. This PR must not be presented as game-ready.

## Integration policy

- No combat/World/UUID changes; no original game test bypasses.
- No modifications to `main` or V326/V327 branches.
- Require exact-head Foundation CI on Windows and Ubuntu. Native original-client CI may also execute but must be reported independently.
