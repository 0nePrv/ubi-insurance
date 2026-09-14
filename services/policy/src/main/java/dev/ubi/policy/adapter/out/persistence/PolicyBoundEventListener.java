package dev.ubi.policy.adapter.out.persistence;

import dev.ubi.policy.application.port.in.BindPolicyUseCase;
import dev.ubi.policy.application.port.out.QuoteRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PolicyBoundEventListener {

    private final QuoteRepository quoteRepository;

    public PolicyBoundEventListener(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPolicyBound(BindPolicyUseCase.PolicyBoundEvent event) {
        quoteRepository.remove(event.quoteId());
    }
}
