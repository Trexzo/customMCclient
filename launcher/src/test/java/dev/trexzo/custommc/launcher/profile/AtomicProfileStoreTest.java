package dev.trexzo.custommc.launcher.profile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class AtomicProfileStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void writeIsDeterministicAndRoundTrips() throws Exception {
        final AtomicProfileStore store = new AtomicProfileStore();
        final Path first = tempDir.resolve("first.profile");
        final Path second = tempDir.resolve("second.profile");

        final Map<String, String> one =
                new LinkedHashMap<String, String>();
        one.put("z.range", "4");
        one.put("a.mode", "legit");

        final Map<String, String> two =
                new LinkedHashMap<String, String>();
        two.put("a.mode", "legit");
        two.put("z.range", "4");

        store.write(first, one);
        store.write(second, two);

        assertArrayEquals(
                Files.readAllBytes(first),
                Files.readAllBytes(second));
        assertEquals(one, store.read(first));
    }

    @Test
    void missingProfileIsAnEmptyProfile() throws Exception {
        final AtomicProfileStore store = new AtomicProfileStore();

        assertEquals(
                java.util.Collections.emptyMap(),
                store.read(tempDir.resolve("missing.profile")));
    }

    @Test
    void duplicateKeysAreRejected() throws Exception {
        final String key = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        "same".getBytes(StandardCharsets.UTF_8));
        final String value = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        "1".getBytes(StandardCharsets.UTF_8));

        final Path profile = tempDir.resolve("duplicate.profile");
        Files.write(
                profile,
                ("custommc-profile-v1\n"
                        + key + "=" + value + "\n"
                        + key + "=" + value + "\n")
                        .getBytes(StandardCharsets.UTF_8));

        assertThrows(
                ProfileFormatException.class,
                () -> new AtomicProfileStore().read(profile));
    }
}
