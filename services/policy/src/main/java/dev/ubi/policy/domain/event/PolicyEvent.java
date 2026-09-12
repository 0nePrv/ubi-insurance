package dev.ubi.policy.domain.event;

import dev.ubi.policy.domain.vo.PolicyId;

import java.time.Instant;

public sealed interface PolicyEvent permits DriverEvent, PolicyBindRejected, PolicyCancelled, PolicyDrafted, PolicyIssued, PremiumAdjusted, VehicleReplaced {

    PolicyId policyId();

    Instant effectiveAt();
}
