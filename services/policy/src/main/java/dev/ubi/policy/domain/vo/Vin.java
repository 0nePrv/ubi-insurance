package dev.ubi.policy.domain.vo;

import dev.ubi.policy.domain.exception.InvalidVinFormatException;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record Vin(String value) {

    private static final Pattern PATTERN = Pattern.compile("[A-HJ-NPR-Z0-9]{17}");

    public Vin {
        Objects.requireNonNull(value);
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!PATTERN.matcher(value).matches()) {
            throw new InvalidVinFormatException(value);
        }
    }

    public static Vin of(String raw) {
        return new Vin(raw);
    }
}
