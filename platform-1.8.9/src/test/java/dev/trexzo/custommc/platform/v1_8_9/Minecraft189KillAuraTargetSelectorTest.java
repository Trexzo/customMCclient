package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189KillAuraTargetSelectorTest {
    @Test
    void switchesBetweenNearestAndCrosshairPriorityInsideConfiguredFov() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState positions = new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat = new Minecraft189WorldEntityCombatState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        local.update(0, 0, 0);
        rotation.update(0.0F, 0.0F); // North: +Z
        positions.update(new double[]{1, 0, 0, 0, 0, 3});
        kinds.update(new int[]{3, 3});
        combat.update(new int[]{1, 1});

        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertTrue(selected.snapshot().found());
        assertEquals(0, selected.snapshot().entityIndex()); // nearer east

        aura.prioritizeCrosshairSetting().set(true);
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertEquals(1, selected.snapshot().entityIndex()); // aligned north

        aura.prioritizeCrosshairSetting().set(false);
        aura.fovSetting().set(45.0D);
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertEquals(1, selected.snapshot().entityIndex()); // east excluded
    }

    @Test
    void optionalTabEvidenceRejectsKnownMissingNetworkPlayer() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189AntiBotModule antiBot = new Minecraft189AntiBotModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState positions = new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat = new Minecraft189WorldEntityCombatState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        local.update(0, 0, 0);
        rotation.update(0, 0);
        positions.update(new double[]{0, 0, 2, 0, 0, 3});
        kinds.update(new int[]{3, 3});
        combat.update(new int[]{1 | 512, 1 | 512 | 256});

        // Off by default: nearer player still wins.
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), antiBot, selected);
        assertEquals(0, selected.snapshot().entityIndex());

        antiBot.onEnable();
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), antiBot, selected);
        assertEquals(1, selected.snapshot().entityIndex());
        combat.update(new int[]{1 | 512, 1 | 512});
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), antiBot, selected);
        assertFalse(selected.snapshot().found());
        antiBot.onDisable();
    }

    @Test
    void switchesFromHurtNearestToNextEligiblePlayerWithoutBankingTargets() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState positions = new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat = new Minecraft189WorldEntityCombatState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        local.update(0, 0, 0);
        rotation.update(0, 0);
        positions.update(new double[]{0, 0, 2, 0, 0, 3});
        kinds.update(new int[]{3, 3});
        combat.update(new int[]{(6 << 1) | 1, 1});

        // Default OFF preserves ordinary nearest selection.
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertEquals(0, selected.snapshot().entityIndex());

        aura.switchHurtTargetsSetting().set(true);
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertEquals(1, selected.snapshot().entityIndex());
        aura.maxSwitchHurtTicksSetting().set(6);
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertEquals(0, selected.snapshot().entityIndex());

        // When all nearby targets are hurt beyond threshold, no attacker
        // identity is fabricated and the selector reports no candidate.
        aura.maxSwitchHurtTicksSetting().set(0);
        combat.update(new int[]{(4 << 1) | 1, (3 << 1) | 1});
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertFalse(selected.snapshot().found());
        assertThrows(IllegalArgumentException.class,
                () -> aura.maxSwitchHurtTicksSetting().set(21));
    }


    @Test
    void stableUuidLockSurvivesReorderedIndicesAndRevalidatesEligibility() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat =
                new Minecraft189WorldEntityCombatState();
        final Minecraft189WorldEntityUuidState identities =
                new Minecraft189WorldEntityUuidState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        final java.util.UUID a = java.util.UUID.randomUUID();
        final java.util.UUID b = java.util.UUID.randomUUID();
        local.update(0, 0, 0);
        rotation.update(0, 0);
        positions.update(new double[]{0, 0, 2, 0, 0, 3});
        kinds.update(new int[]{3, 3});
        combat.update(new int[]{1, 1});
        identities.update(new java.util.UUID[]{a, b});
        aura.lockTargetSetting().set(true);
        aura.onEnable();
        try {
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), null, null, identities.snapshot(), selected);
            assertEquals(0, selected.snapshot().entityIndex());
            assertEquals(a, aura.lockedTargetUuid());

            // Identical entities appear in a different loadedEntityList order.
            // A remains selected by UUID even though index zero is now B.
            positions.update(new double[]{0, 0, 1, 0, 0, 3});
            identities.update(new java.util.UUID[]{b, a});
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), null, null, identities.snapshot(), selected);
            assertEquals(1, selected.snapshot().entityIndex());
            assertEquals(a, aura.lockedTargetUuid());

            // The locked UUID dies; choose an eligible new player.
            combat.update(new int[]{1, 0});
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), null, null, identities.snapshot(), selected);
            assertEquals(0, selected.snapshot().entityIndex());
            assertEquals(b, aura.lockedTargetUuid());

            // A stale or missing identity snapshot must fail closed.
            identities.clear();
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), null, null, identities.snapshot(), selected);
            assertFalse(selected.snapshot().available());
            assertNull(aura.lockedTargetUuid());

            // Disable the optional lock: nearest targeting is unchanged.
            aura.lockTargetSetting().set(false);
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), selected);
            assertEquals(0, selected.snapshot().entityIndex());
        } finally { aura.onDisable(); }
        assertNull(aura.lockedTargetUuid());
    }

    @Test
    void rejoinedCloserOriginalAUnableToStealGuiPausedSurvivorB() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation =
                new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat =
                new Minecraft189WorldEntityCombatState();
        final Minecraft189WorldEntityUuidState identities =
                new Minecraft189WorldEntityUuidState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        // Real-world-shaped, fixed test UUIDs: actual server provenance
        // is only established by the strict original multiplayer gate.
        final java.util.UUID a = java.util.UUID.fromString(
                "e3542dd1-56f3-3ab4-82cb-2c015df72d8d");
        final java.util.UUID b = java.util.UUID.fromString(
                "9c6b2c0b-a653-3300-9829-2617908fae14");
        local.update(0, 0, 0);
        rotation.update(0, 0);
        aura.fovSetting().set(180.0D);
        aura.lockTargetSetting().set(true);
        aura.onEnable();
        try {
            // Original server A has genuinely disconnected; survivor B
            // is the only loaded remote identity, selected as real UUID.
            positions.update(new double[]{0, 0, 2.2});
            kinds.update(new int[]{3});
            combat.update(new int[]{1});
            identities.update(new java.util.UUID[]{b});
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), null, null, identities.snapshot(), selected);
            assertEquals(b, aura.lockedTargetUuid());
            assertEquals(0, selected.snapshot().entityIndex());
            aura.suspendForGui();
            assertEquals(b, aura.lockedTargetUuid());

            // New original A has rejoined with a fresh loadedEntityList
            // index and is closer than B. No cached list slot is reused.
            positions.update(new double[]{0, 0, 1.4, 0, 0, 2.2});
            kinds.update(new int[]{3, 3});
            combat.update(new int[]{1, 1});
            identities.update(new java.util.UUID[]{a, b});
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), null, null, identities.snapshot(), selected);
            assertEquals(1, selected.snapshot().entityIndex());
            assertEquals(b, aura.lockedTargetUuid(),
                    "GUI focus must not transfer B's real original UUID to nearer A");

            // B really unloads: original UUID verification must RELEASE
            // the stale lock, allowing A only on fresh source selection.
            positions.update(new double[]{0, 0, 1.4});
            kinds.update(new int[]{3});
            combat.update(new int[]{1});
            identities.update(new java.util.UUID[]{a});
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), null, null, identities.snapshot(), selected);
            assertEquals(0, selected.snapshot().entityIndex());
            assertEquals(a, aura.lockedTargetUuid());
            identities.clear();
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), null, null, identities.snapshot(), selected);
            assertFalse(selected.snapshot().available());
            assertNull(aura.lockedTargetUuid(),
                    "an invalid native UUID list cannot preserve GUI pause ownership");
        } finally { aura.onDisable(); }
    }

    @Test
    void standaloneTeamGuardFiltersAuraWithoutAntiBotEnabled() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189AntiBotModule bot = new Minecraft189AntiBotModule();
        final Minecraft189TeamGuardModule guard = new Minecraft189TeamGuardModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState pos = new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat = new Minecraft189WorldEntityCombatState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        local.update(0, 0, 0);
        rotation.update(0, 0);
        pos.update(new double[]{0, 0, 2, 0, 0, 3});
        kinds.update(new int[]{3, 3});
        combat.update(new int[]{1 | 2048 | 1024, 1 | 2048});
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), pos.snapshot(), kinds.snapshot(), combat.snapshot(),
                bot, guard, selected);
        assertEquals(0, selected.snapshot().entityIndex());
        guard.onEnable();
        try {
            assertFalse(bot.active());
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), pos.snapshot(), kinds.snapshot(), combat.snapshot(),
                    bot, guard, selected);
            assertEquals(1, selected.snapshot().entityIndex());
            combat.update(new int[]{1 | 2048 | 1024, 1});
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), pos.snapshot(), kinds.snapshot(), combat.snapshot(),
                    bot, guard, selected);
            assertFalse(selected.snapshot().found());
            guard.onDisable();
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), pos.snapshot(), kinds.snapshot(), combat.snapshot(),
                    bot, guard, selected);
            assertEquals(0, selected.snapshot().entityIndex());
        } finally { guard.onDisable(); }
    }


    @Test
    void friendsExcludedFromAuraByUuidWithLockOffAndAfterReordering() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189FriendGuardModule friends = new Minecraft189FriendGuardModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat =
                new Minecraft189WorldEntityCombatState();
        final Minecraft189WorldEntityUuidState identities =
                new Minecraft189WorldEntityUuidState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        final java.util.UUID friend =
                java.util.UUID.fromString("00000000-0000-4000-8000-000000000001");
        final java.util.UUID enemy =
                java.util.UUID.fromString("00000000-0000-4000-8000-000000000002");
        local.update(0, 0, 0);
        rotation.update(0, 0);
        positions.update(new double[]{0, 0, 2, 0, 0, 3});
        kinds.update(new int[]{3, 3});
        combat.update(new int[]{1, 1});
        identities.update(new java.util.UUID[]{friend, enemy});
        friends.friendUuidsSetting().set(friend.toString());
        aura.onEnable();
        try {
            // Friend guard disabled: nearer friend remains eligible.
            Minecraft189KillAuraTargetSelector.select(aura,
                    local.snapshot(), rotation.snapshot(), positions.snapshot(),
                    kinds.snapshot(), combat.snapshot(), null, null,
                    friends, identities.snapshot(), selected);
            assertEquals(0, selected.snapshot().entityIndex());
            friends.onEnable();
            assertFalse(aura.lockTargetSetting().get());
            Minecraft189KillAuraTargetSelector.select(aura,
                    local.snapshot(), rotation.snapshot(), positions.snapshot(),
                    kinds.snapshot(), combat.snapshot(), null, null,
                    friends, identities.snapshot(), selected);
            assertEquals(1, selected.snapshot().entityIndex());

            // Reorder without changing friend identity: friend still rejected.
            identities.update(new java.util.UUID[]{enemy, friend});
            positions.update(new double[]{0, 0, 3, 0, 0, 2});
            Minecraft189KillAuraTargetSelector.select(aura,
                    local.snapshot(), rotation.snapshot(), positions.snapshot(),
                    kinds.snapshot(), combat.snapshot(), null, null,
                    friends, identities.snapshot(), selected);
            assertEquals(0, selected.snapshot().entityIndex());

            identities.clear();
            Minecraft189KillAuraTargetSelector.select(aura,
                    local.snapshot(), rotation.snapshot(), positions.snapshot(),
                    kinds.snapshot(), combat.snapshot(), null, null,
                    friends, identities.snapshot(), selected);
            assertFalse(selected.snapshot().available());

            friends.friendUuidsSetting().set("");
            Minecraft189KillAuraTargetSelector.select(aura,
                    local.snapshot(), rotation.snapshot(), positions.snapshot(),
                    kinds.snapshot(), combat.snapshot(), null, null,
                    friends, identities.snapshot(), selected);
            assertEquals(1, selected.snapshot().entityIndex());
        } finally { friends.onDisable(); aura.onDisable(); }
    }


    @Test
    void wallCheckRejectsOccludedAndUnknownPlayersWithoutChangingDefaultAura() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189WallCheckModule wall = new Minecraft189WallCheckModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat =
                new Minecraft189WorldEntityCombatState();
        final Minecraft189WorldEntityVisibilityState visibility =
                new Minecraft189WorldEntityVisibilityState();
        final Minecraft189NearestPlayerTargetState target =
                new Minecraft189NearestPlayerTargetState();
        local.update(0, 0, 0);
        rotation.update(0, 0);
        positions.update(new double[]{0, 0, 2, 0, 0, 3});
        kinds.update(new int[]{3, 3});
        combat.update(new int[]{1, 1});
        visibility.update(new int[]{0, 1});
        aura.onEnable();
        try {
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), null, null, null, null,
                    wall, visibility.snapshot(), target);
            assertEquals(0, target.snapshot().entityIndex()); // wall off
            wall.onEnable();
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), null, null, null, null,
                    wall, visibility.snapshot(), target);
            assertEquals(1, target.snapshot().entityIndex()); // 0 occluded

            visibility.update(new int[]{1, 0});
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), null, null, null, null,
                    wall, visibility.snapshot(), target);
            assertEquals(0, target.snapshot().entityIndex());

            visibility.update(new int[]{-1, 0});
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), null, null, null, null,
                    wall, visibility.snapshot(), target);
            assertFalse(target.snapshot().found()); // unknown not visible
            visibility.clear();
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), null, null, null, null,
                    wall, visibility.snapshot(), target);
            assertFalse(target.snapshot().available());
            wall.onDisable();
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), null, null, null, null,
                    wall, visibility.snapshot(), target);
            assertEquals(0, target.snapshot().entityIndex());
        } finally { wall.onDisable(); aura.onDisable(); }
    }

    @Test
    void excludesDeadUnknownAndOutOfRangeAndClearsStaleInputs() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState positions = new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat = new Minecraft189WorldEntityCombatState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        local.update(0, 0, 0);
        rotation.update(0, 0);
        positions.update(new double[]{1, 0, 0, 0, 0, 3});
        kinds.update(new int[]{3, 3});
        combat.update(new int[]{0, 1});
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertEquals(1, selected.snapshot().entityIndex());

        aura.rangeSetting().set(1.5D);
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertFalse(selected.snapshot().found());
        aura.rangeSetting().set(6.0D);
        combat.update(new int[]{-1, -1});
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertFalse(selected.snapshot().found());
        combat.clear();
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertFalse(selected.snapshot().available());
    }
}
