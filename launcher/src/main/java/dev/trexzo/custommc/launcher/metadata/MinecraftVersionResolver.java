package dev.trexzo.custommc.launcher.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.trexzo.custommc.launcher.runtime.MinecraftInstallation;
import dev.trexzo.custommc.launcher.runtime.RuntimeTarget;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class MinecraftVersionResolver {
    public MinecraftLaunchTemplate resolve(
            final MinecraftInstallation installation,
            final RuntimeTarget target)
            throws IOException {
        Objects.requireNonNull(installation, "installation");
        Objects.requireNonNull(target, "target");

        final JsonObject root = readRoot(
                installation.versionJson());

        final String id = requireString(root, "id");
        if (!installation.versionId().equals(id)) {
            throw new VersionMetadataException(
                    "version id mismatch: expected "
                            + installation.versionId()
                            + " but metadata declares " + id);
        }

        final String mainClass =
                requireString(root, "mainClass");
        final String gameArguments =
                requireString(root, "minecraftArguments");
        final String assetIndexId =
                resolveAssetIndexId(root);

        final List<ResolvedArtifact> classpath =
                new ArrayList<ResolvedArtifact>();
        final List<NativeArchive> natives =
                new ArrayList<NativeArchive>();

        final JsonArray libraries =
                requireArray(root, "libraries");
        for (JsonElement element : libraries) {
            final JsonObject library =
                    requireObject(element, "library");

            if (!isAllowed(library, target)) {
                continue;
            }

            final boolean hasNativeMapping =
                    library.has("natives")
                            && library.get("natives").isJsonObject();
            final JsonObject downloads =
                    optionalObject(library, "downloads");

            final JsonObject artifact = downloads == null
                    ? null
                    : optionalObject(downloads, "artifact");

            if (artifact != null) {
                classpath.add(resolveDownloadedArtifact(
                        installation.librariesDirectory(),
                        artifact,
                        null));
            } else if (!hasNativeMapping) {
                classpath.add(new ResolvedArtifact(
                        safeResolve(
                                installation.librariesDirectory(),
                                coordinatePath(
                                        requireString(
                                                library,
                                                "name"),
                                        null)),
                        null,
                        null));
            }

            if (hasNativeMapping) {
                final NativeArchive nativeArchive =
                        resolveNative(
                                installation,
                                library,
                                downloads,
                                target);
                if (nativeArchive != null) {
                    natives.add(nativeArchive);
                }
            }
        }

        final JsonObject rootDownloads =
                optionalObject(root, "downloads");
        final JsonObject clientDownload =
                rootDownloads == null
                        ? null
                        : optionalObject(
                                rootDownloads,
                                "client");

        classpath.add(resolveClientArtifact(
                installation,
                clientDownload));

        return new MinecraftLaunchTemplate(
                id,
                mainClass,
                gameArguments,
                assetIndexId,
                classpath,
                natives);
    }

    private static JsonObject readRoot(final Path versionJson)
            throws IOException {
        try (Reader reader = Files.newBufferedReader(
                versionJson)) {
            final JsonElement parsed =
                    JsonParser.parseReader(reader);
            return requireObject(parsed, "version metadata");
        } catch (JsonParseException
                | IllegalStateException invalid) {
            throw new VersionMetadataException(
                    "invalid version metadata JSON",
                    invalid);
        }
    }

    private static ResolvedArtifact resolveClientArtifact(
            final MinecraftInstallation installation,
            final JsonObject clientDownload)
            throws VersionMetadataException {
        if (clientDownload == null) {
            return new ResolvedArtifact(
                    installation.versionJar(),
                    null,
                    null);
        }

        return new ResolvedArtifact(
                installation.versionJar(),
                optionalString(
                        clientDownload,
                        "sha1"),
                optionalLong(
                        clientDownload,
                        "size"));
    }

    private static NativeArchive resolveNative(
            final MinecraftInstallation installation,
            final JsonObject library,
            final JsonObject downloads,
            final RuntimeTarget target)
            throws VersionMetadataException {
        final JsonObject mapping =
                requireObject(
                        library.get("natives"),
                        "native mapping");
        final JsonElement templateElement =
                mapping.get(
                        target.operatingSystem()
                                .metadataName());

        if (templateElement == null
                || !templateElement.isJsonPrimitive()) {
            return null;
        }

        String classifier =
                templateElement.getAsString();
        if (classifier.contains("${arch}")) {
            classifier = classifier.replace(
                    "${arch}",
                    target.architecture()
                            .classifierToken());
        }

        JsonObject classifierDownload = null;
        if (downloads != null) {
            final JsonObject classifiers =
                    optionalObject(
                            downloads,
                            "classifiers");
            if (classifiers != null) {
                classifierDownload =
                        optionalObject(
                                classifiers,
                                classifier);
            }
        }

        final String fallbackPath =
                coordinatePath(
                        requireString(library, "name"),
                        classifier);

        final ResolvedArtifact artifact =
                classifierDownload == null
                        ? new ResolvedArtifact(
                                safeResolve(
                                        installation
                                                .librariesDirectory(),
                                        fallbackPath),
                                null,
                                null)
                        : resolveDownloadedArtifact(
                                installation
                                        .librariesDirectory(),
                                classifierDownload,
                                fallbackPath);

        return new NativeArchive(
                artifact,
                resolveNativeExclusions(library));
    }

    private static List<String> resolveNativeExclusions(
            final JsonObject library)
            throws VersionMetadataException {
        final List<String> exclusions =
                new ArrayList<String>();
        final JsonObject extract =
                optionalObject(library, "extract");
        if (extract == null) {
            return exclusions;
        }

        final JsonArray excluded =
                optionalArray(extract, "exclude");
        if (excluded == null) {
            return exclusions;
        }

        for (JsonElement element : excluded) {
            if (!element.isJsonPrimitive()) {
                throw new VersionMetadataException(
                        "native exclusion must be a string");
            }
            exclusions.add(element.getAsString());
        }
        return exclusions;
    }

    private static ResolvedArtifact resolveDownloadedArtifact(
            final Path librariesRoot,
            final JsonObject download,
            final String fallbackPath)
            throws VersionMetadataException {
        String relativePath =
                optionalString(download, "path");
        if (relativePath == null) {
            relativePath = fallbackPath;
        }
        if (relativePath == null) {
            throw new VersionMetadataException(
                    "artifact download has no path");
        }

        return new ResolvedArtifact(
                safeResolve(
                        librariesRoot,
                        relativePath),
                optionalString(download, "sha1"),
                optionalLong(download, "size"));
    }

    private static boolean isAllowed(
            final JsonObject library,
            final RuntimeTarget target)
            throws VersionMetadataException {
        final JsonArray rules =
                optionalArray(library, "rules");
        if (rules == null || rules.isEmpty()) {
            return true;
        }

        boolean allowed = false;
        for (JsonElement element : rules) {
            final JsonObject rule =
                    requireObject(element, "rule");
            if (!ruleMatches(rule, target)) {
                continue;
            }

            final String action =
                    requireString(rule, "action");
            if ("allow".equals(action)) {
                allowed = true;
            } else if ("disallow".equals(action)) {
                allowed = false;
            } else {
                throw new VersionMetadataException(
                        "unsupported rule action: "
                                + action);
            }
        }
        return allowed;
    }

    private static boolean ruleMatches(
            final JsonObject rule,
            final RuntimeTarget target)
            throws VersionMetadataException {
        final JsonObject os =
                optionalObject(rule, "os");
        if (os != null) {
            final String name =
                    optionalString(os, "name");
            if (name != null
                    && !target.operatingSystem()
                            .metadataName()
                            .equals(name)) {
                return false;
            }

            final String architecture =
                    optionalString(os, "arch");
            if (architecture != null
                    && !target.architecture()
                            .matchesRule(architecture)) {
                return false;
            }

            final String version =
                    optionalString(os, "version");
            if (version != null) {
                try {
                    if (!Pattern.compile(version)
                            .matcher(target.osVersion())
                            .find()) {
                        return false;
                    }
                } catch (PatternSyntaxException invalid) {
                    throw new VersionMetadataException(
                            "invalid OS version rule: "
                                    + version,
                            invalid);
                }
            }
        }

        final JsonObject features =
                optionalObject(rule, "features");
        if (features != null) {
            for (String key : features.keySet()) {
                final JsonElement required =
                        features.get(key);
                if (!required.isJsonPrimitive()
                        || !required.getAsJsonPrimitive()
                                .isBoolean()) {
                    throw new VersionMetadataException(
                            "feature rule must be boolean: "
                                    + key);
                }

                if (required.getAsBoolean()) {
                    return false;
                }
            }
        }

        return true;
    }

    private static String resolveAssetIndexId(
            final JsonObject root)
            throws VersionMetadataException {
        final JsonObject assetIndex =
                optionalObject(root, "assetIndex");
        if (assetIndex != null) {
            final String id =
                    optionalString(assetIndex, "id");
            if (id != null) {
                return id;
            }
        }
        return requireString(root, "assets");
    }

    private static String coordinatePath(
            final String coordinate,
            final String classifierOverride)
            throws VersionMetadataException {
        String value = coordinate;
        String extension = "jar";

        final int extensionSeparator =
                value.indexOf('@');
        if (extensionSeparator >= 0) {
            extension = value.substring(
                    extensionSeparator + 1);
            value = value.substring(
                    0,
                    extensionSeparator);
        }

        final String[] parts = value.split(":");
        if (parts.length != 3 && parts.length != 4) {
            throw new VersionMetadataException(
                    "unsupported library coordinate: "
                            + coordinate);
        }

        final String group = parts[0];
        final String artifact = parts[1];
        final String version = parts[2];
        final String classifier =
                classifierOverride != null
                        ? classifierOverride
                        : parts.length == 4
                                ? parts[3]
                                : null;

        final StringBuilder fileName =
                new StringBuilder();
        fileName.append(artifact)
                .append('-')
                .append(version);
        if (classifier != null
                && !classifier.isEmpty()) {
            fileName.append('-')
                    .append(classifier);
        }
        fileName.append('.')
                .append(extension);

        return group.replace('.', '/')
                + "/"
                + artifact
                + "/"
                + version
                + "/"
                + fileName;
    }

    private static Path safeResolve(
            final Path root,
            final String relativePath)
            throws VersionMetadataException {
        final Path normalizedRoot =
                root.toAbsolutePath().normalize();
        final Path candidate =
                normalizedRoot.resolve(
                        Paths.get(relativePath))
                        .normalize();

        if (!candidate.startsWith(normalizedRoot)) {
            throw new VersionMetadataException(
                    "artifact escapes library root: "
                            + relativePath);
        }
        return candidate;
    }

    private static JsonObject requireObject(
            final JsonElement element,
            final String description)
            throws VersionMetadataException {
        if (element == null || !element.isJsonObject()) {
            throw new VersionMetadataException(
                    description + " must be an object");
        }
        return element.getAsJsonObject();
    }

    private static JsonObject optionalObject(
            final JsonObject parent,
            final String name)
            throws VersionMetadataException {
        final JsonElement element = parent.get(name);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return requireObject(element, name);
    }

    private static JsonArray requireArray(
            final JsonObject parent,
            final String name)
            throws VersionMetadataException {
        final JsonArray array =
                optionalArray(parent, name);
        if (array == null) {
            throw new VersionMetadataException(
                    "missing array: " + name);
        }
        return array;
    }

    private static JsonArray optionalArray(
            final JsonObject parent,
            final String name)
            throws VersionMetadataException {
        final JsonElement element = parent.get(name);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (!element.isJsonArray()) {
            throw new VersionMetadataException(
                    name + " must be an array");
        }
        return element.getAsJsonArray();
    }

    private static String requireString(
            final JsonObject parent,
            final String name)
            throws VersionMetadataException {
        final String value =
                optionalString(parent, name);
        if (value == null || value.trim().isEmpty()) {
            throw new VersionMetadataException(
                    "missing string: " + name);
        }
        return value;
    }

    private static String optionalString(
            final JsonObject parent,
            final String name)
            throws VersionMetadataException {
        final JsonElement element = parent.get(name);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (!element.isJsonPrimitive()
                || !element.getAsJsonPrimitive()
                        .isString()) {
            throw new VersionMetadataException(
                    name + " must be a string");
        }
        return element.getAsString();
    }

    private static Long optionalLong(
            final JsonObject parent,
            final String name)
            throws VersionMetadataException {
        final JsonElement element = parent.get(name);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (!element.isJsonPrimitive()
                || !element.getAsJsonPrimitive()
                        .isNumber()) {
            throw new VersionMetadataException(
                    name + " must be a number");
        }
        return Long.valueOf(element.getAsLong());
    }
}
