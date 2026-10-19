package dev.vladeezyy.rats.platform;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A loader-neutral config description with the calls of NeoForge's {@code ModConfigSpec} builder that Rats uses
 * (sections, comments, defaults and ranges). Each loader binds the options to its own config file: NeoForge builds a
 * real {@code ModConfigSpec} from it (the same rats-common.toml / rats-client.toml as before), Fabric reads and writes
 * the same TOML itself. An option that is not bound yet gives its default.
 */
public final class ConfigSpec {
    private final List<ConfigValue<?>> values;

    private ConfigSpec(List<ConfigValue<?>> values) {
        this.values = List.copyOf(values);
    }

    public List<ConfigValue<?>> values() {
        return this.values;
    }

    public abstract static sealed class ConfigValue<T> implements Supplier<T> permits BooleanValue, IntValue, DoubleValue {
        private final List<String> path;
        private final List<String> comment;
        private final T defaultValue;
        private volatile Supplier<T> source;

        ConfigValue(List<String> path, List<String> comment, T defaultValue) {
            this.path = List.copyOf(path);
            this.comment = List.copyOf(comment);
            this.defaultValue = defaultValue;
        }

        /** The section names, then the option name */
        public List<String> path() {
            return this.path;
        }

        public List<String> comment() {
            return this.comment;
        }

        public T defaultValue() {
            return this.defaultValue;
        }

        /** Binds the option to the loader's config value */
        public void bind(Supplier<T> source) {
            this.source = source;
        }

        @Override
        public T get() {
            Supplier<T> source = this.source;
            return source == null ? this.defaultValue : source.get();
        }
    }

    public static final class BooleanValue extends ConfigValue<Boolean> {
        BooleanValue(List<String> path, List<String> comment, boolean defaultValue) {
            super(path, comment, defaultValue);
        }
    }

    public static final class IntValue extends ConfigValue<Integer> {
        private final int min;
        private final int max;

        IntValue(List<String> path, List<String> comment, int defaultValue, int min, int max) {
            super(path, comment, defaultValue);
            this.min = min;
            this.max = max;
        }

        public int min() {
            return this.min;
        }

        public int max() {
            return this.max;
        }
    }

    public static final class DoubleValue extends ConfigValue<Double> {
        private final double min;
        private final double max;

        DoubleValue(List<String> path, List<String> comment, double defaultValue, double min, double max) {
            super(path, comment, defaultValue);
            this.min = min;
            this.max = max;
        }

        public double min() {
            return this.min;
        }

        public double max() {
            return this.max;
        }
    }

    public static final class Builder {
        private final List<String> sections = new ArrayList<>();
        private final List<ConfigValue<?>> values = new ArrayList<>();
        private List<String> comment = List.of();

        public Builder push(String section) {
            this.sections.add(section);
            return this;
        }

        public Builder pop() {
            this.sections.removeLast();
            return this;
        }

        public Builder pop(int count) {
            for (int i = 0; i < count; i++) {
                this.pop();
            }
            return this;
        }

        public Builder comment(String... lines) {
            this.comment = List.of(lines);
            return this;
        }

        /** NeoForge shows the translation key in its config screen; the files do not store it */
        public Builder translation(String key) {
            return this;
        }

        public BooleanValue define(String name, boolean defaultValue) {
            return this.add(new BooleanValue(this.path(name), this.takeComment(), defaultValue));
        }

        public IntValue defineInRange(String name, int defaultValue, int min, int max) {
            return this.add(new IntValue(this.path(name), this.takeComment(), defaultValue, min, max));
        }

        public DoubleValue defineInRange(String name, double defaultValue, double min, double max) {
            return this.add(new DoubleValue(this.path(name), this.takeComment(), defaultValue, min, max));
        }

        public ConfigSpec build() {
            return new ConfigSpec(this.values);
        }

        /** Like {@code ModConfigSpec.Builder.configure}: builds the config object and the spec of its options */
        public <T> Pair<T, ConfigSpec> configure(Function<Builder, T> consumer) {
            T config = consumer.apply(this);
            return Pair.of(config, this.build());
        }

        private List<String> path(String name) {
            List<String> path = new ArrayList<>(this.sections);
            path.add(name);
            return path;
        }

        private List<String> takeComment() {
            List<String> comment = this.comment;
            this.comment = List.of();
            return comment;
        }

        private <V extends ConfigValue<?>> V add(V value) {
            this.values.add(value);
            return value;
        }
    }
}
