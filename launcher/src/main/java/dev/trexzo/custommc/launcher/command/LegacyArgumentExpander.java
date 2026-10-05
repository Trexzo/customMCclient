package dev.trexzo.custommc.launcher.command;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class LegacyArgumentExpander {
    private static final Pattern UNRESOLVED =
            Pattern.compile("\\$\\{[^}]+}");

    public ExpandedArguments expand(
            final String template,
            final Map<String, String> replacements,
            final Set<String> sensitivePlaceholders)
            throws LaunchCommandException {
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(replacements, "replacements");
        Objects.requireNonNull(
                sensitivePlaceholders,
                "sensitivePlaceholders");

        final List<String> tokens = tokenize(template);
        final List<String> expanded =
                new ArrayList<String>(tokens.size());
        final Set<Integer> sensitiveIndexes =
                new LinkedHashSet<Integer>();

        for (int index = 0; index < tokens.size(); index++) {
            final String token = tokens.get(index);
            boolean sensitive = false;

            for (String placeholder
                    : sensitivePlaceholders) {
                if (token.contains(placeholder)) {
                    sensitive = true;
                    break;
                }
            }

            String value = token;
            for (Map.Entry<String, String> replacement
                    : replacements.entrySet()) {
                value = value.replace(
                        replacement.getKey(),
                        Objects.requireNonNull(
                                replacement.getValue(),
                                replacement.getKey()));
            }

            if (UNRESOLVED.matcher(value).find()) {
                throw new LaunchCommandException(
                        "unresolved launch placeholder in token: "
                                + token);
            }

            expanded.add(value);
            if (sensitive) {
                sensitiveIndexes.add(
                        Integer.valueOf(index));
            }
        }

        return new ExpandedArguments(
                expanded,
                sensitiveIndexes);
    }

    private static List<String> tokenize(
            final String template)
            throws LaunchCommandException {
        final List<String> tokens =
                new ArrayList<String>();
        final StringBuilder current =
                new StringBuilder();

        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        boolean escaping = false;
        boolean tokenStarted = false;

        for (int index = 0;
             index < template.length();
             index++) {
            final char character =
                    template.charAt(index);

            if (escaping) {
                current.append(character);
                escaping = false;
                tokenStarted = true;
                continue;
            }

            if (character == '\\') {
                escaping = true;
                tokenStarted = true;
                continue;
            }

            if (character == '\''
                    && !doubleQuoted) {
                singleQuoted = !singleQuoted;
                tokenStarted = true;
                continue;
            }

            if (character == '"'
                    && !singleQuoted) {
                doubleQuoted = !doubleQuoted;
                tokenStarted = true;
                continue;
            }

            if (Character.isWhitespace(character)
                    && !singleQuoted
                    && !doubleQuoted) {
                if (tokenStarted) {
                    tokens.add(current.toString());
                    current.setLength(0);
                    tokenStarted = false;
                }
                continue;
            }

            current.append(character);
            tokenStarted = true;
        }

        if (escaping) {
            throw new LaunchCommandException(
                    "launch arguments end with an escape");
        }
        if (singleQuoted || doubleQuoted) {
            throw new LaunchCommandException(
                    "launch arguments contain an unterminated quote");
        }
        if (tokenStarted) {
            tokens.add(current.toString());
        }

        return tokens;
    }
}
