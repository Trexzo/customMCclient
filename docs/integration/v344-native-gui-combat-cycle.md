# V344 — Original Minecraft GuiScreen authority across one combat cycle

Minecraft Java 1.8.9 | stacked on V343 (V342 combined combat implementation)

## What changed

The module arbiter still provides **one action per synthetic callback**:
`QUIESCENT → EVALUATING → POTION | ROD | MELEE_ATTEMPT → optional REBLOCK_ATTEMPT → QUIESCENT`.

A genuine Minecraft GuiScreen is a **veto on the entire automated combat lane**,
independent of the client ClickGUI. `Minecraft189NativeGuiCombatVeto`
reads the existing source-pinned `ave`/unique `axu` field via
`Minecraft189ScaffoldScreenGate` and admits only `CLEAR`; `OPEN`
or `UNAVAILABLE` cancels the action. Non-original Java test doubles retain
their existing synthetic testing semantics and can never certify original GUI
evidence.

A first source check follows all existing harmless read-only acceptance
observations, before AutoBlock release, AutoPot, AutoRod or melee scheduling.
Additional final checks execute:
- Inside AutoPot's existing pitch/slot lease **after** original mapped getters,
  before native right click; a GUI denial cannot commit an invented potion use.
- After Rod's original source/slot eligibility, before native right click;
  no Rod attempt is committed on denial.
- After each of HitSelect's **three** final original UUID/ray/hurt checks and
  immediately before TriggerBot, KillAura or Auto Clicker's actual native
  controller/clickMouse invocation.
- After AutoBlock's post-melee native-use reservation, before reblock right click.

On a denied GUI, HostRuntime invalidates Auto Clicker scheduling, TriggerBot
UUID tickets and admission serial, KillAura scheduling, WTap multi-tick pending
reset and a pending KeepSprint restoration. It preserves the module settings,
physical mouse input, vanilla GuiScreen and already-owned vanilla item state.
The outer `combatCycleToken` still leaves through its original `finally`,
and hotbar restoration stays in its existing `finally` blocks.

## Strict boundaries

- No forcing Escape, no modifying `leftClickCounter`, no bypass of original
  PlayerControllerMP, no fabricated packets, UUIDs, hits or scoreboard damage.
- The prior **ConnectScreen = awz** blocking original Auto Clicker acceptance
  remains unresolved. A correct veto is **not** an accepted PvP attack.
- Existing original GUI forensics V335 and ConnectScreen lifecycle diagnostics
  V336 remain separate Draft work; this change integrates the **behavioral
  veto** from V333/334, not those diagnostic or X11 pre-arm experiments.
- Independent UX PRs, existing source feature PRs, and `main` are untouched.

## Required gates

1. Exact-head Windows + Ubuntu Foundation, including mapped 1.8.9 mock `ave`
   with an actual original-shaped `axu` currentScreen field.
2. Official SHA-pinned Mojang Minecraft 1.8.9 binary + vanilla dedicated server.
3. Genuine controller remote-player UUID, actual original player hurt and
   server-owned scoreboard damage, including simultaneous module transitions.
4. Manual vanilla input and World/player/GUI ownership are never overridden.

Until all gates are satisfied, V344 remains **Draft**. Positive Java/Python
unit or source checks do not certify playable original PvP.
