# V178 — FastFall and Glide native descent readback ownership

On the exact V177 stacked source, the real Minecraft 1.8.9 player-motion adapters in \`Minecraft189FastFallModule\` and \`Minecraft189GlideModule\` invoke an external \`customMcMotionY()\` getter after eligibility checks but before advancing delayed descent credit and, eventually, the real \`customMcSetMotionY()\` native player write.

Original player adapter callbacks may synchronously disable/re-enable a module or modify GUI settings. An old source read must not spend a freshly restarted activation or changed live GUI policy. This is a **source code correctness gap**, not an already observed genuine Minecraft crash.

Both modules now increment a per-module epoch on enable and disable; they capture the current native-read lifetime and all relevant delay / fall speed / progressive / ramp step / Sneak settings **before** calling the mapped original getter. After the getter:
- If the module was disabled or restarted, return without advancing the new activation's delay or writing player velocity.
- If current GUI policy differs from the captured policy, discard saved descent callbacks and return without a write.
- When the original source callback and GUI policy remain owned, execute exactly the existing finite motion rules, smoothing, progression and delayed eligibility. No extra writes, no input or source snapshots, no synthetic World motion or packets.

**Eight new adversarial JUnit tests** exercise actual interface getter reentrancy with module OFF->ON, speed/Sneak edits mid-read, activation delay edits mid-read, module disable, fresh valid eligibility after revocation, and GUI-focus hard veto. These are purely offline Java adapter policy tests, **not** original game runtime proof.

This is a **Draft child of V177 #643**; require exact-head Foundation Windows/Ubuntu and full 78/78 Mojang original Minecraft 1.8.9 X11 GUI/dedicated-server CI before merge consideration. V177 world/host fixes remain separately subject to native certification. No merge/main changes.
