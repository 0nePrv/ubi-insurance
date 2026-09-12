package dev.ubi.policy.domain.event;

import dev.ubi.policy.domain.vo.*;

import java.time.Instant;
import java.util.Map;

public record PolicyDrafted(PolicyId policyId, Instant effectiveAt, Term term, Money premium, Vehicle vehicle, Map<DriverId, Driver> drivers) implements PolicyEvent {

    public PolicyDrafted {
        drivers = Map.copyOf(drivers);   // defensive copy
    }
}
