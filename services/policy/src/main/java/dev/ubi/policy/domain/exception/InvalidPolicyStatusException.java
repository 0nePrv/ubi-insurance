package dev.ubi.policy.domain.exception;

import dev.ubi.policy.domain.vo.PolicyStatus;

import java.util.Collection;
import java.util.List;

public class InvalidPolicyStatusException extends PolicyDomainException {

    private final PolicyStatus actual;
    private final Collection<PolicyStatus> expected;

    public InvalidPolicyStatusException(PolicyStatus actual, Collection<PolicyStatus> expected) {
        super("Expected policy status in %s but was %s".formatted(expected, actual));
        this.actual = actual;
        this.expected = expected;
    }

    public InvalidPolicyStatusException(PolicyStatus actual, PolicyStatus expected) {
        super("Expected policy status %s but was %s".formatted(expected, actual));
        this.actual = actual;
        this.expected = List.of(expected);
    }

    public PolicyStatus actual() {
        return actual;
    }

    public Collection<PolicyStatus> expected() {
        return expected;
    }
}
