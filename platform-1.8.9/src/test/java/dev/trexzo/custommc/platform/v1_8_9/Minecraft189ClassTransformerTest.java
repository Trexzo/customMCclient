package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.bootstrap.BootstrapContext;
import dev.trexzo.custommc.bootstrap.TransformingTargetClassLoader;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189ClassTransformerTest {
    @Test
    void transformedMinecraftMainCallsParentOwnedRuntimeBridge()
            throws Exception {
        final Minecraft189BootstrapRuntime runtime =
                Minecraft189BootstrapRuntime.create(
                        new BootstrapContext(
                                Minecraft189ClassTransformer
                                        .TARGET_MAIN_CLASS,
                                new String[0]));

        final URL fixtureLocation =
                net.minecraft.client.main.Main.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation();
        final TransformingTargetClassLoader loader =
                new TransformingTargetClassLoader(
                        new URL[]{fixtureLocation},
                        getClass().getClassLoader(),
                        new Minecraft189ClassTransformer());

        try {
            assertFalse(
                    runtime.targetMainEntered());

            final Class<?> transformedMain =
                    loader.loadClass(
                            Minecraft189ClassTransformer
                                    .TARGET_MAIN_CLASS);

            assertSame(
                    loader,
                    transformedMain.getClassLoader());

            final Method main =
                    transformedMain.getMethod(
                            "main",
                            String[].class);
            main.invoke(
                    null,
                    (Object) new String[]{
                            "--username",
                            "Player One"
                    });

            assertTrue(
                    runtime.targetMainEntered());
            assertEquals(
                    1,
                    transformedMain
                            .getField("invocations")
                            .getInt(null));
        } finally {
            loader.close();
            runtime.close();
        }

        assertFalse(
                Minecraft189RuntimeBridge.active());
    }

    @Test
    void bootstrapRuntimeSelectsAndCachesTransformingLoader() {
        final Minecraft189BootstrapRuntime runtime =
                Minecraft189BootstrapRuntime.create(
                        new BootstrapContext(
                                Minecraft189ClassTransformer
                                        .TARGET_MAIN_CLASS,
                                new String[0]));

        try {
            final ClassLoader parent =
                    getClass().getClassLoader();
            final ClassLoader first =
                    runtime.targetClassLoader(
                            parent);
            final ClassLoader second =
                    runtime.targetClassLoader(
                            parent);

            assertTrue(
                    first instanceof TransformingTargetClassLoader);
            assertSame(
                    first,
                    second);
            assertSame(
                    parent,
                    first.getParent());
        } finally {
            runtime.close();
        }
    }
}
