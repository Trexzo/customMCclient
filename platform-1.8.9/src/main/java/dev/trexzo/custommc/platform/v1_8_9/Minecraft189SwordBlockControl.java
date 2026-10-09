package dev.trexzo.custommc.platform.v1_8_9;

/** Verified vanilla 1.8.9 right-click use-item lifecycle on Minecraft. */
public interface Minecraft189SwordBlockControl extends Minecraft189ClickMouseControl {
    /** Calls mapped EntityPlayer.isUsingItem via Minecraft.thePlayer. */
    boolean customMcIsUsingItem();
    /** Calls mapped PlayerControllerMP.onStoppedUsingItem(thePlayer). */
    void customMcStopUsingItem();
}
