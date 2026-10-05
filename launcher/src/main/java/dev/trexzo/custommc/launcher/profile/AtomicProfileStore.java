package dev.trexzo.custommc.launcher.profile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class AtomicProfileStore {
    private static final String HEADER = "custommc-profile-v1";
    private static final Base64.Encoder ENCODER =
            Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER =
            Base64.getUrlDecoder();

    public void write(
            final Path target,
            final Map<String, String> values)
            throws IOException {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(values, "values");

        final Path absolute = target.toAbsolutePath();
        final Path parent = absolute.getParent();
        if (parent == null) {
            throw new IllegalArgumentException(
                    "profile target has no parent: " + target);
        }

        Files.createDirectories(parent);
        final byte[] bytes = encodeDocument(values);
        final Path temp = Files.createTempFile(
                parent,
                "." + absolute.getFileName().toString() + ".",
                ".tmp");

        try {
            try (FileChannel channel = FileChannel.open(
                    temp,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                channel.write(ByteBuffer.wrap(bytes));
                channel.force(true);
            }

            try {
                Files.move(
                        temp,
                        absolute,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(
                        temp,
                        absolute,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public Map<String, String> read(final Path target)
            throws IOException {
        Objects.requireNonNull(target, "target");
        if (!Files.exists(target)) {
            return Collections.emptyMap();
        }

        final String document = new String(
                Files.readAllBytes(target),
                StandardCharsets.UTF_8);
        return decodeDocument(document);
    }

    private static byte[] encodeDocument(
            final Map<String, String> values) {
        final StringBuilder out = new StringBuilder();
        out.append(HEADER).append('\n');

        final Map<String, String> sorted =
                new TreeMap<String, String>(values);

        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            final String key = Objects.requireNonNull(
                    entry.getKey(),
                    "profile key");
            final String value = Objects.requireNonNull(
                    entry.getValue(),
                    "profile value");

            out.append(encodePart(key))
                    .append('=')
                    .append(encodePart(value))
                    .append('\n');
        }

        return out.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static Map<String, String> decodeDocument(
            final String document) {
        final String[] lines = document.split("\\n", -1);
        if (lines.length == 0 || !HEADER.equals(lines[0])) {
            throw new ProfileFormatException(
                    "unsupported profile header");
        }

        final Map<String, String> values =
                new LinkedHashMap<String, String>();

        for (int index = 1; index < lines.length; index++) {
            final String line = lines[index];
            if (line.isEmpty()) {
                continue;
            }

            final int separator = line.indexOf('=');
            if (separator <= 0) {
                throw new ProfileFormatException(
                        "malformed profile line " + (index + 1));
            }

            final String key = decodePart(
                    line.substring(0, separator));
            final String value = decodePart(
                    line.substring(separator + 1));

            if (values.containsKey(key)) {
                throw new ProfileFormatException(
                        "duplicate profile key: " + key);
            }
            values.put(key, value);
        }

        return Collections.unmodifiableMap(values);
    }

    private static String encodePart(final String value) {
        return ENCODER.encodeToString(
                value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodePart(final String encoded) {
        try {
            return new String(
                    DECODER.decode(encoded),
                    StandardCharsets.UTF_8);
        } catch (IllegalArgumentException invalid) {
            throw new ProfileFormatException(
                    "invalid profile encoding",
                    invalid);
        }
    }
}
