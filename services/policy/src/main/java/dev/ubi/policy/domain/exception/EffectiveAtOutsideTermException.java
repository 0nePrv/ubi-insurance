package dev.ubi.policy.domain.exception;

import dev.ubi.policy.domain.vo.Term;

import java.time.Instant;

public class EffectiveAtOutsideTermException extends PolicyDomainException {

    private final Instant effectiveAt;
    private final Term term;

    public EffectiveAtOutsideTermException(Instant effectiveAt, Term term) {
        super("Effective date time for instant %s does not match term %s".formatted(effectiveAt, term));
        this.effectiveAt = effectiveAt;
        this.term = term;
    }

    public Instant effectiveAt() {
        return effectiveAt;
    }

    public Term term() {
        return term;
    }
}
