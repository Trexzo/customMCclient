package dev.trexzo.custommc.launcher.command;

import java.util.Objects;

public final class LaunchIdentity {
    private final String playerName;
    private final String uuid;
    private final String accessToken;
    private final String userProperties;
    private final String userType;

    public LaunchIdentity(
            final String playerName,
            final String uuid,
            final String accessToken,
            final String userProperties,
            final String userType) {
        this.playerName = requireNonBlank(
                playerName,
                "playerName");
        this.uuid = requireNonBlank(uuid, "uuid");
        this.accessToken = Objects.requireNonNull(
                accessToken,
                "accessToken");
        this.userProperties = Objects.requireNonNull(
                userProperties,
                "userProperties");
        this.userType = requireNonBlank(
                userType,
                "userType");
    }

    public String playerName() {
        return playerName;
    }

    public String uuid() {
        return uuid;
    }

    public String accessToken() {
        return accessToken;
    }

    public String userProperties() {
        return userProperties;
    }

    public String userType() {
        return userType;
    }

    private static String requireNonBlank(
            final String value,
            final String name) {
        Objects.requireNonNull(value, name);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    name + " must not be blank");
        }
        return value;
    }
}
