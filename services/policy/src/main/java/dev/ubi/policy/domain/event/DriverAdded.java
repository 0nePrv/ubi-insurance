package dev.ubi.policy.domain.event;

import dev.ubi.policy.domain.vo.Driver;
import dev.ubi.policy.domain.vo.DriverId;
import dev.ubi.policy.domain.vo.PolicyId;

import java.time.Instant;

public record DriverAdded(PolicyId policyId, Instant effectiveAt, DriverId driverId, Driver driver) implements DriverEvent {
}
