# V270 — original multiplayer ServerData address source ownership

Parent V269 Draft PR #740, immutable `74ac430f78f8969848e6f34204178e0a4922a3c6`.

## Source audit and correction
`Minecraft189HostRuntime.serverAddress` previously read the source-mapped genuine `Minecraft189ServerDataAccess.customMcServerAddress()` with no exact original ServerData owner during the native getter. A nested original callback with a distinct/null ServerData (disconnect, server switch) could return to the stale first getter, which would publish an obsolete multiplayer address. An original getter exception/Error could preserve the previous address.

New typed `Minecraft189ServerAddressHostSourceLease` uses existing `Minecraft189ExactSourceLease`; one getter per admitted original server. Same-reference nested getter does not duplicate reads; distinct/null nested original ServerData irreversibly revokes the outer callback. After the real getter, reject revoked source or null/empty/whitespace-only original address and clear old state. On original RuntimeException/Error, clear and rethrow unchanged; always release lease. Publish the original address using the existing trim-only state normalization; IPv6 text, ordinary hostname and port are unchanged. No connection attempts, packets, multiplayer joins, server selection, GUI, combat decisions, user preference or state redaction changes.

20 deterministic Java 8 JUnit tests cover genuine mapped getter values and count, exact same/different/null source lease, A-B-A reentry, explicit unload, invalid source addresses, original getter RuntimeException/Error, original IPv6 text, successful recovery, and independence of old-source evidence. These offline tests DO NOT prove actual original Mojang runtime.

## Separate V266 native V19 investigation (not a V270 native fix)
V266 second original at [run 39507021922](https://github.com/Trexzo/customMCclient/actions/runs/39507021922), previous job 118932611681, **FAILED** real V19 `World.rayTraceBlocks` UP-face stone-hit criterion. Live original player stayed at x=0.5 y=100 z=0.5 and genuine X11 commands `/time`, `/tp`, `/fill air`, `/fill stone`, `/tp` were issued, but the original UP-face raytrace marker never appeared within 170-second limit. The harness did not waive this result. A single bounded identical-SHA V266 retry was requested to distinguish an intermittent original-world geometry/timing problem from repeatable functionality failure. No claim of root cause or V19 fix is made. V270 only addresses a separately demonstrated source-code hole.

**Strict release gates:** exact SHA Windows+Ubuntu Foundation 10/10 and true pinned Mojang 1.8.9 graphical X11 plus vanilla dedicated-server full 78/78, then an independent same-SHA 78/78. Preserve prior native failures; all PRs Draft; main unchanged.
