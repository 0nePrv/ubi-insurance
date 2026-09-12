package dev.ubi.policy.domain.exception;

import java.time.Instant;

public class TermStartOutOfRangeException extends PolicyDomainException {

    private final Instant start;
    private final Instant maximumAhead;
    private final Instant recordedAt;

    public TermStartOutOfRangeException(Instant start, Instant maximumAhead, Instant recordedAt) {
        super("Term start %s is outside allowed range [%s, %s]".formatted(start, recordedAt, maximumAhead));
        this.start = start;
        this.maximumAhead = maximumAhead;
        this.recordedAt = recordedAt;
    }

    public Instant start() {
        return start;
    }

    public Instant maximumAhead() {
        return maximumAhead;
    }

    public Instant recordedAt() {
        return recordedAt;
    }
}
