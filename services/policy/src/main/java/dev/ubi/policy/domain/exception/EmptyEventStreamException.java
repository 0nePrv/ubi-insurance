package dev.ubi.policy.domain.exception;

public class EmptyEventStreamException extends PolicyDomainException {

    public EmptyEventStreamException() {
        super("Event stream is empty");
    }
}
