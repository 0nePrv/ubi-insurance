package dev.ubi.policy.application;

import dev.ubi.policy.domain.vo.QuoteId;

public class QuoteExpiredException extends RuntimeException {

    private final QuoteId quoteId;

    public QuoteExpiredException(QuoteId quoteId) {
        super("Quote with id %s expired".formatted(quoteId));
        this.quoteId = quoteId;
    }

    public QuoteId quoteId() {
        return quoteId;
    }
}
