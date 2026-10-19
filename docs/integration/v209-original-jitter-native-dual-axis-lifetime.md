# V209 — Jitter's original player yaw and pitch belong to one native setter lifetime

## Exact parent source-gate history
At parent V207 immutable source `f466f463821b55ad39ca719e9374cbb1dcd80a84`, both Ubuntu and Windows Foundation CI PASS and its pinned Mojang 1.8.9 graphical X11+vanilla dedicated-server suite completed full **78/78 PASS**. Parent V208 `8eb7eae87fa69e5a79c4738cac767663de9e1786` passed Foundation on both OSes and its own original suite was in progress at time of this change. V205 and V206 had completed first original 78/78 PASS; independent same-SHA reruns were in progress. None of those older source commits certifies V209.

## Genuine original source flaw
`Minecraft189JitterModule` computes two separate *genuine mapped native player rotation* writes from one eligible source snapshot, potentially using one paired variable-strength RNG sample and an interval timer. It flips its outward/return stroke phase before sending the first `player.customMcSetRotationYaw`, then sends `player.customMcSetRotationPitch`. An original setter can synchronously reenter module onDisable→onEnable, edit persisted Jitter pitch axis/strength, random interval, input permissions or GUI policy. Previously the old native callback still wrote its second source pitch and/or left old paired random stroke state in a newer lifecycle, violating original one-pair ownership. This is source reentrancy analysis, not an observed Mojang crash.

## V209 production source correction
- Maintain monotonically advancing `nativeRotationEpoch` at every Jitter enable/disable, including an intervening OFF→ON during original mapped setter.
- Before calling any real original player yaw/pitch setter, capture exact immutable values of all **13 persisted Jitter permission, amplitude and cadence settings**: Yaw/Pitch Degrees, enabled axes, Interval Ticks, Require Hold, Pause Right Clicking, Ground Only, Pause While Sneaking, Variable Strength, Strength Variation Percent, Random Interval and Interval Variation Ticks.
- After actual native yaw setter, verify module is still enabled in same epoch and every setting still matches. If not, stop before source pitch, reset synthetic outward/return random stroke/cadence and return non-success. A real yaw mutation already applied to vanilla player is never fabricated back. After actual original pitch setter, repeat same check before reporting success. A single enabled yaw-only/pitch-only source axis still works unchanged.
- Preserve established randomized paired-return behavior when unchanged and original genuine input/rotation safety checks. An original setter that throws after partly modifying yaw retains real setter error and never spends a second pitch write.

## Offline regression and gate
16 JUnit adversarial real `Minecraft189PlayerRotationControl` adapter cases cover full positive two-axis and paired variable-strength return, native yaw disabling/restarting Jitter, GUI pitch/strength/random interval/Hold/Right Click/variation edits, original pitch disabling or interval edit, native yaw partial mutation then exception, yaw-only/pitch-only, disabled module no-op. These source Java adapter regressions do not prove real Mojang graphical runtime.

This V209 cumulative Draft child of V208 PR #674 must pass own exact source SHA Foundation Windows+Ubuntu and complete genuine pinned Minecraft 1.8.9 **78-step** X11 graphical+dedicated server original suite, followed by independent same-SHA rerun. No changed native World entity/ray/input/packet, no merge, `main` unchanged.
