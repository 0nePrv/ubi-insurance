package dev.ubi.policy.application.service;

import dev.ubi.policy.application.port.in.RequestQuoteUseCase;
import dev.ubi.policy.application.port.out.QuoteRepository;
import dev.ubi.policy.application.port.out.RatingPort;
import dev.ubi.policy.domain.Quote;
import dev.ubi.policy.domain.vo.QuoteId;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

@Service
public class PolicyQuotingService implements RequestQuoteUseCase {

    private static final Duration QUOTE_TTL = Duration.ofDays(1);

    private final RatingPort ratingPort;
    private final QuoteRepository repository;
    private final Clock clock;

    public PolicyQuotingService(RatingPort ratingPort, QuoteRepository repository, Clock clock) {
        this.ratingPort = ratingPort;
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public Quote requestQuote(RequestQuoteCommand command) {
        var issuedAt = clock.instant();
        var premium = ratingPort.calculatePremium(
            command.vehicle(), command.drivers().values(), command.termStart().toLocalDate());
        var quote = new Quote(
            new QuoteId(UUID.randomUUID()),
            command.holderId(),
            command.vehicle(),
            command.drivers(),
            command.termStart(),
            command.termMode(),
            premium.annualPremium(),
            premium.tariffVersion(),
            issuedAt,
            issuedAt.plus(QUOTE_TTL)
        );
        repository.save(quote);
        return quote;
    }
}
