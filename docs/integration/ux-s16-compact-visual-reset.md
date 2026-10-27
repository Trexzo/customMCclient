# UX S16 — Ghost-client visual course correction

**Stack:** S15 live theme sync PR #835. S16 replaces the ineffective equal-width tile aesthetic with restrained Minecraft 1.8.9 HUD presentation. GitHub-only, Draft, no combat tree modifications.

## Implemented in actual renderers
- **ArrayList:** width-sorted enabled module labels (category first only when requested); intrinsic-size translucent backplates; consistent right edge and restrained 2px accent; clip guard; preserve left-aligned absolute-X for saved profiles. Remove the 184px hard-coded equal-width card.
- **ClickGUI:** cap centered window at 738x490 logical px, 158px max sidebar, typography-led CustomMC wordmark instead of app icon, slimmer search and category spacing, 29px module rows and actual state toggles. All clicks, settings, search, keyboard and focus logic still belong to their existing controllers.
- **TargetHUD:** label the source-mapped remote entity as a **NEARBY** index, emphasize grounded distance in meters, and move yaw/pitch to a low-key LOOK value, rather than claiming unavailable player name, health, ping or avatar.
- **Themes:** retained working persisted Nebula/Frost/Mono from S15.

## Deliberate limits
- **Text width is only a conservative glyph-table approximation.** The current Minecraft renderer bridge cannot call native FontRenderer.getStringWidth; do not claim exact font metrics or fully calibrated right alignment. It does correctly distinguish narrow/wide labels and eliminates identical tile widths.
- No fake target HP, avatar, identity, hit animation, PvP-only combat source or fabricated latency. TargetHUD remains strictly source-based nearest remote-player information.
- No animated sorting/toggling in this cut; native timing and render-order review still needed.
- Preview screenshots in chat are not game screenshots. A genuine Minecraft 1.8.9 capture remains a separate visual acceptance gate.

## Verification
- Strict Windows+Ubuntu Foundation CI and official Mojang 1.8.9 binary CI must complete at exact head.
- No edits to RuntimeBridge, HostRuntime, native hooks, networking, UUID, player ownership or combat control.
- Do not merge to main until these, plus physical UI cursor/focus acceptance, are verified.

## Default TargetHUD presentation
The new-install default is compact=true and proximityMeter=true, producing a compact source-grounded two-row nearby-player tracker with a real distance meter rather than the old larger debug-style YAW/PITCH panel. Existing persisted settings remain authoritative and the full diagnostic source angle layout remains user-selectable. A proximity meter is **not target HP**.
