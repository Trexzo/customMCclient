# V308 — original sticky UUID phase4 source margin without changing acceptance

V305 Draft PR #776 immutable `a9c8ea5419149f7c7a473d12ec6d18eb440a6478`
first original Mojang Minecraft 1.8.9 GUI/X11+vanilla server 78/78
PASS, but independent [second original attempt 2](https://github.com/Trexzo/customMCclient/actions/runs/39798056714/attempts/2)
job `120454645153` **COMPLETED FAILURE at original sticky UUID V56
after 56 earlier stages**. This is a true original Minecraft crash and
must remain red; V305 is NOT double-native certified.

Original Minecraft 1.8.9 crash report:
`java.lang.IllegalStateException: wrong locked target UUID phase=4`
at `Minecraft189LockedTargetAcceptance.verifyOwnedClick(...:235)`,
from `Minecraft189RuntimeBridge.autoClick(...:2968)`. The
**SAME true source evidence** at rejection showed:
`sameReadA3D=4.27320473283224`,
`sameReadB3D=1.4142135623730951`,
`sameReadNearest=1`, `sameReadRotation=1`,
clicked/locked B UUID both `9c6b2c0b-a653-3300-9829-2617908fae14`.
The native production selector did choose B, and A was outside normal
3.5-block Aura reach, but the stricter test-only
`Evidence.aFarBSelected()` requires original `A > 4.5`.
Thus a genuine B click during the intermediate 3.5 < A <=4.5 interval
was rejected by the strict test witness. We cannot claim the exact
cause of the intermediate distance was teleport scheduling vs physics.

The original sticky test server fixture `actual_server_three_client_locked_fixture`
moves A to `x+5` (actual source can drift more than the 0.5 margin)
and uses fire-and-brief-sleep `send_server_command(a_far)` **without
verifying the actual vanilla server /tp reply**. Rather than relaxing
original `4.5`, this separate cumulative Draft child (V308)
moves ONLY the real source fixture final A target to `x+6`, requires
`original_locked_tp_server_reply(a_far)` (fresh coordinate-specific
vanilla 1.8.9 ACK) before awaiting the exact unchanged original
`LOCK_RELEASE` and `LOCK_HIT_B` and dedicated-server damage.
Other target-switch/fov/UUID tests and all world/controller/ray/packet
rules unchanged. No synthetic target/player, false pass, more clicks,
test retry or relaxation.

Two existing server-fixture expected strings were updated from +5 to +6
ONLY in locked-target tests; four new source-only regressions enforce
positive original far margin, reverse spawn, strict unchanged Java 4.5
source predicate, actual server-ack ordering, retained game/damage gates
and unchanged earlier target-switch fixture.

V308 requires own exact-head Foundation Windows+Ubuntu 10/10 plus
TWO independent completely successful full genuine Mojang original
78/78 GUI/X11+vanilla dedicated server runs. V307 also has separate
real initial rejoin floor fix under CI and is not assumed certified.
V304 immutable double-original PASS stands only on V304 exact SHA.
All Draft, no main change, no merge/release or history rewrite.
