package dev.trexzo.custommc.launcher.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class MinecraftLaunchTemplate {
    private final String versionId;
    private final String mainClass;
    private final String legacyGameArguments;
    private final String assetIndexId;
    private final List<ResolvedArtifact> classpath;
    private final List<NativeArchive> nativeArchives;

    public MinecraftLaunchTemplate(
            final String versionId,
            final String mainClass,
            final String legacyGameArguments,
            final String assetIndexId,
            final List<ResolvedArtifact> classpath,
            final List<NativeArchive> nativeArchives) {
        this.versionId = Objects.requireNonNull(
                versionId,
                "versionId");
        this.mainClass = Objects.requireNonNull(
                mainClass,
                "mainClass");
        this.legacyGameArguments = Objects.requireNonNull(
                legacyGameArguments,
                "legacyGameArguments");
        this.assetIndexId = Objects.requireNonNull(
                assetIndexId,
                "assetIndexId");
        this.classpath = Collections.unmodifiableList(
                new ArrayList<ResolvedArtifact>(
                        Objects.requireNonNull(
                                classpath,
                                "classpath")));
        this.nativeArchives = Collections.unmodifiableList(
                new ArrayList<NativeArchive>(
                        Objects.requireNonNull(
                                nativeArchives,
                                "nativeArchives")));
    }

    public String versionId() {
        return versionId;
    }

    public String mainClass() {
        return mainClass;
    }

    public String legacyGameArguments() {
        return legacyGameArguments;
    }

    public String assetIndexId() {
        return assetIndexId;
    }

    public List<ResolvedArtifact> classpath() {
        return classpath;
    }

    public List<NativeArchive> nativeArchives() {
        return nativeArchives;
    }
}
