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
