"""V346 exact AutoBlock native lease cleanup source regression.

No synthetic game/menu or server-success claim: original controller/UUID,
hurt and vanilla scoreboard remain mandatory in real acceptance.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SRC = (ROOT / "platform-1.8.9" / "src" / "main" / "java" /
       "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9")
BRIDGE = (SRC / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
HOST = (SRC / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
BLOCK = (SRC / "Minecraft189AutoBlockModule.java").read_text(encoding="utf-8")
HARNESS = (ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py"
           ).read_text(encoding="utf-8")


class V346NativeSwordUseCleanup(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.method = BRIDGE.split(
            "public static synchronized void autoClick(", 1)[1].split(
            "public static synchronized void publishTick(", 1)[0]

    def test_exact_native_lease_always_finally_cleans_after_gui_veto(self):
        code = self.method
        begin = code.index("final long useToken = host.beginAutoBlockNativeUse();")
        veto = code.index("if (vetoOriginalScreenBeforeNativeCombat(", begin)
        use = code.index("minecraft.customMcRightClickMouse();", veto)
        commit = code.index("host.recordAutoBlockStart(useToken);", use)
        cleanup = code.index("host.cancelAutoBlockNativeUse(useToken);", commit)
        self.assertIn("try {", code[begin:veto])
        self.assertIn("} finally {", code[commit:cleanup])
        self.assertLess(begin, veto)
        self.assertLess(veto, use)
        self.assertLess(use, commit)
        self.assertLess(commit, cleanup)
        self.assertIn("host.leaveSyntheticCombatCycle(combatCycleToken)", code)
        self.assertIn("host.restoreCombatSlotAfterSyntheticClick(originalSlot)", code)

    def test_cancellation_is_only_exact_ticket_no_game_item_release(self):
        cancel = BLOCK.split("synchronized boolean cancelPendingNativeUse(", 1)[1].split(
            "synchronized boolean completeNativeUse(", 1)[0]
        self.assertIn("token != pendingUseToken", cancel)
        self.assertIn("pendingUseToken = 0L;", cancel)
        self.assertIn("pendingHeldOwnerRequired = false;", cancel)
        self.assertNotIn("ownsBlock = false", cancel)
        self.assertNotIn("stopUsingItem(", cancel)
        self.assertNotIn("customMcRightClickMouse(", cancel)
        host = HOST.split("void cancelAutoBlockNativeUse(", 1)[1].split(
            "void recordAutoBlockStart(", 1)[0]
        self.assertIn("cancelPendingNativeUse(useToken)", host)
        self.assertNotIn("customMcStopUsingItem(", host)

    def test_native_server_success_gates_remain_strict_and_independent(self):
        for gate in (
            "wait_for(host, host_log, CONTROLLER_ATTACK,",
            "TRIGGER_HURT if args.triggerbot_pvp else AURA_HURT,",
            "wait_for(host, host_log, REJOIN_B_HIT, deadline,",
            "scoreboard players test CITarget taken 1 999999",
        ):
            self.assertIn(gate, HARNESS)
        self.assertNotIn("V346_AUTO_BLOCK_PASS", HARNESS)


if __name__ == "__main__":
    unittest.main()
