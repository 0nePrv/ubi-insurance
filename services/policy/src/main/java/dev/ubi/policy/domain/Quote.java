package dev.ubi.policy.domain;

import dev.ubi.policy.domain.vo.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;

public record Quote(QuoteId id,
                    String holderId,
                    Vehicle vehicle,
                    Map<DriverId, Driver> drivers,
                    ZonedDateTime termStart,
                    TermMode termMode,
                    Money annualPremium,
                    String tariffVersion,
                    Instant issuedAt,
                    Instant expiresAt) {

    public Quote {
        drivers = Map.copyOf(drivers);
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }
}
