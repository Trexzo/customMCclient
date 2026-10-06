package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleKeyChord;
import dev.trexzo.custommc.core.module.ModuleKeybind;
import dev.trexzo.custommc.core.module.ModuleKeybindController;
import dev.trexzo.custommc.core.module.ModuleKeybindRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiInputController;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.platform.PlatformContext;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189ModuleKeybindRuntimeTest {
    @Test
    void pressRoutesWithoutClickGuiAndRepeatReleaseDoNotRetrigger() {
        final AtomicInteger enables =
                new AtomicInteger();
        final AtomicInteger disables =
                new AtomicInteger();
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(
                module(
                        "combat.aura",
                        enables,
                        disables));
        final ModuleController lifecycle =
                new ModuleController(modules);
        final ServiceRegistry services =
                new ServiceRegistry();
        final Minecraft189Platform platform =
                platform(
                        modules,
                        lifecycle,
                        services);

        final ModuleKeybindRegistry keybinds =
                new ModuleKeybindRegistry(modules);
        keybinds.register(
                new ModuleKeybind(
                        "combat.aura",
                        ModuleKeyChord.key(
                                "legacy-key-37")));

        final Minecraft189ModuleKeybindRuntime runtime =
                Minecraft189ModuleKeybindRuntime.install(
                        platform,
                        keybinds);
        final Minecraft189InputHooks hooks =
                new Minecraft189InputHooks(platform);

        assertSame(
                runtime.controller(),
                services.require(
                        ModuleKeybindController.class));

        assertTrue(
                hooks.key(
                        37,
                        'k',
                        true,
                        false,
                        false,
                        false,
                        false));
        assertEquals(
                ModuleState.ENABLED,
                lifecycle.stateOf(
                        "combat.aura"));
        assertEquals(
                1,
                enables.get());

        assertFalse(
                hooks.key(
                        37,
                        'k',
                        true,
                        true,
                        false,
                        false,
                        false));
        assertFalse(
                hooks.key(
                        37,
                        'k',
                        false,
                        false,
                        false,
                        false,
                        false));
        assertEquals(
                ModuleState.ENABLED,
                lifecycle.stateOf(
                        "combat.aura"));
        assertEquals(
                1,
                enables.get());
        assertEquals(
                0,
                disables.get());

        assertTrue(
                hooks.key(
                        37,
                        'k',
                        true,
                        false,
                        false,
                        false,
                        false));
        assertEquals(
                ModuleState.DISABLED,
                lifecycle.stateOf(
                        "combat.aura"));
        assertEquals(
                1,
                disables.get());

        runtime.close();

        assertTrue(runtime.closed());
        assertFalse(
                services.contains(
                        ModuleKeybindController.class));
        assertFalse(
                hooks.key(
                        37,
                        'k',
                        true,
                        false,
                        false,
                        false,
                        false));

        runtime.close();
        assertTrue(runtime.closed());
    }

    @Test
    void openClickGuiSuppressesBindAfterGuiGetsFirstChance() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(
                module(
                        "render.esp",
                        new AtomicInteger(),
                        new AtomicInteger()));
        final ModuleController lifecycle =
                new ModuleController(modules);
        final ServiceRegistry services =
                new ServiceRegistry();
        final Minecraft189Platform platform =
                platform(
                        modules,
                        lifecycle,
                        services);

        final ModuleKeybindRegistry keybinds =
                new ModuleKeybindRegistry(modules);
        keybinds.register(
                new ModuleKeybind(
                        "render.esp",
                        new ModuleKeyChord(
                                "legacy-key-37",
                                false,
                                true,
                                false)));
        final Minecraft189ModuleKeybindRuntime runtime =
                Minecraft189ModuleKeybindRuntime.install(
                        platform,
                        keybinds);

        final ClickGuiModel model =
                new ClickGuiModel();
        model.register(
                new ClickGuiPage(
                        "modules",
                        "Modules",
                        0));
        model.open();

        final ClickGuiInputController input =
                new ClickGuiInputController(
                        model,
                        new UiFocusManager());
        services.register(
                ClickGuiModel.class,
                model);
        services.register(
                ClickGuiInputController.class,
                input);

        try {
            final Minecraft189InputHooks hooks =
                    new Minecraft189InputHooks(
                            platform);

            assertFalse(
                    hooks.key(
                            37,
                            'k',
                            true,
                            false,
                            false,
                            true,
                            false));
            assertEquals(
                    ModuleState.DISABLED,
                    lifecycle.stateOf(
                            "render.esp"));

            model.close();

            assertTrue(
                    hooks.key(
                            37,
                            'k',
                            true,
                            false,
                            false,
                            true,
                            false));
            assertEquals(
                    ModuleState.ENABLED,
                    lifecycle.stateOf(
                            "render.esp"));

            assertFalse(
                    hooks.key(
                            37,
                            'k',
                            true,
                            false,
                            false,
                            false,
                            false));
            assertEquals(
                    ModuleState.ENABLED,
                    lifecycle.stateOf(
                            "render.esp"));
        } finally {
            input.close();
            runtime.close();
        }
    }

    private static Minecraft189Platform platform(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ServiceRegistry services) {
        final Minecraft189Platform platform =
                new Minecraft189Platform();
        platform.attach(
                new PlatformContext(
                        new EventBus(),
                        modules,
                        controller,
                        services));
        return platform;
    }

    private static Module module(
            final String id,
            final AtomicInteger enables,
            final AtomicInteger disables) {
        return new Module() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public void onEnable() {
                enables.incrementAndGet();
            }

            @Override
            public void onDisable() {
                disables.incrementAndGet();
            }
        };
    }
}
