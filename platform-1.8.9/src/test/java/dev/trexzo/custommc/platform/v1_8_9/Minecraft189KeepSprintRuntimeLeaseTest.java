package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189KeepSprintRuntimeLeaseTest {

    @Test void restoreRequiresPositivePreclickSourceAndIsOneShot() {
        try (final Fixture fixture = new Fixture()) {
            final Player source = fixture.player();
            fixture.controller.enable(Minecraft189KeepSprintModule.ID);
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0, source.setCalls,
                    "enabled module cannot set sprint without a granted click");
            assertFalse(fixture.runtime.shouldKeepSprintAfterSyntheticClick(false));
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0, source.setCalls,
                    "a non-player hit never grants the default native lease");
            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            source.sprinting = false; // Vanilla itself clears sprint after hit.
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(1, source.setCalls);
            assertTrue(source.sprinting);
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(1, source.setCalls,
                    "one permitted original click restores sprint only once");
        }
    }

    @Test void replacingOriginalLocalPlayerRevokesOldClickAndCannotRestoreNewOne() {
        try (final Fixture fixture = new Fixture()) {
            final Player old = fixture.player();
            fixture.controller.enable(Minecraft189KeepSprintModule.ID);
            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            final Player replacement = new Player();
            fixture.runtime.playerMovementState(replacement);
            fixture.runtime.playerSprintControl(replacement);
            old.sprinting = false;
            replacement.sprinting = false;
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0, old.setCalls);
            assertEquals(0, replacement.setCalls,
                    "new live source cannot inherit old player's authorization");
            replacement.sprinting = true;
            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            replacement.sprinting = false;
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(1, replacement.setCalls);
            assertEquals(0, old.setCalls);
        }
    }

    @Test void guiPolicyEditsAndDisableRevokeOriginalClick() {
        try (final Fixture fixture = new Fixture()) {
            final Player source = fixture.player();
            fixture.controller.enable(Minecraft189KeepSprintModule.ID);
            final Minecraft189KeepSprintModule module =
                    fixture.runtime.featureCatalog().keepSprint();
            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            module.requireForwardSetting().set(false);
            source.sprinting = false;
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0, source.setCalls,
                    "relaxing a setting after admission must NOT authorize old hit");

            source.sprinting = true;
            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            module.pauseWhileSneakingSetting().set(false);
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0, source.setCalls,
                    "changing Sneak policy revokes pending sprint restoration");

            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            fixture.controller.disable(Minecraft189KeepSprintModule.ID);
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0, source.setCalls);
        }
    }

    @Test void genuineForwardAndSneakChangesAreRecheckedAfterNativeClick() {
        try (final Fixture fixture = new Fixture()) {
            final Player source = fixture.player();
            fixture.controller.enable(Minecraft189KeepSprintModule.ID);
            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            fixture.runtime.inputState().key(LegacyKeyboardCodes.W, false);
            source.sprinting = false;
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0, source.setCalls);

            fixture.runtime.inputState().key(LegacyKeyboardCodes.W, true);
            source.sprinting = true;
            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            source.sneaking = true;
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0, source.setCalls);
            source.sneaking = false;
            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            source.sprinting = false;
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(1, source.setCalls);
        }
    }

    @Test void disableReenableSameSourceSettingsCannotAdoptPreviousClickLease() {
        try (final Fixture fixture = new Fixture()) {
            final Player source = fixture.player();
            fixture.controller.enable(Minecraft189KeepSprintModule.ID);
            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            fixture.controller.disable(Minecraft189KeepSprintModule.ID);
            fixture.controller.enable(Minecraft189KeepSprintModule.ID);
            source.sprinting = false;
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0, source.setCalls,
                    "new enabled module lifecycle cannot inherit old click");
            source.sprinting = true;
            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            source.sprinting = false;
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(1, source.setCalls,
                    "fresh admission in new module lifecycle is allowed");
        }
    }

    @Test void nestedSourceAdmissionMustNotResurrectOuterClickPermission() {
        try (final Fixture fixture = new Fixture()) {
            final Player source = fixture.player();
            fixture.controller.enable(Minecraft189KeepSprintModule.ID);
            source.onRead = () -> {
                source.onRead = null;
                assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true),
                        "reentrant native source can independently evaluate");
            };
            assertFalse(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true),
                    "older admission must fail when source getter reenters");
            source.sprinting = false;
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0, source.setCalls,
                    "nested approval is also revoked when outer frame is stale");
            source.sprinting = true;
            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            source.sprinting = false;
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(1, source.setCalls,
                    "fresh independent original click re-authorizes");
        }
    }

    @Test void nativeSourceLifecycleChangesDuringApprovalNeverMintGrant() {
        try (final Fixture fixture = new Fixture()) {
            final Player source = fixture.player();
            fixture.controller.enable(Minecraft189KeepSprintModule.ID);
            source.onRead = () -> {
                source.onRead = null;
                fixture.controller.disable(Minecraft189KeepSprintModule.ID);
                fixture.controller.enable(Minecraft189KeepSprintModule.ID);
            };
            assertFalse(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            source.sprinting = false;
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0, source.setCalls,
                    "module was reset mid-check, even if end state enabled");
            source.sprinting = true;
            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            source.sprinting = false;
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(1, source.setCalls);
        }
    }

    @Test void nativeSprintSetterReentryAndExceptionCannotReplayOldGrant() {
        try (final Fixture fixture = new Fixture()) {
            final Player source = fixture.player();
            fixture.controller.enable(Minecraft189KeepSprintModule.ID);
            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            source.sprinting = false;
            source.onSet = () -> {
                fixture.runtime.restoreSprintAfterSyntheticClick();
                throw new IllegalStateException("original native sprint setter threw");
            };
            assertThrows(IllegalStateException.class,
                    fixture.runtime::restoreSprintAfterSyntheticClick);
            assertEquals(1, source.setCalls);
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(1, source.setCalls, "exception already consumed grant");
            source.onSet = null;
            source.sprinting = true;
            assertTrue(fixture.runtime.shouldKeepSprintAfterSyntheticClick(true));
            source.sprinting = false;
            fixture.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(2, source.setCalls,
                    "fresh genuine click may gain its own independent grant");
        }
    }

    private static final class Player implements
            Minecraft189PlayerMovementStateAccess,
            Minecraft189PlayerSprintControl {
        private boolean onGround = true;
        private boolean sneaking;
        private boolean sprinting = true;
        private int setCalls;
        private Runnable onSet;
        private Runnable onRead;
        @Override public boolean customMcOnGround() { return onGround; }
        @Override public boolean customMcSneaking() { return sneaking; }
        @Override public boolean customMcSprinting() {
            if (onRead != null) onRead.run();
            return sprinting;
        }
        @Override public void customMcSetSprinting(boolean value) {
            setCalls++;
            sprinting = value;
            if (onSet != null) onSet.run();
        }
    }

    private static final class Fixture implements AutoCloseable {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189HostRuntime runtime;

        Fixture() {
            final ServiceRegistry services = new ServiceRegistry();
            services.register(RenderPipeline.class, new RenderPipeline());
            final Minecraft189Platform platform = new Minecraft189Platform();
            platform.attach(new PlatformContext(new EventBus(), modules,
                    controller, services));
            runtime = Minecraft189HostRuntime.install(
                    platform, new ModulePresentationRegistry(),
                    new ModuleCategoryRegistry(),
                    new ModuleSettingRegistry(modules, settings),
                    null, null, settings, new SettingPresentationRegistry(),
                    new NoOpHost());
            runtime.inputState().key(LegacyKeyboardCodes.W, true);
        }

        Player player() {
            final Player player = new Player();
            runtime.playerMovementState(player);
            runtime.playerSprintControl(player);
            return player;
        }

        @Override public void close() { runtime.close(); }
    }

    private static final class NoOpHost implements LegacyUiHostCallbacks {
        @Override public int framebufferWidth() { return 1280; }
        @Override public int framebufferHeight() { return 720; }
        @Override public float uiScale() { return 1.0F; }
        @Override public void beginUi(UiViewport viewport) { }
        @Override public void fillRect(float x, float y, float width,
                float height, int argb) { }
        @Override public void fillRoundedRect(float x, float y, float width,
                float height, float radius, int argb) { }
        @Override public void strokeRect(float x, float y, float width,
                float height, float thickness, int argb) { }
        @Override public void pushClip(float x, float y, float width,
                float height) { }
        @Override public void popClip() { }
        @Override public void drawText(UiFontHandle font, float x,
                float y, String text, int argb) { }
        @Override public void endUi() { }
    }
}
