package dev.ubi.policy.domain.exception;

import java.time.Instant;

public class ForwardDatingLimitExceededException extends PolicyDomainException {

    private final Instant effectiveAt;
    private final Instant recordedAt;

    public ForwardDatingLimitExceededException(Instant effectiveAt, Instant recordedAt) {
        super("Effective at %s is more than allowed after recorded at %s".formatted(effectiveAt, recordedAt));
        this.effectiveAt = effectiveAt;
        this.recordedAt = recordedAt;
    }

    public Instant effectiveAt() {
        return effectiveAt;
    }

    public Instant recordedAt() {
        return recordedAt;
    }
}
