package dev.ubi.policy.application.port.in;

import dev.ubi.policy.domain.vo.PolicyId;

import java.time.Instant;

public interface CancelPolicyUseCase {

    void cancel(CancelPolicyCommand command);

    record CancelPolicyCommand(PolicyId policyId, String reason, Instant effectiveAt) {

    }
}
