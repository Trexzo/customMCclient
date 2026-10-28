# V358 — WTap ↔ AutoSprint original sprint-writer arbitration

Minecraft Java 1.8.9 · Combat WTap + Movement AutoSprint · draft stacked
on V357 HitSelect PR #864, no `main` merge or parallel visuals edits.

## Original source code failure mode

`Minecraft189HostRuntime.playerSprintControl` already sequences
`WTap.apply(...)` before `AutoSprint.apply(...)` and leases one real
`EntityPlayerSP` source. But the fallback previously depended solely
on **WTap's boolean return**.

WTap supports multi-tick sprint resets. On a later reset tick, genuine
original movement can show `sprinting=false` while WTap still calls
original `setSprinting(false)` to complete the elected reset. That
native setter can synchronously change WTap enable state/policy,
causing WTap to return **false after a real setter write**. The
host may then let AutoSprint see the same earlier `sprinting=false`
snapshot and call `setSprinting(true)` in the same source callback.
This silently undoes the sprint reset and breaks module precedence.

## Source-native state machine

`Minecraft189SprintWriteArbiter` owns exactly one original host callback:

```
IDLE -- first WTap native setter attempt --> W_TAP_OWNS
IDLE -- WTap no-op, AutoSprint native attempt --> AUTO_SPRINT_OWNS
W_TAP_OWNS -- later AutoSprint attempt --> VETO
AUTO_SPRINT_OWNS -- later WTap attempt --> VETO
any state -- original callback finally --> REVOKED
REVOKED -- stale wrapper or old generation --> VETO
```

The owner claim is made **before** delegating to the original
`Minecraft189SprintHostSourceLease.guardedPlayer`. A real native
setter that throws or reenters does not open a second sprint writer.
An actual WTap no-op preserves AutoSprint's existing priority and
movement policy. Host retains actual GUI focus, same original player,
original sprint bit, physical LMB edge, and WTap's timing settings.

Additionally, the existing original sprint source lease now stamps
each guarded control with its own **generation**. A wrapper from a
previous callback is never reauthorized just because a later callback
happens to use the same player reference. This is a source-identity
hardening, not a synthetic sprint setter or fake native state.

KeepSprint remains separately tied to the exact original synthetic
attack owner, native sprint flag, and prior lifecycle checks. Its
existing `wTapOwnsSprint` conflict veto remains intact; this PR does
**not** infer actual server damage, rewrite click ownership, or
intercept any manual vanilla input.

## Verification

- JUnit `Minecraft189SprintWriteArbiterTest` covers WTap precedence,
  native reentry, AutoSprint no-op fallthrough, stale wrappers, and
  native setter failures.
- Existing `Minecraft189SprintHostSourceLeaseTest` gets three
  native integration regressions: old callback wrapper revocation,
  WTap reset OFF→ON reentry with AutoSprint concurrently enabled, and
  AutoSprint still writing when WTap does not.
- Source contract:
  `scripts/test_mojang189_v358_wtap_sprint_writer.py`.
- Required: exact-head Windows + Ubuntu Foundation CI; then actual
  Mojang 1.8.9 original client/native acceptance, including the
  already-failing independent V47 rejoin B→A UUID/vanilla
  controller/dedicated-server damage gate.

Successful offline tests **cannot** certify the original Minecraft
binary. All safety gates and failed evidence must remain visible.
