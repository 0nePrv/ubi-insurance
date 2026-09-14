package dev.ubi.policy.application;

import dev.ubi.policy.domain.vo.QuoteId;

public class QuoteOwnershipException extends RuntimeException {

    private final QuoteId id;

    public QuoteOwnershipException(QuoteId id) {
        super("Quote ownership expired for quote with id %s".formatted(id));
        this.id = id;
    }

    public QuoteId id() {
        return id;
    }
}
