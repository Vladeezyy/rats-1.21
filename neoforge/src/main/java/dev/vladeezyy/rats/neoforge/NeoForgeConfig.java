package dev.vladeezyy.rats.neoforge;

import dev.vladeezyy.rats.platform.ConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec;

/** A ModConfigSpec built from a common {@link ConfigSpec}, its values bound to it (the same TOML files as before) */
final class NeoForgeConfig {
    private NeoForgeConfig() {}

    static ModConfigSpec build(ConfigSpec spec) {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        for (ConfigSpec.ConfigValue<?> value : spec.values()) {
            if (!value.comment().isEmpty()) {
                builder.comment(value.comment().toArray(String[]::new));
            }
            builder.translation(value.path().getLast());
            if (value instanceof ConfigSpec.BooleanValue bool) {
                bool.bind(builder.define(value.path(), (boolean) bool.defaultValue()));
            } else if (value instanceof ConfigSpec.IntValue integer) {
                integer.bind(builder.defineInRange(value.path(), (int) integer.defaultValue(), integer.min(), integer.max()));
            } else if (value instanceof ConfigSpec.DoubleValue number) {
                number.bind(builder.defineInRange(value.path(), (double) number.defaultValue(), number.min(), number.max()));
            }
        }
        return builder.build();
    }
}
