"""V362 original AutoPot native health identity and physical RMB contract.

Source-only safety checks do not certify original Mojang gameplay, genuine
ItemPotion, vanilla splash use, server healing or strict B/A rejoin damage.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
MODULE = (BASE / "Minecraft189AutoPotModule.java").read_text(encoding="utf-8")
HOST = (BASE / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
BRIDGE = (BASE / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
CAST = (BASE / "Minecraft189AutoPotSourceCast.java").read_text(encoding="utf-8")
HARNESS = (ROOT / "scripts/RunMojang189SingleplayerSmoke.py").read_text(
    encoding="utf-8")


class OriginalPotionLiveNativeUseV362(unittest.TestCase):
    def test_read_only_source_discovery_is_distinct_from_native_health_cast(self):
        source = HOST.split(
            "    boolean mayExecuteAutoPotUse(final int originalSlot) {", 1
        )[1].split("\n    /**", 1)[0]
        self.assertIn("Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(",
                      source)
        self.assertIn("sourceAutoPotHealthLeaseValid()", source)
        self.assertNotIn("mayDispatchAutoPotNativeUse(", source)
        self.assertNotIn("Minecraft189ClickRateTracker.RIGHT_BUTTON", source)
        self.assertIn("original source potion final-use gate", (
            BASE / "Minecraft189OriginalCombatItemDiscoveryAcceptance.java"
        ).read_text(encoding="utf-8"))

    def test_production_native_cast_observes_same_real_low_health_player(self):
        dispatch = HOST.split(
            "    boolean mayDispatchAutoPotNativeUse(final int originalSlot) {", 1
        )[1].split("\n    /**", 1)[0]
        for term in (
            "mayExecuteAutoPotUse(originalSlot)",
            "originalPlayer != (Object) originalHeld",
            "originalPlayer instanceof Minecraft189PlayerHealthAccess",
            "autoPotLeaseOriginal != originalSlot",
            "live.customMcMaxHealth()",
            "live.customMcHealth()",
            "finalMaximum == maximum && finalHealth == health",
            "tickHotbarPlayer == originalPlayer",
            "tickHeldItemAccess == originalHeld",
            "tickHotbarControl == inventory",
            "autoPotLeaseInventory == inventory",
            "Minecraft189ClickRateTracker.RIGHT_BUTTON",
            "clickGuiRuntime.coreRuntime().model().snapshot().open()",
            "permitsLiveNativeHealth(",
        ):
            self.assertIn(term, dispatch)
        self.assertNotIn("customMcRightClickMouse", dispatch)
        self.assertNotIn("customMcSetSelectedHotbarSlot", dispatch)

    def test_no_potion_at_full_dead_or_unknown_health(self):
        pred = MODULE.split(
            "    synchronized boolean permitsLiveNativeHealth(", 1
        )[1].split("\n    synchronized void commitUseAttempt()", 1)[0]
        for term in ("Float.isFinite(current)", "Float.isFinite(maximum)",
                     "maximum > 0.0F", "current > 0.0F",
                     "current < maximum",
                     "100.0D * current / maximum <= healthThreshold.get()"):
            self.assertIn(term, pred)
        self.assertNotIn("commitUseAttempt", pred)

    def test_native_rightclick_has_final_gui_and_health_before_dispatch(self):
        section = BRIDGE.split("if (host.shouldAutoPot(playerHit, playerIndex))", 1)[1]
        section = section.split("if (host.shouldAutoRod(playerHit, playerIndex))", 1)[0]
        first = section.index("host.mayDispatchAutoPotNativeUse(")
        self.assertLess(section.index("host.releaseAutoBlockBeforeAction()"),
                        first)
        self.assertIn("!vetoOriginalScreenBeforeNativeCombat(", section[:first])
        self.assertIn("!vetoOriginalScreenBeforeNativeCombat(",
                      section[first:])
        self.assertIn("Minecraft189AutoPotSourceCast.execute(", section)
        self.assertIn("host.commitAutoPotUseAttempt(originalSlot)", CAST)
        self.assertLess(CAST.index("if (!nativeAdmission.getAsBoolean()) return false;"),
                        CAST.index("nativeRightClick.run();"))
        self.assertIn("CUSTOMMC_OFFICIAL_189_AUTOPOT_ACTUAL_SYNTHETIC_RIGHTCLICK_PASS",
                      HARNESS)
        self.assertIn("CUSTOMMC_OFFICIAL_189_AUTOPOT_REAL_SURVIVAL_HEALTH_GAIN_PASS",
                      HARNESS)


if __name__ == "__main__":
    unittest.main()
