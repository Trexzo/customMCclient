package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189FriendGuardModuleTest {
    @Test void filtersOnlyExactVerifiedUuidAndTracksLiveEdits() {
        final UUID a = UUID.fromString("00000000-0000-4000-8000-000000000001");
        final UUID b = UUID.fromString("00000000-0000-4000-8000-000000000002");
        final UUID c = UUID.fromString("00000000-0000-4000-8000-000000000003");
        final Minecraft189FriendGuardModule guard = new Minecraft189FriendGuardModule();
        final Minecraft189WorldEntityUuidState world =
                new Minecraft189WorldEntityUuidState();
        world.update(new UUID[]{a,b,c});
        assertTrue(guard.permits(0, null)); // Default off
        guard.friendUuidsSetting().set(a + ", " + b.toString().toUpperCase(java.util.Locale.ROOT));
        assertEquals(a + ", " + b.toString().toUpperCase(java.util.Locale.ROOT),
                guard.friendUuidsSetting().encode());
        assertFalse(guard.requiresIdentity());
        guard.onEnable();
        try {
            assertTrue(guard.requiresIdentity());
            assertFalse(guard.permits(0, world.snapshot()));
            assertFalse(guard.permits(1, world.snapshot()));
            assertTrue(guard.permits(2, world.snapshot()));
            assertFalse(guard.permits(9, world.snapshot()));
            assertFalse(guard.permits(-1, world.snapshot()));
            assertFalse(guard.permits(2, null));
            world.update(new UUID[]{c,a,b}); // World list reorder
            assertFalse(guard.permits(1, world.snapshot()));
            assertTrue(guard.permits(0, world.snapshot()));
            // Live edit immediately updates filtering, with no module restart.
            guard.friendUuidsSetting().set(c.toString());
            assertFalse(guard.permits(0, world.snapshot()));
            assertTrue(guard.permits(1, world.snapshot()));
            guard.friendUuidsSetting().set("");
            assertFalse(guard.requiresIdentity());
            assertTrue(guard.permits(1, null));
        } finally { guard.onDisable(); }
        assertTrue(guard.permits(0, null));
    }

    @Test void invalidOrDuplicateUuidInputCannotReplaceSavedFriends() {
        final Minecraft189FriendGuardModule guard = new Minecraft189FriendGuardModule();
        final String friend = "00000000-0000-4000-8000-000000000001";
        guard.friendUuidsSetting().set(friend);
        for (String bad : new String[]{
                "friend-name", "1-1-1-1-1", friend + ",", "," + friend,
                friend + ", , " + friend, friend + ", " + friend,
                "00000000-0000-4000-8000-00000000000g",
                String.join("", java.util.Collections.nCopies(1025, "x"))
        }) {
            assertThrows(IllegalArgumentException.class,
                    () -> guard.friendUuidsSetting().set(bad));
            assertEquals(friend, guard.friendUuidsSetting().get());
        }
        guard.friendUuidsSetting().set("   ");
        assertFalse(guard.requiresIdentity());
    }

    @Test void registeredSettingIsPersistentAndModuleLifecycleIsIndependent() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189FriendGuardFeature feature =
                Minecraft189FriendGuardFeature.install(
                        modules, controller, new ModulePresentationRegistry(),
                        new ModuleSettingRegistry(modules, settings), settings,
                        new SettingPresentationRegistry());
        try {
            assertNotNull(modules.find(Minecraft189FriendGuardModule.ID));
            assertTrue(feature.module().friendUuidsSetting().isPersistent());
            assertFalse(feature.module().active());
            controller.enable(Minecraft189FriendGuardModule.ID);
            assertTrue(feature.module().active());
        } finally { feature.close(); feature.close(); }
        assertNull(modules.find(Minecraft189FriendGuardModule.ID));
    }
}
