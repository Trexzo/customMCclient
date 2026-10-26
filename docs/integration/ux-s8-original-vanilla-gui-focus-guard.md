# UX S8 — original Minecraft vanilla-screen and cursor ownership guard

This Draft stacks on UX S7 PR #820, isolated from combat and main. It performs **read-only** native 1.8.9 focus inspection using pinned mappings:

- `ave.m : Laxu;` — original `currentScreen` (MCP `field_71462_r`)
- `ave.w : Z` — original `inGameHasFocus` (MCP `field_71415_G`)
- `ave.A()Lave;` — already pinned original static Minecraft singleton getter

The guard is evaluated by native ClickGUI presentation and UI input hooks, before showing overlay content or dispatching UI input. Physical held-pointer events still enter via the original render path, but the input hook rejects them when vanilla owns focus. An original vanilla `GuiScreen` always owns focus: it closes the CustomMC overlay, without altering the original screen. When the original game reports `inGameHasFocus=true`, ClickGUI remains keyboard-accessible but cannot dispatch UI mouse movements or mouse presses. The existing HostRuntime always updates physical button/CPS bookkeeping before the UI input hook, so gameplay metrics remain unchanged even when UI forwarding is denied. HostRuntime and RuntimeBridge are not touched by this Draft.

Original Mojang's `runTick` re-grabs gameplay focus automatically when `currentScreen == null` and the user clicks with `inGameHasFocus=false`. Therefore **simply calling `Mouse.setGrabbed(false)` is not a correct solution**. S8 deliberately does not force/ungrab or restore focus. It makes the current editor fail closed rather than risk simultaneous gameplay/editor input. Physical cursor usability still requires a native `GuiScreen`/focus lease design and a genuine X11 accept-test under [issue #821](https://github.com/Trexzo/customMCclient/issues/821).

Unit tests cover native screen precedence, gameplay-focus mouse veto, UI-ready allowance, mock-host unknown mapping, closed state, pinned field names. Foundation Windows/Ubuntu and genuine 1.8.9 binary CI are mandatory on exact SHA. Do not claim that generic original binary pass demonstrates physically moving a cursor. Do not touch V326/V327/V328/V329 combat or native acceptance veto gates; no main merge/release.
