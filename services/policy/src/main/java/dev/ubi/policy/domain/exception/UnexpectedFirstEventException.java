package dev.ubi.policy.domain.exception;

import dev.ubi.policy.domain.event.PolicyEvent;

public class UnexpectedFirstEventException extends PolicyDomainException {

    private final PolicyEvent first;

    public UnexpectedFirstEventException(PolicyEvent first) {
        super("Unexpected first event: %s".formatted(first));
        this.first = first;
    }

    public PolicyEvent firstEvent() {
        return first;
    }
}
