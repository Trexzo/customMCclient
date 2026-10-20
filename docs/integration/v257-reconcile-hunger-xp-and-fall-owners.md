# V257 — reconcile sibling V256 native source-ownership branches

Base: Draft PR #727, original NoFall→Criticals fall owner, exact parent `64f42af5f3f79ce0cfba99a7be2a32ad432a0c30`.
Integrated: Draft PR #726, original Hunger and XP owners, exact source `cf6192d02b276a8ed6ff2114d965a0dcb61dd602`.
Their common parent is V255 `c1b84a039fdc0e98d6bc5feeb7ffa74d8d15d378`.

## Scope
- Preserve PR #727 NoFall/Criticals source owner, all its production behavior and 31 existing tests.
- Include PR #726's original hunger and experience source owner classes, 42 JUnit adversarial tests and its documentation unchanged.
- Integrate only the independent Hunger and XP fields and getter methods into the shared HostRuntime file; leave #727's fall method unchanged.
- No native mappings, multiplayer acceptance expectations, Combat/Render modules or user settings changed.

## Verification
Require fresh exact-head Windows and Ubuntu Foundation, real pinned Mojang Minecraft 1.8.9 X11 GUI with vanilla server official 78/78, plus an independent unchanged-SHA second full original pass. Do not inherit Foundation or real-game passes from either parent. PR #726's original multiplayer scoreboard mismatch is retained as a genuine CI failure; original FOV/UUID flakiness is not claimed fixed. This is a cumulative Draft PR; main remains unchanged.
