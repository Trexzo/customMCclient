package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.*;
import dev.trexzo.custommc.core.setting.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** V32 registration/ClickGUI descriptor and lifecycle rollback invariants. */
final class Minecraft189ScaffoldFeatureTest {
    @Test void registerToggleAndCloseOwnExactlyOneScaffoldModule() {
        ModuleRegistry modules=new ModuleRegistry();
        ModuleController controller=new ModuleController(modules);
        ModulePresentationRegistry presentations=new ModulePresentationRegistry();
        SettingRegistry settings = new SettingRegistry();
        SettingPresentationRegistry settingPresentations = new SettingPresentationRegistry();
        ModuleSettingRegistry moduleSettings = new ModuleSettingRegistry(modules, settings);
        Minecraft189ScaffoldFeature feature=Minecraft189ScaffoldFeature.install(
                modules,controller,presentations, moduleSettings,settings,settingPresentations);
        Minecraft189ScaffoldModule owned=feature.module();
        assertSame(owned,modules.find(Minecraft189ScaffoldModule.ID));
        ModuleDescriptor descriptor=presentations.find(Minecraft189ScaffoldModule.ID);
        assertNotNull(descriptor,"new Movement module must appear in ClickGUI");
        assertEquals("Scaffold",descriptor.displayName());
        assertFalse(feature.module().active(),"normal startup is disabled");
        assertEquals(Boolean.FALSE, owned.twoPlacementsSetting().get(),
                "two-block production mode must start disabled");
        assertEquals(Boolean.FALSE, owned.threePlacementsSetting().get(),
                "three-block production mode must start disabled");
        assertEquals(Boolean.FALSE, owned.fourPlacementsSetting().get(),
                "four-block production mode must start disabled");
        assertEquals(Boolean.FALSE, owned.adjacentEdgeTargetsSetting().get(),
                "source-native adjacent edge setting must default off");
        assertSame(owned.twoPlacementsSetting(),
                settings.find(Minecraft189ScaffoldModule.TWO_PLACEMENTS_SETTING_ID));
        assertNotNull(settingPresentations.find(
                Minecraft189ScaffoldModule.TWO_PLACEMENTS_SETTING_ID));
        assertSame(owned.threePlacementsSetting(),
                settings.find(Minecraft189ScaffoldModule.THREE_PLACEMENTS_SETTING_ID));
        assertNotNull(settingPresentations.find(
                Minecraft189ScaffoldModule.THREE_PLACEMENTS_SETTING_ID));
        assertSame(owned.fourPlacementsSetting(),
                settings.find(Minecraft189ScaffoldModule.FOUR_PLACEMENTS_SETTING_ID));
        assertNotNull(settingPresentations.find(
                Minecraft189ScaffoldModule.FOUR_PLACEMENTS_SETTING_ID));
        assertSame(owned.adjacentEdgeTargetsSetting(),
                settings.find(Minecraft189ScaffoldModule.ADJACENT_EDGE_SETTING_ID));
        assertNotNull(settingPresentations.find(
                Minecraft189ScaffoldModule.ADJACENT_EDGE_SETTING_ID));
        assertEquals(Minecraft189ScaffoldModule.ID,
                moduleSettings.ownerOf(Minecraft189ScaffoldModule.TWO_PLACEMENTS_SETTING_ID).moduleId());
        assertEquals(Minecraft189ScaffoldModule.ID,
                moduleSettings.ownerOf(Minecraft189ScaffoldModule.THREE_PLACEMENTS_SETTING_ID).moduleId());
        assertEquals(Minecraft189ScaffoldModule.ID,
                moduleSettings.ownerOf(Minecraft189ScaffoldModule.FOUR_PLACEMENTS_SETTING_ID).moduleId());
        assertEquals(Minecraft189ScaffoldModule.ID,
                moduleSettings.ownerOf(Minecraft189ScaffoldModule.ADJACENT_EDGE_SETTING_ID).moduleId());
        controller.enable(Minecraft189ScaffoldModule.ID);
        assertTrue(feature.module().active(),"controller must own enable");
        feature.close();
        assertFalse(modules.snapshot().stream()
                        .anyMatch(m -> Minecraft189ScaffoldModule.ID.equals(m.id())));
        assertFalse(owned.active());
        assertNull(presentations.find(Minecraft189ScaffoldModule.ID));
        assertNull(settings.find(Minecraft189ScaffoldModule.TWO_PLACEMENTS_SETTING_ID));
        assertNull(settingPresentations.find(
                Minecraft189ScaffoldModule.TWO_PLACEMENTS_SETTING_ID));
        assertNull(moduleSettings.ownerOf(Minecraft189ScaffoldModule.TWO_PLACEMENTS_SETTING_ID));
        assertNull(settings.find(Minecraft189ScaffoldModule.THREE_PLACEMENTS_SETTING_ID));
        assertNull(settingPresentations.find(
                Minecraft189ScaffoldModule.THREE_PLACEMENTS_SETTING_ID));
        assertNull(moduleSettings.ownerOf(Minecraft189ScaffoldModule.THREE_PLACEMENTS_SETTING_ID));
        assertNull(settings.find(Minecraft189ScaffoldModule.FOUR_PLACEMENTS_SETTING_ID));
        assertNull(settingPresentations.find(
                Minecraft189ScaffoldModule.FOUR_PLACEMENTS_SETTING_ID));
        assertNull(moduleSettings.ownerOf(Minecraft189ScaffoldModule.FOUR_PLACEMENTS_SETTING_ID));
        assertNull(settings.find(Minecraft189ScaffoldModule.ADJACENT_EDGE_SETTING_ID));
        assertNull(settingPresentations.find(
                Minecraft189ScaffoldModule.ADJACENT_EDGE_SETTING_ID));
        assertNull(moduleSettings.ownerOf(Minecraft189ScaffoldModule.ADJACENT_EDGE_SETTING_ID));
        feature.close();
    }


    @Test void optInQuotaIsFrozenPerActivationAndConflictingSettingsFailClosed() {
        Minecraft189ScaffoldModule module = new Minecraft189ScaffoldModule();
        assertFalse(module.active());
        module.onEnable();
        assertTrue(module.armed(), "default stays a single placement");
        assertEquals(0, module.productionRepeatCountForActivation());
        module.onDisable();

        module.twoPlacementsSetting().set(Boolean.TRUE);
        module.onEnable();
        assertTrue(module.productionRepeatEnabledForActivation());
        assertEquals(2, module.productionRepeatCountForActivation());
        module.onDisable();

        module.twoPlacementsSetting().set(Boolean.FALSE);
        module.threePlacementsSetting().set(Boolean.TRUE);
        module.onEnable();
        assertTrue(module.productionRepeatEnabledForActivation());
        assertEquals(3, module.productionRepeatCountForActivation());
        module.twoPlacementsSetting().set(Boolean.TRUE);
        assertEquals(3, module.productionRepeatCountForActivation(),
                "mid-activation UI changes must never escalate quota");
        module.onDisable();

        module.onEnable();
        assertTrue(module.active());
        assertFalse(module.armed(),
                "simultaneously selected modes must not grant a one-shot fallback");
        assertFalse(module.productionRepeatEnabledForActivation());
        assertEquals(0, module.productionRepeatCountForActivation());
        module.onDisable();
        module.twoPlacementsSetting().set(Boolean.FALSE);
        module.onEnable();
        assertTrue(module.armed(), "fresh explicit activation restores valid mode");
        assertEquals(3, module.productionRepeatCountForActivation());
        module.onDisable();

        module.threePlacementsSetting().set(Boolean.FALSE);
        module.fourPlacementsSetting().set(Boolean.TRUE);
        module.onEnable();
        assertTrue(module.productionRepeatEnabledForActivation());
        assertEquals(4, module.productionRepeatCountForActivation());
        module.fourPlacementsSetting().set(Boolean.FALSE);
        assertEquals(4, module.productionRepeatCountForActivation(),
                "changing quota mid-activation never downgrades/falls back");
        module.onDisable();

        module.fourPlacementsSetting().set(Boolean.TRUE);
        module.twoPlacementsSetting().set(Boolean.TRUE);
        module.onEnable();
        assertTrue(module.active());
        assertFalse(module.armed(), "two plus four must not grant one-shot");
        assertFalse(module.productionRepeatEnabledForActivation());
        assertEquals(0, module.productionRepeatCountForActivation());
        module.onDisable();

        module.twoPlacementsSetting().set(Boolean.FALSE);
        module.threePlacementsSetting().set(Boolean.TRUE);
        module.onEnable();
        assertFalse(module.armed(), "three plus four also conflicts");
        assertFalse(module.productionRepeatEnabledForActivation());
        module.onDisable();

        module.threePlacementsSetting().set(Boolean.FALSE);
        module.onEnable();
        assertTrue(module.armed());
        assertEquals(4, module.productionRepeatCountForActivation());
    }

    @Test void adjacentEdgeOnlyIsNeverAOneShotOrAutomaticRepeatGrant() {
        final Minecraft189ScaffoldModule module = new Minecraft189ScaffoldModule();
        module.adjacentEdgeTargetsSetting().set(Boolean.TRUE);
        module.onEnable();
        assertTrue(module.active());
        assertFalse(module.armed(),
                "edge toggle alone is invalid, no hidden one-shot fallback");
        assertFalse(module.productionRepeatEnabledForActivation());
        module.onDisable();

        module.twoPlacementsSetting().set(Boolean.TRUE);
        module.onEnable();
        assertTrue(module.productionRepeatEnabledForActivation());
        assertEquals(2, module.productionRepeatCountForActivation());
        assertFalse(module.armed(), "repeat mode takes the single-use arm");
        module.adjacentEdgeTargetsSetting().set(Boolean.FALSE);
        assertEquals(2, module.productionRepeatCountForActivation(),
                "live toggle may never silently restore underfoot authority");
        module.onDisable();
        module.adjacentEdgeTargetsSetting().set(Boolean.FALSE);
        module.onEnable();
        assertTrue(module.productionRepeatEnabledForActivation());
        assertEquals(2, module.productionRepeatCountForActivation());
        module.onDisable();
        module.twoPlacementsSetting().set(Boolean.FALSE);
        module.onEnable();
        assertTrue(module.armed(), "default one-shot restored only after opt-ins off");
    }

    @Test void duplicatePresentationFailsWithoutLeavingHiddenModuleRegistered() {
        ModuleRegistry modules=new ModuleRegistry();
        ModuleController controller=new ModuleController(modules);
        ModulePresentationRegistry presentations=new ModulePresentationRegistry();
        SettingRegistry settings = new SettingRegistry();
        SettingPresentationRegistry settingPresentations = new SettingPresentationRegistry();
        ModuleSettingRegistry moduleSettings = new ModuleSettingRegistry(modules, settings);
        ModuleDescriptor reserved=new ModuleDescriptor(
                Minecraft189ScaffoldModule.ID,"reserved","Test collision",
                Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID,63);
        ModulePresentationRegistry.Registration prior=presentations.register(reserved);
        try {
            assertThrows(IllegalArgumentException.class, () ->
                    Minecraft189ScaffoldFeature.install(modules,controller,presentations,
                            moduleSettings,settings,settingPresentations));
            assertNull(modules.find(Minecraft189ScaffoldModule.ID),
                    "failed install must roll back its module registration");
            assertSame(reserved,presentations.find(Minecraft189ScaffoldModule.ID));
            assertNull(settings.find(Minecraft189ScaffoldModule.TWO_PLACEMENTS_SETTING_ID));
            assertNull(moduleSettings.ownerOf(Minecraft189ScaffoldModule.TWO_PLACEMENTS_SETTING_ID));
            assertNull(settings.find(Minecraft189ScaffoldModule.THREE_PLACEMENTS_SETTING_ID));
            assertNull(moduleSettings.ownerOf(Minecraft189ScaffoldModule.THREE_PLACEMENTS_SETTING_ID));
            assertNull(settings.find(Minecraft189ScaffoldModule.FOUR_PLACEMENTS_SETTING_ID));
            assertNull(moduleSettings.ownerOf(Minecraft189ScaffoldModule.FOUR_PLACEMENTS_SETTING_ID));
            assertNull(settings.find(Minecraft189ScaffoldModule.ADJACENT_EDGE_SETTING_ID));
            assertNull(moduleSettings.ownerOf(Minecraft189ScaffoldModule.ADJACENT_EDGE_SETTING_ID));
        } finally {
            prior.close();
        }
    }
}
