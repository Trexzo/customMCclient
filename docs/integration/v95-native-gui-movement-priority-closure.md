# V95 — native ClickGUI input ownership and movement arbitration

## Why V95 exists

V84–V92 added explicit CustomMC ClickGUI focus gates to Flight, AutoSneak,
Glide, AirJump, ReverseStep, FastFall, NoGravity, Step and NoClip. However,
Minecraft189HostRuntime still dispatched several lower-priority keyboard-driven
movement and jump policies using the same held W/A/S/D/Space state **without**
the CustomMC GUI-focus veto. A previous Flight fix alone therefore could not
guarantee that all relevant movement policies left a source player untouched.

V95 addresses that gap at the existing production host boundary. The live
source of this ownership decision is strictly
`clickGuiRuntime.coreRuntime().model().snapshot().open()`. It is **not** a
blanket assertion about vanilla inventory, native pause menu, OS focus or
external overlay state.

## Matrix

| Native policy | V95 behavior while CustomMC ClickGUI owns focus |
| --- | --- |
| LongJump motion | Existing pending boost is consumed, no source X/Z write |
| HighJump, LowHop motion | Existing one-shot boost/adjustment consumed, no source Y write |
| BunnyHop motion | Existing movement suspended, no source X/Z write |
| Ground MovementSpeed | Existing suspended gate rejects source X/Z edits |
| AirSpeed | Existing suspended gate rejects source X/Z edits |
| TargetStrafe | Existing suspended gate rejects source X/Z edits |
| Strafe | Existing suspended gate rejects source X/Z edits |
| DamageBoost | Observes real hurt-time edge but consumes it as suspended, no synthetic XYZ edit or post-GUI replay |
| HighJump / LowHop / LongJump jump | Physical held Space observed, but actual source jump call suppressed while focused; new post-GUI press required |
| BunnyHop jump | Grounded delay and armed lease explicitly revoked even if focus began while airborne; must see a fresh source airborne-to-ground transition |
| AutoJump, AirJump | Existing V84–V92 focus gates remain in effect |
| Flight, NoGravity, FastFall, Glide, ReverseStep | Existing explicit focus gates remain in effect |
| Step, NoClip | Existing explicit persistent native-field restoration remains in effect |

Freeze is an independent explicit enabled-state policy, not keyboard-derived
movement, and still retains its previously defined native motion authority.
These changes do not claim that **all** client gameplay is globally suspended
whenever any kind of Minecraft UI is open.

## Verification

Focused JUnit host-runtime regressions cover:

1. Original CustomMC GUI toggle while W is held: MovementSpeed, AirSpeed
   and Strafe must leave source X/Z untouched, then resume when the GUI closes.
2. BunnyHop: opening GUI clears an armed jump. Closing while still on ground
   cannot trigger it; genuine new airborne-to-ground transition is necessary.
3. DamageBoost: mapped hurt time 8→10 during GUI focus is consumed, not
   buffered into a later in-world boost; a truly new hurt edge after close
   remains eligible.
4. HighJump, LowHop and LongJump: a Space press owned by the GUI cannot
   produce an original player jump on menu open or close, but a genuine
   release-then-press afterward can.

These are original *host dispatch* tests using Java player-control doubles.
They **do not replace** the official pinned Mojang 1.8.9 binary acceptance.
V94's genuinely source-native Step field X11 Right Shift fixture remains
mandatory. V95 places that exact fixture immediately after basic original
singleplayer smoke instead of at the end of all preexisting PvP and Scaffold
tests, so a failing GUI integration gets earlier diagnosis without skipping
or weakening later historical original-client negative requirements.

## Certification policy

A green Foundation run proves Java compilation and the covered JUnit
contracts, not full native gameplay. The full official 1.8.9 workflow must
pass at the exact V95 commit to mark the release branch eligible for review.
If a historical test fails, preserve its assertion and attach its actual
step, native-client logs, and run attempt to the tracking issue. No automatic
merge to main, no duplicate merge of V84–V95 stacked PRs.

Base: V94 #560 → V93 #559 → V83 #549. Current PR: V95 #561.
