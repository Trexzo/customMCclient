package dev.trexzo.custommc.platform.v1_8_9;

public interface Minecraft189PlayerArmorAccess {
    boolean customMcArmorBoots();

    boolean customMcArmorLeggings();

    boolean customMcArmorChestplate();

    boolean customMcArmorHelmet();

    default Minecraft189ItemStackAccess customMcArmorBootsItem() {
        return null;
    }

    default Minecraft189ItemStackAccess customMcArmorLeggingsItem() {
        return null;
    }

    default Minecraft189ItemStackAccess customMcArmorChestplateItem() {
        return null;
    }

    default Minecraft189ItemStackAccess customMcArmorHelmetItem() {
        return null;
    }
}
