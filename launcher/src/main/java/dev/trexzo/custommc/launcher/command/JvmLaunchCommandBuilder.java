package dev.trexzo.custommc.launcher.command;

import dev.trexzo.custommc.launcher.metadata.ResolvedArtifact;
import dev.trexzo.custommc.launcher.runtime.OperatingSystem;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class JvmLaunchCommandBuilder {
    private static final String ACCESS_TOKEN_PLACEHOLDER =
            "${auth_access_token}";

    public LaunchCommand build(
            final LaunchRequest request)
            throws LaunchCommandException {
        final Map<String, String> replacements =
                replacements(request);
        final Set<String> sensitivePlaceholders =
                new LinkedHashSet<String>();
        sensitivePlaceholders.add(
                ACCESS_TOKEN_PLACEHOLDER);

        final ExpandedArguments gameArguments =
                new LegacyArgumentExpander().expand(
                        request.template()
                                .legacyGameArguments(),
                        replacements,
                        sensitivePlaceholders);

        final LaunchRuntimeOverlay overlay =
                request.runtimeOverlay();

        final List<String> command =
                new ArrayList<String>();
        command.add(
                request.javaExecutable().toString());
        command.add(
                "-Xms"
                        + request.minimumMemoryMb()
                        + "m");
        command.add(
                "-Xmx"
                        + request.maximumMemoryMb()
                        + "m");
        command.add(
                "-Djava.library.path="
                        + request.nativeDirectory());
        command.add("-cp");
        command.add(classpath(request));
        command.add(
                overlay.mainClass(
                        request.template()
                                .mainClass()));
        command.addAll(
                overlay.mainArgumentsPrefix());

        final int gameArgumentOffset =
                command.size();
        command.addAll(gameArguments.arguments());

        final Set<Integer> sensitiveIndexes =
                new LinkedHashSet<Integer>();
        for (Integer index
                : gameArguments.sensitiveIndexes()) {
            sensitiveIndexes.add(
                    Integer.valueOf(
                            gameArgumentOffset
                                    + index.intValue()));
        }

        return new LaunchCommand(
                command,
                request.gameDirectory(),
                sensitiveIndexes);
    }

    private static Map<String, String> replacements(
            final LaunchRequest request) {
        final Map<String, String> values =
                new LinkedHashMap<String, String>();

        values.put(
                "${auth_player_name}",
                request.identity().playerName());
        values.put(
                "${version_name}",
                request.template().versionId());
        values.put(
                "${game_directory}",
                request.gameDirectory().toString());
        values.put(
                "${assets_root}",
                request.assetsDirectory().toString());
        values.put(
                "${assets_index_name}",
                request.template().assetIndexId());
        values.put(
                "${auth_uuid}",
                request.identity().uuid());
        values.put(
                ACCESS_TOKEN_PLACEHOLDER,
                request.identity().accessToken());
        values.put(
                "${user_properties}",
                request.identity().userProperties());
        values.put(
                "${user_type}",
                request.identity().userType());

        return values;
    }

    private static String classpath(
            final LaunchRequest request) {
        final String separator =
                request.operatingSystem()
                        == OperatingSystem.WINDOWS
                        ? ";"
                        : ":";

        final StringBuilder classpath =
                new StringBuilder();

        for (Path path
                : request.runtimeOverlay()
                .classpathPrefix()) {
            appendClasspath(
                    classpath,
                    separator,
                    path.toString());
        }

        for (ResolvedArtifact artifact
                : request.template().classpath()) {
            appendClasspath(
                    classpath,
                    separator,
                    artifact.path().toString());
        }

        return classpath.toString();
    }

    private static void appendClasspath(
            final StringBuilder classpath,
            final String separator,
            final String entry) {
        if (classpath.length() > 0) {
            classpath.append(separator);
        }
        classpath.append(entry);
    }
}
