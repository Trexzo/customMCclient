package dev.trexzo.custommc.launcher.natives;

import dev.trexzo.custommc.launcher.integrity.ArtifactVerification;
import dev.trexzo.custommc.launcher.integrity.ArtifactVerifier;
import dev.trexzo.custommc.launcher.metadata.NativeArchive;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class NativeStager {
    private final ArtifactVerifier verifier;

    public NativeStager(final ArtifactVerifier verifier) {
        this.verifier = Objects.requireNonNull(
                verifier,
                "verifier");
    }

    public Path stage(
            final List<NativeArchive> archives,
            final Path stagingParent)
            throws IOException {
        Objects.requireNonNull(archives, "archives");
        Objects.requireNonNull(stagingParent, "stagingParent");

        final Path parent = stagingParent
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(parent);

        final Path staging = Files.createTempDirectory(
                parent,
                "custommc-natives-");
        final Set<Path> outputs = new HashSet<Path>();

        try {
            for (NativeArchive archive : archives) {
                final ArtifactVerification verification =
                        verifier.verify(archive.artifact());
                if (!verification.passes()) {
                    throw new NativeStagingException(
                            "native artifact failed verification: "
                                    + archive.artifact().path()
                                    + " status="
                                    + verification.status());
                }

                extract(
                        archive,
                        staging,
                        outputs);
            }
            return staging;
        } catch (IOException | RuntimeException failure) {
            try {
                cleanup(staging);
            } catch (IOException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    public static void cleanup(final Path staging)
            throws IOException {
        Objects.requireNonNull(staging, "staging");
        deleteTree(
                staging.toAbsolutePath().normalize());
    }

    private static void extract(
            final NativeArchive archive,
            final Path staging,
            final Set<Path> outputs)
            throws IOException {
        try (ZipFile zip = new ZipFile(
                archive.artifact().path().toFile())) {
            final Enumeration<? extends ZipEntry> entries =
                    zip.entries();

            while (entries.hasMoreElements()) {
                final ZipEntry entry =
                        entries.nextElement();
                final String name = entry.getName();

                if (isExcluded(
                        name,
                        archive.exclusions())) {
                    continue;
                }
                if (name.startsWith("/")
                        || name.contains("\\")) {
                    throw new NativeStagingException(
                            "invalid native entry path: "
                                    + name);
                }

                final Path destination =
                        staging.resolve(name)
                                .normalize();
                if (!destination.startsWith(staging)) {
                    throw new NativeStagingException(
                            "native entry escapes staging root: "
                                    + name);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(destination);
                    continue;
                }

                if (!outputs.add(destination)) {
                    throw new NativeStagingException(
                            "duplicate native output: "
                                    + name);
                }

                final Path parent = destination.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }

                try (InputStream input =
                        zip.getInputStream(entry)) {
                    Files.copy(input, destination);
                }
            }
        }
    }

    private static boolean isExcluded(
            final String entryName,
            final List<String> exclusions) {
        for (String exclusion : exclusions) {
            final String normalized =
                    exclusion.replace('\\', '/');
            if (entryName.startsWith(normalized)) {
                return true;
            }
        }
        return false;
    }

    private static void deleteTree(final Path root)
            throws IOException {
        if (!Files.exists(root)) {
            return;
        }

        final List<Path> paths = new ArrayList<Path>();
        try (java.util.stream.Stream<Path> stream =
                Files.walk(root)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(paths::add);
        }

        IOException firstFailure = null;
        for (Path path : paths) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException failure) {
                if (firstFailure == null) {
                    firstFailure = failure;
                } else {
                    firstFailure.addSuppressed(failure);
                }
            }
        }

        if (firstFailure != null) {
            throw firstFailure;
        }
    }
}
