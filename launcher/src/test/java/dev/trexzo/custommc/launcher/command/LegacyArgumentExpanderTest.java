package dev.trexzo.custommc.launcher.command;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class LegacyArgumentExpanderTest {
    @Test
    void tokenizesBeforeExpansionSoReplacementSpacesStayTogether()
            throws Exception {
        final Map<String, String> values =
                new LinkedHashMap<String, String>();
        values.put(
                "${user_properties}",
                "{\"label\":\"hello world\"}");

        final ExpandedArguments result =
                new LegacyArgumentExpander().expand(
                        "--title \"Hello world\" "
                                + "--userProperties ${user_properties}",
                        values,
                        Collections.<String>emptySet());

        assertEquals(
                java.util.Arrays.asList(
                        "--title",
                        "Hello world",
                        "--userProperties",
                        "{\"label\":\"hello world\"}"),
                result.arguments());
    }

    @Test
    void rejectsUnknownPlaceholders() {
        assertThrows(
                LaunchCommandException.class,
                () -> new LegacyArgumentExpander().expand(
                        "--unknown ${missing}",
                        Collections.<String, String>emptyMap(),
                        Collections.<String>emptySet()));
    }

    @Test
    void rejectsUnterminatedQuotes() {
        assertThrows(
                LaunchCommandException.class,
                () -> new LegacyArgumentExpander().expand(
                        "--title \"broken",
                        Collections.<String, String>emptyMap(),
                        Collections.<String>emptySet()));
    }
}
