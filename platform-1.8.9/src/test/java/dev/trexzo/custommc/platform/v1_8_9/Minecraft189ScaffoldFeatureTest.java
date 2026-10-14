package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** V32 registration/ClickGUI descriptor and lifecycle rollback invariants. */
final class Minecraft189ScaffoldFeatureTest {
    @Test void registerToggleAndCloseOwnExactlyOneScaffoldModule() {
        ModuleRegistry modules=new ModuleRegistry();
        ModuleController controller=new ModuleController(modules);
        ModulePresentationRegistry presentations=new ModulePresentationRegistry();
        Minecraft189ScaffoldFeature feature=Minecraft189ScaffoldFeature.install(
                modules,controller,presentations);
        Minecraft189ScaffoldModule owned=feature.module();
        assertSame(owned,modules.find(Minecraft189ScaffoldModule.ID));
        ModuleDescriptor descriptor=presentations.find(Minecraft189ScaffoldModule.ID);
        assertNotNull(descriptor,"new Movement module must appear in ClickGUI");
        assertEquals("Scaffold",descriptor.displayName());
        assertFalse(feature.module().active(),"normal startup is disabled");
        controller.enable(Minecraft189ScaffoldModule.ID);
        assertTrue(feature.module().active(),"controller must own enable");
        feature.close();
        assertFalse(modules.snapshot().stream()
                        .anyMatch(m -> Minecraft189ScaffoldModule.ID.equals(m.id())));
        assertFalse(owned.active());
        assertNull(presentations.find(Minecraft189ScaffoldModule.ID));
        feature.close();
    }

    @Test void duplicatePresentationFailsWithoutLeavingHiddenModuleRegistered() {
        ModuleRegistry modules=new ModuleRegistry();
        ModuleController controller=new ModuleController(modules);
        ModulePresentationRegistry presentations=new ModulePresentationRegistry();
        ModuleDescriptor reserved=new ModuleDescriptor(
                Minecraft189ScaffoldModule.ID,"reserved","Test collision",
                Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID,63);
        ModulePresentationRegistry.Registration prior=presentations.register(reserved);
        try {
            assertThrows(IllegalArgumentException.class, () ->
                    Minecraft189ScaffoldFeature.install(modules,controller,presentations));
            assertNull(modules.find(Minecraft189ScaffoldModule.ID),
                    "failed install must roll back its module registration");
            assertSame(reserved,presentations.find(Minecraft189ScaffoldModule.ID));
        } finally {
            prior.close();
        }
    }
}
