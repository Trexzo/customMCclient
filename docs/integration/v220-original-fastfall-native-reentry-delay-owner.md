# V220 — original FastFall motionY host reentry and delayed-descent ownership

## Concrete source gap
V219's prior `Minecraft189FastFallModule.apply` protected the original motionY getter's lifecycle epoch and GUI policy but did not protect a synchronous *nested host call* while reading or writing genuine `EntityPlayer.motionY`. A mapped original Y getter can run the same FastFall callback again, spending several `activationDelayTicks` credits on one original stack; a mapped setter can do the same before vanilla Y state updates and repeat a write. The previous code also treated a nested GUI, suspended source, grounded event, different original player or missing mapped movement as if the *outer* callback still owned the original motion read.

## Production changes
- Protect both original `customMcMotionY` and `customMcSetMotionY` under a single same-stack dispatch guard. Nested qualifying callbacks may not read original Y, increment delayed descent, or dispatch another original Y setter.
- If nested authority observes a different player, genuine ClickGUI focus, suspension, landing, unavailable movement or opted-in sneak veto, revoke the outer source read, reset pending activation credit and fail closed.
- Keep existing source epoch and all five GUI policy snapshots; source getter OFF→ON/settings edits remain invalid. Only a new original host tick can start a new read.
- Release guard in `finally` even for genuine mapped getter/setter exceptions; never forge a native motion rollback. Preserve progressive ramps, direct default fall speed, sprint-independent onGround, real GUI veto, and early-return priority.

## Offline tests vs native acceptance
20 Java 8/JUnit source-adapter tests cover actual native-shaped read/write callbacks, nested same/different original players, delayed eligibility not double-spent, mid-native GUI/suspension/landing/unavailable movement/sneak pause, OFF→ON and speed edit, getter/setter partial exceptions, progressive ramp, valid rising/fastfall veto, original GUI reset and module disabled.

Offline source-adapter regression PASS does **not** prove genuine Mojang behavior. Admit only the exact head after Windows and Ubuntu Foundation, full pinned 78-step real Mojang 1.8.9 X11 graphical + vanilla dedicated-server run, and independent unchanged-commit repeat. Existing V213/V219 prior runs do not certify V220. All PRs Draft, main unchanged.