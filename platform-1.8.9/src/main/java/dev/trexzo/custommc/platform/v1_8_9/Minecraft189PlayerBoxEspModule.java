package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPass;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.render.RenderStage;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import java.util.List;
import java.util.Objects;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * Genuine source-AABB 2D Box ESP, registered disabled by default.
 * Does not use approximate player-height rectangles or stale tick indexes.
 */
public final class Minecraft189PlayerBoxEspModule implements Module {
    public static final String ID = "render.playerBoxEsp";
    public static final String BORDER_WIDTH = ID + ".borderWidth";
    static final String PASS_ID = "native-player-box-esp";
    private final Minecraft189WorldCameraFrameState state;
    private final RenderPipeline pipeline;
    private final LegacyUiHostCallbacks host;
    private final Setting<Integer> borderWidth = new Setting<Integer>(
            BORDER_WIDTH, Integer.valueOf(2),
            value -> value != null && value >= 1 && value <= 4,
            SettingCodecs.INTEGER);
    private RenderPipeline.Registration registration;
    private boolean acceptanceRemoteBoxReported;
    private boolean acceptancePixelReported;
    private int acceptancePixelFailureSamples;

    public Minecraft189PlayerBoxEspModule(
            final Minecraft189WorldCameraFrameState state,
            final RenderPipeline pipeline, final LegacyUiHostCallbacks host) {
        this.state = Objects.requireNonNull(state, "state");
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
        this.host = Objects.requireNonNull(host, "host");
    }

    @Override public String id() { return ID; }
    public Setting<Integer> borderWidthSetting() { return borderWidth; }
    @Override public synchronized void onEnable() {
        if (registration != null)
            throw new IllegalStateException("native box render pass already enabled");
        registration = pipeline.register(new BoxPass());
    }
    @Override public synchronized void onDisable() {
        if (registration != null) {
            registration.close();
            registration = null;
        }
    }
    synchronized boolean renderPassInstalled() {
        return registration != null && registration.active();
    }

    static boolean admissible(final RenderFrame hud,
            final Minecraft189WorldCameraFrameState.Snapshot camera,
            final Minecraft189ProjectedPlayerFrame players,
            final int fw, final int fh, final float scale) {
        if (hud == null || camera == null || players == null
                || !camera.originAvailable() || hud.frameIndex() != camera.frame()
                || players.frame() != hud.frameIndex()
                || Math.abs(hud.partialTicks() - camera.partialTicks()) > 0.0001F
                || fw < 100 || fh < 100 || !Float.isFinite(scale) || scale <= 0)
            return false;
        final int[] viewport = camera.viewport();
        return viewport[0] >= 0 && viewport[1] >= 0
                && viewport[2] > 0 && viewport[3] > 0
                && (long)viewport[0] + viewport[2] <= fw
                && (long)viewport[1] + viewport[3] <= fh;
    }

    private final class BoxPass implements RenderPass {
        @Override public String id() { return PASS_ID; }
        @Override public RenderStage stage() { return RenderStage.HUD; }
        @Override public int priority() { return 128; }

        @Override public void render(final RenderFrame frame) {
            final Minecraft189WorldCameraFrameState.Snapshot camera =
                    state.snapshot();
            final Minecraft189ProjectedPlayerFrame evidence =
                    state.projectedPlayers();
            final int fw = host.framebufferWidth(), fh = host.framebufferHeight();
            final float scale = host.uiScale();
            if (!admissible(frame, camera, evidence, fw, fh, scale)
                    || evidence.visible().isEmpty()) return;
            final List<Minecraft189ProjectedPlayerFrame.Player> boxes =
                    evidence.visible();
            final float stroke = borderWidth.get().intValue();
            final String expected = System.getProperty(
                    "custommc.acceptance.expectedRealRemotePlayerBoxUuid");
            String realRemoteDrawn = null;
            String pixelRemoteUuid = null;
            int[][] samples = null;
            final boolean pixelRequired = Boolean.getBoolean(
                    "custommc.acceptance.requireRealRemoteBoxPixel");
            host.beginUi(new UiViewport(fw, fh, scale));
            RuntimeException failure = null;
            try {
                for (final Minecraft189ProjectedPlayerFrame.Player p : boxes) {
                    final Minecraft189WorldToScreenProjection.Rect r = p.rect;
                    if (r == null || r.width() <= 0 || r.height() <= 0) continue;
                    final float left = r.left/scale;
                    final float top = r.top/scale;
                    final float width = r.width()/scale;
                    final float height = r.height()/scale;
                    // Original framebuffer coordinates -> UI logical pixels.
                    // Outline is HUD-overlaid and intentionally not depth-occluded.
                    final float t = Math.min(stroke, Math.min(width, height) * 0.5F);
                    if (!Float.isFinite(left) || !Float.isFinite(top)
                            || !Float.isFinite(width) || !Float.isFinite(height)
                            || t <= 0) continue;
                    final int underlay = 0xC0101422, color = 0xFF70D9FF;
                    drawOutline(left-1, top-1, width+2, height+2, t+1, underlay);
                    drawOutline(left, top, width, height, t, color);
                    final String actualUuid = p.uuid.toString();
                    if (!acceptanceRemoteBoxReported && expected != null
                            && expected.equals(actualUuid)
                            && width > 1.0F && height > 1.0F) {
                        realRemoteDrawn = actualUuid;
                    }
                    // A successful original HUD draw is a one-time event;
                    // the real framebuffer pixel is a DISTINCT event.
                    // Never disable future original per-frame pixel probes
                    // simply because the first HUD-draw callback occurred.
                    // V190 real native original CI proved HUD PASS but
                    // timed out waiting for cyan framebuffer evidence.
                    if (Minecraft189PlayerBoxPixelProbe.shouldProbeOriginalRemote(
                            pixelRequired, acceptancePixelReported,
                            expected, actualUuid, width, height)) {
                        final int[][] candidate =
                                Minecraft189PlayerBoxPixelProbe.sampleVisibleStrokePixels(
                                        left, top, width, height, t,
                                        scale, fw, fh);
                        if (candidate.length > 0) {
                            samples = candidate;
                            pixelRemoteUuid = actualUuid;
                        } else if (acceptancePixelFailureSamples++ < 4) {
                            // Distinguish a real clipped original AABB
                            // with zero on-screen strokes from a failed GL
                            // color read. No success is inferred in either.
                            System.out.println("CUSTOMMC_BOX_PIXEL_DIAG no_visible_foreground_stroke"
                                    + " uuid=" + actualUuid
                                    + " rect=" + left + "," + top
                                    + "," + width + "," + height
                                    + " framebuffer=" + fw + "x" + fh);
                            System.out.flush();
                        }
                    }
                }
            } catch (RuntimeException error) {
                failure = error;
                throw error;
            } finally {
                try { host.endUi(); }
                catch (RuntimeException closeError) {
                    if (failure != null) failure.addSuppressed(closeError);
                    else throw closeError;
                }
            }
            // Evidence requires original native per-frame UUID/AABB projection
            // AND a completed real HUD begin/draw/end callback sequence.
            if (realRemoteDrawn != null && !acceptanceRemoteBoxReported) {
                acceptanceRemoteBoxReported = true;
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_REAL_REMOTE_PLAYER_BOX_HUD_DRAW_PASS=YES uuid="
                                + realRemoteDrawn);
                System.out.flush();
            }
            // Exact expected UUID's outline was genuinely drawn into the
            // CURRENT native GL render target; only the explicit acceptance
            // test uses glReadPixels. No screenshot/pixel read in normal play.
            if (pixelRemoteUuid != null && samples != null
                    && !acceptancePixelReported && pixelRequired) {
                try {
                    // Every candidate lies within an actual visible opaque
                    // cyan original HUD stroke on the exact UUID-matched
                    // source native projected player rectangle. Only the
                    // REAL current GL framebuffer can certify its color.
                    for (final int[] sample : samples) {
                        final ByteBuffer rgba = BufferUtils.createByteBuffer(4);
                        GL11.glReadPixels(sample[0], sample[1], 1, 1,
                                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, rgba);
                        final int red = rgba.get(0) & 0xFF;
                        final int green = rgba.get(1) & 0xFF;
                        final int blue = rgba.get(2) & 0xFF;
                        if (Minecraft189PlayerBoxPixelProbe.isForegroundColor(
                                red, green, blue)) {
                            acceptancePixelReported = true;
                            System.out.println(
                                    "CUSTOMMC_OFFICIAL_189_REAL_REMOTE_BOX_FRAMEBUFFER_PIXEL_PASS=YES uuid="
                                            + pixelRemoteUuid
                                            + " rgba=" + red + "," + green + ","
                                            + blue + "," + (rgba.get(3) & 0xFF));
                            System.out.flush();
                            break;
                        }
                        if (acceptancePixelFailureSamples++ < 4) {
                            System.out.println("CUSTOMMC_BOX_PIXEL_DIAG expected=112,217,255"
                                    + " actual=" + red + "," + green + "," + blue
                                    + " sample=" + sample[0] + "," + sample[1]);
                            System.out.flush();
                        }
                    }
                } catch (RuntimeException | LinkageError readError) {
                    if (acceptancePixelFailureSamples++ < 4) {
                        System.out.println("CUSTOMMC_BOX_PIXEL_DIAG error="
                                + readError.getClass().getSimpleName());
                        System.out.flush();
                    }
                }
            }
        }

        private void drawOutline(final float x, final float y,
                final float width, final float height, final float thickness,
                final int color) {
            if (width <= 0 || height <= 0 || thickness <= 0) return;
            host.fillRoundedRect(x, y, width, thickness, 0, color);
            host.fillRoundedRect(x, y+height-thickness, width, thickness, 0, color);
            host.fillRoundedRect(x, y+thickness, thickness,
                    Math.max(0, height-2*thickness), 0, color);
            host.fillRoundedRect(x+width-thickness, y+thickness, thickness,
                    Math.max(0, height-2*thickness), 0, color);
        }
    }
}
