package dev.trexzo.custommc.platform.v1_8_9;

public interface Minecraft189ClickMouseControl {
    void customMcClickMouse();

    /** Kill Aura only: use Mojang PlayerControllerMP.attackEntity on real raycast. */
    void customMcAttackCrosshairEntity();

    /** Exact Minecraft.rightClickMouse() delegated by mapped transformer. */
    void customMcRightClickMouse();
}
