# V132 — genuine Mojang 1.8.9 Rod/Potion native hotbar and GUI acceptance

## Scope and base

Draft child of V131 #597 exact head `15f84369282b57950c9c59a1f23bc09a8a4ba275`. V129 #595 is last fully certified ancestor at authoring; V130 and V131 Foundation passed on both Windows and Ubuntu, and their exact-head complete pinned original-client runs were still underway. No claim of a full run succeeding before results exist, no merge to main, and no change/relaxation to prior UUID/FOV/Scaffold/X11 test gates.

## Newly required native proof
One additional **mandatory** official workflow step runs the pinned genuine Mojang 1.8.9 obfuscated client, actual renderer, an X11 window manager, and a fresh creative integrated-server world. Via original vanilla chat and server commands (not Java-created ItemStacks), it grants `fishing_rod` and `potion`. A new opt-in acceptance component observes actual transformed `EntityPlayerSP`, `InventoryPlayer.currentItem/mainInventory`, and genuine `ItemStack` source subclass access.

Only after both actual items are observed in the real hotbar does the acceptance harness configure deliberately *incorrect* fixed slots, leave V131's opt-in auto-discovery enabled, and use the normal enabled production `Minecraft189HostRuntime.selectAutoRodSlot` / `selectAutoPotSlot` and final `mayExecute...Use` methods. Strict assertions require correct real item slot, positive source final-use eligibility, rejecting a foreign token, and original slot restored in finally. It next sends **physical Right Shift** to open/close CustomMC's ClickGUI; genuine GUI focus must veto both leases without currentItem writes. On GUI close a second positive native item selection runs, followed by normal ModuleController disable and original selected-slot consistency.

The fixture does **not** invoke `Minecraft.rightClickMouse`, simulate server particles/damage or report an actual rod projectile / healing potion effect. Native ItemStack selection and final right-click eligibility are stronger than host JUnit, but actual dedicated-server item-use outcome still requires independent proof.

## Release contract
Both Foundation Windows+Ubuntu and the complete original-client/dedicated suite must return SUCCESS **on V132 exact head**, including new X11 source acceptance and all retained historical checks. No weakening/renaming old markers. Optional flag `--native-rod-potion-discovery` in `scripts/RunMojang189SingleplayerSmoke.py` is isolated from other smoke scenarios and guarded by the dedicated JVM property `custommc.acceptance.requireNativeRodPotionDiscovery`. All source fixture methods gated on that property; production defaults are untouched.
