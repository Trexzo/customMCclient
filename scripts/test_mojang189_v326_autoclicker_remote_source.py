"""V326 original Auto Clicker exact remote UUID source-wiring regressions.

Textual/offline checks are NEVER permission to pass original Mojang PvP.
The full genuine Minecraft 1.8.9 + dedicated server CI remains mandatory.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "platform-1.8.9" / "src" / "main" / "java" / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9"


class V326RealRemoteSourceWiringTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.guard = (JAVA / "Minecraft189AutoClickerRemoteUuidAcceptance.java").read_text(encoding="utf-8")
        cls.bridge = (JAVA / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
        cls.host = (JAVA / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
        cls.harness = (ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py").read_text(encoding="utf-8")
        cls.workflow = (ROOT / ".github" / "workflows" / "minecraft-189-real-client.yml").read_text(encoding="utf-8")

    def test_exact_native_controller_path_is_opt_in_and_pre_attack(self):
        control = self.bridge[self.bridge.index("acceptanceControllerDispatchAllowed("):]
        gate = control.index('custommc.acceptance.requireAutoClickerExactRemoteUuid')
        actual = control.index("Minecraft189AutoClickerRemoteUuidAcceptance.admits(")
        marker = control.index("CUSTOMMC_189_AUTOCLICKER_NATIVE_EXACT_REMOTE_PLAYER_UUID_PASS=YES")
        aura = control.index("if (expected != null) {")
        self.assertTrue(0 <= gate < actual < marker < aura)
        for required in ("expected != null", "expectedTrigger != null",
                         "autoClicker().active()", "killAura().active()",
                         "triggerBot().active()", "customMcNativeEntityUuid()"):
            self.assertIn(required, control[gate:aura])

    def test_guard_rejects_missing_wrong_or_nonlocal_native_world_uuids(self):
        guard = self.guard
        for required in ("expectedRemote.equals(controllerArgument)",
                         "!ids.available()", "!kinds.available()",
                         "ids.entityCount() != kinds.entityCount()",
                         "!kinds.player(i)", "!kinds.living(i)",
                         "kinds.localPlayer(i)", "if (++matching > 1)",
                         "return matching == 1"):
            self.assertIn(required, guard)

    def test_original_uuid_capture_is_only_opt_in_for_autoclicker(self):
        identity = self.host[self.host.index("private boolean identityRequired()"):]
        self.assertIn(
            '"custommc.acceptance.requireAutoClickerExactRemoteUuid"',
            identity[:1700])
        self.assertIn("featureCatalog.killAura().active()", identity[:1700])

    def test_real_two_player_harness_requires_uuid_and_authoritative_damage(self):
        self.assertIn('group.add_argument("--autoclicker-pvp"', self.harness)
        self.assertIn('official_offline_uuid("CITarget")', self.harness)
        self.assertIn('AUTO_NATIVE_EXACT_REMOTE, deadline', self.harness)
        self.assertIn('AUTO_NATIVE_UNSCOPED, deadline', self.harness)
        self.assertIn('AUTO_HURT if args.autoclicker_pvp', self.harness)
        self.assertIn('scoreboard players test CITarget taken 1 999999', self.harness)
        self.assertIn('keep_host_active=args.autoclicker_pvp', self.harness)
        self.assertIn('AUTOCLICKER_EXACT_REMOTE_UUID_AND_SERVER_DAMAGE_PASS', self.harness)
        self.assertNotIn('args.pvp = True\n    if args.autoclicker_pvp', self.harness)

    def test_new_original_ci_is_independent_of_prior_m426_and_triggerbot(self):
        stages = self.workflow
        old = stages.index("M426 verify real Auto Clicker native attack")
        strict = stages.index("V326 exact native Auto Clicker remote player UUID")
        trig = stages.index("V72 original TriggerBot real player UUID")
        self.assertLess(old, strict)
        self.assertLess(strict, trig)
        section = stages[strict:trig]
        self.assertIn("RunMojang189LocalhostTwoPlayerSmoke.py", section)
        self.assertIn("--autoclicker-pvp", section)
        self.assertIn('test "$accepted" -eq 1', section)


if __name__ == "__main__":
    unittest.main()
