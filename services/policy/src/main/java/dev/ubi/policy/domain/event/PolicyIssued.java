package dev.ubi.policy.domain.event;

import dev.ubi.policy.domain.vo.PolicyId;

import java.time.Instant;

public record PolicyIssued(PolicyId policyId, Instant effectiveAt) implements PolicyEvent {
}
