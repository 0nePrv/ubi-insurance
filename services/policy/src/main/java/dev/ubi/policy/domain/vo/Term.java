package dev.ubi.policy.domain.vo;

import java.time.Instant;
import java.time.ZonedDateTime;

public record Term(ZonedDateTime startedAt, TermMode mode) {

    public boolean contains(Instant instant) {
        var zonedDateTime = instant.atZone(startedAt.getZone());
        return !zonedDateTime.isBefore(startedAt) && zonedDateTime.isBefore(endExclusive());
    }

    public Instant startInstant() {
        return startedAt.toInstant();
    }

    public ZonedDateTime endExclusive() {
        return startedAt.plus(mode.period());
    }
}
