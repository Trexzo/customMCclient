package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Read-only native 1.8.9 ownership evidence for the ClickGUI overlay.
 * Never invokes Minecraft's focus setters or touches game/world state.
 *
 * A real vanilla GuiScreen takes precedence over a CustomMC overlay. Mouse
 * actions are not allowed while vanilla still owns the gameplay cursor.
 */
public final class Minecraft189OverlayFocusGuardian {
    private Minecraft189OverlayFocusGuardian() { }

    public enum Mode {
        UNKNOWN,
        GAMEPLAY_CAPTURED,
        GUI_READY,
        VANILLA_SCREEN
    }

    public static final class Snapshot {
        private final boolean available;
        private final boolean vanillaScreen;
        private final boolean gameplayFocus;

        private Snapshot(final boolean available,
                final boolean vanillaScreen,
                final boolean gameplayFocus) {
            this.available = available;
            this.vanillaScreen = vanillaScreen;
            this.gameplayFocus = gameplayFocus;
        }

        public static Snapshot unknown() {
            return new Snapshot(false, false, false);
        }

        public static Snapshot known(final boolean vanillaScreen,
                final boolean gameplayFocus) {
            return new Snapshot(true, vanillaScreen, gameplayFocus);
        }

        public boolean available() { return available; }
        public boolean vanillaScreen() { return vanillaScreen; }
        public boolean gameplayFocus() { return gameplayFocus; }
    }

    public static Mode classify(final Snapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        if (!snapshot.available()) {
            return Mode.UNKNOWN;
        }
        if (snapshot.vanillaScreen()) {
            return Mode.VANILLA_SCREEN;
        }
        return snapshot.gameplayFocus()
                ? Mode.GAMEPLAY_CAPTURED : Mode.GUI_READY;
    }

    /**
     * Returns true if the overlay can keep owning GUI input. A real vanilla
     * screen always wins and forces the overlay to close.
     */
    public static boolean reconcile(
            final ClickGuiModel model, final Snapshot snapshot) {
        Objects.requireNonNull(model, "model");
        final Mode mode = classify(snapshot);
        if (mode == Mode.VANILLA_SCREEN && model.snapshot().open()) {
            model.close();
        }
        return model.snapshot().open();
    }

    /**
     * Safe for mock-host tests, whose original class 'ave' is not loaded.
     * Production native checks must still enforce native LWJGL mouse state.
     */
    public static boolean permitsMouseInput(final Snapshot snapshot) {
        final Mode mode = classify(snapshot);
        return mode != Mode.VANILLA_SCREEN
                && mode != Mode.GAMEPLAY_CAPTURED;
    }

    /**
     * Never interpret missing original-game evidence as permission to
     * interact with a real graphical Minecraft client. Mock-host tests
     * without a live Display retain their existing source-path semantics.
     */
    public static boolean permitsPhysicalMouse(final Snapshot snapshot,
            final boolean displayCreated,
            final boolean mouseCreated,
            final boolean grabbed,
            final boolean insideWindow) {
        if (!permitsMouseInput(snapshot)) {
            return false;
        }
        if (!displayCreated) {
            return true;
        }
        return snapshot.available() && mouseCreated
                && !grabbed && insideWindow;
    }

    public static boolean permitsProductionMouseInput(
            final Snapshot snapshot) {
        try {
            if (!org.lwjgl.opengl.Display.isCreated()) {
                return permitsPhysicalMouse(
                        snapshot, false, false, false, false);
            }
            if (!org.lwjgl.input.Mouse.isCreated()) {
                return false;
            }
            return permitsPhysicalMouse(
                    snapshot, true, true,
                    org.lwjgl.input.Mouse.isGrabbed(),
                    org.lwjgl.input.Mouse.isInsideWindow());
        } catch (LinkageError nativeLibraryUnavailable) {
            // Unit/mock-host runs intentionally have no LWJGL JNI. Their
            // GUI input bridge must remain independently testable.
            // A true original game cannot render without this library.
            return permitsPhysicalMouse(
                    snapshot, false, false, false, false);
        }
    }

    /**
     * Read the original 1.8.9 game fields through the exact mapped class.
     * Try our defining loader first and the live Minecraft thread-context
     * loader second: some original-game launchers isolate transformed classes
     * from the client overlay loader. No non-original substitute is accepted.
     */
    public static Snapshot observeNative() {
        final ClassLoader owned =
                Minecraft189OverlayFocusGuardian.class.getClassLoader();
        final ClassLoader context = Thread.currentThread().getContextClassLoader();
        final Snapshot primary = tryNativeRead(owned, "overlay-loader");
        if (primary.available()) {
            return primary;
        }
        if (context != null && context != owned) {
            final Snapshot secondary = tryNativeRead(context, "thread-context-loader");
            if (secondary.available()) {
                return secondary;
            }
        }
        return Snapshot.unknown();
    }

    static Object findOriginalMinecraftInstance() {
        final ClassLoader owned =
                Minecraft189OverlayFocusGuardian.class.getClassLoader();
        Object instance = tryOriginalInstance(owned, "overlay-loader");
        if (instance != null) {
            return instance;
        }
        final ClassLoader context = Thread.currentThread().getContextClassLoader();
        return context == null || context == owned
                ? null : tryOriginalInstance(context, "thread-context-loader");
    }

    private static Snapshot tryNativeRead(
            final ClassLoader loader, final String label) {
        final Object instance = tryOriginalInstance(loader, label);
        if (instance == null) {
            return Snapshot.unknown();
        }
        try {
            final Class<?> minecraft = instance.getClass();
            final Field screen = minecraft.getDeclaredField(
                    Minecraft189Mappings.MINECRAFT_CURRENT_SCREEN.obfuscatedName());
            final Field gameplay = minecraft.getDeclaredField(
                    Minecraft189Mappings.MINECRAFT_INGAME_HAS_FOCUS.obfuscatedName());
            screen.setAccessible(true);
            gameplay.setAccessible(true);
            return Snapshot.known(
                    screen.get(instance) != null,
                    gameplay.getBoolean(instance));
        } catch (ReflectiveOperationException unavailable) {
            diagnose("native-field", label, unavailable);
        } catch (LinkageError unavailable) {
            diagnose("native-field", label, unavailable);
        } catch (SecurityException unavailable) {
            diagnose("native-field", label, unavailable);
        }
        return Snapshot.unknown();
    }

    private static Object tryOriginalInstance(
            final ClassLoader loader, final String label) {
        try {
            final Class<?> minecraft = Class.forName(
                    Minecraft189Mappings.MINECRAFT.obfuscatedInternalName()
                            .replace('/', '.'), false, loader);
            final Method get = minecraft.getDeclaredMethod(
                    Minecraft189Mappings.MINECRAFT_GET_MINECRAFT.obfuscatedName());
            get.setAccessible(true);
            final Object instance = get.invoke(null);
            if (instance == null) {
                diagnose("native-singleton", label, "null");
            } else if (instance.getClass() != minecraft) {
                diagnose("native-singleton", label, "wrong-class");
                return null;
            }
            return instance;
        } catch (ReflectiveOperationException unavailable) {
            diagnose("native-instance", label, unavailable);
        } catch (LinkageError unavailable) {
            diagnose("native-instance", label, unavailable);
        } catch (SecurityException unavailable) {
            diagnose("native-instance", label, unavailable);
        }
        return null;
    }

    private static void diagnose(
            final String stage, final String loader, final Throwable error) {
        diagnose(stage, loader, error.getClass().getSimpleName()
                + ":" + String.valueOf(error.getMessage()));
    }

    private static void diagnose(
            final String stage, final String loader, final String cause) {
        if (Boolean.getBoolean("custommc.acceptance.reportNativeVisualPage")) {
            final String safe = cause.length() <= 130
                    ? cause : cause.substring(0, 130);
            System.out.println("CUSTOMMC_189_NATIVE_FOCUS_READ_FAILURE="
                    + stage + ":" + loader + ":" + safe);
        }
    }

}
