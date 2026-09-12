package dev.ubi.policy.domain.event;

import dev.ubi.policy.domain.vo.Money;
import dev.ubi.policy.domain.vo.PolicyId;

import java.time.Instant;

public record PremiumAdjusted(PolicyId policyId, Instant effectiveAt,
                              Money proposed, Money applied,
                              String tariffVersion) implements PolicyEvent {

}
