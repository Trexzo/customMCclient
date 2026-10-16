package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Opt-in V96 native Mojang 1.8.9 collision-field acceptance.
 * The unmodified game's transformed local player executes the normal
 * production NoClip callback. The separate X11 harness opens/closes the
 * original CustomMC GUI through genuine physical Right Shift input.
 * Neither this witness nor the Python harness injects a noClip setter.
 */
final class Minecraft189OriginalClickGuiNoClipAcceptance {
    private static final String PROPERTY =
            "custommc.acceptance.requireNativeNoClipGuiFocus";

    // 0 = acquire original disabled baseline; 1 = wait for real GUI open;
    // 2 = wait for real GUI close; 3 = verified native disable.
    private static int phase;
    private static int baselineCallbacks;

    private Minecraft189OriginalClickGuiNoClipAcceptance() {
    }

    static synchronized void observe(
            final Minecraft189HostRuntime host,
            final Minecraft189PlayerNoClipControl originalPlayer) {
        if (!Boolean.getBoolean(PROPERTY)
                || phase == 3
                || !host.playerMovementState().snapshot().available()) {
            return;
        }
        final boolean guiOpen = host.clickGuiRuntime().coreRuntime()
                .model().snapshot().open();
        final boolean nativeNoClip = originalPlayer.customMcNoClip();
        final Minecraft189NoClipModule module = host.featureCatalog().noClip();

        if (phase == 0) {
            if (guiOpen || module.active() || nativeNoClip) {
                throw new IllegalStateException(
                        "V96 original NoClip baseline must be false, disabled and GUI closed");
            }
            if (++baselineCallbacks >= 12) {
                host.featureCatalog().enableNoClipForOriginalGuiAcceptance();
                // Only production NoClip applies a real native setter here.
                host.playerNoClipControl(originalPlayer);
                requireState(originalPlayer, true,
                        "original module-enable native noClip true");
                phase = 1;
                report("CUSTOMMC_OFFICIAL_189_NOCLIP_NATIVE_MODULE_ARMED_PASS=YES");
            }
            return;
        }

        if (phase == 1) {
            if (!module.active()) {
                throw new IllegalStateException(
                        "V96 original NoClip was disabled before GUI focus");
            }
            requireState(originalPlayer, !guiOpen,
                    "original NoClip GUI-open collision restoration");
            if (guiOpen) {
                phase = 2;
                report("CUSTOMMC_OFFICIAL_189_NOCLIP_GUI_REAL_KEY_OPEN_NATIVE_FALSE_PASS=YES");
            }
            return;
        }

        if (phase == 2) {
            if (!module.active()) {
                throw new IllegalStateException(
                        "V96 original NoClip was disabled before GUI close");
            }
            requireState(originalPlayer, !guiOpen,
                    "original NoClip GUI-close collision reacquisition");
            if (!guiOpen) {
                report("CUSTOMMC_OFFICIAL_189_NOCLIP_GUI_REAL_KEY_CLOSE_NATIVE_TRUE_PASS=YES");
                host.featureCatalog().disableNoClipForOriginalGuiAcceptance();
                host.playerNoClipControl(originalPlayer);
                requireState(originalPlayer, false,
                        "original module-disable collision restoration");
                phase = 3;
                report("CUSTOMMC_OFFICIAL_189_NOCLIP_GUI_REAL_KEY_DISABLE_NATIVE_FALSE_PASS=YES");
            }
        }
    }

    private static void requireState(
            final Minecraft189PlayerNoClipControl player,
            final boolean expected,
            final String stage) {
        final boolean actual = player.customMcNoClip();
        if (actual != expected) {
            throw new IllegalStateException(
                    "V96 " + stage + " expected=" + expected
                            + " actual=" + actual);
        }
    }

    private static void report(final String marker) {
        System.out.println(marker);
        System.out.flush();
    }
}
