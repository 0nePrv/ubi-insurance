package dev.ubi.policy.domain.vo;

import java.time.Period;

public enum TermMode {
    YEAR(Period.ofYears(1)),
    SIX_MONTH(Period.ofMonths(6))
    ;

    private final Period period;

    TermMode(Period period) {
        this.period = period;
    }

    public Period period() {
        return period;
    }
}
