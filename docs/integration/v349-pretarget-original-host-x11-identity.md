# V349 — Original host window identity captured before target launch

**Minecraft Java 1.8.9 · Native test-harness correction · Draft only**

## Observed original failure

[V348 official Minecraft #40488809042](https://github.com/Trexzo/customMCclient/actions/runs/40488809042)
**FAILED** with `V348 original host X11 window not unique`. The original
JVM printed `awz → (in-game/no GUI) → axp` and the rejoin harness tried
to select the CIHost Minecraft window by `xdotool search --pid HOST`
**after** launching other Minecraft JVMs.

The existing V320 original host-window admission correctly warns that
vanilla LWJGL2 can omit `_NET_WM_PID`. A PID-filtered X11 search is
not a reliable ownership witness for this game. Nor may we choose a
window by ordering, foreground focus or a plausible title after three
clients exist.

## Source-owned fix

- For **rejoin_target/rejoin_release only**, find the **exactly one visible**
  original Minecraft window in the isolated Xvfb display after the real
  CIHost GLFW/LWJGL game window appears, but **before launching** any
  CITarget or CITargetB JVM. Use the existing independently tested
  `identify_original_host_window_before_target(host)` function.
- Retain this captured X11 window ID through the full three-client and
  same-UUID new-JVM rejoin lifecycle.
- During V348 pause recovery, recheck that this **same captured ID** is
  still visible among potentially multiple real Minecraft windows,
  its title still matches Minecraft, and the original CIHost JVM is alive.
  Do not use `--pid`, blindly take the first/last X11 hit, or select
  whichever game is currently focused.
- The existing source screen `axp` and **fresh native source CLEAR**
  after one physical XTest Escape are still required. No screen evidence
  means no Escape. Never send mouse clicks from this recovery helper.
- Existing UI and production combat code are untouched. Manual/physical
  input remains real X11 input owned by the original game. The dedicated
  vanilla server still decides actual damage.

## Tests and proof limits

Adapt V348 unit regressions for captured original host ID with multiple
later visible Minecraft windows. Four new V349 regressions check initial
single-window capture, ambiguous pretarget refusal, disappeared/misnamed
host refusal and strict unchanged controller UUID/hurt/server-damage
oracles.

Require exact-head Windows and Ubuntu Foundation, official Mojang 1.8.9
binary and vanilla dedicated-server acceptance before advancing to
certification. Historical V348 native FAIL remains visible. Do not
substitute green source/unit tests for actual server-authoritative PvP.
