# V148 — real source ItemSword durability-aware AutoWeapon

## Ancestry and gameplay intent

Draft cumulative child of V147 #613 exact parent `734cc53b07df3eaf013a7afd6808f19bab1938e3`. No changes to `main`; do not merge overlapping cumulative PRs independently. The existing AutoWeapon module already chooses only source-confirmed vanilla Minecraft 1.8.9 `ItemSword` hotbar candidates using mapped base sword damage, validates exact original Item singleton/ItemStack membership after its native currentItem setter and at final melee dispatch, and restores the original human hotbar in the owner finally. However it could choose a **nearly broken higher-damage sword** over a healthy slightly weaker sword, and among same-damage swords chose the held or first slot even when its remaining durability was nearly gone.

## New persisted Combat GUI options

Three genuine SettingRegistry-backed source settings are introduced:

- `combat.autoWeapon.ignoreFragileSwords` / **Avoid Fragile Swords**, **false by default**.
- `combat.autoWeapon.minRemainingDurability` / **Minimum Durability Left**, integer 1–128, default **8**. Only matters with Avoid Fragile Swords.
- `combat.autoWeapon.preferDurableTies` / **Prefer Durable Sword Ties**, **false by default**.

With Avoid Fragile enabled, candidates must have genuine `ItemSword` source type, finite positive original base damage, positive actual stack count, `ItemStack.getMaxDamage() > 0`, and native `0 <= getItemDamage() < getMaxDamage()`; remaining native uses `getMaxDamage()-getItemDamage()` must be **at least the configured threshold**. Thus a 3-base-damage original diamond sword with only 3 uses remaining can be skipped in favor of a healthy 2-base-damage original iron sword. No fabricated attack power, item name, NBT interpretation, guessed enchanted sharpness or external server damage is used.

With Prefer Durable Sword Ties enabled, still rank by verified vanilla source base damage FIRST. Among equal-base-damage genuine source swords prefer the most remaining native durability. True equal durability favors the original held sword then first original source hotbar slot. With both new options false, preserve exact prior held-first/lowest-slot default.

A candidate with invalid original damage/maxDamage evidence cannot be ranked under either opt-in durability option, rather than guessing. If none is eligible AutoWeapon performs **no automatic weapon selection**, preserving manual player input. This is not permission to bypass the normal existing original human held-sword attack. No packet generation or attack ownership changes.

## Native use lease and adversarial regressions

Capture the opt-in policy booleans and native durability threshold **with** the existing exact source InventoryPlayer, original selected slot, base damage and source Item singleton. The real Java inventory/currentItem setter may run reentrantly and wear/replace the actual ItemStack. Before publishing selection, and immediately before original melee dispatch, re-validate original ItemSword class, positive count, finite unchanged base damage, exact native singleton (when mapped), actual ItemStack array membership, currentItem and captured durability policy; when Avoid Fragile is active re-read actual remaining source durability and reject anything now below threshold. A GUI policy/threshold change during the action rejects the in-flight lease instead of turning stale metadata into an accepted melee attack. Finally respects human hotbar changes and restores the originally owned hotbar only when that original action still owns it.

`Minecraft189AutoWeaponModuleTest` includes original-shaped diamond(7 damage, 3 native uses) vs healthy iron(6 damage), legacy strong choice with feature off, source fragile veto and iron selection with option on, threshold change, zero eligible candidate leaves user slot untouched, equal damage worn-held vs pristine later sword, default held-first and opt-in source durability tie, original setter changing native item durability below threshold, source GUI threshold or boolean changed before final attack, successful later re-admission, and SettingRegistry persisted defaults/updates.

## Verified source damage semantics

The pinned Mojang 1.8.9 original X11 source diagnostic witnessed a real worn diamond ItemSword with `getDamageVsEntity() == 3.0F`, native `getMaxDamage()==1561` and `getItemDamage()==1558`, and a real iron ItemSword with `getDamageVsEntity()==2.0F`, native `getMaxDamage()==250` and `getItemDamage()==10`. **3F and 2F are the per-item sword damage bonuses**, unlike the familiar total melee values 7 and 6 after player base attack 4. The earlier V148 source fixture incorrectly required `7F/6F` from `ItemSword.getDamageVsEntity()` and never reached the mandatory marker despite both genuine `/give` commands succeeding. Fixed original acceptance now requires the **observed exact 3F/2F**, retaining strict item type/owner/count/damage/order/hotbar checks. Never change real gameplay metadata to satisfy a test.

## Mandatory genuine Mojang 1.8.9 X11 acceptance

The pinned full Mojang original graphical/dedicated binary workflow's existing native `--native-rod-potion-discovery` acceptance retains **every** original V132–V147 Rod/Potion, genuine fishing hook physical cast/manual reel, Regeneration I/II and physical splash impact, Survival AutoPot actual heal/consumption, source health-tier ranking, physical GUI and all multiplayer/UUID/Wall/FOV/Scaffold gates.

After the V147 stage, the physical X11 runner issues **genuine vanilla integrated server chat** commands `/clear CIProbe`, physical hotbar key 9, `/give CIProbe diamond_sword 1 1558` (real original 1561-durability diamond ItemSword with 3 uses remaining), and `/give CIProbe iron_sword 1 10`. The Java fixture obtains unchanged same original local EntityPlayer and InventoryPlayer and checks actual native ItemSword subclasses, stack count, damaged source ItemStack max/damage, real mapped original base damage (3 vs 2) and physical sword positions. Through the real `ModuleController` it enables AutoWeapon, proves legacy source selector chooses worn but stronger diamond with option OFF, then proves the 8-remaining-use durability option instead chooses viable iron. Both require native currentItem selection, verified original dispatch permission and exact original user hotbar restore. Marker `CUSTOMMC_OFFICIAL_189_AUTO_WEAPON_REAL_FRAGILE_DIAMOND_VETO_PASS=YES`.

Next physical `/give CIProbe diamond_sword 1 0` grants another genuine pristine original source diamond sword AFTER the worn version. Original default-off tied base-damage choice must pick the earlier worn diamond. Enabling `Prefer Durable Sword Ties` must instead choose the later genuine fresh original diamond with the same real base damage, verify normal original native hotbar/dispatch ownership and restore the human selected slot. Then normal module-controller disable. Marker `CUSTOMMC_OFFICIAL_189_AUTO_WEAPON_REAL_DURABLE_TIE_PASS=YES` and full original runner `CUSTOMMC_OFFICIAL_189_REAL_SWORD_DURABILITY_POLICY_X11_PASS=YES` are mandatory.

These observations prove original-source weapon *choice* and owned hotbar/dispatch *eligibility*, **not a successful sword attack or dedicated-server damage**. The old separate genuine two-player server damage, rejoin, FOV/Wall gates remain mandatory and unchanged.

## Certification and release discipline

Require exact-head Foundation **Windows + Ubuntu both SUCCESS** and **entire pinned Mojang original 1.8.9 graphical/dedicated workflow SUCCESS**, not only native sword stage. At branch creation V145 #611 was fully certified; V146 #612 same-head historical rejoin rerun was in progress, V147 #613 full suite ongoing though new source health-tier X11 step succeeded. Keep this V148 PR Draft, `main` untouched.
