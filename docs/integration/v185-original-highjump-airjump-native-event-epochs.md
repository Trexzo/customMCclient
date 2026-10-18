# V185 — original mapped HighJump/AirJump event lifetime

## Source-level correctness scope
The current baseline is cumulative Draft V184 (#650), including Flight XYZ mapped getter/setter lifetime, persistent Step height safe restore, and NoClip original player collision owner. The immutable V181 source previously produced a **genuine complete 78-step original Mojang 1.8.9 binary PASS**, and a same-SHA independent original job was requested. Each later child must independently pass on its own exact commit.

The actual source `Minecraft189HighJumpModule` consumes a physically observed **fresh Space key edge**, invokes original player `customMcJump()`, then records a pending vertical boost; its follow-up source callback reads original `customMcMotionY()` and conditionally writes original motion. The original mapped callbacks may synchronously reenter module enable/disable or edit current user settings. Before V185:
- an old `customMcJump()` invocation could record `boostPending=true` even after module OFF→ON, wrongly assigning the old event to a new activation;
- an old original `customMcMotionY()` read could be used after module restart or a changed Vertical Speed GUI setting;
- the old callback's finally block wrote a pre-jump Space sample over a new activation's real input state, or failed to observe actual Space release mid-jump.

`Minecraft189AirJumpModule` has no pending motion write, but the same finally behavior could clobber a legitimately resampled Space edge after a reentrant original `customMcJump()` and could miss a physical release made inside the callback.

## V185 implementation
- HighJump/AirJump each own a monotonically increasing `nativeJumpEpoch`, incremented at **every** onEnable and onDisable.
- For HighJump: capture event epoch and Require Movement setting before original `jump()`. A reentrant module lifecycle or policy edit prevents that earlier event from leaving `boostPending` for a new owner. In finally, only the **still-current** activation samples the actual current physical Space key. A new owner's onEnable/onDisable source-sampled key is never overwritten by the previous activation.
- For HighJump's pending motion stage: consume pending once, capture epoch and Vertical Speed before actual original Y getter, and confirm the same live module/setting after getter. Only genuinely authorized values may call original `customMcSetMotionY`. A stale callback returns false and cannot apply a forced high-jump velocity to the new activation.
- For AirJump: restore real physical Space edge after original mapped jump only when it is still owned by the same activation. A disabled/restarted module keeps its new input baseline. GUI focus continues consuming held Space without replay.
- Normal ground-only HighJump, airborne-only AirJump, movement gate, immutable snapshots, click-GUI suppression, and existing vertical arbitration remain unchanged. No fake player state, synthetic packets, fabricated jump, or relaxed original native restrictions.

## Regression matrix (offline policy, not runtime proof)
Eleven new JUnit tests using the same mapped Java jump/motion interfaces cover: HighJump native jump callback OFF→ON, disable, Require Movement edit, Y getter OFF→ON, Vertical Speed edit, fresh valid boost once, source Space release inside jump and new press; AirJump original Space release mid-jump, OFF→ON old-space ownership, new Space press after reenable, and real GUI focus suppressing stale presses.

This is a cumulative **Draft child of V184 PR #650** and subject to exact V185 Windows/Ubuntu Foundation + complete original Mojang Minecraft 1.8.9 X11/dedicated-server acceptance and independent same-SHA stability repeat before merge. `main` must stay unchanged.
