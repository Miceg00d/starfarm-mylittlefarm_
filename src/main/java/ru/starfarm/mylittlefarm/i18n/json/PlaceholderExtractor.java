package ru.starfarm.mylittlefarm.i18n.json;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class PlaceholderExtractor {
    private final static Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    public static List<String> extractPlaceholders(List<String> input) {
        List<String> PlaceholderType = new ArrayList<>();
        for (String string : input) {
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(string);
            while (matcher.find()) {
                PlaceholderType.add(matcher.group(1));
            }
        }
        return PlaceholderType;
    }

    public static List<PlaceholderType> mapToEnum(List<String> extractedPlaceholderType) {
        if (extractedPlaceholderType.isEmpty()) {
            return List.of();
        }
        List<PlaceholderType> matchedEnums = new ArrayList<>();
        for (String placeholder : extractedPlaceholderType) {
            try {
                PlaceholderType enumValue = PlaceholderType.valueOf(placeholder.toUpperCase());
                matchedEnums.add(enumValue);
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown placeholder: " + placeholder);
            }
        }
        return matchedEnums;
    }

    /**
     * Replaces all placeholders in a list of strings using the provided map of placeholder values.
     *
     * @param inputList      The list of strings containing placeholders.
     * @param placeholderMap A map where the key is the placeholder (enum) and the value is the string to replace it with.
     * @return A new list of strings with all placeholders replaced by their corresponding values.
     */
    public static List<String> replacePlaceholdersInList(List<String> inputList, Map<PlaceholderType, String> placeholderMap) {
        return inputList.stream()
                .map(line -> replacePlaceholders(line, placeholderMap))
                .collect(Collectors.toList());
    }

    /**
     * Replaces all PlaceholderType in a single string using the provided map of placeholder values.
     *
     * @param input          The input string containing PlaceholderType.
     * @param placeholderMap A map where the key is the placeholder (enum) and the value is the string to replace it with.
     * @return The string with all PlaceholderType replaced by their corresponding values.
     */
    private static String replacePlaceholders(String input, Map<PlaceholderType, String> placeholderMap) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(input);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String placeholderKey = matcher.group(1); // Get the placeholder name without ${ and }
            PlaceholderType placeholderEnum = PlaceholderType.of(placeholderKey);

            String replacement;
            if (!placeholderMap.containsKey(placeholderEnum)) {
                log.error("Missing replacement for placeholder: " + placeholderKey);
                replacement = "§cN/A";
            } else {
                replacement = placeholderMap.get(placeholderEnum);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
