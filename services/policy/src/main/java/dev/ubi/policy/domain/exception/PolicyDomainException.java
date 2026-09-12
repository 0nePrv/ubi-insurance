package dev.ubi.policy.domain.exception;

public abstract class PolicyDomainException extends RuntimeException {

    public PolicyDomainException(String message) {
        super(message);
    }

    public PolicyDomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public PolicyDomainException(Throwable cause) {
        super(cause);
    }
}
