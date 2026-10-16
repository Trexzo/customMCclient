package dev.trexzo.custommc.platform.v1_8_9;

/**
 * V103: optional source-native Mojang 1.8.9 EntityRenderer.getMouseOver
 * acceptance. The original obfuscated renderer invokes the actual Reach
 * transformer callback. Physical Right Shift is delivered separately by X11.
 * Neither this witness nor the Python runner fabricates a raycast result.
 *
 * Does not certify server-authorized reach or a successful combat attack.
 */
final class Minecraft189OriginalClickGuiReachAcceptance {
    private static final String PROPERTY =
            "custommc.acceptance.requireNativeReachGuiFocus";
    private static final float CONFIGURED_REACH = 4.2F;
    private static final float EPSILON = 0.00001F;

    // 0 disabled baseline, 1 active game focus, 2 UI focus opened,
    // 3 original focus restored + module disabled and original ray restored.
    private static int phase;
    private static int baselineCallbacks;
    private static boolean enabledProof;

    private Minecraft189OriginalClickGuiReachAcceptance() { }

    static synchronized void observe(
            final Minecraft189HostRuntime host, final float originalNative,
            final float actualCallbackResult) {
        if (!Boolean.getBoolean(PROPERTY) || phase == 3
                || !host.playerMovementState().snapshot().available()) {
            return;
        }
        if (!Float.isFinite(originalNative) || originalNative <= 0.0F) {
            // Only a genuine finite Mojang render-world ray operand can
            // establish the original baseline. Other callback sources
            // never advance the acceptance witness.
            return;
        }

        final boolean guiOpen = host.clickGuiRuntime().coreRuntime()
                .model().snapshot().open();
        final Minecraft189ReachModule reach = host.featureCatalog().reach();

        if (phase == 0) {
            if (guiOpen || reach.active()) {
                throw new IllegalStateException(
                        "V103 original Reach baseline requires disabled module and closed GUI");
            }
            requireValue(actualCallbackResult, originalNative,
                    "disabled source-native renderer operand");
            if (++baselineCallbacks >= 12) {
                host.featureCatalog().enableReachForOriginalGuiAcceptance();
                phase = 1;
                report("CUSTOMMC_OFFICIAL_189_REACH_NATIVE_MODULE_ARMED_PASS=YES");
            }
            return;
        }

        if (phase == 1) {
            if (!reach.active()) {
                throw new IllegalStateException(
                        "V103 source Reach unexpectedly disabled");
            }
            requireValue(actualCallbackResult,
                    guiOpen ? originalNative : CONFIGURED_REACH,
                    "enabled source renderer pick");
            if (guiOpen) {
                phase = 2;
                report("CUSTOMMC_OFFICIAL_189_REACH_GUI_REAL_KEY_OPEN_NATIVE_PASS=YES");
            } else if (!enabledProof) {
                enabledProof = true;
                report("CUSTOMMC_OFFICIAL_189_REACH_SOURCE_NATIVE_ENABLED_PASS=YES");
            }
            return;
        }

        if (phase == 2) {
            if (!reach.active()) {
                throw new IllegalStateException(
                        "V103 source Reach lost module state across GUI lease");
            }
            requireValue(actualCallbackResult,
                    guiOpen ? originalNative : CONFIGURED_REACH,
                    "original GUI focus return");
            if (!guiOpen) {
                report("CUSTOMMC_OFFICIAL_189_REACH_GUI_REAL_KEY_CLOSE_NATIVE_PASS=YES");
                host.featureCatalog().disableReachForOriginalGuiAcceptance();
                requireValue(host.raycastBlockDistance(originalNative),
                        originalNative, "normal ModuleController disable");
                phase = 3;
                report("CUSTOMMC_OFFICIAL_189_REACH_NATIVE_DISABLE_VANILLA_PASS=YES");
            }
        }
    }

    private static void requireValue(
            final float observed, final float expected, final String stage) {
        if (!Float.isFinite(observed)
                || Math.abs(observed - expected) > EPSILON) {
            throw new IllegalStateException(
                    "V103 " + stage + " expected=" + expected
                            + " observed=" + observed);
        }
    }

    private static void report(final String marker) {
        System.out.println(marker);
        System.out.flush();
    }
}
