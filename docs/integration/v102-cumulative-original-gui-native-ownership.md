# V102 cumulative original Minecraft 1.8.9 ClickGUI ownership integration

## Source ancestry / no duplicate merges

This review lineage is strictly:
- **V83 #549** existing integration base
- **V93 #559** cumulatively includes V84–V92 (nine actual movement and player-field modules; full original-game and Foundation CI **PASS** on exact V93 head)
- **V94 #560** source-native original player Step GUI X11 acceptance
- **V95 #561** full horizontal/vertical and jump lease GUI arbitration; exact head Foundation and full original-game CI **PASS**
- **V96 #562** original player NoClip GUI X11 acceptance
- **V97 #563** NoSlow item-use movement factor input ownership
- **V98 #564** NoWeb and NoFall field-write ownership
- **V99 #565** new opt-in Freeze GUI pause setting; existing hard Freeze is default
- **V100 #566** Velocity original incoming knockback authority during GUI focus
- **V101 #567** Reach and Hitbox native ray-pick ownership
- **V102** this combined review surface, with V101 content as its parent and this integration document only as its own extra commit

The V93 base through V101 tip is a linear **39-commit** ancestry at comparison time, with no behind commits. Do not independently merge the same stacked PRs and then merge V102, as this would duplicate the changes. All PRs stay DRAFT and `main` remains unchanged pending explicit merge authorization.

## Production changes and motivation

| Production path | Behavior and priority |
|---|---|
| Flight / AutoSneak | Preserve source motion/sneak under the actual ClickGUI input owner |
| Glide / FastFall / ReverseStep | Veto GUI-owned vertical writes and revoke stale delayed activation/ground transition credit |
| AirJump / HighJump / LowHop / LongJump | A GUI-owned Space press cannot create an original player jump or be replayed on closing the menu |
| BunnyHop | Revoke armed grounded jump lease and require fresh source airborne-to-ground transition |
| Ground MovementSpeed / AirSpeed / Strafe / TargetStrafe | Do not write original X/Z motion while CustomMC GUI owns keyboard focus |
| NoGravity / DamageBoost | Avoid GUI-owned vertical/hurt momentum writes while retaining owner precedence and consuming stale hurt-time edge |
| Step | Explicitly restore original native `stepHeight` to vanilla 0.6 while menu owns focus |
| NoClip | Restore original local-player `noClip` false baseline only when this module owned forced true, retaining player instance identity |
| NoSlow | Return exact original item-use slowdown multiplier while menu owns focus |
| NoWeb / NoFall | Preserve original source `inWeb` / positive `fallDistance` field during menu callbacks |
| Freeze | New `movement.freeze.pauseWhileClickGui` **false by default**. An opt-in yields writes but not high-priority Freeze ownership (Flight and other lower policies cannot leak through) |
| Velocity | Return exact original incoming knockback delta while GUI is open, including signed zero/nonfinite values; configured percentages resume on focus release |
| Reach + Hitbox | Route four native `EntityRenderer.getMouseOver` pick sites (block distance, extended branch, extended distance, player border) through the same GUI owner; native values dominate during menu focus |

The shared gate is CustomMC's live `clickGuiRuntime.coreRuntime().model().snapshot().open()`, **not** vanilla inventory, pause menu, physical OS foreground status or an arbitrary overlay. The original source hook remains authoritative with no HostRuntime installed.

## Original-game acceptance vs Java host regressions

The following were **directly observed successful in real Mojang 1.8.9 CI jobs**, not inferred from Java doubles:

- V94 physical X11 Right Shift and native original-player Step field 0.6 → 1.8 → 0.6 → 1.8 → 0.6, on V95 #561 full-suite-success run.
- V96 physical X11 Right Shift and native original-player NoClip false → true → false → true → false, targeted **SUCCESS** on V96 #562 official run even while the full workflow was still continuing.
- Full pinned 1.8.9 original binary and historical PvP/UUID/FOV/Scaffold gates **PASS** on V93 #559 and V95 #561 exact heads.

The remaining new V97–V101 behavior has focused JUnit regressions using real CustomMC host controls and mocked native-player interfaces. These are not proof that **every** listed module was exercised with a real transformed Mojang player in a world, nor do original ray-pick overrides imply server-authorized reach or attacks. The full original 1.8.9 workflow must pass on **this exact V102 head** before release certification.

## Explicit checks for V102 review

1. Run Foundation CI on Windows and Ubuntu (full Java tests, setting registration and close/rollback, launcher smoke).
2. Run complete pinned Mojang 1.8.9 official binary workflow, including real X11 Step and NoClip GUI field proofs and every historical negative PvP/FOV/UUID/Scaffold case; do not shorten, skip or weaken a test.
3. Review actual mouse focus/Key.next hook and adapter mappings at the exact source Obfuscated 1.8.9 version and do not label mock-host-only behaviors as fully native-certified.
4. Ensure Freeze's new setting remains default false, is ClickGUI-visible, profile-persisted and unregisters cleanly on feature close.
5. Review Velocity GUI-focus knockback parity carefully: the native incoming after-value has absolute precedence during the UI lease, including the exact bit-pattern of negative zero.
6. Confirm `RaycastHitboxBorder` remains player-only in gameplay focus, native-only in ClickGUI focus, and never changes actual entity/world bounding boxes or server-side authority.
7. Once CI passes, review a single integration strategy. Maintain original V83 and V93 rollback boundaries, no unapproved main merge.

## CI status is time-sensitive

This document records evidence observed when authored and **does not** claim that V102's full original binary suite has already passed. Check GitHub Actions by exact commit SHA before any review/merge decision. Historical flakiness in earlier V84/V86 FOV/UUID original runtime checks was resolved by same-head reruns without changing assertions; root cause remains unknown.
