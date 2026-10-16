package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Opt-in V94 real Mojang 1.8.9 game acceptance. The original transformed
 * player invokes the production step callback. The separate X11 harness
 * presses genuine Right Shift through the original Minecraft keyboard path.
 * No synthetic ClickGUI toggle or synthetic player object is used here.
 */
final class Minecraft189OriginalClickGuiStepAcceptance {
    private static final String PROPERTY =
            "custommc.acceptance.requireNativeStepGuiFocus";
    private static final float VANILLA = Minecraft189StepModule.VANILLA_STEP_HEIGHT;
    private static final float BOOST = 1.8F;
    private static final float TOLERANCE = 0.0001F;

    private static int phase;
    private static int baselineCallbacks;

    private Minecraft189OriginalClickGuiStepAcceptance() {
    }

    static synchronized void observe(
            final Minecraft189HostRuntime host,
            final Minecraft189PlayerStepControl originalPlayer) {
        if (!Boolean.getBoolean(PROPERTY) || phase == 3
                || !host.playerMovementState().snapshot().available()) {
            return;
        }
        final boolean guiOpen = host.clickGuiRuntime()
                .coreRuntime().model().snapshot().open();
        final float observed = originalPlayer.customMcStepHeight();
        if (!Float.isFinite(observed)) {
            throw new IllegalStateException(
                    "V94 original Mojang player stepHeight is nonfinite");
        }
        final Minecraft189StepModule step = host.featureCatalog().step();

        if (phase == 0) {
            if (guiOpen || step.active()) {
                throw new IllegalStateException(
                        "V94 original Step baseline must be disabled and GUI closed");
            }
            requireStep(observed, VANILLA,
                    "disabled source-native baseline");
            if (++baselineCallbacks >= 12) {
                host.featureCatalog().enableStepForOriginalGuiAcceptance();
                // The same actual player object receives the real production
                // callback immediately after normal module enable. We read
                // the transformed original field; no test-owned step setter.
                host.playerStepControl(originalPlayer);
                requireStep(originalPlayer.customMcStepHeight(), BOOST,
                        "source-native Step enabled");
                phase = 1;
                report("CUSTOMMC_OFFICIAL_189_STEP_NATIVE_MODULE_ARMED_PASS=YES");
            }
            return;
        }

        if (phase == 1) {
            if (!step.active()) {
                throw new IllegalStateException(
                        "V94 source Step was disabled before UI focus");
            }
            requireStep(observed, guiOpen ? VANILLA : BOOST,
                    "original GUI-open Step restoration");
            if (guiOpen) {
                phase = 2;
                report("CUSTOMMC_OFFICIAL_189_STEP_GUI_REAL_KEY_OPEN_NATIVE_VANILLA_PASS=YES");
            }
            return;
        }

        if (phase == 2) {
            if (!step.active()) {
                throw new IllegalStateException(
                        "V94 Step unexpectedly disabled before UI close");
            }
            requireStep(observed, guiOpen ? VANILLA : BOOST,
                    "original GUI-close Step reactivation");
            if (!guiOpen) {
                report("CUSTOMMC_OFFICIAL_189_STEP_GUI_REAL_KEY_CLOSE_NATIVE_BOOST_PASS=YES");
                host.featureCatalog().disableStepForOriginalGuiAcceptance();
                host.playerStepControl(originalPlayer);
                requireStep(originalPlayer.customMcStepHeight(), VANILLA,
                        "normal original native module disable");
                phase = 3;
                report("CUSTOMMC_OFFICIAL_189_STEP_GUI_REAL_KEY_DISABLE_NATIVE_VANILLA_PASS=YES");
            }
        }
    }

    private static void requireStep(
            final float observed,
            final float expected,
            final String stage) {
        if (!Float.isFinite(observed)
                || Math.abs(observed - expected) > TOLERANCE) {
            throw new IllegalStateException(
                    "V94 " + stage + " expected=" + expected
                            + " observed=" + observed);
        }
    }

    private static void report(final String marker) {
        System.out.println(marker);
        System.out.flush();
    }
}
