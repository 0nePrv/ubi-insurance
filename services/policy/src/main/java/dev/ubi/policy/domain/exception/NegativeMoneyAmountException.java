package dev.ubi.policy.domain.exception;

import java.math.BigDecimal;

public class NegativeMoneyAmountException extends PolicyDomainException {

    private final BigDecimal value;

    public NegativeMoneyAmountException(BigDecimal value) {
        super("Money amount can not be negative: %s".formatted(value.toPlainString()));
        this.value = value;
    }

    public BigDecimal amount() {
        return value;
    }
}
