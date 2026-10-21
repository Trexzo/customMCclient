# V277 — original Mojang native subtrial retry transparency

Parent V276 Draft PR #747, immutable `9bc3a344c06a8b96cb48a324887c1a23f605c342`.

## A source-grounded audit issue, not an acceptance bypass
Prior V266 genuine original job `119174629079` had GitHub workflow overall 78/78 SUCCESS, but its **first native AutoClicker trial failed** and the job executed `AUTOCLICKER_REAL_GAME_TRIAL=2` (a fresh real graphical JVM); only the second original trial supplied all original AutoClicker damage and ownership evidence. This is a valid workflow success, but not first-attempt clean behavior. Subsequent manual examination of V271 first/second and V273 first/second full original job logs found that the four designated bounded-retry loops each logged **trial=1**.

## Machine-readable outcome without changing criteria
The four existing explicit native retry loops in `.github/workflows/minecraft-189-real-client.yml` each still execute a **full real Mojang client/server** subtrial, require that subtrial's original external marker/server damage, retry at most once with fresh temporary JVM directories, and retain their exact `test "$accepted" -eq 1` failure gate. No old `CUSTOMMC_OFFICIAL_189_*_PASS=YES` proof is replaced.

**Only after a loop has met its old true original criteria**, print:
- `CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT=<label>:first` for attempt one.
- `CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT=<label>:retried` for attempt two, also an actual GitHub workflow warning.

Labels: `autoclicker`, `m424-remote-pair`, `m423-unlocked-scope`, `m407-upstream-pvp`. These are REPORT markers, explicitly not game PASS markers and not a new certification standard; a green job after a retry remains green, but reviewers can distinguish it from first-try stability. A failed pair of attempts still fails the original step, not a report "success." Zero new named GitHub steps (same 67 explicit named steps and original job's total 78), same existing native targets, 78-step gate, test deadlines, actual X11, dedicated server, and no production game code changed.

Eight offline Python tests confirm all four mandatory gates, step-count preservation, report format, parser ignoring source code echoes, missing/duplicate/unknown signals, and no synthetic marker. Only exact-head actual Mojang 1.8.9 X11/vanilla 78/78 followed by independent complete unchanged SHA 78/78 plus Foundation Windows and Ubuntu can certify the V277 head.

All PRs Draft, main unchanged; issue #503 tracks the underlying intermittent original native failures.
