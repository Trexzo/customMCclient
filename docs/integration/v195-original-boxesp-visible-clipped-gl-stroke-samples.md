# V195 — real OpenGL Box ESP pixel proof for partially clipped original native AABBs

## What is proven and what is not
The genuine Mojang Minecraft 1.8.9 original binary V190 CI [#39133559422](https://github.com/Trexzo/customMCclient/actions/runs/39133559422) **FAILED** at real remote player Box ESP framebuffer cyan pixel assertion. Exact source remote player UUID and real HUD draw callback passed; this does NOT prove that any visible framebuffer pixel was the opaque cyan actual original outline. V193 corrects a definite source bug by keeping genuinely reprojected UUID/AABB pixel reads eligible after first HUD draw, but its own exact-head original suite was not yet completed while this follow-up was authored.

V194's actual native FOV GameSettings owner transaction also belongs to a separate exact SHA and must pass independently. V195 does NOT reclassify a previously failed run.

## Additional actual production/certification path defect
`Minecraft189PlayerBoxPixelProbe.sampleTopStroke` only chose one point on the projected remote AABB **top** outline. A physically rendered original remote player's AABB can be partly clipped by the current original native X11 framebuffer. Its top outline then may be outside the display even while a real opaque cyan left, right or bottom outline segment is visible. Single top-point sampling can never provide actual GL pixel evidence in that geometric state, regardless of whether the source HUD drawer correctly drew valid visible strokes.

V195 retains the same genuine source UUID and exact projected AABB but samples from up to four independently visible **ACTUALLY DRAWN** cyan outline rectangles. It computes the clipped intersection of the top, bottom, left and right opaque stroke rectangles with the current original framebuffer. Only a point more than 1px inside actual visible cyan foreground rectangle may be read. The GL lower-left pixel conversion uses the real `UiViewport` scale/framebuffer, and duplicate candidates are removed. There are no fabricated screenshots, synthetic pixel values, guessed players or source AABBs. The unchanged `GL11.glReadPixels` reads the actual current native render target and accepts only the previous opaque cyan RGB (112,217,255) ±12. Even if a geometric candidate exists, **that does not pass anything** without a real sampled matching framebuffer pixel.

If no foreground stroke is genuinely onscreen, the original test remains NOT PASSED and bounded acceptance-only diagnostic reports `no_visible_foreground_stroke` with real UUID/AABB/framebuffer. Normal gameplay without the explicit original acceptance opt-in does not invoke any `glReadPixels`, and successful pixel proof stops further reads. This is not a color-tolerance relaxation or a weakening of genuine remote source identity.

## Regression coverage
Seven new deterministic Java JUnit geometry/policy regressions extend existing original `Minecraft189PlayerBoxPixelProbeTest`: real top/bottom/left/right in-bounds source positions; true native top-edge clipping with three visible independent strokes; original left-edge clipping; fully offscreen AABB produces no candidates; UI scale conversion to true GL lower-left frame coordinates; nonfinite/bad input and small native framebuffer fail closed; candidate existence does not mean cyan pixel success, strict existing color threshold remains.

These are offline source geometry tests; **only a complete pinned original Mojang Minecraft 1.8.9 X11 native graphical + vanilla dedicated-server run and independent repeat at exact V195 SHA** could certify real rendering. Previous main, combat, UUID, FOV/ray, controller and server damage gates remain untouched.

Cumulative Draft child V194 #660, V193 #659. No merge; `main` unchanged.
