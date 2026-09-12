package dev.ubi.policy.domain.event;

import dev.ubi.policy.domain.vo.PolicyId;

import java.time.Instant;

public record PolicyCancelled(PolicyId policyId, Instant effectiveAt, String reason) implements PolicyEvent {
}
