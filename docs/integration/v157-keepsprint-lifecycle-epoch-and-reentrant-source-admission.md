# V157 — KeepSprint source-click lifetime epoch and nested callback rejection

## Exact source authority

This cumulative Draft change is based on unchanged V156 `4437e3686c413b80b54a9c56b32654814e0ba71a`, whose Foundation Windows/Ubuntu and original Mojang Minecraft 1.8.9 GUI/dedicated-server run were **SUCCESS**. The original V156 rerun on the same immutable SHA was requested independently to measure intermittent acceptance reliability. Earlier V154/V155 single-run failures are documented separately, not hidden or implicitly fixed: V154 M405 original client exited code 255 in a three-client UUID phase, and V155 original Scaffold context reported its native player not grounded. V156's later PASS cannot explain their root causes.

## Native source production hardening

V156 already bound `Minecraft189HostRuntime` KeepSprint restoration to an admitted native source player and captured original Require Forward / Require Player Hit / Pause Sneak policy with one-use callback ownership, rather than restoring to whichever local player happened to be current.

V157 closes two remaining authority gaps:

1. **Lifecycle mismatch** — the KeepSprint module now exposes an internal monotonically advancing `lifecycleEpoch()` incremented at every real `onEnable/onDisable`. Source click admission captures this epoch, and original post-click sprint restoration rejects any other epoch, even if a disable→enable sequence ends with the same settings and enabled state. This prevents a prior click from surviving a different registered active lifetime.
2. **Reentrant source getters during admission** — the host increments its internal approval serial for each pre-click source verification and on source player/tick changes. If an actual source getter triggers a nested `shouldKeepSprintAfterSyntheticClick`, an outer frame with an obsolete serial is refused and any nested pending grant revoked. If the module is toggled during the same source snapshot read, the changed lifecycle epoch also refuses the frame. No synthetic gameplay data, source field write, extra click or packet is introduced.

The native original source player's sprint bit is still only written by the existing mapped `customMcSetSprinting(true)` after exact pre-click admission and revalidated original GUI/WTap/W/sneak and current live module policy, with the existing one-use lease consumed **before** that setter. Original settings IDs, default values and legitimate fresh-click restore behavior remain unchanged.

## Adversarial regression tests

Three new installed-runtime JUnit tests in `Minecraft189KeepSprintRuntimeLeaseTest` verify:
- A disable→enable with identical persistent policy and original player never inherits an old click; a genuinely fresh original click in the new lifetime works.
- A source sprint getter that synchronously performs nested admission cannot leave an actionable old **or** nested click lease after the outer frame returns; a separate new admission still works.
- A module lifecycle change during source sprint getter verification cannot mint a use permission, even if module returns to enabled before the getter ends.

These are **Java source ownership** tests with mocked original accessors, not proof that real server PvP sprint is preserved. Exact-head Windows/Ubuntu Foundation and pinned full Mojang original binary GUI/dedicated server suites are required. Keep physical M425 vanilla ray/hurt, M405 source UUID and actual dedicated-server damage, original GUI and inventory/rod/potion acceptance untouched. Do not mark V157 native certified unless its own exact SHA fully passes.

## Integration

[PR #617](https://github.com/Trexzo/customMCclient/pull/617) V151 → [#618](https://github.com/Trexzo/customMCclient/pull/618) V152 → [#619](https://github.com/Trexzo/customMCclient/pull/619) V153 → [#620](https://github.com/Trexzo/customMCclient/pull/620) V154 → [#621](https://github.com/Trexzo/customMCclient/pull/621) V155 → [#622](https://github.com/Trexzo/customMCclient/pull/622) V156 → V157 new Draft child. No merges or `main` changes; test failures remain visible and documented in #503 and #537.
