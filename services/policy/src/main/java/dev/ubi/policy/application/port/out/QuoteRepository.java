package dev.ubi.policy.application.port.out;

import dev.ubi.policy.domain.Quote;
import dev.ubi.policy.domain.vo.QuoteId;

import java.util.Optional;

public interface QuoteRepository {

    void save(Quote quote);

    Optional<Quote> findById(QuoteId id);

    boolean remove(QuoteId id);
}
