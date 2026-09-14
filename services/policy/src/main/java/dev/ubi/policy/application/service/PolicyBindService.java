package dev.ubi.policy.application.service;

import dev.ubi.policy.application.PolicyBindingConflictException;
import dev.ubi.policy.application.QuoteExpiredException;
import dev.ubi.policy.application.QuoteNotFoundException;
import dev.ubi.policy.application.QuoteOwnershipException;
import dev.ubi.policy.application.port.in.BindPolicyUseCase;
import dev.ubi.policy.application.port.out.IdempotencyStore;
import dev.ubi.policy.application.port.out.IdempotencyStore.IdempotencyRecord;
import dev.ubi.policy.application.port.out.PolicyRepository;
import dev.ubi.policy.application.port.out.QuoteRepository;
import dev.ubi.policy.application.port.out.VehicleReservationPort;
import dev.ubi.policy.domain.Policy;
import dev.ubi.policy.domain.vo.Money;
import dev.ubi.policy.domain.vo.PolicyId;
import dev.ubi.policy.domain.vo.QuoteId;
import dev.ubi.policy.domain.vo.Term;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.UUID;

@Service
public class PolicyBindService implements BindPolicyUseCase {

    private final IdempotencyStore idempotencyStore;
    private final VehicleReservationPort vehicleReservation;
    private final QuoteRepository quoteRepository;
    private final PolicyRepository policyRepository;
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public PolicyBindService(IdempotencyStore idempotencyStore, VehicleReservationPort vehicleReservation,
                             QuoteRepository quoteRepository, PolicyRepository policyRepository,
                             ApplicationEventPublisher publisher, ObjectMapper objectMapper, Clock clock) {
        this.idempotencyStore = idempotencyStore;
        this.vehicleReservation = vehicleReservation;
        this.quoteRepository = quoteRepository;
        this.policyRepository = policyRepository;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public BindResult bind(BindPolicyCommand command) {
        var recordedAt = clock.instant();
        var commandBytes = objectMapper.writeValueAsBytes(command);
        var commandHash = Sha512DigestUtils.shaHex(commandBytes);
        var existing = idempotencyStore.claim(command.idempotencyKey(), commandHash);
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            return switch (record.status()) {
                case COMPLETED -> objectMapper.readValue(record.responseBody(), BindResult.class);
                case IN_PROGRESS -> throw new PolicyBindingConflictException(command.idempotencyKey());
            };
        }

        QuoteId quoteId = command.quoteId();
        var quote = quoteRepository.findById(quoteId).orElseThrow(() -> new QuoteNotFoundException(quoteId));
        if (quote.isExpired(recordedAt)) {
            throw new QuoteExpiredException(quote.id());
        }
        if (!quote.holderId().equals(command.holderId())) {
            throw new QuoteOwnershipException(quote.id());
        }

        var policyId = new PolicyId(UUID.randomUUID());
        vehicleReservation.reserve(quote.vehicle().vin(), policyId);

        var term = new Term(quote.termStart(), quote.termMode());
        var events = Policy.draft(policyId, term, monthlyOf(quote.annualPremium()),
            quote.vehicle(), quote.drivers(), recordedAt);

        policyRepository.append(policyId, 0, events, recordedAt);
        // outbox появится здесь же, когда дойдём до шага 3

        var result = new BindResult(policyId);
        idempotencyStore.complete(command.idempotencyKey(), 201, objectMapper.writeValueAsString(result));
        publisher.publishEvent(new PolicyBoundEvent(quote.id()));
        return result;
    }

    private Money monthlyOf(Money money) {
        return new Money(money.value().divide(BigDecimal.valueOf(12), RoundingMode.CEILING), money.currency());
    }
}
