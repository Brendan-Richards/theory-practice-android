package com.example.theorypractice;

import java.util.ArrayList;
import java.util.List;

final class SimpleTomlParser {
    TheoryConfig parse(String toml) {
        TheoryConfig config = new TheoryConfig();
        String[] lines = toml.split("\\R");
        for (String rawLine : lines) {
            String line = stripComment(rawLine).trim();
            if (line.isEmpty() || !line.contains("=")) {
                continue;
            }

            String[] parts = line.split("=", 2);
            String key = parts[0].trim();
            String value = parts[1].trim();

            if ("roots".equals(key)) {
                config.roots.addAll(parseStringList(value));
            } else if ("intervals".equals(key)) {
                config.intervals.addAll(parseStringList(value));
            } else if ("directions".equals(key)) {
                config.directions.addAll(parseStringList(value));
            } else if ("chord_types".equals(key)) {
                config.chordTypes.addAll(parseStringList(value));
            } else if ("low_strings".equals(key)) {
                config.lowStrings.addAll(parseStringList(value));
            } else if ("inversions".equals(key)) {
                config.inversions.addAll(parseIntegerList(value));
            }
        }
        return config;
    }

    private String stripComment(String line) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (current == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (current == '#' && !inSingleQuote && !inDoubleQuote) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private List<String> parseStringList(String value) {
        List<String> parsed = new ArrayList<>();
        for (String item : parseListItems(value)) {
            String cleaned = item.trim();
            if ((cleaned.startsWith("'") && cleaned.endsWith("'"))
                    || (cleaned.startsWith("\"") && cleaned.endsWith("\""))) {
                cleaned = cleaned.substring(1, cleaned.length() - 1);
            }
            if (!cleaned.isEmpty()) {
                parsed.add(cleaned);
            }
        }
        return parsed;
    }

    private List<Integer> parseIntegerList(String value) {
        List<Integer> parsed = new ArrayList<>();
        for (String item : parseListItems(value)) {
            String cleaned = item.trim();
            if (!cleaned.isEmpty()) {
                parsed.add(Integer.parseInt(cleaned));
            }
        }
        return parsed;
    }

    private List<String> parseListItems(String value) {
        List<String> items = new ArrayList<>();
        String trimmed = value.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return items;
        }

        String body = trimmed.substring(1, trimmed.length() - 1);
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < body.length(); i++) {
            char ch = body.charAt(i);
            if (ch == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                current.append(ch);
            } else if (ch == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                current.append(ch);
            } else if (ch == ',' && !inSingleQuote && !inDoubleQuote) {
                items.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        if (current.length() > 0 || !body.trim().isEmpty()) {
            items.add(current.toString());
        }
        return items;
    }
}
