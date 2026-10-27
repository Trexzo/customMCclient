# V346 — AutoBlock native use lease cleanup, exact token only

**Minecraft Java 1.8.9 · Draft, stacked on V345 (V344 + V343 + V342).**

## Why this is needed

In the combined V344 state machine, AutoBlock reserves an original
HeldItem-source-bound token **after melee** but before original
`rightClickMouse`. An original GuiScreen may open during a mapped getter
between these two stages; `vetoOriginalScreenBeforeNativeCombat` correctly
cancels the synthetic input. Before V346, the reserved AutoBlock pending token
could remain outstanding until another reservation replaced it or the feature
was disabled. A native callback that threw or a right-click that refused to
start using an item had the same orphaned-ticket risk.

## Fix

- Add `Minecraft189AutoBlockModule.cancelPendingNativeUse(token)`.
  Clear only the exact outstanding token and its source serial / native use
  policy snapshot; a stale token cannot revoke a reentrant newer ticket.
- Add `Minecraft189HostRuntime.cancelAutoBlockNativeUse(token)`.
  It never sends a game action and does not demand an open runtime to clear
  already-owned bookkeeping during exception cleanup.
- Wrap **the precise post-melee sword-use attempt** in `try/finally`.
  On a vanilla GUI veto, original right-click failure or thrown native
  exception, the pending ticket is abandoned. If an original vanilla block
  was legitimately completed, the pending token has already been consumed,
  so cleanup cannot revoke a successfully owned block.
- Preserve physical mouse ownership, actual vanilla `stopUsingItem` source
  handling, native slot restoration, `MELEE_ATTEMPT` ownership and exact
  original HeldItem serial verification from V340.1.

## Proof limits

Four new Java regressions cover cancellation, stale token, success/manual
ownership, and lifecycle replacement. Python checks the actual bridge source
order and unchanged native UUID/hurt/server-score gates. Neither source tests
nor mock calls certify actual Mojang PvP damage.

Release only after exact-head Windows+Ubuntu Foundation and genuine SHA-pinned
original Minecraft 1.8.9 plus vanilla dedicated-server acceptance, including
the pre-existing GUI/ConnectScreen blocker. No main or UI branch changes.
