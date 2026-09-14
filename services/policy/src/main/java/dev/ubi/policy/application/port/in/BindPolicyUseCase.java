package dev.ubi.policy.application.port.in;

import dev.ubi.policy.domain.vo.PolicyId;
import dev.ubi.policy.domain.vo.QuoteId;

public interface BindPolicyUseCase {

    BindResult bind(BindPolicyUseCase.BindPolicyCommand command);

    record BindResult(PolicyId policyId) {}

    record BindPolicyCommand(String holderId,
                             String idempotencyKey,
                             QuoteId quoteId) {}

    record PolicyBoundEvent(QuoteId quoteId) {}
}
