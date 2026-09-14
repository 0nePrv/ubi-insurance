package dev.ubi.policy.application;

import dev.ubi.policy.domain.vo.QuoteId;

public class QuoteNotFoundException extends RuntimeException {

    private final QuoteId id;

    public QuoteNotFoundException(QuoteId id) {
        super("Quote not found for id %s".formatted(id));
        this.id = id;
    }

    public QuoteId id() {
        return id;
    }
}
