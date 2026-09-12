package dev.ubi.policy.domain.exception;

import java.time.Instant;

public class BackdatedEndorsementException extends PolicyDomainException {

    private final Instant effectiveAt;
    private final Instant recordedAt;

    public BackdatedEndorsementException(Instant effectiveAt, Instant recordedAt) {
        super("Effective at %s is before recorded at %s".formatted(effectiveAt, recordedAt));
        this.effectiveAt = effectiveAt;
        this.recordedAt = recordedAt;
    }

    public Instant recordedAt() {
        return recordedAt;
    }

    public Instant effectiveAt() {
        return effectiveAt;
    }
}
