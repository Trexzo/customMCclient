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
        NONE, RELEASE_ORIGINAL, RESTORE_ORIGINAL, REJECT_OPEN
    }

    private boolean ownsRelease;

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
                mouseCreated, grabbed, ownsRelease);
        if (action == Decision.REJECT_OPEN) {
            model.close();
            System.out.println("CUSTOMMC_189_NATIVE_GUI_FOCUS_REJECTED=YES");
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
            ownsRelease = true;
            System.out.println("CUSTOMMC_189_NATIVE_GUI_CURSOR_RELEASE=YES");
            return;
        }
        if (action == Decision.RESTORE_ORIGINAL) {
            ownsRelease = false;
            if (Display.isActive()
                    && invokeNative(Minecraft189Mappings.MINECRAFT_SET_INGAME_FOCUS)) {
                System.out.println("CUSTOMMC_189_NATIVE_GUI_CURSOR_RESTORE=YES");
            }
            return;
        }

        // A genuine vanilla GuiScreen retains its own input ownership.
        if (!nowOpen && ownsRelease && nativeState.vanillaScreen()) {
            ownsRelease = false;
        }
    }

    static Decision decide(
            final boolean wasOpen, final boolean nowOpen,
            final boolean displayCreated,
            final Minecraft189OverlayFocusGuardian.Snapshot nativeState,
            final boolean mouseCreated, final boolean grabbed,
            final boolean ownsRelease) {
        Objects.requireNonNull(nativeState, "nativeState");
        if (wasOpen == nowOpen || !displayCreated) {
            return Decision.NONE;
        }
        if (nowOpen) {
            if (!nativeState.available() || nativeState.vanillaScreen()
                    || !mouseCreated) {
                return Decision.REJECT_OPEN;
            }
            if (nativeState.gameplayFocus()) {
                return grabbed ? Decision.RELEASE_ORIGINAL
                        : Decision.REJECT_OPEN;
            }
            return !grabbed ? Decision.NONE : Decision.REJECT_OPEN;
        }
        return ownsRelease && nativeState.available()
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
