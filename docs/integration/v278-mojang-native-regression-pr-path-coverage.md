# V278 — close original-Mojang CI trigger gaps for test-only changes

Parent V277 Draft PR #748 exact immutable SHA `6bb6e63a9fa7efac47a4a6f308cfec9ee50fc849`.

## Audited source defect
The official 78-step Mojang 1.8.9 CI workflow's *pull_request.paths* includes `Minecraft189*AcceptanceTest.java`, several **specific** regression test class names, and the production original acceptance classes. But new Java 8 source regressions for V273 (original nonplayer UUID identity), V274/V276 (genuine simultaneous B click source failure) and V272 (native sentinel readiness) are named `Minecraft189*Test.java` **without the narrow `AcceptanceTest` suffix**. A future change solely to one of these existing tests is not among the original workflow PR paths. The full native suite could therefore silently not run even while Windows/Ubuntu Foundation goes green. Likewise, new Python original-fixture unit tests beyond individually listed names were uncovered by the filter. A full original platform Java test-tree audit found 241 current Java tests in the native package: 233 matched Minecraft189*Test.java, but EIGHT additional real Legacy*/Lwjgl2* GUI/LWJGL tests did not. The corrected package-only *Test.java rule covers all 241. This is a real CI scheduling coverage gap, not a failed game behavior claim.

## Strict trigger repair
Add only TWO narrow PR path patterns to `.github/workflows/minecraft-189-real-client.yml`:
- `platform-1.8.9/src/test/java/dev/trexzo/custommc/platform/v1_8_9/*Test.java` — all current/future platform-specific native regression source tests, including actual Legacy*/Lwjgl2* original GUI tests.
- `scripts/test_mojang189_*.py` — original client fixture/asset/mapping tests in the existing Python test suite.

This broadens when the **same real unchanged** workflow is triggered on a Draft PR. It neither removes nor modifies its 78 native acceptance steps; changes no original X11/vanilla server requirements; no game code, mappings, controller, geometry, source owner or native events touched. Foundation remains separate. No docs or unrelated root files matched.

Fifteen offline Python tests verify existing V272/V273/V274/V276 regression-only test paths, all eight previously omitted GUI/LWJGL test paths, V270 source test-only, V277 reporting test, original Survival fixture test, hypothetical future relevant Java/Python tests, unchanged actual scripts/workflow triggers and exclusion of unrelated docs. These tests alone do not prove the native suite, so strict CI still requires exact-head Windows+Ubuntu Foundation 10/10 and independent TWO full 78/78 genuine original Mojang 1.8.9 GUI X11 + vanilla dedicated server runs on identical SHA.

Historical V270 four-ArmorStand, V272 two-player, V274 native V29 and V266 V19 failures remain recorded and unwaived; V273 and V275 now double-original 78/78 certified. V276 and V277 had first genuine 78/78 PASS and their second runs were requested. All stacked PRs Draft, `main` untouched; no release.
