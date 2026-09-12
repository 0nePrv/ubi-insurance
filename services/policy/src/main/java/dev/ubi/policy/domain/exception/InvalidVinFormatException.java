package dev.ubi.policy.domain.exception;

public class InvalidVinFormatException extends PolicyDomainException {

    private final String value;

    public InvalidVinFormatException(String value) {
        super("Invalid VIN format for %s".formatted(value));
        this.value = value;
    }

    public String value() {
        return value;
    }
}
