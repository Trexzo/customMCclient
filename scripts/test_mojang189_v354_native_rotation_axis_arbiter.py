"""V354 original Minecraft 1.8.9 native rotation writer ownership wiring.

A source integration regression only. Real original mouse deltas and
vanilla/server-owned attack acceptance are separate required proof.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
ARBITER = (BASE / "Minecraft189RotationAxisArbiter.java").read_text(encoding="utf-8")
HOST = (BASE / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
SOURCE = (BASE / "Minecraft189RotationHostSourceLease.java").read_text(encoding="utf-8")
BRIDGE = (BASE / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")


class NativeRotationOwnerV354(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.callback = HOST.split(
            "    void playerRotation(\n", 1)[1].split(
            "    void playerDimension(\n", 1)[0]

    def test_one_native_writer_uses_all_four_original_modules(self):
        callback = self.callback
        self.assertIn("rotationAxisArbiter.enter();", callback)
        self.assertIn("rotationAxisArbiter.exit();", callback)
        self.assertLess(callback.index("rotationAxisArbiter.exit();"),
                        callback.index("rotationHostLease.exit();"))
        for mod in ("SPIN", "KILL_AURA", "AIM_ASSIST", "JITTER"):
            self.assertIn("Minecraft189RotationAxisArbiter.Owner." + mod, callback)
        for control in ("spinControl", "auraControl",
                        "assistControl", "jitterControl"):
            self.assertIn("rotationAxisArbiter.guardedControl(", callback)
            self.assertIn(control, callback)
        self.assertIn("spinOwnsRotation || rotationAxisArbiter.ownedBy(", callback)
        self.assertIn("assistOwnsRotation || rotationAxisArbiter.ownedBy(", callback)

    def test_actual_writer_not_module_return_flag_or_stale_callback(self):
        self.assertIn("if (current != null && current != requested)", ARBITER)
        self.assertIn("current = requested;", ARBITER)
        self.assertIn("if (!active || token != generation", ARBITER)
        self.assertIn("++generation;", ARBITER)
        self.assertIn("if (claim(requested, token))", ARBITER)
        self.assertIn("original.customMcSetRotationYaw(value);", ARBITER)
        self.assertIn("original.customMcSetRotationPitch(value);", ARBITER)
        self.assertNotIn("minecraft.customMcAttackCrosshairEntity()", ARBITER)
        self.assertNotIn("sendPacket", ARBITER)

    def test_original_source_gui_and_minecraft_authority_are_unchanged(self):
        callback = self.callback
        self.assertIn("rotationHostLease.enter(player, originalGuiFocus)", callback)
        self.assertIn("rotationHostSourceStillOwned()", callback)
        self.assertIn("clickGuiRuntime.coreRuntime().model().snapshot().open()", callback)
        self.assertIn("featureCatalog.killAura().suspend();", callback)
        self.assertIn("rotationHostLease.guardedControl(player,", callback)
        self.assertIn("nativeSource.customMcSetRotationYaw(yaw)", SOURCE)
        self.assertIn("nativeSource.customMcSetRotationPitch(pitch)", SOURCE)
        self.assertIn("minecraft.customMcAttackCrosshairEntity();", BRIDGE)
        self.assertIn("host.enterSyntheticCombatCycle()", BRIDGE)
        self.assertIn("host.leaveSyntheticCombatCycle(combatCycleToken)", BRIDGE)

    def test_historical_priority_and_conditional_jitter_fallback(self):
        callback = self.callback
        order = [
            "final boolean spinOwnsRotation = featureCatalog.spin()",
            "final Minecraft189KillAuraModule aura =",
            "final Minecraft189AimAssistModule assist =",
            "final boolean assistOwnsRotation = assist.apply(",
            "featureCatalog.jitter()\n                    .apply(\n                            jitterControl",
        ]
        positions = [callback.index(x) for x in order]
        self.assertEqual(sorted(positions), positions)
        self.assertIn("if (aura.active())", callback)
        self.assertIn("if (assistOwnsRotation || rotationAxisArbiter.ownedBy(", callback)
        self.assertIn("if (spinOwnsRotation || rotationAxisArbiter.ownedBy(", callback)


if __name__ == "__main__":
    unittest.main()
