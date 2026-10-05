package dev.trexzo.custommc.core.module;

public interface Module {
    String id();

    default void onEnable() {
    }

    default void onDisable() {
    }
}
