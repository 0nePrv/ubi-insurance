package dev.ubi.policy.domain.exception;

import java.util.Currency;

public class IllegalCurrencyException extends PolicyDomainException {

    private final Currency actual;
    private final Currency expected;

    public IllegalCurrencyException(Currency actual, Currency expected) {
        super("Illegal currency: expected %s but was %s".formatted(expected, actual));
        this.actual = actual;
        this.expected = expected;
    }

    public Currency actual() {
        return actual;
    }

    public Currency expected() {
        return expected;
    }
}
