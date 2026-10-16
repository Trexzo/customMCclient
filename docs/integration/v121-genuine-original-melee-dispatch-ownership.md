# V121 — source-native melee dispatch ownership at original attack boundary

## Lineage and exact review surface

This cumulative draft is stacked on **fully certified V118 #584**:
- V119 #585: actual Minecraft189HostRuntime and Minecraft189RuntimeBridge melee dispatch gate, with two adversarial host regressions
- V120 #586: physical X11 original Mojang `InventoryPlayer.currentItem` acceptance extended to the *same dispatch gate*, with a distinct required source-native marker
- V121: this integration branch and evidence/rollback document

GitHub `V118 → V120` comparison: **five commits strictly ahead, zero behind**, touching five actual production, JUnit, native-acceptance and Python harness files before this document. Avoid merging V119/V120 individually and also V121. All PRs remain Draft, `main` untouched until separate reviewed authorization.

**Certified rollback source** V118 #584, commit `b35908545eb776824a128fcc3f71f671872ef34c`: Foundation CI on Windows/Ubuntu **SUCCESS** and complete pinned original Mojang 1.8.9 client + dedicated server workflow **SUCCESS**. The cumulative current V121 head needs **its own** successful full checks; do not confuse the V118 result with validation of newly added dispatch gating.

## Actual production source change

Previously: the exclusive Rod/Pot/melee original hotbar owner could correctly deny a nested `CombatSlot` or `AutoWeapon` **slot switch** and return -1, yet the original RuntimeBridge continued to dispatch `Minecraft.clickMouse` or native original `PlayerControllerMP.attackEntity` anyway. This permitted an action to use a different module's still-selected temporary rod, potion or weapon.

Now:
- `Minecraft189HostRuntime.selectCombatSlotBeforeSyntheticClick` captures the **exact native selected InventoryPlayer.currentItem slot** at successful module selection, plus the original local-player identity, inventory identity, owner module and previous-slot token.
- `mayDispatchSyntheticMelee(originalSlot)` is the final fail-closed production authorization immediately before either original-game melee dispatch path. While GUI owns focus, return false.
- A **positive** original-slot token must match the active captured owner and its native currentItem; player and inventory references must be unchanged and owning module still enabled. A legitimate user hotbar switch or respawn invalidates authorization.
- A **negative/no-switch token** preserves normal vanilla click semantics only when **no source combat module owns or is still selecting** a temporary original currentItem. An action denied while Rod, Potion, or melee already holds the source slot therefore cannot click with someone else's temporary item.
- `Minecraft189RuntimeBridge` gates the actual `customMcClickMouse` / source `customMcAttackCrosshairEntity` calls, not merely a local UI state flag. An early veto still executes the original hotbar-owner `finally`; no invented packets, synthetic server damage, target UUID bypass or extra clicks.
- Existing PvP target identity and UUID restrictions remain in their original source gates; this change does **not** claim to expand reach, spoof remote players, or infer server approval.

## Adversarial production-host JUnit regressions

`Minecraft189CombatItemUseBoundaryTest` now covers:

1. Valid Rod original selected slot obtained. Nested melee select returns -1, **and its melee dispatch permit must also return false** while Rod owns currentItem. A wrong positive token cannot authorize a foreign melee dispatch. The true Rod finally restores original currentItem, after which normal vanilla no-switch melee permit resumes.
2. Proper CombatSlot selection permits source melee dispatch. A user independently switches the real selected slot during the action: original selected-slot ownership is lost, so the attack permit is false; original user's new slot is preserved by finally.
3. Proper CombatSlot selection, then physical CustomMC GUI toggle: dispatch must be false while menu has focus even though module is still enabled.
4. In-flight CombatSlot lease followed by host callback publishing a **different local player and inventory**: dispatch denied, old captured source inventory restored, and the new player's currentItem untouched.

These are Java host source-interface doubles with actual CustomMC GUI model toggles, not a claim of native server damage, fishing cast, potion heal, or in-game respawn during a callback.

## Genuine original Mojang local-player X11 acceptance

V120 extends the existing actual transformed Mojang local-player `InventoryPlayer.currentItem` proof, not an invented player or simulated GUI key:

- Physical X11 Right Shift opens actual CustomMC ClickGUI: a denied no-switch CombatSlot cannot pass `mayDispatchSyntheticMelee(-1)`.
- Physical Right Shift closes menu. Normal ModuleController selects a *genuinely different native currentItem*: correct captured prior-token must **permit** actual source-gate authorization.
- A deliberately wrong-but-valid positive token is passed to the production restore callback. The real selected slot must remain unchanged, nested source selection must be denied, and the foreign token must not authorize source dispatch.
- A nested `restore(-1)` must likewise preserve real selected source slot and the original owner, while `mayDispatchSyntheticMelee(-1)` remains denied.
- The correct original owner token still permits a dispatch, then restores actual Mojang selected currentItem through the production finally; the module is disabled normally.
- New independent mandatory `CUSTOMMC_OFFICIAL_189_COMBATSLOT_NATIVE_MELEE_DISPATCH_OWNER_PASS=YES` is awaited by the original-client Python harness in addition to V108/V117 preexisting physical X11 markers. **No attack is actually sent by this fixture**; only the production dispatch decision and real original source-state ownership are proved.

## Release / merge gate

1. Exact V121 head: Foundation CI full Java, native preflight, launcher tests on **Windows and Ubuntu** SUCCESS.
2. Exact V121 head: complete pinned Mojang Minecraft 1.8.9 binary graphical/dedicated-server workflow SUCCESS, including physical X11 Step, NoClip, Reach and CombatSlot (now with melee-dispatch positive/negative marker) and all original real UUID, PvP, FOV, Scaffold and world negative tests unchanged.
3. Do not mislabel the targeted original Mojang fixture's green step as a complete historical workflow PASS. A historical rejoin UUID marker has failed on an older V113 exact head on two attempts, despite successful later aggregate runs; preserve the negative requirements rather than weakening tests.
4. Source-host tests do not replace a Windows local graphics/runtime acceptance on the user's own system, a genuine original in-world respawn test, or server-authorized potion/heal/rod or target-hit proof.
5. Keep V118 as rollback anchor and **all PRs Draft**. No duplicate child-plus-aggregate merges; `main` unchanged pending explicit user approval.

This record contains observed ancestor evidence and a release checklist. It is **not** a claim that V121's newly started full official CI has already completed.
