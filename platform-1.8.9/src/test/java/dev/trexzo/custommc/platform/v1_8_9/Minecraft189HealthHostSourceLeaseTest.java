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

final class Minecraft189HealthHostSourceLeaseTest {
    static final class OriginalHealth implements Minecraft189PlayerHealthAccess {
        float health = 12F, maximum = 20F;
        int healthReads, maximumReads;
        Runnable onHealth, onMaximum;
        boolean failHealth, failMaximum, errorHealth, errorMaximum;

        @Override public float customMcHealth() {
            healthReads++;
            final Runnable callback = onHealth;
            onHealth = null;
            if (callback != null) callback.run();
            if (errorHealth) throw new AssertionError("native health error");
            if (failHealth) throw new IllegalStateException("native health getter");
            return health;
        }

        @Override public float customMcMaxHealth() {
            maximumReads++;
            final Runnable callback = onMaximum;
            onMaximum = null;
            if (callback != null) callback.run();
            if (errorMaximum) throw new AssertionError("native maxHealth error");
            if (failMaximum) throw new IllegalStateException("native maxHealth getter");
            return maximum;
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
            final Minecraft189Platform platform = new Minecraft189Platform();
            platform.attach(new PlatformContext(new EventBus(), modules, controller, services));
            runtime = Minecraft189HostRuntime.install(platform,
                    new ModulePresentationRegistry(),
                    new ModuleCategoryRegistry(),
                    new ModuleSettingRegistry(modules, settings),
                    null, null, settings,
                    new SettingPresentationRegistry(), new NoOpHost());
        }
        Minecraft189PlayerHealthState.Snapshot snapshot() {
            return runtime.playerHealthState().snapshot();
        }
        @Override public void close() { runtime.close(); }
    }

    private static final class NoOpHost implements LegacyUiHostCallbacks {
        @Override public int framebufferWidth() { return 1280; }
        @Override public int framebufferHeight() { return 720; }
        @Override public float uiScale() { return 1F; }
        @Override public void beginUi(UiViewport v) {}
        @Override public void fillRect(float x, float y, float w, float h, int c) {}
        @Override public void fillRoundedRect(float x, float y, float w, float h, float r, int c) {}
        @Override public void strokeRect(float x, float y, float w, float h, float t, int c) {}
        @Override public void pushClip(float x, float y, float w, float h) {}
        @Override public void popClip() {}
        @Override public void drawText(UiFontHandle f, float x, float y, String s, int c) {}
        @Override public void endUi() {}
    }

    @Test void firstOriginalAndRecovery() {
        Minecraft189HealthHostSourceLease l = new Minecraft189HealthHostSourceLease();
        assertTrue(l.enter(new OriginalHealth())); assertTrue(l.stillOwned()); l.exit();
        assertTrue(l.enter(new OriginalHealth())); assertTrue(l.stillOwned()); l.exit();
    }
    @Test void nestedSameOriginalNotNewTick() {
        Minecraft189HealthHostSourceLease l = new Minecraft189HealthHostSourceLease();
        OriginalHealth a = new OriginalHealth();
        assertTrue(l.enter(a)); assertFalse(l.enter(a));
        assertTrue(l.stillOwned()); l.exit();
    }
    @Test void changedOriginalRevokesIrrevocably() {
        Minecraft189HealthHostSourceLease l = new Minecraft189HealthHostSourceLease();
        OriginalHealth a = new OriginalHealth();
        assertTrue(l.enter(a)); assertFalse(l.enter(new OriginalHealth()));
        assertFalse(l.enter(a)); assertFalse(l.stillOwned()); l.exit();
    }
    @Test void nullNativeCallbackRevokesPendingSample() {
        Minecraft189HealthHostSourceLease l = new Minecraft189HealthHostSourceLease();
        assertTrue(l.enter(new OriginalHealth())); assertFalse(l.enter(null));
        assertFalse(l.stillOwned()); l.exit();
    }
    @Test void nullOuterCannotInheritNewPlayer() {
        Minecraft189HealthHostSourceLease l = new Minecraft189HealthHostSourceLease();
        assertTrue(l.enter(null)); assertFalse(l.enter(new OriginalHealth()));
        assertFalse(l.stillOwned()); l.exit();
    }
    @Test void twoLeasesRemainIndependent() {
        Minecraft189HealthHostSourceLease one = new Minecraft189HealthHostSourceLease();
        Minecraft189HealthHostSourceLease two = new Minecraft189HealthHostSourceLease();
        assertTrue(one.enter(new OriginalHealth())); assertTrue(two.enter(new OriginalHealth()));
        assertFalse(one.enter(null)); assertFalse(one.stillOwned());
        assertTrue(two.stillOwned()); one.exit(); two.exit();
    }
    @Test void referenceIdentityNotEquals() {
        Minecraft189HealthHostSourceLease l = new Minecraft189HealthHostSourceLease();
        Minecraft189PlayerHealthAccess a = new Minecraft189PlayerHealthAccess() {
            public float customMcHealth(){return 2F;}
            public float customMcMaxHealth(){return 20F;}
            @Override public boolean equals(Object b){return b instanceof Minecraft189PlayerHealthAccess;}
            @Override public int hashCode(){return 42;}
        };
        Minecraft189PlayerHealthAccess b = new Minecraft189PlayerHealthAccess() {
            public float customMcHealth(){return 4F;}
            public float customMcMaxHealth(){return 20F;}
            @Override public boolean equals(Object x){return x instanceof Minecraft189PlayerHealthAccess;}
            @Override public int hashCode(){return 42;}
        };
        assertTrue(a.equals(b)); assertTrue(l.enter(a));
        assertFalse(l.enter(b)); assertFalse(l.stillOwned()); l.exit();
    }
    @Test void twoRealMappedValuesPublishTogether() {
        try(Fixture f = new Fixture()) {
            OriginalHealth a = new OriginalHealth();
            f.runtime.playerHealth(a);
            assertTrue(f.snapshot().available());
            assertEquals(12F, f.snapshot().health(), 0F);
            assertEquals(20F, f.snapshot().maxHealth(), 0F);
            assertEquals(1, a.healthReads); assertEquals(1, a.maximumReads);
        }
    }
    @Test void zeroHealthIsValidOriginal() {
        try(Fixture f = new Fixture()) {
            OriginalHealth a = new OriginalHealth(); a.health = 0F;
            f.runtime.playerHealth(a); assertTrue(f.snapshot().available());
            assertEquals(0F, f.snapshot().health(), 0F);
        }
    }
    @Test void negativeOriginalHealthRetainsExistingValidatorBehavior() {
        try(Fixture f = new Fixture()) {
            OriginalHealth a = new OriginalHealth(); a.health = -1F;
            f.runtime.playerHealth(a); assertTrue(f.snapshot().available());
            assertEquals(-1F, f.snapshot().health(), 0F);
        }
    }
    @Test void sameSourceNestedHealthDoesNotRepeatGetters() {
        try(Fixture f = new Fixture()) {
            OriginalHealth a = new OriginalHealth();
            a.onHealth = () -> f.runtime.playerHealth(a);
            f.runtime.playerHealth(a);
            assertTrue(f.snapshot().available());
            assertEquals(1,a.healthReads); assertEquals(1,a.maximumReads);
        }
    }
    @Test void sameSourceNestedMaximumDoesNotRepeatGetters() {
        try(Fixture f = new Fixture()) {
            OriginalHealth a = new OriginalHealth();
            a.onMaximum = () -> f.runtime.playerHealth(a);
            f.runtime.playerHealth(a);
            assertTrue(f.snapshot().available());
            assertEquals(1,a.healthReads); assertEquals(1,a.maximumReads);
        }
    }
    @Test void changedSourceDuringHealthClearsOldDataWithoutReadingMaximum() {
        try(Fixture f = new Fixture()) {
            f.runtime.playerHealth(new OriginalHealth());
            OriginalHealth a = new OriginalHealth(), b = new OriginalHealth();
            a.onHealth = () -> f.runtime.playerHealth(b);
            f.runtime.playerHealth(a);
            assertFalse(f.snapshot().available());
            assertEquals(0,a.maximumReads); assertEquals(0,b.healthReads);
        }
    }
    @Test void changedSourceDuringMaximumClearsPreviouslyValidState() {
        try(Fixture f = new Fixture()) {
            f.runtime.playerHealth(new OriginalHealth());
            OriginalHealth a = new OriginalHealth(), b = new OriginalHealth();
            a.onMaximum = () -> f.runtime.playerHealth(b);
            f.runtime.playerHealth(a);
            assertFalse(f.snapshot().available());
            assertEquals(0,b.healthReads);
        }
    }
    @Test void nullNativeReentryDuringHealthInvalidates() {
        try(Fixture f = new Fixture()) {
            f.runtime.playerHealth(new OriginalHealth());
            OriginalHealth a = new OriginalHealth();
            a.onHealth = () -> f.runtime.playerHealth(null);
            f.runtime.playerHealth(a); assertFalse(f.snapshot().available());
        }
    }
    @Test void nullNativeReentryDuringMaximumInvalidates() {
        try(Fixture f = new Fixture()) {
            f.runtime.playerHealth(new OriginalHealth());
            OriginalHealth a = new OriginalHealth();
            a.onMaximum = () -> f.runtime.playerHealth(null);
            f.runtime.playerHealth(a); assertFalse(f.snapshot().available());
        }
    }
    @Test void exceptionFromFirstOriginalGetterClearsOldEvidence() {
        try(Fixture f = new Fixture()) {
            f.runtime.playerHealth(new OriginalHealth());
            OriginalHealth a = new OriginalHealth(); a.failHealth = true;
            assertThrows(IllegalStateException.class, () -> f.runtime.playerHealth(a));
            assertFalse(f.snapshot().available());
        }
    }
    @Test void exceptionFromSecondOriginalGetterClearsOldEvidence() {
        try(Fixture f = new Fixture()) {
            f.runtime.playerHealth(new OriginalHealth());
            OriginalHealth a = new OriginalHealth(); a.failMaximum = true;
            assertThrows(IllegalStateException.class, () -> f.runtime.playerHealth(a));
            assertFalse(f.snapshot().available());
        }
    }
    @Test void errorFromFirstOriginalGetterPropagatesAndClears() {
        try(Fixture f = new Fixture()) {
            f.runtime.playerHealth(new OriginalHealth());
            OriginalHealth a = new OriginalHealth(); a.errorHealth = true;
            assertThrows(AssertionError.class, () -> f.runtime.playerHealth(a));
            assertFalse(f.snapshot().available());
        }
    }
    @Test void errorFromSecondOriginalGetterPropagatesAndClears() {
        try(Fixture f = new Fixture()) {
            f.runtime.playerHealth(new OriginalHealth());
            OriginalHealth a = new OriginalHealth(); a.errorMaximum = true;
            assertThrows(AssertionError.class, () -> f.runtime.playerHealth(a));
            assertFalse(f.snapshot().available());
        }
    }
    @Test void invalidNonfiniteHealthClearsPreviousEvidence() {
        try(Fixture f = new Fixture()) {
            f.runtime.playerHealth(new OriginalHealth());
            OriginalHealth a = new OriginalHealth(); a.health = Float.NaN;
            assertThrows(IllegalArgumentException.class, () -> f.runtime.playerHealth(a));
            assertFalse(f.snapshot().available());
        }
    }
    @Test void invalidNonfiniteMaximumClearsPreviousEvidence() {
        try(Fixture f = new Fixture()) {
            f.runtime.playerHealth(new OriginalHealth());
            OriginalHealth a = new OriginalHealth(); a.maximum = Float.POSITIVE_INFINITY;
            assertThrows(IllegalArgumentException.class, () -> f.runtime.playerHealth(a));
            assertFalse(f.snapshot().available());
        }
    }
    @Test void invalidZeroMaximumClearsPreviousEvidence() {
        try(Fixture f = new Fixture()) {
            f.runtime.playerHealth(new OriginalHealth());
            OriginalHealth a = new OriginalHealth(); a.maximum = 0F;
            assertThrows(IllegalArgumentException.class, () -> f.runtime.playerHealth(a));
            assertFalse(f.snapshot().available());
        }
    }
    @Test void originalNullClearsPreviouslyValidHealth() {
        try(Fixture f = new Fixture()) {
            f.runtime.playerHealth(new OriginalHealth());
            f.runtime.playerHealth(null); assertFalse(f.snapshot().available());
        }
    }
    @Test void freshOriginalAfterSourceRevocationRecovers() {
        try(Fixture f = new Fixture()) {
            OriginalHealth a = new OriginalHealth(), b = new OriginalHealth();
            a.onHealth = () -> f.runtime.playerHealth(b);
            f.runtime.playerHealth(a); assertFalse(f.snapshot().available());
            b.health = 7F; f.runtime.playerHealth(b);
            assertTrue(f.snapshot().available());
            assertEquals(7F, f.snapshot().health(), 0F);
        }
    }
    @Test void freshOriginalAfterExceptionRecovers() {
        try(Fixture f = new Fixture()) {
            OriginalHealth a = new OriginalHealth(); a.failMaximum = true;
            assertThrows(IllegalStateException.class, () -> f.runtime.playerHealth(a));
            OriginalHealth b = new OriginalHealth(); b.health = 3F;
            f.runtime.playerHealth(b); assertTrue(f.snapshot().available());
            assertEquals(3F,f.snapshot().health(),0F);
        }
    }
}