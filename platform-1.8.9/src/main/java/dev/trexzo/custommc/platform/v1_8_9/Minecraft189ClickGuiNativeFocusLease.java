package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Original Minecraft 1.8.9 UI-only cursor ownership transition.
 *
 * The existing physical mouse guard is deliberately unchanged: gameplay
 * capture must NEVER be interpreted as a GUI-ready pointer. Releasing it
 * happens only through ave.o() (the genuine 1.8.9 vanilla method), not by
 * spoofing Mouse.isGrabbed or inGameHasFocus. The same owned lease restores
 * vanilla input through ave.n() when the overlay actually closes.
 */
final class Minecraft189ClickGuiNativeFocusLease {
    enum Decision {
        NONE, ALREADY_READY, RELEASE_ORIGINAL, RESTORE_ORIGINAL, REJECT_OPEN
    }

    private boolean ownsGuiFocus;

    void afterKeyTransition(final ClickGuiModel model,
            final boolean previouslyOpen) {
        Objects.requireNonNull(model, "model");
        final boolean nowOpen = model.snapshot().open();
        if (previouslyOpen == nowOpen) {
            return;
        }

        // Core/mock-host regression suites run with no actual X11 Display.
        // Never invent native focus state in that environment.
        final boolean displayCreated;
        try {
            displayCreated = Display.isCreated();
        } catch (LinkageError unavailable) {
            return;
        }
        if (!displayCreated) {
            return;
        }

        final Minecraft189OverlayFocusGuardian.Snapshot nativeState =
                Minecraft189OverlayFocusGuardian.observeNative();
        final boolean mouseCreated;
        final boolean grabbed;
        try {
            mouseCreated = Mouse.isCreated();
            grabbed = mouseCreated && Mouse.isGrabbed();
        } catch (LinkageError unavailable) {
            if (nowOpen) {
                model.close();
            }
            return;
        }

        final Decision action = decide(
                previouslyOpen, nowOpen, true, nativeState,
                mouseCreated, grabbed, ownsGuiFocus);
        // Record actual original native state at the transition, never
        // guess why a graphical focus check failed. This is diagnostic
        // evidence, not acceptance or permission to forward mouse clicks.
        if (Boolean.getBoolean("custommc.acceptance.reportNativeVisualPage")) {
            System.out.println("CUSTOMMC_189_NATIVE_GUI_FOCUS_OBSERVED="
                    + "available:" + nativeState.available()
                    + ",vanilla:" + nativeState.vanillaScreen()
                    + ",gameplay:" + nativeState.gameplayFocus()
                    + ",mouse:" + mouseCreated
                    + ",grabbed:" + grabbed
                    + ",active:" + Display.isActive()
                    + ",inside:" + (mouseCreated && Mouse.isInsideWindow())
                    + ",action:" + action.name());
        }
        if (action == Decision.REJECT_OPEN) {
            model.close();
            ownsGuiFocus = false;
            System.out.println("CUSTOMMC_189_NATIVE_GUI_FOCUS_REJECTED=YES");
            return;
        }
        if (action == Decision.ALREADY_READY) {
            // This is an observed original ungrabbed GUI state, not an
            // invented release. Closing the overlay must still return
            // ownership to the original game through its own method.
            ownsGuiFocus = true;
            System.out.println("CUSTOMMC_189_NATIVE_GUI_CURSOR_READY=YES");
            return;
        }
        if (action == Decision.RELEASE_ORIGINAL) {
            if (!invokeNative(
                    Minecraft189Mappings.MINECRAFT_SET_INGAME_NOT_IN_FOCUS)) {
                model.close();
                System.out.println("CUSTOMMC_189_NATIVE_GUI_FOCUS_RELEASE_FAILED=YES");
                return;
            }
            final Minecraft189OverlayFocusGuardian.Snapshot ready =
                    Minecraft189OverlayFocusGuardian.observeNative();
            if (Minecraft189OverlayFocusGuardian.classify(ready)
                    != Minecraft189OverlayFocusGuardian.Mode.GUI_READY
                    || !Mouse.isCreated() || Mouse.isGrabbed()) {
                model.close();
                // Best-effort genuine Minecraft rollback; do not bypass the
                // existing original-physical mouse guard on failure.
                invokeNative(Minecraft189Mappings.MINECRAFT_SET_INGAME_FOCUS);
                System.out.println("CUSTOMMC_189_NATIVE_GUI_FOCUS_VERIFY_FAILED=YES");
                return;
            }
            ownsGuiFocus = true;
            System.out.println("CUSTOMMC_189_NATIVE_GUI_CURSOR_RELEASE=YES");
            return;
        }
        if (action == Decision.RESTORE_ORIGINAL) {
            ownsGuiFocus = false;
            if (Display.isActive()
                    && invokeNative(Minecraft189Mappings.MINECRAFT_SET_INGAME_FOCUS)) {
                final Minecraft189OverlayFocusGuardian.Snapshot restored =
                        Minecraft189OverlayFocusGuardian.observeNative();
                if (Minecraft189OverlayFocusGuardian.classify(restored)
                        == Minecraft189OverlayFocusGuardian.Mode.GAMEPLAY_CAPTURED
                        && Mouse.isCreated() && Mouse.isGrabbed()) {
                    System.out.println("CUSTOMMC_189_NATIVE_GUI_CURSOR_RESTORE=YES");
                } else {
                    System.out.println("CUSTOMMC_189_NATIVE_GUI_FOCUS_RESTORE_VERIFY_FAILED=YES");
                }
            } else {
                System.out.println("CUSTOMMC_189_NATIVE_GUI_FOCUS_RESTORE_FAILED=YES");
            }
            return;
        }

        // Another original vanilla GUI or loss of X11 focus wins.
        // Never try to grab the mouse on its behalf.
        if (!nowOpen) {
            ownsGuiFocus = false;
        }
    }

    static Decision decide(
            final boolean wasOpen, final boolean nowOpen,
            final boolean displayCreated,
            final Minecraft189OverlayFocusGuardian.Snapshot nativeState,
            final boolean mouseCreated, final boolean grabbed,
            final boolean ownsGuiFocus) {
        Objects.requireNonNull(nativeState, "nativeState");
        if (wasOpen == nowOpen || !displayCreated) {
            return Decision.NONE;
        }
        if (nowOpen) {
            if (!nativeState.available() || nativeState.vanillaScreen()
                    || !mouseCreated) {
                return Decision.REJECT_OPEN;
            }
            // The game can be observed between mouse-grab and native
            // inGameHasFocus updates. If either says gameplay still owns
            // the cursor, ask *vanilla* to release; do not make it usable
            // until the strict post-readback confirms GUI_READY/ungrabbed.
            if (nativeState.gameplayFocus() || grabbed) {
                return Decision.RELEASE_ORIGINAL;
            }
            return Decision.ALREADY_READY;
        }
        return ownsGuiFocus && nativeState.available()
                && !nativeState.vanillaScreen()
                ? Decision.RESTORE_ORIGINAL : Decision.NONE;
    }

    private static boolean invokeNative(
            final Minecraft189Mappings.MappedMethod target) {
        try {
            final Class<?> original =
                    Class.forName(
                            Minecraft189Mappings.MINECRAFT.obfuscatedInternalName()
                                    .replace('/', '.'),
                            false, Minecraft189ClickGuiNativeFocusLease.class.getClassLoader());
            final Method get = original.getDeclaredMethod(
                    Minecraft189Mappings.MINECRAFT_GET_MINECRAFT.obfuscatedName());
            get.setAccessible(true);
            final Object minecraft = get.invoke(null);
            if (minecraft == null) {
                return false;
            }
            final Method focus = original.getDeclaredMethod(
                    target.obfuscatedName());
            focus.setAccessible(true);
            focus.invoke(minecraft);
            return true;
        } catch (ReflectiveOperationException unavailable) {
            return false;
        } catch (SecurityException unavailable) {
            return false;
        } catch (LinkageError unavailable) {
            return false;
        }
    }
}
