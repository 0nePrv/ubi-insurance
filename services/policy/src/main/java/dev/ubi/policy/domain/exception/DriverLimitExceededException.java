package dev.ubi.policy.domain.exception;

public class DriverLimitExceededException extends PolicyDomainException {

    private final int value;

    public DriverLimitExceededException(int value) {
        super("Driver limit exceeded: %s".formatted(value));
        this.value = value;
    }

    public int value() {
        return value;
    }
}
