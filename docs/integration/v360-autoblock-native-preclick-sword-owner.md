# V360 — AutoBlock exact original sword owner at rightClickMouse

Minecraft Java 1.8.9 · original native combat; draft stacked after
KeepSprint V359 PR #866. No parallel HUD/ClickGUI/ArrayList edits.

## Source weakness

The original reblock sequence was:

`mayStartAutoBlock → claimReblock → beginNativeUse → original vanilla
rightClickMouse → isUsingItem → recordAutoBlockStart → completeNativeUse`.

Existing V340/V346 had strong **post-click** original HeldItem
generation and token checks, but the original held sword, module policy,
and player source were not re-read at a last **pre-click** boundary.
A synchronous native getter/callback could replace the held sword or
source after reservation, allowing an actual vanilla right click into
the wrong held item. Rejecting `completeNativeUse` afterward is not
sufficient to prevent that native action.

## V360 behavior

- Add `Minecraft189AutoBlockModule.permitsNativeUseDispatch`:
  accepts only this exact nonzero live pending reservation token,
  active module lifecycle generation, unchanged hold/manual policy,
  no existing owned block, no GUI or physical right-click takeover,
  genuine current sword, and the SAME original HeldItem callback serial
  that began native use. **No consume** and no synthetic attack or item
  packet. The separate positive native completion gate is unchanged.
- Host `mayDispatchAutoBlockNativeUse` performs a fresh
  `holdingVerifiedSword()` original getter and verifies the
  held-item generation is unchanged through its two genuine getters.
  A nested original player/source callback or sword replacement
  rejects the old token instead of authorizing rightClickMouse.
- Bridge invokes the host pre-click gate **before**
  `minecraft.customMcRightClickMouse()`. Recheck the original vanilla
  GuiScreen AFTER the fresh sword getter because it may open a UI.
  Exact `finally` token-specific cancellation, real
  `customMcIsUsingItem()` success observation, and original source
  `recordAutoBlockStart()` remain in place.

## Preserved behavior

No manual mouse input interception. Manual right-click continues
to take ownership and is never forcibly released by AutoBlock.
Release-before-Pot, release-before-Rod and release-before-melee
semantics are not modified. CombatCycle's one-primary + one-reblock
policy and vanilla controller, actual currentItem checks,
ClickGUI/original GuiScreen, per-player UUID guards, and authoritative
server damage tests are unchanged.

## Verification
- Module JUnit `Minecraft189AutoBlockPreDispatchTest`: token,
  source serial, GUI/manual-right, changed policy, disable→enable,
  stale token, and real native completion unchanged.
- Integrated JUnit `Minecraft189AutoBlockOriginalHostDispatchTest`:
  original sword getter, real source changes, nested callbacks,
  policy/GUI/module lifecycle veto BEFORE right-click.
- Python source-only regression
  `scripts/test_mojang189_v360_autoblock_native_preclick_owner.py`.

Require exact-head Foundation Windows/Ubuntu and Mojang original-binary
acceptance. Prior V359 Foundation passed on both platforms; original
V47 KillAura A/B rejoin positive still fails. This unrelated legacy
failure is a release blocker and is **not** claimed fixed by V360.
Draft only; do not merge to main.
