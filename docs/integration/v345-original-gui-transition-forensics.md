# V345 — Original Mojang 1.8.9 GuiScreen transition evidence

**Draft, diagnostics-only, stacked on V344 #840.**

## Motivation

Strict Auto Clicker/PvP acceptance previously saw source-original
`Minecraft.currentScreen=awz` (`ConnectScreen`) even when the Minecraft
World/remote UUID was populating. An OPEN screen correctly blocks automated
combat in V344, but a blocked click is not a real controller attack.

The missing evidence is whether original Minecraft transitions from the
connection GUI to normal gameplay, *or* clears it and subsequently reopens it.

## Added observability

- Under existing strictly opt-in original combat acceptance properties only,
  use the existing `reportAcceptanceScreen()` gameTick observer already used
  in `reportLiveWorld` acceptance. Emit class name only when the genuine
  source `ave` GUI field changes. No GUI text or contents are captured.
- On failure of the real two-player smoke test, print the **first five** and
  **last five** original `CUSTOMMC_189_ACCEPTANCE_SCREEN=` source lines from
  a bounded 16 MiB prefix of the real host log. If the bound is reached,
  the report is incomplete, not evidence that a later transition did not
  occur. Do not open symlink source files.
- Preserve existing native original `PlayerControllerMP.attackEntity`
  remote UUID, actual hurt, and server-scoreboard damage gates unchanged.

## Distinguishing outcomes

- `awz` only, no subsequent source-screen transition: the original game's
  connection GUI may still be preventing click admission. Check full native
  failure evidence; bounded logs cannot prove a permanent state.
- `awz → axs → (in-game/no GUI)`: original source observed a transition
  into gameplay; actual combat acceptance is still independently required.
- `awz → CLEAR → awz`: a new connection GUI appeared after a genuine clear;
  investigate original multiplayer lifecycle, not synthetic CPS or click
  counter bypasses.
- No screen lines: source observation absent/incomplete. Never synthesize
  CLEAR from a missing log.

No Escape injection, forced GUI close, movement, packets, click-counter edits,
fabricated hits, modified controller ownership or main/UX changes.

## Release gates

Exact-head Foundation Windows+Ubuntu, official Mojang 1.8.9 binary preflight,
then genuine remote-player native UUID and dedicated-server damage acceptance
for the combined V344 modules. This PR must remain Draft until those are proven.
