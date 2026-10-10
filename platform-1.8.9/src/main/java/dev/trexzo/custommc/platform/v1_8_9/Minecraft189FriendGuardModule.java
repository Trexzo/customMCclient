package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Default-off UUID-based friend exclusion, independent of AntiBot and Team Guard.
 * No player name guesses, network lookups or case-dependent identity comparisons.
 */
public final class Minecraft189FriendGuardModule implements Module {
    public static final String ID = "combat.friendGuard";
    public static final String FRIEND_UUIDS = ID + ".friendUuids";
    public static final int MAX_LIST_LENGTH = 1024;
    private static final Pattern EXACT_UUID = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-"
                    + "[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private final Setting<String> friendUuids = new Setting<String>(
            FRIEND_UUIDS, "", Minecraft189FriendGuardModule::validList,
            SettingCodecs.STRING);
    private boolean enabled;
    private String cachedList;
    private Set<UUID> cachedFriends = Collections.emptySet();

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() { enabled = true; }
    @Override public synchronized void onDisable() { enabled = false; }
    synchronized boolean active() { return enabled; }
    public Setting<String> friendUuidsSetting() { return friendUuids; }

    synchronized boolean requiresIdentity() {
        return enabled && !friends().isEmpty();
    }

    synchronized boolean permits(final int index,
            final Minecraft189WorldEntityUuidState.Snapshot evidence) {
        if (!enabled) return true;
        final Set<UUID> configured = friends();
        if (configured.isEmpty()) return true;
        final UUID id = evidence == null ? null : evidence.at(index);
        return id != null && !configured.contains(id);
    }

    private Set<UUID> friends() {
        final String raw = friendUuids.get();
        if (!raw.equals(cachedList)) {
            // Setting validator guarantees validList before persisting.
            cachedFriends = Collections.unmodifiableSet(parse(raw));
            cachedList = raw;
        }
        return cachedFriends;
    }

    private static boolean validList(final String raw) {
        if (raw == null || raw.length() > MAX_LIST_LENGTH) return false;
        try {
            parse(raw);
            return true;
        } catch (IllegalArgumentException bad) {
            return false;
        }
    }

    private static Set<UUID> parse(final String raw) {
        final Set<UUID> friends = new HashSet<UUID>();
        if (raw.trim().isEmpty()) return friends;
        for (String part : raw.split(",", -1)) {
            final String token = part.trim();
            if (!EXACT_UUID.matcher(token).matches()) {
                throw new IllegalArgumentException("Friend identity must be an exact UUID");
            }
            if (!friends.add(UUID.fromString(token))) {
                throw new IllegalArgumentException("Duplicate friend UUID");
            }
        }
        return friends;
    }
}
