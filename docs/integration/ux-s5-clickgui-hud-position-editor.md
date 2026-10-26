# UX S5 — ClickGUI HUD Layout click-to-place editor

A new **HUD Layout** page previews the native 1.8.9 TargetHUD at the current live framebuffer/UI scale. Left-clicking its scaled screen canvas updates the existing persistent `render.targetHud.x` and `render.targetHud.y` settings; **Reset Position** restores their defaults. The preview box follows native compact and coordinates width/height variants, with in-bounds positions unchanged.

The editor operates only if both native settings exist and expose encoded numeric values. SettingRegistry applies the pair after validating both. Missing settings produce an explanatory message, not fabricated defaults. Input cannot change World, packets, target identity, combat modules or keybinds.

**Important limitation:** Existing native LWJGL bridge exposes mouse button press/release, not cursor-motion forwarding. S5 is a functional **click-to-place editor**, not continuous drag-and-drop. The separate, tested HudDragController branch #811 can be integrated later after actual mouse motion and focus ownership are hooked and original-game verified. No dependency on competing PR #810 is introduced.

Stacked only on UI #814. Demand exact-head Windows/Ubuntu Foundation and genuine original 1.8.9 checks. No merge/release, no combat-branch modification.
