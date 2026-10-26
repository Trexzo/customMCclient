# UX S9: mouse-free ClickGUI quick search

In original Minecraft 1.8.9 the custom ClickGUI is currently an overlay. Until native cursor capture is resolved under issue #821, users need a keyboard path for module search.

With ClickGUI open and no text/setting/keybind editor focused, Ctrl+F focuses its existing search input. Typing updates the search query; Enter or Escape exits search without closing the GUI. The search text is not discarded when focus changes. The existing search clear affordance remains.

This is a **core ClickGUI-only** change: it does not use world, input packets, combat module commands, player identity or native focus methods. Other keyboard shortcuts and page navigation work as before. The key uses the legacy LWJGL2 keycode 33 (F), already mapped by LegacyInputTranslator to UiKey id `legacy-key-33`. Repeats, alt combinations and focused text/keybind editors are excluded.

Require Windows+Ubuntu Foundation and original Minecraft 1.8.9 binary CI independently. A fully physical Ctrl+F test is separate from generic native preflight.
