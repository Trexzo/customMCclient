# UX S4 — focus-safe keyboard navigation

Stacked on UI Draft #813. When ClickGUI is open and no text, search or keybind editor owns focus, Up/Down cycle between registered pages and Home/End jump to either end. Navigation rejects Ctrl/Alt chords, ignores key-repeat events and does not change gameplay/module/keybind state. Focused editors remain authoritative.

Two deterministic JUnit regressions check page transitions, wrap, focus isolation, modifiers, repeat, closed and empty GUIs. The genuine Minecraft 1.8.9 binary workflow remains separate. No combat, UUID, packet or World changes. Keep Draft; no main update.
