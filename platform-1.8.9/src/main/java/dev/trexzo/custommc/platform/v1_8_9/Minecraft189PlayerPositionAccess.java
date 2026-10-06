package dev.trexzo.custommc.platform.v1_8_9;

public interface Minecraft189PlayerPositionAccess {
    Minecraft189PlayerPositionAccess NONE =
            new Minecraft189PlayerPositionAccess() {
                @Override
                public boolean playerPresent() {
                    return false;
                }

                @Override
                public double playerX() {
                    throw new IllegalStateException(
                            "player is not available");
                }

                @Override
                public double playerY() {
                    throw new IllegalStateException(
                            "player is not available");
                }

                @Override
                public double playerZ() {
                    throw new IllegalStateException(
                            "player is not available");
                }
            };

    boolean playerPresent();

    double playerX();

    double playerY();

    double playerZ();
}
