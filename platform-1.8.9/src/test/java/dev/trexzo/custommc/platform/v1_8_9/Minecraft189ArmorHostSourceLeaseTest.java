package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.*;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.setting.*;
import dev.trexzo.custommc.core.ui.*;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189ArmorHostSourceLeaseTest {
    static final class OriginalItem implements Minecraft189ItemStackAccess {
        int damage = 5, max = 100;
        int damageReads, maxReads;
        Runnable onDamage, onMax;
        boolean failDamage, failMax, errorDamage;
        @Override public String customMcDisplayName() { return "original armor"; }
        @Override public int customMcStackSize() { return 1; }
        @Override public int customMcItemDamage() {
            damageReads++;
            Runnable call = onDamage; onDamage = null;
            if (call != null) call.run();
            if (errorDamage) throw new AssertionError("original armor item");
            if (failDamage) throw new IllegalStateException("native item damage");
            return damage;
        }
        @Override public int customMcMaxDamage() {
            maxReads++;
            Runnable call = onMax; onMax = null;
            if (call != null) call.run();
            if (failMax) throw new IllegalStateException("native item max");
            return max;
        }
    }

    static final class OriginalArmor implements Minecraft189PlayerArmorAccess {
        final OriginalItem[] items = {
            new OriginalItem(), new OriginalItem(),
            new OriginalItem(), new OriginalItem()
        };
        final int[] reads = new int[8];
        int callbackAt = -1, throwAt = -1, errorAt = -1;
        Runnable callback;
        boolean boots = true, leggings = true, chestplate = true, helmet = true;
        void read(int index) {
            reads[index]++;
            if (callbackAt == index && callback != null) {
                Runnable run = callback; callback = null; run.run();
            }
            if (errorAt == index) throw new AssertionError("original armor getter");
            if (throwAt == index) throw new IllegalStateException("original armor getter");
        }
        @Override public boolean customMcArmorBoots() { read(0); return boots; }
        @Override public boolean customMcArmorLeggings() { read(1); return leggings; }
        @Override public boolean customMcArmorChestplate() { read(2); return chestplate; }
        @Override public boolean customMcArmorHelmet() { read(3); return helmet; }
        @Override public Minecraft189ItemStackAccess customMcArmorBootsItem() { read(4); return items[0]; }
        @Override public Minecraft189ItemStackAccess customMcArmorLeggingsItem() { read(5); return items[1]; }
        @Override public Minecraft189ItemStackAccess customMcArmorChestplateItem() { read(6); return items[2]; }
        @Override public Minecraft189ItemStackAccess customMcArmorHelmetItem() { read(7); return items[3]; }
        int totalReads() {
            int result = 0;
            for (int n : reads) result += n;
            return result;
        }
    }

    static final class Fixture implements AutoCloseable {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        final Minecraft189HostRuntime runtime;
        Fixture() {
            services.register(RenderPipeline.class, new RenderPipeline());
            Minecraft189Platform platform = new Minecraft189Platform();
            platform.attach(new PlatformContext(
                    new EventBus(), modules, controller, services));
            runtime = Minecraft189HostRuntime.install(platform,
                    new ModulePresentationRegistry(), new ModuleCategoryRegistry(),
                    new ModuleSettingRegistry(modules, settings), null, null,
                    settings, new SettingPresentationRegistry(), new NoOpHost());
        }
        Minecraft189PlayerArmorState.Snapshot armor() {
            return runtime.playerArmorState().snapshot();
        }
        @Override public void close() { runtime.close(); }
    }
    static final class NoOpHost implements LegacyUiHostCallbacks {
        @Override public int framebufferWidth() { return 1280; }
        @Override public int framebufferHeight() { return 720; }
        @Override public float uiScale() { return 1F; }
        @Override public void beginUi(UiViewport v) {}
        @Override public void fillRect(float x,float y,float w,float h,int c) {}
        @Override public void fillRoundedRect(float x,float y,float w,float h,float rad,int c) {}
        @Override public void strokeRect(float x,float y,float w,float h,float t,int c) {}
        @Override public void pushClip(float x,float y,float w,float h) {}
        @Override public void popClip() {}
        @Override public void drawText(UiFontHandle f,float x,float y,String text,int c) {}
        @Override public void endUi() {}
    }

    @Test void exactReferenceAndRecovery() {
        Minecraft189ArmorHostSourceLease lease = new Minecraft189ArmorHostSourceLease();
        OriginalArmor a = new OriginalArmor(), b = new OriginalArmor();
        assertTrue(lease.enter(a));
        assertFalse(lease.enter(a));
        assertTrue(lease.stillOwned());
        assertFalse(lease.enter(b));
        assertFalse(lease.stillOwned());
        assertFalse(lease.enter(a));
        lease.exit();
        assertTrue(lease.enter(b));
        lease.exit();
    }
    @Test void nullWorldUnloadIrrevocablyRevokesOriginal() {
        Minecraft189ArmorHostSourceLease lease = new Minecraft189ArmorHostSourceLease();
        assertTrue(lease.enter(new OriginalArmor()));
        assertFalse(lease.enter(null));
        assertFalse(lease.stillOwned());
        lease.exit();
    }
    @Test void genuineArmorAndDurabilityPublishTogether() {
        try (Fixture f = new Fixture()) {
            OriginalArmor a = new OriginalArmor();
            f.runtime.playerArmor(a);
            assertTrue(f.armor().available());
            assertEquals(4, f.armor().equippedCount());
            assertEquals(5, f.armor().bootsDurability().itemDamage());
            assertEquals(100, f.armor().helmetDurability().maxDamage());
            assertEquals(8, a.totalReads());
            for (OriginalItem item : a.items) {
                assertEquals(1, item.damageReads);
                assertEquals(1, item.maxReads);
            }
        }
    }
    @Test void nestedSameSourceDoesNotSpendSecondNativeGetterTick() {
        try (Fixture f = new Fixture()) {
            for (int i=0; i<8; i++) {
                OriginalArmor a = new OriginalArmor();
                a.callbackAt = i;
                a.callback = () -> f.runtime.playerArmor(a);
                f.runtime.playerArmor(a);
                assertTrue(f.armor().available());
                assertEquals(8, a.totalReads());
            }
        }
    }
    @Test void changedSourceDuringAnyOfEightNativeGettersClearsAndStops() {
        try (Fixture f = new Fixture()) {
            for (int i=0; i<8; i++) {
                f.runtime.playerArmor(new OriginalArmor());
                OriginalArmor a = new OriginalArmor(), b = new OriginalArmor();
                a.callbackAt = i;
                a.callback = () -> f.runtime.playerArmor(b);
                f.runtime.playerArmor(a);
                assertFalse(f.armor().available(), "native getter index " + i);
                assertEquals(0, b.totalReads());
                assertEquals(i+1, a.totalReads());
            }
        }
    }
    @Test void nullDuringAnyOfEightNativeGettersClears() {
        try (Fixture f = new Fixture()) {
            for (int i=0; i<8; i++) {
                f.runtime.playerArmor(new OriginalArmor());
                OriginalArmor a = new OriginalArmor();
                a.callbackAt = i;
                a.callback = () -> f.runtime.playerArmor(null);
                f.runtime.playerArmor(a);
                assertFalse(f.armor().available(), "null getter " + i);
                assertEquals(i+1, a.totalReads());
            }
        }
    }
    @Test void originalGetterExceptionNeverRetainsPreviousArmor() {
        try (Fixture f = new Fixture()) {
            for (int i=0; i<8; i++) {
                f.runtime.playerArmor(new OriginalArmor());
                OriginalArmor bad = new OriginalArmor();
                bad.throwAt = i;
                assertThrows(IllegalStateException.class,
                        () -> f.runtime.playerArmor(bad));
                assertFalse(f.armor().available(), "exception getter " + i);
            }
        }
    }
    @Test void originalErrorClearsOldStateAndPropagates() {
        try (Fixture f = new Fixture()) {
            f.runtime.playerArmor(new OriginalArmor());
            OriginalArmor bad = new OriginalArmor();
            bad.errorAt = 7;
            assertThrows(AssertionError.class, () -> f.runtime.playerArmor(bad));
            assertFalse(f.armor().available());
        }
    }
    @Test void nativeItemDamageReentryDifferentPlayerInvalidatesArmor() {
        try (Fixture f = new Fixture()) {
            f.runtime.playerArmor(new OriginalArmor());
            OriginalArmor a = new OriginalArmor(), b = new OriginalArmor();
            a.items[0].onDamage = () -> f.runtime.playerArmor(b);
            f.runtime.playerArmor(a);
            assertFalse(f.armor().available());
            assertEquals(0, b.totalReads());
        }
    }
    @Test void nativeItemMaxGetterReentryNullInvalidatesArmor() {
        try (Fixture f = new Fixture()) {
            OriginalArmor a = new OriginalArmor();
            a.items[3].onMax = () -> f.runtime.playerArmor(null);
            f.runtime.playerArmor(a);
            assertFalse(f.armor().available());
        }
    }
    @Test void sameOriginalReentryThroughItemMaxNeverDuplicatesReads() {
        try (Fixture f = new Fixture()) {
            OriginalArmor a = new OriginalArmor();
            a.items[2].onMax = () -> f.runtime.playerArmor(a);
            f.runtime.playerArmor(a);
            assertTrue(f.armor().available());
            assertEquals(8, a.totalReads());
            assertEquals(1, a.items[2].maxReads);
        }
    }
    @Test void invalidNegativeNativeItemDamageClearsEntireOldSnapshot() {
        try (Fixture f = new Fixture()) {
            f.runtime.playerArmor(new OriginalArmor());
            OriginalArmor bad = new OriginalArmor();
            bad.items[3].damage = -1;
            assertThrows(IllegalArgumentException.class,
                    () -> f.runtime.playerArmor(bad));
            assertFalse(f.armor().available());
            assertEquals(0, f.armor().mask());
        }
    }
    @Test void invalidNegativeNativeMaximumClearsEntireOldSnapshot() {
        try (Fixture f = new Fixture()) {
            f.runtime.playerArmor(new OriginalArmor());
            OriginalArmor bad = new OriginalArmor();
            bad.items[2].max = -1;
            assertThrows(IllegalArgumentException.class,
                    () -> f.runtime.playerArmor(bad));
            assertFalse(f.armor().available());
        }
    }
    @Test void originalItemGetterRuntimeFailureClearsOldState() {
        try (Fixture f = new Fixture()) {
            f.runtime.playerArmor(new OriginalArmor());
            OriginalArmor bad = new OriginalArmor();
            bad.items[3].failMax = true;
            assertThrows(IllegalStateException.class,
                    () -> f.runtime.playerArmor(bad));
            assertFalse(f.armor().available());
        }
    }
    @Test void originalItemGetterErrorClearsOldState() {
        try (Fixture f = new Fixture()) {
            f.runtime.playerArmor(new OriginalArmor());
            OriginalArmor bad = new OriginalArmor();
            bad.items[1].errorDamage = true;
            assertThrows(AssertionError.class, () -> f.runtime.playerArmor(bad));
            assertFalse(f.armor().available());
        }
    }
    @Test void originalNullExplicitlyClearsArmorAndFreshSourceRecovers() {
        try (Fixture f = new Fixture()) {
            f.runtime.playerArmor(new OriginalArmor());
            f.runtime.playerArmor(null);
            assertFalse(f.armor().available());
            OriginalArmor next = new OriginalArmor();
            next.helmet = false;
            f.runtime.playerArmor(next);
            assertTrue(f.armor().available());
            assertEquals(3, f.armor().equippedCount());
        }
    }
    @Test void stateUpdateIsAtomicEvenBeforeHostClearsAfterBadItem() {
        Minecraft189PlayerArmorState state = new Minecraft189PlayerArmorState();
        OriginalArmor source = new OriginalArmor();
        state.update(true, true, true, true,
                source.items[0], source.items[1], source.items[2], source.items[3]);
        assertEquals(4, state.snapshot().equippedCount());
        assertEquals(5, state.snapshot().helmetDurability().itemDamage());
        OriginalItem bad = new OriginalItem(); bad.max = -1;
        assertThrows(IllegalArgumentException.class,
                () -> state.update(false, false, false, false,
                        null, null, null, bad));
        assertTrue(state.snapshot().available());
        assertEquals(4, state.snapshot().equippedCount());
        assertEquals(5, state.snapshot().helmetDurability().itemDamage());
    }
    @Test void nonDamageableRealItemRemainsAcceptedWithZeroMaximum() {
        try(Fixture f = new Fixture()) {
            OriginalArmor a = new OriginalArmor();
            a.items[0].max = 0;
            f.runtime.playerArmor(a);
            assertTrue(f.armor().available());
            assertFalse(f.armor().bootsDurability().damageable());
            assertEquals(0, f.armor().bootsDurability().durabilityRemaining());
        }
    }
}
