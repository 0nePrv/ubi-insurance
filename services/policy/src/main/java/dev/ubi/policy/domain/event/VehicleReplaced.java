package dev.ubi.policy.domain.event;

import dev.ubi.policy.domain.vo.PolicyId;
import dev.ubi.policy.domain.vo.Vehicle;

import java.time.Instant;

public record VehicleReplaced(PolicyId policyId, Instant effectiveAt, Vehicle vehicle) implements PolicyEvent {
}
