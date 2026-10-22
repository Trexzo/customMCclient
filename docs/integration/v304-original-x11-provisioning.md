# V304 — original 1.8.9 CI X11 package provisioning isolation

Parent: Draft V303 #774, immutable commit `b5f7e56be44082cc65066a47292ee1996a81f5f6`.
Scope: **GitHub Actions environment setup only**; Minecraft client, game code,
Scaffold fixtures, input, bytecode transformations, and all original positive/
negative acceptance stages are unchanged.

## Observed original CI failure — do not count as gameplay PASS

V303 corrected head `b5f7e56be44082cc65066a47292ee1996a81f5f6`, genuine original workflow
[#39765542121](https://github.com/Trexzo/customMCclient/actions/runs/39765542121),
job `119779945856`, **COMPLETED FAILURE** in original stage
"Run offline singleplayer with genuine player and world Combat snapshots".
The offline `test_mojang189_*.py` suite passed; the original Mojang client was
downloaded, pinned preflight ran and a production runtime overlay was built.
Xvfb/XRandR provisioning completed.

At 10:47:55 UTC the next stage began `apt-get update; apt-get install -y
xdotool openbox`. The Ubuntu mirror fetched slow unrelated recommended packages
(including fonts, Ghostscript) and the **four-minute stage timer expired at
10:52:08 UTC** while downloading `libgs10-common`. There was no native
Minecraft stage result or original V26 cursor evidence in this run.
This is **infrastructure/setup timeout**, not a native gameplay fix.
V302's independent original second stage similarly timed out pre-V19/V26,
without proven identical apt cause on that previous immutable run.

## Narrow workflow correction

- Provision and verify **xvfb-run, xrandr, xdotool, openbox** in one distinct
  X11 setup step **before** the first gameplay-stage timebox.
- Retry transient apt retrieval at most twice; request actual Ubuntu packages
  with `--no-install-recommends --no-upgrade` to avoid unrelated
  recommended-package downloads/upgrades. Setup has its own 12-minute bound
  and still fails if required tools cannot be obtained; no unconditional pass.
- Keep the first native game step's original **4-minute** bound and real
  `--timeout 120 --native-world-camera --native-safe-walk` unchanged.
  The step now checks that input/window tools actually exist; it no longer
  performs package downloads under a native gameplay deadline.
- Later conditional original X11 guard paths are unchanged; the first setup
  supplies their expected dependencies. No skip, continue-on-error, modified
  native 78-stage set, client/server bypass, or gameplay deadline expansion.
- Six source-only regression tests assert correct sequencing and fail-closed
  dependencies. These are **not native Minecraft results**.

## Certification remains strict

This change only makes failures distinguishable between host provisioning and
original gameplay. Its exact-head Foundation Windows 10/10 and Ubuntu 10/10
must pass, then **two separate full original pinned Mojang 1.8.9 GUI/X11 +
vanilla dedicated-server 78/78 PASS jobs on the same V304 SHA** are required
before double-original certification. V303's prior failure, V302's second
timeout, and every older genuine red stay FAILED. All PRs Draft,
`main` untouched, no merge, no release.
