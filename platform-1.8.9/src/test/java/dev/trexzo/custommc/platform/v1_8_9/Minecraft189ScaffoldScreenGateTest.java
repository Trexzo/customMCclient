package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Pure reflection proof; real original-Mojang Laxu; field is checked in CI. */
final class Minecraft189ScaffoldScreenGateTest {
    private static final class Screen { }
    private static final class OriginalLike {
        private Screen screen;
        private final String unrelated="not a Minecraft screen";
    }
    private static final class NoScreen {
        private String unrelated="not a Minecraft screen";
    }
    private static final class Ambiguous {
        private Screen a,b;
    }
    private static final class Other {
        private Object screen;
    }

    @Test void nullVanillaScreenPermitsOnlyExistingIdentifiedField() {
        OriginalLike game=new OriginalLike();
        String guiType=Screen.class.getName();
        assertEquals(Minecraft189ScaffoldScreenGate.Verdict.CLEAR,
                Minecraft189ScaffoldScreenGate.inspectFieldOfType(game,guiType));
        game.screen=new Screen();
        assertEquals(Minecraft189ScaffoldScreenGate.Verdict.OPEN,
                Minecraft189ScaffoldScreenGate.inspectFieldOfType(game,guiType));
        game.screen=null;
        assertEquals(Minecraft189ScaffoldScreenGate.Verdict.CLEAR,
                Minecraft189ScaffoldScreenGate.inspectFieldOfType(game,guiType));
    }

    @Test void unknownOrAmbiguousOriginalGuiFailsShut() {
        String guiType=Screen.class.getName();
        assertEquals(Minecraft189ScaffoldScreenGate.Verdict.UNAVAILABLE,
                Minecraft189ScaffoldScreenGate.inspect(null));
        assertEquals(Minecraft189ScaffoldScreenGate.Verdict.UNAVAILABLE,
                Minecraft189ScaffoldScreenGate.inspect(new OriginalLike()),
                "synthetic or non-original Minecraft host is never authorized");
        assertEquals(Minecraft189ScaffoldScreenGate.Verdict.UNAVAILABLE,
                Minecraft189ScaffoldScreenGate.inspectFieldOfType(null,guiType));
        assertEquals(Minecraft189ScaffoldScreenGate.Verdict.UNAVAILABLE,
                Minecraft189ScaffoldScreenGate.inspectFieldOfType(new NoScreen(),guiType));
        assertEquals(Minecraft189ScaffoldScreenGate.Verdict.UNAVAILABLE,
                Minecraft189ScaffoldScreenGate.inspectFieldOfType(new Ambiguous(),guiType));
        assertEquals(Minecraft189ScaffoldScreenGate.Verdict.UNAVAILABLE,
                Minecraft189ScaffoldScreenGate.inspectFieldOfType(new Other(),guiType));
        assertEquals(Minecraft189ScaffoldScreenGate.Verdict.UNAVAILABLE,
                Minecraft189ScaffoldScreenGate.inspectFieldOfType(new OriginalLike(),""));
    }
}
