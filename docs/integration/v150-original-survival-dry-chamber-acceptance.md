# V150 — Original Survival dry-cell acceptance isolation

## Precise upstream / release boundary

Cumulative child of V149 #615, pinned parent `0627bd9ae7e313c8a45ecb0d981f894cb0db9260`. Stays Draft. Do **not** merge onto `main` or certify until the entire pinned Mojang 1.8.9 original-client/dedicated-server binary suite and Foundation Windows/Ubuntu pass on this exact V150 head.

## Observed original-run failure (not an invented source bug)

V148 #614's official binary run **39045179417**, job **117432370406**, failed at the inherited V132 rod/potion original X11 step; the source client still proved actual V145 production Survival healing before failing at V147's two-tier checkpoint. The original Minecraft log shows two `Cannot place blocks outside of the world` messages on the original `/fill -3 ... 3` commands, immediately after clearing earlier potions. That cell crossed negative X/Z chunk boundaries. The runner never checked command acceptance, teleported the genuine player anyway, witnessed `CIProbe drowned`, and finally failed `no genuine original full-health Survival and both source healing tiers checkpoint before bounded deadline`. No evidence from that failure establishes a broken sword selector.

## Corrective test-environment change

Only `scripts/RunMojang189SingleplayerSmoke.py` changes in the runner. Construct a **closed** 9x9x9 stone shell with 7x7x7 air interior, entirely within Minecraft source chunk (0,0):

- `/fill 4 79 4 12 87 12 stone`
- `/fill 5 80 5 11 86 11 air`
- `/tp CIProbe 8 80 8` **only after both real server fill replies**

New `send_verified_fill` scans only freshly appended original client log bytes after each dispatched vanilla X11 chat command. Reject observed unloaded-chunk /fill errors, too-large region and no-blocks messages, plus absent new success within a bounded eight-second window. Only an observed `[positive count] blocks filled` acknowledgement lets the subsequent command proceed. The emitted `CUSTOMMC_189_SURVIVAL_DRY_CHAMBER_FILL_PASS=YES` means *vanilla command acknowledgements* for this chamber; it does not itself claim potion healing, sword selection, multiplayer, or server damage.

The existing actual original Survival player health, distinct vanilla ItemPotion NBT/damage metadata, true production `Minecraft.rightClickMouse()` and consumption, physical potion impact, native health-tier selection, original ItemSword/ItemFishingRod checks, FOV/GUI/UUID/Scaffold and dedicated server gates are **unchanged and mandatory**.

## Regression checks

`scripts/test_mojang189_dry_chamber.py` is discovered by the existing early offline Python unittest suite in the official workflow. It checks rejecting unloaded-chunk output despite an old success marker, positive fresh success despite an old failure, immediate error refusal, and coordinates staying inside one chunk with enclosing walls/roof/floor. These are isolated parser/fixture tests; they cannot substitute for the original game.

## Open evidence

- Verify Foundation Linux + Windows and the entire pinned real Mojang X11/binary suite at exact V150 SHA.
- Inspect actual vanilla two-fill acknowledgements, `CUSTOMMC_189_SURVIVAL_DRY_CHAMBER_FILL_PASS=YES`, real two-healing-tier markers, V148 sword and V149 rod markers, and all existing original multiplayer/dedicated checks.
- If a source test fails next, **preserve it** and diagnose the exact JVM assertion; never delete or relax gates to force green.

## V150 first original-run feedback and strict follow-up

The first V150 original run [39046204633](https://github.com/Trexzo/customMCclient/actions/runs/39046204633), job 117435310765, validated all early five fixture-parser tests and reached the genuine Rod/Potion X11 world stage. Its first single-chunk `/fill` **also** received genuine `Cannot place blocks outside of the world`: being geometrically inside chunk (0,0) is insufficient unless the *actual original player causes the integrated server to load that chunk first*. The fail-closed runner correctly refused to proceed (there was no misleading drowning or downstream potion PASS).

Second V150 patch `e36352fb` now enters genuine creative mode, sends vanilla `/tp CIProbe 8 100 8`, requires a **fresh** source `[CHAT] Teleported CIProbe to 8.5, 100.0, 8.5` acknowledgement, waits a real client/server tick and then runs the original two verified `/fill` commands. Finally it returns the same original player to the dry chamber and Survival; actual source health, potion consumption, sword and rod tests remain mandatory. The new Python test also checks actual command ordering and fresh preloading teleport acknowledgement. The earlier test failure is retained as evidence, **not** treated as certification.


## Genuine 1.8.9 acknowledgment wording (third run)

The next original-binary run [39046599129](https://github.com/Trexzo/customMCclient/actions/runs/39046599129) got the real `/tp CIProbe 8 100 8` server confirmation and **actually executed the outer fill**: vanilla emitted `[CHAT] 729 blocks filled`. It then failed at the deliberately strict new success-ack parser because that parser assumed the **newer** `Successfully filled ...` wording. This log explicitly proves the first source fill did **not** fail. The parser is now restricted to a freshly appended actual `[CHAT] [1-9][0-9]* blocks filled` message; rejected/error/no-op responses still fail. Offline fixtures use that exact observed 1.8.9 wording. This only proves the first command was accepted in that earlier run, not the second air fill or subsequent source healing; all remain mandatory for a full PASS.
