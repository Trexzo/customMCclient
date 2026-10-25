package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;

/**
 * M428/V326 offline original-game acceptance only.
 *
 * The actual vanilla controller's Entity argument UUID must be exactly the
 * login UUID of the dedicated-server's genuine remote player AND must occur
 * precisely once as a nonlocal living Player in the original loaded World
 * UUID/kind snapshot. A UUID-bearing mob, local player, wrong remote player,
 * missing snapshot or ambiguous list can NEVER produce a positive proof.
 *
 * This class does not enable combat, change packets or grant attack authority.
 */
final class Minecraft189AutoClickerRemoteUuidAcceptance {
    private Minecraft189AutoClickerRemoteUuidAcceptance() { }

    static boolean admits(
            final UUID expectedRemote, final UUID controllerArgument,
            final Minecraft189WorldEntityUuidState.Snapshot ids,
            final Minecraft189WorldEntityKindState.Snapshot kinds) {
        if (expectedRemote == null || controllerArgument == null
                || !expectedRemote.equals(controllerArgument)
                || ids == null || kinds == null
                || !ids.available() || !kinds.available()
                || ids.entityCount() != kinds.entityCount())
            return false;
        int matching = 0;
        for (int i = 0; i < ids.entityCount(); i++) {
            if (!controllerArgument.equals(ids.at(i))) continue;
            if (!kinds.player(i) || !kinds.living(i)
                    || kinds.localPlayer(i))
                return false;
            if (++matching > 1) return false;
        }
        return matching == 1;
    }
}
