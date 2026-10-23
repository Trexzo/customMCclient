# V308 — strict original novel nonplayer sentinel identity (2026-10-23)

Parent: independently double-original-certified V307 [PR #781](https://github.com/Trexzo/customMCclient/pull/781) at immutable SHA `2539c81f7f5b856189f0227f27b5b28f4cfc089a`. This Draft child does **not** inherit V307's original 78/78 certificates.

## Evidence of omission

Outside-stack donor [PR #526](https://github.com/Trexzo/customMCclient/pull/526) addressed the original World sentinel identity versus count distinction. The certified V307 source already has `m273BaselineNonplayerUuids` and `m273NovelNativeNonplayerCount`, including diagnostic coverage, but its mandatory M417 positive marker is still guarded by `sourceNonPlayerCount(e) >= nativeNonPlayerBaseline + 4` alone. As a result, the M417 acceptance condition itself did not test novel nonplayer UUID identities. This is a *test-acceptance evidence integrity* correction, not an observed false gameplay attack.

## Minimal fail-closed integration

Introduce `m308StrictOriginalNovelSentinelGate` using exactly the original live World UUID/kind snapshots. Preserve the old **net count +4** prerequisite and additionally require **at least four distinct source-visible nonplayer UUIDs not present in the frozen original baseline**. Reject unavailable/mismatched original source, duplicate new UUIDs and count overflow. The existing original B-only selected and locked UUID checks, actual A-unloaded/rejoined index checks, unchanged genuine vanilla controller/PvP/78 workflow gates, and native timeouts stay untouched. Baseline is still captured before real dedicated-server summons. The M417 marker now prints original observed novel UUID count as independent evidence.

This deliberately does **not** copy the whole divergent V61 donor branch or relax the former net-count rule. New Java offline regressions test authentic/negative identity fixtures, duplicate IDs, stale index order, changing entity totals, zero baseline, invalid sources and integer overflow. Such source tests are **not** proof of original Minecraft behavior.

Certification gate: new exact-head Foundation Ubuntu/Windows 10/10 and **two full separate genuine original Mojang 1.8.9 GUI/X11 + dedicated vanilla-server 78/78 PASS** on this child SHA. Historical red CI results remain red. Keep Draft; no merge, `main` edits or release pending full integration review [#777](https://github.com/Trexzo/customMCclient/issues/777).
