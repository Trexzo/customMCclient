package dev.trexzo.custommc.platform.v1_8_9;

public interface Minecraft189BlockMiningControl {
    boolean customMcIsHittingBlock();

    float customMcBlockDamageProgress();

    void customMcSetBlockDamageProgress(float progress);
}
