# V176 — native player readback permission lifetime and GUI-policy revocation

## Source audit (original Java classes)
On the V175 stacked development head, three real movement production modules checked enabled/GUI/movement policy **before** an externally implemented native player getter, then performed a setter afterward without checking that permission was still owned.

- NoWeb: original \`customMcInWeb()\` -> \`customMcSetInWeb(false)\`.
- NoFall: original \`customMcFallDistance()\` -> \`customMcSetFallDistance(0)\`.
- NoGravity: original \`customMcMotionY()\` -> \`customMcSetMotionY(next)\`.

The original mapped accessor may synchronously reenter source callbacks that disable/re-enable a module or edit one of its persisted GUI settings. A stale native getter result is **not** authorization to perform a write across such a change. This is a potential stale-permission error identified in source audit; it is **not claimed to have been reproduced in genuine Minecraft**.

## Implementation
Each module increments a native-read lifetime epoch on both enable and disable. Before calling the original read method, it captures the epoch and all relevant GUI-policy values. After the original getter returns, the module requires the same epoch, enabled state and original policy before executing its existing write. On mismatch it leaves the native player unchanged. All existing GUI, actual movement, Ground/Air, Sneak, real original sentinel and motion threshold gates remain untouched.

No new packets, host player objects, synthetic world state, original game mouse actions, native click, damage or false acceptance markers. The default successful source behavior remains unchanged.

## Offline evidence
Nine adversarial JUnit tests in \`Minecraft189NativeReadbackLeaseTest\` cover module OFF->ON during each genuine interface getter, independent NoWeb require-sneaking and NoFall threshold GUI edits, NoGravity lift-speed/physical Space policy edits, a mid-read module disable, and opt-in GUI focus that skips all original getters. Fresh subsequent authorized calls still succeed.

These tests use Java adapter fakes; they prove **only policy logic**. They do not prove original Mojang Minecraft 1.8.9 runtime, physical movement, combat or dedicated-server parity.

## Release requirements
Cumulative Draft child of V175 #641. Require exact V176 head Foundation Windows+Ubuntu and complete pinned original Mojang Minecraft 1.8.9 78/78 GUI/server test, then independent exact-SHA stability rerun where warranted. Keep older failures visible; do not merge or update main from this Draft without an integration decision.
