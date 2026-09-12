package dev.ubi.policy.domain.exception;

public class DriverRequiredException extends PolicyDomainException {

    public DriverRequiredException() {
        super("At least one driver have to be included");
    }
}
