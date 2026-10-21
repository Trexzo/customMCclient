# V288 — why the genuine original nonplayer negative fixture never armed

Immutable parent V287 Draft PR #758 `be1634ac33245081eef9876ae7d5568bf27ddd34`.

## New actual Mojang failure, not a failing JUnit

V286 [original Mojang run #39625775380](https://github.com/Trexzo/customMCclient/actions/runs/39625775380), exact SHA `0198339a1331e54062e4c620823e49097d6a8bb1`, genuine job `119310504470`, **COMPLETED FAILURE** at stage 65 **`Verify real player-only Kill Aura excludes passive native mob`** after 64 successful original steps. Full log proves:
- Original pinned Minecraft 1.8.9 LWJGL GUI launched, original integrated server/world and player callbacks ran.
- Real original /fill commands returned **14** and **41** blocks filled.
- Actual original /tp was acknowledged, and genuine native /summon command returned **`Object successfully summoned`**.
- Nevertheless **no** `CUSTOMMC_189_KILLAURA_DISABLED_NONPLAYER_TICKS_PASS=20` was recorded before the strict bounded deadline. The driver threw `no disabled Kill Aura nonplayer negative control checkpoint before bounded deadline`.
- This evidence does **not** prove the source target selector selected a mob incorrectly: the original 20 valid nonplayer crosshair callbacks needed to enable Aura were not established, so the actual enabled nonplayer veto phase was **never reached**. It could be crosshair miss, ray type, observed World identity, entity/index/kind source misalignment, bad original setup geometry, or runtime reflection. The true cause remains unproven. Startup Realms/OpenAL warnings appeared but the original world and chat command sequence completed; they do not establish a cause.

V283 also experienced a separate true **second-run V29 original Scaffold placement deadline failure**; V282 experienced a true **V46 original FOV unexpected click and yaw transition 0°→−180°**. All real red outcomes stay red under issue #503; later/earlier successful runs never rewrite them.

## Strictly read-only original-ray rejection diagnosis

Within **only** the already opt-in `reportKillAuraNonPlayer` code path, `Minecraft189VanillaMeleeAcceptance.raycastLivingNonPlayer` retains every original return condition, the exact same reflected Minecraft and native World reads, the original identity-index lookup, and the exact same `LIVING`/not-`PLAYER`/not-`LOCAL_PLAYER` kind rule. It now records, for each actual existing evaluation, which outcome occurred:

- Missing input/kind state or unavailable original World source
- Native crosshair hit absent, or actual ray type not entity
- Native entity null; original World null, wrong identity or own player ray
- Native hit not in original World `loadedEntityList`, or index beyond original kind array
- Native kind flags not a living nonplayer
- Exact original living nonplayer accepted
- Original mapped reflection unavailable (preserves the prior null-return behavior)

It also records the same already-observed entity index, raw kind flags, original loaded-entity count and native kind count when available. A small resettable synchronized in-memory `Minecraft189OriginalNonPlayerRaycastTrace` stores total calls and valid observations, last reason and those original values.

The existing `Minecraft189RuntimeBridge` nonplayer acceptance branch prints **bounded** read-only diagnostic observations on its first and every 40th true original callback, including absence of the already-read kind-state snapshot, accepted ray count, current rejection reason and the original strict disabled/enabled consecutive callback counters. It **never** invokes an extra native getter or reflected World query. The original identity/object comparison, strict 20 disabled and 80 enabled *consecutive* sample gates, entity identity retention, hard-fail attack veto, test timeout, genuine original Mojang binary, vanilla server and original 78-stage pipeline remain unchanged. No fake PASS signal and no target or camera mutation.

Twelve Java 8 JUnit regressions exercise reporter reset, unavailable input, non-entity ray, native World mismatch, index-kind length mismatch, raw kind-bit evidence, valid observation counting without spoofing consecutive passes, reflection-unavailable branch recording, invalid diagnostic rejection and absence of original PASS marker. These **only** verify offline diagnostic storage, **not** the native original predicate behavior nor actual Minecraft acceptance.

## Gate for certification

Require unchanged-SHA Foundation **Windows and Ubuntu 10/10** and **two independent complete pinned original Mojang Minecraft 1.8.9 X11 GUI + vanilla dedicated-server 78/78 runs**. This diagnostic patch is not a fix for V286's real negative fixture, V283 Scaffold, or V282 FOV; their red history remains preserved. PR remains Draft, `main` untouched, no merge or release.
