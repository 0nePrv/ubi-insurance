package dev.ubi.policy.domain.vo;

import dev.ubi.policy.domain.exception.IllegalCurrencyException;
import dev.ubi.policy.domain.exception.NegativeMoneyAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal value, Currency currency) {

    public static final Currency RUB = Currency.getInstance("RUB");

    public Money {
        Objects.requireNonNull(currency);
        Objects.requireNonNull(value);
        if (value.signum() < 0) {
            throw new NegativeMoneyAmountException(value);
        }
        value = value.stripTrailingZeros();
    }

    public static Money ofMinor(long value, Currency currency) {
        return new Money(new BigDecimal(value), currency);
    }

    public static Money ofMinorRub(long value) {
        return ofMinor(value, RUB);
    }

    public boolean isZero() {
        return value.signum() == 0;
    }

    public Money withStepLimit(Money proposed, BigDecimal maxStep) {
        if (!currency.equals(proposed.currency)) {
            throw new IllegalCurrencyException(proposed.currency, currency);
        }
        var max = value.multiply(BigDecimal.ONE.add(maxStep));
        if (proposed.value().compareTo(max) > 0) {
            return new Money(max.setScale(0, RoundingMode.FLOOR), currency);
        }
        var min = value.multiply(BigDecimal.ONE.subtract(maxStep));
        if (proposed.value().compareTo(min) < 0) {
            return new Money(min.setScale(0, RoundingMode.CEILING), currency);
        }
        return proposed;
    }
}
