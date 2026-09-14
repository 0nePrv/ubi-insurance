package dev.ubi.policy.application;

import dev.ubi.policy.domain.vo.PolicyId;

public class PolicyNotFoundException extends RuntimeException {

    private final PolicyId id;

    public PolicyNotFoundException(PolicyId id) {
        super("Policy not found for id %s".formatted(id));
        this.id = id;
    }

    public PolicyId id() {
        return id;
    }
}
