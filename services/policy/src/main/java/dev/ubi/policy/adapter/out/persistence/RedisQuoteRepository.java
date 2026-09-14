package dev.ubi.policy.adapter.out.persistence;

import dev.ubi.policy.application.port.out.QuoteRepository;
import dev.ubi.policy.domain.Quote;
import dev.ubi.policy.domain.vo.QuoteId;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

@Repository
public class RedisQuoteRepository implements QuoteRepository {

    private final RedisOperations<QuoteId, Quote> template;
    private final Clock clock;

    public RedisQuoteRepository(RedisOperations<QuoteId, Quote> template, Clock clock) {
        this.template = template;
        this.clock = clock;
    }

    @Override
    public void save(Quote quote) {
        var ttl = Duration.between(clock.instant(), quote.expiresAt());
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }
        template.opsForValue().set(quote.id(), quote, ttl);
    }

    @Override
    public Optional<Quote> findById(QuoteId id) {
        return Optional.ofNullable(template.opsForValue().get(id));
    }

    @Override
    public boolean remove(QuoteId id) {
        return Boolean.TRUE.equals(template.unlink(id));
    }
}
