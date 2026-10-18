# V175 — original Minecraft 1.8.9 M419 real ray hit A, not UUID-locked B

## Actual root cause isolated from pinned original Minecraft output

[V172 pinned original Mojang GUI + dedicated server run 39118291015](https://github.com/Trexzo/customMCclient/actions/runs/39118291015), immutable V172 SHA `320c6856cdb8fd6052a1db4b9d5ecdef769e7076`, **FAILED** late in genuine M419, after 51 earlier successful workflow steps and independently passing the previously flaky original V19 UP-face raytrace. Its new V170 read-only mapped original `Minecraft.objectMouseOver` trace is decisive:

- `phase=4 realPlayerHit=true realHitIndex=6 ... rayIsSurvivorB=false`. The actual ray had a real player hit — **A**, rejoined original native UUID `e3542dd1-56f3-3ab4-82cb-2c015df72d8d`, original source entity index 6 — **not** locked surviving B.
- Simultaneously `nativeTargetAvailable=true nativeTargetIndex=1 BstillLocked=true BcurrentOriginalIndex=1`: the original KillAura selected **B** and kept its real B UUID lock and native target state, but the mapped original raycast physically hit another player before reaching B.
- Real original B source distance ≈3.074, A ≈2.340, `M419_WAITING_NATIVE_CONTROLLER_SOURCE reason=null`. The existing test-only strict source 3D range/UUID/selection predicates were valid. This **does not imply** a real controller was called for B, nor that server B hurt occurred. The official dedicated server finished with `server did not record positive damage for CITargetB`. It correctly **FAILED**.
- Original yaw and desired target yaw both ≈-141.603°, `realPitch=0`; the read-only observer repeatedly reported the genuine player A under the crosshair from real game source snapshots, not a missing crosshair or fabricated hit.
- V170 previously failed a different genuine condition `da+.12<db` after runtime displacement left an A-B gap≈.0985; V159 previously failed lower `da>=1` with a one-block A teleport. All these are separate and must remain documented.

Thus there is a fixture-level *line-of-fire occlusion* problem: **real A can intercept the ray aimed toward real B** even when B's selected UUID and 3D source distance criteria pass. V173's new 1.35-forward position increases nominal distance margin, but leaves rejoined A on the same forward centerline; it cannot reliably prevent obstruction. The actual original ray proves an occlusion occurred on V172; no source analysis guarantees exactly why the player moved into that ray at runtime.

## V175 isolated genuine-server test geometry

Cumulative Draft child of [V174 #640](https://github.com/Trexzo/customMCclient/pull/640) and all earlier source/runtime work. Modify only `actual_server_rejoined_a_controller_fixture` used by the dedicated original server **after the same-UUID original A rejoin**.

- Keep rejoined A's existing **1.35-block forward** *actual decimal-Z* position relative to parsed real CIHost original login. Original B remains **two genuine blocks forward and one sideways** on its existing separate off-axis source player hitbox (not changed, not fabricated). The true Minecraft source World, players and all native entity UUIDs/indexes are still real.
- Add a **0.45-block lateral offset opposite B's true side**, using a **literal decimal X** in the actual vanilla dedicated-server console `tp CITarget` command. If B is one X block positive from host, A is 0.45 X negative; if B is negative, A is 0.45 X positive. Example actual host `tp CIHost 243 5 444 ...` gives host (243.5,5,444.5), B (244.5,5,446.5) and rejoined A (243.05,5,445.85). Negative signed host case gives real A (-33.05,5,-34.85) with B one block negative. All three remain inside the same original loaded 16-block X/Z chunks even for host near the local block boundary. The original `tp` uses decimals intentionally so vanilla does not re-center either coordinate.
- True original vanilla players are 0.6 blocks wide (AABB X halfwidth 0.3). For the **static source command geometry only**, the projected B ray over the entire `A.z±0.3` band has at least ~0.975 block lateral center separation at the near face when A is placed opposite B, greater than its 0.3 halfwidth. This rejects the old *static* shared-line-of-sight pattern. If A subsequently moves onto B's actual ray due to real original physics, the **native ray-B positive and B server damage still FAIL**; we never alter the original ray or pretend static geometry proves a real 3D click.
- Preflight real source 3D A min and A-plus-.12 closer than B bounds now compute from **both true decimal X and Z** (including independent 0.3 original movement-toward-host and 0.9 upward-hop stress margins). If A or B crosses loaded source chunk or the intended B ray geometrically overlaps A's 0.6-wide source hitbox, fixture fails before marking any run positive.
- The actual original post-rejoin **survival support floor already parses FLOAT** XYZ from the returned server console `tp`, uses `math.floor` to construct four stone blocks and issues the exact same genuine A fractional second `tp` after switching actual Minecraft gamemode. No other test scenario positions, original Hitbox/World, item use, GUI, source target selection, controller, timeout or scoreboard changed.

## Source-only regression evidence

Expanded `OriginalM419RejoinedNativeDistanceFixtureTest` in `scripts/test_mojang189_server_spawn.py`:
1. Real parsed positive/negative original server login supplies exact literal fractional X/Z and genuine B opposite-side geometry, preserving same source chunk and B distinct hitbox.
2. Tests project real B's intended X/Z ray through each planned real A hitbox Z band and assert it cannot overlap its original 0.6-wide X hitbox on the **static source fixture**. Source historic V172 `originalHitIndex=6` vs `Bindex=1` is maintained as a **negative**, not a success claim or fabricated native hit.
3. Actual real survival rejoin floor must parse fractional X/Z floats, floor original block cells, positively change four vanilla floor blocks and issue the same source A coordinate before awaiting real B controller and server B damage.
4. Existing V159 low bound and V170 A-to-B true 3D close margin counterfactual negative regressions remain, as do invalid original server spawn/UUID/geometry and dedicated-server scoreboard gates.

These are policy/geometry mocks only; no native objectMouseOver ray can be certified without the real Minecraft GUI.

## Mandatory exact source validation

Full V175 exact SHA Windows and Ubuntu Foundation plus **entire pinned original Mojang 1.8.9 GUI/X11 + dedicated server 78/78** with M419 original real B `objectMouseOver` hit **and** native PlayerControllerMP controller argument exact locked B UUID **and** independently server-damaged B / zero A. Original M405 first lock, V19 true UP ray, V29 Scaffold original ItemBlock/controller and 8→7 stack, M425 physical manual player ray/hurt, Wall/FOV, actual seven-block Aura 20 disabled / 80 enabled + server no-damage, physical rod/potion and click GUI tests remain mandatory. Keep V174 corrected seven-block source eligibility and V173 source-3D bounds unchanged. On first full success rerun identical immutable SHA independently because original World/controller state intermittency is documented.

Last doubly confirmed 78/78 original baseline was immutable V162 `66db7002abadb07207fbdfbf07171311bf494828`. A parent Green Foundation or full-game result never certifies a newer source SHA. All PRs remain cumulative Draft, no merges, `main` untouched; reliability #503 and module parity #537 open.
