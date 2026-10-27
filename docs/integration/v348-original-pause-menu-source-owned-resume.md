# V348 — Rejoin-only original Minecraft pause-menu recovery

**Draft · Original Minecraft 1.8.9 acceptance harness, not production gameplay**

## Original evidence

Original V345/V346 Mojang JVM smoke runs logged the real source sequence:

```text
CUSTOMMC_189_ACCEPTANCE_SCREEN=awz
CUSTOMMC_189_ACCEPTANCE_SCREEN=(in-game/no GUI)
CUSTOMMC_189_ACCEPTANCE_SCREEN=axp
```

and then failed waiting for a genuine surviving player B owned native attack.
`axp` is the original 1.8.9 pause menu (`GuiIngameMenu`). V344 properly
denies synthetic attacks while this vanilla screen remains open. It must not
be bypassed by fake LMB events, rewriting the 10000 vanilla counter, or
manufacturing a controller success marker.

## Scoped physical-input fixture experiment

When and ONLY when running genuine `rejoin_target` / `rejoin_release`
acceptance, this branch checks the latest *complete original JVM screen
transition* in a bounded 16 MiB **tail** of the host process log.

- `axp`: source-original pause menu confirmed. Find exactly one visible
  Minecraft X11 window belonging to the actual running host process PID.
  Use the existing whitelisted original `send_real_host_key(..., "Escape")`
  XTest keyboard path to send one genuine Escape to that exact game window.
  Afterwards, require a **fresh** original JVM
  `CUSTOMMC_189_ACCEPTANCE_SCREEN=(in-game/no GUI)` written after that key.
  If this proof does not arrive, fail the real test.
- `(in-game/no GUI)`: **do not press Escape**; this would create a menu.
- `awz`, no original observation, malformed/ambiguous X11 ownership:
  no synthetic source-clear or attack is inferred; never press blind Escape.
- A second check follows the later actual `CITarget` graphical JVM rejoin
  (a genuine X11 focus transition can open the host pause menu again).

The first check is after all three genuine original clients join and the
real World has observed a nonlocal player for 20 ticks, before the 3-player
rejoin damage baselines. The second check is after server-side new-player
rejoin proof and before resumed B-versus-A validations.

This change has **no effect on ordinary CustomMC gameplay or non-rejoin
acceptance**. No manual attack, server-hit marker, fake remote UUID,
non-original screen override, packet, client binary patch or foreground
assumption is introduced. The actual Minecraft GUI itself must provide the
new CLEAR evidence. Vanilla raycast, actual PlayerControllerMP remote UUID,
source player hurt, and unmodified dedicated-server scoreboard remain
necessary, independent positive acceptance gates.

## Verification

Five Python regressions test the bounded complete source transition,
already-CLEAR no-Escape, exact same-PID X11 host, ambiguous window rejection,
two correctly scoped rejoin call sites, and unchanged original game/server
success criteria.

Exact-head Windows + Ubuntu Foundation and genuine original Mojang 1.8.9
binary/local dedicated server must pass before this is certified. This
experiment is Draft and does not alter UI, main, or other feature PRs.
