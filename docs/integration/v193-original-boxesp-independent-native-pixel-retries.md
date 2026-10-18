# V193 — original Minecraft remote Box ESP framebuffer pixel evidence must outlive first HUD draw

## Reproduced genuine-original failure
V190 exact SHA `796a728f2bab86306eb674ecaf8cb11ad3dec28a`, original Mojang Minecraft 1.8.9 binary [run #39133559422](https://github.com/Trexzo/customMCclient/actions/runs/39133559422), FAILED at authentic V9 remote player Box ESP proof. The original dedicated server spawned *real* CIHost and CITarget with correct source offline UUID; issued real Creative/slowness and accepted original player teleports. Original CIHost Java logged:
- `CUSTOMMC_OFFICIAL_189_OPENGL_FRAME_PASS=YES`
- `CUSTOMMC_OFFICIAL_189_REMOTE_PLAYER_KIND_20_TICKS_PASS=YES`
- `CUSTOMMC_OFFICIAL_189_REAL_REMOTE_PLAYER_BOX_HUD_DRAW_PASS=YES uuid=e3542dd1-56f3-3ab4-82cb-2c015df72d8d`
But the harness correctly timed out with `missing actual original OpenGL framebuffer pixel from exact remote AABB marker within deadline` and exit 1. A native box draw callback is **not** pixel evidence; this run stays FAIL. The original V188 pinned binary on a separate immutable SHA had previously passed a complete 78-step suite.

## Identified code-level blocker
`Minecraft189PlayerBoxEspModule.BoxPass.render` used `!acceptanceRemoteBoxReported` as the gate not only for logging the first source-native UUID/AABB HUD draw, but ALSO for selecting the `glReadPixels` sample candidate. Once HUD draw was reported, later legitimate original frames could no longer attempt pixel sampling, **even when `acceptancePixelReported` remained false**. Thus a single early framebuffer pixel miss could never recover. This is a definite source-code bug independently of whether the first missed pixel was caused by framebuffer timing or a transient projection; the CI output doesn't prove any one such renderer timing cause.

## Scoped V193 correction
- Separate **one-time HUD-draw acceptance** from **repeated actual framebuffer pixel acceptance**. Continue to source-project the same genuine remote player's current UUID-matched AABB on each original rendered HUD frame, and select a new sample whenever the pixel was **not yet confirmed** and the original acceptance test opt-in switch is active.
- Require exact **unchanged source native UUID**, valid finite projected native AABB width/height, original framebuffer coordinate transform, actual `GL11.glReadPixels` from the current native render target, and the existing strict opaque cyan foreground `(112,217,255)` tolerance (±12 RGB). No fake or cached screenshot, no guessed target/rect, and no lowered brightness threshold.
- On the first genuine accepted foreground pixel, emit the original exact UUID native pixel PASS and stop further probes. HUD draw PASS still emits only once. Normal gameplay without `requireRealRemoteBoxPixel` NEVER samples GL.
- This does not alter real Box ESP rectangle source, appearance, projected player selection, gameplay FPS policy, or any original vanilla FOV/ray/UUID/controller/server scoreboard requirements. The genuine original graphical test still has to pass; a helper regression alone proves nothing about native OpenGL.

## Regression evidence
Five additional Java JUnit tests appended to the existing native `Minecraft189PlayerBoxPixelProbeTest`: first HUD PASS does not consume independent pixel opportunity; positive pixel disables further sampling; wrong/null original remote UUID never arms probe; acceptance switch off never reads pixels in normal gameplay; nonfinite/small AABB values fail closed. Existing exact GL coordinate and strict cyan tests retained.

Cumulative Draft child V192 #658, inheriting V191 Timer source 1x restore and earlier Fullbright/NoBobbing source native settings transaction fixes. Require exact V193 Foundation Windows+Ubuntu and full original Minecraft 1.8.9 native X11+dedicated server suite 78-step success plus independent same-SHA rerun before any merge. Failed historical V190 run is not retroactively certified. Keep main unchanged.
