"""V341 combat cycle owner wiring and strict original-game proof regression."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = (ROOT / "platform-1.8.9" / "src" / "main" / "java"
        / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9")
BRIDGE = (JAVA / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
HOST = (JAVA / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
MACHINE = (JAVA / "Minecraft189CombatCycleArbiter.java").read_text(encoding="utf-8")
HARNESS = (ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py"
           ).read_text(encoding="utf-8")


class V341NativeCombatCycle(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.body = BRIDGE.split(
            "public static synchronized void autoClick(", 1
        )[1].split("public static synchronized void publishTick(", 1)[0]

    def test_one_cycle_token_wraps_entire_original_synthetic_callback(self):
        body = self.body
        self.assertIn("host.enterSyntheticCombatCycle()", body)
        self.assertIn("if (combatCycleToken == 0L) return;", body)
        self.assertIn("} finally {", body)
        self.assertIn("host.leaveSyntheticCombatCycle(combatCycleToken);", body)
        self.assertLess(body.index("host.enterSyntheticCombatCycle()"),
                        body.index("host.shouldAutoPot("))
        self.assertGreater(body.index("host.leaveSyntheticCombatCycle("),
                           body.index("host.recordAutoBlockStart(useToken);"))

    def test_potion_rod_melee_priority_and_single_reblock_sequence(self):
        body = self.body
        pot = body.index("Minecraft189CombatCycleArbiter.Primary.POTION")
        rod = body.index("Minecraft189CombatCycleArbiter.Primary.ROD")
        melee = body.index("Minecraft189CombatCycleArbiter.Primary.MELEE")
        block = body.index("host.claimSyntheticCombatReblock(combatCycleToken)")
        self.assertLess(pot, rod)
        self.assertLess(rod, melee)
        self.assertLess(melee, block)
        self.assertLess(pot, body.index("Minecraft189AutoPotSourceCast.execute("))
        self.assertLess(rod, body.index("host.mayExecuteAutoRodUse("))
        self.assertLess(melee, body.index("host.shouldKeepSprintAfterSyntheticClick("))
        self.assertLess(block, body.index("host.beginAutoBlockNativeUse()"))

    def test_module_authority_stays_independent_from_cycle(self):
        self.assertIn("case POTION:", MACHINE)
        self.assertIn("case ROD:", MACHINE)
        self.assertIn("case MELEE:", MACHINE)
        self.assertIn("phase != Phase.MELEE_ATTEMPT", MACHINE)
        self.assertIn("phase != Phase.QUIESCENT", MACHINE)
        self.assertIn("void revoke()", MACHINE)
        self.assertNotIn("attackEntity(", MACHINE)
        self.assertNotIn("setSprinting(", MACHINE)
        self.assertNotIn("sendPacket(", MACHINE)
        self.assertIn("featureCatalog.autoBlock().beginNativeUse(", HOST)
        self.assertIn("heldItemOwnerSerial", HOST)
        self.assertIn("featureCatalog.wTap()", HOST)
        self.assertIn("featureCatalog.keepSprint()", HOST)
        self.assertIn("combatCycle.revoke();", HOST)

    def test_unchanged_original_controller_uuid_hurt_and_server_damage(self):
        for original_required in (
            "wait_for(host, host_log, CONTROLLER_ATTACK,",
            "TRIGGER_HURT if args.triggerbot_pvp else AURA_HURT,",
            "wait_for(host, host_log, REJOIN_B_HIT, deadline,",
            "scoreboard players test CITarget taken 1 999999",
        ):
            self.assertIn(original_required, HARNESS)
        self.assertNotIn("COMBAT_CYCLE_PASS", HARNESS)


if __name__ == "__main__":
    unittest.main()
