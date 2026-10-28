"""V353 — source contract: sticky UUID ownership without synthetic FOV bypass.

Unit/source checks are NOT original Mojang 1.8.9 client acceptance.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
SELECTOR = (BASE / "Minecraft189KillAuraTargetSelector.java").read_text(encoding="utf-8")
AURA = (BASE / "Minecraft189KillAuraModule.java").read_text(encoding="utf-8")
HOST = (BASE / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
BRIDGE = (BASE / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
REJOIN = (BASE / "Minecraft189RejoinTargetAcceptance.java").read_text(encoding="utf-8")


class StickyOriginalUuidV353(unittest.TestCase):
    def test_eligible_original_owner_pinned_by_uuid_not_index(self):
        self.assertIn("lockedUuid.equals(identities.at(i))", SELECTOR)
        self.assertIn("pinnedOwner = lockEnabled && lockedUuid != null", SELECTOR)
        self.assertIn("verifiedLockRetainable(", SELECTOR)
        self.assertIn("!pinnedOwner || lockedUuid.equals(", SELECTOR)
        self.assertIn("identities.at(candidate.entityIndex())", SELECTOR)
        self.assertIn("pinnedOwner ? lockedUuid : null", SELECTOR)
        self.assertIn("if (ownedIndex >= 0) return false;", SELECTOR)

    def test_source_safety_and_original_fov_always_required_for_attacks(self):
        for keyword in ("combat.alive(ownedIndex)", "range*range",
                        "antiBot.permits(ownedIndex, combat)",
                        "teamGuard.permits(ownedIndex, combat)",
                        "friendGuard.permits(ownedIndex, identities)",
                        "wallCheck.permits(ownedIndex, visibility)"):
            self.assertIn(keyword, SELECTOR)
        self.assertIn("angularScore(local, rotation, candidate, fov)", SELECTOR)
        self.assertIn("combat.hurtTime(candidate.entityIndex()) <= acceptedHurtTime", SELECTOR)
        self.assertIn("if (!eligible(target, attackHeld, movement))", AURA)
        self.assertIn("resetSchedule();\n            lastEntityIndex = -1;", AURA)
        self.assertIn("Math.abs(dyaw) > fov.get()", AURA)
        self.assertIn("Math.abs(dpitch) > fov.get()", AURA)
        self.assertIn("Math.abs(wrap(target.yaw() - rotation.yaw())) > MAX_AIM_ERROR", AURA)
        self.assertIn("verifiedPlayerIndex != target.entityIndex()", AURA)

    def test_explicit_world_revocation_and_combat_owner_remain_hard_gates(self):
        self.assertIn('"original-world-source-revoked"', HOST)
        self.assertIn('"original-world-identity-changed"', HOST)
        self.assertIn('"original-world-combat-inconsistent"', HOST)
        self.assertIn('featureCatalog.killAura().rememberSelectedTarget(', HOST)
        self.assertIn("featureCatalog.killAura().suspend();", HOST)
        self.assertIn("ids.matches(tickUuidWorld.customMcLoadedEntityUuids())", HOST)
        self.assertIn("selectedUuid.equals(ids.at(crosshairPlayerIndex))", HOST)
        self.assertIn("host.mayDispatchSyntheticMelee(originalSlot)", BRIDGE)
        self.assertIn("host.enterSyntheticCombatCycle()", BRIDGE)
        self.assertIn("host.leaveSyntheticCombatCycle(combatCycleToken)", BRIDGE)
        self.assertIn("minecraft.customMcAttackCrosshairEntity();", BRIDGE)
        self.assertIn("Minecraft189RejoinTargetAcceptance.verifyOwnedClick(", BRIDGE)

    def test_original_rejoin_native_ownership_oracles_not_replaced(self):
        self.assertIn("rejoinedTicks>=20", REJOIN)
        self.assertIn("e.aCloserButBSelected()", REJOIN)
        self.assertIn("survivorUuid.equals(aura.lockedTargetUuid())", REJOIN)
        self.assertIn("verifyRealControllerB(", REJOIN)
        self.assertIn("requireRejoinedControllerEntityUuid", REJOIN)
        self.assertIn("vetoTransientUnownedPhaseFourSyntheticClick(", REJOIN)
        self.assertNotIn("V353_REJOIN_FAKE_PASS", REJOIN)


if __name__ == "__main__":
    unittest.main()
