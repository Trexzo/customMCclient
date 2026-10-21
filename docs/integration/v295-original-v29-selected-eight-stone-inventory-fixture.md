# V295 — genuine V29 original Minecraft Survival selected hotbar stack must be exactly eight

Immutable direct parent V294 Draft PR #765 SHA `99f914c9e8a4310c2ff5841275efde4089f780e7`.

## Decisive new actual original evidence

V293 independent **SECOND** pinned Mojang 1.8.9 GUI/vanilla-server [original #39656668417](https://github.com/Trexzo/customMCclient/actions/runs/39656668417), job `119477910024`, **COMPLETED FAILURE** after 34 full original steps at V29: `no real enabled Scaffold module admitted vanilla click checkpoint before bounded deadline`.

Unlike previous sparse V29 logs, that genuine client source emitted multiple actual original `CUSTOMMC_OFFICIAL_189_SCAFFOLD_ARM_WAIT` lines. They show:
- `screen=CLEAR`; the original real GUI is **not open**.
- `source=positive_candidate_already_available`, including original stable player x=1.05, y=100, z=0.5, genuine native eye/reach, ground true, real World cells and source ray hit.
- **`stack=1`** — the original **selected** native ItemBlock stack had one block, not the eight required by the unchanged `Minecraft189RuntimeBridge.originalScaffoldAcceptanceMayArm` predicate.
- No genuine `SCAFFOLD_MODULE_OWNER_PASS` or `ORIGINAL_CONTROLLER_ATTEMPT` was observed; original module had not been admitted to the source-owned single click.

The original V293 /give command had been **sent** using actual X11 vanilla chat. But an incoming `/give CIProbe stone 8` can coexist with a stale, different selected one-stone stack. The existing code's source readiness predicate quite correctly requires **the selected real hotbar ItemBlock stack to equal exactly eight**, not simply inventory contents containing eight elsewhere. This is direct evidence of a **specific fixture inventory-state precondition failure**, not wrong Scaffold placement or a bad World raycast. The source also proves the actual native candidate and screen were acceptable. V292 independent second run likewise failed V29, without the same retained arm-wait details.

## Targeted correction ONLY to V29's original fixture

In `scripts/RunMojang189SingleplayerSmoke.py` **only for original `native_scaffold_placement` scenario**, after the original verified World/Survival edge and source player ground-stability checks and after checking that no premature module/controller/readback markers appeared:

1. Execute the **real** 1.8.9 `/clear CIProbe` via physical X11 chat. Capture a fresh original-client chat log byte offset before the command and require its exact 1.8.x translated server reply: either `Cleared the inventory of CIProbe, removing N items` with positive integer N, or `Could not clear the inventory of CIProbe, no items to remove` (source-confirmed already empty). An absent, stale, wrong-player, or unknown-command response **fails closed** within the existing bounded 8-second source-reply window. This removes old selected one-block stacks and any other earlier fixture leftovers **only in this isolated acceptance world**.
2. Use a **real X11 1 keypress** only after original vanilla GUI/chat has closed, to select the original vanilla hotbar slot zero. Do not mutate `currentItem`, forge an ItemStack or bypass the selected inventory source getter.
3. Send **the same original** `/give CIProbe stone 8` through actual Minecraft chat and require a **fresh, item-, count- and player-specific** original `Given [Stone] * 8 to CIProbe` source reply. This new chat receipt proves only that the original server ran /give, **not** that the original player selected the correct ItemBlock.
4. Continue with the **UNCHANGED** actual original runTick source `Minecraft189ScaffoldSelectedHotbar.from(...)`, which must observe `selected.sourceStackSize == 8` before ModuleController can enable Scaffold. The original one-shot same-World/source/GUI lease check, native controller invocation, real right-click returned-true requirement, genuine WorldClient placed-stone & consumed-stack receipt, negative disabled control and original hard timeout are not touched. The same single ordinary default-off module activation remains required.

Eleven Python source and pure receipt classifier regressions check genuine [CHAT] exact original 1.8.x clear positive or verified already-empty response; fail-closed wrong actor/zero amount; exact Stone×8 user/quantity (reject one, nine, Dirt, other player, forged no-CHAT); actual clear→real hotbar1→give order in the **original runner**; real edge grounding precedes inventory normalization; unchanged native controller/real readback hard gates; no fake `PASS=YES` or synthetic original client World/player writes.

### No overclaim

The historical second V293 original run is **FAILED**, unchanged; all old V292 second/V283 second V29, V291 V19, V288 initial UUID A, V286 V65 nonplayer, V282 FOV and V276/V277 native UUID failures remain authentic FAILED evidence at #503. The V293 second run establishes that the **selected original stack** was one when required eight; it cannot independently prove whether /give filled a different hotbar slot or another inventory race caused the one-stack. The new fixture deterministically removes that ambiguity but is **NOT validated native** until the exact new binary runs.

V294 parent had Foundation Windows/Ubuntu 10/10 PASS, native first running, and only read-only V29 guard-refusal instrumentation. V295 requires **both exact-head Foundation Windows+Ubuntu 10/10 and two independent genuine full pinned Mojang Minecraft 1.8.9 X11 GUI + vanilla server 78/78** before declaring double-native certification. All PRs Draft, `main` untouched; no merge or release.
