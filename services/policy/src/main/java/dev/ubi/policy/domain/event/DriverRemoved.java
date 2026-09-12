package dev.ubi.policy.domain.event;

import dev.ubi.policy.domain.vo.DriverId;
import dev.ubi.policy.domain.vo.PolicyId;

import java.time.Instant;

public record DriverRemoved(PolicyId policyId, Instant effectiveAt, DriverId driverId) implements DriverEvent {
}
