# UX S6 — held-mouse HUD positioning in original 1.8.9

## Native path

The original Minecraft HUD frame already calls `Minecraft189RuntimeBridge.renderHudFrame`; when ClickGUI is open, that callback polls the native LWJGL2 mouse **only if created, inside the game window, not grabbed, and the physical left button is held**. It forwards the current pixel mouse coordinates through the existing host and input bridge to a UI-only `MOVE` event converted to logical scaled coordinates. No vanilla Mouse.next() queue reads, clicks, packets, source World writes or combat state changes occur in this path.

The existing `HUD Layout` page treats held `MOVE` as continuous click-to-position within its miniature preview. It writes only the existing persistent TargetHUD X/Y pair with the SettingRegistry's prepare-before-commit semantics. Right button, release, outside preview and closed GUI cannot reposition the card. The behavior is intentionally **not a draggable free-floating overlay**; it is held-pointer movement inside a scaled GUI screen preview. Future full widget drag handles may reuse the independent core HUD drag prototype in PR #811 after #810 API conflict resolution.

## Verification plan and negative evidence

- Core JUnit: held UI pointer movement, release/right/outside denials, no unrelated settings changed
- Native platform JUnit: in-memory LWJGL held pointer source, no coordinates accessed when pointer unavailable/unheld, GUI closed denial
- Native mock-host render: same real installed TargetHUD settings receive changed coordinates and render them
- Genuine 1.8.9 original binary CI: must pass independently for this exact head; source tests do not demonstrate that a physical mouse motion was reproduced inside original Minecraft. Actual manual/X11 movement coverage remains separately required for full UX certification
- Windows/Ubuntu Foundation CI on exact SHA

No change to V326/V327/V328/V329 combat branches, original validation gates, `main`, release, or inventory/attack inputs. Keep Draft.
