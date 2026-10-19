package dev.vladeezyy.rats.fabric;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.alexthe666.rats.RatsMod;
import dev.vladeezyy.rats.platform.ConfigSpec;

import net.fabricmc.loader.api.FabricLoader;

/**
 * config/rats-common.toml and rats-client.toml on Fabric, the same files and layout NeoForge writes: read once at start
 * (what is missing or out of range gets its default), then written back with every option (from Natura's port).
 */
final class FabricConfig {
    private FabricConfig() {}

    static void load(ConfigSpec spec, String type) {
        Path file = FabricLoader.getInstance().getConfigDir().resolve(RatsMod.MODID + "-" + type + ".toml");
        Map<String, String> read = new HashMap<>();
        if (Files.exists(file)) {
            try {
                read = parse(Files.readAllLines(file, StandardCharsets.UTF_8));
            } catch (IOException | RuntimeException e) {
                RatsMod.LOGGER.error("Could not read {}, using the defaults", file, e);
            }
        }
        for (ConfigSpec.ConfigValue<?> value : spec.values()) {
            String raw = read.get(String.join(".", value.path()));
            if (value instanceof ConfigSpec.BooleanValue bool) {
                boolean result = raw == null ? bool.defaultValue() : raw.equals("true") || (!raw.equals("false") && bool.defaultValue());
                bool.bind(() -> result);
            } else if (value instanceof ConfigSpec.IntValue integer) {
                int result = integer.defaultValue();
                if (raw != null) {
                    try {
                        int parsed = Integer.parseInt(raw);
                        if (parsed >= integer.min() && parsed <= integer.max()) {
                            result = parsed;
                        }
                    } catch (NumberFormatException ignored) {
                        // the default
                    }
                }
                int bound = result;
                integer.bind(() -> bound);
            } else if (value instanceof ConfigSpec.DoubleValue number) {
                double result = number.defaultValue();
                if (raw != null) {
                    try {
                        double parsed = Double.parseDouble(raw);
                        if (parsed >= number.min() && parsed <= number.max()) {
                            result = parsed;
                        }
                    } catch (NumberFormatException ignored) {
                        // the default
                    }
                }
                double bound = result;
                number.bind(() -> bound);
            }
        }
        try {
            Files.write(file, write(spec), StandardCharsets.UTF_8);
        } catch (IOException e) {
            RatsMod.LOGGER.error("Could not write {}", file, e);
        }
    }

    /** "[a.b]" sections and "key = value" lines; keys may be quoted. Returns "section.key" -> value. */
    private static Map<String, String> parse(List<String> lines) {
        Map<String, String> values = new HashMap<>();
        String section = "";
        for (String line : lines) {
            String trimmed = line.strip();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                section = String.join(".", keys(trimmed.substring(1, trimmed.length() - 1)));
                continue;
            }
            int eq = equalsIndex(trimmed);
            if (eq < 0) {
                continue;
            }
            String key = String.join(".", keys(trimmed.substring(0, eq).strip()));
            String value = trimmed.substring(eq + 1).strip();
            int comment = value.indexOf('#');
            if (comment >= 0) {
                value = value.substring(0, comment).strip();
            }
            values.put(section.isEmpty() ? key : section + "." + key, value);
        }
        return values;
    }

    private static int equalsIndex(String line) {
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                quoted = !quoted;
            } else if (c == '=' && !quoted) {
                return i;
            }
        }
        return -1;
    }

    /** a."b c".d -> [a, b c, d] */
    private static List<String> keys(String dotted) {
        List<String> keys = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (char c : dotted.toCharArray()) {
            if (c == '"') {
                quoted = !quoted;
            } else if (c == '.' && !quoted) {
                keys.add(current.toString().strip());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        keys.add(current.toString().strip());
        return keys;
    }

    private static List<String> write(ConfigSpec spec) {
        List<String> lines = new ArrayList<>();
        String section = null;
        for (ConfigSpec.ConfigValue<?> value : spec.values()) {
            List<String> path = value.path();
            String valueSection = String.join(".", path.subList(0, path.size() - 1).stream().map(FabricConfig::quote).toList());
            if (!valueSection.equals(section)) {
                if (section != null) {
                    lines.add("");
                }
                lines.add("[" + valueSection + "]");
                section = valueSection;
            }
            for (String comment : value.comment()) {
                lines.add("\t#" + comment);
            }
            if (value instanceof ConfigSpec.DoubleValue number) {
                lines.add("\t#Range: " + number.min() + " ~ " + number.max());
            }
            if (value instanceof ConfigSpec.IntValue integer) {
                lines.add("\t#Range: " + (integer.min() == Integer.MIN_VALUE ? "< " + integer.max()
                        : integer.max() == Integer.MAX_VALUE ? "> " + integer.min() : integer.min() + " ~ " + integer.max()));
            }
            lines.add("\t" + quote(path.getLast()) + " = " + value.get());
        }
        return lines;
    }

    private static String quote(String key) {
        return key.matches("[A-Za-z0-9_-]+") ? key : "\"" + key + "\"";
    }
}
