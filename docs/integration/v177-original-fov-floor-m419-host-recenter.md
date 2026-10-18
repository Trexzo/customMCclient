# V177 — original FOV world support and M419 phase4 host recenter

## Exact CI failure evidence
- V175 pinned original Minecraft run 39120851108 FAILED late FOV negative: original World source showed one real nonlocal rear player, horizontal separation 2.0 and 180° yaw, but native **3D distance 5.852349955**; `nativeSourceValid=false` and the strict 20 callback gate never armed. Earlier independent real FOV test had PASS. This is a real vertical fixture instability, not evidence to change the 3.5-reach or native FOV veto.
- V176 exact head `07a5a4802872b515c213ef0957f50a0c284d9797` original run 39122093009 FAILED M419 phase4 with real host (-1.20,4,-.09), A (-.06,4,-2.15), B (-1.50,4,-2.50). Real original 3D A 2.349 / B 2.428, gap .079 < mandatory .120. B selection + original UUID lock were true. Host had shifted since its original server spawn-anchored teleport, while real A/B were distinct and alive. The actual game correctly rejected controller permission.
- Earlier V174 M419 failed the independent `da>=1.0` lower bound for a different real A position, which V175's opposite-side real A is intended to address. Neither older failure is marked PASS.

## Genuine Minecraft 1.8.9 fixture corrections
1. **FOV:** derive a 3x5 stone floor (15 true blocks) directly beneath the genuine source host and rear player positions, inside the real server's loaded neighborhood. Before re-teleporting original live players, require a fresh server response to sacrificial air contrast preclear; only here may `No blocks filled` count as an acceptable *preclear* response. Then require a separate new genuine `15 blocks filled` positive stone response. After stone confirmation, send the unchanged original host and target teleports and demand the same 20 disabled + 80 enabled true native FOV veto and actual zero server damage. No real source position, yaw, target selection or native 3.5 reach is modified.
2. **M419:** in phase4 only, after A's second original-server teleport and genuinely acknowledged A survival floor, require a fresh vanilla server `Teleported CIHost to` reply for original login-derived `host_tp`. Source helper validates original A fractional XYZ (1.35 forward, .45 opposite B), actual separate B, source 3D margin, and player identities before issuing. This recenters real CIHost *after* prior authentic PvP, whose knockback/collision displaced it in the exact V176 failed run. It does not mutate Java World positions or bypass UUID/ray/controller/server damage.

Both fixes change **only original dedicated-server smoke harness** and separate Python offline regressions. Existing real local A/B native World source and controller acceptance still decide PASS/FAIL, not static source geometry. Static counterfactuals are explicitly not genuine Mojang execution evidence.

## Regressions and certification
Six Python regression tests cover positive/reverse source chunks, 15-block FOV support, malformed/unsafe fixture rejection, the true 5.852 native FOV failure, true original M419 .079 gap versus source-center counterfactual, preserved native B UUID/server score gates and exact late-phase host recenter command ordering.

Stacked Draft above V176 #642. Exact-head Foundation Windows/Ubuntu and entire genuine Mojang original 1.8.9 78/78 graphical + dedicated-server source gate required. No merge/main changes until independently verified.
