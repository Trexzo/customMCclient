# V269 – original AutoBlock sword type source pinning

**Parent** V268 corrected Draft PR #739, exact head `ff10fbea9d9878a86f43f0d1d99267a13669d7fa`.

## Source bug
Even after V268 atomically validates original `playerHeldItem`, `Minecraft189HostRuntime.holdingVerifiedSword()` separately calls real `player.customMcHeldItem()` and `ItemStack.customMcIsSword()` for AutoBlock start/release/commit. Neither native getter previously required the *same* original local-player identity from start to end. Each native call can synchronously reenter a different/null `playerHeldItem` source or a new `playerPosition` tick; the old outer function could return `true` for an **old player's sword** and authorize a native block or complete an old use token.

## Implementation
Reuse the existing typed `Minecraft189HeldItemHostSourceLease` for the later sword type dispatch, not just for HUD HeldItem reads. Snapshot a monotonic original held-item owner serial before either original getter, check **both** the exact source lease owner and the current published player/serial after each. A nested different/null `playerHeldItem` irrevocably revokes old call; a new original position tick increments the serial and invalidates even A-B-A source reappearance. Nested same-player callbacks cannot issue a second source read. A stale source never returns sword=true and any actual native getter failure/Error invalidates the old owner and propagates, as already enforced elsewhere.

Only revoke the *captured* source when still current; never destroy an unrelated new player snapshot published while a native getter runs. Genuine original non-sword and valid empty hand both return false, without destroying verified empty-hand inventory owner required by AutoRod/AutoPot. Unchanged original `customMcIsSword()` determines classification, no display-name guess, fake item or new packet. Existing AutoBlock active/target/manual-right/GUI/token settings and native use callbacks unchanged.

18 deterministic JUnit tests cover real verified sword and non-sword/empty-hand, both native getters with same/different/null player reentry, new player-position tick during both getters, native runtime failures/Errors, stale-source recovery, and true empty hand. Source tests never replace pinned native 78/78.

Require exact SHA Windows+Ubuntu Foundation and full official Mojang Minecraft 1.8.9 X11 client/vanilla server 78/78 plus independent unchanged-SHA full second 78/78. All Draft, main untouched, preserve prior real failures.
