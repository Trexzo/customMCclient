"""V365 JumpReset genuine-source final native jump admission contract.
Source-only; NEVER substitutes for original Mojang gameplay/controller evidence.
"""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/"platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
HOST=(BASE/"Minecraft189HostRuntime.java").read_text(encoding="utf-8")
RESET=(BASE/"Minecraft189JumpResetModule.java").read_text(encoding="utf-8")
JUMP=(BASE/"Minecraft189JumpHostSourceLease.java").read_text(encoding="utf-8")
BRIDGE=(BASE/"Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")

class V365OriginalHurtJumpReset(unittest.TestCase):
    def test_real_source_getters_are_bracketed_and_read_twice(self):
        s=HOST.split("private boolean mayDispatchOriginalJumpReset(",1)[1].split(
            "\n    void playerJumpControl(",1)[0]
        for key in (
            "player != jumpResetMovementOwner",
            "player != jumpResetHurtOwner",
            "jumpResetSourceGeneration",
            "Minecraft189PlayerMovementStateAccess",
            "Minecraft189PlayerHurtTimeAccess",
            "sourceHurt.customMcHurtTime()",
            "sourceMovement.customMcOnGround()",
            "sourceMovement.customMcSneaking()",
            "sourceMovement.customMcSprinting()",
            "originalHurt == hurtAgain",
            "originalHurt == hurt.hurtTime()",
            "ground && groundAgain && movement.onGround()",
            "sneak == sneakAgain && sneak == movement.sneaking()",
            "sprint == sprintAgain && sprint == movement.sprinting()",
            "jumpResetNativeSourceStillOwned(player, generation)",
            "featureCatalog.longJump().active()",
            "featureCatalog.highJump().active()",
            "featureCatalog.lowHop().active()",
            "featureCatalog.bunnyHop().active()",
            "featureCatalog.airJump().active()",
            "featureCatalog.flight().active()",
            "featureCatalog.freeze().active()",
        ):
            self.assertIn(key,s)
        self.assertEqual(2,s.count("sourceHurt.customMcHurtTime()"))
        self.assertEqual(2,s.count("sourceMovement.customMcOnGround()"))
        self.assertEqual(2,s.count("sourceMovement.customMcSneaking()"))
        self.assertEqual(2,s.count("sourceMovement.customMcSprinting()"))
        self.assertNotIn("customMcJump()",s)
        self.assertNotIn("customMcSetMotion",s)

    def test_scheduler_reserves_lane_before_external_native_getters(self):
        s=RESET.split("synchronized boolean apply(",1)[1].split(
            "private Object[] nativeJumpPolicy()",1)[0]
        self.assertIn("java.util.function.BooleanSupplier nativeAdmission",s)
        self.assertIn("nativeJumpDispatching = true;",s)
        self.assertIn("if (!nativeAdmission.getAsBoolean()",s)
        self.assertIn("!nativeJumpOwned(originalEpoch, originalPolicy)",s)
        self.assertLess(s.index("nativeJumpDispatching = true;"),
                        s.index("nativeAdmission.getAsBoolean()"))
        self.assertLess(s.index("nativeAdmission.getAsBoolean()"),
                        s.index("cooldownRemaining = configuredCooldown;"))
        self.assertLess(s.index("cooldownRemaining = configuredCooldown;"),
                        s.index("player.customMcJump();"))
        self.assertIn("nativeJumpDispatching = false;",s)
        self.assertIn("throw nativeFailure;",s)

    def test_host_only_adds_strict_jumpreset_gate(self):
        s=HOST.split("    void playerJumpControl(",1)[1].split(
            "    void playerStepControl(",1)[0]
        self.assertIn("mayDispatchOriginalJumpReset(",s)
        self.assertIn("featureCatalog.jumpReset().apply(",s)
        for owner in ("featureCatalog.longJump()","featureCatalog.highJump()",
                      "featureCatalog.lowHop()","featureCatalog.bunnyHop()",
                      "featureCatalog.autoJump()","featureCatalog.airJump()"):
            self.assertIn(owner,s)
        self.assertIn("jumpHostLease.exit()",s)
        self.assertIn("jumpHostLease.stillOwned(",HOST)
        self.assertIn("playerHurtTimeState.update(nativeHurtTime)",HOST)
        self.assertIn("playerMovementState.update(ground, sneaking, sprinting)",HOST)
        self.assertIn("++jumpResetSourceGeneration;",HOST)
        self.assertIn("original Minecraft",JUMP)
        self.assertIn("host.playerJumpControl(player)",BRIDGE)

if __name__=="__main__":
    unittest.main()
