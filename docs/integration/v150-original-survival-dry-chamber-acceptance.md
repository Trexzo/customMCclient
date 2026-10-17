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

New `send_verified_fill` scans only freshly appended original client log bytes after each dispatched vanilla X11 chat command. Reject observed unloaded-chunk /fill errors, too-large region and no-blocks messages, plus absent new success within a bounded eight-second window. Only an observed `Successfully filled ` acknowledgement lets the subsequent command proceed. The emitted `CUSTOMMC_189_SURVIVAL_DRY_CHAMBER_FILL_PASS=YES` means *vanilla command acknowledgements* for this chamber; it does not itself claim potion healing, sword selection, multiplayer, or server damage.

The existing actual original Survival player health, distinct vanilla ItemPotion NBT/damage metadata, true production `Minecraft.rightClickMouse()` and consumption, physical potion impact, native health-tier selection, original ItemSword/ItemFishingRod checks, FOV/GUI/UUID/Scaffold and dedicated server gates are **unchanged and mandatory**.

## Regression checks

`scripts/test_mojang189_dry_chamber.py` is discovered by the existing early offline Python unittest suite in the official workflow. It checks rejecting unloaded-chunk output despite an old success marker, positive fresh success despite an old failure, immediate error refusal, and coordinates staying inside one chunk with enclosing walls/roof/floor. These are isolated parser/fixture tests; they cannot substitute for the original game.

## Open evidence

- Verify Foundation Linux + Windows and the entire pinned real Mojang X11/binary suite at exact V150 SHA.
- Inspect actual vanilla two-fill acknowledgements, `CUSTOMMC_189_SURVIVAL_DRY_CHAMBER_FILL_PASS=YES`, real two-healing-tier markers, V148 sword and V149 rod markers, and all existing original multiplayer/dedicated checks.
- If a source test fails next, **preserve it** and diagnose the exact JVM assertion; never delete or relax gates to force green.
